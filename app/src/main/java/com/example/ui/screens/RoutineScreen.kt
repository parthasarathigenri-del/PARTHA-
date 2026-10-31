package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ExamRoutine
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue

@Composable
fun RoutineScreen(
    routines: List<ExamRoutine>,
    language: AppLanguage,
    onStartTest: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("সকল (All)") }
    val filters = listOf("সকল (All)", "WBCS 2026", "PSC Clerkship", "WBP / KP SI")

    val filteredRoutines = remember(selectedFilter, routines) {
        if (selectedFilter == "সকল (All)") routines
        else routines.filter { it.targetExam.contains(selectedFilter.split(" ").first()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("routine_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentAmber.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = AccentAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = when (language) {
                                    AppLanguage.BENGALI -> "ইনস্টিটিউট পরীক্ষার রুটিন ও সময়সূচী"
                                    AppLanguage.ENGLISH -> "Institute Exam Routine & Schedule"
                                    AppLanguage.HINDI -> "संस्थान परीक्षा दिनचर्या व समय सारणी"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "সাপ্তাহিক ও মাসিক মক টেস্ট সময়সীমা",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }

        // Exam Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        leadingIcon = if (selectedFilter == filter) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        items(filteredRoutines) { routine ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routine_card_${routine.id}"),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = routine.targetExam,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${routine.durationMinutes} মি.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = when (language) {
                            AppLanguage.BENGALI -> routine.title
                            AppLanguage.ENGLISH -> if (routine.titleEn.isNotEmpty()) routine.titleEn else routine.title
                            AppLanguage.HINDI -> if (routine.titleHi.isNotEmpty()) routine.titleHi else routine.title
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = EmeraldGreen
                            )
                            Text(
                                text = routine.date,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AccentAmber
                            )
                            Text(
                                text = routine.timeSlot,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "সিলেবাস:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = routine.syllabus,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "পূর্ণমান: ${routine.totalMarks} • নেগেটিভ: -${routine.negativeMarkRate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (routine.isMockReady) {
                            Button(
                                onClick = { onStartTest(routine.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("start_routine_mock_${routine.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("টেস্টে বসুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
