package com.ovt.nested_scroll_sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovt.nested_scroll_sample.ui.theme.Pink40
import com.ovt.nested_scroll_sample.ui.theme.Purple40

@Composable
fun StickyCartList() {
    val commodities = List(20) { it }
    val groupedCommodities = remember(commodities) {
        commodities.groupBy { it / 3 }
    }
    val startIndexes = remember(commodities) {
        getStartIndexes(groupedCommodities.entries)
    }
    val endIndexes = remember(commodities) {
        getEndIndexes(groupedCommodities.entries)
    }
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(horizontal = 10.dp),
        state = listState
    ) {
        itemsIndexed(commodities) { index, commodity ->
            if (startIndexes.contains(index)) {
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White, ShapeOfCardTop)
                )
                Text(
                    text = "Header${commodity / 3}",
                    color = Purple40,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .background(Color.White)
                        .padding(start = 10.dp)
                )
            }
            Text(
                text = "Item$commodity",
                color = Pink40,
                fontSize = 50.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White)
                    .padding(start = 10.dp)
            )
            if (endIndexes.contains(index)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White, ShapeOfCardBottom)
                )
            }
        }
    }

    val density = LocalDensity.current
    val topPadding = with(density) { 20.dp.toPx() }
    val headerHeight = with(density) { 30.dp.toPx() }
    val itemHeight = with(density) { 100.dp.toPx() }
    val showHeader by remember {
        derivedStateOf {
            !(startIndexes.contains(listState.firstVisibleItemIndex)
                    && listState.firstVisibleItemScrollOffset < topPadding)
        }
    }
    val moveHeader by remember {
        derivedStateOf {
            endIndexes.contains(listState.firstVisibleItemIndex)
                    && listState.firstVisibleItemScrollOffset > itemHeight - headerHeight
        }
    }
    val firstVisibleItemIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }
    if (showHeader) {
        Text(
            text = "Header${firstVisibleItemIndex / 3}",
            color = Purple40,
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 10.dp)
                .then(
                    if (moveHeader) Modifier.offset {
                        IntOffset(
                            0,
                            -(listState.firstVisibleItemScrollOffset - (itemHeight - headerHeight).toInt())
                        )
                    } else {
                        Modifier
                    }
                )
                .background(Color.White)
                .padding(start = 10.dp)
        )
    }
}

private fun getStartIndexes(entries: Set<Map.Entry<Int, List<Int>>>): List<Int> {
    var acc = 0
    val list = mutableListOf<Int>()
    entries.forEach { entry ->
        list.add(acc)
        acc += entry.value.size
    }
    return list
}

private fun getEndIndexes(entries: Set<Map.Entry<Int, List<Int>>>): List<Int> {
    var acc = -1
    val list = mutableListOf<Int>()
    entries.forEach { entry ->
        acc += entry.value.size
        list.add(acc)
    }
    return list
}