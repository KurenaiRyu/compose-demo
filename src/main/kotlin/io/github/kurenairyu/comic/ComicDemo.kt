package io.github.kurenairyu.comic

import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import io.github.kurenairyu.compose.util.getPreferredWindowSize
import io.github.kurenairyu.getLogger
import navcontroller.NavController
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * @author Kurenai
 * @since 2023/2/23 0:49
 */

val navController = NavController(Destination.LIST.name, mutableSetOf())
val log = getLogger("TBC-Comic")

fun main() = application {

    val state = WindowState(
        position = WindowPosition.Aligned(Alignment.Center),
        size = getPreferredWindowSize(1024, 800)
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "List Demo",
        state = state
//            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    ) {
        MaterialTheme {
            val comicManagerStatus by ComicManager.status.collectAsState()
            when (comicManagerStatus) {
                ComicManager.Status.READING -> ReadingScreen()
                else -> ListScreen()
            }
        }
    }
}