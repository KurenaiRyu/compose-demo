package io.github.kurenairyu.comic

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

@Composable
fun ListScreen() {
    Column(Modifier.fillMaxSize().background(Color.DarkGray)) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(color = Color(180, 180, 180))
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                SearchTextField()
                Spacer(Modifier.height(20.dp))
                ComicCardList()
            }
        }
    }
}

@Composable
fun SearchTextField() {
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
fun ColumnScope.ComicCardList() {
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
        currentPath.parent?.let { item { ComicCard(it, isUpDirectory = true) } }
        if (paths.isNotEmpty()) {
            paths.forEach { comic ->
                item {
                    ComicCard(comic)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.ComicCard(path: Path, isUpDirectory: Boolean = false) {
    Card(
        modifier = Modifier.padding(6.dp).animateItem(),
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
                    modifier = Modifier.align(Alignment.Center).onClick {
                        navController.navigate(Destination.READING.name)
                        ComicManager.openComic(path)
                    },
                    contentColor = Color.Transparent
                ) {
                    if (isUpDirectory) {
                        Image(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回", //image的无障碍描述
                            modifier = Modifier.clickable {// 通过modifier来设置点击事件
                                ComicManager.changeCurrentPath(path)
                            }.size(50.dp)
                        )
                    } else if (path.isDirectory()) {
                        Image(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = path.name, //image的无障碍描述
                            modifier = Modifier.clickable {// 通过modifier来设置点击事件
                                ComicManager.changeCurrentPath(path)
                            }.size(50.dp)
                        )
                    } else {


                        val bitmap by with(LocalDensity.current) {
                            produceState<ImageBitmap?>(initialValue = null, path) {
                                value = ComicManager.getCover(path, 400.dp.toPx().toInt())
                            }
                        }

                        if (bitmap != null) {
                            //TODO: 加载失败或者是不合法的文件没法剔除
                            Image(bitmap!!, path.name, modifier = Modifier.fillMaxHeight())
                        } else {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            Text(text = if (isUpDirectory) "" else path.name, modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
        }
    }
}