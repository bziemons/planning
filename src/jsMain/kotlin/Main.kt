import org.w3c.fetch.*
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.*

private fun defaultApiFetchOptions(): RequestInit {
    return RequestInit(
        mode = RequestMode.SAME_ORIGIN,
        credentials = RequestCredentials.OMIT,
        headers = json(Pair("Accept", "application/json")),
        redirect = RequestRedirect.ERROR
    )
}

fun getRequestOptions(): RequestInit {
    val fetchOptions = defaultApiFetchOptions()
    fetchOptions.method = "GET"
    return fetchOptions
}

fun postRequestOptions(data: Any?): RequestInit {
    val fetchOptions = defaultApiFetchOptions()
    fetchOptions.method = "POST"
    if (data != null) {
        fetchOptions.body = JSON.stringify(data)
    }
    return fetchOptions
}

class NewButton(props: dynamic) : React.Component(props) {
    override fun render(): dynamic {
        return React.createElement("button", object {
            val className = "btn btn-dark"
            val style = object {
                val marginLeft = this@NewButton.props.marginLeft
                val marginRight = this@NewButton.props.marginRight
            }
            val onClick = { (this@NewButton.props.cardContainer as CardContainer).newCard() }
        }, "New")
    }
}

class RefreshButton(props: dynamic) : React.Component(props) {
    override fun render(): dynamic {
        return React.createElement("button", object {
            val className = "btn btn-dark"
            val onClick = { (this@RefreshButton.props.cardContainer as CardContainer).refreshCards() }
        }, "Refresh")
    }
}

class Card(props: dynamic) : React.Component(props) {
    init {
        props.id as Int
        this.state = CardState("")
    }

    override fun componentDidMount() {
        val id = this.props.id as Int
        window.fetch(
            "/card/$id",
            getRequestOptions()
        ).then {
            if (!it.ok) {
                val responseJson = responseToJson(it)
                error("Fetch GET /card/$id was not successful: $responseJson")
            }

            return@then it.json()
        }.then {
            this@Card.setState(it)
        }.catch { console.error(it) }
    }

    override fun render(): dynamic {
        return React.createElement(
            "div",
            object {
                val className = "card"
            },
            React.createElement(
                "div", object {
                    val className = "card-header"
                },
                arrayOf(
                    React.createElement(
                        "span",
                        object {
                            val key = "card-" + this@Card.props.id.toString() + "-title"
                            val className = "card-title"
                        },
                        this@Card.state.title
                    ),
                    React.createElement(
                        "span",
                        object {
                            val key = "card-" + this@Card.props.id.toString() + "-id"
                            val className = "card-title float-right"
                        },
                        "#" + this@Card.props.id.toString()
                    )
                )
            )
        )
    }
}

class CardContainer(props: dynamic) : React.Component(props) {
    init {
        this.state = object {
            val cardIds = emptyArray<Int>()
        }
    }

    fun newCard() {
        window.fetch(
            "/card",
            postRequestOptions(null)
        ).then {
            if (it.status != 201.toShort()) {
                val responseJson = responseToJson(it)
                error("Fetch POST /card was not successful: $responseJson")
            }

            this@CardContainer.refreshCards()
        }.catch { console.error(it) }
    }

    fun refreshCards() {
        window.fetch(
            "/cards",
            getRequestOptions()
        ).then {
            if (!it.ok) {
                val responseJson = responseToJson(it)
                error("Fetch GET /cards was not successful: $responseJson")
            }

            return@then it.json()
        }.then {
            val receivedCardIds = it as Array<*>
            val newCardIds = Array(receivedCardIds.size) { index -> receivedCardIds[index] as Int }
            this@CardContainer.setState(object {
                val cardIds = newCardIds
            })
        }.catch { console.error(it) }
    }

    override fun componentDidMount() {
        this.refreshCards()
    }

    private fun renderCards(): dynamic {
        return this.state.cardIds.map { cardId: Int ->
            React.createElement(Card::class.js, object {
                val key = cardId.toString()
                val id = cardId
            })
        }
    }

    override fun render(): dynamic {
        return arrayOf(
            React.createElement(
                "div",
                object {
                    val key = "card-column"
                    val className = "card-columns"
                },
                this.renderCards()
            ),
            React.createElement(
                "div",
                object {
                    val key = "controls"
                    val className = "controls"
                },
                arrayOf(
                    React.createElement(NewButton::class.js, object {
                        val key = "newButton"
                        val cardContainer: CardContainer = this@CardContainer
                        val marginRight = "0.5em"
                    }),
                    React.createElement(RefreshButton::class.js, object {
                        val key = "refreshButton"
                        val cardContainer: CardContainer = this@CardContainer
                    })
                )
            )
        )
    }
}

private fun responseToJson(response: Response): String {
    return try {
        JSON.stringify(response)
    } catch (e: RuntimeException) {
        console.error(response)
        "Could not encode response"
    }
}

fun main() {
    ReactDOM.render(
        React.createElement(CardContainer::class.js),
        document.getElementById("card-container")
    )
}
