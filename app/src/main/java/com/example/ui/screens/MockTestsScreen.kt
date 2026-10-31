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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MockTest
import com.example.data.model.TestAttempt
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun MockTestsScreen(
    mockTests: List<MockTest>,
    attempts: List<TestAttempt>,
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI,
    onStartTest: (String) -> Unit,
    onViewResult: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("সব পরীক্ষা") }
    val categories = listOf("সব পরীক্ষা", "WBCS Prelims", "WBP Police SI", "SSC CGL")

    val filteredTests = if (selectedCategory == "সব পরীক্ষা") {
        mockTests
    } else {
        mockTests.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("mock_tests_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Anti-cheating lockdown informational banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("anti_cheating_info_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "সুরক্ষিত লকডাউন মোড (Anti-Cheating)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                        Text(
                            text = "পরীক্ষা চলাকালীন ব্যাকগ্রাউন্ড উইন্ডো সুইচ বা কপি-পেস্ট প্রতিরোধক সক্রিয় থাকবে। ৩টি সতর্কবার্তায় অটো-সাবমিট হয়ে যাবে। পরীক্ষা শেষে রিভিউ মোডে ব্যাকগ্রাউন্ড খোলার অনুমতি মিলবে।",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
        }

        // Section header
        item {
            Text(
                text = "উপলব্ধ মক টেস্ট সিরিজ (${filteredTests.size} টি)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Mock test cards
        items(filteredTests) { test ->
            val testAttempts = attempts.filter { it.mockTestId == test.id }
            val highestScore = testAttempts.maxOfOrNull { it.score }
            val lastAttempt = testAttempts.firstOrNull()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_card_${test.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = test.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Negative marking badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CrimsonRed.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "নেগেটিভ: -${test.negativeMarkPerWrong}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed
                            )
                        }
                    }

                    Text(
                        text = test.getTitle(language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = test.getDescription(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${test.questions.size} টি প্রশ্ন",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${test.durationMinutes} মিনিট",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পূর্ণমান: ${test.totalMarks}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Attempt summary badge if attempted
                    if (testAttempts.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldGreen.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "মোট প্রচেষ্টা: ${testAttempts.size} বার | সর্বোচ্চ স্কোর: $highestScore",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                                if (lastAttempt != null) {
                                    TextButton(
                                        onClick = { onViewResult(lastAttempt.attemptId, test.id) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("রিভিউ", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockClock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "টাইমার ও অটো-সাবমিশন সক্রিয়",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (test.isLockedByAdmin) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CrimsonRed.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "🔒 টেস্ট লকড",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = CrimsonRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { onStartTest(test.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("start_test_btn_${test.id}")
                            ) {
                                Icon(
                                    imageVector = if (testAttempts.isEmpty()) Icons.Default.PlayArrow else Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (testAttempts.isEmpty()) "পরীক্ষা শুরু করুন" else "রি-অ্যাটেম্পট দিন")
                            }
                        }
                    }
                }
            }
        }
    }
}
