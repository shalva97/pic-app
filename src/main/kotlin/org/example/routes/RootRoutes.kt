package org.example.routes

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.example.models.AppSession
import org.example.services.ImageService
import org.example.services.WatcherService
import org.example.ui.Templates.adminPage
import org.example.ui.Templates.indexPage

fun Route.rootRoutes(imageService: ImageService, watcherService: WatcherService) {
    get("/") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respondRedirect("/admin")
        } else {
            watcherService.startWatching(dirPath)
            call.respondHtml { indexPage() }
        }
    }

    get("/admin") {
        val dirPath = imageService.getCurrentImageDir()
        call.respondHtml { adminPage(dirPath) }
    }

    post("/admin") {
        val params = call.receiveParameters()
        val dir = params["dir"] ?: ""
        if (imageService.isValidDirectory(dir)) {
            imageService.setImageDir(dir)
            watcherService.startWatching(dir)
            call.respondRedirect("/")
        } else {
            call.respondText("Invalid directory")
        }
    }
}
