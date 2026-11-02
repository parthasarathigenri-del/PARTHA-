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
    data object PhoneAuth : Screen()
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
    val testAccessRequests = repository.testAccessRequests
    val testFreezeRecords = repository.testFreezeRecords
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

    // Per-question individual timer tracker (tracks seconds spent on each question)
    private val _perQuestionTimeSeconds = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val perQuestionTimeSeconds: StateFlow<Map<Int, Int>> = _perQuestionTimeSeconds.asStateFlow()

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

    private fun startExamTimer(initialSeconds: Int) {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000L)
                _timeRemainingSeconds.value -= 1
                timeSpentSeconds += 1
                // Increment individual timer for the currently viewed question
                val qIdx = _currentQuestionIndex.value
                _perQuestionTimeSeconds.update { map ->
                    val prev = map[qIdx] ?: 0
                    map + (qIdx to prev + 1)
                }
            }
            // Auto submit when time expires!
            submitExam(isAutoSubmit = true)
        }
    }

    // Start a Mock Test (with Lockdown protection)
    fun startMockTest(testId: String) {
        val test = mockTests.value.find { it.id == testId } ?: return
        _activeMockTest.value = test
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _perQuestionTimeSeconds.value = emptyMap()
        _examViolationStrikes.value = 0
        _violationWarningDialogVisible.value = false
        totalExamDurationSeconds = test.durationMinutes * 60
        _timeRemainingSeconds.value = totalExamDurationSeconds
        timeSpentSeconds = 0

        startExamTimer(totalExamDurationSeconds)
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
    // Protected Lockdown Detection: Strike 1 & 2 warn, Strike 3 Freezes test for Admin unfreeze
    fun reportExamViolation() {
        if (_currentScreen.value !is Screen.ActiveExam) return
        val test = _activeMockTest.value ?: return
        val currentStrikes = _examViolationStrikes.value
        val updatedStrikes = repository.reportViolationStrike(
            mockTestId = test.id,
            mockTestTitle = test.getTitle(appLanguage.value),
            currentStrikes = currentStrikes
        )
        _examViolationStrikes.value = updatedStrikes

        if (updatedStrikes >= 3) {
            // Exam frozen! Pause timer and hide warning dialog, frozen screen will take over
            examTimerJob?.cancel()
            _violationWarningDialogVisible.value = false
        } else {
            _violationWarningDialogVisible.value = true
        }
    }

    fun dismissViolationDialog() {
        _violationWarningDialogVisible.value = false
    }

    // Submit Exam with overall time and individual question times
    fun submitExam(isAutoSubmit: Boolean = false, isForceDisqualified: Boolean = false) {
        examTimerJob?.cancel()
        val test = _activeMockTest.value ?: return
        val attempt = repository.submitTestAttempt(
            mockTest = test,
            answers = _selectedAnswers.value,
            timeSpentSeconds = timeSpentSeconds,
            questionTimeSeconds = _perQuestionTimeSeconds.value,
            violationStrikes = _examViolationStrikes.value
        )
        _lastAttemptResult.value = attempt
        _currentScreen.value = Screen.ExamResult(attempt.attemptId, test.id)
    }

    // Role-Based Login: Admin (8902720907 / 258789) vs Student Phone Login
    fun isAdminPhoneNumber(phone: String): Boolean = repository.isAdminPhoneNumber(phone)

    fun loginAsAdmin(phone: String, password: String): Boolean = repository.loginAsAdmin(phone, password)

    fun registerWithPhone(name: String, phone: String, targetExam: String) {
        repository.registerStudentWithPhone(name, phone, targetExam)
    }

    fun approveStudent(studentId: String) {
        repository.approveStudent(studentId)
    }

    fun rejectStudent(studentId: String) {
        repository.rejectStudent(studentId)
    }

    // First-Time Test Access Permission Request
    fun requestMockTestAccess(mockTestId: String, mockTestTitle: String) {
        repository.requestMockTestAccess(mockTestId, mockTestTitle)
    }

    fun approveTestAccessRequest(requestId: String) {
        repository.approveTestAccessRequest(requestId)
    }

    fun canStudentAccessTest(mockTestId: String): Boolean {
        return repository.canStudentAccessTest(currentStudent.value, mockTestId)
    }

    // 3-Strike Unfreeze
    fun unfreezeStudentTest(freezeRecordId: String) {
        repository.unfreezeStudentTest(freezeRecordId)
        _examViolationStrikes.value = 0
        // Resume timer if in active exam
        if (_currentScreen.value is Screen.ActiveExam && _timeRemainingSeconds.value > 0) {
            startExamTimer(_timeRemainingSeconds.value)
        }
    }

    // Admin Profile & Registration
    fun registerOrUpdateAdmin(name: String, phone: String) {
        repository.registerOrUpdateAdmin(name, phone)
    }

    // Individual Student Permissions (Mock Test & PDF)
    fun toggleMockTestPermission(studentId: String) {
        repository.toggleMockTestPermission(studentId)
    }

    fun togglePdfPermission(studentId: String) {
        repository.togglePdfPermission(studentId)
    }

    fun grantAllMockTestPermission(grant: Boolean) {
        repository.grantAllMockTestPermission(grant)
    }

    fun grantAllPdfPermission(grant: Boolean) {
        repository.grantAllPdfPermission(grant)
    }

    fun toggleAdminRole(studentId: String) {
        repository.toggleAdminRole(studentId)
    }

    fun switchToStudent(studentId: String) {
        repository.switchToStudent(studentId)
    }

    fun addQuestionsBatchToMockTest(testId: String, questions: List<MockQuestion>) {
        repository.addQuestionsBatchToMockTest(testId, questions)
    }

    fun addNotice(title: String, content: String, category: String = "ইনস্টিটিউট নোটিশ", isUrgent: Boolean = false) {
        val notice = InstituteNotice(
            id = "not_${System.currentTimeMillis()}",
            title = title,
            dateString = "আজ",
            category = category,
            content = content,
            isUrgent = isUrgent
        )
        repository.addNotice(notice)
    }

    fun getLeaderboard(): List<LeaderboardEntry> = repository.getLeaderboard()

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
        isLockedByAdmin: Boolean = true,
        questions: List<MockQuestion> = emptyList()
    ): MockTest {
        return repository.createMockTest(
            title = title,
            category = category,
            durationMinutes = durationMinutes,
            negativeMarkPerWrong = negativeMarkPerWrong,
            markPerCorrect = markPerCorrect,
            isLockedByAdmin = isLockedByAdmin,
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
