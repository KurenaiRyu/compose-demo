package io.github.kurenairyu.bangumi

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.fasterxml.jackson.annotation.JsonProperty
import com.sksamuel.aedile.core.caffeineBuilder
import io.github.kurenairyu.getProp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.kurenai.bgm.BgmClient
import moe.kurenai.bgm.request.Request
import org.jetbrains.skia.Image
import java.io.InputStream
import kotlin.time.Duration.Companion.minutes


/**
 * @author Kurenai
 * @since 2023/2/10 6:05
 */
object BgmClient {

    private val OS_NAME = System.getProperty("os.name")
    private val OS_ARCH = System.getProperty("os.arch")
    private val OS_VERSION = System.getProperty("os.version")
    private val UA = "Kurenai Bangumi SDK Client/0.0.1 ($OS_NAME $OS_ARCH $OS_VERSION)"

    private val imageCache = caffeineBuilder<Int, ImageBitmap> {
        maximumSize = 1000
        expireAfterWrite = 20.minutes
        expireAfterAccess = 20.minutes
    }.build()

    private val json = Json {
        encodeDefaults = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val client = HttpClient(OkHttp) {
        defaultRequest {
            url("https://api.bgm.tv")
            headers {
                append(HttpHeaders.Accept, ContentType.Application.Json)
                append(HttpHeaders.ContentType, ContentType.Application.Json)
                append(HttpHeaders.UserAgent, UA)
            }
        }
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val bgm = BgmClient(
        appId = getProp("api.secret"),
        appSecret = getProp("api.secret"),
        redirectUri = getProp("api.redirect"),
        isDebugEnabled = false
    ).coroutine()

    suspend fun <T> send(request: Request<T>): T {
        return bgm.send(request)
    }

    suspend fun search(keyword: String, offset: Int = 0, limit: Int = 10): Page<Subject> {
        return client.post {
            url {
                path("v0/search/subjects")
                parameter("limit", limit)
                parameter("offset", offset)
                parameter("keyword", keyword)
            }
            setBody(SearchSubjectRequest(keyword))
        }.body()
    }

    fun getImage(id: Int): StateFlow<ImageBitmap?> {
        return flow {
            emit(imageCache.get(id) {
                println("Loading image $id ...")
                client.get("/v0/subjects/$id/image") {
                    parameter("type", "medium")
                }.body<InputStream>().use {
                    Image.makeFromEncoded(it.readAllBytes()).toComposeImageBitmap()
                }.also {
                    println("Load image $id success.")
                }
            })
        }.stateIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.WhileSubscribed(5000),
            null
        )
    }
}

@Serializable
data class SearchSubjectRequest(
    val keyword: String,
    val sort: String = "match",
    val filter: Filter = Filter()
) {
    @Serializable
    data class Filter(
        val type: List<Int> = emptyList(),
        val tag: List<String> = emptyList(),
        @JsonProperty("air_date")
        val airDate: List<String> = emptyList(),
        val rating: List<String> = emptyList(),
        val rank: List<String> = emptyList(),
        val nsfw: Boolean = false
    )
}

@Serializable
data class Page<T>(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val data: List<T>,
)

@Serializable
data class Tag(
    val name: String,
    val count: Int
)

@Serializable
class Subject {
    var id: Int = 0
    var type: Int = 0
    var name: String = ""
    var nameCn: String = ""
    var summary: String = ""
    var nsfw: Boolean = false
    var locked: Boolean = false
    var date: String? = ""
    var platform: String? = null
    var volumes: Int = 0
    var eps: Int = 0
    var totalEpisodes: Int = 0
    var tags: List<Tag>? = null
}
