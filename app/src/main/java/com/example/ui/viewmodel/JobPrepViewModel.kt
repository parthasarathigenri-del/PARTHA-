package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.JobPrepRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    data object Home : Screen()
    data object Routine : Screen()
    data object MockTests : Screen()
    data class ActiveExam(val mockTestId: String) : Screen()
    data class ExamResult(val attemptId: String, val mockTestId: String) : Screen()
    data class ExamReview(val attemptId: String, val mockTestId: String) : Screen()
    data object Library : Screen()
    data class PdfViewer(val pdfId: String) : Screen()
    data object LiveClassList : Screen()
    data class LiveRoom(val classId: String) : Screen()
    data object Analytics : Screen()
    data object Chat : Screen()
    data object AiChatbot : Screen()
    data object Fees : Screen()
    data object Admin : Screen()
}

class JobPrepViewModel(
    val repository: JobPrepRepository = JobPrepRepository()
) : ViewModel() {

    // Current navigation screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Screen backstack for robust back handling
    private val backStack = mutableListOf<Screen>(Screen.Home)

    // Language stream
    val appLanguage = repository.appLanguage

    // Data streams from repository
    val isAdminMode = repository.isAdminMode
    val currentStudent = repository.currentStudent
    val allStudents = repository.allStudents
    val examRoutines = repository.examRoutines
    val mockTests = repository.mockTests
    val testAttempts = repository.testAttempts
    val pdfBooks = repository.pdfBooks
    val liveClasses = repository.liveClasses
    val notices = repository.notices
    val feePayments = repository.feePayments
    val chatMessages = repository.chatMessages

    fun setLanguage(language: AppLanguage) {
        repository.setLanguage(language)
    }

    // Active Exam State
    private val _activeMockTest = MutableStateFlow<MockTest?>(null)
    val activeMockTest: StateFlow<MockTest?> = _activeMockTest.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(0)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _examViolationStrikes = MutableStateFlow(0)
    val examViolationStrikes: StateFlow<Int> = _examViolationStrikes.asStateFlow()

    private val _violationWarningDialogVisible = MutableStateFlow(false)
    val violationWarningDialogVisible: StateFlow<Boolean> = _violationWarningDialogVisible.asStateFlow()

    private val _lastAttemptResult = MutableStateFlow<TestAttempt?>(null)
    val lastAttemptResult: StateFlow<TestAttempt?> = _lastAttemptResult.asStateFlow()

    // OMR submission state for PDF test
    private val _pdfOmrAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val pdfOmrAnswers: StateFlow<Map<Int, Int>> = _pdfOmrAnswers.asStateFlow()

    // Payment dialog & receipt state
    private val _selectedFeeToPay = MutableStateFlow<FeePayment?>(null)
    val selectedFeeToPay: StateFlow<FeePayment?> = _selectedFeeToPay.asStateFlow()

    private val _latestReceipt = MutableStateFlow<FeePayment?>(null)
    val latestReceipt: StateFlow<FeePayment?> = _latestReceipt.asStateFlow()

    private var examTimerJob: Job? = null
    private var totalExamDurationSeconds = 0
    private var timeSpentSeconds = 0

    init {
        // App active time tracker (adds 5s periodically)
        viewModelScope.launch {
            while (true) {
                delay(15000L)
                repository.recordActiveTime(15)
            }
        }
    }

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value is Screen.ActiveExam) {
            // Cannot simply back out of active exam without confirmation
            return false
        }
        if (backStack.isNotEmpty()) {
            val previous = backStack.removeAt(backStack.size - 1)
            _currentScreen.value = previous
            return true
        }
        return false
    }

    fun toggleAdminMode() {
        repository.toggleAdminMode()
    }

    fun setStudentApproval(studentId: String, approved: Boolean) {
        repository.setStudentApproval(studentId, approved)
    }

    fun togglePdfLock(pdfId: String) {
        repository.togglePdfLock(pdfId)
    }

    // Start a Mock Test (with Lockdown protection)
    fun startMockTest(testId: String) {
        val test = mockTests.value.find { it.id == testId } ?: return
        _activeMockTest.value = test
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _examViolationStrikes.value = 0
        _violationWarningDialogVisible.value = false
        totalExamDurationSeconds = test.durationMinutes * 60
        _timeRemainingSeconds.value = totalExamDurationSeconds
        timeSpentSeconds = 0

        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000L)
                _timeRemainingSeconds.value -= 1
                timeSpentSeconds += 1
            }
            // Auto submit when time expires!
            submitExam(isAutoSubmit = true)
        }

        navigateTo(Screen.ActiveExam(testId))
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _selectedAnswers.update { current ->
            current + (questionIndex to optionIndex)
        }
    }

    fun clearAnswer(questionIndex: Int) {
        _selectedAnswers.update { current ->
            current - questionIndex
        }
    }

    fun goToQuestion(index: Int) {
        val test = _activeMockTest.value ?: return
        if (index in test.questions.indices) {
            _currentQuestionIndex.value = index
        }
    }

    // Protected Lockdown Detection: Called when user minimizes or switches away during exam
    fun reportExamViolation() {
        if (_currentScreen.value !is Screen.ActiveExam) return
        val currentStrikes = _examViolationStrikes.value + 1
        _examViolationStrikes.value = currentStrikes
        _violationWarningDialogVisible.value = true

        if (currentStrikes >= 3) {
            // Force submission due to multiple cheating / tab-out violations!
            submitExam(isForceDisqualified = true)
        }
    }

    fun dismissViolationDialog() {
        _violationWarningDialogVisible.value = false
    }

    // Submit Exam
    fun submitExam(isAutoSubmit: Boolean = false, isForceDisqualified: Boolean = false) {
        examTimerJob?.cancel()
        val test = _activeMockTest.value ?: return
        val attempt = repository.submitTestAttempt(
            mockTest = test,
            answers = _selectedAnswers.value,
            timeSpentSeconds = timeSpentSeconds,
            violationStrikes = _examViolationStrikes.value
        )
        _lastAttemptResult.value = attempt
        _currentScreen.value = Screen.ExamResult(attempt.attemptId, test.id)
    }

    fun openReviewMode(attemptId: String, testId: String) {
        _currentScreen.value = Screen.ExamReview(attemptId, testId)
    }

    // OMR Submission for PDF Test
    fun selectPdfOmr(questionNumber: Int, optionIndex: Int) {
        _pdfOmrAnswers.update { it + (questionNumber to optionIndex) }
    }

    fun submitPdfTest(pdfId: String) {
        val pdf = pdfBooks.value.find { it.id == pdfId } ?: return
        // Generate mock attempt
        val fakeScore = (_pdfOmrAnswers.value.size * 0.8)
        val attempt = TestAttempt(
            attemptId = "pdf_att_${System.currentTimeMillis()}",
            mockTestId = pdf.id,
            mockTestTitle = pdf.title,
            studentId = currentStudent.value.id,
            studentName = currentStudent.value.name,
            attemptNumber = 1,
            userAnswers = _pdfOmrAnswers.value,
            score = fakeScore,
            correctCount = _pdfOmrAnswers.value.size,
            incorrectCount = 0,
            unattemptedCount = 0,
            negativeDeducted = 0.0,
            timeSpentSeconds = 600,
            violationStrikes = 0
        )
        _lastAttemptResult.value = attempt
        _pdfOmrAnswers.value = emptyMap()
        _currentScreen.value = Screen.ExamResult(attempt.attemptId, pdf.id)
    }

    // Payment operations
    fun openPayDialog(fee: FeePayment) {
        _selectedFeeToPay.value = fee
    }

    fun closePayDialog() {
        _selectedFeeToPay.value = null
    }

    fun processPayment(paymentMethod: String) {
        val fee = _selectedFeeToPay.value ?: return
        repository.payFee(fee.id, paymentMethod)
        val updated = feePayments.value.find { it.id == fee.id }
        _latestReceipt.value = updated
        _selectedFeeToPay.value = null
    }

    fun closeReceipt() {
        _latestReceipt.value = null
    }

    // Chat
    fun sendChatMessage(text: String, attachmentName: String? = null, attachmentType: String? = null) {
        if (text.isNotBlank() || attachmentName != null) {
            repository.sendChatMessage(text, attachmentName, attachmentType)
        }
    }

    // Admin Mock Test & Question Uploads
    fun createMockTest(
        title: String,
        category: String,
        durationMinutes: Int,
        negativeMarkPerWrong: Double = 0.25,
        markPerCorrect: Double = 1.0,
        questions: List<MockQuestion> = emptyList()
    ): MockTest {
        return repository.createMockTest(
            title = title,
            category = category,
            durationMinutes = durationMinutes,
            negativeMarkPerWrong = negativeMarkPerWrong,
            markPerCorrect = markPerCorrect,
            questions = questions
        )
    }

    fun addQuestionToMockTest(testId: String, question: MockQuestion) {
        repository.addQuestionToMockTest(testId, question)
    }

    fun deleteMockTest(testId: String) {
        repository.deleteMockTest(testId)
    }

    fun toggleMockTestLock(testId: String) {
        repository.toggleMockTestLock(testId)
    }

    fun addPdfBook(pdfBook: PdfBook) {
        repository.addPdfBook(pdfBook)
    }

    fun addExamRoutine(routine: ExamRoutine) {
        repository.addExamRoutine(routine)
    }
}
