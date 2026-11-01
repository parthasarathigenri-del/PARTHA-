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
    perQuestionTimeSeconds: Map<Int, Int> = emptyMap(),
    violationStrikes: Int,
    isViolationDialogVisible: Boolean,
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.BENGALI,
    onLanguageChange: (com.example.data.model.AppLanguage) -> Unit = {},
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
    var currentExamLanguage by remember(language) { mutableStateOf(language) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

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
                            text = mockTest.getTitle(currentExamLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = when (currentExamLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "প্রশ্ন ${currentQuestionIndex + 1} / ${mockTest.questions.size} • ${currentQ.getSubject(currentExamLanguage)}"
                                com.example.data.model.AppLanguage.ENGLISH -> "Question ${currentQuestionIndex + 1} / ${mockTest.questions.size} • ${currentQ.getSubject(currentExamLanguage)}"
                                com.example.data.model.AppLanguage.HINDI -> "प्रश्न ${currentQuestionIndex + 1} / ${mockTest.questions.size} • ${currentQ.getSubject(currentExamLanguage)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Quick Language Selector Dropdown in AppBar
                    Box(modifier = Modifier.padding(end = 4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showLanguageDropdown = true }
                                .testTag("exam_language_dropdown_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Test Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = currentExamLanguage.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageDropdown,
                            onDismissRequest = { showLanguageDropdown = false }
                        ) {
                            com.example.data.model.AppLanguage.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (currentExamLanguage == lang) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = EmeraldGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = when (lang) {
                                                    com.example.data.model.AppLanguage.BENGALI -> "বাংলা (Bengali)"
                                                    com.example.data.model.AppLanguage.ENGLISH -> "English (ইংরেজি)"
                                                    com.example.data.model.AppLanguage.HINDI -> "हिन्दी (Hindi)"
                                                },
                                                fontWeight = if (currentExamLanguage == lang) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        currentExamLanguage = lang
                                        onLanguageChange(lang)
                                        showLanguageDropdown = false
                                    },
                                    modifier = Modifier.testTag("exam_menu_lang_${lang.code}")
                                )
                            }
                        }
                    }

                    // Exam Countdown Timer Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isTimerLow) CrimsonRed.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.12f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Exam Timer",
                                tint = if (isTimerLow) CrimsonRed else PrimaryBlue,
                                modifier = Modifier.size(16.dp)
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
                    val prevText = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "পূর্ববর্তী"
                        com.example.data.model.AppLanguage.ENGLISH -> "Previous"
                        com.example.data.model.AppLanguage.HINDI -> "पिछला"
                    }
                    val clearText = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "মুছে ফেলুন"
                        com.example.data.model.AppLanguage.ENGLISH -> "Clear"
                        com.example.data.model.AppLanguage.HINDI -> "साफ़ करें"
                    }
                    val nextText = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "পরবর্তী"
                        com.example.data.model.AppLanguage.ENGLISH -> "Next"
                        com.example.data.model.AppLanguage.HINDI -> "अगला"
                    }
                    val submitText = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "সাবমিট করুন"
                        com.example.data.model.AppLanguage.ENGLISH -> "Submit Exam"
                        com.example.data.model.AppLanguage.HINDI -> "सबमिट करें"
                    }

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
                        Text(prevText)
                    }

                    if (selectedAnswers.containsKey(currentQuestionIndex)) {
                        TextButton(
                            onClick = { onClearAnswer(currentQuestionIndex) },
                            modifier = Modifier.testTag("clear_response_button")
                        ) {
                            Text(clearText, color = CrimsonRed, fontSize = 12.sp)
                        }
                    }

                    if (currentQuestionIndex < mockTest.questions.size - 1) {
                        Button(
                            onClick = { onGoToQuestion(currentQuestionIndex + 1) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("exam_next_button")
                        ) {
                            Text(nextText)
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
                            Text(submitText)
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
                            text = when (currentExamLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "লকডাউন পরীক্ষা মোড সক্রিয় (কপি ও ব্যাকগ্রাউন্ড উইন্ডো ব্লকড)"
                                com.example.data.model.AppLanguage.ENGLISH -> "Lockdown Exam Mode Active (Copy & Background blocked)"
                                com.example.data.model.AppLanguage.HINDI -> "लॉकडाउन परीक्षा मोड सक्रिय (कॉपी व बैकग्राउंड ब्लॉक)"
                            },
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
                            text = when (currentExamLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "স্ট্রাইক: $violationStrikes/৩"
                                com.example.data.model.AppLanguage.ENGLISH -> "Strikes: $violationStrikes/3"
                                com.example.data.model.AppLanguage.HINDI -> "स्ट्राइक: $violationStrikes/3"
                            },
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
                    val correctLabel = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "সঠিক: +${mockTest.markPerCorrect}"
                        com.example.data.model.AppLanguage.ENGLISH -> "Correct: +${mockTest.markPerCorrect}"
                        com.example.data.model.AppLanguage.HINDI -> "सही: +${mockTest.markPerCorrect}"
                    }
                    val wrongLabel = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "নেগেটিভ: -${mockTest.negativeMarkPerWrong}"
                        com.example.data.model.AppLanguage.ENGLISH -> "Negative: -${mockTest.negativeMarkPerWrong}"
                        com.example.data.model.AppLanguage.HINDI -> "नेगेटिव: -${mockTest.negativeMarkPerWrong}"
                    }
                    val answeredLabel = when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "উত্তর: ${selectedAnswers.size}/${mockTest.questions.size}"
                        com.example.data.model.AppLanguage.ENGLISH -> "Done: ${selectedAnswers.size}/${mockTest.questions.size}"
                        com.example.data.model.AppLanguage.HINDI -> "उत्तर: ${selectedAnswers.size}/${mockTest.questions.size}"
                    }

                    Text(
                        text = correctLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = wrongLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = answeredLabel,
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive In-Test Language Segmented Switcher (সরাসরি টেস্ট চলাকালীন ১-ট্যাপে ভাষা বদল)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when (currentExamLanguage) {
                                    com.example.data.model.AppLanguage.BENGALI -> "টেস্ট ভাষা:"
                                    com.example.data.model.AppLanguage.ENGLISH -> "Exam Lang:"
                                    com.example.data.model.AppLanguage.HINDI -> "टेस्ट भाषा:"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // 3 Segmented Pills: বাংলা | English | हिन्दी
                        Row(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf(
                                com.example.data.model.AppLanguage.BENGALI to "বাংলা",
                                com.example.data.model.AppLanguage.ENGLISH to "English",
                                com.example.data.model.AppLanguage.HINDI to "हिन्दी"
                            ).forEach { (lang, label) ->
                                val isSelected = currentExamLanguage == lang
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                        .clickable {
                                            currentExamLanguage = lang
                                            onLanguageChange(lang)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("in_exam_lang_pill_${lang.code}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val qBadgeText = when (currentExamLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "প্রশ্ন #${currentQuestionIndex + 1} (${currentQ.getSubject(currentExamLanguage)})"
                                com.example.data.model.AppLanguage.ENGLISH -> "Question #${currentQuestionIndex + 1} (${currentQ.getSubject(currentExamLanguage)})"
                                com.example.data.model.AppLanguage.HINDI -> "प्रश्न #${currentQuestionIndex + 1} (${currentQ.getSubject(currentExamLanguage)})"
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = qBadgeText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Individual Question Timer (প্রতিটি প্রশ্নের স্বতন্ত্র টাইমার)
                            val qSeconds = perQuestionTimeSeconds[currentQuestionIndex] ?: 0
                            val qMin = qSeconds / 60
                            val qSec = qSeconds % 60
                            val qTimerLabel = when (currentExamLanguage) {
                                com.example.data.model.AppLanguage.BENGALI -> "এই প্রশ্নে: ${String.format("%02d:%02d", qMin, qSec)}"
                                com.example.data.model.AppLanguage.ENGLISH -> "Time: ${String.format("%02d:%02d", qMin, qSec)}"
                                com.example.data.model.AppLanguage.HINDI -> "समय: ${String.format("%02d:%02d", qMin, qSec)}"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AccentAmber.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassBottom,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = qTimerLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Text(
                            text = currentQ.getQuestionText(currentExamLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Options List
                val optionsPrompt = when (currentExamLanguage) {
                    com.example.data.model.AppLanguage.BENGALI -> "সঠিক বিকল্পটি নির্বাচন করুন:"
                    com.example.data.model.AppLanguage.ENGLISH -> "Select the correct option:"
                    com.example.data.model.AppLanguage.HINDI -> "सही विकल्प का चयन करें:"
                }

                Text(
                    text = optionsPrompt,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                currentQ.getOptions(currentExamLanguage).forEachIndexed { optIndex, optionText ->
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
                    Text(
                        text = when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "⚠️ চিটিং প্রতিরোধক সতর্কবার্তা!"
                            com.example.data.model.AppLanguage.ENGLISH -> "⚠️ Anti-Cheating Warning!"
                            com.example.data.model.AppLanguage.HINDI -> "⚠️ चीटिंग-रोधी चेतावनी!"
                        },
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "আপনি পরীক্ষা চলাকালীন অ্যাপ ছেড়ে অন্য উইন্ডো বা ব্রাউজারে যাওয়ার চেষ্টা করেছেন।"
                            com.example.data.model.AppLanguage.ENGLISH -> "You attempted to switch windows or minimize the app during the active test."
                            com.example.data.model.AppLanguage.HINDI -> "आपने परीक्षा के दौरान ऐप छोड़कर अन्य विंडो या ब्राउज़र पर जाने का प्रयास किया।"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "বর্তমান স্ট্রাইক: $violationStrikes / ৩\n" +
                                    if (violationStrikes >= 3) {
                                        "🚨 সর্বোচ্চ ৩টি স্ট্রাইক অতিক্রম করায় পরীক্ষা তাৎক্ষণিক বাতিল ও স্বয়ংক্রিয়ভাবে জমা করা হচ্ছে!"
                                    } else {
                                        "মনে রাখবেন, ৩টি স্ট্রাইক পূর্ণ হলে পরীক্ষা স্বয়ংক্রিয়ভাবে সাবমিট হয়ে যাবে। পরীক্ষা শেষে রিভিউ মোডে ব্যাকগ্রাউন্ড খোলার অনুমতি পাবেন।"
                                    }
                            com.example.data.model.AppLanguage.ENGLISH -> "Current strikes: $violationStrikes / 3\n" +
                                    if (violationStrikes >= 3) {
                                        "🚨 3 strikes reached. Exam is being immediately submitted!"
                                    } else {
                                        "Remember, on 3 strikes the test will be auto-submitted. Background references are allowed in Review mode after submission."
                                    }
                            com.example.data.model.AppLanguage.HINDI -> "वर्तमान स्ट्राइक: $violationStrikes / 3\n" +
                                    if (violationStrikes >= 3) {
                                        "🚨 3 स्ट्राइक पार होने पर परीक्षा तुरंत स्वतः सबमिट की जा रही है!"
                                    } else {
                                        "याद रखें, 3 स्ट्राइक होने पर परीक्षा स्वतः सबमिट हो जाएगी।"
                                    }
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
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "আমি বুঝতে পেরেছি"
                            com.example.data.model.AppLanguage.ENGLISH -> "I Understand"
                            com.example.data.model.AppLanguage.HINDI -> "मैं समझ गया"
                        }
                    )
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
            title = {
                Text(
                    when (currentExamLanguage) {
                        com.example.data.model.AppLanguage.BENGALI -> "মক টেস্ট সমাপ্ত ও জমা নিশ্চিতকরণ"
                        com.example.data.model.AppLanguage.ENGLISH -> "Confirm Exam Submission"
                        com.example.data.model.AppLanguage.HINDI -> "मॉक टेस्ट सबमिट करने की पुष्टि"
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "আপনি কি নিশ্চিত যে আপনি পরীক্ষা জমা দিতে চান?"
                            com.example.data.model.AppLanguage.ENGLISH -> "Are you sure you want to finish and submit this test?"
                            com.example.data.model.AppLanguage.HINDI -> "क्या आप वाकई परीक्षा सबमिट करना चाहते हैं?"
                        }
                    )
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "মোট প্রশ্ন: ${mockTest.questions.size}"
                            com.example.data.model.AppLanguage.ENGLISH -> "Total Questions: ${mockTest.questions.size}"
                            com.example.data.model.AppLanguage.HINDI -> "कुल प्रश्न: ${mockTest.questions.size}"
                        }
                    )
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "উত্তর প্রদান করেছেন: $answeredCount"
                            com.example.data.model.AppLanguage.ENGLISH -> "Answered: $answeredCount"
                            com.example.data.model.AppLanguage.HINDI -> "उत्तर दिया: $answeredCount"
                        },
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "অনুত্তরিত বাকি: $unansweredCount"
                            com.example.data.model.AppLanguage.ENGLISH -> "Unanswered: $unansweredCount"
                            com.example.data.model.AppLanguage.HINDI -> "अनुत्तरित: $unansweredCount"
                        },
                        color = CrimsonRed,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "ভুল উত্তরের জন্য প্রতিটিতে -${mockTest.negativeMarkPerWrong} নেগেটিভ মার্কিং প্রযোজ্য হবে।"
                            com.example.data.model.AppLanguage.ENGLISH -> "-${mockTest.negativeMarkPerWrong} negative marking applies for each incorrect answer."
                            com.example.data.model.AppLanguage.HINDI -> "प्रत्येक गलत उत्तर के लिए -${mockTest.negativeMarkPerWrong} नेगेटिव मार्किंग लागू होगी।"
                        },
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
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "হ্যাঁ, ফাইনাল সাবমিট করুন"
                            com.example.data.model.AppLanguage.ENGLISH -> "Yes, Submit Final"
                            com.example.data.model.AppLanguage.HINDI -> "हाँ, फाइनल सबमिट करें"
                        }
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text(
                        when (currentExamLanguage) {
                            com.example.data.model.AppLanguage.BENGALI -> "পরীক্ষায় ফিরে যান"
                            com.example.data.model.AppLanguage.ENGLISH -> "Resume Exam"
                            com.example.data.model.AppLanguage.HINDI -> "परीक्षा पर वापस जाएँ"
                        }
                    )
                }
            }
        )
    }
}
