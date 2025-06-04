package io.github.kurenairyu.comic

import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onSizeChanged
import io.github.kurenairyu.comic.ComicManager.currentPageNum
import io.github.kurenairyu.comic.ComicManager.getBitmap
import io.github.kurenairyu.comic.ComicManager.getMergedBitmap
import io.github.kurenairyu.comic.ComicManager.pageFlow
import io.github.kurenairyu.comic.ComicManager.zipFileFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.lingala.zip4j.ZipFile
import java.nio.file.Path

@Composable
fun ReadingScreen() {

    val requester = remember { FocusRequester() }
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        var height by remember { mutableStateOf(0) }
        Row(modifier = Modifier
            .onSizeChanged { size ->
                height = size.height
            }
            .fillMaxSize().background(Color.Magenta)
            .onKeyEvent {
                if (it.type != KeyEventType.KeyUp) false
                else when (it.key) {
                    Key.PageUp,
                    Key.DirectionRight -> {
                        ComicManager.prevPage()
                        true
                    }

                    Key.PageDown,
                    Key.DirectionLeft -> {
                        ComicManager.nextPage()
                        true
                    }

                    Key.DirectionDown -> {
                        ComicManager.nextStep()
                        true
                    }

                    Key.DirectionUp -> {
                        ComicManager.prevStep()
                        true
                    }

                    Key.Escape, Key.Backspace -> {
                        ComicManager.closeComic()
                        true
                    }
                    else -> false
                }
            }.focusRequester(requester).focusable()
        ) {
            val pageNum by currentPageNum.collectAsState()
            val pages by pageFlow.collectAsState()
            val zipFile by zipFileFlow.collectAsState()

            if (zipFile == null || pages.isEmpty()) return@Row

            ComicPage2(pageNum, zipFile, pages)

        }
    }
    LaunchedEffect(Unit) {
        requester.requestFocus()
    }
}

@Composable
@Preview
private fun RowScope.ComicPage(
    pageNum: Int,
    zipFile: ZipFile?,
    pages: List<String>,
    maxHeight: Int
) {

    val first = pageNum.coerceIn(0..< pages.lastIndex)

    var image: ImageBitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(pageNum) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = zipFile?.getMergedBitmap(listOf(pages[first], pages[first+1]), maxHeight)?.asComposeImageBitmap()
            bitmap?.prepareToDraw()
            image = bitmap
        }
    }

    if (image == null) {
        CircularProgressIndicator()
        return
    }

    Image(
        bitmap = image!!,
        pages[pageNum],
        modifier = Modifier.animateContentSize().fillMaxHeight().weight(1F).background(Color.DarkGray),
        alignment = Alignment.Center
    )
}

@Composable
@Preview
private fun RowScope.ComicPage2(
    pageNum: Int,
    zipFile: ZipFile?,
    pages: List<String>
) {

    val first = pageNum.coerceIn(0..< pages.lastIndex)
    val image by produceState<ImageBitmap?>(null, pageNum) {
        value = zipFile?.getBitmap(pages[first])
    }

    val second = first +1
    val image2 by produceState<ImageBitmap?>(null, pageNum) {
        value = zipFile?.getBitmap(pages[second])
    }

    if (image2 != null) {
        Image(
            bitmap = image2!!,
            pages[second],
            modifier = Modifier.animateContentSize().fillMaxHeight().weight(1F).background(Color.DarkGray),
            alignment = Alignment.CenterEnd
        )
    }

    if (image != null) {
        Image(
            bitmap = image!!,
            pages[first],
            modifier = Modifier.animateContentSize().fillMaxHeight().weight(1F).background(Color.DarkGray),
            alignment = Alignment.CenterStart
        )
    }
}

@Composable
@Preview
fun RedingScreenPreview() {
    ComicManager.changeCurrentPath(Path.of("Y:\\Comic\\(一般コミック) [藤近小梅] 隣のお姉さんが好き\\第1話.cbz"))
    MaterialTheme {
        ReadingScreen()
    }
}
