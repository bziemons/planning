class AST

external class commonmark {
    companion object {
        fun Parser(options: Any = definedExternally): MarkdownParser { definedExternally }
    }

    class HtmlRenderer(options: Any = definedExternally) {
        fun render(parsed: AST): String { definedExternally }
    }
}

external class MarkdownParser {
    fun parse(toParse: String): AST { definedExternally }
}
