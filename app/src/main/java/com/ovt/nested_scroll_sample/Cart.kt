package com.ovt.nested_scroll_sample

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.fengdai.compose.pulltorefresh.PullToRefresh
import com.github.fengdai.compose.pulltorefresh.rememberPullToRefreshState
import com.ovt.nested_scroll_sample.ui.theme.Pink40
import com.ovt.nested_scroll_sample.ui.theme.Purple40
import com.ovt.nested_scroll_sample.ui.theme.Purple80
import com.ovt.nested_scroll_sample.ui.theme.PurpleGrey40
import com.ovt.nested_scroll_sample.ui.theme.PurpleGrey80
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CartList() {
    val scope = rememberCoroutineScope()
    val state = rememberPullToRefreshState(isRefreshing = false)
    PullToRefresh(
        state = state,
        onRefresh = {
            scope.launch {
                state.isRefreshing = true
                delay(1000)
                state.isRefreshing = false
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(horizontal = 10.dp),

            ) {
            item {
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White, ShapeOfCardTop)
                )
            }
            (0..20).forEach() { index ->
                stickyHeader {
                    Text(
                        text = "Header$index",
                        color = Purple40,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(Color.White)
                            .padding(start = 10.dp)
                    )
                }
                item {
                    Text(
                        text = "Item$index",
                        color = Pink40,
                        fontSize = 50.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.White)
                            .padding(start = 10.dp)
                    )

                }
                stickyHeader {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(Color.White, ShapeOfCardBottom)
                    )
                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                    if (index != 20) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color.White, ShapeOfCardTop)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 卡片上部分的形状.
 */
val ShapeOfCardTop = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

/**
 * 卡片下部分的形状.
 */
val ShapeOfCardBottom = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)

@Composable
fun rememberCartState(): CartState {
    return remember { CartState() }
}

@Stable
class CartState {
    var value: Int by mutableStateOf(0)
        private set
    var maxValue: Int
        get() = _maxValueState.value
        internal set(newMax) {
            _maxValueState.value = newMax
            if (value > newMax) {
                value = newMax
            }
        }
    private var _maxValueState = mutableStateOf(Int.MAX_VALUE)
    private var accumulator: Float = 0f

    val scrollableState = ScrollableState {
        val absolute = (value + it + accumulator)
        val newValue = absolute.coerceIn(0f, maxValue.toFloat())
        val changed = absolute != newValue
        val consumed = newValue - value
        val consumedInt = consumed.roundToInt()
        value += consumedInt
        accumulator = consumed - consumedInt

        // Avoid floating-point rounding error
        if (changed) consumed else it
    }

    private fun consume(available: Offset): Offset {
        val consumedY = -scrollableState.dispatchRawDelta(-available.y)
        return available.copy(y = consumedY)
    }

    internal val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return consume(available)
        }
    }
}


@Composable
fun Cart(modifier: Modifier = Modifier, state: CartState = rememberCartState()) {
    Layout(
        content = {
            // TopBar()
            Text(
                text = "TopBar",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(listOf(Purple40, Purple80)))
            )
            // SortBar()
            Text(
                text = "SortBar",
                color = Color.White,
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Brush.verticalGradient(listOf(PurpleGrey40, PurpleGrey80)))
            )
            // CartList()
            CartList()
        },
        modifier = modifier
            .fillMaxSize()
            .scrollable(
                state = state.scrollableState,
                orientation = Orientation.Vertical,
                reverseDirection = true,
            )
            .nestedScroll(state.nestedScrollConnection)
    ) { measurables, constraints ->
        check(constraints.hasBoundedHeight)
        val height = constraints.maxHeight
        val firstPlaceable = measurables[0].measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val firstHeight = firstPlaceable.height
        val secondPlaceable = measurables[1].measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val bottomPlaceable = measurables[2].measure(
            constraints.copy(minHeight = height - firstHeight, maxHeight = height - firstHeight)
        )
        state.maxValue = firstPlaceable.height
        layout(constraints.maxWidth, constraints.maxHeight) {
            firstPlaceable.placeRelative(0, -state.value)
            secondPlaceable.placeRelative(0, firstHeight - state.value)
            bottomPlaceable.placeRelative(0, firstHeight + secondPlaceable.height - state.value)
        }
    }
}