package de.athox.planning

import CardState
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection.TRANSACTION_SERIALIZABLE
import java.util.*

object Cards : IntIdTable() {
    val title = text("title").nullable()
    val body = text("body").nullable()
}

fun main(args: Array<String>) {
    Database.connect("jdbc:sqlite:planning.db", driver = "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Cards)
    }

    val gson = Gson()

    val server = embeddedServer(Netty, 9080) {
        routing {
            static {
                files("src/jsMain/web")
            }
            accept(ContentType.Text.Html) {
                get("/") {
                    call.respondHtml {
                        lang = "en"
                        head {
                            meta {
                                charset = "utf-8"
                            }
                            meta {
                                name = "viewport"
                                content = "width=device-width, initial-scale=1, shrink-to-fit=no"
                            }
                            link {
                                rel = "stylesheet"
                                href = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
                                attributes["integrity"] =
                                    "sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
                                attributes["crossorigin"] = "anonymous"
                            }
                            styleLink("/style.css")
                            title("planning")
                        }
                        body {
                            div("container-fluid") {
                                style = "padding-top: 15px; padding-bottom: 15px;"
                                h1 {
                                    +"Planning"
                                }
                                div {
                                    id = "card-container"
                                    div(classes = "d-flex align-items-center") {
                                        strong {
                                            +"Loading..."
                                        }
                                        span(classes = "m-1 small text-muted") {
                                            +"check your JavaScript blocker, if you have one"
                                        }
                                        div(classes = "spinner-border ml-auto") {
                                            attributes["role"] = "status"
                                            attributes["aria-hidden"] = "true"
                                        }
                                    }
                                }
                            }
                            script {
                                src = "https://code.jquery.com/jquery-3.3.1.slim.min.js"
                                attributes["integrity"] =
                                    "sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"
                                attributes["crossorigin"] = "anonymous"
                            }
                            script {
                                src = "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js"
                                attributes["integrity"] =
                                    "sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1"
                                attributes["crossorigin"] = "anonymous"
                            }
                            script {
                                src = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
                                attributes["integrity"] =
                                    "sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
                                attributes["crossorigin"] = "anonymous"
                            }
                            script {
                                src = "https://unpkg.com/react@16/umd/react.development.js"
                                attributes["crossorigin"] = "anonymous"
                            }
                            script {
                                src = "https://unpkg.com/react-dom@16/umd/react-dom.development.js"
                                attributes["crossorigin"] = "anonymous"
                            }
                            script {
                                src = "/kotlin.js"
                            }
                            script {
                                src = "/planning.js"
                            }
                        }
                    }
                }
            }
            accept(ContentType.Application.Json) {
                get("/cards/") {
                    val cardIds = LinkedList<Int>()
                    transaction {
                        Cards.selectAll().forEach { cardIds.push(it[Cards.id].value) }
                    }
                    call.respondText(
                        gson.toJson(cardIds.map { "/cards/$it" }),
                        contentType = ContentType.Application.Json
                    )
                }
                get("/cards/{id}") {
                    val cardId = call.parameters["id"]!!.toInt()
                    val result = transaction {
                        Cards.select { Cards.id.eq(cardId) }.firstOrNull()
                    }
                    if (result == null) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    val card = CardState(
                        "/cards/$cardId",
                        result[Cards.id].value,
                        result[Cards.title].orEmpty(),
                        result[Cards.body].orEmpty()
                    )
                    call.respondText(
                        gson.toJson(card),
                        contentType = ContentType.Application.Json
                    )
                }
                post("/cards/") {
                    val result = transaction {
                        Cards.insertAndGetId {
                            it[title] = ""
                            it[body] = ""
                        }
                    }
                    val responseObj = object {
                        val id: Int = result.value
                    }
                    call.response.headers.append("Location", "/cards/${responseObj.id}")
                    call.respondText(
                        gson.toJson(responseObj),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Created
                    )
                }
            }
        }
    }
    server.start(wait = true)
}