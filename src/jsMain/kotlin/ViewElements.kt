import org.w3c.dom.Element
import org.w3c.dom.events.Event
import kotlin.browser.document

class ViewElements(
    private val callback: (id: Int) -> Unit,
    private val offsetX: Int = 300,
    private val offsetY: Int = 1200) {

    private val idToRect = HashMap<Int, Rectangle>()
    private val rectToId = HashMap<Rectangle, Int>()

    fun embark() {
        document.addEventListener(
            "resize",
            this::onViewportChanged,
            object {
                val passive = true
            })
        document.addEventListener(
            "scroll",
            this::onViewportChanged,
            object {
                val passive = true
            })
    }

    fun disembark() {
        document.removeEventListener(
            "resize",
            this::onViewportChanged,
            object {
                val passive = true
            })
        document.removeEventListener(
            "scroll",
            this::onViewportChanged,
            object {
                val passive = true
            })

        this.idToRect.clear()
        this.rectToId.clear()
    }

    fun put(id: Int, element: Element) {
        console.log("ViewElements.put($id, ...)")
        val domRect = element.getBoundingClientRect()
        val rect = Rectangle(
            xStart = domRect.left.toInt(),
            yStart = domRect.top.toInt(),
            xEnd = domRect.right.toInt(),
            yEnd = domRect.bottom.toInt()
        )
        if (idToRect.containsKey(id)) {
            rectToId.remove(idToRect[id])
            idToRect.remove(id)
        }
        if (rectToId.containsKey(rect)) {
            error("Rectangle $rect already in map, won't override!")
        }
        rectToId[rect] = id
        idToRect[id] = rect
    }

    fun remove(id: Int) {
        if (!idToRect.containsKey(id)) {
            error("Id $id is not registered in this ViewElements")
        }
        rectToId.remove(idToRect[id])
        idToRect.remove(id)
    }

    fun forceUpdate() {
        this.onViewportChanged(null)
    }

    private fun onViewportChanged(event: Event?) {
        rectToId.entries.filter {
            true // TODO: check viewport
        }.forEach { this.callback(it.value) }
    }
}
