package io.github.kurenairyu.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ItemListScreen(items: List<Item>, onItemClick: (id: Long) -> Unit) {
    LazyColumn {
        items(items = items) { item ->
            Text(
                text = item.text,
                modifier = Modifier.clickable { onItemClick(item.id) }
            )
        }
    }
}