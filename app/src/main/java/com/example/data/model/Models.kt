package com.example.data.model

enum class AppLanguage(val code: String, val label: String) {
    BENGALI("bn", "বাংলা"),
    ENGLISH("en", "English"),
    HINDI("hi", "हिन्दी")
}

enum class PerformanceTrend {
    IMPROVING,   // বাড়ছে
    DECLINING,   // কমছে
    STABLE       // একই আছে
}

data class Student(
    val id: String,
    val name: String,
    val rollNo: String,
    val targetExam: String,
    val phone: String,
    val isApproved: Boolean = false,
    val isAdmin: Boolean = false,
    val canAccessMockTests: Boolean = false,
    val canAccessPdfs: Boolean = false,
    val approvedTestIds: List<String> = emptyList(), // Specific test IDs approved by admin
    val completedTestIds: List<String> = emptyList(), // Tests already attempted that can be re-attempted freely
    val isTestFrozen: Boolean = false,
    val frozenTestId: String? = null,
    val freezeReason: String = "",
    val registrationDate: String = "আজ",
    val activeTimeSeconds: Long = 0L,
    val batch: String = "কম্পিটিটিভ পাঠশালা ব্যাচ",
    val avatarUrl: String? = null,
    val scoresHistory: List<Double> = emptyList()
) {
    val trend: PerformanceTrend
        get() {
            if (scoresHistory.size < 2) return PerformanceTrend.STABLE
            val first = scoresHistory.first()
            val last = scoresHistory.last()
            val diff = last - first
            return when {
                diff > 3.0 -> PerformanceTrend.IMPROVING
                diff < -3.0 -> PerformanceTrend.DECLINING
                else -> PerformanceTrend.STABLE
            }
        }

    val trendPercentChange: Double
        get() {
            if (scoresHistory.size < 2) return 0.0
            val first = scoresHistory.first()
            val last = scoresHistory.last()
            return Math.round((last - first) * 10.0) / 10.0
        }
}

data class ExamRoutine(
    val id: String,
    val title: String,
    val titleEn: String = "",
    val titleHi: String = "",
    val targetExam: String,
    val subject: String,
    val date: String,
    val timeSlot: String,
    val syllabus: String,
    val totalMarks: Int,
    val negativeMarkRate: Double,
    val durationMinutes: Int,
    val isMockReady: Boolean = true
)

data class MockQuestion(
    val id: Int,
    val questionBengali: String,
    val questionEnglish: String,
    val questionHindi: String,
    val optionsBengali: List<String>,
    val optionsEnglish: List<String>,
    val optionsHindi: List<String>,
    val correctOptionIndex: Int,
    val explanationBengali: String,
    val explanationEnglish: String,
    val explanationHindi: String,
    val subjectBengali: String,
    val subjectEnglish: String,
    val subjectHindi: String
) {
    fun getQuestionText(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> questionBengali
        AppLanguage.ENGLISH -> questionEnglish.ifBlank { questionBengali }
        AppLanguage.HINDI -> questionHindi.ifBlank { questionBengali }
    }

    fun getOptions(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> optionsBengali
        AppLanguage.ENGLISH -> if (optionsEnglish.isNotEmpty() && optionsEnglish.any { it.isNotBlank() }) optionsEnglish else optionsBengali
        AppLanguage.HINDI -> if (optionsHindi.isNotEmpty() && optionsHindi.any { it.isNotBlank() }) optionsHindi else optionsBengali
    }

    fun getExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> explanationBengali
        AppLanguage.ENGLISH -> explanationEnglish.ifBlank { explanationBengali }
        AppLanguage.HINDI -> explanationHindi.ifBlank { explanationBengali }
    }

    fun getSubject(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> subjectBengali
        AppLanguage.ENGLISH -> subjectEnglish.ifBlank { subjectBengali }
        AppLanguage.HINDI -> subjectHindi.ifBlank { subjectBengali }
    }
}

data class MockTest(
    val id: String,
    val titleBengali: String,
    val titleEnglish: String,
    val titleHindi: String,
    val descriptionBengali: String,
    val descriptionEnglish: String,
    val descriptionHindi: String,
    val category: String,
    val durationMinutes: Int,
    val totalMarks: Int,
    val markPerCorrect: Double = 1.0,
    val negativeMarkPerWrong: Double = 0.25,
    val isLockedByAdmin: Boolean = false,
    val questions: List<MockQuestion>
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> titleBengali
        AppLanguage.ENGLISH -> titleEnglish.ifBlank { titleBengali }
        AppLanguage.HINDI -> titleHindi.ifBlank { titleBengali }
    }

    fun getDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> descriptionBengali
        AppLanguage.ENGLISH -> descriptionEnglish.ifBlank { descriptionBengali }
        AppLanguage.HINDI -> descriptionHindi.ifBlank { descriptionBengali }
    }
}

data class TestAttempt(
    val attemptId: String,
    val mockTestId: String,
    val mockTestTitle: String,
    val studentId: String,
    val studentName: String,
    val attemptNumber: Int,
    val userAnswers: Map<Int, Int>,
    val score: Double,
    val correctCount: Int,
    val incorrectCount: Int,
    val unattemptedCount: Int,
    val negativeDeducted: Double,
    val timeSpentSeconds: Int,
    val questionTimeSeconds: Map<Int, Int> = emptyMap(),
    val violationStrikes: Int,
    val completedAt: Long = System.currentTimeMillis()
)

data class PdfBook(
    val id: String,
    val title: String,
    val author: String,
    val category: String,
    val totalPages: Int,
    val fileSize: String,
    val isScheduledExamPdf: Boolean = false,
    val isLocked: Boolean = false,
    val scheduledUnlockTime: String = "",
    val description: String = "",
    val fileUri: String? = null,
    val samplePages: List<String> = emptyList()
)

data class LiveClass(
    val id: String,
    val title: String,
    val subject: String,
    val facultyName: String,
    val facultyDesignation: String,
    val scheduledTime: String,
    val isLiveNow: Boolean,
    val liveStudentCount: Int = 0,
    val summary: String
)

data class FeePayment(
    val id: String,
    val title: String,
    val amount: Double,
    val billingMonth: String,
    val dueDate: String,
    val isPaid: Boolean,
    val transactionId: String? = null,
    val paymentDate: String? = null,
    val paymentMethod: String? = null
)

data class ChatMessage(
    val id: String,
    val senderName: String,
    val isTeacher: Boolean,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentName: String? = null,
    val attachmentType: String? = null
)

data class InstituteNotice(
    val id: String,
    val title: String,
    val dateString: String,
    val category: String,
    val content: String,
    val isUrgent: Boolean = false,
    val examTargetDateTimestamp: Long? = null
)

data class TestAccessRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentPhone: String,
    val mockTestId: String,
    val mockTestTitle: String,
    val requestedAt: String = "আজ",
    val isApproved: Boolean = false
)

data class TestFreezeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentPhone: String,
    val mockTestId: String,
    val mockTestTitle: String,
    val strikeCount: Int = 3,
    val frozenAt: String = "এখনই",
    val isUnfrozen: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val studentName: String,
    val rollNo: String,
    val targetExam: String,
    val score: Double,
    val totalMarks: Int,
    val accuracyPercent: Double,
    val timeSpentSeconds: Int,
    val mockTestTitle: String
)

