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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    students: List<Student>,
    pdfBooks: List<PdfBook>,
    mockTests: List<MockTest>,
    onToggleStudentApproval: (String, Boolean) -> Unit,
    onTogglePdfLock: (String) -> Unit,
    onCreateMockTest: (title: String, category: String, durationMinutes: Int, negativeMark: Double, markPerCorrect: Double, questions: List<MockQuestion>) -> MockTest,
    onAddQuestionToTest: (testId: String, question: MockQuestion) -> Unit,
    onDeleteMockTest: (String) -> Unit,
    onToggleMockTestLock: (String) -> Unit,
    onAddPdfBook: (PdfBook) -> Unit,
    onStartMockTest: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("পরীক্ষা ও প্রশ্ন আপলোড", "শিক্ষার্থী এক্সেস", "পিডিএফ লক ম্যানেজার", "উপস্থিতি ও সক্রিয়তা")

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
                                                // Pre-fill sample question
                                                questionText = "ভারতের সংবিধানের কত নম্বর ধারায় সুপ্রিম কোর্টে রিট জারির ক্ষমতা (Right to Constitutional Remedies) বর্ণিত রয়েছে?"
                                                optionA = "ধারা ১৯"
                                                optionB = "ধারা ২১"
                                                optionC = "ধারা ৩২"
                                                optionD = "ধারা ২২৬"
                                                selectedCorrectOption = 2
                                                explanationText = "সংবিধানের ৩২ নম্বর অনুচ্ছেদকে ড. বি আর আম্বেদকর 'সংবিধানের হৃদয় ও আত্মা' (Heart and Soul of the Constitution) বলে অভিহিত করেছিলেন।"
                                                subjectTag = "ভারতের সংবিধান ও রাজনীতি"
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoFixHigh,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("নমুনা লোড", fontSize = 11.sp)
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
                        // TAB 1: Student Permission & Access Whitelist
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
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                                    Text(
                                        text = "শুধুমাত্র যে সকল ছাত্রছাত্রীকে আপনি অনুমোদন দেবেন তারাই অ্যাপের কনটেন্ট ও পরীক্ষা এক্সেস করতে পারবে। পারমিশন বন্ধ করলে তাৎক্ষণিক অ্যাক্সেস ব্লক হবে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        items(students) { student ->
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
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = student.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (student.isApproved) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (student.isApproved) "অনুমোদিত" else "ব্লকড",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (student.isApproved) EmeraldGreen else CrimsonRed,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        Text(
                                            text = "রোল: ${student.rollNo} • লক্ষ্য: ${student.targetExam}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "ফোন: ${student.phone}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            TextButton(
                                                onClick = { selectedStudentGraph = student },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Timeline, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("পারফরম্যান্স গ্রাফ", fontSize = 11.sp)
                                            }
                                        }
                                    }

                                    Switch(
                                        checked = student.isApproved,
                                        onCheckedChange = { approved ->
                                            onToggleStudentApproval(student.id, approved)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = EmeraldGreen,
                                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier.testTag("admin_approval_switch_${student.id}")
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: PDF Lock / Unlock Manager
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

                    3 -> {
                        // TAB 3: Attendance & Active Hours Report
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
