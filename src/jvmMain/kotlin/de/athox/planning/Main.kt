package de.athox.planning

import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.response.respondText
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import CardState

fun main(args: Array<String>) {
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
                            title("planning")
                        }
                        body {
                            div("container-fluid") {
                                h1 {
                                    +"Hello Worlds!"
                                }
                                div {
                                    id = "card-container"
                                    +"Still connecting or cannot connect to API endpoint.. check your JavaScript blocker"
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
                get("/cards") {
                    call.respondText("[1,2,3]", contentType = ContentType.Application.Json)
                }
                get("/card/{id}") {
                    val cardId = call.parameters["id"]!!.toInt()
                    val gson = Gson()
                    val card = CardState("Card $cardId")
                    call.respondText(gson.toJson(card), contentType = ContentType.Application.Json)
                }
                post("/card") {
                    call.respondText(
                        "{\"id\":4}",
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.Created
                    )
                }
            }
        }
    }
    server.start(wait = true)
}