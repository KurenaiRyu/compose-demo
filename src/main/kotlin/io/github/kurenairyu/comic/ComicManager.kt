package io.github.kurenairyu.comic

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.sksamuel.aedile.core.caffeineBuilder
import io.github.kurenairyu.comic.Utils.ZIP_EXTENSIONS
import io.github.kurenairyu.comic.Utils.imageFileNames
import io.github.kurenairyu.comic.Utils.resizeImageToMaxHeight
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.lingala.zip4j.ZipFile
import org.jetbrains.skia.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes
import kotlin.time.measureTimedValue

/**
 * @author Kurenai
 * @since 2023/2/23 0:50
 */

object ComicManager {
    private const val COVER_WIDTH = 450
    private const val COVER_HEIGHT = 600

    @OptIn(ExperimentalCoroutinesApi::class)
    private val comicWorker = Dispatchers.IO.limitedParallelism(4) + CoroutineName("ReadingComicWorker")

    private val coverCache = caffeineBuilder<String, CacheEntity> {
        maximumSize = 10
        expireAfterWrite = 20.minutes
        expireAfterAccess = 20.minutes
    }.build()

    val status = MutableStateFlow(Status.LISTING)
    private val _currentPath = MutableStateFlow(Path.of("Y:\\Comic\\(一般コミック) [あだちとか] ノラガミ"))
    val currentPath = _currentPath.asStateFlow()

    private val _currentComic = MutableStateFlow<Path>(Path.of(""))
    val currentComic = _currentComic.asStateFlow()

    var zipFileFlow: MutableStateFlow<ZipFile?> = MutableStateFlow(null)
    var pageFlow: StateFlow<List<String>> = zipFileFlow.map {
        it?.imageFileNames()?: emptyList()
    }.stateIn(CoroutineScope(this.comicWorker), SharingStarted.Eagerly, emptyList())

    val currentPageNum = MutableStateFlow(0)
    val showPages = 2

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword = _searchKeyword.asStateFlow()

    private val _paths = pathsFlow()
    val paths = pathsFilterFlow()

    private fun pathsFlow(): StateFlow<List<Path>> {
        return _currentPath
            .onEach { log.info("Read path $it") }
            .map { path ->
                kotlin.runCatching {
                    Files.list(path)
                        .filter { it.isDirectory() || ZIP_EXTENSIONS.any { ext -> it.name.endsWith(ext, true) } }
                        .toList()
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
        _currentPath.update { path }
        changeSearchKeyword("")
        coverCache.invalidateAll()
    }

    fun openComic(path: Path) {
        if (_currentComic.value != path) {
            currentPageNum.update { 0 }
            zipFileFlow.getAndUpdate { ZipFile(path.pathString) }?.close()
            _currentComic.update { path }
        }
        status.update { Status.READING }
    }

    fun closeComic() {
        status.update { Status.LISTING }
    }

    suspend fun getCover(path: Path, maxHeight: Int): ImageBitmap? = withContext(this.comicWorker) {
        ZipFile(path.pathString).use { zipFile ->
            val fileNames = zipFile.imageFileNames()
            fileNames.firstOrNull()
                ?.let { zipFile.getBitmap(it, maxHeight) }
        }
    }

    fun ZipFile.getBitmap(name: String, maxHeight: Int = -1): ImageBitmap? = kotlin.runCatching {
        var result: ImageBitmap?
        val fullPath = "${this.file.absolutePath}/$name"
        measureTimeMillis {
                result = this.getFileHeader(name)
                    ?.let { this.getInputStream(it).use { Image.makeFromEncoded(it.readAllBytes()) } }
                    ?.let {
                        if (maxHeight > 0) resizeImageToMaxHeight(it, maxHeight).asComposeImageBitmap()
                        else it.toComposeImageBitmap()
                    }
                if (result == null) log.warn("Read $fullPath bitmap is null")
        }.also { log.info("Read bitmap of $fullPath in $it ms") }
        result
    }.getOrNull()

    fun ZipFile.getMergedBitmap(list: List<String>, maxHeight: Int): ImageBitmap? = kotlin.runCatching {
        val fullPath = "${this.file.absolutePath}/${list.joinToString()}"
        val (result, duration) = measureTimedValue {
            val images = list.reversed().mapNotNull { name ->
                this.getFileHeader(name)
                    ?.let { this.getInputStream(it).use { Image.makeFromEncoded(it.readAllBytes()) } }
            }
            Utils.mergeImages(images, maxHeight).asComposeImageBitmap()
        }
        log.info("Read bitmap of $fullPath in ${duration.inWholeMilliseconds} ms")
        result
    }.getOrNull()

    fun nextPage() = currentPageNum.update {
        log.info("NextPage")
        (it + showPages).coerceAtMost(pageFlow.value.lastIndex)
    }

    fun prevPage() = currentPageNum.update {
        log.info("PrevPage")
        (it - showPages).coerceAtLeast(0)
    }

    fun nextStep() = currentPageNum.update {
        log.info("NextStep")
        (it + 1).coerceAtMost(pageFlow.value.lastIndex)
    }

    fun prevStep() = currentPageNum.update {
        log.info("PrevStep")
        (it - 1).coerceAtLeast(0)
    }


    enum class Status {
        LISTING, READING
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

data object NothingCacheEntity : CacheEntity