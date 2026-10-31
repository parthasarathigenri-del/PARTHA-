package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JobPrepRepository {

    // Language state (Bengali, English, Hindi)
    private val _appLanguage = MutableStateFlow(AppLanguage.BENGALI)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Current user state (Can toggle between Student and Admin for demonstration)
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    private val _currentStudent = MutableStateFlow(
        Student(
            id = "std_101",
            name = "সুমন ব্যানার্জী",
            rollNo = "SA-2026-1042",
            targetExam = "WBCS & SSC CGL",
            phone = "+91 98301 23456",
            isApproved = true,
            activeTimeSeconds = 18450L,
            batch = "অদম্য WBCS ব্যাচ ২০২৬"
        )
    )
    val currentStudent: StateFlow<Student> = _currentStudent.asStateFlow()

    // Institute registered students list for Admin & peer visibility
    private val _allStudents = MutableStateFlow(
        listOf(
            Student("std_101", "সুমন ব্যানার্জী", "SA-1042", "WBCS & SSC", "+91 98301 23456", isApproved = true, activeTimeSeconds = 18450L, scoresHistory = listOf(58.0, 64.0, 72.0, 79.0, 85.0)),
            Student("std_102", "প্রিয়াঙ্কা চক্রবর্তী", "SA-1043", "Primary TET", "+91 98302 34567", isApproved = true, activeTimeSeconds = 24100L, scoresHistory = listOf(75.0, 80.0, 84.0, 88.0, 92.5)),
            Student("std_103", "রাহুল ঘোষ", "SA-1044", "WBP SI & Constable", "+91 98303 45678", isApproved = true, activeTimeSeconds = 12900L, scoresHistory = listOf(78.0, 74.0, 70.0, 65.0, 61.0)),
            Student("std_104", "স্নেহা সেনগুপ্ত", "SA-1045", "SSC CGL", "+91 98304 56789", isApproved = false, activeTimeSeconds = 3200L, scoresHistory = listOf(65.0, 64.0, 66.0, 65.5, 65.0)),
            Student("std_105", "অনিরুদ্ধ দত্ত", "SA-1046", "Railway NTPC", "+91 98305 67890", isApproved = true, activeTimeSeconds = 9800L, scoresHistory = listOf(52.0, 58.0, 63.0, 69.0, 76.0)),
            Student("std_106", "মৌসুমি রায়", "SA-1047", "WBCS Prelims", "+91 98306 78901", isApproved = false, activeTimeSeconds = 1200L, scoresHistory = listOf(62.0, 58.0, 54.0, 50.0, 46.0))
        )
    )
    val allStudents: StateFlow<List<Student>> = _allStudents.asStateFlow()

    // Exam Routine
    private val _examRoutines = MutableStateFlow(
        listOf(
            ExamRoutine(
                id = "rout_1",
                title = "WBCS প্রিলিমস গ্র্যান্ড মক টেস্ট - পর্ব ১",
                titleEn = "WBCS Prelims Grand Mock Test - Part 1",
                titleHi = "WBCS प्रीलिम्स ग्रैंड मॉक टेस्ट - भाग 1",
                targetExam = "WBCS 2026",
                subject = "ভারতের ইতিহাস, ভূগোল ও সংবিধান",
                date = "রবিবার, ৮ নভেম্বর",
                timeSlot = "সকাল ১০:০০ - দুপুর ১২:৩০",
                syllabus = "আধুনিক ভারতের ইতিহাস (১৮৫৭-১৯৪৭), পশ্চিমবঙ্গের ভূগোল, মৌলিক অধিকার ও সংসদীয় ব্যবস্থা",
                totalMarks = 100,
                negativeMarkRate = 0.25,
                durationMinutes = 60
            ),
            ExamRoutine(
                id = "rout_2",
                title = "পশ্চিমবঙ্গ পুলিশ SI ও কনস্টেবল স্পেশাল টেস্ট",
                titleEn = "WB Police SI & Constable Special Test",
                titleHi = "पश्चिम बंगाल पुलिस SI व कांस्टेबल स्पेशल टेस्ट",
                targetExam = "WBP SI / Constable",
                subject = "পাটিগণিত ও সাধারণ জ্ঞান",
                date = "বুধবার, ১১ নভেম্বর",
                timeSlot = "সন্ধ্যা ৭:০০ - রাত ৮:০০",
                syllabus = "শতকরা, লাভ ও ক্ষতি, অনুপাত, সমসাময়িক ঘটনাবলী ও সাধারণ বিজ্ঞান",
                totalMarks = 50,
                negativeMarkRate = 0.25,
                durationMinutes = 45
            ),
            ExamRoutine(
                id = "rout_3",
                title = "SSC CGL টিয়ার-১ রিজনিং ও ইংলিশ কমপ্রিহেনশন",
                titleEn = "SSC CGL Tier-1 Reasoning & English",
                titleHi = "SSC CGL टियर-1 रीजनिंग व इंग्लिश",
                targetExam = "SSC CGL 2026",
                subject = "Reasoning & English Comprehension",
                date = "শনিবার, ১৪ নভেম্বর",
                timeSlot = "দুপুর ৩:০০ - বিকেল ৪:০০",
                syllabus = "Syllogism, Blood Relations, Idioms & Phrases, Spotting Errors",
                totalMarks = 50,
                negativeMarkRate = 0.50,
                durationMinutes = 40
            ),
            ExamRoutine(
                id = "rout_4",
                title = "প্রাইমারি টেট (Primary TET) শিশু মনস্তত্ত্ব টেস্ট",
                titleEn = "Primary TET Child Pedagogy Test",
                titleHi = "प्राइमरी टीईटी बाल मनोविज्ञान टेस्ट",
                targetExam = "WB Primary TET",
                subject = "Child Development & Bengali Pedagogy",
                date = "সোমবার, ১৬ নভেম্বর",
                timeSlot = "সকাল ১১:০০ - দুপুর ১২:১৫",
                syllabus = "পেডাগজি ও শিশু মনস্তত্ত্ব, বাংলা ভাষার ব্যাকরণ ও শিখনপদ্ধতি",
                totalMarks = 50,
                negativeMarkRate = 0.0,
                durationMinutes = 50
            )
        )
    )
    val examRoutines: StateFlow<List<ExamRoutine>> = _examRoutines.asStateFlow()

    // Mock Tests
    private val _mockTests = MutableStateFlow(createSampleMockTests())
    val mockTests: StateFlow<List<MockTest>> = _mockTests.asStateFlow()

    // Test Attempts History
    private val _testAttempts = MutableStateFlow<List<TestAttempt>>(createSampleAttempts())
    val testAttempts: StateFlow<List<TestAttempt>> = _testAttempts.asStateFlow()

    // Books and Scheduled PDF Tests
    private val _pdfBooks = MutableStateFlow(createSamplePdfs())
    val pdfBooks: StateFlow<List<PdfBook>> = _pdfBooks.asStateFlow()

    // Live Classes
    private val _liveClasses = MutableStateFlow(
        listOf(
            LiveClass(
                id = "live_1",
                title = "WBCS প্রিলিমিনারির জন্য আধুনিক ভারতের জাতীয় আন্দোলন (Masterclass)",
                subject = "ভারতের ইতিহাস (History)",
                facultyName = "প্রফেসর অমিতাভ চ্যাটার্জী (Ex-WBCS Officer)",
                facultyDesignation = "সিনিয়র ফ্যাকাল্টি, সংকল্প একাডেমি",
                scheduledTime = "আজ সরাসরি চলছে",
                isLiveNow = true,
                liveStudentCount = 142,
                summary = "স্বদেশী আন্দোলন, গান্ধীজীর উত্থান ও নেতাজীর আজাদ হিন্দ ফৌজের সংগ্রাম বিষয়ে গভীর আলোচনা।"
            ),
            LiveClass(
                id = "live_2",
                title = "শর্টকাট মেথডে পরিমিতি ও বীজগণিত সমাধান",
                subject = "কোয়ান্টিটেটিভ অ্যাপটিটিউড (Math)",
                facultyName = "সুজিত কুমার বিশ্বাস (M.Sc, B.Ed)",
                facultyDesignation = "ম্যাথমেটিক্স স্পেশালিস্ট",
                scheduledTime = "আজ সন্ধ্যা ৭:০০ টা",
                isLiveNow = false,
                liveStudentCount = 0,
                summary = "চক্রবৃদ্ধি সুদ, অংশীদারি কারবার ও জ্যামিতির জটিল সূত্রের সহজ টেকনিক।"
            ),
            LiveClass(
                id = "live_3",
                title = "West Bengal Current Affairs & Government Schemes 2026",
                subject = "সাম্প্রতিক ঘটনাপ্রবাহ",
                facultyName = "অনন্যা মুখার্জী",
                facultyDesignation = "কান্ট্রি অ্যান্ড স্টেট জিকে রিসার্চার",
                scheduledTime = "আগামীকাল সকাল ৯:০০ টা",
                isLiveNow = false,
                liveStudentCount = 0,
                summary = "কন্যাশ্রী, লক্ষ্মীর ভাণ্ডার, কৃষক বন্ধু ও পশ্চিমবঙ্গের বাজেট সংক্রান্ত প্রশ্নাবলী।"
            )
        )
    )
    val liveClasses: StateFlow<List<LiveClass>> = _liveClasses.asStateFlow()

    // Institute Notices
    private val _notices = MutableStateFlow(
        listOf(
            InstituteNotice(
                id = "not_1",
                title = "🚨 WBCS 2026 প্রিলিমিনারি পরীক্ষার অফিশিয়াল দিন ধার্য",
                dateString = "৩১ অক্টোবর, ২০২৬",
                category = "পরীক্ষার বিজ্ঞপ্তি",
                content = "পশ্চিমবঙ্গ পাবলিক সার্ভিস কমিশন (WBPSC) কর্তৃক WBCS প্রিলিমিনারি পরীক্ষা আগামী ২০ ডিসেম্বর ২০২৬ অনুষ্ঠিত হবে। সকল শিক্ষার্থীকে নির্ধারিত মক টেস্টে নিয়মিত অংশগ্রহণের অনুরোধ জানানো হচ্ছে।",
                isUrgent = true,
                examTargetDateTimestamp = System.currentTimeMillis() + (50L * 24 * 3600 * 1000)
            ),
            InstituteNotice(
                id = "not_2",
                title = "🔒 মক টেস্টে নতুন চিটিং প্রতিরোধক লকডাউন সিকিউরিটি সক্রিয়",
                dateString = "২৯ অক্টোবর, ২০২৬",
                category = "পরীক্ষা গাইডলাইন",
                content = "অনলাইন মক টেস্ট চলাকালীন অ্যাপ ছেড়ে অন্য কোনো ব্রাউজার বা ট্যাবে যাওয়া কঠোরভাবে নিষিদ্ধ। পরপর ৩ বার উইন্ডো বদল করলে টেস্ট স্বয়ংক্রিয়ভাবে বাতিল ও সাবমিট হবে। তবে পরীক্ষা শেষে রিভিউ মোডে ব্যাকগ্রাউন্ড রেফারেন্স দেখার অনুমতি রয়েছে।",
                isUrgent = true
            ),
            InstituteNotice(
                id = "not_3",
                title = "📚 নভেম্বর মাসের মাসিক ফি জমার শেষ তারিখ ১৫ নভেম্বর",
                dateString = "২৮ অক্টোবর, ২০২৬",
                category = "ফি বিজ্ঞপ্তি",
                content = "সকল ছাত্রছাত্রীদের অনুরোধ করা হচ্ছে অ্যাপের 'অনলাইন ফি পেমেন্ট' সেকশন থেকে UPI বা কার্ডের মাধ্যমে মাসিক ফি জমা দিয়ে ডিজিটাল রসিদ সংগ্রহ করুন।"
            ),
            InstituteNotice(
                id = "not_4",
                title = "📖 নতুন পরিবেশ বিদ্যা ও বিজ্ঞান ই-বুক লাইব্রেরিতে উন্মুক্ত",
                dateString = "২৬ অক্টোবর, ২০২৬",
                category = "লাইব্রেরি আপডেট",
                content = "প্রাইমারি টেট ও রেলওয়ে পরীক্ষার জন্য ৫০০ পৃষ্ঠার এক্সক্লুসিভ স্টাডি ম্যাটেরিয়াল লাইব্রেরি সেকশনে যুক্ত করা হয়েছে।"
            )
        )
    )
    val notices: StateFlow<List<InstituteNotice>> = _notices.asStateFlow()

    // Fee Payments
    private val _feePayments = MutableStateFlow(
        listOf(
            FeePayment(
                id = "fee_nov",
                title = "নভেম্বর ২০২৬ - ইনস্টিটিউট টিউশন ও মক টেস্ট ফি",
                amount = 1200.0,
                billingMonth = "নভেম্বর ২০২৬",
                dueDate = "১৫ নভেম্বর, ২০২৬",
                isPaid = false
            ),
            FeePayment(
                id = "fee_oct",
                title = "অক্টোবর ২০২৬ - ইনস্টিটিউট টিউশন ও স্টাডি ম্যাটেরিয়াল ফি",
                amount = 1200.0,
                billingMonth = "অক্টোবর ২০২৬",
                dueDate = "১৫ অক্টোবর, ২০২৬",
                isPaid = true,
                transactionId = "UPI-SANKALP-99482143",
                paymentDate = "১০ অক্টোবর, ২০২৬",
                paymentMethod = "Google Pay (UPI)"
            ),
            FeePayment(
                id = "fee_sep",
                title = "সেপ্টেম্বর ২০২৬ - অ্যাডমিশন ও রেজিস্ট্রেশন ফি",
                amount = 1500.0,
                billingMonth = "সেপ্টেম্বর ২০২৬",
                dueDate = "০৫ সেপ্টেম্বর, ২০২৬",
                isPaid = true,
                transactionId = "UPI-SANKALP-88123049",
                paymentDate = "০৩ সেপ্টেম্বর, ২০২৬",
                paymentMethod = "PhonePe (UPI)"
            )
        )
    )
    val feePayments: StateFlow<List<FeePayment>> = _feePayments.asStateFlow()

    // Chat messages
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "msg_1",
                senderName = "সুমন ব্যানার্জী (শিক্ষার্থী)",
                isTeacher = false,
                messageText = "স্যার, WBCS ইতিহাস প্রশ্নে ১৯৩২ সালের পুনা চুক্তি সম্পর্কিত প্রশ্নে একটা সন্দেহ আছে।",
                timestamp = System.currentTimeMillis() - 7200000
            ),
            ChatMessage(
                id = "msg_2",
                senderName = "প্রফেসর অমিতাভ চ্যাটার্জী (শিক্ষক)",
                isTeacher = true,
                messageText = "হ্যাঁ সুমন, পুনা চুক্তি মহাত্মা গান্ধী ও ড. বি আর আম্বেদকরের মধ্যে হয়েছিল দলিতদের পৃথক নির্বাচন ব্যবস্থার পরিবর্তে যৌথ নির্বাচনের সংরক্ষিত আসন বিষয়ে। এই নোটসটি দেখে নাও।",
                attachmentName = "Poona_Pact_Summary_Notes.pdf",
                attachmentType = "PDF",
                timestamp = System.currentTimeMillis() - 5400000
            ),
            ChatMessage(
                id = "msg_3",
                senderName = "সুমন ব্যানার্জী (শিক্ষার্থী)",
                isTeacher = false,
                messageText = "অনেক ধন্যবাদ স্যার! আর আগামী রবিবারের মক টেস্টে কি আধুনিক ভারতের সব অংশ থাকবে?",
                timestamp = System.currentTimeMillis() - 3600000
            ),
            ChatMessage(
                id = "msg_4",
                senderName = "প্রফেসর অমিতাভ চ্যাটার্জী (শিক্ষক)",
                isTeacher = true,
                messageText = "হ্যাঁ, ১৮৫৭ সালের মহাবিদ্রোহ থেকে ১৯৪৭ স্বাধীনতা আইন পর্যন্ত। ভালো করে রিভিশন দাও!",
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }

    fun toggleAdminMode() {
        _isAdminMode.update { !it }
    }

    fun setStudentApproval(studentId: String, approved: Boolean) {
        _allStudents.update { list ->
            list.map { student ->
                if (student.id == studentId) student.copy(isApproved = approved) else student
            }
        }
        if (_currentStudent.value.id == studentId) {
            _currentStudent.update { it.copy(isApproved = approved) }
        }
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
            violationStrikes = violationStrikes
        )

        _testAttempts.update { listOf(attempt) + it }
        return attempt
    }

    fun sendChatMessage(text: String, attachmentName: String? = null, attachmentType: String? = null) {
        val isTeacher = _isAdminMode.value
        val name = if (isTeacher) "প্রফেসর অমিতাভ চ্যাটার্জী (শিক্ষক)" else _currentStudent.value.name
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
        val txId = "UPI-SANKALP-${(10000000..99999999).random()}"
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
            isLockedByAdmin = false,
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
                studentName = "সুমন ব্যানার্জী",
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
                title = "🔒 WBCS মক টেস্ট পেপার - ২০২৬ (পিডিএফ স্পেশাল শিডিউলড এক্সাম)",
                author = "সংকল্প একাডেমি রিসার্চ প্যানেল",
                category = "শিডিউলড পিডিএফ টেস্ট",
                totalPages = 12,
                fileSize = "2.4 MB",
                isScheduledExamPdf = true,
                isLocked = true,
                scheduledUnlockTime = "রবিবার সকাল ১০:০০ টা (নির্দিষ্ট সময়ে আনলক হবে)",
                description = "এই পিডিএফ টেস্টটি নির্দিষ্ট সময়ে স্বয়ংক্রিয়ভাবে বা শিক্ষকের নির্দেশে আনলক হবে। শিক্ষার্থীরা নির্দিষ্ট সময়ে প্রশ্ন দেখে অ্যাপের মাধ্যমে উত্তর সাবমিট করবে।",
                samplePages = listOf(
                    "সংকল্প একাডেমি - সাপ্তাহিক টেস্ট পেপার (পৃষ্ঠা ১)\nবিভাগ ক: প্রাচীন ও মধ্যযুগীয় ইতিহাস\n১. হরপ্পা সভ্যতার বৃহত্তম প্রত্নক্ষেত্র কোনটি?\n২. সম্রাট অশোকের কলিঙ্গ যুদ্ধের বিবরণ কোন শিলালিপিতে রয়েছে?\n৩. দিল্লির কোন সুলতান রক্ত ও লৌহ নীতি (Blood and Iron policy) গ্রহণ করেন?",
                    "সংকল্প একাডেমি - সাপ্তাহিক টেস্ট পেপার (পৃষ্ঠা ২)\nবিভাগ খ: ভূগোল ও পরিবেশ\n৪. তিস্তা নদীর উৎপত্তি কোন হিমবাহ থেকে?\n৫. পশ্চিমবঙ্গের সুন্দরবনকে রামসার সাইট হিসেবে কবে ঘোষণা করা হয়?"
                )
            ),
            PdfBook(
                id = "pdf_book_02",
                title = "ভারতের স্বাধীনতা সংগ্রামের সম্পূর্ণ ইতিহাস ও সংক্ষিপ্ত সূত্রাবলী",
                author = "অধ্যাপক অমিতাভ চ্যাটার্জী",
                category = "ইতিহাস (History)",
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
                id = "pdf_book_03",
                title = "পাটিগণিত ও শর্টকাট শর্ট-ট্রিকস সংকলন (বাংলা ও ইংরেজি)",
                author = "সুজিত কুমার বিশ্বাস",
                category = "গণিত (Quantitative Aptitude)",
                totalPages = 210,
                fileSize = "12.1 MB",
                isScheduledExamPdf = false,
                isLocked = false,
                description = "চাকরির পরীক্ষার জন্য উপযোগী অনুপাত, শতকরা, সময় ও কার্য, নল ও চৌবাচ্চার শর্টকাট সূত্রাবলী।",
                samplePages = listOf(
                    "অধ্যায় ৩: শতকরা ও লাভ-ক্ষতির শর্টকাট নিয়ম\nসূত্র ১: যদি কোনো দ্রব্যের মূল্য x% বৃদ্ধি পায়, তবে খরচ অপরিবর্তিত রাখতে ব্যবহার কমাতে হবে = [x / (100 + x)] * 100%\nউদাহরণ: চিনির দাম ২০% বৃদ্ধি পেলে ব্যবহার কত কমাতে হবে?\nসমাধান: [২০ / ১২০] * ১০০% = ১৬.৬৬%।"
                )
            )
        )
    }
}
