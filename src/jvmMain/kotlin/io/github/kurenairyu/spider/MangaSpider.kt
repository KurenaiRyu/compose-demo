package io.github.kurenairyu.spider

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.kurenairyu.compose.util.getPreferredWindowSize
import io.github.kurenairyu.compose.view.SplashUI
import kotlinx.coroutines.delay

/**
 * @author Kurenai
 * @since 2023/1/8 23:51
 */

fun main() = application {

    var isReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1000)
        isReady = true
    }

    if (!isReady) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Compose Demo",
            state = WindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                size = getPreferredWindowSize(800, 300)
            ),
            undecorated = true,
        ) {
            MaterialTheme {
                SplashUI("Compose Demo")
            }
        }
    } else {
        val state = WindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = getPreferredWindowSize(1024, 800)
        )
        Window(
            onCloseRequest = { },
            title = "Compose Demo",
            state = state
//            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
        ) {
            MaterialTheme {
                Surface {
                    ItemCard("无职转生", 12)
                }
            }
        }
    }
}

@Composable
fun ItemCard(title: String, total: Int) {
    val progress = remember { mutableStateOf(0F) }
    val downloadState = remember { mutableStateOf<DownloadState>(Stop(0)) }
    Card(
        modifier = Modifier.padding(15.dp).width(300.dp),
        backgroundColor = Color.LightGray,
        elevation = 5.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(8.dp))
            Column {
                Text(
                    text = "${downloadState.value.progress}/$total",
                    style = MaterialTheme.typography.caption
                )
                if (downloadState.value is Prepare) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(downloadState.value.progress.toFloat() / total, modifier = Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    downloadState.value = downloadState.value.switch()
                },
            ) {
                Text(downloadState.value.text)
            }
        }

        if (downloadState.value !is Stop) {
            LaunchedEffect(downloadState) {
                if (downloadState.value.progress < total) {
                    for (i in downloadState.value.progress..total) {
                        if (i == 0) delay(2000)
                        progress.value = i.toFloat() / total
                        delay(1000)
                        downloadState.value = Progress(i)
                    }
                }
                downloadState.value = downloadState.value.switch()
            }
        }
    }
}

@Composable
@Preview
fun ItemCardPreview() {
    MaterialTheme {
        Surface {
            ItemCard("无职转生", 100)
        }
    }
}

@Composable
fun ItemCardList(list: List<String>) {
    LazyColumn(
        modifier = Modifier.background(Color.Gray)
    ) {
        items(list.size) { index ->
            ItemCard(list[index], 12)
        }
    }
}

@Composable
@Preview
fun ItemCardListPreview() {
    MaterialTheme {
        Surface {
            ItemCardList(listOf("无职转生", "天使降临我身边", "为这个美好的世界献上祝福"))
        }
    }
}

sealed class DownloadState(val text: String, val progress: Int, val msg: String? = null) {
    fun switch(): DownloadState {
        return when (this) {
            is Prepare,
            is Progress -> Stop(progress, msg)

            is Stop -> Prepare(progress, msg)
        }
    }
}

class Progress(progress: Int, msg: String? = null) : DownloadState("暂停", progress, msg)
class Prepare(progress: Int, msg: String? = null) : DownloadState("暂停", progress, msg)
class Stop(progress: Int, msg: String? = null) : DownloadState("开始", progress, msg)