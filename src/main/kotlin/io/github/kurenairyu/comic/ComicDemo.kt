package io.github.kurenairyu.comic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.kurenairyu.compose.util.getPreferredWindowSize
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

/**
 * @author Kurenai
 * @since 2023/2/23 0:49
 */

@OptIn(ExperimentalMaterialApi::class)
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
            Column(Modifier.fillMaxSize().background(Color.DarkGray)) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(color = Color(180, 180, 180))
                        .padding(10.dp)
                ) {
                    1 + 2
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        SearchTextField()
                        Spacer(Modifier.height(20.dp))
                        comicCardList()
                    }
                }
            }
        }
    }
}


@Composable
fun ApplicationScope.SearchTextField() {
    val searchKeyword by ComicManager.searchKeyword.collectAsState()
    OutlinedTextField(
        value = searchKeyword,
        onValueChange = ComicManager::changeSearchKeyword,
        singleLine = true,
        placeholder = {
            Text("Please input keyword for search")
        },
        modifier = Modifier.padding(4.dp),
        leadingIcon = {
            Image(
                imageVector = Icons.Filled.Search,
                contentDescription = "search", //image的无障碍描述
                modifier = Modifier.clickable {// 通过modifier来设置点击事件

                })
        },
        trailingIcon = {
            Image(
                imageVector = Icons.Filled.Clear,
                contentDescription = "clear", //image的无障碍描述
                modifier = Modifier.clickable {// 通过modifier来设置点击事件
                    ComicManager.changeSearchKeyword("")
                }
            )
        }
    )
}

@Composable
fun ColumnScope.comicCardList() {
    val currentPath by ComicManager.currentPath.collectAsState()
    val paths by ComicManager.paths.collectAsState()
    val state = rememberLazyGridState()
    LaunchedEffect(currentPath) {
        state.animateScrollToItem(0)
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        state = state,
    ) {
        currentPath.parent?.let { item { comicCard(it, true) } }
        if (paths.isNotEmpty()) {
            paths.forEach { comic ->
                item { comicCard(comic) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.comicCard(path: Path, isUpDirectory: Boolean = false) {
    val bitmap by ComicManager.getCover(path).collectAsState()
    Card(
        modifier = Modifier.padding(6.dp).animateItemPlacement(),
        elevation = 8.dp,
        backgroundColor = Color.LightGray,
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier.height(400.dp).align(Alignment.CenterHorizontally)
            ) {
                    Card(
                        elevation = 6.dp,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        if (isUpDirectory) {
                            Image(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "返回", //image的无障碍描述
                                modifier = Modifier.clickable {// 通过modifier来设置点击事件
                                    ComicManager.changeCurrentPath(path)
                                }.size(50.dp)
                            )
                        } else if (path.isDirectory()) {
                            Image(
                                imageVector = Icons.Filled.List,
                                contentDescription = path.name, //image的无障碍描述
                                modifier = Modifier.clickable {// 通过modifier来设置点击事件
                                    ComicManager.changeCurrentPath(path)
                                }.size(50.dp)
                            )
                        } else if (bitmap != null) {
                            //TODO: 加载失败或者是不合法的文件没法剔除
                            Image(bitmap!!, path.name)
                        }
                }
            }
            Text(text = if (isUpDirectory) "" else path.name, modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
        }
    }
}