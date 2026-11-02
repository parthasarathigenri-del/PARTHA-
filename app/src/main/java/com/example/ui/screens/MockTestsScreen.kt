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
import com.example.data.model.Student
import com.example.data.model.TestAttempt
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryBlue

@Composable
fun MockTestsScreen(
    currentStudent: Student,
    mockTests: List<MockTest>,
    attempts: List<TestAttempt>,
    isAdminMode: Boolean = false,
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI,
    onRequestTestAccess: (testId: String, testTitle: String) -> Unit = { _, _ -> },
    onStartTest: (String) -> Unit,
    onViewResult: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("সব পরীক্ষা") }
    val categories = listOf("সব পরীক্ষা", "WBCS Prelims", "WBP Police SI", "SSC CGL", "প্রাইমারি টেট")
    var requestSentNotice by remember { mutableStateOf<String?>(null) }

    val filteredTests = if (selectedCategory == "সব পরীক্ষা") {
        mockTests
    } else {
        mockTests.filter { it.category.contains(selectedCategory, ignoreCase = true) }
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
                            text = "সুরক্ষিত ৩-স্ট্রাইক লকডাউন মোড",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonRed
                        )
                        Text(
                            text = "পরীক্ষা চলাকালীন ১ম ও ২য় বার অ্যাপ বদলালে কড়া সতর্কবার্তা মিলবে। ৩য় বার নিয়ম ভাঙলে পরীক্ষাটি ফ্রোজেন হয়ে লক হয়ে যাবে এবং অ্যাডমিন আনফ্রিজ না করা পর্যন্ত শুরু করা যাবে না।",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (requestSentNotice != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen)
                        Text(requestSentNotice!!, color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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

        if (filteredTests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("বর্তমানে কোনো মক টেস্ট যুক্ত নেই", fontWeight = FontWeight.Bold)
                        Text("ইনস্টিটিউট অ্যাডমিন প্যানেল থেকে প্রশ্ন আপলোড করলেই তা এখানে প্রদর্শিত হবে।", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        items(filteredTests) { test ->
            val testAttempts = attempts.filter { it.mockTestId == test.id && it.studentId == currentStudent.id }
            val highestScore = testAttempts.maxOfOrNull { it.score } ?: 0.0
            val lastAttempt = testAttempts.maxByOrNull { it.completedAt }

            // Permission logic:
            // 1. Admin always has full access
            // 2. If test was previously completed by student -> can re-attempt freely
            // 3. If specifically approved in approvedTestIds -> can attempt
            // 4. Otherwise needs first-time access request approval from Admin!
            val hasCompletedBefore = currentStudent.completedTestIds.contains(test.id) || testAttempts.isNotEmpty()
            val hasExplicitApproval = currentStudent.approvedTestIds.contains(test.id)
            val canStart = isAdminMode || currentStudent.isAdmin || hasCompletedBefore || hasExplicitApproval

            val isFrozenForThisStudent = currentStudent.isTestFrozen && currentStudent.frozenTestId == test.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mock_test_card_${test.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                text = test.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }

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
                            Icon(imageVector = Icons.Default.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${test.questions.size} টি প্রশ্ন", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${test.durationMinutes} মিনিট", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "পূর্ণমান: ${test.totalMarks}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
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
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Action Button Logic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isFrozenForThisStudent) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CrimsonRed.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonRed)
                                    Text("🚨 ৩য় নিয়ম লঙ্ঘনের কারণে পরীক্ষাটি ফ্রোজেন। অ্যাডমিন আনফ্রিজ অনুমোদন দিলে পুনরায় শুরু করতে পারবেন।", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (test.isLockedByAdmin && !isAdminMode && !currentStudent.isAdmin) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CrimsonRed.copy(alpha = 0.12f)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(16.dp))
                                    Text("🔒 টেস্ট লকড (অ্যাডমিন উন্মুক্ত করেননি)", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (canStart) {
                            Button(
                                onClick = { onStartTest(test.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.testTag("start_test_btn_${test.id}")
                            ) {
                                Icon(
                                    imageVector = if (hasCompletedBefore) Icons.Default.Refresh else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (hasCompletedBefore) "বিনামূল্যে রি-অ্যাটেম্পট দিন" else "পরীক্ষা শুরু করুন", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // First-time access requires Admin Approval!
                            OutlinedButton(
                                onClick = {
                                    onRequestTestAccess(test.id, test.getTitle(language))
                                    requestSentNotice = "✅ '${test.getTitle(language)}' পরীক্ষার অনুমতির আবেদন প্রধান অ্যাডমিনের কাছে পাঠানো হয়েছে! অ্যাডমিন অনুমোদন দিলেই পরীক্ষা শুরু করতে পারবেন।"
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue),
                                modifier = Modifier.testTag("request_test_access_btn_${test.id}")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("অনুমোদনের আবেদন পাঠান (Request Permission)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
