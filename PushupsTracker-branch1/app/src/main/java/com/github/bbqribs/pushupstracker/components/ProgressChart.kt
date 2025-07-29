// File: app/src/main/java/com/github/bbqribs/pushupstracker/ui/components/ProgressChart.kt
package com.github.bbqribs.pushupstracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.bbqribs.pushupstracker.data.Attempt
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressChart(
    attempts: List<Attempt>,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(attempts) {
        val sessionAttempts = attempts.filter { it.outcome != "TEST" }

        val entries: List<ChartEntry> = sessionAttempts.flatMapIndexed { xIndex, attempt ->
            val yValues = attempt.setsCompleted.split('|').mapNotNull { it.toFloatOrNull() }
            yValues.map { yValue ->
                entryOf(xIndex.toFloat(), yValue)
            }
        }
        modelProducer.setEntries(entries)
    }

    val pastelBlue = Color(0xFFADC6FF)
    val pastelYellow = Color(0xFFFDFFB6)

    val sessionComponents = remember(pastelBlue) {
        listOf(
            LineComponent(pastelBlue.toArgb(), 12f, Shapes.roundedCornerShape(topLeftPercent = 25, topRightPercent = 25)),
            // ✅ FIX: Use RectShape() instead of Shapes.rect
            LineComponent(pastelBlue.copy(alpha = 0.8f).toArgb(), 12f, shape = Shapes.rectShape),
            LineComponent(pastelBlue.copy(alpha = 0.6f).toArgb(), 12f, shape = Shapes.rectShape),
            LineComponent(pastelBlue.copy(alpha = 0.4f).toArgb(), 12f, shape = Shapes.rectShape),
            LineComponent(pastelBlue.copy(alpha = 0.2f).toArgb(), 12f, shape = Shapes.rectShape),
        )
    }

    val testColumn = remember(pastelYellow) {
        LineComponent(pastelYellow.toArgb(), 12f, Shapes.roundedCornerShape(topLeftPercent = 25, topRightPercent = 25))
    }

    Chart(
        chart = columnChart(
            columns = if (attempts.any { it.outcome != "TEST" }) sessionComponents else listOf(testColumn)
        ),
        chartModelProducer = modelProducer,
        modifier = modifier,
        startAxis = rememberStartAxis(
            title = "Total Reps",
            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 6)
        ),
        bottomAxis = rememberBottomAxis(
            title = "Date",
            valueFormatter = { value, _ ->
                val sessionAttempts = attempts.filter { it.outcome != "TEST" }
                val index = value.toInt()
                if (index in sessionAttempts.indices) {
                    val timestamp = sessionAttempts[index].timestamp
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
                } else {
                    ""
                }
            }
        ),
    )
}