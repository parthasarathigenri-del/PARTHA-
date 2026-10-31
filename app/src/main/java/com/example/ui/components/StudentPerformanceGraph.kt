package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerformanceTrend
import com.example.data.model.Student
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun StudentPerformanceGraphCard(
    student: Student,
    modifier: Modifier = Modifier
) {
    val scores = student.scoresHistory
    val trend = student.trend
    val percentChange = student.trendPercentChange

    val trendColor = when (trend) {
        PerformanceTrend.IMPROVING -> EmeraldGreen
        PerformanceTrend.DECLINING -> CrimsonRed
        PerformanceTrend.STABLE -> AccentAmber
    }

    val trendTitle = when (trend) {
        PerformanceTrend.IMPROVING -> "📈 পারফরম্যান্স ক্রমান্বয়ে বাড়ছে (Improving Trend)"
        PerformanceTrend.DECLINING -> "📉 পারফরম্যান্স কমছে (Declining Trend)"
        PerformanceTrend.STABLE -> "⚖️ পারফরম্যান্স একই আছে (Stable / Consistent)"
    }

    val trendSubtitle = when (trend) {
        PerformanceTrend.IMPROVING -> "চমৎকার উন্নতি! ধারাবাহিক অনুশীলন চালিয়ে যান (+$percentChange% প্রবৃদ্ধি)।"
        PerformanceTrend.DECLINING -> "সতর্কতা: সাম্প্রতিক মক টেস্টগুলোতে নম্বর হ্রাস পেয়েছে ($percentChange%)। শিক্ষকদের পরামর্শ নিন।"
        PerformanceTrend.STABLE -> "ধারাবাহিকতা বজায় রয়েছে। স্কোর বাড়াতে দুর্বল বিষয়গুলোতে জোর দিন।"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_performance_graph_card_${student.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student Title & Trend Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${student.name} (${student.rollNo})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "লক্ষ্য: ${student.targetExam} • ${student.batch}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = trendColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (trend) {
                                PerformanceTrend.IMPROVING -> Icons.Default.TrendingUp
                                PerformanceTrend.DECLINING -> Icons.Default.TrendingDown
                                PerformanceTrend.STABLE -> Icons.Default.TrendingFlat
                            },
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (trend) {
                                PerformanceTrend.IMPROVING -> "বাড়ছে (+$percentChange%)"
                                PerformanceTrend.DECLINING -> "কমছে ($percentChange%)"
                                PerformanceTrend.STABLE -> "একই আছে"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }

            // Summary notification bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = trendColor.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = trendTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = trendColor
                    )
                    Text(
                        text = trendSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Canvas Graph View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val maxScore = 100f
                    val minScore = 0f

                    // 1. Draw horizontal guide lines (0%, 25%, 50%, 75%, 100%)
                    val gridLines = listOf(0.25f, 0.50f, 0.75f)
                    gridLines.forEach { ratio ->
                        val y = height * (1f - ratio)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    if (scores.size < 2) return@Canvas

                    val stepX = width / (scores.size - 1)
                    val points = scores.mapIndexed { index, score ->
                        val x = index * stepX
                        val normalizedScore = (score.toFloat() - minScore) / (maxScore - minScore)
                        val y = height * (1f - normalizedScore.coerceIn(0.1f, 0.95f))
                        Offset(x, y)
                    }

                    // 2. Draw smooth filled gradient area under the line
                    val fillPath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                trendColor.copy(alpha = 0.35f),
                                trendColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // 3. Draw trend stroke line
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = strokePath,
                        color = trendColor,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw data point circles & badges
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = trendColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            // X-axis Mock Test milestone tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                scores.forEachIndexed { idx, score ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${score.toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                        Text(
                            text = "টেস্ট ${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Metric boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("শুরুতে স্কোর", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${scores.first().toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("বর্তমান স্কোর", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${scores.last().toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = trendColor)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("মোট পরিবর্তন", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (percentChange >= 0) "+$percentChange%" else "$percentChange%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }
                }
            }
        }
    }
}
