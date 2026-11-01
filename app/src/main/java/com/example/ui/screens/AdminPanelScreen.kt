package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.MockQuestion
import com.example.data.model.MockTest
import com.example.data.model.PdfBook
import com.example.data.model.Student
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy800
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    students: List<Student>,
    pdfBooks: List<PdfBook>,
    mockTests: List<MockTest>,
    onToggleStudentApproval: (String, Boolean) -> Unit,
    onToggleAdminRole: (String) -> Unit = {},
    onTogglePdfLock: (String) -> Unit,
    onCreateMockTest: (title: String, category: String, durationMinutes: Int, negativeMark: Double, markPerCorrect: Double, questions: List<MockQuestion>) -> MockTest,
    onAddQuestionToTest: (testId: String, question: MockQuestion) -> Unit,
    onBatchAddQuestions: (testId: String, List<MockQuestion>) -> Unit = { _, _ -> },
    onDeleteMockTest: (String) -> Unit,
    onToggleMockTestLock: (String) -> Unit,
    onAddPdfBook: (PdfBook) -> Unit,
    onStartMockTest: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("প্রশ্ন ও মক টেস্ট", "পিডিএফ থেকে প্রশ্ন আপলোড", "রেজিস্ট্রেশন ও WhatsApp অ্যাডমিন", "পিডিএফ লক ম্যানেজার", "সক্রিয়তা")

    var selectedStudentGraph by remember { mutableStateOf<Student?>(null) }
    var showCreateTestDialog by remember { mutableStateOf(false) }
    var showUploadPdfDialog by remember { mutableStateOf(false) }

    // Test selection for adding questions
    var selectedTestIdForQuestion by remember {
        mutableStateOf(mockTests.firstOrNull()?.id ?: "")
    }

    // Keep selectedTestId valid
    LaunchedEffect(mockTests) {
        if (mockTests.isNotEmpty() && (selectedTestIdForQuestion.isEmpty() || mockTests.none { it.id == selectedTestIdForQuestion })) {
            selectedTestIdForQuestion = mockTests.first().id
        }
    }

    // Question form state
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var selectedCorrectOption by remember { mutableIntStateOf(0) }
    var explanationText by remember { mutableStateOf("") }
    var subjectTag by remember { mutableStateOf("ভারতের ইতিহাস") }
    var questionAddedNotice by remember { mutableStateOf<String?>(null) }

    // Expandable questions accordion in test list
    var expandedTestId by remember { mutableStateOf<String?>(null) }

    // Direct PDF/Text Question Upload State
    var pdfRawText by remember { mutableStateOf("") }
    var parsedPdfQuestions by remember { mutableStateOf<List<MockQuestion>>(emptyList()) }
    var pdfBatchSuccessMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ইনস্টিটিউট অ্যাডমিন ও ডিরেক্টর প্যানেল",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "COMPETITIVE PATHSHALA • লাইভ কনটেন্ট ও প্রশ্ন আপলোড সিস্টেম",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
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
                .testTag("admin_panel_screen")
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
                        },
                        modifier = Modifier.testTag("admin_tab_$index")
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
                        // TAB 0: Direct Mock Test & Question Upload Engine
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Navy900)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                                    imageVector = Icons.Default.CloudUpload,
                                                    contentDescription = null,
                                                    tint = AccentAmber,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "সরাসরি টেস্ট ও প্রশ্ন আপলোড কন্ট্রোল",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "কোনো কোডিং বা অ্যাপ আপডেট ছাড়াই আপনার ফোন থেকেই সরাসরি মক টেস্ট তৈরি ও প্রশ্ন যোগ করুন।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }

                                    // Quick Action Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showCreateTestDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_create_test_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("নতুন মক টেস্ট", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = { showUploadPdfDialog = true },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_upload_pdf_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("পিডিএফ পেপার", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Success banner when a question is added
                        if (questionAddedNotice != null) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = questionAddedNotice!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION 1: Add Question Form
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_add_question_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PostAdd,
                                            contentDescription = null,
                                            tint = PrimaryBlue
                                        )
                                        Text(
                                            text = "মক টেস্টে সরাসরি নতুন প্রশ্ন যোগ করুন",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Target Mock Test Selector
                                    Text(
                                        text = "কোন টেস্টে প্রশ্নটি যোগ করতে চান সিলেক্ট করুন:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(mockTests) { test ->
                                            val isSelected = test.id == selectedTestIdForQuestion
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedTestIdForQuestion = test.id },
                                                label = {
                                                    Text(
                                                        text = "${test.titleBengali} (${test.questions.size}টি প্রশ্ন)",
                                                        fontSize = 11.sp,
                                                        maxLines = 1
                                                    )
                                                },
                                                leadingIcon = if (isSelected) {
                                                    {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                } else null
                                            )
                                        }
                                    }

                                    // Subject Category Selector
                                    val subjects = listOf("ভারতের ইতিহাস", "পশ্চিমবঙ্গের ভূগোল", "ভারতের সংবিধান ও রাজনীতি", "পাটিগণিত ও জিআই", "সাধারণ বিজ্ঞান ও প্রযুক্তি", "কারেন্ট অ্যাফেয়ার্স")
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(subjects) { subj ->
                                            AssistChip(
                                                onClick = { subjectTag = subj },
                                                label = { Text(subj, fontSize = 10.sp) },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = if (subjectTag == subj) PrimaryBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            )
                                        }
                                    }

                                    // Question Input
                                    OutlinedTextField(
                                        value = questionText,
                                        onValueChange = { questionText = it },
                                        label = { Text("প্রশ্ন লিখুন (Question Text)") },
                                        placeholder = { Text("যেমন: বঙ্গভঙ্গ রদ কত সালে কার্যকর হয়েছিল?") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("admin_question_input"),
                                        shape = RoundedCornerShape(10.dp),
                                        minLines = 2
                                    )

                                    // 4 Options
                                    Text(
                                        text = "৪টি উত্তরের অপশন লিখুন:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    OutlinedTextField(
                                        value = optionA,
                                        onValueChange = { optionA = it },
                                        label = { Text("অপশন A") },
                                        placeholder = { Text("যেমন: ১৯০৫") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = optionB,
                                        onValueChange = { optionB = it },
                                        label = { Text("অপশন B") },
                                        placeholder = { Text("যেমন: ১৯১১") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = optionC,
                                        onValueChange = { optionC = it },
                                        label = { Text("অপশন C") },
                                        placeholder = { Text("যেমন: ১৯৪৭") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = optionD,
                                        onValueChange = { optionD = it },
                                        label = { Text("অপশন D") },
                                        placeholder = { Text("যেমন: ১৯১৯") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    // Correct Option Selector
                                    Text(
                                        text = "সঠিক উত্তর নির্বাচন করুন (Correct Option):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val optionLabels = listOf("অপশন A", "অপশন B", "অপশন C", "অপশন D")
                                        optionLabels.forEachIndexed { idx, label ->
                                            val isSelected = selectedCorrectOption == idx
                                            Button(
                                                onClick = { selectedCorrectOption = idx },
                                                colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = EmeraldGreen) else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f),
                                                contentPadding = PaddingValues(vertical = 4.dp)
                                            ) {
                                                Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }

                                    // Explanation Input
                                    OutlinedTextField(
                                        value = explanationText,
                                        onValueChange = { explanationText = it },
                                        label = { Text("উত্তরের ব্যাখ্যা ও শর্টকাট টিপস (Explanation)") },
                                        placeholder = { Text("যেমন: ১৯১১ সালে লর্ড হার্ডিঞ্জ বঙ্গভঙ্গ রদ করেন এবং ভারতের রাজধানী কলকাতা থেকে দিল্লিতে স্থানান্তরিত হয়।") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        minLines = 2
                                    )

                                    // Save Button & Quick Auto-fill
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                // Reset question fields
                                                questionText = ""
                                                optionA = ""
                                                optionB = ""
                                                optionC = ""
                                                optionD = ""
                                                explanationText = ""
                                                selectedCorrectOption = 0
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("ফিল্ড মুছুন", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank() && selectedTestIdForQuestion.isNotEmpty()) {
                                                    val newQ = MockQuestion(
                                                        id = 0,
                                                        questionBengali = questionText.trim(),
                                                        questionEnglish = questionText.trim(),
                                                        questionHindi = questionText.trim(),
                                                        optionsBengali = listOf(optionA.trim(), optionB.trim(), if (optionC.isNotBlank()) optionC.trim() else "অন্যান্য", if (optionD.isNotBlank()) optionD.trim() else "কোনোটিই নয়"),
                                                        optionsEnglish = listOf(optionA.trim(), optionB.trim(), if (optionC.isNotBlank()) optionC.trim() else "Other", if (optionD.isNotBlank()) optionD.trim() else "None"),
                                                        optionsHindi = listOf(optionA.trim(), optionB.trim(), if (optionC.isNotBlank()) optionC.trim() else "अन्य", if (optionD.isNotBlank()) optionD.trim() else "कोई नहीं"),
                                                        correctOptionIndex = selectedCorrectOption,
                                                        explanationBengali = if (explanationText.isNotBlank()) explanationText.trim() else "সঠিক বিকল্পটি নির্বাচন করা হয়েছে।",
                                                        explanationEnglish = if (explanationText.isNotBlank()) explanationText.trim() else "Correct option verified.",
                                                        explanationHindi = if (explanationText.isNotBlank()) explanationText.trim() else "सही विकल्प चुना गया।",
                                                        subjectBengali = subjectTag,
                                                        subjectEnglish = subjectTag,
                                                        subjectHindi = subjectTag
                                                    )
                                                    onAddQuestionToTest(selectedTestIdForQuestion, newQ)
                                                    questionAddedNotice = "প্রশ্নটি সফলভাবে মক টেস্টে যুক্ত হয়েছে! শিক্ষার্থীরা অবিলম্বে পরীক্ষা দিতে পারবে।"
                                                    // Reset fields
                                                    questionText = ""
                                                    optionA = ""
                                                    optionB = ""
                                                    optionC = ""
                                                    optionD = ""
                                                    explanationText = ""
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1.5f)
                                                .testTag("admin_save_question_btn"),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("প্রশ্নটি টেস্টে যুক্ত করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION 2: Existing Mock Tests List & Control
                        item {
                            Text(
                                text = "বিদ্যমান মক টেস্টসমূহ (${mockTests.size}টি চালু আছে)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(mockTests) { test ->
                            val isExpanded = expandedTestId == test.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_test_card_${test.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PrimaryBlue.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = test.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryBlue,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (test.isLockedByAdmin) CrimsonRed.copy(alpha = 0.15f) else EmeraldGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (test.isLockedByAdmin) "🔒 লকড" else "🔓 উন্মুক্ত",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (test.isLockedByAdmin) CrimsonRed else EmeraldGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = test.titleBengali,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "মোট প্রশ্ন: ${test.questions.size} টি • সময়: ${test.durationMinutes} মিনিট",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "নেগেটিভ: -${test.negativeMarkPerWrong}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CrimsonRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Action bar for this test
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quick set as active target for adding question
                                        OutlinedButton(
                                            onClick = { selectedTestIdForQuestion = test.id },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("প্রশ্ন যোগ", fontSize = 11.sp)
                                        }

                                        // Lock/Unlock toggle
                                        Button(
                                            onClick = { onToggleMockTestLock(test.id) },
                                            colors = if (test.isLockedByAdmin) ButtonDefaults.buttonColors(containerColor = EmeraldGreen) else ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(if (test.isLockedByAdmin) "আনলক" else "লক", fontSize = 11.sp)
                                        }

                                        // Preview Test
                                        IconButton(
                                            onClick = { onStartMockTest(test.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "প্রিভিউ টেস্ট",
                                                tint = PrimaryBlue
                                            )
                                        }

                                        // Expand questions
                                        IconButton(
                                            onClick = {
                                                expandedTestId = if (isExpanded) null else test.id
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "প্রশ্ন তালিকা"
                                            )
                                        }

                                        // Delete Test
                                        IconButton(
                                            onClick = { onDeleteMockTest(test.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "টেস্ট মুছুন",
                                                tint = CrimsonRed
                                            )
                                        }
                                    }

                                    // Expandable Question List
                                    AnimatedVisibility(visible = isExpanded) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "এই টেস্টের অন্তর্ভুক্ত প্রশ্নসমূহ (${test.questions.size}টি):",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )

                                            test.questions.forEachIndexed { qIdx, q ->
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(
                                                            text = "${qIdx + 1}. ${q.questionBengali}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "সঠিক উত্তর: অপশন ${('A'.code + q.correctOptionIndex).toChar()} (${q.optionsBengali.getOrElse(q.correctOptionIndex) { "" }})",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = EmeraldGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Direct Question Upload from PDF / Text
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_question_importer_card"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = PrimaryBlue.copy(alpha = 0.15f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.PictureAsPdf,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "পিডিএফ / টেক্সট থেকে সরাসরি প্রশ্ন আপলোড",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "পিডিএফ বা ওয়ার্ড ফাইলের প্রশ্নগুলো কপি করে এখানে পেস্ট করুন, সাথে সাথে প্রশ্ন তৈরি হয়ে যাবে",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Divider()

                                    // Mock Test target selection
                                    Text(
                                        text = "প্রশ্ন যুক্ত করার জন্য মক টেস্ট নির্বাচন করুন:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(mockTests) { test ->
                                            val isSelected = test.id == selectedTestIdForQuestion
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedTestIdForQuestion = test.id },
                                                label = { Text(test.titleBengali, maxLines = 1) },
                                                leadingIcon = if (isSelected) {
                                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                                } else null
                                            )
                                        }
                                    }

                                    // Raw Text Area
                                    OutlinedTextField(
                                        value = pdfRawText,
                                        onValueChange = { pdfRawText = it },
                                        label = { Text("পিডিএফ প্রশ্ন পেস্ট করুন (Paste Questions from PDF)") },
                                        placeholder = { Text("১. প্রশ্ন...\nA) বিকল্প ক\nB) বিকল্প খ\nC) বিকল্প গ\nD) বিকল্প ঘ\nউত্তর: B") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .testTag("pdf_raw_text_input"),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                parsedPdfQuestions = parseQuestionsFromRawText(pdfRawText)
                                                pdfBatchSuccessMsg = if (parsedPdfQuestions.isNotEmpty()) "${parsedPdfQuestions.size} টি প্রশ্ন সফলভাবে চিহ্নিত করা হয়েছে!" else "কোনো প্রশ্ন চিহ্নিত করা যায়নি। ফরম্যাট চেক করুন।"
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("parse_questions_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("প্রশ্ন এক্সট্র্যাক্ট করুন (Parse)")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                pdfRawText = ""
                                                parsedPdfQuestions = emptyList()
                                                pdfBatchSuccessMsg = null
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("টেক্সট মুছুন")
                                        }
                                    }

                                    if (pdfBatchSuccessMsg != null) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldGreen.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = pdfBatchSuccessMsg!!,
                                                color = EmeraldGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }

                                    if (parsedPdfQuestions.isNotEmpty()) {
                                        Button(
                                            onClick = {
                                                onBatchAddQuestions(selectedTestIdForQuestion, parsedPdfQuestions)
                                                pdfBatchSuccessMsg = "🎉 ${parsedPdfQuestions.size} টি প্রশ্ন সফলভাবে মক টেস্টে যুক্ত করা হয়েছে!"
                                                parsedPdfQuestions = emptyList()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("upload_all_parsed_questions_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                                        ) {
                                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("এক ক্লিকে সমস্ত প্রশ্ন মক টেস্টে যুক্ত করুন", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (parsedPdfQuestions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "এক্সট্র্যাক্টকৃত প্রশ্নের প্রিভিউ (${parsedPdfQuestions.size} টি):",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            itemsIndexed(parsedPdfQuestions) { idx, q ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${idx + 1}. ${q.questionBengali}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        q.optionsBengali.forEachIndexed { optIdx, opt ->
                                            val isCorrect = optIdx == q.correctOptionIndex
                                            Text(
                                                text = "${('A'.code + optIdx).toChar()}) $opt ${if (isCorrect) "✓ (সঠিক উত্তর)" else ""}",
                                                fontSize = 11.sp,
                                                color = if (isCorrect) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        if (q.explanationBengali.isNotBlank()) {
                                            Text("ব্যাখ্যা: ${q.explanationBengali}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: WhatsApp Registration Requests & Individual Make Admin Toggle
                        val pendingStudents = students.filter { !it.isApproved }
                        val approvedStudents = students.filter { it.isApproved }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF075E54).copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = Color(0xFF075E54))
                                    Column {
                                        Text(
                                            text = "WhatsApp স্টাইল সদস্য ও অ্যাডমিন নিয়ন্ত্রণ",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF075E54)
                                        )
                                        Text(
                                            text = "নতুন ফোন রেজিস্ট্রেশন রিকোয়েস্ট এক্সেপ্ট করুন এবং গ্রুপ অ্যাডমিনের মতো যেকোনো শিক্ষার্থীকে ইন্ডিভিজুয়ালি অ্যাডমিন করুন।",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Section 1: Pending Registration Requests
                        item {
                            Text(
                                text = "অনুমোদনের অপেক্ষায় আবেদন (${pendingStudents.size} টি)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (pendingStudents.isNotEmpty()) CrimsonRed else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (pendingStudents.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "বর্তমানে কোনো নতুন পেন্ডিং রেজিস্ট্রেশন নেই। সকল আবেদন অনুমোদিত হয়েছে।",
                                        modifier = Modifier.padding(14.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(pendingStudents) { student ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pending_request_${student.id}"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("ফোন: ${student.phone} • ${student.targetExam}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text("আবেদনের সময়: ${student.registrationDate}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = AccentAmber.copy(alpha = 0.15f)
                                            ) {
                                                Text("অপেক্ষারত", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { onToggleStudentApproval(student.id, true) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("approve_btn_${student.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("এক্সেপ্ট (Approve)", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { onToggleStudentApproval(student.id, false) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("reject_btn_${student.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = CrimsonRed)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("বাতিল (Reject)", fontSize = 11.sp, color = CrimsonRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Approved Students & WhatsApp Make Admin
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "অনুমোদিত শিক্ষার্থী ও হোয়াটসঅ্যাপ অ্যাডমিন রোল (${approvedStudents.size} জন)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(approvedStudents) { student ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("student_admin_card_${student.id}"),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(student.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            if (student.isAdmin) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = PrimaryBlue
                                                ) {
                                                    Text(
                                                        text = "🛡️ গ্রুপ অ্যাডমিন",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "রোল: ${student.rollNo} • ফোন: ${student.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "টার্গেট: ${student.targetExam}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // WhatsApp Group Admin style: Make Admin / Dismiss Admin
                                    Button(
                                        onClick = { onToggleAdminRole(student.id) },
                                        colors = if (student.isAdmin) ButtonDefaults.buttonColors(containerColor = Color.DarkGray) else ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("toggle_admin_${student.id}")
                                    ) {
                                        Text(
                                            text = if (student.isAdmin) "অ্যাডমিন সরান" else "মেক অ্যাডমিন",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: PDF Lock / Unlock Manager
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LockClock, contentDescription = null, tint = AccentAmber)
                                    Text(
                                        text = "শিডিউলড পরীক্ষার পিডিএফ নির্দিষ্ট পরীক্ষার আগে শিক্ষার্থীদের জন্য লক রাখতে পারেন। পরীক্ষা শুরু করার মুহূর্তে বা নির্ধারিত সময়ে এখান থেকে এক ক্লিকে আনলক করুন।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        items(pdfBooks) { pdf ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pdf.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "বিভাগ: ${pdf.category} • ${pdf.totalPages} পৃষ্ঠা",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (pdf.isLocked) "অবস্থা: 🔒 লকড (শিক্ষার্থীরা অ্যাক্সেস করতে পারছে না)" else "অবস্থা: 🔓 উন্মুক্ত (শিক্ষার্থীরা পরীক্ষা দিচ্ছে)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (pdf.isLocked) CrimsonRed else EmeraldGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { onTogglePdfLock(pdf.id) },
                                        colors = if (pdf.isLocked) ButtonDefaults.buttonColors(containerColor = EmeraldGreen) else ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("pdf_lock_toggle_${pdf.id}")
                                    ) {
                                        Text(if (pdf.isLocked) "আনলক করুন" else "লক করুন", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: Attendance & Active Hours Report
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "📊 ইনস্টিটিউট সক্রিয়তা রিপোর্ট: শিক্ষার্থীদের পরীক্ষার প্রচেষ্টা সংখ্যা এবং সক্রিয় সময়সূচী",
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        items(students) { std ->
                            val hours = std.activeTimeSeconds / 3600
                            val mins = (std.activeTimeSeconds % 3600) / 60

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = std.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "রোল: ${std.rollNo}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "পরীক্ষায় অংশগ্রহণ: ${std.scoresHistory.size} বার",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "${hours} ঘণ্টা ${mins} মিনিট সক্রিয়",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
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
    }

    // Modal: Create New Mock Test Dialog
    if (showCreateTestDialog) {
        var newTestTitle by remember { mutableStateOf("") }
        var newTestCategory by remember { mutableStateOf("WBCS 2026") }
        var newTestDuration by remember { mutableStateOf("45") }
        var newTestNegative by remember { mutableStateOf("0.25") }

        AlertDialog(
            onDismissRequest = { showCreateTestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = PrimaryBlue)
                    Text("নতুন মক টেস্ট তৈরি করুন", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTestTitle,
                        onValueChange = { newTestTitle = it },
                        label = { Text("মক টেস্টের নাম (Title)") },
                        placeholder = { Text("যেমন: WBCS প্রিলিমস স্পেশাল মক ৩") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("বিভাগ নির্বাচন করুন:", style = MaterialTheme.typography.labelSmall)
                    val catList = listOf("WBCS 2026", "PSC Clerkship", "WBP / KP SI", "Railway NTPC", "SSC CGL")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(catList) { cat ->
                            FilterChip(
                                selected = newTestCategory == cat,
                                onClick = { newTestCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTestDuration,
                            onValueChange = { newTestDuration = it },
                            label = { Text("সময়সীমা (মিনিট)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newTestNegative,
                            onValueChange = { newTestNegative = it },
                            label = { Text("নেগেটিভ মার্কিং") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTestTitle.isNotBlank()) {
                            val duration = newTestDuration.toIntOrNull() ?: 45
                            val neg = newTestNegative.toDoubleOrNull() ?: 0.25
                            val created = onCreateMockTest(
                                newTestTitle.trim(),
                                newTestCategory,
                                duration,
                                neg,
                                1.0,
                                emptyList()
                            )
                            selectedTestIdForQuestion = created.id
                            showCreateTestDialog = false
                            questionAddedNotice = "মক টেস্ট '${newTestTitle}' সফলভাবে তৈরি হয়েছে! নিচে প্রশ্ন যোগ করুন।"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("তৈরি করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTestDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Modal: Upload PDF Dialog
    if (showUploadPdfDialog) {
        var pdfTitle by remember { mutableStateOf("") }
        var pdfCategory by remember { mutableStateOf("শিডিউলড পিডিএফ টেস্ট") }
        var pdfPages by remember { mutableStateOf("10") }
        var pdfUnlockTime by remember { mutableStateOf("আগামী রবিবার সকাল ১০:০০ টা") }

        AlertDialog(
            onDismissRequest = { showUploadPdfDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = EmeraldGreen)
                    Text("নতুন পিডিএফ টেস্ট আপলোড", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pdfTitle,
                        onValueChange = { pdfTitle = it },
                        label = { Text("পিডিএফ ফাইলের নাম") },
                        placeholder = { Text("যেমন: WBCS ২০২১-২০২৫ বিগত ৫ বছরের সমাধান") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = pdfUnlockTime,
                        onValueChange = { pdfUnlockTime = it },
                        label = { Text("আনলক সময়সূচী") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = pdfPages,
                        onValueChange = { pdfPages = it },
                        label = { Text("মোট পৃষ্ঠা সংখ্যা") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pdfTitle.isNotBlank()) {
                            val newPdf = PdfBook(
                                id = "pdf_${System.currentTimeMillis()}",
                                title = pdfTitle.trim(),
                                author = "COMPETITIVE PATHSHALA রিসার্চ প্যানেল",
                                category = pdfCategory,
                                totalPages = pdfPages.toIntOrNull() ?: 10,
                                fileSize = "3.2 MB",
                                isScheduledExamPdf = true,
                                isLocked = true,
                                scheduledUnlockTime = pdfUnlockTime,
                                description = "অ্যাডমিন প্যানেল থেকে আপলোডকৃত অফিশিয়াল টেস্ট পেপার। নির্দিষ্ট সময়ে শিক্ষার্থীদের জন্য উন্মুক্ত করা হবে।",
                                samplePages = listOf(
                                    "COMPETITIVE PATHSHALA - অফিসিয়াল টেস্ট পেপার (পৃষ্ঠা ১)\nবিভাগ ক: জেনারেল স্টাডিজ ও পাটিগণিত\n১. ভারতীয় জাতীয়তাবাদের জনক কাকে বলা হয়?\n২. পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ কোনটি?",
                                    "COMPETITIVE PATHSHALA - অফিসিয়াল টেস্ট পেপার (পৃষ্ঠা ২)\nবিভাগ খ: ভারতীয় সংবিধান ও অর্থনীতি\n৩. নীতি আয়োগ (NITI Aayog) কবে গঠিত হয়?"
                                )
                            )
                            onAddPdfBook(newPdf)
                            showUploadPdfDialog = false
                            questionAddedNotice = "পিডিএফ টেস্ট '${pdfTitle}' সফলভাবে লাইব্রেরিতে আপলোড করা হয়েছে!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("আপলোড সম্পন্ন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadPdfDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (selectedStudentGraph != null) {
        val std = selectedStudentGraph!!
        AlertDialog(
            onDismissRequest = { selectedStudentGraph = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = null, tint = PrimaryBlue)
                    Text("${std.name}-এর পারফরম্যান্স ট্র্যাজেক্টরি")
                }
            },
            text = {
                com.example.ui.components.StudentPerformanceGraphCard(student = std)
            },
            confirmButton = {
                Button(onClick = { selectedStudentGraph = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
