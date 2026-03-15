package org.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import org.example.plugins.configureSerialization
import org.example.plugins.configureSessions
import org.example.routes.apiRoutes
import org.example.routes.rootRoutes
import org.example.services.ImageService
import org.example.services.WatcherService

fun main() {
    val imageService = ImageService()
    val watcherService = WatcherService(imageService)
    
    embeddedServer(Netty, port = 8080) {
        configureSerialization()
        configureSessions()

        routing {
            rootRoutes(imageService, watcherService)
            apiRoutes(imageService)
        }
    }.start(wait = true)
}

