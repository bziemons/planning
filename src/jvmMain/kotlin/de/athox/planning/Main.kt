package de.athox.planning

import de.athox.planning.data.GitHubBackend
import de.athox.planning.ui.Index
import de.athox.planning.ui.api.Rest
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.routing.accept
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    val dataBackend: Any = GitHubBackend("RS485", "LogisticsPipes")
    val index = Index()
    val restApi = Rest(dataBackend)

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

            index(routing = this)
            restApi(routing = this)
        }
    }
    server.start(wait = true)
}
