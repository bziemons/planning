package de.athox.planning.ui.api

import CardState
import com.google.gson.Gson
import de.athox.planning.data.CardNotFoundException
import de.athox.planning.data.CardReadProvider
import de.athox.planning.data.CardWriteProvider
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveStream
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.patch
import io.ktor.routing.post

class Rest(val dataBackend: Any) {
    private val gson = Gson()

    operator fun invoke(route: Route) {
        route.get("/cards/") {
            (dataBackend as? CardReadProvider)?.run {
                call.respondText(
                    gson.toJson(allCards().map { "/cards/$it" }.toTypedArray()),
                    contentType = ContentType.Application.Json
                )
            } ?: run {
                call.respond(HttpStatusCode.NotImplemented)
            }
        }
        route.get("/cards/{id}") {
            (dataBackend as? CardReadProvider)?.run {
                val cardId = call.parameters["id"]!!.toInt()
                val card = try {
                    dataBackend.getCard(cardId)
                } catch (e: CardNotFoundException) {
                    if (e.message?.isNotEmpty() == true) {
                        call.respond(HttpStatusCode.NotFound, e.message)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                    return@get
                }
                card.uri = "/cards/${card.id}"
                call.respondText(
                    gson.toJson(card),
                    contentType = ContentType.Application.Json
                )
            } ?: run {
                call.respond(HttpStatusCode.NotImplemented)
            }
        }
        route.patch("/cards/{id}") {
            (dataBackend as? CardWriteProvider)?.run {
                val cardId = call.parameters["id"]!!.toInt()
                val obj = gson.fromJson<CardState>(call.receiveStream().reader(), CardState::class.java)
                if (obj.title == null && obj.body == null) {
                    call.respond(HttpStatusCode.BadRequest, "no update item received")
                    return@patch
                }

                try {
                    dataBackend.updateCard(cardId, obj)
                    call.respond(HttpStatusCode.Accepted)
                } catch (err: RuntimeException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            } ?: run {
                call.respond(HttpStatusCode.NotImplemented)
            }
        }
        route.post("/cards/") {
            (dataBackend as? CardWriteProvider)?.run {
                val responseObj = object {
                    val id: Int = dataBackend.insertCard()
                }
                call.response.headers.append("Location", "/cards/${responseObj.id}")
                call.respondText(
                    gson.toJson(responseObj),
                    contentType = ContentType.Application.Json,
                    status = HttpStatusCode.Created
                )
            } ?: run {
                call.respond(HttpStatusCode.NotImplemented)
            }
        }
    }
}