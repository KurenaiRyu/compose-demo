package io.github.kurenairyu.compose

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

object GifDemo {
    @JvmStatic
    fun main(args: Array<String>) = application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "Compose Demo",
                state = rememberWindowState(width = 800.dp, height = 600.dp)
            ) {
                val gifCodec = remember {
                    val bytes = File("V:\\WorkSpace\\code\\compose-demo\\src\\main\\resources\\gif-01.gif").readBytes()
                    Codec.makeFromData(Data.makeFromBytes(bytes))
                }
                val webpCodec = remember {
                    val bytes = File("V:\\WorkSpace\\code\\compose-demo\\src\\main\\resources\\100579048-4e006a80-3298-11eb-8ea0-a7205221f389.gif").readBytes()
                    Codec.makeFromData(Data.makeFromBytes(bytes))
                }
//        Column(Modifier.fillMaxSize(), Arrangement.Center) {
//            GifAnimation(gifCodec, Modifier.fillMaxSize())
//        }
                Column(Modifier.fillMaxSize(), Arrangement.Center) {
                    GifAnimation(webpCodec, Modifier.fillMaxSize())
                }
            }
    }
}

@Composable
fun GifAnimation(codec: Codec, modifier: Modifier) {
    val transition = rememberInfiniteTransition()
    val frameIndex by transition.animateValue(
        initialValue = 0,
        targetValue = codec.frameCount - 1,
        Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 0
                for ((index, frame) in codec.framesInfo.withIndex()) {
                    index at durationMillis
                    durationMillis += frame.duration
                    println(durationMillis)
                }
            }
        )
    )

    val bitmap = remember { Bitmap().apply { allocPixels(codec.imageInfo) } }
    Canvas(modifier) {
        codec.readPixels(bitmap, frameIndex)
        drawImage(bitmap.asComposeImageBitmap())
    }
}