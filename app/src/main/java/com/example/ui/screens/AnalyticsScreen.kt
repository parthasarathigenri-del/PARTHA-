package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerformanceTrend
import com.example.data.model.Student
import com.example.data.model.TestAttempt
import com.example.ui.components.StudentPerformanceGraphCard
import com.example.ui.theme.*

data class LeaderboardEntry(
    val rank: Int,
    val student: Student,
    val score: Double,
    val testsAttempted: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun AnalyticsScreen(
    student: Student,
    attempts: List<TestAttempt>,
    allStudents: List<Student>
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("আমার গ্রাফ ও স্কোর", "সকল শিক্ষার্থীর গ্রাফ", "লিডারবোর্ড", "উপস্থিতি রিপোর্ট")

    // State for selected peer student to view graph
    var selectedPeerStudent by remember { mutableStateOf(allStudents.firstOrNull() ?: student) }
    var modalStudentGraph by remember { mutableStateOf<Student?>(null) }

    val totalActiveHours = student.activeTimeSeconds / 3600
    val totalActiveMinutes = (student.activeTimeSeconds % 3600) / 60

    val averageScore = if (attempts.isNotEmpty()) {
        Math.round((attempts.map { it.score }.average()) * 100.0) / 100.0
    } else 0.0

    val totalCorrect = attempts.sumOf { it.correctCount }
    val totalIncorrect = attempts.sumOf { it.incorrectCount }
    val accuracy = if (totalCorrect + totalIncorrect > 0) {
        ((totalCorrect.toDouble() / (totalCorrect + totalIncorrect)) * 100).toInt()
    } else 0

    // Leaderboard entries linked to real student models
    val leaderboard = listOf(
        LeaderboardEntry(1, allStudents.getOrElse(1) { student }, 28.5, 4),
        LeaderboardEntry(2, student, 26.25, attempts.size + 2, isCurrentUser = true),
        LeaderboardEntry(3, allStudents.getOrElse(2) { student }, 24.0, 3),
        LeaderboardEntry(4, allStudents.getOrElse(4) { student }, 21.75, 3),
        LeaderboardEntry(5, allStudents.getOrElse(5) { student }, 18.5, 2)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("analytics_screen_container")
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.testTag("analytics_tab_$index")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: Current Student's Performance Graph & Scorecard
                    item {
                        StudentPerformanceGraphCard(student = student)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Navy900, PrimaryBlue)
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "${student.name}-এর স্টুডেন্ট প্রোফাইল",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "রোল: ${student.rollNo} • ব্যাচ: ${student.batch}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )

                                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("গড় স্কোর (Average)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                            Text("$averageScore", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AccentAmber)
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("নিখুঁত হার (Accuracy)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                            Text("$accuracy%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("মোট সক্রিয় সময়", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                            Text("${totalActiveHours}ঘ ${totalActiveMinutes}মি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Subject-wise accuracy breakdown
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "বিষয়ভিত্তিক দক্ষতা ও পারফরম্যান্স এনালাইসিস",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                SubjectProgressItem(subject = "ভারতের ইতিহাস (History)", accuracyPercent = 88, color = PrimaryBlue)
                                SubjectProgressItem(subject = "পশ্চিমবঙ্গের ভূগোল (Geography)", accuracyPercent = 82, color = Color(0xFF0EA5E9))
                                SubjectProgressItem(subject = "সংবিধান ও রাষ্ট্রনীতি (Polity)", accuracyPercent = 90, color = EmeraldGreen)
                                SubjectProgressItem(subject = "পাটিগণিত ও ম্যাথ (Math)", accuracyPercent = 75, color = AccentAmber)
                                SubjectProgressItem(subject = "সাধারণ বিজ্ঞান (Science)", accuracyPercent = 80, color = Color(0xFF8B5CF6))
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: Every Student's Performance Graph (Peer / Teacher Visibility)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(28.dp))
                                Column {
                                    Text(
                                        text = "প্রতিটি শিক্ষার্থীর অগ্রগতি গ্রাফ (Peer & Teacher View)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "যেকোনো শিক্ষার্থীর নামের উপর ট্যাপ করে দেখুন তার পরীক্ষার ফলাফল ক্রমান্বয়ে বাড়ছে, কমছে, নাকি স্থিতিশীল রয়েছে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Student horizontal selector chips
                    item {
                        Text(
                            text = "শিক্ষার্থী নির্বাচন করুন:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(allStudents) { peer ->
                                val isSelected = selectedPeerStudent.id == peer.id
                                val trendIcon = when (peer.trend) {
                                    PerformanceTrend.IMPROVING -> Icons.Default.TrendingUp
                                    PerformanceTrend.DECLINING -> Icons.Default.TrendingDown
                                    PerformanceTrend.STABLE -> Icons.Default.TrendingFlat
                                }
                                val trendColor = when (peer.trend) {
                                    PerformanceTrend.IMPROVING -> EmeraldGreen
                                    PerformanceTrend.DECLINING -> CrimsonRed
                                    PerformanceTrend.STABLE -> AccentAmber
                                }

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPeerStudent = peer },
                                    label = { Text(peer.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    trailingIcon = {
                                        Icon(imageVector = trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                                    },
                                    modifier = Modifier.testTag("student_chip_${peer.id}")
                                )
                            }
                        }
                    }

                    // The selected student's detailed graph
                    item {
                        StudentPerformanceGraphCard(student = selectedPeerStudent)
                    }

                    item {
                        Text(
                            text = "সকল শিক্ষার্থীর পারফরম্যান্স তালিকা ও ট্রেন্ড সারাংশ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allStudents) { peer ->
                        val trendColor = when (peer.trend) {
                            PerformanceTrend.IMPROVING -> EmeraldGreen
                            PerformanceTrend.DECLINING -> CrimsonRed
                            PerformanceTrend.STABLE -> AccentAmber
                        }
                        val trendIcon = when (peer.trend) {
                            PerformanceTrend.IMPROVING -> Icons.Default.TrendingUp
                            PerformanceTrend.DECLINING -> Icons.Default.TrendingDown
                            PerformanceTrend.STABLE -> Icons.Default.TrendingFlat
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPeerStudent = peer },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedPeerStudent.id == peer.id) PrimaryBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (selectedPeerStudent.id == peer.id) BorderStroke(1.5.dp, PrimaryBlue) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = trendColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(imageVector = trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Column {
                                        Text(text = peer.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(text = "রোল: ${peer.rollNo} • লক্ষ্য: ${peer.targetExam}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = trendColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = when (peer.trend) {
                                            PerformanceTrend.IMPROVING -> "বাড়ছে 📈 (+${peer.trendPercentChange}%)"
                                            PerformanceTrend.DECLINING -> "কমছে 📉 (${peer.trendPercentChange}%)"
                                            PerformanceTrend.STABLE -> "একই আছে ⚖️"
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = trendColor
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: Batch Leaderboard
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "কম্পিটিটিভ পাঠশালা মেধা তালিকা (Leaderboard)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "যেকোনো শিক্ষার্থীর নামের উপর ট্যাপ করে তার সম্পূর্ণ পারফরম্যান্স গ্রাফ দেখতে পারেন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(leaderboard) { _, entry ->
                        val medalColor = when (entry.rank) {
                            1 -> Color(0xFFFFD700)
                            2 -> Color(0xFFC0C0C0)
                            3 -> Color(0xFFCD7F32)
                            else -> MaterialTheme.colorScheme.outline
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { modalStudentGraph = entry.student },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (entry.isCurrentUser) PrimaryBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = if (entry.isCurrentUser) BorderStroke(1.5.dp, PrimaryBlue) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (entry.rank <= 3) medalColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${entry.rank}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.rank <= 3) medalColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.student.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (entry.isCurrentUser) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = PrimaryBlue
                                            ) {
                                                Text(
                                                    text = "আপনি",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "রোল: ${entry.student.rollNo} • মক টেস্ট: ${entry.testsAttempted} টি",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { modalStudentGraph = entry.student },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("গ্রাফ", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 4: Batch Activity & Attendance Report
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📊 শিক্ষার্থী সক্রিয়তা ও উপস্থিতি রিপোর্ট",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ইনস্টিটিউটের কতজন শিক্ষার্থী কতবার মক টেস্টে অংশ নিয়েছে এবং অ্যাপে কতক্ষণ সক্রিয় রয়েছে তার বিস্তারিত রিপোর্ট।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    itemsIndexed(allStudents) { _, std ->
                        val hours = std.activeTimeSeconds / 3600
                        val mins = (std.activeTimeSeconds % 3600) / 60
                        val attCount = if (std.id == student.id) attempts.size else (1..4).random()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { modalStudentGraph = std },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = std.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(text = "রোল: ${std.rollNo} • লক্ষ্য: ${std.targetExam}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$attCount বার অ্যাটেন্ড",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = "${hours}ঘ ${mins}মি সক্রিয়",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal popup to view any student's performance trajectory graph
    if (modalStudentGraph != null) {
        val targetStudent = modalStudentGraph!!
        AlertDialog(
            onDismissRequest = { modalStudentGraph = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = PrimaryBlue)
                    Text("${targetStudent.name}-এর পারফরম্যান্স গ্রাফ")
                }
            },
            text = {
                StudentPerformanceGraphCard(student = targetStudent)
            },
            confirmButton = {
                Button(onClick = { modalStudentGraph = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}

@Composable
fun SubjectProgressItem(subject: String, accuracyPercent: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = subject, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(text = "$accuracyPercent%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { accuracyPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
        )
    }
}
