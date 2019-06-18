package de.athox.planning

import de.athox.planning.data.GitHubBackend
import de.athox.planning.ui.Index
import de.athox.planning.ui.api.Rest
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.ContentType
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.accept
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val dataBackend: Any = GitHubBackend("RS485", "LogisticsPipes")
    val index = Index()
    val rest = Rest(dataBackend)

    val server = embeddedServer(Netty, 9080) {
        install(DefaultHeaders)
        install(CallLogging)
        routing {
            static {
                files("src/jsMain/web")
            }
            static {
                files(".")
            }
            accept(ContentType.Text.Html) {
                get("/") {
                    call.respondHtmlTemplate(index) {}
                }
            }

            // REST API
            accept(ContentType.Application.Json) {
                rest(route = this)
            }
        }
    }
    server.start(wait = true)
}
