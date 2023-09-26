package com.ovt.nested_scroll_sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ovt.nested_scroll_sample.ui.theme.NestedscrollsampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NestedscrollsampleTheme {
                val showCollapsing = remember { mutableStateOf(false) }
                val showSearch = remember { mutableStateOf(false) }
                if (showCollapsing.value) {
                    Collapsing(showCollapsing)
                } else if (showSearch.value) {
                    SearchView(showSearch)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        lightColorScheme().primary,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .size(200.dp, 60.dp)
                                    .clickable {
                                        showCollapsing.value = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Collapsing",
                                    color = Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        lightColorScheme().primary,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .size(200.dp, 60.dp)
                                    .clickable {
                                        showSearch.value = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Search",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                BackHandler {
                    if (showCollapsing.value) {
                        showCollapsing.value = false
                    } else if (showSearch.value) {
                        showSearch.value = false
                    } else {
                        finish()
                    }
                }
            }
        }
    }
}