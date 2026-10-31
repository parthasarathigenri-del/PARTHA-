package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.MockTest
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveExamScreen(
    mockTest: MockTest,
    currentQuestionIndex: Int,
    selectedAnswers: Map<Int, Int>,
    timeRemainingSeconds: Int,
    violationStrikes: Int,
    isViolationDialogVisible: Boolean,
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI,
    onSelectAnswer: (Int, Int) -> Unit,
    onClearAnswer: (Int) -> Unit,
    onGoToQuestion: (Int) -> Unit,
    onReportViolation: () -> Unit,
    onDismissViolationDialog: () -> Unit,
    onSubmitExam: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }

    // 1. Anti-cheating Window Security: Set FLAG_SECURE during active test to prevent screen recording/screenshots
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // 2. Anti-cheating Lifecycle Tracker: Detect when user leaves app / switches window / minimizes during active test
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                // Cheating attempt: Student minimized or switched out to another app/browser window!
                onReportViolation()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Intercept back button during exam to prevent accidental exit
    BackHandler {
        showSubmitConfirmDialog = true
    }

    val currentQ = mockTest.questions.getOrNull(currentQuestionIndex) ?: return

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val isTimerLow = timeRemainingSeconds < 120 // < 2 mins

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = mockTest.getTitle(language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "প্রশ্ন ${currentQuestionIndex + 1} / ${mockTest.questions.size} • ${currentQ.getSubject(language)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Exam Countdown Timer Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTimerLow) CrimsonRed.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Exam Timer",
                                tint = if (isTimerLow) CrimsonRed else PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = String.format("%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isTimerLow) CrimsonRed else PrimaryBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentQuestionIndex > 0) onGoToQuestion(currentQuestionIndex - 1)
                        },
                        enabled = currentQuestionIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("exam_prev_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("পূর্ববর্তী")
                    }

                    if (selectedAnswers.containsKey(currentQuestionIndex)) {
                        TextButton(
                            onClick = { onClearAnswer(currentQuestionIndex) },
                            modifier = Modifier.testTag("clear_response_button")
                        ) {
                            Text("মুছে ফেলুন", color = CrimsonRed, fontSize = 12.sp)
                        }
                    }

                    if (currentQuestionIndex < mockTest.questions.size - 1) {
                        Button(
                            onClick = { onGoToQuestion(currentQuestionIndex + 1) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("exam_next_button")
                        ) {
                            Text("পরবর্তী")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("exam_final_submit_button")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("সাবমিট করুন")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Protected Exam Lockdown Warning Banner
            Surface(
                color = Navy900,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "লকডাউন পরীক্ষা মোড সক্রিয় (কপি ও ব্যাকগ্রাউন্ড উইন্ডো ব্লকড)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (violationStrikes > 0) CrimsonRed else EmeraldGreen
                    ) {
                        Text(
                            text = "স্ট্রাইক: $violationStrikes/৩",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Negative marking guidance bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সঠিক উত্তর: +${mockTest.markPerCorrect}",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "নেগেটিভ মার্কিং: -${mockTest.negativeMarkPerWrong}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "উত্তর প্রদান: ${selectedAnswers.size}/${mockTest.questions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Question Navigator Palette (Horizontally scrollable badges)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(mockTest.questions) { index, _ ->
                    val isCurrent = index == currentQuestionIndex
                    val isAnswered = selectedAnswers.containsKey(index)

                    val bgColor = when {
                        isCurrent -> PrimaryBlue
                        isAnswered -> EmeraldGreen
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val textColor = when {
                        isCurrent || isAnswered -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(bgColor)
                            .clickable { onGoToQuestion(index) }
                            .testTag("nav_question_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Main Question Body (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "প্রশ্ন #${currentQuestionIndex + 1} (${currentQ.getSubject(language)})",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Text(
                            text = currentQ.getQuestionText(language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Options List
                Text(
                    text = "সঠিক বিকল্পটি নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                currentQ.getOptions(language).forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedAnswers[currentQuestionIndex] == optIndex
                    val optionPrefix = when (optIndex) {
                        0 -> "A"
                        1 -> "B"
                        2 -> "C"
                        else -> "D"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectAnswer(currentQuestionIndex, optIndex) }
                            .testTag("option_${currentQuestionIndex}_$optIndex"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = optionPrefix,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Text(
                                text = optionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectAnswer(currentQuestionIndex, optIndex) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Violation Strike Modal Dialog
    if (isViolationDialogVisible) {
        AlertDialog(
            onDismissRequest = { /* Must click acknowledge */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CrimsonRed)
                    Text("⚠️ চিটিং প্রতিরোধক সতর্কবার্তা!", color = CrimsonRed, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "আপনি পরীক্ষা চলাকালীন অ্যাপ ছেড়ে অন্য উইন্ডো বা ব্রাউজারে যাওয়ার চেষ্টা করেছেন।",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "বর্তমান স্ট্রাইক: $violationStrikes / ৩\n" +
                                if (violationStrikes >= 3) {
                                    "🚨 সর্বোচ্চ ৩টি স্ট্রাইক অতিক্রম করায় পরীক্ষা তাৎক্ষণিক বাতিল ও স্বয়ংক্রিয়ভাবে জমা করা হচ্ছে!"
                                } else {
                                    "মনে রাখবেন, ৩টি স্ট্রাইক পূর্ণ হলে পরীক্ষা স্বয়ংক্রিয়ভাবে সাবমিট হয়ে যাবে। পরীক্ষা শেষে রিভিউ মোডে ব্যাকগ্রাউন্ড খোলার অনুমতি পাবেন।"
                                },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissViolationDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("আমি বুঝতে পেরেছি")
                }
            }
        )
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmDialog) {
        val answeredCount = selectedAnswers.size
        val unansweredCount = mockTest.questions.size - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("মক টেস্ট সমাপ্ত ও জমা নিশ্চিতকরণ") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("আপনি কি নিশ্চিত যে আপনি পরীক্ষা জমা দিতে চান?")
                    Text("মোট প্রশ্ন: ${mockTest.questions.size}")
                    Text("উত্তর প্রদান করেছেন: $answeredCount", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    Text("অনুত্তরিত বাকি: $unansweredCount", color = CrimsonRed, fontWeight = FontWeight.Bold)
                    Text(
                        "ভুল উত্তরের জন্য প্রতিটিতে -${mockTest.negativeMarkPerWrong} নেগেটিভ মার্কিং প্রযোজ্য হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        onSubmitExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("confirm_submit_exam_button")
                ) {
                    Text("হ্যাঁ, ফাইনাল সাবমিট করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("পরীক্ষায় ফিরে যান")
                }
            }
        )
    }
}
