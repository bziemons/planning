import org.w3c.dom.Element
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window

class ViewElements(
    private val callback: (id: Int) -> Unit,
    private val xEps: Int = 300,
    private val yEps: Int = 1200
) {

    private val idToElement = HashMap<Int, Element>()
    private val elementToId = HashMap<Element, Int>()

    private var windowRect: Rectangle = Rectangle(0, 0, 0, 0)
    private var justResized: Boolean = false
    private var resizeDirty: Boolean = false
    private var justScrolled: Boolean = false
    private var scrollDirty: Boolean = false

    fun embark() {
        window.addEventListener(
            "resize",
            this::onResize,
            object {
                val passive = true
            })
        document.addEventListener(
            "scroll",
            this::onScroll,
            object {
                val passive = true
            })

        this.onResize(null) // initialize window rectangle
    }

    fun disembark() {
        window.removeEventListener(
            "resize",
            this::onResize
        )
        document.removeEventListener(
            "scroll",
            this::onScroll
        )

        this.idToElement.clear()
        this.elementToId.clear()
    }

    fun put(id: Int, element: Element) {
        elementToId[element] = id
        idToElement[id] = element
    }

    fun remove(id: Int) {
        if (!idToElement.containsKey(id)) {
            error("id $id is not registered in this ViewElements")
        }
        elementToId.remove(idToElement[id])
        idToElement.remove(id)
    }

    fun forceUpdate() {
        this.onResize(null)
    }

    private fun onResize(event: Event?) {
        if (this.justResized) {
            this.resizeDirty = true
            return
        }
        this.justResized = true
        this.resizeDirty = false

        val documentElement = document.documentElement!!
        windowRect = Rectangle(
            xStart = documentElement.clientLeft,
            yStart = documentElement.clientTop,
            xEnd = documentElement.clientLeft + documentElement.clientWidth,
            yEnd = documentElement.clientTop + documentElement.clientHeight
        )

        this.onScroll(null)

        window.setTimeout({
            this.justResized = false
            if (this.resizeDirty) {
                this.onResize(null)
            }
        }, 200)
    }

    private fun onScroll(event: Event?) {
        if (this.justScrolled) {
            this.scrollDirty = true
            return
        }
        this.justScrolled = true
        this.scrollDirty = false

        elementToId.keys.filter {
            return@filter windowRect.contains(it, xEps, yEps)
        }.forEach {
            this.callback(elementToId[it]!!)
        }

        window.setTimeout({
            this.justScrolled = false
            if (this.scrollDirty) {
                this.onScroll(null)
            }
        }, 150)
    }

    private fun Rectangle.contains(elem: Element, xEps: Int, yEps: Int): Boolean {
        val domRect = elem.getBoundingClientRect()
        return (this.xStart - xEps) < domRect.right &&
                (this.xEnd + xEps) > domRect.left &&
                (this.yStart - yEps) < domRect.bottom &&
                (this.yEnd + yEps) > domRect.top
    }
}
