@file:Suppress("unused")

import org.w3c.dom.Element

external class React {
    companion object {
        fun createElement(
            node: dynamic,
            props: dynamic = definedExternally,
            content: dynamic = definedExternally
        ): dynamic
    }

    abstract class Component(props: dynamic = definedExternally) {
        companion object {
            fun getDerivedStateFromProps(props: dynamic, state: dynamic): dynamic
        }

        var state: dynamic
        var props: dynamic

        fun setState(state: dynamic)

        fun forceUpdate(callback: dynamic)

        abstract fun render(): dynamic

        open fun componentDidMount()

        open fun componentWillUnmount()

        open fun componentDidUpdate(prevProps: dynamic, prevState: dynamic, snapshot: dynamic)

        open fun shouldComponentUpdate(nextProps: dynamic, nextState: dynamic): Boolean
    }

}

external class ReactDOM {
    companion object {
        fun render(element: dynamic, container: Element?): Element?
    }

}