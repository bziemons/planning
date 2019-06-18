package de.athox.planning.ui.api

import CardState
import com.google.gson.Gson
import de.athox.planning.data.CardNotFoundException
import de.athox.planning.data.CardReadProvider
import de.athox.planning.data.CardWriteProvider
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*

class Rest(dataBackend: Any) {
    private val gson = Gson()

    private val runReader: suspend (call: ApplicationCall, run: suspend CardReadProvider.() -> Unit) -> Unit
    private val runWriter: suspend (call: ApplicationCall, run: suspend CardWriteProvider.() -> Unit) -> Unit

    init {
        val respondNotImplemented: suspend (ApplicationCall, Any) -> Unit =
            { call: ApplicationCall, _ -> call.respond(HttpStatusCode.NotImplemented) }

        this.runReader = if (dataBackend is CardReadProvider) {
            { _, run: suspend CardReadProvider.() -> Unit -> run(dataBackend) }
        } else respondNotImplemented

        this.runWriter = if (dataBackend is CardWriteProvider) {
            { _, run: suspend CardWriteProvider.() -> Unit -> run(dataBackend) }
        } else respondNotImplemented
    }

    operator fun invoke(routing: Routing) {
        routing.route("/rest/v1") {
            accept(ContentType.Application.Json) {
                get("/cards/") {
                    runReader(call) {
                        call.respondText(
                            gson.toJson(allCards().map { "/cards/$it" }.toTypedArray()),
                            contentType = ContentType.Application.Json
                        )
                    }
                }

                get("/cards/{id}") {
                    runReader(call) {
                        val cardId = call.parameters["id"]!!.toInt()
                        val card = try {
                            getCard(cardId)
                        } catch (e: CardNotFoundException) {
                            if (e.message?.isNotEmpty() == true) {
                                call.respond(HttpStatusCode.NotFound, e.message)
                            } else {
                                call.respond(HttpStatusCode.NotFound)
                            }
                            return@runReader
                        }
                        card.uri = "/rest/v1/cards/${card.id}"
                        call.respondText(
                            gson.toJson(card),
                            contentType = ContentType.Application.Json
                        )
                    }
                }

                patch("/cards/{id}") {
                    runWriter(call) {
                        val cardId = call.parameters["id"]!!.toInt()
                        val obj = gson.fromJson<CardState>(call.receiveStream().reader(), CardState::class.java)
                        if (obj.title == null && obj.body == null) {
                            call.respond(HttpStatusCode.BadRequest, "no update item received")
                            return@runWriter
                        }

                        try {
                            updateCard(cardId, obj)
                            call.respond(HttpStatusCode.Accepted)
                        } catch (err: RuntimeException) {
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }
                }

                post("/cards/") {
                    runWriter(call) {
                        val responseObj = object {
                            val id: Int = insertCard()
                        }
                        call.response.headers.append("Location", "/rest/v1/cards/${responseObj.id}")
                        call.respondText(
                            gson.toJson(responseObj),
                            contentType = ContentType.Application.Json,
                            status = HttpStatusCode.Created
                        )
                    }
                }

            }
        }
    }
}