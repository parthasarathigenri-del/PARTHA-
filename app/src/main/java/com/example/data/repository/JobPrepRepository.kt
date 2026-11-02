package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JobPrepRepository {

    companion object {
        const val ADMIN_PHONE = "8902720907"
        const val ADMIN_PASSWORD = "258789"
    }

    // Language state (Bengali, English, Hindi)
    private val _appLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Current user state (Can toggle between Student and Admin for demonstration)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _currentStudent = MutableStateFlow(
        Student(
            id = "guest_student",
            name = "নতুন শিক্ষার্থী",
            rollNo = "CP-101",
            targetExam = "চাকরির প্রস্তুতি",
            phone = "",
            isApproved = false,
            isAdmin = false,
            canAccessMockTests = false,
            canAccessPdfs = false,
            registrationDate = "আজ",
            activeTimeSeconds = 0L,
            batch = "কম্পিটিটিভ পাঠশালা"
        )
    )
    val currentStudent: StateFlow<Student> = _currentStudent.asStateFlow()

    // Institute registered students list for Admin
    private val _allStudents = MutableStateFlow<List<Student>>(emptyList())
    val allStudents: StateFlow<List<Student>> = _allStudents.asStateFlow()

    // First-Time Test Access Permission Requests
    private val _testAccessRequests = MutableStateFlow<List<TestAccessRequest>>(emptyList())
    val testAccessRequests: StateFlow<List<TestAccessRequest>> = _testAccessRequests.asStateFlow()

    // Anti-Cheating 3-Strike Frozen Test Records
    private val _testFreezeRecords = MutableStateFlow<List<TestFreezeRecord>>(emptyList())
    val testFreezeRecords: StateFlow<List<TestFreezeRecord>> = _testFreezeRecords.asStateFlow()

    // Exam Routine (Empty by default - user creates their own)
    private val _examRoutines = MutableStateFlow<List<ExamRoutine>>(emptyList())
    val examRoutines: StateFlow<List<ExamRoutine>> = _examRoutines.asStateFlow()

    // Mock Tests (Empty by default - user uploads their own)
    private val _mockTests = MutableStateFlow<List<MockTest>>(emptyList())
    val mockTests: StateFlow<List<MockTest>> = _mockTests.asStateFlow()

    // Test Attempts History (Empty by default)
    private val _testAttempts = MutableStateFlow<List<TestAttempt>>(emptyList())
    val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()

    // Books and Scheduled PDF Tests (Categories: History, Geography, Science, Reasoning, Math, Constitution)
    private val _pdfBooks = MutableStateFlow(createSamplePdfs())
    val pdfBooks: StateFlow<List<PdfBook>> = _pdfBooks.asStateFlow()

    // Live Classes
    private val _liveClasses = MutableStateFlow<List<LiveClass>>(emptyList())
    val liveClasses: StateFlow<List<LiveClass>> = _liveClasses.asStateFlow()

    // Institute Notices
    private val _notices = MutableStateFlow(
        listOf(
            InstituteNotice(
                id = "not_1",
                title = "স্বাগতম কম্পিটিটিভ পাঠশালা প্ল্যাটফর্মে",
                dateString = "আজ",
                category = "ইনস্টিটিউট সূচনা",
                content = "এই অ্যাপটি আপনার নিজস্ব। অ্যাডমিন (8902720907) পাসওয়ার্ড (258789) দিয়ে প্রবেশ করে মক টেস্ট তৈরি ও পিডিএফ আপলোড করতে পারবেন। ছাত্রছাত্রীরা প্রথমবার মক টেস্ট দিতে চাইলে অ্যাডমিনের অনুমতি প্রয়োজন হবে।",
                isUrgent = false
            )
        )
    )
    val notices: StateFlow<List<InstituteNotice>> = _notices.asStateFlow()

    // Fee Payments
    private val _feePayments = MutableStateFlow<List<FeePayment>>(emptyList())
    val feePayments: StateFlow<List<FeePayment>> = _feePayments.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }

    fun toggleAdminMode() {
        _isAdminMode.update { !it }
    }

    fun isAdminPhoneNumber(phone: String): Boolean {
        val clean = phone.filter { it.isDigit() }
        return clean == ADMIN_PHONE || clean.endsWith(ADMIN_PHONE)
    }

    fun loginAsAdmin(phone: String, password: String): Boolean {
        val cleanPhone = phone.filter { it.isDigit() }
        val isPhoneMatch = cleanPhone == ADMIN_PHONE || cleanPhone.endsWith(ADMIN_PHONE)
        val isPasswordMatch = password.trim() == ADMIN_PASSWORD
        if (isPhoneMatch && isPasswordMatch) {
            val admin = Student(
                id = "admin_master",
                name = "ইনস্টিটিউট প্রধান / অ্যাডমিন",
                rollNo = "ADM-8902",
                targetExam = "অ্যাডমিন কন্ট্রোল সেন্টার",
                phone = "+91 $ADMIN_PHONE",
                isApproved = true,
                isAdmin = true,
                canAccessMockTests = true,
                canAccessPdfs = true,
                registrationDate = "যাচাইকৃত প্রধান অ্যাডমিন",
                activeTimeSeconds = 0L,
                batch = "প্রধান অ্যাডমিন প্যানেল",
                scoresHistory = emptyList()
            )
            _currentStudent.value = admin
            _isAdminMode.value = true
            _allStudents.update { list ->
                listOf(admin) + list.filterNot { it.id == "admin_master" }
            }
            return true
        }
        return false
    }

    fun registerOrUpdateAdmin(name: String, phone: String) {
        val adminName = name.ifBlank { "ইনস্টিটিউট প্রধান (অ্যাডমিন)" }
        val adminPhone = phone.ifBlank { "+91 $ADMIN_PHONE" }
        val updatedAdmin = Student(
            id = "admin_master",
            name = adminName,
            rollNo = "ADM-8902",
            targetExam = "ইনস্টিটিউট অ্যাডমিন প্যানেল",
            phone = adminPhone,
            isApproved = true,
            isAdmin = true,
            canAccessMockTests = true,
            canAccessPdfs = true,
            registrationDate = "অ্যাডমিন রেজিস্টার্ড",
            activeTimeSeconds = 0L,
            batch = "প্রধান অ্যাডমিন",
            scoresHistory = emptyList()
        )
        _currentStudent.value = updatedAdmin
        _isAdminMode.value = true
        _allStudents.update { list ->
            listOf(updatedAdmin) + list.filterNot { it.id == "admin_master" }
        }
    }

    fun registerStudentWithPhone(name: String, phone: String, targetExam: String): Student {
        val newStudent = Student(
            id = "std_${System.currentTimeMillis()}",
            name = name.ifBlank { "নতুন শিক্ষার্থী" },
            rollNo = "CP-${(100..999).random()}",
            targetExam = targetExam.ifBlank { "চাকরির প্রস্তুতি" },
            phone = phone,
            isApproved = false, // Admin must approve!
            isAdmin = false,
            canAccessMockTests = false,
            canAccessPdfs = false,
            approvedTestIds = emptyList(),
            completedTestIds = emptyList(),
            registrationDate = "আজ নতুন রেজিস্ট্রেশন",
            activeTimeSeconds = 0L,
            batch = "কম্পিটিটিভ পাঠশালা"
        )
        _allStudents.update { listOf(newStudent) + it }
        _currentStudent.value = newStudent
        return newStudent
    }

    fun approveStudent(studentId: String) {
        _allStudents.update { list ->
            list.map { student ->
                if (student.id == studentId) {
                    student.copy(isApproved = true, canAccessMockTests = true, canAccessPdfs = true)
                } else student
            }
        }
        if (_currentStudent.value.id == studentId) {
            _currentStudent.update { it.copy(isApproved = true, canAccessMockTests = true, canAccessPdfs = true) }
        }
    }

    fun rejectStudent(studentId: String) {
        _allStudents.update { list -> list.filterNot { it.id == studentId } }
        if (_currentStudent.value.id == studentId) {
            _allStudents.value.firstOrNull()?.let { _currentStudent.value = it }
        }
    }

    // First-Time Test Access Permission Request
    fun requestMockTestAccess(mockTestId: String, mockTestTitle: String) {
        val student = _currentStudent.value
        val existing = _testAccessRequests.value.find { it.studentId == student.id && it.mockTestId == mockTestId }
        if (existing == null) {
            val req = TestAccessRequest(
                id = "req_${System.currentTimeMillis()}",
                studentId = student.id,
                studentName = student.name,
                studentPhone = student.phone,
                mockTestId = mockTestId,
                mockTestTitle = mockTestTitle,
                requestedAt = "এখনই আবেদন করা হয়েছে",
                isApproved = false
            )
            _testAccessRequests.update { listOf(req) + it }
        }
    }

    fun approveTestAccessRequest(requestId: String) {
        val req = _testAccessRequests.value.find { it.id == requestId } ?: return
        _testAccessRequests.update { list ->
            list.map { if (it.id == requestId) it.copy(isApproved = true) else it }
        }
        _allStudents.update { list ->
            list.map { st ->
                if (st.id == req.studentId) {
                    st.copy(approvedTestIds = (st.approvedTestIds + req.mockTestId).distinct())
                } else st
            }
        }
        if (_currentStudent.value.id == req.studentId) {
            _currentStudent.update {
                it.copy(approvedTestIds = (it.approvedTestIds + req.mockTestId).distinct())
            }
        }
    }

    fun canStudentAccessTest(student: Student, mockTestId: String): Boolean {
        if (student.isAdmin || _isAdminMode.value) return true
        if (student.completedTestIds.contains(mockTestId)) return true
        return student.approvedTestIds.contains(mockTestId)
    }

    // 3-Strike Anti-Cheating & Admin Freeze
    fun reportViolationStrike(mockTestId: String, mockTestTitle: String, currentStrikes: Int): Int {
        val newStrikes = currentStrikes + 1
        if (newStrikes >= 3) {
            val student = _currentStudent.value
            _currentStudent.update {
                it.copy(
                    isTestFrozen = true,
                    frozenTestId = mockTestId,
                    freezeReason = "৩ বার নিয়ম লঙ্ঘন (অ্যাপ/উইন্ডো পরিবর্তন)"
                )
            }
            val freezeRecord = TestFreezeRecord(
                id = "freeze_${System.currentTimeMillis()}",
                studentId = student.id,
                studentName = student.name,
                studentPhone = student.phone,
                mockTestId = mockTestId,
                mockTestTitle = mockTestTitle,
                strikeCount = 3,
                frozenAt = "এখনই ফ্রোজেন",
                isUnfrozen = false
            )
            _testFreezeRecords.update {
                listOf(freezeRecord) + it.filterNot { rec -> rec.studentId == student.id && rec.mockTestId == mockTestId }
            }
        }
        return newStrikes
    }

    fun unfreezeStudentTest(freezeRecordId: String) {
        val rec = _testFreezeRecords.value.find { it.id == freezeRecordId } ?: return
        _testFreezeRecords.update { list ->
            list.map { if (it.id == freezeRecordId) it.copy(isUnfrozen = true) else it }
        }
        _allStudents.update { list ->
            list.map { st ->
                if (st.id == rec.studentId) {
                    st.copy(isTestFrozen = false, frozenTestId = null, freezeReason = "")
                } else st
            }
        }
        if (_currentStudent.value.id == rec.studentId) {
            _currentStudent.update {
                it.copy(isTestFrozen = false, frozenTestId = null, freezeReason = "")
            }
        }
    }

    // Toggle individual student permission for Mock Tests
    fun toggleMockTestPermission(studentId: String) {
        _allStudents.update { list ->
            list.map { student ->
                if (student.id == studentId) {
                    val updated = !student.canAccessMockTests
                    student.copy(canAccessMockTests = updated)
                } else student
            }
        }
        if (_currentStudent.value.id == studentId) {
            _currentStudent.update { it.copy(canAccessMockTests = !it.canAccessMockTests) }
        }
    }

    // Toggle individual student permission for PDFs
    fun togglePdfPermission(studentId: String) {
        _allStudents.update { list ->
            list.map { student ->
                if (student.id == studentId) {
                    val updated = !student.canAccessPdfs
                    student.copy(canAccessPdfs = updated)
                } else student
            }
        }
        if (_currentStudent.value.id == studentId) {
            _currentStudent.update { it.copy(canAccessPdfs = !it.canAccessPdfs) }
        }
    }

    fun grantAllMockTestPermission(grant: Boolean) {
        _allStudents.update { list ->
            list.map { student ->
                if (!student.isAdmin) student.copy(canAccessMockTests = grant) else student
            }
        }
        if (!_currentStudent.value.isAdmin) {
            _currentStudent.update { it.copy(canAccessMockTests = grant) }
        }
    }

    fun grantAllPdfPermission(grant: Boolean) {
        _allStudents.update { list ->
            list.map { student ->
                if (!student.isAdmin) student.copy(canAccessPdfs = grant) else student
            }
        }
        if (!_currentStudent.value.isAdmin) {
            _currentStudent.update { it.copy(canAccessPdfs = grant) }
        }
    }

    fun getLeaderboard(): List<LeaderboardEntry> {
        val attempts = _testAttempts.value
        if (attempts.isEmpty()) return emptyList()
        return attempts
            .sortedByDescending { it.score }
            .mapIndexed { idx, att ->
                val total = (att.correctCount + att.incorrectCount + att.unattemptedCount).coerceAtLeast(1)
                val accuracy = Math.round((att.correctCount.toDouble() / total) * 1000.0) / 10.0
                val st = _allStudents.value.find { it.id == att.studentId }
                LeaderboardEntry(
                    rank = idx + 1,
                    studentName = att.studentName,
                    rollNo = st?.rollNo ?: "CP-${100 + idx}",
                    targetExam = st?.targetExam ?: "চাকরির প্রস্তুতি",
                    score = att.score,
                    totalMarks = (total * 1.0).toInt(),
                    accuracyPercent = accuracy,
                    timeSpentSeconds = att.timeSpentSeconds,
                    mockTestTitle = att.mockTestTitle
                )
            }
    }

    // WhatsApp Group Admin style: Individually make any student Admin or dismiss Admin
    fun toggleAdminRole(studentId: String) {
        _allStudents.update { list ->
            list.map { student ->
                if (student.id == studentId) {
                    val newRole = !student.isAdmin
                    student.copy(
                        isAdmin = newRole,
                        canAccessMockTests = if (newRole) true else student.canAccessMockTests,
                        canAccessPdfs = if (newRole) true else student.canAccessPdfs
                    )
                } else student
            }
        }
        if (_currentStudent.value.id == studentId) {
            val newRole = !_currentStudent.value.isAdmin
            _currentStudent.update {
                it.copy(
                    isAdmin = newRole,
                    canAccessMockTests = if (newRole) true else it.canAccessMockTests,
                    canAccessPdfs = if (newRole) true else it.canAccessPdfs
                )
            }
        }
    }

    fun switchToStudent(studentId: String) {
        _allStudents.value.find { it.id == studentId }?.let { target ->
            _currentStudent.value = target
            if (target.isAdmin) {
                _isAdminMode.value = true
            }
        }
    }

    fun setStudentApproval(studentId: String, approved: Boolean) {
        if (approved) approveStudent(studentId) else rejectStudent(studentId)
    }

    fun togglePdfLock(pdfId: String) {
        _pdfBooks.update { list ->
            list.map { pdf ->
                if (pdf.id == pdfId) pdf.copy(isLocked = !pdf.isLocked) else pdf
            }
        }
    }

    fun recordActiveTime(seconds: Long) {
        _currentStudent.update { it.copy(activeTimeSeconds = it.activeTimeSeconds + seconds) }
    }

    fun submitTestAttempt(
        mockTest: MockTest,
        answers: Map<Int, Int>,
        timeSpentSeconds: Int,
        questionTimeSeconds: Map<Int, Int> = emptyMap(),
        violationStrikes: Int
    ): TestAttempt {
        var correct = 0
        var incorrect = 0
        var unattempted = 0

        mockTest.questions.forEachIndexed { index, q ->
            val userAns = answers[index]
            if (userAns == null) {
                unattempted++
            } else if (userAns == q.correctOptionIndex) {
                correct++
            } else {
                incorrect++
            }
        }

        val positiveMarks = correct * mockTest.markPerCorrect
        val negativeMarks = incorrect * mockTest.negativeMarkPerWrong
        val finalScore = (positiveMarks - negativeMarks).coerceAtLeast(0.0)

        val previousAttemptsCount = _testAttempts.value.count {
            it.mockTestId == mockTest.id && it.studentId == _currentStudent.value.id
        }

        val attempt = TestAttempt(
            attemptId = "att_${System.currentTimeMillis()}",
            mockTestId = mockTest.id,
            mockTestTitle = mockTest.getTitle(_appLanguage.value),
            studentId = _currentStudent.value.id,
            studentName = _currentStudent.value.name,
            attemptNumber = previousAttemptsCount + 1,
            userAnswers = answers,
            score = Math.round(finalScore * 100.0) / 100.0,
            correctCount = correct,
            incorrectCount = incorrect,
            unattemptedCount = unattempted,
            negativeDeducted = Math.round(negativeMarks * 100.0) / 100.0,
            timeSpentSeconds = timeSpentSeconds,
            questionTimeSeconds = questionTimeSeconds,
            violationStrikes = violationStrikes
        )

        _testAttempts.update { listOf(attempt) + it }
        _currentStudent.update {
            it.copy(
                completedTestIds = (it.completedTestIds + mockTest.id).distinct(),
                isTestFrozen = false,
                frozenTestId = null
            )
        }
        _allStudents.update { list ->
            list.map { st ->
                if (st.id == _currentStudent.value.id) {
                    st.copy(
                        completedTestIds = (st.completedTestIds + mockTest.id).distinct(),
                        isTestFrozen = false,
                        frozenTestId = null
                    )
                } else st
            }
        }
        return attempt
    }

    fun sendChatMessage(text: String, attachmentName: String? = null, attachmentType: String? = null) {
        val isTeacher = _isAdminMode.value
        val name = if (isTeacher) "শিক্ষক (কম্পিটিটিভ পাঠশালা)" else _currentStudent.value.name
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = name,
            isTeacher = isTeacher,
            messageText = text,
            attachmentName = attachmentName,
            attachmentType = attachmentType
        )
        _chatMessages.update { it + newMsg }
    }

    fun payFee(feeId: String, paymentMethod: String): Boolean {
        val txId = "UPI-CPATHSHALA-${(10000000..99999999).random()}"
        _feePayments.update { list ->
            list.map { fee ->
                if (fee.id == feeId) {
                    fee.copy(
                        isPaid = true,
                        transactionId = txId,
                        paymentDate = "আজ সম্পন্ন",
                        paymentMethod = paymentMethod
                    )
                } else fee
            }
        }
        return true
    }

    fun addExamRoutine(routine: ExamRoutine) {
        _examRoutines.update { it + routine }
    }

    fun addNotice(notice: InstituteNotice) {
        _notices.update { listOf(notice) + it }
    }

    fun createMockTest(
        title: String,
        category: String,
        durationMinutes: Int,
        negativeMarkPerWrong: Double = 0.25,
        markPerCorrect: Double = 1.0,
        isLockedByAdmin: Boolean = true,
        questions: List<MockQuestion> = emptyList()
    ): MockTest {
        val totalMarks = if (questions.isNotEmpty()) (questions.size * markPerCorrect).toInt() else 25
        val newTest = MockTest(
            id = "mock_${System.currentTimeMillis()}",
            titleBengali = title,
            titleEnglish = title,
            titleHindi = title,
            descriptionBengali = "অ্যাডমিন প্যানেল থেকে সরাসরি আপলোডকৃত টেস্ট • মোট প্রশ্ন: ${questions.size} • সময়: $durationMinutes মিনিট",
            descriptionEnglish = "Directly uploaded from Admin Panel • Total questions: ${questions.size} • Time: $durationMinutes mins",
            descriptionHindi = "एडमिन पैनल से सीधे अपलोड किया गया टेस्ट • कुल प्रश्न: ${questions.size} • समय: $durationMinutes मिनट",
            category = category,
            durationMinutes = durationMinutes,
            totalMarks = totalMarks,
            markPerCorrect = markPerCorrect,
            negativeMarkPerWrong = negativeMarkPerWrong,
            isLockedByAdmin = isLockedByAdmin,
            questions = questions
        )
        _mockTests.update { listOf(newTest) + it }
        return newTest
    }

    fun addQuestionToMockTest(testId: String, question: MockQuestion) {
        _mockTests.update { list ->
            list.map { test ->
                if (test.id == testId) {
                    val updatedQuestions = test.questions + question.copy(id = test.questions.size + 1)
                    val newTotalMarks = (updatedQuestions.size * test.markPerCorrect).toInt()
                    test.copy(
                        questions = updatedQuestions,
                        totalMarks = newTotalMarks,
                        descriptionBengali = "${test.category} মক টেস্ট • মোট প্রশ্ন: ${updatedQuestions.size} টি • সময়: ${test.durationMinutes} মিনিট"
                    )
                } else test
            }
        }
    }

    // Direct upload of multiple questions extracted from PDF / Raw Text
    fun addQuestionsBatchToMockTest(testId: String, newQuestions: List<MockQuestion>) {
        _mockTests.update { list ->
            list.map { test ->
                if (test.id == testId) {
                    var currentId = test.questions.size
                    val formattedNewQuestions = newQuestions.map { q ->
                        currentId++
                        q.copy(id = currentId)
                    }
                    val updatedQuestions = test.questions + formattedNewQuestions
                    val newTotalMarks = (updatedQuestions.size * test.markPerCorrect).toInt()
                    test.copy(
                        questions = updatedQuestions,
                        totalMarks = newTotalMarks,
                        descriptionBengali = "${test.category} মক টেস্ট • মোট প্রশ্ন: ${updatedQuestions.size} টি • সময়: ${test.durationMinutes} মিনিট"
                    )
                } else test
            }
        }
    }

    fun deleteMockTest(testId: String) {
        _mockTests.update { list -> list.filterNot { it.id == testId } }
    }

    fun toggleMockTestLock(testId: String) {
        _mockTests.update { list ->
            list.map { test ->
                if (test.id == testId) test.copy(isLockedByAdmin = !test.isLockedByAdmin) else test
            }
        }
    }

    fun addPdfBook(pdfBook: PdfBook) {
        _pdfBooks.update { listOf(pdfBook) + it }
    }

    private fun createSampleMockTests(): List<MockTest> {
        val wbcsQuestions = listOf(
            MockQuestion(
                id = 1,
                questionBengali = "১৮৮৫ সালে ভারতীয় জাতীয় কংগ্রেসের প্রথম অধিবেশন কোথায় অনুষ্ঠিত হয়েছিল?",
                questionEnglish = "Where was the first session of the Indian National Congress held in 1885?",
                questionHindi = "1885 में भारतीय राष्ट्रीय कांग्रेस का पहला अधिवेशन कहाँ आयोजित किया गया था?",
                optionsBengali = listOf("কলকাতা", "বোম্বাই (গোকুলদাস তেজপাল সংস্কৃত কলেজ)", "মাদ্রাজ", "লাহোর"),
                optionsEnglish = listOf("Calcutta", "Bombay (Gokuldas Tejpal Sanskrit College)", "Madras", "Lahore"),
                optionsHindi = listOf("कलकत्ता", "बॉम्बे (गोकुलदास तेजपाल संस्कृत कॉलेज)", "मद्रास", "लाहौर"),
                correctOptionIndex = 1,
                explanationBengali = "১৮৮৫ সালের ২৮ ডিসেম্বর বোম্বাইয়ের গোকুলদাস তেজপাল সংস্কৃত কলেজে উমেশচন্দ্র বন্দ্যোপাধ্যায়ের সভাপতিত্বে প্রথম অধিবেশন বসেছিল এবং এতে ৭২ জন প্রতিনিধি উপস্থিত ছিলেন।",
                explanationEnglish = "The first session was held on 28 December 1885 at Gokuldas Tejpal Sanskrit College in Bombay, presided over by W.C. Bonnerjee with 72 delegates.",
                explanationHindi = "पहला अधिवेशन 28 दिसंबर 1885 को बॉम्बे के गोकुलदास तेजपाल संस्कृत कॉलेज में डब्ल्यू.सी. बनर्जी की अध्यक्षता में हुआ था, जिसमें 72 प्रतिनिधि शामिल हुए थे।",
                subjectBengali = "ভারতের ইতিহাস",
                subjectEnglish = "Indian History",
                subjectHindi = "भारतीय इतिहास"
            ),
            MockQuestion(
                id = 2,
                questionBengali = "পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ কোনটি?",
                questionEnglish = "Which is the highest peak of West Bengal?",
                questionHindi = "पश्चिम बंगाल की सबसे ऊँची चोटी कौन सी है?",
                optionsBengali = listOf("সান্দাকফু (৩৬৩৬ মিটার)", "ফালোট", "সবরগ্রাম", "টাইগার হিল"),
                optionsEnglish = listOf("Sandakphu (3636 m)", "Phalut", "Sabargram", "Tiger Hill"),
                optionsHindi = listOf("संदकफू (3636 मीटर)", "फालूट", "सबरग्राम", "टाइगर हिल"),
                correctOptionIndex = 0,
                explanationBengali = "সান্দাকফু (Sandakphu) পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ, যার উচ্চতা ৩,৬৩৬ মিটার। এটি সিঙ্গালীলা পর্বতশ্রেণীতে অবস্থিত।",
                explanationEnglish = "Sandakphu is the highest peak in West Bengal at an altitude of 3,636 meters, located on the Singalila Ridge.",
                explanationHindi = "संदकफू पश्चिम बंगाल की सबसे ऊँची चोटी है (ऊँचाई 3,636 मीटर)। यह सिंगालीला श्रेणी में स्थित है।",
                subjectBengali = "পশ্চিমবঙ্গের ভূগোল",
                subjectEnglish = "Geography of West Bengal",
                subjectHindi = "पश्चिम बंगाल का भूगोल"
            ),
            MockQuestion(
                id = 3,
                questionBengali = "ভারতীয় সংবিধানের কোন ধারায় 'মৌলিক অধিকার' ভঙ্গের বিরুদ্ধে সুপ্রিম কোর্টে রিট (Writ) জারির ক্ষমতা দেওয়া হয়েছে?",
                questionEnglish = "Which article empowers the Supreme Court to issue writs for the enforcement of Fundamental Rights?",
                questionHindi = "भारतीय संविधान का कौन सा अनुच्छेद मौलिक अधिकारों के प्रवर्तन के लिए सुप्रीम कोर्ट को रिट जारी करने का अधिकार देता है?",
                optionsBengali = listOf("ধারা ২১", "ধারা ৩২", "ধারা ২২৬", "ধারা ৫০"),
                optionsEnglish = listOf("Article 21", "Article 32", "Article 226", "Article 50"),
                optionsHindi = listOf("अनुच्छेद 21", "अनुच्छेद 32", "अनुच्छेद 226", "अनुच्छेद 50"),
                correctOptionIndex = 1,
                explanationBengali = "ধারা ৩২ অনুসারে সুপ্রিম কোর্ট রিট জারি করতে পারে। ড. বি আর আম্বেদকর এটিকে সংবিধানের 'হৃদয় ও আত্মা' (Heart and Soul) বলে অভিহিত করেছেন।",
                explanationEnglish = "Article 32 guarantees the right to constitutional remedies. Dr. B.R. Ambedkar called it the 'Heart and Soul' of the Constitution.",
                explanationHindi = "अनुच्छेद 32 सुप्रीम कोर्ट को रिट जारी करने की शक्ति देता है। डॉ. बी.आर. अंबेडकर ने इसे संविधान का 'हृदय और आत्मा' कहा था।",
                subjectBengali = "ভারতীয় সংবিধান",
                subjectEnglish = "Indian Polity",
                subjectHindi = "भारतीय संविधान"
            ),
            MockQuestion(
                id = 4,
                questionBengali = "একটি জিনিসের ক্রয়মূল্য ৮০০ টাকা। ২৫% লাভে বিক্রি করতে হলে বিক্রয়মূল্য কত হতে হবে?",
                questionEnglish = "The cost price of an item is ₹800. What should be the selling price for a 25% profit?",
                questionHindi = "एक वस्तु का क्रय मूल्य ₹800 है। 25% लाभ प्राप्त करने के लिए विक्रय मूल्य कितना होना चाहिए?",
                optionsBengali = listOf("৯৫০ টাকা", "১০০০ টাকা", "১০৫০ টাকা", "১১০০ টাকা"),
                optionsEnglish = listOf("₹950", "₹1000", "₹1050", "₹1100"),
                optionsHindi = listOf("₹950", "₹1000", "₹1050", "₹1100"),
                correctOptionIndex = 1,
                explanationBengali = "লাভ = ৮০০ × ২৫/১০০ = ২০০ টাকা। অতএব বিক্রয়মূল্য = ৮০০ + ২০০ = ১০০০ টাকা।",
                explanationEnglish = "Profit = 800 * 25% = ₹200. Therefore, Selling Price = 800 + 200 = ₹1000.",
                explanationHindi = "लाभ = 800 * 25/100 = ₹200। अतः विक्रय मूल्य = 800 + 200 = ₹1000।",
                subjectBengali = "পাটিগণিত (Math)",
                subjectEnglish = "Quantitative Aptitude",
                subjectHindi = "अंकगणित (गणित)"
            ),
            MockQuestion(
                id = 5,
                questionBengali = "নীতি আয়োগের (NITI Aayog) পদাধিকারবলে সভাপতি কে হন?",
                questionEnglish = "Who is the ex-officio Chairman of NITI Aayog?",
                questionHindi = "नीति आयोग का पदेन अध्यक्ष कौन होता है?",
                optionsBengali = listOf("ভারতের রাষ্ট্রপতি", "ভারতের অর্থমন্ত্রী", "ভারতের প্রধানমন্ত্রী", "রিজার্ভ ব্যাংকের গভর্নর"),
                optionsEnglish = listOf("President of India", "Finance Minister", "Prime Minister of India", "RBI Governor"),
                optionsHindi = listOf("भारत के राष्ट्रपति", "वित्त मंत्री", "भारत के प्रधानमंत्री", "आरबीआई गवर्नर"),
                correctOptionIndex = 2,
                explanationBengali = "ভারতের প্রধানমন্ত্রী পদাধিকারবলে নীতি আয়োগের সভাপতি হন। ১ জানুয়ারি ২০১৫ সালে যোজনা কমিশনের পরিবর্তে এটি গঠিত হয়।",
                explanationEnglish = "The Prime Minister of India is the ex-officio Chairperson of NITI Aayog, established on 1 January 2015.",
                explanationHindi = "भारत के प्रधानमंत्री नीति आयोग के पदेन अध्यक्ष होते हैं। इसका गठन 1 जनवरी 2015 को योजना आयोग के स्थान पर किया गया था।",
                subjectBengali = "ভারতীয় অর্থনীতি",
                subjectEnglish = "Indian Economy",
                subjectHindi = "भारतीय अर्थव्यवस्था"
            ),
            MockQuestion(
                id = 6,
                questionBengali = "ক্লোরোফিলে উপস্থিত কোন ধাতব উপাদান উদ্ভিদের সালোকসংশ্লেষে সহায়তা করে?",
                questionEnglish = "Which metal is present in chlorophyll that aids in photosynthesis?",
                questionHindi = "क्लोरोफिल में उपस्थित कौन सा धातु तत्व प्रकाश संश्लेषण में सहायता करता है?",
                optionsBengali = listOf("লোহা (Iron)", "ম্যাগনেসিয়াম (Magnesium)", "তামা (Copper)", "দস্তা (Zinc)"),
                optionsEnglish = listOf("Iron", "Magnesium", "Copper", "Zinc"),
                optionsHindi = listOf("लोहा (Iron)", "मैग्नीशियम (Magnesium)", "तांबा (Copper)", "जस्ता (Zinc)"),
                correctOptionIndex = 1,
                explanationBengali = "ক্লোরোফিল অণুর কেন্দ্রে ম্যাগনেসিয়াম (Mg) পরমাণু যুক্ত থাকে যা আলোক শক্তি শোষণে কেন্দ্রীয় ভূমিকা রাখে।",
                explanationEnglish = "Magnesium (Mg) atom is located at the center of the porphyrin ring of the chlorophyll molecule.",
                explanationHindi = "क्लोरोफिल अणु के केंद्र में मैग्नीशियम (Mg) धातु परमाणु होता है जो प्रकाश ऊर्जा अवशोषण में मुख्य भूमिका निभाता है।",
                subjectBengali = "সাধারণ বিজ্ঞান",
                subjectEnglish = "General Science",
                subjectHindi = "सामान्य विज्ञान"
            ),
            MockQuestion(
                id = 7,
                questionBengali = "কোন বছর বঙ্গভঙ্গ রদ (Annulment of Partition of Bengal) করা হয়েছিল?",
                questionEnglish = "In which year was the Partition of Bengal annulled?",
                questionHindi = "बंगाल विभाजन किस वर्ष रद्द (निरस्त) किया गया था?",
                optionsBengali = listOf("১৯০৫", "১৯০৬", "১৯১১", "১৯১৯"),
                optionsEnglish = listOf("1905", "1906", "1911", "1919"),
                optionsHindi = listOf("1905", "1906", "1911", "1919"),
                correctOptionIndex = 2,
                explanationBengali = "১৯১১ সালের ১২ ডিসেম্বর লর্ড হার্ডিঞ্জ-এর সময় পঞ্চম জর্জের দিল্লি দরবারে বঙ্গভঙ্গ রদ ঘোষণা করা হয় এবং রাজধানী কলকাতা থেকে দিল্লিতে স্থানান্তরিত হয়।",
                explanationEnglish = "Partition of Bengal was annulled in 1911 by Lord Hardinge at the Delhi Durbar of King George V.",
                explanationHindi = "1911 में लॉर्ड हार्डिंग के समय जॉर्ज पंचम के दिल्ली दरबार में बंगाल विभाजन रद्द करने की घोषणा की गई थी।",
                subjectBengali = "ভারতের ইতিহাস",
                subjectEnglish = "Indian History",
                subjectHindi = "भारतीय इतिहास"
            ),
            MockQuestion(
                id = 8,
                questionBengali = "যদি CAT = 24 এবং DOG = 26 হয়, তবে LION = কত?",
                questionEnglish = "If CAT = 24 and DOG = 26, then LION = ?",
                questionHindi = "यदि CAT = 24 और DOG = 26 है, तो LION = कितना होगा?",
                optionsBengali = listOf("45", "50", "52", "48"),
                optionsEnglish = listOf("45", "50", "52", "48"),
                optionsHindi = listOf("45", "50", "52", "48"),
                correctOptionIndex = 1,
                explanationBengali = "বর্ণমালার অবস্থানের যোগফল: L(12) + I(9) + O(15) + N(14) = 50।",
                explanationEnglish = "Sum of alphabetical positions: L(12) + I(9) + O(15) + N(14) = 50.",
                explanationHindi = "वर्णमाला स्थिति का योग: L(12) + I(9) + O(15) + N(14) = 50।",
                subjectBengali = "লজিক্যাল রিজনিং",
                subjectEnglish = "Logical Reasoning",
                subjectHindi = "तार्किक योग्यता (Reasoning)"
            )
        )

        return listOf(
            MockTest(
                id = "test_wbcs_01",
                titleBengali = "WBCS 2026 প্রিলিমিনারি মডেল মক টেস্ট - ১",
                titleEnglish = "WBCS 2026 Preliminary Model Mock Test - 1",
                titleHindi = "WBCS 2026 प्रारंभिक मॉडल मॉक टेस्ट - 1",
                descriptionBengali = "WBPSC স্ট্যান্ডার্ড অনুযায়ী নেগেটিভ মার্কিং সহ প্রিলিমসের চূড়ান্ত প্রস্তুতি। এতে চিটিং প্রতিরোধক সুরক্ষিত মোড সক্রিয় রয়েছে।",
                descriptionEnglish = "Comprehensive preliminary prep with negative marking and lockdown anti-cheating mode active.",
                descriptionHindi = "नेगेटिव मार्किंग और चीटिंग-रोधी सुरक्षित लॉकडाउन मोड के साथ प्रारंभिक परीक्षा की पूर्ण तैयारी।",
                category = "WBCS Prelims",
                durationMinutes = 15,
                totalMarks = 8,
                markPerCorrect = 1.0,
                negativeMarkPerWrong = 0.25,
                questions = wbcsQuestions
            ),
            MockTest(
                id = "test_wbp_02",
                titleBengali = "পশ্চিমবঙ্গ পুলিশ SI ও কনস্টেবল স্পেশাল টেস্ট",
                titleEnglish = "WB Police SI & Constable Special Mock",
                titleHindi = "पश्चिम बंगाल पुलिस SI व कांस्टेबल स्पेशल मॉक",
                descriptionBengali = "WBP রিক্রুটমেন্ট বোর্ডের সিলেবাস অনুযায়ী গণিত, রিজনিং ও সাধারণ জ্ঞান।",
                descriptionEnglish = "WBP recruitment syllabus based practice for SI and Constable posts.",
                descriptionHindi = "WBP भर्ती बोर्ड के पाठ्यक्रम पर आधारित गणित, रीजनिंग और सामान्य ज्ञान।",
                category = "WBP Police SI",
                durationMinutes = 10,
                totalMarks = 8,
                markPerCorrect = 1.0,
                negativeMarkPerWrong = 0.25,
                questions = wbcsQuestions
            )
        )
    }

    private fun createSampleAttempts(): List<TestAttempt> {
        return listOf(
            TestAttempt(
                attemptId = "att_init_1",
                mockTestId = "test_wbcs_01",
                mockTestTitle = "WBCS 2026 প্রিলিমিনারি মডেল মক টেস্ট - ১",
                studentId = "std_101",
                studentName = "শিক্ষার্থী",
                attemptNumber = 1,
                userAnswers = mapOf(0 to 1, 1 to 0, 2 to 1, 3 to 1, 4 to 2, 5 to 0, 6 to 2),
                score = 5.75,
                correctCount = 6,
                incorrectCount = 1,
                unattemptedCount = 1,
                negativeDeducted = 0.25,
                timeSpentSeconds = 540,
                violationStrikes = 0,
                completedAt = System.currentTimeMillis() - 86400000
            )
        )
    }

    private fun createSamplePdfs(): List<PdfBook> {
        return listOf(
            PdfBook(
                id = "pdf_exam_01",
                title = "🔒 কম্পিটিটিভ পাঠশালা মক টেস্ট পেপার (শিডিউলড এক্সাম)",
                author = "কম্পিটিটিভ পাঠশালা রিসার্চ প্যানেল",
                category = "শিডিউলড টেস্ট",
                totalPages = 12,
                fileSize = "2.4 MB",
                isScheduledExamPdf = true,
                isLocked = true,
                scheduledUnlockTime = "রবিবার সকাল ১০:০০ টা (নির্দিষ্ট সময়ে আনলক হবে)",
                description = "এই পিডিএফ টেস্টটি নির্দিষ্ট সময়ে স্বয়ংক্রিয়ভাবে বা শিক্ষকের নির্দেশে আনলক হবে। শিক্ষার্থীরা নির্দিষ্ট সময়ে প্রশ্ন দেখে অ্যাপের মাধ্যমে উত্তর সাবমিট করবে।",
                samplePages = listOf(
                    "কম্পিটিটিভ পাঠশালা - সাপ্তাহিক টেস্ট পেপার (পৃষ্ঠা ১)\nবিভাগ ক: প্রাচীন ও মধ্যযুগীয় ইতিহাস\n১. হরপ্পা সভ্যতার বৃহত্তম প্রত্নক্ষেত্র কোনটি?\n২. সম্রাট অশোকের কলিঙ্গ যুদ্ধের বিবরণ কোন শিলালিপিতে রয়েছে?\n৩. দিল্লির কোন সুলতান রক্ত ও লৌহ নীতি (Blood and Iron policy) গ্রহণ করেন?",
                    "কম্পিটিটিভ পাঠশালা - সাপ্তাহিক টেস্ট পেপার (পৃষ্ঠা ২)\nবিভাগ খ: ভূগোল ও পরিবেশ\n৪. তিস্তা নদীর উৎপত্তি কোন হিমবাহ থেকে?\n৫. পশ্চিমবঙ্গের সুন্দরবনকে রামসার সাইট হিসেবে কবে ঘোষণা করা হয়?"
                )
            ),
            PdfBook(
                id = "pdf_hist_01",
                title = "ভারতের স্বাধীনতা সংগ্রাম ও প্রাচীন-মধ্যযুগীয় ইতিহাস নোটস",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "ইতিহাস",
                totalPages = 145,
                fileSize = "8.6 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "১৭৫৭ পলাশীর যুদ্ধ থেকে ১৯৪৭ ভারতের স্বাধীনতা লাভ পর্যন্ত বিস্তারিত তথ্যাবলী, গভর্নর জেনারেলদের কার্যকাল ও চার্ট।",
                samplePages = listOf(
                    "অধ্যায় ১: ব্রিটিশ শক্তির বিস্তার ও ১৮৫৭ মহাবিদ্রোহ\nপলাশীর যুদ্ধ (১৭৫৭) ও বক্সারের যুদ্ধের (১৭৬৪) মাধ্যমে বাংলায় ইংরেজ কর্তৃত্ব প্রতিষ্ঠা পায়। লর্ড ক্লাইভের দ্বৈত শাসন ব্যবস্থা বাংলায় তীব্র অর্থনৈতিক সংকট সৃষ্টি করে।",
                    "অধ্যায় ২: ভারতীয় জাতীয় কংগ্রেস ও নরমপন্থী পর্যায় (১৮৮৫-১৯০৫)\n১৮৮৫ সালের ডিসেম্বরে সুরেন্দ্রনাথ বন্দ্যোপাধ্যায় ও অ্যালান অক্টাভিয়ান হিউমের প্রচেষ্টায় জাতীয় কংগ্রেস প্রতিষ্ঠিত হয়।"
                )
            ),
            PdfBook(
                id = "pdf_geo_01",
                title = "পশ্চিমবঙ্গ ও ভারতের সামগ্রিক ভূগোল মানচিত্র ও সারসংক্ষেপ",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "ভূগোল",
                totalPages = 120,
                fileSize = "7.2 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "পশ্চিমবঙ্গের জেলাভিত্তিক সম্পদ, নদনদী, কৃষিকাজ, শিল্পাঞ্চল এবং ভারতের ভূ-প্রকৃতি সংক্রান্ত সম্পূর্ণ নোটস।",
                samplePages = listOf(
                    "অধ্যায় ১: পশ্চিমবঙ্গের ভূ-প্রাকৃতিক বিভাগ\nউত্তরের পার্বত্য অঞ্চল, পশ্চিমের মালভূমি অঞ্চল এবং গাঙ্গেয় সমভূমি অঞ্চলের বিস্তারিত আলোচনা। সান্দাকফু (৩৬৩৬ মি) রাজ্যের সর্বোচ্চ শৃঙ্গ।",
                    "অধ্যায় ২: ভারতের নদী ব্যবস্থা\nহিমালয় সৃষ্ট নদীসমূহ (গঙ্গা, সিন্ধু, ব্রহ্মপুত্র) এবং উপদ্বীপীয় নদীসমূহের (গোদাবরী, কৃষ্ণা, কাবেরী) গতিপথ ও উপনদী।"
                )
            ),
            PdfBook(
                id = "pdf_sci_01",
                title = "সাধারণ বিজ্ঞান (পদার্থ, রসায়ন ও জীববিদ্যা) কমপ্লিট গাইড",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "সায়েন্স",
                totalPages = 160,
                fileSize = "9.5 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "দৈনন্দিন জীবনে বিজ্ঞানের প্রয়োগ, ভিটামিন ও রোগব্যাধি, রাসায়নিক সংকেত এবং নিউটনের গতিসূত্রের ব্যাখ্যা।",
                samplePages = listOf(
                    "অধ্যায় ১: জীববিদ্যা ও মানব শারীরবিদ্যা\nরক্তের গ্রুপ, হৃদপিণ্ডের গঠন, মানবদেহের বিভিন্ন হরমোন ও প্রধান ভিটামিনসমূহের রাসায়নিক নাম ও অভাবজনিত রোগ।",
                    "অধ্যায় ২: পদার্থবিজ্ঞান ও আলোকবিদ্যা\nআলোর প্রতিফলন, প্রতিসরণ ও বিচ্ছুরণ। রামধনু সৃষ্টির প্রাকৃতিক কারণ এবং উত্তল ও অবতল লেন্সের ব্যবহার।"
                )
            ),
            PdfBook(
                id = "pdf_reas_01",
                title = "লজিক্যাল রিজনিং ও মেন্টাল অ্যাবিলিটি শর্টকাট ট্রিকস",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "রিজনিং",
                totalPages = 130,
                fileSize = "6.8 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "কোডিং-ডিকোডিং, রক্ত সম্পর্কিত সম্পর্ক (Blood Relation), সিটিং অ্যারেঞ্জমেন্ট ও সিরিজ টেস্টের সহজ সমাধান।",
                samplePages = listOf(
                    "অধ্যায় ১: কোডিং ও ডিকোডিং শর্টকাট\nইংরেজি বর্ণমালার বিপরীত বর্ণের মান মনে রাখার নিয়ম (A=Z, B=Y, C=X ইত্যাদি) এবং নম্বর কোডিং ট্রিকস।",
                    "অধ্যায় ২: দিকনির্ণয় পরীক্ষা (Direction Sense Test)\nউত্তর, দক্ষিণ, পূর্ব, পশ্চিম এবং কোণ সম্পর্কিত সমস্যা সহজে পাইথাগোরাসের সূত্র দিয়ে নির্ণয়।"
                )
            ),
            PdfBook(
                id = "pdf_math_01",
                title = "পাটিগণিত ও বীজগণিত শর্টকাট শর্ট-ট্রিকস সংকলন",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "অঙ্ক",
                totalPages = 210,
                fileSize = "12.1 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "চাকরির পরীক্ষার জন্য উপযোগী অনুপাত, শতকরা, সময় ও কার্য, নল ও চৌবাচ্চার শর্টকাট সূত্রাবলী।",
                samplePages = listOf(
                    "অধ্যায় ১: শতকরা ও লাভ-ক্ষতির শর্টকাট নিয়ম\nসূত্র ১: যদি কোনো দ্রব্যের মূল্য x% বৃদ্ধি পায়, তবে খরচ অপরিবর্তিত রাখতে ব্যবহার কমাতে হবে = [x / (100 + x)] * 100%\nউদাহরণ: চিনির দাম ২০% বৃদ্ধি পেলে ব্যবহার কত কমাতে হবে?\nসমাধান: [২০ / ১২০] * ১০০% = ১৬.৬৬%।",
                    "অধ্যায় ২: সময় ও দূরত্ব\nট্রেন সংক্রান্ত অঙ্ক, আপেক্ষিক গতিবেগ (Relative Speed) এবং প্ল্যাটফর্ম অতিক্রমের দ্রুত সমাধান টেকনিক।"
                )
            ),
            PdfBook(
                id = "pdf_polity_01",
                title = "ভারতীয় সংবিধান ও রাষ্ট্রব্যবস্থা (Indian Polity) পূর্ণাঙ্গ সংকলন",
                author = "কম্পিটিটিভ পাঠশালা ফ্যাকাল্টি",
                category = "সংবিধান",
                totalPages = 175,
                fileSize = "10.4 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "সংবিধানের প্রস্তাবনা, মৌলিক অধিকার (ধারা ১২-৩৫), নির্দেশমূলক নীতি (DPSP), রাষ্ট্রপতি ও সুপ্রিম কোর্টের ক্ষমতা।",
                samplePages = listOf(
                    "অধ্যায় ১: ভারতীয় সংবিধানের প্রস্তাবনা ও উৎস\n১৯৪৯ সালের ২৬ নভেম্বর গণপরিষদে সংবিধান গৃহীত হয় এবং ১৯৫০ সালের ২৬ জানুয়ারি এটি কার্যকর হয়।",
                    "অধ্যায় ২: মৌলিক অধিকার ও জরুরি অবস্থা\nধারা ৩২ অনুযায়ী ৫টি রিট (হেবিয়াস কর্পাস, ম্যান্ডামাস, প্রহিবিশন, সারশিওরারি, কুও-ওয়ারেন্টো) এবং জাতীয় জরুরি অবস্থা (ধারা ৩৫২)।"
                )
            )
        )
    }
}
