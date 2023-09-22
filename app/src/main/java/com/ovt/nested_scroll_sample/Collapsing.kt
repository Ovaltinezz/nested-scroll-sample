package com.ovt.nested_scroll_sample

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovt.nested_scroll_sample.ui.theme.Purple80
import kotlinx.coroutines.launch

@Composable
fun Collapsing() {
    val context = LocalContext.current
    val screenWidthPx = context.resources.displayMetrics.widthPixels
    val screenWidth = with(LocalDensity.current) { screenWidthPx.toDp() }
    val minHeight = 45.dp
    val minHeightPx = with(LocalDensity.current) { minHeight.roundToPx().toFloat() }
    val maxHeight = 200.dp
    val maxHeightPx = with(LocalDensity.current) { maxHeight.roundToPx().toFloat() }
    val headerFromStart = 50.dp
    val deltaHeight = maxHeightPx - minHeightPx
    var headerWidthPx by remember { mutableStateOf(-1) }
    val targetPercent by remember { mutableStateOf(Animatable(1f)) }
    val coroutineScope = rememberCoroutineScope()
    val nestedScrollConnection = remember {
        var consumedY = 0f

        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0) return consume(available, source)
                return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0) return consume(available, source)
                return super.onPostScroll(consumed, available, source)
            }

            private fun consume(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val oldY = consumedY
                consumedY += delta
                /* Compared to the initial state, the title bar can only be dragged upwards */
                consumedY = consumedY.coerceIn(-deltaHeight, 0f)
                val percent = 1f + consumedY / deltaHeight
                coroutineScope.launch {
                    targetPercent.animateTo(percent)
                }
                return Offset(0f, consumedY - oldY)
            }
        }
    }
    Column(
        modifier = Modifier
            .systemBarsPadding()
            .nestedScroll(nestedScrollConnection)
    ) {
        Box(
            modifier = Modifier
                .background(lightColorScheme().primary)
                .fillMaxWidth()
                .height(minHeight + (maxHeight - minHeight) * targetPercent.value),
            contentAlignment = Alignment.BottomStart
        ) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
                    .size(30.dp)
                    .align(Alignment.TopStart)
            )
            Text(
                text = "Header",
                color = Color.White,
                fontSize = 30.sp.times(targetPercent.value * 0.5 + 1),
                modifier = Modifier
                    .onGloballyPositioned {
                        headerWidthPx = it.size.width
                    }
                    .offset(
                        x = if (headerWidthPx > 0) {
                            val headerWidth = with(LocalDensity.current) { headerWidthPx.toDp() }
                            (screenWidth - headerWidth - headerFromStart * 2) *
                                    targetPercent.value / 2 + headerFromStart
                        } else 0.dp,
                    )
            )
        }
        List()
    }
}

@Composable
fun List() {
    LazyColumn(
        Modifier.fillMaxWidth()
    ) {
        repeat(100) {
            item {
                Text(
                    text = "Item $it",
                    modifier = Modifier
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Purple80.copy(alpha = 0.2f))
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(10.dp),
                    fontSize = 20.sp
                )
            }
        }
    }
}
