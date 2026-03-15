package org.example.services

import java.io.File
import org.example.models.ImageList
import org.example.models.ImageMetadata
import net.coobird.thumbnailator.Thumbnails
import java.awt.image.BufferedImage
import java.sql.DriverManager
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.stream.FileImageInputStream

class ImageService {
    private val allowedExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
    private var dbConnection: Connection? = null
    private val thumbnailCache = ConcurrentHashMap<String, File>()
    private val metadataCache = ConcurrentHashMap<String, Pair<Int, Int>>()
    private var currentImageDir: String? = null
    private val configFile = File("config.txt")
    private val dbFile = File("data.db")

    init {
        loadConfig()
        initDb()
    }

    private fun initDb() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
            dbConnection?.createStatement()?.use { stmt ->
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS image_data (
                        path TEXT PRIMARY KEY,
                        is_favorite INTEGER DEFAULT 0,
                        star_count INTEGER DEFAULT 0,
                        is_flagged INTEGER DEFAULT 0,
                        width INTEGER DEFAULT 0,
                        height INTEGER DEFAULT 0,
                        last_modified INTEGER DEFAULT 0
                    )
                """.trimIndent())
                
                // Migrations
                val migrations = listOf(
                    "ALTER TABLE image_data ADD COLUMN is_flagged INTEGER DEFAULT 0",
                    "ALTER TABLE image_data ADD COLUMN width INTEGER DEFAULT 0",
                    "ALTER TABLE image_data ADD COLUMN height INTEGER DEFAULT 0",
                    "ALTER TABLE image_data ADD COLUMN last_modified INTEGER DEFAULT 0"
                )
                
                migrations.forEach { migration ->
                    try {
                        stmt.execute(migration)
                    } catch (e: Exception) {
                        // Column might already exist, ignore
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("[ERROR] Failed to initialize database: ${e.message}")
        }
    }

    private fun getDbConnection(): Connection? {
        if (dbConnection == null || dbConnection!!.isClosed) {
            initDb()
        }
        return dbConnection
    }

    private fun loadConfig() {
        if (configFile.exists()) {
            currentImageDir = configFile.readText().trim()
        }
    }

    private fun saveConfig(path: String) {
        configFile.writeText(path)
        currentImageDir = path
    }

    fun getCurrentImageDir(): String? = currentImageDir

    fun setImageDir(path: String) {
        if (isValidDirectory(path)) {
            saveConfig(path)
            clearCaches()
        }
    }

    fun getImages(dirPath: String, page: Int, pageSize: Int, onlyFavorites: Boolean = false, onlyStarred: Boolean = false, onlyFlagged: Boolean = false, sort: String = "new"): ImageList {
        val filteredImages = getFilteredImages(dirPath, onlyFavorites, onlyStarred, onlyFlagged, sort)
        val totalCount = filteredImages.size
        val fromIndex = (page - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, totalCount)

        return if (fromIndex >= totalCount || fromIndex < 0) {
            ImageList(emptyList(), totalCount)
        } else {
            val pagedImages = filteredImages.subList(fromIndex, toIndex)
            ImageList(pagedImages, totalCount)
        }
    }

    private fun getFilteredImages(dirPath: String, onlyFavorites: Boolean, onlyStarred: Boolean, onlyFlagged: Boolean, sort: String): List<ImageMetadata> {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) {
            return emptyList()
        }

        // Fetch all image files first
        val imageFiles = dir.listFiles { file ->
            file.isFile && allowedExtensions.contains(file.extension.lowercase())
        } ?: emptyArray()

        // Fetch all metadata from DB in one go to improve performance
        val metadataMap = getAllImageData()

        val allImages = imageFiles.map { file ->
            val normalizedPath = file.absolutePath
            // Try exact match first, then case-insensitive
            val data = metadataMap[normalizedPath] 
                ?: metadataMap.entries.find { it.key.equals(normalizedPath, ignoreCase = true) }?.value
                ?: DbImageData(false, 0, false, 0, 0, 0)
                
            val (width, height) = if (data.width > 0 && data.height > 0 && data.lastModifiedDb == file.lastModified()) {
                data.width to data.height
            } else {
                val dims = getDimensions(file)
                updateDimensionsInDb(file, dims)
                dims
            }
            ImageMetadata(file.name, width, height, data.isFavorite, data.starCount, data.isFlagged, file.lastModified())
        }

        val sortedImages = when (sort) {
            "new" -> allImages.sortedByDescending { it.lastModified }
            "old" -> allImages.sortedBy { it.lastModified }
            else -> allImages.sortedBy { it.name }
        }

        var filteredImages = if (onlyFavorites) {
            sortedImages.filter { it.isFavorite }
        } else {
            sortedImages
        }

        if (onlyStarred) {
            filteredImages = filteredImages.filter { it.starCount > 0 }
        }

        if (onlyFlagged) {
            filteredImages = filteredImages.filter { it.isFlagged }
        }
        
        return filteredImages
    }

    fun getRandomImage(dirPath: String, onlyFavorites: Boolean = false, onlyStarred: Boolean = false, onlyFlagged: Boolean = false): ImageMetadata? {
        val filteredImages = getFilteredImages(dirPath, onlyFavorites, onlyStarred, onlyFlagged, "name")
        if (filteredImages.isEmpty()) return null
        return filteredImages.random()
    }

    private fun getAllImageData(): Map<String, DbImageData> {
        val result = mutableMapOf<String, DbImageData>()
        try {
            getDbConnection()?.createStatement()?.use { stmt ->
                val rs = stmt.executeQuery("SELECT path, is_favorite, star_count, is_flagged, width, height, last_modified FROM image_data")
                while (rs.next()) {
                    result[rs.getString("path")] = DbImageData(
                        rs.getInt("is_favorite") == 1,
                        rs.getInt("star_count"),
                        rs.getInt("is_flagged") == 1,
                        rs.getInt("width"),
                        rs.getInt("height"),
                        rs.getLong("last_modified")
                    )
                }
            }
        } catch (e: Exception) {
            System.err.println("[ERROR] Failed to fetch all image data: ${e.message}")
        }
        return result
    }

    private fun updateDimensionsInDb(file: File, dims: Pair<Int, Int>) {
        try {
            getDbConnection()?.prepareStatement("""
                INSERT INTO image_data (path, width, height, last_modified) 
                VALUES (?, ?, ?, ?) 
                ON CONFLICT(path) DO UPDATE SET 
                    width = excluded.width,
                    height = excluded.height,
                    last_modified = excluded.last_modified
            """.trimIndent())?.use { stmt ->
                stmt.setString(1, file.absolutePath)
                stmt.setInt(2, dims.first)
                stmt.setInt(3, dims.second)
                stmt.setLong(4, file.lastModified())
                stmt.executeUpdate()
            }
        } catch (e: Exception) {
            System.err.println("[ERROR] Failed to update dimensions in DB for ${file.name}: ${e.message}")
        }
    }

    private fun getDimensions(file: File): Pair<Int, Int> {
        return metadataCache.getOrPut(file.absolutePath) {
            val extension = file.extension.lowercase()
            val iter = ImageIO.getImageReadersBySuffix(extension)
            if (iter.hasNext()) {
                val reader = iter.next()
                try {
                    FileImageInputStream(file).use { input ->
                        reader.input = input
                        val width = reader.getWidth(reader.minIndex)
                        val height = reader.getHeight(reader.minIndex)
                        width to height
                    }
                } catch (e: Exception) {
                    try {
                        val bimg = ImageIO.read(file)
                        if (bimg != null) bimg.width to bimg.height else 0 to 0
                    } catch (e2: Exception) {
                        0 to 0
                    }
                } finally {
                    reader.dispose()
                }
            } else {
                0 to 0
            }
        }
    }

    fun getThumbnail(dirPath: String, filename: String): File? {
        val originalFile = File(dirPath, filename)
        if (!originalFile.exists() || !originalFile.isFile) return null

        val key = originalFile.absolutePath
        return try {
            thumbnailCache.computeIfAbsent(key) { _ ->
                val tempDir = System.getProperty("java.io.tmpdir")
                // Use .jpg extension as we force JPG output
                val thumbName = "thumb_${originalFile.nameWithoutExtension}.jpg"
                val thumbFile = File(tempDir, thumbName)
                
                if (!thumbFile.exists()) {
                    Thumbnails.of(originalFile)
                        .size(600, 600)
                        .outputFormat("jpg")
                        .toFile(thumbFile)
                }
                thumbFile
            }
        } catch (e: Exception) {
            System.err.println("[ERROR] Failed to generate thumbnail for $filename: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    private data class DbImageData(
        val isFavorite: Boolean, 
        val starCount: Int, 
        val isFlagged: Boolean,
        val width: Int = 0,
        val height: Int = 0,
        val lastModifiedDb: Long = 0
    )

    private fun getImageData(path: String): DbImageData {
        val normalizedPath = File(path).absolutePath
        return try {
            getDbConnection()?.prepareStatement("SELECT is_favorite, star_count, is_flagged, width, height, last_modified FROM image_data WHERE LOWER(path) = LOWER(?)")?.use { stmt ->
                stmt.setString(1, normalizedPath)
                val rs = stmt.executeQuery()
                if (rs.next()) {
                    DbImageData(
                        rs.getInt("is_favorite") == 1,
                        rs.getInt("star_count"),
                        rs.getInt("is_flagged") == 1,
                        rs.getInt("width"),
                        rs.getInt("height"),
                        rs.getLong("last_modified")
                    )
                } else {
                    DbImageData(false, 0, false)
                }
            } ?: DbImageData(false, 0, false)
        } catch (e: Exception) {
            System.err.println("[ERROR] Failed to fetch image data for $path: ${e.message}")
            DbImageData(false, 0, false)
        }
    }

    fun toggleFavorite(dirPath: String, filename: String, isFavorite: Boolean) {
        val file = File(dirPath, filename)
        if (file.exists()) {
            val normalizedPath = file.absolutePath
            try {
                // First check if it exists with different case
                val existingPath = getDbConnection()?.prepareStatement("SELECT path FROM image_data WHERE LOWER(path) = LOWER(?)")?.use { stmt ->
                    stmt.setString(1, normalizedPath)
                    val rs = stmt.executeQuery()
                    if (rs.next()) rs.getString("path") else null
                }
                
                val finalPath = existingPath ?: normalizedPath

                getDbConnection()?.prepareStatement("""
                    INSERT INTO image_data (path, is_favorite, star_count, is_flagged) 
                    VALUES (?, ?, ?, ?) 
                    ON CONFLICT(path) DO UPDATE SET 
                        is_favorite = excluded.is_favorite,
                        star_count = CASE WHEN excluded.is_favorite = 0 THEN 0 ELSE star_count END
                """.trimIndent())?.use { stmt ->
                    stmt.setString(1, finalPath)
                    stmt.setInt(2, if (isFavorite) 1 else 0)
                    stmt.setInt(3, 0) // Default star count if inserting
                    stmt.setInt(4, 0) // Default flagged if inserting
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                System.err.println("[ERROR] Failed to toggle favorite in DB: ${e.message}")
            }
        }
    }

    fun incrementStar(dirPath: String, filename: String) {
        val file = File(dirPath, filename)
        if (file.exists()) {
            val normalizedPath = file.absolutePath
            try {
                getDbConnection()?.prepareStatement("""
                    UPDATE image_data SET star_count = star_count + 1 
                    WHERE LOWER(path) = LOWER(?) AND is_favorite = 1
                """.trimIndent())?.use { stmt ->
                    stmt.setString(1, normalizedPath)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                System.err.println("[ERROR] Failed to increment star in DB: ${e.message}")
            }
        }
    }

    fun toggleFlag(dirPath: String, filename: String, isFlagged: Boolean) {
        val file = File(dirPath, filename)
        if (file.exists()) {
            val normalizedPath = file.absolutePath
            try {
                // First check if it exists with different case
                val existingPath = getDbConnection()?.prepareStatement("SELECT path FROM image_data WHERE LOWER(path) = LOWER(?)")?.use { stmt ->
                    stmt.setString(1, normalizedPath)
                    val rs = stmt.executeQuery()
                    if (rs.next()) rs.getString("path") else null
                }
                
                val finalPath = existingPath ?: normalizedPath

                getDbConnection()?.prepareStatement("""
                    INSERT INTO image_data (path, is_favorite, star_count, is_flagged) 
                    VALUES (?, ?, ?, ?) 
                    ON CONFLICT(path) DO UPDATE SET 
                        is_flagged = excluded.is_flagged
                """.trimIndent())?.use { stmt ->
                    stmt.setString(1, finalPath)
                    stmt.setInt(2, 0) // Default favorite if inserting
                    stmt.setInt(3, 0) // Default star count if inserting
                    stmt.setInt(4, if (isFlagged) 1 else 0)
                    stmt.executeUpdate()
                }
            } catch (e: Exception) {
                System.err.println("[ERROR] Failed to toggle flag in DB: ${e.message}")
            }
        }
    }

    fun getImageFile(dirPath: String, filename: String): File? {
        val file = File(dirPath, filename)
        return if (file.exists() && file.isFile) file else null
    }

    fun isValidDirectory(path: String): Boolean {
        return File(path).isDirectory
    }

    fun clearCaches() {
        thumbnailCache.clear()
        metadataCache.clear()
    }
}
