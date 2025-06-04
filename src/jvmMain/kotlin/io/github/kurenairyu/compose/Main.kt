package io.github.kurenairyu.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import io.github.kurenairyu.compose.util.getPreferredWindowSize
import io.github.kurenairyu.compose.view.SplashUI
import kotlinx.coroutines.delay

@Composable
fun App(state: WindowState) {
    var text by remember { mutableStateOf("Hello, World!") }
    val count = remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().background(Color.LightGray)) {
        TopAppBar(
            title = {
                Column {

                    OutlinedButton(onClick = {}, modifier = Modifier.padding(8.dp)) {
                        Text(text = "/")
                    }
                }
                Column {

                    OutlinedButton(onClick = {}, modifier = Modifier.padding(8.dp)) {
                        Text(text = "Comic/")
                    }
                }
            },
            elevation = 8.dp,
            navigationIcon = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
            })
//        ListItem(icon = { Icon(Icons.Default.Menu, "Folder") }, text = { Text("ノラガミ") }, secondaryText = { Text("一般コミック") })
//
//        Divider()
//
//        ListItem(text = { Text("無職転生") }, secondaryText = { Text("一般コミック") })
//
//        Divider()
//
//        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//            onClick = {
//                count.value++
//            }) {
//            Text(if (count.value == 0) "Hello World" else "Clicked ${count.value}!")
//        }
//        Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//            onClick = {
//                count.value = 0
//            }) {
//            Text("Reset")
//        }
//        Button(onClick = {
//            text = "Hello, Desktop!"
//        }) {
//            Text(text)
//        }

        var counter by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                counter++
                delay(1000)
            }
        }
        Column {
            Text(counter.toString())
        }
        Column {
            Text(
                "Position ${state.position}",
                Modifier.clickable {
                    val position = state.position
                    if (position is WindowPosition.Absolute) {
                        state.position = position.copy(x = state.position.x + 10.dp)
                    }
                }
            )

            Text(
                "Size ${state.size}",
                Modifier.clickable {
                    state.size = state.size.copy(width = state.size.width + 10.dp)
                }
            )
        }

        var isDialogOpen by remember { mutableStateOf(false) }

        var expanded by remember { mutableStateOf(false) }
        Card(Modifier.align(Alignment.CenterHorizontally)) {
            Column(Modifier.clickable { expanded = !expanded }, Arrangement.Center, Alignment.CenterHorizontally) {
                Image(painterResource("chitose.jpg"), "Test")
                AnimatedVisibility(expanded) {
                    Text(
                        text = "Animated Visibility",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        Button(onClick = { isDialogOpen = true }) {
            Text(text = "Open dialog")
        }

        if (isDialogOpen) {
            Dialog(
                onCloseRequest = { isDialogOpen = false },
                state = rememberDialogState(position = WindowPosition(Alignment.Center))
            ) {
                // Dialog's content
            }
        }

        //进度条
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(color = Color(180, 180, 180))
                .height(45.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = remember { mutableStateOf(0F) }
            if (progress.value == 0F) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(progress.value, modifier = Modifier.fillMaxWidth())
            }
            LaunchedEffect(progress) {
                for (i in 0..100) {
                    if (i == 0) delay(2000)
                    progress.value = i.toFloat() / 100
                    delay(1000)
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(color = Color(180, 180, 180))
                .height(60.dp)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = remember { mutableStateOf(0F) }
            if (progress.value == 0F) {
                CircularProgressIndicator()
            } else {
                CircularProgressIndicator(progress.value)
            }
            LaunchedEffect(progress) {
                for (i in 0..100) {
                    if (i == 0) delay(2000)
                    progress.value = i.toFloat() / 100
                    delay(1000)
                }
            }
        }

        //列表滚动条
        Box(
            modifier = Modifier.fillMaxSize()
                .background(color = Color(180, 180, 180))
                .padding(10.dp)
        ) {
            val stateVertical = rememberScrollState(0)
            val stateHorizontal = rememberScrollState(0)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
                    .padding(end = 12.dp, bottom = 12.dp)
                    .horizontalScroll(stateHorizontal)
            ) {
                Column {
                    for (item in 0..30) {
                        TextBox("Item #$item")
                        if (item < 30) {
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
            HorizontalScrollbar(
                modifier = Modifier.align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(end = 12.dp),
                adapter = rememberScrollbarAdapter(stateHorizontal)
            )
        }
    }
}

fun main() = application {
    // 透明窗口
//    var isOpen by remember { mutableStateOf(true) }
//    if (isOpen) {
//        Window(
//            onCloseRequest = { isOpen = false },
//            title = "Transparent Window Example",
//            transparent = true,
//            undecorated = true, //transparent window must be undecorated
//        ) {
//            Surface(
//                modifier = Modifier.fillMaxSize().padding(5.dp).shadow(3.dp, RoundedCornerShape(30.dp)),
//                color = Color(55, 55, 55),
//                shape = RoundedCornerShape(30.dp) //window has round corners now
//            ) {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    WindowDraggableArea{
//                        Box(Modifier.fillMaxWidth().height(48.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
//                            Text(text = "Compose Demo", color = Color.White)
//                        }
//                    }
//                    Text("Hello World!", color = Color.White,)
//                }
//            }
//        }
//    }
    var isReady by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

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
            onCloseRequest = { isVisible = false },
            title = "Compose Demo",
            visible = isVisible,
            state = state
//            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified)
        ) {

            if (!isVisible) {
                Tray(
                    TrayIcon,
                    tooltip = "Counter",
                    onAction = { isVisible = true },
                    menu = {
                        Item("Exit", onClick = ::exitApplication)
                    },
                )
            }

            MaterialTheme {
                App(state)
//                Conversation(SampleData.conversationSample)
            }
        }
    }
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}

@Composable
fun TextBox(text: String = "Item") {
    Box(
        modifier = Modifier.height(32.dp)
            .width(400.dp)
            .background(color = Color(200, 0, 0, 20))
            .padding(start = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = text)
    }
}


data class Message(val author: String, val body: String)

@Composable
fun MessageCard(msg: Message) {
    Row(Modifier.padding(all = 8.dp)) {
        val imagePath = if (msg.author == "千歳") "chitose.jpg" else "GRANBLUE FANTASY Versus ORIGINAL SOUNDTRACK .jpg"
        Image(
            painterResource(imagePath), msg.author,
            Modifier.size(70.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )

        Column {
            Text(
                msg.author,
                color = MaterialTheme.colors.secondary,
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(shape = MaterialTheme.shapes.medium, elevation = 4.dp,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
                    .animateContentSize()
                    .padding(1.dp),
                color = surfaceColor
            ) {
                Text(
                    msg.body,
                    modifier = Modifier.padding(8.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewMessageCard() {
    MaterialTheme {
        Surface {
            MessageCard(Message("Message Card", "The body!"))
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    val visibilityMsg = mutableStateListOf<Message>()
    LaunchedEffect(Unit) {
        messages.forEach {
            visibilityMsg.add(it)
            delay(2000)
        }
    }
    LazyColumn {
        items(visibilityMsg.size) { index ->
            MessageCard(visibilityMsg[index])
        }
    }
}

@Preview
@Composable
fun PreviewConversation() {
    MaterialTheme {
        Conversation(SampleData.conversationSample)
    }
}




