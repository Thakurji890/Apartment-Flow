package com.example.feature.analytics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.feature.analytics.domain.model.CategorySpending
import com.example.feature.analytics.domain.model.TrendPoint

@Composable
fun SimpleDonutChart(
    data: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    Canvas(modifier = modifier) {
        val strokeWidth = size.width * 0.15f
        val radius = (size.minDimension - strokeWidth) / 2
        
        var currentAngle = -90f
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.percentage / 100f) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            currentAngle += sweepAngle
        }
    }
}

@Composable
fun SimpleBarChart(
    data: List<TrendPoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No trend data", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxAmount = data.maxOf { it.amount }
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barSpacing = 8.dp.toPx()
        val barWidth = (width - (barSpacing * (data.size - 1))) / data.size
        
        data.forEachIndexed { index, point ->
            val barHeight = if (maxAmount > 0) (point.amount / maxAmount).toFloat() * height else 0f
            val x = index * (barWidth + barSpacing)
            val y = height - barHeight
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
