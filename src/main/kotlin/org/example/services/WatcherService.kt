package org.example.services

import java.nio.file.*
import kotlin.concurrent.thread

class WatcherService(private val imageService: ImageService) {
    private var watchThread: Thread? = null
    private var currentPath: Path? = null

    fun startWatching(pathString: String) {
        val path = Paths.get(pathString)
        if (currentPath == path) return
        
        stopWatching()
        currentPath = path
        
        watchThread = thread(start = true, isDaemon = true) {
            try {
                val watchService = FileSystems.getDefault().newWatchService()
                path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )

                while (!Thread.currentThread().isInterrupted) {
                    val key = watchService.take()
                    for (event in key.pollEvents()) {
                        // Clear caches when directory changes
                        imageService.clearCaches()
                    }
                    if (!key.reset()) break
                }
            } catch (e: Exception) {
                // Log or handle error
            }
        }
    }

    fun stopWatching() {
        watchThread?.interrupt()
        watchThread = null
        currentPath = null
    }
}
