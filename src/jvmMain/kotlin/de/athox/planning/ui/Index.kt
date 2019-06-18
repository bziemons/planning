package de.athox.planning.ui

import io.ktor.application.call
import io.ktor.html.Template
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.get
import kotlinx.html.*

class Index : Template<HTML> {
    override fun HTML.apply() {
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
                src = "https://cdnjs.cloudflare.com/ajax/libs/commonmark/0.29.0/commonmark.min.js"
                attributes["integrity"] = "sha256-ISvYtCg0IbaL4016ONlJrQQb8hfp9P8PaxQ2G3l1C0c="
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

    operator fun invoke(routing: Routing) {
        routing.accept(ContentType.Text.Html) {
            get("/") {
                call.respondHtmlTemplate(this@Index) {}
            }
        }
    }
}