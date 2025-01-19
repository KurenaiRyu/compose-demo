//package io.github.kurenairyu.bangumi
//
//import androidx.compose.foundation.*
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyGridItemScope
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.*
//import io.github.kurenairyu.compose.util.getPreferredWindowSize
//import io.ktor.client.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.*
//
///**
// * @author Kurenai
// * @since 2023/2/10 2:02
// */
//
//fun main() = application {
//
//    val state = WindowState(
//        position = WindowPosition.Aligned(Alignment.Center),
//        size = getPreferredWindowSize(1024, 800)
//    )
//    Window(
//        onCloseRequest = ::exitApplication,
//        title = "List Demo",
//        state = state
////            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
//    ) {
//
//        MaterialTheme {
//            Column(Modifier.fillMaxSize().background(Color.DarkGray)) {
//                Box(
//                    modifier = Modifier.fillMaxSize()
//                        .background(color = Color(180, 180, 180))
//                        .padding(10.dp)
//                ) {
//
//                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//                        SearchTextField()
//                        Spacer(Modifier.height(20.dp))
//                        lazySubjectCardList()
//                    }
//                }
//            }
//        }
//    }
//}
//
//object DataModule {
//    private val _isLast = MutableStateFlow(false)
//    var isLast = _isLast.asStateFlow()
//
//    private val _isSearching = MutableStateFlow(false)
//    val isSearching = _isSearching.asStateFlow()
//
//    private val _doSearch = MutableStateFlow(false)
//
//    private val _searchText = MutableStateFlow("")
//    val searchText = _searchText.asStateFlow()
//
//    private val _subjects = MutableStateFlow(emptyList<Subject>())
//    val subjects = _doSearch
//        .onEach {
//            if (it) {
//                doLoadMore()
//                _doSearch.update { false }
//            }
//        }
//        .map { _subjects.value }
//        .stateIn(
//            CoroutineScope(Dispatchers.IO),
//            SharingStarted.WhileSubscribed(5000),
//            _subjects.value
//        )
//
//    private val offset = MutableStateFlow(0)
//    private const val pageSize = 25
//
//    private suspend fun doLoadMore() {
//        if (_searchText.value.isNotBlank() && _isLast.value.not()) {
//            println("Loading Data: ${offset.value} $isLast")
//            val result = BgmClient.search(_searchText.value, offset.value, pageSize)
//            _subjects.update { it + result.data }
//            offset.update { it + result.data.size }
//            _isLast.update { offset.value >= result.total }
//            println("Loaded Data: ${_subjects.value.size} / ${result.total} $isLast")
//            println("---------------------------------------")
//        } else {
//            _isSearching.update { false }
//        }
//    }
//
//    fun doSearch() {
//        //reset
//        _isLast.update { false }
//        offset.update { 0 }
//        _subjects.update { emptyList() }
//
//        _isSearching.update { true }
//        _doSearch.update { true }
//    }
//
//    fun loadMore() {
//        _doSearch.update { true }
//    }
//
//    fun onSearchTextChange(text: String) {
//        _searchText.update { text }
//    }
//
//}
//
//@Composable
//fun ApplicationScope.SearchTextField() {
//    val searchText by DataModule.searchText.collectAsState()
//    OutlinedTextField(
//        value = searchText,
//        onValueChange = DataModule::onSearchTextChange,
//        keyboardActions = KeyboardActions(onSend = {
//
//        }),
//        singleLine = true,
//        placeholder = {
//            Text("Please input keyword for search")
//        },
//        modifier = Modifier.padding(4.dp),
//        leadingIcon = {
//            Image(
//                imageVector = Icons.Filled.Search,
//                contentDescription = "search", //image的无障碍描述
//                modifier = Modifier.clickable {// 通过modifier来设置点击事件
//                    DataModule.doSearch()
//                })
//        },
//        trailingIcon = {
//            Image(
//                imageVector = Icons.Filled.Clear,
//                contentDescription = "clear", //image的无障碍描述
//                modifier = Modifier.clickable {// 通过modifier来设置点击事件
//
//                }
//            )
//        }
//    )
//}
//
//@Composable
//fun ColumnScope.lazySubjectCardList() {
//    val subjects by DataModule.subjects.collectAsState()
//    val isSearching by DataModule.isSearching.collectAsState()
//    val isLast by DataModule.isLast.collectAsState()
//    if (subjects.isNotEmpty()) {
//        LazyVerticalGrid(
//            columns = GridCells.Adaptive(300.dp),
//        ) {
//            subjects.forEachIndexed { i, subject ->
//                item { subjectCard(subject) }
//            }
//            if (isLast.not() && isSearching) {
//                item {
//                    CircularProgressIndicator(modifier = Modifier.padding(100.dp).align(Alignment.CenterHorizontally))
//                    LaunchedEffect(subjects.size) {
//                        DataModule.loadMore()
//                    }
//                }
//            }
//        }
//    } else if (isSearching) {
//        CircularProgressIndicator(modifier = Modifier.padding(100.dp).align(Alignment.CenterHorizontally))
//        LaunchedEffect(Unit) {
//            DataModule.loadMore()
//        }
//    }
//}
//
//@Composable
//fun LazyGridItemScope.subjectCard(subject: Subject) {
//    val bitmap by BgmClient.getImage(subject.id).collectAsState()
//    Card(
//        modifier = Modifier.padding(6.dp),
//        elevation = 8.dp,
//        backgroundColor = Color.LightGray,
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Box (
//                modifier = Modifier.height(400.dp).align(Alignment.CenterHorizontally)
//            ) {
//                if (bitmap == null) {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).height(50.dp))
//                } else {
//                    Card(
//                        elevation = 6.dp,
//                        modifier = Modifier.align(Alignment.Center)
//                    ) {
//                        Image(bitmap!!, subject.name, contentScale = ContentScale.Crop)
//                    }
//                }
//            }
//            Text(text = subject.name, modifier = Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
//        }
//    }
//}