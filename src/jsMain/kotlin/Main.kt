import kotlin.browser.document
import kotlin.browser.window
import kotlin.collections.set
import kotlin.js.*

class Card(props: dynamic) : React.Component(props) {
    private val endpointUri = this.props.uri as String
    private val onDataFetched = this.props.onDataFetched as (card: CardState) -> Nothing

    private fun refresh() {
        window.fetch(
            endpointUri,
            getRequestOptions()
        ).then {
            if (!it.ok) {
                val responseJson = responseToJson(it)
                error("Fetch GET $endpointUri was not successful: $responseJson")
            }

            return@then it.json()
        }.then {
            val obj = it.asDynamic()
            this.onDataFetched(CardState(obj.uri as String, obj.id as Int, obj.title as String))
        }.catch { console.error(it) }
    }

    override fun componentDidMount() {
        if (!this.props.isInitialized as Boolean) {
            this.refresh()
        }
    }

    private fun renderElements(uri: String, title: dynamic, id: dynamic, body: dynamic): dynamic {
        return React.createElement(
            "div",
            object {
                val className = "card"
            },
            arrayOf(
                React.createElement(
                    "div",
                    object {
                        val key = "$uri#header"
                        val className = "card-header"
                    },
                    arrayOf(
                        React.createElement(
                            "span",
                            object {
                                val key = "$uri#title"
                                val className = "card-title"
                            },
                            title
                        ),
                        React.createElement(
                            "span",
                            object {
                                val key = "$uri#id"
                                val className = "card-title float-right text-muted"
                            },
                            id
                        )
                    )
                ),
                React.createElement(
                    "div",
                    object {
                        val key = "$uri#body"
                        val className = "card-body"
                    },
                    body
                )
            )
        )
    }

    override fun render(): dynamic {
        val uri = this.props.uri as String
        return if (this.props.isInitialized as Boolean) {
            val cardState = this.props.card as CardState
            renderElements(uri, cardState.title, "#${cardState.id}", "")
        } else {
            renderElements(
                uri,
                title = React.createElement("em", object {}, "Loading..."),
                id = "#???",
                body = React.createElement(
                    "div",
                    object {
                        val className = "text-center"
                        val style = object {
                            val display = "flex"
                            val justifyContent = "center"
                            val minHeight = "12em"
                        }
                    },
                    React.createElement(
                        "div",
                        object {
                            val className = "spinner-border"
                            val role = "status"
                            val style = object {
                                val alignSelf = "center"
                            }
                        },
                        React.createElement(
                            "span",
                            object {
                                val className = "sr-only"
                            },
                            "Loading..."
                        )
                    )
                )
            )
        }
    }
}

class CardContainer(props: dynamic) : React.Component(props) {
    init {
        this.state = object {
            val cardIds = emptyArray<Int>()
        }
    }

    override fun render(): dynamic {
        return React.createElement(
            "div",
            object {
                val key = "card-column"
                val className = "card-columns single-column"
            },
            this.props.children
        )
    }
}

class NewButton(props: dynamic) : React.Component(props) {
    override fun render(): dynamic {
        return React.createElement("button", object {
            val className = "btn btn-dark"
            val style = object {
                val marginLeft = this@NewButton.props.marginLeft
                val marginRight = this@NewButton.props.marginRight
            }
            val onClick = { (this@NewButton.props.app as CardApp).newCard() }
        }, "New")
    }
}

class RefreshButton(props: dynamic) : React.Component(props) {
    override fun render(): dynamic {
        return React.createElement("button", object {
            val className = "btn btn-dark"
            val onClick = { (this@RefreshButton.props.app as CardApp).refresh() }
        }, "Refresh")
    }
}

class CardApp(props: dynamic) : React.Component(props) {
    private val endpointUri = "/cards/"

    init {
        this.state = object {
            val isInitialized = false
            val cards = HashMap<Int, CardState?>()
        }
    }

    fun newCard() {
        window.fetch(
            endpointUri,
            postRequestOptions(null)
        ).then {
            if (it.status != 201.toShort()) {
                val responseJson = responseToJson(it)
                error("Fetch POST $endpointUri was not successful: $responseJson")
            }

            this.refresh()
        }.catch { console.error(it) }
    }

    fun refresh() {
        window.fetch(
            endpointUri,
            getRequestOptions()
        ).then {
            if (!it.ok) {
                val responseJson = responseToJson(it)
                error("Fetch GET $endpointUri was not successful: $responseJson")
            }

            return@then it.json() as Promise<*>
        }.then { cardIds ->
            cardIds as Array<*>
            val cardIdArray = Array(cardIds.size) { index -> cardIds[index] as Int }
            this.setState { prevState: dynamic ->
                val previousCards = prevState.cards as HashMap<Int, CardState?>
                return@setState object {
                    val isInitialized = true
                    val cards: HashMap<Int, CardState?> = HashMap(cardIdArray.associate {
                        return@associate if (previousCards.containsKey(it)) {
                            it to previousCards[it]
                        } else it to null
                    })
                }
            }
        }.catch { console.error(it) }
    }

    private fun onCardDataFetched(card: CardState) {
        this.setState { prevState: dynamic ->
            val previousCards = prevState.cards as HashMap<Int, CardState?>
            if (!previousCards.contains(card.id)) {
                console.log("onCardDataFetched with unknown card id ${card.id} was fetched")
                return@setState object {}
            }
            previousCards[card.id] = card
            return@setState object {
                val cards = previousCards
            }
        }
    }

    override fun componentDidMount() {
        this.refresh()
    }

    override fun render(): dynamic {
        return if (this.state.isInitialized as Boolean) {
            val cards = this.state.cards as HashMap<Int, CardState?>
            arrayOf(
                React.createElement(
                    CardContainer::class.js,
                    object {
                        val key = endpointUri
                        val cardIds = cards.keys as Set<Int>
                    },
                    cards.entries.map {
                        val cardProps: dynamic = object {
                            val key = it.key.toString()
                            val uri = "$endpointUri${it.key}"
                            val onDataFetched = { card: CardState -> this@CardApp.onCardDataFetched(card) }
                        }
                        cardProps["isInitialized"] = if (it.value == null) {
                            false
                        } else {
                            cardProps["card"] = it.value
                            true
                        }
                        return@map React.createElement(Card::class.js, cardProps)
                    }.toTypedArray()
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
                            val app: CardApp = this@CardApp
                            val marginRight = "0.5em"
                        }),
                        React.createElement(RefreshButton::class.js, object {
                            val key = "refreshButton"
                            val app: CardApp = this@CardApp
                        })
                    )
                )
            )
        } else {
            val spinnerAttributes: dynamic = object {
                val key = "cardapp-placeholder#spinner"
                val className = "spinner-border ml-auto"
                val role = "status"
            }
            spinnerAttributes["aria-hidden"] = "true"
            React.createElement(
                "div",
                object {
                    val className = "d-flex align-items-center"
                },
                arrayOf(
                    React.createElement(
                        "strong",
                        object {
                            val key = "cardapp-placeholder#loading"
                        },
                        "Loading..."
                    ),
                    React.createElement(
                        "span",
                        object {
                            val key = "cardapp-placeholder#loading-more"
                            val className = "m-1 small text-muted"
                        },
                        "just a little bit more.."
                    ),
                    React.createElement(
                        "div",
                        spinnerAttributes
                    )
                )
            )
        }
    }
}

fun main() {
    ReactDOM.render(
        React.createElement(CardApp::class.js),
        document.getElementById("card-container")
    )
}
