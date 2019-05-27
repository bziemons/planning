import kotlin.browser.document
import kotlin.browser.window
import kotlin.collections.set
import kotlin.js.*

class Card(props: dynamic) : React.Component(props) {
    private val cardId = this.props.cardId as Int
    private val onMount = this.props.onMount as (cardId: Int) -> Unit

    override fun componentDidMount() {
        this.onMount(cardId)
    }

    private fun renderElements(uid: String, title: dynamic, id: dynamic, body: dynamic): dynamic {
        val cardAnchor = encodeURIComponent(uid)
        return React.createElement(
            "div",
            object {
                val className = "card"
                val id = cardAnchor
            },
            arrayOf(
                React.createElement(
                    "div",
                    object {
                        val key = "$uid#header"
                        val className = "card-header"
                    },
                    arrayOf(
                        React.createElement(
                            "span",
                            object {
                                val key = "$uid#title"
                                val className = "card-title"
                            },
                            if (title === "") "\u00a0" else title
                        ),
                        React.createElement(
                            "a",
                            object {
                                val key = "$uid#id"
                                val href = "#$cardAnchor"
                                val className = "card-title float-right text-muted"
                            },
                            id
                        )
                    )
                ),
                React.createElement(
                    "div",
                    object {
                        val key = "$uid#body"
                        val className = "card-body"
                    },
                    body
                )
            )
        )
    }

    override fun render(): dynamic {
        val uid = this.props.uid as String
        return if (this.props.isInitialized as Boolean) {
            val cardState = this.props.card as CardState
            renderElements(uid, cardState.title, "#${cardState.id}", cardState.body)
        } else {
            renderElements(
                uid,
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
    private val cardsBeingFetched = HashSet<Int>()
    private val cardsOffView = ViewElements(this::onCardVisible)

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
                error("fetch POST $endpointUri was not successful: $responseJson")
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
                error("fetch GET $endpointUri was not successful: $responseJson")
            }

            return@then it.json()
        }.then { cardUris ->
            cardUris as Array<*>
            val cardUriArray = Array(cardUris.size) { index -> cardUris[index] as String }
            this.setState { prevState: dynamic ->
                val previousCards = prevState.cards as HashMap<Int, CardState?>
                val nextCards = HashMap(cardUriArray.associate { cardUri ->
                    val cardId: Int = cardUri.substringAfterLast('/').toInt()
                    if (!previousCards.containsKey(cardId) || previousCards[cardId] == null) {
                        return@associate cardId to null
                    } else {
                        this@CardApp.fetchCard(cardId)
                        return@associate cardId to previousCards[cardId]
                    }
                })
                return@setState object {
                    val isInitialized = true
                    val cards = nextCards
                }
            }
        }.catch { console.error(it) }
    }

    private fun fetchCard(cardId: Int) {
        this.cardsBeingFetched.add(cardId)
        window.fetch(
            "$endpointUri$cardId",
            getRequestOptions()
        ).then {
            if (!it.ok) {
                val responseJson = responseToJson(it)
                error("fetch GET $endpointUri was not successful: $responseJson")
            }

            return@then it.json()
        }.then {
            val obj = it.asDynamic()
            return@then CardState(obj.uri as String, obj.id as Int, obj.title as String, obj.body as String)
        }.then(this::onCardDataFetched).catch {
            this.cardsBeingFetched.remove(cardId)
            console.error(it)
        }
    }

    private fun onCardDataFetched(card: CardState) {
        this.setState { prevState: dynamic ->
            val previousCards = prevState.cards as HashMap<Int, CardState?>
            if (!previousCards.contains(card.id)) {
                console.warn("card data with unknown card id ${card.id} was fetched!")
                return@setState object {}
            }
            previousCards[card.id] = card
            return@setState object {
                val cards = previousCards
            }
        }
    }

    fun getCardUid(cardId: Int): String {
        return "$endpointUri$cardId"
    }

    private fun onCardVisible(cardId: Int) {
        this.fetchCard(cardId)
        this.cardsOffView.remove(cardId)
    }

    override fun componentDidMount() {
        this.refresh()
        this.cardsOffView.embark()
    }

    private fun onCardMount(cardId: Int) {
        val cards = this.state.cards as HashMap<Int, CardState?>
        if (this.cardsBeingFetched.contains(cardId)) {
            this.cardsBeingFetched.remove(cardId)
        } else if (cards[cardId] == null) {
            val uid = this.getCardUid(cardId)
            val element = document.getElementById(encodeURIComponent(uid))
            if (element == null) {
                console.warn("could not find element for card id $cardId!")
                return
            }

            this.cardsOffView.put(cardId, element)
        }
        this.cardsOffView.forceUpdate()
    }

    override fun componentWillUnmount() {
        this.cardsOffView.disembark()
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
                            val cardId = it.key
                            val uid = this@CardApp.getCardUid(it.key) // TODO: card-container uid
                            val uri = "$endpointUri${it.key}"
                            val onMount = this@CardApp::onCardMount
                        }
                        cardProps["isInitialized"] = if (it.value == null) {
                            false
                        } else {
                            cardProps["card"] = it.value
                            true
                        }
                        return@map React.createElement(Card::class.js, cardProps) as Any
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
