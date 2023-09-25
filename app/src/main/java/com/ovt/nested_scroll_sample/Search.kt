package com.ovt.nested_scroll_sample

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.ovt.nested_scroll_sample.ui.theme.Pink40
import com.ovt.nested_scroll_sample.ui.theme.Pink80
import com.ovt.nested_scroll_sample.ui.theme.Purple40
import com.ovt.nested_scroll_sample.ui.theme.Purple80
import com.ovt.nested_scroll_sample.ui.theme.PurpleGrey40
import com.ovt.nested_scroll_sample.ui.theme.PurpleGrey80
import kotlin.math.roundToInt

@Composable
fun rememberSearchState(): SearchState {
    return remember { SearchState() }
}

@Stable
class SearchState {
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
    var cardHeight: Int
        get() = _cardHeightState.value
        internal set(newHeight) {
            _cardHeightState.value = newHeight
        }
    private var _maxValueState = mutableStateOf(Int.MAX_VALUE)
    private var _cardHeightState = mutableStateOf(Int.MAX_VALUE)
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

    val canScrollForward by derivedStateOf { value < maxValue }
    val canScrollForward1 by derivedStateOf { value < cardHeight }
    val canScrollForward2 by derivedStateOf { value in cardHeight..maxValue }

    suspend fun scrollTo(scrollY: Float): Float = scrollableState.scrollBy(scrollY - this.value)

    suspend fun animateScrollTo(
        scrollY: Float,
        animationSpec: AnimationSpec<Float> = SpringSpec()
    ) {
        scrollableState.animateScrollBy(scrollY - this.value, animationSpec)
    }

    internal val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return if (available.y < 0) consume(available)
            else if (available.y > 0 && canScrollForward2) {
                val deltaY = available.y.coerceAtMost((value - cardHeight).toFloat())
                consume(available.copy(y = deltaY))
            }
            else super.onPreScroll(available, source)
        }
    }
}

@Composable
fun Search(
    state: SearchState = rememberSearchState()
) {
    Layout(
        content = {
            // TopBar()
            Text(
                text = "TopBar",
                color = Color.White,
                fontSize = 50.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.verticalGradient(listOf(Purple40, Purple80)))
            )
            // ShopCard()
            Text(
                text = "ShopCard",
                color = Color.White,
                fontSize = 50.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(listOf(Pink40, Pink80)),
                        alpha = 1 - state.value / state.maxValue.toFloat()
                    )
                    .alpha(1 - state.value / state.maxValue.toFloat())
            )
            // SortBar()
            Text(
                text = "SortBar",
                color = Color.White,
                fontSize = 50.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.verticalGradient(listOf(PurpleGrey40, PurpleGrey80)))
            )
            // CommodityList()
            List()
        },
        modifier = Modifier
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
        val secondPlaceable = measurables[1].measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val thirdPlaceable = measurables[2].measure(
            constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
        )
        val bottomPlaceable = measurables[3].measure(
            constraints.copy(minHeight = height, maxHeight = height)
        )
        state.maxValue = secondPlaceable.height + firstPlaceable.height + thirdPlaceable.height
        state.cardHeight = secondPlaceable.height
        layout(constraints.maxWidth, constraints.maxHeight) {
            secondPlaceable.placeRelative(0, firstPlaceable.height - state.value)
            // TopBar 覆盖在 ShopCard 上面，所以后放置
            firstPlaceable.placeRelative(
                0,
                secondPlaceable.height - state.value.coerceAtLeast(secondPlaceable.height)
            )
            thirdPlaceable.placeRelative(
                0,
                firstPlaceable.height + secondPlaceable.height - state.value
            )
            bottomPlaceable.placeRelative(
                0,
                firstPlaceable.height + secondPlaceable.height + thirdPlaceable.height - state.value
            )
        }
    }
}
