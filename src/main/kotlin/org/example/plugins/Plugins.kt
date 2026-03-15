package org.example.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
import org.example.models.AppSession

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

fun Application.configureSessions() {
    install(Sessions) {
        cookie<AppSession>("APP_SESSION")
    }
}
