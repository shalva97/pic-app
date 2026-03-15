package org.example.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.example.models.AppSession
import org.example.models.FavoriteRequest
import org.example.models.ImageList
import org.example.models.StarRequest
import org.example.models.FlagRequest
import org.example.services.ImageService

fun Route.apiRoutes(imageService: ImageService) {
    get("/api/images") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(ImageList(emptyList(), 0))
            return@get
        }

        val page = call.parameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 10
        val onlyFavorites = call.parameters["favorites"]?.toBoolean() ?: false
        val onlyStarred = call.parameters["starred"]?.toBoolean() ?: false
        val onlyFlagged = call.parameters["flagged"]?.toBoolean() ?: false
        val sort = call.parameters["sort"] ?: "new"

        val result = imageService.getImages(dirPath, page, pageSize, onlyFavorites, onlyStarred, onlyFlagged, sort)
        call.respond(result)
    }

    get("/api/images/random") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        val onlyFavorites = call.parameters["favorites"]?.toBoolean() ?: false
        val onlyStarred = call.parameters["starred"]?.toBoolean() ?: false
        val onlyFlagged = call.parameters["flagged"]?.toBoolean() ?: false

        val result = imageService.getRandomImage(dirPath, onlyFavorites, onlyStarred, onlyFlagged)
        if (result != null) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post("/api/favorites") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@post
        }
        val request = call.receive<FavoriteRequest>()
        imageService.toggleFavorite(dirPath, request.path, request.isFavorite)
        call.respond(HttpStatusCode.OK)
    }

    post("/api/stars") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@post
        }
        val request = call.receive<StarRequest>()
        imageService.incrementStar(dirPath, request.path)
        call.respond(HttpStatusCode.OK)
    }

    post("/api/flags") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@post
        }
        val request = call.receive<FlagRequest>()
        imageService.toggleFlag(dirPath, request.path, request.isFlagged)
        call.respond(HttpStatusCode.OK)
    }

    get("/api/admin/scan") {
        imageService.clearCaches()
        call.respond(HttpStatusCode.OK)
    }

    get("/images/{filename}") {
        val dirPath = imageService.getCurrentImageDir()
        if (dirPath == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        val filename = call.parameters["filename"]
        if (filename == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val file = imageService.getImageFile(dirPath, filename)

        if (file != null) {
            call.respondFile(file)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
