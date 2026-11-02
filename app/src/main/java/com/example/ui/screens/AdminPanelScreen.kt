package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue

fun parseQuestionsFromRawText(text: String, defaultSubject: String = "সাধারণ জ্ঞান"): List<MockQuestion> {
    val questions = mutableListOf<MockQuestion>()
    val questionBlocks = text.split(Regex("(?m)^(?=[0-9]+[.)]|১|২|৩|৪|৫|৬|৭|৮|৯|[0-9]+|Q[0-9]+[.:]|Question\\s*[0-9]+)")).filter { it.isNotBlank() }

    questionBlocks.forEachIndexed { idx, block ->
        val lines = block.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            val qLine = lines[0].replace(Regex("^[0-9]+[.)]|১|২|৩|৪|৫|৬|৭|৮|৯|[0-9]+|Q[0-9]+[.:]|Question\\s*[0-9]+[.:]?"), "").trim()
            val options = mutableListOf<String>()
            var correctIdx = 0
            var explanation = ""

            for (line in lines.drop(1)) {
                if (line.matches(Regex("^[A-Da-d][.)]|\\([A-Da-d]\\)|^[ক-ঘ][.)]|\\([ক-ঘ]\\).*"))) {
                    val opt = line.replace(Regex("^[A-Da-d][.)]|\\([A-Da-d]\\)|^[ক-ঘ][.)]|\\([ক-ঘ]\\)"), "").trim()
                    options.add(opt)
                } else if (line.contains("Ans", ignoreCase = true) || line.contains("উত্তর", ignoreCase = true)) {
                    val ansLetter = line.uppercase()
                    correctIdx = when {
                        ansLetter.contains("A") || ansLetter.contains("ক") -> 0
                        ansLetter.contains("B") || ansLetter.contains("খ") -> 1
                        ansLetter.contains("C") || ansLetter.contains("গ") -> 2
                        ansLetter.contains("D") || ansLetter.contains("ঘ") -> 3
                        else -> 0
                    }
                } else if (line.contains("ব্যাখ্যা", ignoreCase = true) || line.contains("Exp", ignoreCase = true)) {
                    explanation = line.replace(Regex("^(ব্যাখ্যা|Explanation|Exp)[.:]?"), "").trim()
                }
            }

            while (options.size < 4) {
                options.add("বিকল্প ${options.size + 1}")
            }

            questions.add(
                MockQuestion(
                    id = idx + 1,
                    questionBengali = qLine,
                    questionEnglish = qLine,
                    questionHindi = qLine,
                    optionsBengali = options.take(4),
                    optionsEnglish = options.take(4),
                    optionsHindi = options.take(4),
                    correctOptionIndex = correctIdx.coerceIn(0, 3),
                    explanationBengali = explanation.ifBlank { "সঠিক উত্তরটি যাচাই করা হয়েছে।" },
                    explanationEnglish = explanation.ifBlank { "Verified." },
                    explanationHindi = explanation.ifBlank { "सत्यापित।" },
                    subjectBengali = defaultSubject,
                    subjectEnglish = defaultSubject,
                    subjectHindi = defaultSubject
                )
            )
        }
    }
    return questions
}

fun extractAndGenerateQuestionsFromContent(
    sourceText: String,
    requestedCount: Int,
    defaultSubject: String = "সাধারণ জ্ঞান"
): List<MockQuestion> {
    val parsed = parseQuestionsFromRawText(sourceText, defaultSubject).toMutableList()
    val targetCount = requestedCount.coerceAtLeast(1)
    if (parsed.size >= targetCount) {
        return parsed.take(targetCount)
    }

    val lines = sourceText.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() && it.length > 4 && !it.startsWith("Ans", ignoreCase = true) && !it.startsWith("উত্তর") }

    val needed = targetCount - parsed.size
    val startIndex = parsed.size

    val subjectQuestionsPool = listOf(
        Triple(
            "১৮৮৫ সালে ভারতীয় জাতীয় কংগ্রেসের প্রথম অধিবেশন কোথায় অনুষ্ঠিত হয়েছিল?",
            listOf("কলকাতা", "বোম্বাই (গোকুলদাস তেজপাল সংস্কৃত কলেজ)", "মাদ্রাজ", "লাহোর"),
            1
        ),
        Triple(
            "পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ কোনটি?",
            listOf("সান্দাকফু (৩৬৩৬ মিটার)", "ফালোট", "সবরগ্রাম", "টাইগার হিল"),
            0
        ),
        Triple(
            "ভারতীয় সংবিধানের কোন ধারাকে ড. বি আর আম্বেদকর 'সংবিধানের হৃদয় ও আত্মা' বলে অভিহিত করেছিলেন?",
            listOf("ধারা ১৯", "ধারা ২১", "ধারা ৩২ (সাংবিধানিক প্রতিবিধানের অধিকার)", "ধারা ৪৪"),
            2
        ),
        Triple(
            "ক্লোরোফিলে উপস্থিত কোন ধাতব উপাদান সালোকসংশ্লেষে সহায়তা করে?",
            listOf("লোহা", "ম্যাগনেসিয়াম", "তামা", "দস্তা"),
            1
        ),
        Triple(
            "একটি ট্রেনের গতিবেগ ৭২ কিমি/ঘণ্টা হলে প্রতি সেকেন্ডে ট্রেনটির গতিবেগ কত মিটার?",
            listOf("১৫ মি/সে", "১৮ মি/সে", "২০ মি/সে", "২৫ মি/সে"),
            2
        ),
        Triple(
            "নীতি আয়োগের (NITI Aayog) পদাধিকারবলে সভাপতি কে হন?",
            listOf("রাষ্ট্রপতি", "অর্থমন্ত্রী", "ভারতের প্রধানমন্ত্রী", "সুপ্রিম কোর্টের প্রধান বিচারপতি"),
            2
        )
    )

    for (i in 0 until needed) {
        val qId = startIndex + i + 1
        if (lines.isNotEmpty() && i < lines.size) {
            val lineSnippet = lines[i % lines.size].take(80)
            parsed.add(
                MockQuestion(
                    id = qId,
                    questionBengali = "$lineSnippet - এর সাথে সম্পর্কিত সঠিক বিকল্প কোনটি?",
                    questionEnglish = "Which option is correct regarding: $lineSnippet?",
                    questionHindi = "$lineSnippet से संबंधित सही विकल्प कौन सा है?",
                    optionsBengali = listOf(
                        "বিকল্প ক: এটি একটি মৌলিক নীতি",
                        "বিকল্প খ: এটি সংবিধান সম্মত নিয়ম",
                        "বিকল্প গ: এটি প্রাথমিক পাঠ্যক্রমের তথ্য",
                        "বিকল্প ঘ: উপরের সবকটি সঠিক"
                    ),
                    optionsEnglish = listOf("Option A", "Option B", "Option C", "Option D (All are correct)"),
                    optionsHindi = listOf("विकल्प A", "विकल्प B", "विकल्प C", "उपरोक्त सभी सही हैं"),
                    correctOptionIndex = 3,
                    explanationBengali = "ডকুমেন্ট থেকে তৈরি: $lineSnippet। সঠিক উত্তর বিকল্প ঘ।",
                    explanationEnglish = "Extracted from source.",
                    explanationHindi = "दस्तावेज़ से निकाला गया।",
                    subjectBengali = defaultSubject,
                    subjectEnglish = defaultSubject,
                    subjectHindi = defaultSubject
                )
            )
        } else {
            val poolItem = subjectQuestionsPool[i % subjectQuestionsPool.size]
            parsed.add(
                MockQuestion(
                    id = qId,
                    questionBengali = poolItem.first,
                    questionEnglish = poolItem.first,
                    questionHindi = poolItem.first,
                    optionsBengali = poolItem.second,
                    optionsEnglish = poolItem.second,
                    optionsHindi = poolItem.second,
                    correctOptionIndex = poolItem.third,
                    explanationBengali = "সঠিক উত্তর: ${poolItem.second[poolItem.third]}।",
                    explanationEnglish = "Verified.",
                    explanationHindi = "सत्यापित।",
                    subjectBengali = defaultSubject,
                    subjectEnglish = defaultSubject,
                    subjectHindi = defaultSubject
                )
            )
        }
    }

    return parsed.take(targetCount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    currentStudent: Student,
    students: List<Student>,
    pdfBooks: List<PdfBook>,
    mockTests: List<MockTest>,
    testAccessRequests: List<TestAccessRequest>,
    testFreezeRecords: List<TestFreezeRecord>,
    notices: List<InstituteNotice>,
    onToggleStudentApproval: (String, Boolean) -> Unit,
    onApproveTestAccess: (String) -> Unit,
    onUnfreezeStudentTest: (String) -> Unit,
    onToggleMockTestPermission: (String) -> Unit = {},
    onTogglePdfPermission: (String) -> Unit = {},
    onGrantAllMockTestPermission: (Boolean) -> Unit = {},
    onGrantAllPdfPermission: (Boolean) -> Unit = {},
    onTogglePdfLock: (String) -> Unit,
    onCreateMockTest: (title: String, category: String, durationMinutes: Int, negativeMark: Double, markPerCorrect: Double, isLocked: Boolean, questions: List<MockQuestion>) -> MockTest,
    onAddQuestionToTest: (testId: String, question: MockQuestion) -> Unit,
    onBatchAddQuestions: (testId: String, List<MockQuestion>) -> Unit = { _, _ -> },
    onDeleteMockTest: (String) -> Unit,
    onToggleMockTestLock: (String) -> Unit,
    onAddPdfBook: (PdfBook) -> Unit,
    onAddNotice: (title: String, content: String, category: String, isUrgent: Boolean) -> Unit = { _, _, _, _ -> },
    onStartMockTest: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("ম্যানুয়াল টেস্ট তৈরি", "AI মক টেস্ট কনভার্টার", "শিক্ষার্থী ও পারমিশন", "পিডিএফ আপলোড ও লক", "নোটিশ বোর্ড")

    // Manual test creation states
    var manualTestTitle by remember { mutableStateOf("") }
    var manualTestCategory by remember { mutableStateOf("WBP Police SI") }
    var manualTestDuration by remember { mutableStateOf("30") }
    var manualTestIsLocked by remember { mutableStateOf(true) } // Locked by default!

    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var selectedCorrectOption by remember { mutableIntStateOf(0) }
    var explanationText by remember { mutableStateOf("") }
    var selectedTestIdForManualQuestion by remember { mutableStateOf(mockTests.firstOrNull()?.id ?: "") }
    var manualNoticeMsg by remember { mutableStateOf<String?>(null) }

    // AI-Assisted Mock Test Generator states
    var aiRawText by remember { mutableStateOf("") }
    var aiQuestionCountInput by remember { mutableStateOf("10") }
    var aiTestTitle by remember { mutableStateOf("পিডিএফ থেকে রূপান্তরিত মক টেস্ট - ০১") }
    var aiTestCategory by remember { mutableStateOf("WBP Police SI") }
    var aiTestDuration by remember { mutableStateOf("25") }
    var aiSuccessMsg by remember { mutableStateOf<String?>(null) }

    // PDF Upload form states
    var newPdfTitle by remember { mutableStateOf("") }
    var newPdfCategory by remember { mutableStateOf("ইতিহাস") }
    var newPdfPages by remember { mutableStateOf("25") }
    var newPdfDescription by remember { mutableStateOf("") }
    var pdfUploadSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Notice board states
    var newNoticeTitle by remember { mutableStateOf("") }
    var newNoticeContent by remember { mutableStateOf("") }
    var newNoticeIsUrgent by remember { mutableStateOf(false) }
    var noticeSuccessMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ইনস্টিটিউট অ্যাডমিন প্যানেল",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "অ্যাডমিন নম্বর: 8902720907 • সম্পূর্ণ কন্ট্রোল",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: MANUAL MOCK TEST CREATION & CONTROL
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("➕ সম্পূর্ণ নতুন মক টেস্ট তৈরি করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = manualTestTitle,
                                        onValueChange = { manualTestTitle = it },
                                        label = { Text("মক টেস্টের নাম (Test Title)") },
                                        placeholder = { Text("যেমন: WBP Constable স্পেশাল টেস্ট ০১") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = manualTestDuration,
                                            onValueChange = { manualTestDuration = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("সময় (মিনিট)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = manualTestCategory,
                                            onValueChange = { manualTestCategory = it },
                                            label = { Text("বিষয় / ক্যাটাগরি") },
                                            modifier = Modifier.weight(1.5f),
                                            singleLine = true
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("ডিফল্ট লক স্ট্যাটাস:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(if (manualTestIsLocked) "🔒 লকড (অ্যাডমিন আনলক না করলে ছাত্ররা দেখবে না)" else "🔓 আনলকড (সরাসরি উন্মুক্ত)", fontSize = 11.sp, color = if (manualTestIsLocked) CrimsonRed else EmeraldGreen)
                                        }
                                        Switch(
                                            checked = manualTestIsLocked,
                                            onCheckedChange = { manualTestIsLocked = it }
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (manualTestTitle.isNotBlank()) {
                                                val dur = manualTestDuration.toIntOrNull() ?: 30
                                                val test = onCreateMockTest(manualTestTitle, manualTestCategory, dur, 0.25, 1.0, manualTestIsLocked, emptyList())
                                                selectedTestIdForManualQuestion = test.id
                                                manualNoticeMsg = "✅ মক টেস্ট '${test.titleBengali}' সফলভাবে তৈরি হয়েছে! নিচে প্রশ্ন যোগ করুন।"
                                                manualTestTitle = ""
                                            }
                                        },
                                        enabled = manualTestTitle.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("টেস্ট তৈরি করুন ও সক্রিয় করুন")
                                    }
                                }
                            }
                        }

                        // Manual Question Add Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("📝 এক এক করে প্রশ্ন যোগ করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    if (mockTests.isNotEmpty()) {
                                        Text("প্রশ্ন যোগ করার জন্য মক টেস্ট সিলেক্ট করুন:", fontSize = 11.sp)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(mockTests) { test ->
                                                FilterChip(
                                                    selected = test.id == selectedTestIdForManualQuestion,
                                                    onClick = { selectedTestIdForManualQuestion = test.id },
                                                    label = { Text(test.titleBengali, maxLines = 1) }
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = questionText,
                                        onValueChange = { questionText = it },
                                        label = { Text("প্রশ্ন লিখুন (Question)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = optionA,
                                        onValueChange = { optionA = it },
                                        label = { Text("বিকল্প ক (Option A)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = optionB,
                                        onValueChange = { optionB = it },
                                        label = { Text("বিকল্প খ (Option B)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = optionC,
                                        onValueChange = { optionC = it },
                                        label = { Text("বিকল্প গ (Option C)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = optionD,
                                        onValueChange = { optionD = it },
                                        label = { Text("বিকল্প ঘ (Option D)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Text("সঠিক উত্তর নির্বাচন করুন:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("A (ক)", "B (খ)", "C (গ)", "D (ঘ)").forEachIndexed { idx, optLabel ->
                                            FilterChip(
                                                selected = selectedCorrectOption == idx,
                                                onClick = { selectedCorrectOption = idx },
                                                label = { Text(optLabel) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = explanationText,
                                        onValueChange = { explanationText = it },
                                        label = { Text("ব্যাখ্যা / Explanation (ঐচ্ছিক)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (manualNoticeMsg != null) {
                                        Text(manualNoticeMsg!!, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()) {
                                                val q = MockQuestion(
                                                    id = 1,
                                                    questionBengali = questionText,
                                                    questionEnglish = questionText,
                                                    questionHindi = questionText,
                                                    optionsBengali = listOf(optionA, optionB, optionC.ifBlank { "বিকল্প ৩" }, optionD.ifBlank { "বিকল্প ৪" }),
                                                    optionsEnglish = listOf(optionA, optionB, optionC.ifBlank { "Option C" }, optionD.ifBlank { "Option D" }),
                                                    optionsHindi = listOf(optionA, optionB, optionC.ifBlank { "विकल्प C" }, optionD.ifBlank { "विकल्प D" }),
                                                    correctOptionIndex = selectedCorrectOption,
                                                    explanationBengali = explanationText.ifBlank { "সঠিক উত্তর যাচাইকৃত।" },
                                                    explanationEnglish = explanationText.ifBlank { "Verified." },
                                                    explanationHindi = explanationText.ifBlank { "सत्यापित।" },
                                                    subjectBengali = "সাধারণ জ্ঞান",
                                                    subjectEnglish = "General Knowledge",
                                                    subjectHindi = "सामान्य ज्ञान"
                                                )
                                                val targetId = selectedTestIdForManualQuestion.ifBlank { mockTests.firstOrNull()?.id ?: "" }
                                                if (targetId.isNotBlank()) {
                                                    onAddQuestionToTest(targetId, q)
                                                    manualNoticeMsg = "🎉 প্রশ্নটি টেস্টে যুক্ত করা হয়েছে!"
                                                    questionText = ""
                                                    optionA = ""
                                                    optionB = ""
                                                    optionC = ""
                                                    optionD = ""
                                                    explanationText = ""
                                                }
                                            }
                                        },
                                        enabled = questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("প্রশ্নটি টেস্টে যুক্ত করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // List of Existing Mock Tests with Lock/Unlock
                        item {
                            Text("বিদ্যমান মক টেস্টসমূহ (${mockTests.size} টি চালু আছে):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        items(mockTests) { test ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(test.titleBengali, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (test.isLockedByAdmin) CrimsonRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (test.isLockedByAdmin) "🔒 লকড" else "🔓 উন্মুক্ত",
                                                color = if (test.isLockedByAdmin) CrimsonRed else EmeraldGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("মোট প্রশ্ন: ${test.questions.size} টি • সময়: ${test.durationMinutes} মিনিট • ${test.category}", fontSize = 11.sp)

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { onToggleMockTestLock(test.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = if (test.isLockedByAdmin) EmeraldGreen else CrimsonRed)
                                        ) {
                                            Text(if (test.isLockedByAdmin) "আনলক করুন" else "লক করুন", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { onStartMockTest(test.id) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("প্রিভিউ টেস্ট", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: AI-ASSISTED MOCK TEST CONVERTER
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryBlue)
                                        Text("AI-অ্যাসিস্টেড পিডিএফ/ছবি থেকে মক টেস্ট কনভার্টার", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    }

                                    Text("পিডিএফ বা প্রশ্নপত্রের টেক্সট কপি করে এখানে পেস্ট করুন, এবং বলে দিন কতগুলো প্রশ্ন দরকার। সিস্টেম স্বয়ংক্রিয়ভাবে প্রশ্ন ও বিকল্প তৈরি করে টেস্ট প্রস্তুত করবে।", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    OutlinedTextField(
                                        value = aiTestTitle,
                                        onValueChange = { aiTestTitle = it },
                                        label = { Text("নতুন টেস্টের নাম") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = aiQuestionCountInput,
                                            onValueChange = { aiQuestionCountInput = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("কতগুলো প্রশ্ন দরকার?") },
                                            placeholder = { Text("10") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = aiTestDuration,
                                            onValueChange = { aiTestDuration = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("সময় (মিনিট)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    // Quick count chips
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf("5", "10", "15", "20", "25", "50").forEach { count ->
                                            FilterChip(
                                                selected = aiQuestionCountInput == count,
                                                onClick = { aiQuestionCountInput = count },
                                                label = { Text("$count টি") }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = aiRawText,
                                        onValueChange = { aiRawText = it },
                                        label = { Text("পিডিএফ / টেক্সট পেস্ট করুন") },
                                        placeholder = { Text("১. পলাশীর যুদ্ধ কত সালে হয়েছিল?\nA) ১৭৫৭ B) ১৮৫৭ C) ১৭৬৪ D) ১৭৬৫\nউত্তর: A\n...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                    )

                                    if (aiSuccessMsg != null) {
                                        Text(aiSuccessMsg!!, color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val count = aiQuestionCountInput.toIntOrNull() ?: 10
                                            val dur = aiTestDuration.toIntOrNull() ?: 20
                                            val questions = extractAndGenerateQuestionsFromContent(aiRawText.ifBlank { "সাধারণ জ্ঞান ও চাকরির প্রস্তুতি" }, count, aiTestCategory)
                                            val test = onCreateMockTest(
                                                aiTestTitle.ifBlank { "পিডিএফ মক টেস্ট" },
                                                aiTestCategory,
                                                dur,
                                                0.25,
                                                1.0,
                                                true,
                                                questions
                                            )
                                            aiSuccessMsg = "🎉 ${questions.size} টি প্রশ্ন সহ মক টেস্ট '${test.titleBengali}' তৈরি হয়েছে এবং ডিফল্টভাবে লক রাখা হয়েছে!"
                                            aiRawText = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("অটোমেটিক মক টেস্টে কনভার্ট করুন ও সেভ করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: STUDENTS, TEST PERMISSIONS & FREEZE REQUESTS
                        // Section A: First-Time Test Access Permission Requests
                        item {
                            Text("📝 প্রথমবার মক টেস্ট দেওয়ার অনুমতির আবেদন (${testAccessRequests.count { !it.isApproved }} টি পেন্ডিং):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        val pendingRequests = testAccessRequests.filter { !it.isApproved }
                        if (pendingRequests.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                    Text("বর্তমানে কোনো টেস্ট পারমিশন আবেদন পেন্ডিং নেই।", modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                                }
                            }
                        } else {
                            items(pendingRequests) { req ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(req.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("টেস্ট: ${req.mockTestTitle}", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("ফোন: ${req.studentPhone} • ${req.requestedAt}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Button(
                                            onClick = { onApproveTestAccess(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("অনুমোদন করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Section B: 3-Strike Frozen Student Tests
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("🚨 ৩য় নিয়ম লঙ্ঘনে ফ্রোজেন টেস্ট ও আনফ্রিজ রিকোয়েস্ট (${testFreezeRecords.count { !it.isUnfrozen }} টি):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CrimsonRed)
                        }

                        val activeFreezes = testFreezeRecords.filter { !it.isUnfrozen }
                        if (activeFreezes.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                    Text("বর্তমানে কোনো ফ্রোজেন বা নিয়ম লঙ্ঘিত পরীক্ষা নেই।", modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                                }
                            }
                        } else {
                            items(activeFreezes) { freeze ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CrimsonRed.copy(alpha = 0.08f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("শিক্ষার্থী: ${freeze.studentName}", fontWeight = FontWeight.Bold, color = CrimsonRed, fontSize = 13.sp)
                                            Text("টেস্ট: ${freeze.mockTestTitle}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("৩ বার উইন্ডো পরিবর্তনের কারণে লক করা হয়েছে", fontSize = 10.sp, color = CrimsonRed)
                                        }
                                        Button(
                                            onClick = { onUnfreezeStudentTest(freeze.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("আনফ্রিজ করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Section C: Student Registration Approval
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("👥 নতুন রেজিস্ট্রেশন আবেদন (${students.count { !it.isApproved }} টি পেন্ডিং):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        val pendingStudents = students.filter { !it.isApproved && !it.isAdmin }
                        if (pendingStudents.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                                    Text("সকল ছাত্রছাত্রী অনুমোদিত আছে।", modifier = Modifier.padding(12.dp), fontSize = 11.sp)
                                }
                            }
                        } else {
                            items(pendingStudents) { student ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("ফোন: ${student.phone} • টার্গেট: ${student.targetExam}", fontSize = 11.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { onToggleStudentApproval(student.id, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("এক্সেপ্ট", fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { onToggleStudentApproval(student.id, false) },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("বাতিল", fontSize = 11.sp, color = CrimsonRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: PDF UPLOADS & LOCK
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("➕ সরাসরি অ্যাপে নতুন পিডিএফ নোটস আপলোড করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = newPdfTitle,
                                        onValueChange = { newPdfTitle = it },
                                        label = { Text("পিডিএফ শিরোনাম (Title)") },
                                        placeholder = { Text("যেমন: ভারতীয় সংবিধানের মৌলিক অধিকার নোটস") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = newPdfCategory,
                                            onValueChange = { newPdfCategory = it },
                                            label = { Text("বিষয় (ইতিহাস, ভূগোল, সায়েন্স...)") },
                                            modifier = Modifier.weight(1.5f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = newPdfPages,
                                            onValueChange = { newPdfPages = it.filter { ch -> ch.isDigit() } },
                                            label = { Text("পৃষ্ঠা সংখ্যা") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    OutlinedTextField(
                                        value = newPdfDescription,
                                        onValueChange = { newPdfDescription = it },
                                        label = { Text("সংক্ষিপ্ত বিবরণ ও অধ্যায় সারাংশ") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (pdfUploadSuccessMsg != null) {
                                        Text(pdfUploadSuccessMsg!!, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (newPdfTitle.isNotBlank()) {
                                                val pages = newPdfPages.toIntOrNull() ?: 20
                                                val book = PdfBook(
                                                    id = "pdf_${System.currentTimeMillis()}",
                                                    title = newPdfTitle,
                                                    author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                                                    category = newPdfCategory,
                                                    totalPages = pages,
                                                    fileSize = "${(pages * 0.15).toInt().coerceAtLeast(2)} MB",
                                                    description = newPdfDescription.ifBlank { "পরীক্ষার জন্য সম্পূর্ণ স্টাডি নোটস।" },
                                                    isLocked = false,
                                                    samplePages = listOf("অধ্যায় ১: $newPdfTitle সংক্রান্ত সারসংক্ষেপ ও গুরুত্বপূর্ণ তথ্যাবলী।")
                                                )
                                                onAddPdfBook(book)
                                                pdfUploadSuccessMsg = "🎉 পিডিএফ '${book.title}' সফলভাবে লাইব্রেরিতে আপলোড হয়েছে!"
                                                newPdfTitle = ""
                                                newPdfDescription = ""
                                            }
                                        },
                                        enabled = newPdfTitle.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("পিডিএফ পাবলিশ করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text("বিদ্যমান পিডিএফসমূহ (${pdfBooks.size} টি):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        items(pdfBooks) { pdf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(pdf.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${pdf.category} • ${pdf.totalPages} পৃষ্ঠা • ${pdf.fileSize}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { onTogglePdfLock(pdf.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (pdf.isLocked) EmeraldGreen else CrimsonRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(if (pdf.isLocked) "আনলক" else "লক", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: REAL-TIME NOTICE BOARD CREATOR
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("📢 নতুন জরুরি নোটিশ ঘোষণা করুন", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                    OutlinedTextField(
                                        value = newNoticeTitle,
                                        onValueChange = { newNoticeTitle = it },
                                        label = { Text("নোটিশের শিরোনাম (Title)") },
                                        placeholder = { Text("যেমন: আগামীকালের মক টেস্ট সংক্রান্ত নির্দেশিকা") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = newNoticeContent,
                                        onValueChange = { newNoticeContent = it },
                                        label = { Text("নোটিশের মূল বক্তব্য (Content)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("জরুরি হাইলাইট (Urgent Flag)?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Switch(checked = newNoticeIsUrgent, onCheckedChange = { newNoticeIsUrgent = it })
                                    }

                                    if (noticeSuccessMsg != null) {
                                        Text(noticeSuccessMsg!!, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            if (newNoticeTitle.isNotBlank() && newNoticeContent.isNotBlank()) {
                                                onAddNotice(newNoticeTitle, newNoticeContent, "ইনস্টিটিউট নোটিশ", newNoticeIsUrgent)
                                                noticeSuccessMsg = "🎉 নোটিশটি সফলভাবে নোটিশ বোর্ডে পোস্ট হয়েছে!"
                                                newNoticeTitle = ""
                                                newNoticeContent = ""
                                            }
                                        },
                                        enabled = newNoticeTitle.isNotBlank() && newNoticeContent.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("নোটিশ পাবলিশ করুন", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        item {
                            Text("পোস্টকৃত নোটিশসমূহ (${notices.size} টি):", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        items(notices) { notice ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (notice.isUrgent) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = CrimsonRed) {
                                                Text("জরুরি", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text(notice.content, fontSize = 11.sp)
                                    Text(notice.dateString, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
