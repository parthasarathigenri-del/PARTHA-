package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.MockQuestion
import com.example.data.model.MockTest
import com.example.data.model.TestAttempt
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy800
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamReviewScreen(
    attempt: TestAttempt,
    mockTest: MockTest?,
    language: AppLanguage,
    onAskTeacher: (String) -> Unit,
    onBack: () -> Unit
) {
    val questions = mockTest?.questions ?: emptyList()

    // State for which question has the "Explain" chatbot helper opened
    var activeExplainQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var followUpDoubtText by remember { mutableStateOf("") }
    val followUpAnswers = remember { mutableStateMapOf<Int, String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (language) {
                                AppLanguage.BENGALI -> "উত্তর ও সমাধান পর্যালোচনা (Preview)"
                                AppLanguage.ENGLISH -> "Review Answers & Solutions (Preview)"
                                AppLanguage.HINDI -> "उत्तर व व्याख्या समीक्षा (Preview)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "প্রচেষ্টা নং #${attempt.attemptNumber} • মোট প্রাপ্ত নম্বর: ${attempt.score}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("review_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("review_screen_lazy_column"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Background permission banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "রিভিউ মোড বিশেষ অনুমতি সক্রিয়",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "শুধুমাত্র রিভিউ মোডের জন্য ব্যাকগ্রাউন্ড উইন্ডো ব্যবহারের অনুমতি কার্যকর রয়েছে যাতে আপনি বই ও রেফারেন্স দেখে ভুল সংশোধন ও যাচাই করতে পারেন। তলায় 'Explain' বাটনে ক্লিক করে সাথে সাথে চ্যাটবোর্ড থেকে ব্যাখ্যা পান।",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Questions list
            itemsIndexed(questions) { qIndex, q ->
                val userChoice = attempt.userAnswers[qIndex]
                val isCorrect = userChoice == q.correctOptionIndex
                val isUnattempted = userChoice == null

                val isExplainOpen = activeExplainQuestionIndex == qIndex
                val questionText = q.getQuestionText(language)
                val optionsList = q.getOptions(language)
                val explanationText = q.getExplanation(language)
                val subjectText = q.getSubject(language)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_question_card_$qIndex"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "প্রশ্ন ${qIndex + 1} • $subjectText",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            val statusBg = when {
                                isCorrect -> EmeraldGreen
                                isUnattempted -> AccentAmber
                                else -> CrimsonRed
                            }
                            val statusText = when {
                                isCorrect -> "✓ সঠিক উত্তর (+১.০)"
                                isUnattempted -> "উত্তর দেননি (০.০)"
                                else -> "✗ ভুল উত্তর (-০.২৫)"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = statusText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusBg
                                )
                            }
                        }

                        Text(
                            text = questionText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        // Options breakdown
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            optionsList.forEachIndexed { optIndex, optText ->
                                val isRightOption = optIndex == q.correctOptionIndex
                                val isUserPicked = optIndex == userChoice

                                val optBorder = when {
                                    isRightOption -> BorderStroke(1.5.dp, EmeraldGreen)
                                    isUserPicked && !isCorrect -> BorderStroke(1.5.dp, CrimsonRed)
                                    else -> null
                                }
                                val optBg = when {
                                    isRightOption -> EmeraldGreen.copy(alpha = 0.08f)
                                    isUserPicked && !isCorrect -> CrimsonRed.copy(alpha = 0.08f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = optBg,
                                    border = optBorder,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = when (optIndex) {
                                                0 -> "A."
                                                1 -> "B."
                                                2 -> "C."
                                                else -> "D."
                                            },
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )

                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (isRightOption) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "সঠিক",
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else if (isUserPicked) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "ভুল",
                                                tint = CrimsonRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Explanation card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = when (language) {
                                            AppLanguage.BENGALI -> "স্ট্যান্ডার্ড সমাধান ও সূত্র:"
                                            AppLanguage.ENGLISH -> "Standard Solution & Key Concept:"
                                            AppLanguage.HINDI -> "मानक समाधान व मुख्य बिंदु:"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = explanationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // THE REQUIRED SHORTCUT: Dedicated Explain Button right below the question!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeExplainQuestionIndex = if (isExplainOpen) null else qIndex
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isExplainOpen) Navy900 else PrimaryBlue
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("explain_shortcut_btn_$qIndex")
                            ) {
                                Icon(
                                    imageVector = if (isExplainOpen) Icons.Default.KeyboardArrowUp else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isExplainOpen) {
                                        when (language) {
                                            AppLanguage.BENGALI -> "ব্যাখ্যা বন্ধ করুন"
                                            AppLanguage.ENGLISH -> "Close Explanation"
                                            AppLanguage.HINDI -> "व्याख्या बंद करें"
                                        }
                                    } else {
                                        when (language) {
                                            AppLanguage.BENGALI -> "💡 এক্সপ্লেন (Explain Doubt)"
                                            AppLanguage.ENGLISH -> "💡 Explain in Depth"
                                            AppLanguage.HINDI -> "💡 समझाएं (Explain)"
                                        }
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    onAskTeacher("প্রশ্ন ${qIndex + 1}: $questionText")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("চ্যাটে জিজ্ঞাসা", fontSize = 11.sp)
                            }
                        }

                        // Instant Interactive Chatbot Explain Drawer
                        AnimatedVisibility(visible = isExplainOpen) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Navy800),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("explain_drawer_$qIndex")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = AccentAmber.copy(alpha = 0.2f),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.SmartToy,
                                                    contentDescription = null,
                                                    tint = AccentAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = when (language) {
                                                AppLanguage.BENGALI -> "চ্যাটবোর্ড ইন্টারেক্টিভ ডাউট ব্যাখ্যা:"
                                                AppLanguage.ENGLISH -> "AI Chat Assistant Detailed Breakdown:"
                                                AppLanguage.HINDI -> "चैटबॉट इंटरैक्टिव विस्तृत व्याख्या:"
                                            },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = generateDetailedExplanation(q, language, userChoice),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        lineHeight = 20.sp
                                    )

                                    if (followUpAnswers.containsKey(qIndex)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = PrimaryBlue.copy(alpha = 0.25f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = followUpAnswers[qIndex] ?: "",
                                                modifier = Modifier.padding(8.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    // Quick prompt chips
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        AssistChip(
                                            onClick = {
                                                followUpAnswers[qIndex] = when (language) {
                                                    AppLanguage.BENGALI -> "💡 শর্ট ট্রিক: এই ধরনের প্রশ্ন মনে রাখতে সময়কাল ও মূল ব্যক্তিত্বের তালিকা তৈরি করে রিভিশন করুন।"
                                                    AppLanguage.ENGLISH -> "💡 Pro-Tip: Memorize key dates and associate with landmark decisions for quick recall in exams."
                                                    AppLanguage.HINDI -> "💡 परीक्षा युक्ति: मुख्य तारीखों और प्रमुख निर्णयों को एक चार्ट में लिखकर दोहराएं।"
                                                }
                                            },
                                            label = { Text("শর্ট ট্রিক", fontSize = 10.sp) }
                                        )

                                        AssistChip(
                                            onClick = {
                                                followUpAnswers[qIndex] = when (language) {
                                                    AppLanguage.BENGALI -> "📚 সম্পর্কিত টপিক: গত ৫ বছরের WBCS ও SSC পরীক্ষায় এই অধ্যায় থেকে ৮টি প্রশ্ন এসেছে।"
                                                    AppLanguage.ENGLISH -> "📚 Exam Trend: In the past 5 years, 8 questions were asked from this exact topic."
                                                    AppLanguage.HINDI -> "📚 परीक्षा ट्रेंड: पिछले 5 वर्षों में इस अध्याय से 8 प्रश्न पूछे गए हैं।"
                                                }
                                            },
                                            label = { Text("পরীক্ষার ট্রেন্ড", fontSize = 10.sp) }
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
}

private fun generateDetailedExplanation(
    question: MockQuestion,
    lang: AppLanguage,
    userChoice: Int?
): String {
    val correctOptLetter = when (question.correctOptionIndex) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        else -> "D"
    }

    return when (lang) {
        AppLanguage.BENGALI -> {
            "🔍 গভীর বিশ্লেষণ:\n" +
                    "• সঠিক বিকল্প ($correctOptLetter): ${question.optionsBengali[question.correctOptionIndex]}।\n" +
                    "• মূল যুক্তি: ${question.explanationBengali}\n" +
                    (if (userChoice != null && userChoice != question.correctOptionIndex) "• আপনার ভুল কারণ: নির্বাচিত বিকল্পটি প্রাসঙ্গিক সময়ের সাথে অসঙ্গত বা বিভ্রান্তিকর বিকল্প।" else "• চমৎকার! আপনি প্রশ্নটির সঠিক কারণ অনুধাবন করে উত্তর দিয়েছেন।")
        }
        AppLanguage.ENGLISH -> {
            "🔍 Detailed Analysis:\n" +
                    "• Correct Option ($correctOptLetter): ${question.optionsEnglish[question.correctOptionIndex]}.\n" +
                    "• Core Logic: ${question.explanationEnglish}\n" +
                    (if (userChoice != null && userChoice != question.correctOptionIndex) "• Why your choice was incorrect: That option refers to a different context or year." else "• Great job! You correctly identified the primary concept.")
        }
        AppLanguage.HINDI -> {
            "🔍 विस्तृत विश्लेषण:\n" +
                    "• सही विकल्प ($correctOptLetter): ${question.optionsHindi[question.correctOptionIndex]}।\n" +
                    "• मुख्य कारण: ${question.explanationHindi}\n" +
                    (if (userChoice != null && userChoice != question.correctOptionIndex) "• गलत उत्तर का कारण: आपका चुना हुआ विकल्प इस समय या संदर्भ से मेल नहीं खाता।" else "• बहुत बढ़िया! आपने सही सिद्धांत को समझा और उत्तर दिया।")
        }
    }
}
