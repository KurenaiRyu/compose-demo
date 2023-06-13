package io.github.kurenairyu.comic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.sksamuel.aedile.core.caffeineBuilder
import io.github.kurenairyu.comic.CacheEntity.Companion.cacheOf
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import moe.kurenai.bgm.util.getLogger
import net.lingala.zip4j.ZipFile
import org.jetbrains.skia.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * @author Kurenai
 * @since 2023/2/23 0:50
 */

object ComicManager {

    private val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp")
    private val ZIP_EXTENSIONS = listOf(".zip", ".cbz")
    private const val COVER_WIDTH = 450
    private const val COVER_HEIGHT = 600

    private val log = getLogger()
    private val coverCache = caffeineBuilder<String, CacheEntity> {
        maximumSize = 1000
        expireAfterWrite = 20.minutes
        expireAfterAccess = 20.minutes
    }.build()
    private val rollingCache = caffeineBuilder<String, Int> {
        maximumSize = 1000
        expireAfterWrite = 20.minutes.apply {  }
        expireAfterAccess = 20.minutes
    }.build()

    private val _currentPath = MutableStateFlow(Path.of("Y:\\Comic\\(一般コミック) [あだちとか] ノラガミ"))
    val currentPath = _currentPath.asStateFlow()

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _paths = pathsFlow()
    val paths = pathsFilterFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val readingComicWorker = Dispatchers.IO.limitedParallelism(2) + CoroutineName("ReadingComicWorker")

    private fun pathsFlow(): StateFlow<List<Path>> {
        return _currentPath
            .onEach { log.info("Read path $it") }
            .map { path ->
                kotlin.runCatching {
                    Files.list(path).filter { it.isDirectory() || ZIP_EXTENSIONS.any { ext -> it.name.endsWith(ext, true) } }.toList()
                }.getOrDefault(emptyList())
            }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())
    }

    @OptIn(FlowPreview::class)
    private fun pathsFilterFlow(): StateFlow<List<Path>> {
        return searchKeyword
            .debounce(1000L)
            .onEach { log.info("Searching $it") }
            .combine(_paths) { searchKeyword, comics ->
                if (searchKeyword.isBlank()) comics else {
                    val keywords = searchKeyword.split(" ")
                    comics.filter { c ->
                        keywords.any { c.name.contains(it, true) }
                    }
                }
            }.stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun changeSearchKeyword(text: String) {
        _searchKeyword.update { text }
    }

    fun changeCurrentPath(path: Path) {
        _currentPath.getAndUpdate { path }
        changeSearchKeyword("")
    }

    @Suppress("UNCHECKED_CAST")
    fun getCover(path: Path): StateFlow<ImageBitmap?> {
        return flow {
            if (path.isDirectory()) emit(null) else {
                val id = path.pathString
                var entity = coverCache.getIfPresent(id)
                if (entity != null) emit((entity as? DataCacheEntity<ImageBitmap>)?.data) else {
                    measureTimeMillis {
                        entity = coverCache.get(id) {
                            kotlin.runCatching {
                                withContext(readingComicWorker) {
                                    ZipFile(id).use { zipFile ->
                                        val fileNames = zipFile.fileNames()
                                        val coverList = listOf(fileNames.find { it.contains("cover", true) }) + fileNames.sorted().subList(0, 2.coerceAtMost(fileNames.size))
                                        coverList.filterNotNull().forEach {
                                            zipFile.getBitmap(it)?.let { return@withContext cacheOf(it) }
                                        }
                                        NothingCacheEntity
                                    }
                                }
                            }.onFailure {
                                log.error("Read $id fail", it)
                            }.getOrDefault(NothingCacheEntity)
                        }
                        val bitmap = when (entity) {
                            is DataCacheEntity<*> -> (entity as DataCacheEntity<*>).data as? ImageBitmap
                            NothingCacheEntity -> null
                            else -> null
                        }
                        emit(bitmap)
                    }.also { log.info("Read $id in $it ms") }
                }
            }
        }.stateIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            null
        )
    }

    private fun ZipFile.fileNames(): List<String> {
        return this.fileHeaders
            .asSequence()
            .filter {
                !it.isDirectory &&
                        IMAGE_EXTENSIONS.any { ext -> it.fileName.endsWith(ext, true) }
            }.map { it.fileName }
            .toList()
    }

    private fun ZipFile.getBitmap(name: String): ImageBitmap? = kotlin.runCatching {
        var result: ImageBitmap?
        measureTimeMillis {
            result = this.getFileHeader(name)?.let { this.getInputStream(it).use { Image.makeFromEncoded(it.readAllBytes()) } }?.let { image ->
                val scale = if (image.width > image.height) {
                    image.width / COVER_WIDTH
                } else {
                    image.height / COVER_HEIGHT
                }.coerceAtLeast(1)
                scaleUsingSurface(image, image.width / scale, image.height / scale).toComposeImageBitmap()
            }
            if (result == null) log.warn("Read $name bitmap is null")
        }.also { log.info("Read bitmap of $name in $it ms") }
        result
    }.getOrNull()

    private fun scaleUsingSurface(image: Image, width: Int, height: Int) = Surface.makeRasterN32Premul(width, height).use { surface ->
        val canvas = surface.canvas
        canvas.drawImageRect(
            image,
            Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
            Rect.makeWH(width.toFloat(), height.toFloat()),
            SamplingMode.LINEAR,
            null,
            true
        )
        surface.makeImageSnapshot()
    }
}

sealed interface CacheEntity {
    companion object {
        fun <T> cacheOf(data: T?) = data?.let { DataCacheEntity(data) } ?: NothingCacheEntity
    }
}

data class DataCacheEntity<T>(
    val data: T
) : CacheEntity

object NothingCacheEntity : CacheEntity