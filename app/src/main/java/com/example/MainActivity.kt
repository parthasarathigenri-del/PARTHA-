package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppLanguage
import com.example.ui.components.SankalpBottomNavigation
import com.example.ui.components.SankalpTopAppBar
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JobPrepViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.Strings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JobPrepApp()
            }
        }
    }
}

@Composable
fun JobPrepApp(
    viewModel: JobPrepViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val currentStudent by viewModel.currentStudent.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val examRoutines by viewModel.examRoutines.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val testAttempts by viewModel.testAttempts.collectAsState()
    val pdfBooks by viewModel.pdfBooks.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val feePayments by viewModel.feePayments.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    // Active exam states
    val activeMockTest by viewModel.activeMockTest.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val timeRemainingSeconds by viewModel.timeRemainingSeconds.collectAsState()
    val violationStrikes by viewModel.examViolationStrikes.collectAsState()
    val isViolationDialogVisible by viewModel.violationWarningDialogVisible.collectAsState()
    val lastAttemptResult by viewModel.lastAttemptResult.collectAsState()

    // PDF OMR & Fees
    val pdfOmrAnswers by viewModel.pdfOmrAnswers.collectAsState()
    val selectedFeeToPay by viewModel.selectedFeeToPay.collectAsState()
    val latestReceipt by viewModel.latestReceipt.collectAsState()

    val canNavigateBack = currentScreen !is Screen.Home

    BackHandler(enabled = canNavigateBack) {
        viewModel.navigateBack()
    }

    // Role-based Access Control: If student is not approved and in Student Mode, show AccessRestrictedScreen
    if (!currentStudent.isApproved && !isAdminMode) {
        AccessRestrictedScreen(
            student = currentStudent,
            onSwitchToAdmin = { viewModel.toggleAdminMode() }
        )
        return
    }

    val screenTitle = when (currentScreen) {
        is Screen.Home -> Strings.appTitle(appLanguage)
        is Screen.Routine -> "পরীক্ষার রুটিন"
        is Screen.MockTests -> Strings.navMockTests(appLanguage)
        is Screen.ActiveExam -> "মক টেস্ট (লকডাউন মোড)"
        is Screen.ExamResult -> "পরীক্ষার ফলাফল"
        is Screen.ExamReview -> Strings.reviewAnswers(appLanguage)
        is Screen.Library -> Strings.navLibrary(appLanguage)
        is Screen.PdfViewer -> "পিডিএফ ভিউয়ার"
        is Screen.LiveClassList -> Strings.navLiveClass(appLanguage)
        is Screen.LiveRoom -> "লাইভ ভিডিও ক্লাস"
        is Screen.Analytics -> Strings.navAnalytics(appLanguage)
        is Screen.Chat -> "শিক্ষক-শিক্ষার্থী চ্যাট"
        is Screen.Fees -> "অনলাইন ফি পেমেন্ট"
        is Screen.Admin -> "ইনস্টিটিউট অ্যাডমিন প্যানেল"
        is Screen.AiChatbot -> Strings.chatbotAssistant(appLanguage)
    }

    val showBars = currentScreen !is Screen.ActiveExam && currentScreen !is Screen.LiveRoom && currentScreen !is Screen.PdfViewer

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBars) {
                SankalpTopAppBar(
                    title = screenTitle,
                    language = appLanguage,
                    onLanguageChange = { viewModel.setLanguage(it) },
                    isAdminMode = isAdminMode,
                    onAdminToggle = { viewModel.toggleAdminMode() },
                    onOpenChat = { viewModel.navigateTo(Screen.Chat) },
                    onOpenAiChat = { viewModel.navigateTo(Screen.AiChatbot) },
                    onOpenFees = { viewModel.navigateTo(Screen.Fees) },
                    canNavigateBack = canNavigateBack,
                    onBackClick = { viewModel.navigateBack() }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                SankalpBottomNavigation(
                    currentScreen = currentScreen,
                    language = appLanguage,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        val modifier = if (showBars) Modifier.padding(innerPadding) else Modifier

        androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        student = currentStudent,
                        isAdminMode = isAdminMode,
                        routines = examRoutines,
                        notices = notices,
                        attempts = testAttempts,
                        onNavigate = { viewModel.navigateTo(it) },
                        onStartMockTest = { testId -> viewModel.startMockTest(testId) }
                    )
                }

                is Screen.Routine -> {
                    RoutineScreen(
                        routines = examRoutines,
                        language = appLanguage,
                        onStartTest = { testId -> viewModel.startMockTest(testId) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                is Screen.MockTests -> {
                    MockTestsScreen(
                        mockTests = mockTests,
                        attempts = testAttempts,
                        language = appLanguage,
                        onStartTest = { testId -> viewModel.startMockTest(testId) },
                        onViewResult = { attemptId, testId ->
                            viewModel.navigateTo(Screen.ExamResult(attemptId, testId))
                        }
                    )
                }

                is Screen.ActiveExam -> {
                    val test = activeMockTest
                    if (test != null) {
                        ActiveExamScreen(
                            mockTest = test,
                            currentQuestionIndex = currentQuestionIndex,
                            selectedAnswers = selectedAnswers,
                            timeRemainingSeconds = timeRemainingSeconds,
                            violationStrikes = violationStrikes,
                            isViolationDialogVisible = isViolationDialogVisible,
                            language = appLanguage,
                            onSelectAnswer = { qIdx, optIdx -> viewModel.selectAnswer(qIdx, optIdx) },
                            onClearAnswer = { qIdx -> viewModel.clearAnswer(qIdx) },
                            onGoToQuestion = { qIdx -> viewModel.goToQuestion(qIdx) },
                            onReportViolation = { viewModel.reportExamViolation() },
                            onDismissViolationDialog = { viewModel.dismissViolationDialog() },
                            onSubmitExam = { viewModel.submitExam() }
                        )
                    }
                }

                is Screen.ExamResult -> {
                    val attempt = testAttempts.find { it.attemptId == screen.attemptId } ?: lastAttemptResult
                    val test = mockTests.find { it.id == screen.mockTestId } ?: activeMockTest
                    if (attempt != null) {
                        ExamResultScreen(
                            attempt = attempt,
                            mockTest = test,
                            onReviewAnswers = {
                                viewModel.openReviewMode(attempt.attemptId, test?.id ?: "")
                            },
                            onReattempt = {
                                test?.id?.let { viewModel.startMockTest(it) }
                            },
                            onViewLeaderboard = {
                                viewModel.navigateTo(Screen.Analytics)
                            },
                            onBackToHome = {
                                viewModel.navigateTo(Screen.Home)
                            }
                        )
                    }
                }

                is Screen.ExamReview -> {
                    val attempt = testAttempts.find { it.attemptId == screen.attemptId } ?: lastAttemptResult
                    val test = mockTests.find { it.id == screen.mockTestId } ?: activeMockTest
                    if (attempt != null) {
                        ExamReviewScreen(
                            attempt = attempt,
                            mockTest = test,
                            language = appLanguage,
                            onAskTeacher = { doubtQuestion ->
                                viewModel.sendChatMessage("স্যার, এই প্রশ্নটিতে সন্দেহ রয়েছে:\n$doubtQuestion")
                                viewModel.navigateTo(Screen.Chat)
                            },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }

                is Screen.Library -> {
                    LibraryScreen(
                        books = pdfBooks,
                        isAdminMode = isAdminMode,
                        onToggleLock = { pdfId -> viewModel.togglePdfLock(pdfId) },
                        onOpenPdf = { pdfId -> viewModel.navigateTo(Screen.PdfViewer(pdfId)) }
                    )
                }

                is Screen.PdfViewer -> {
                    val pdf = pdfBooks.find { it.id == screen.pdfId }
                    if (pdf != null) {
                        PdfViewerScreen(
                            pdf = pdf,
                            omrAnswers = pdfOmrAnswers,
                            onSelectOmr = { qNum, optIdx -> viewModel.selectPdfOmr(qNum, optIdx) },
                            onSubmitPdfTest = { pdfId -> viewModel.submitPdfTest(pdfId) },
                            onBack = { viewModel.navigateBack() }
                        )
                    }
                }

                is Screen.LiveClassList -> {
                    LiveClassScreen(
                        classes = liveClasses,
                        onJoinLiveRoom = { classId -> viewModel.navigateTo(Screen.LiveRoom(classId)) }
                    )
                }

                is Screen.LiveRoom -> {
                    val liveClass = liveClasses.find { it.id == screen.classId } ?: liveClasses.first()
                    LiveRoomScreen(
                        liveClass = liveClass,
                        onLeaveRoom = { viewModel.navigateBack() }
                    )
                }

                is Screen.Analytics -> {
                    AnalyticsScreen(
                        student = currentStudent,
                        attempts = testAttempts,
                        allStudents = allStudents
                    )
                }

                is Screen.Chat -> {
                    TeacherChatScreen(
                        messages = chatMessages,
                        isAdminMode = isAdminMode,
                        onSendMessage = { text, attName, attType ->
                            viewModel.sendChatMessage(text, attName, attType)
                        },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                is Screen.Fees -> {
                    FeesPaymentScreen(
                        student = currentStudent,
                        fees = feePayments,
                        selectedFeeToPay = selectedFeeToPay,
                        latestReceipt = latestReceipt,
                        onOpenPayDialog = { fee -> viewModel.openPayDialog(fee) },
                        onClosePayDialog = { viewModel.closePayDialog() },
                        onProcessPayment = { method -> viewModel.processPayment(method) },
                        onCloseReceipt = { viewModel.closeReceipt() },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                is Screen.Admin -> {
                    AdminPanelScreen(
                        students = allStudents,
                        pdfBooks = pdfBooks,
                        mockTests = mockTests,
                        onToggleStudentApproval = { stdId, approved ->
                            viewModel.setStudentApproval(stdId, approved)
                        },
                        onTogglePdfLock = { pdfId -> viewModel.togglePdfLock(pdfId) },
                        onCreateMockTest = { title, cat, dur, neg, mark, qs ->
                            viewModel.createMockTest(title, cat, dur, neg, mark, qs)
                        },
                        onAddQuestionToTest = { testId, question ->
                            viewModel.addQuestionToMockTest(testId, question)
                        },
                        onDeleteMockTest = { testId -> viewModel.deleteMockTest(testId) },
                        onToggleMockTestLock = { testId -> viewModel.toggleMockTestLock(testId) },
                        onAddPdfBook = { pdf -> viewModel.addPdfBook(pdf) },
                        onStartMockTest = { testId -> viewModel.startMockTest(testId) },
                        onBack = { viewModel.navigateBack() }
                    )
                }

                is Screen.AiChatbot -> {
                    AiChatbotScreen(
                        language = appLanguage,
                        onBack = { viewModel.navigateBack() }
                    )
                }
            }
        }
    }
}
