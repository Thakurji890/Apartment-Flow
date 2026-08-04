package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    }
}

@Composable
fun ElevatedStandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        ElevatedCard(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    } else {
        ElevatedCard(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            }
        )
    }
}
