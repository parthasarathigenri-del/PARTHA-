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
    val isApproved: Boolean = true,
    val activeTimeSeconds: Long = 18450L,
    val batch: String = "সফলতা ব্যাচ ২০২৬",
    val avatarUrl: String? = null,
    val scoresHistory: List<Double> = listOf(55.0, 62.0, 70.0, 78.5, 84.0)
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
        AppLanguage.ENGLISH -> questionEnglish
        AppLanguage.HINDI -> questionHindi
    }

    fun getOptions(lang: AppLanguage): List<String> = when (lang) {
        AppLanguage.BENGALI -> optionsBengali
        AppLanguage.ENGLISH -> optionsEnglish
        AppLanguage.HINDI -> optionsHindi
    }

    fun getExplanation(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> explanationBengali
        AppLanguage.ENGLISH -> explanationEnglish
        AppLanguage.HINDI -> explanationHindi
    }

    fun getSubject(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> subjectBengali
        AppLanguage.ENGLISH -> subjectEnglish
        AppLanguage.HINDI -> subjectHindi
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
        AppLanguage.ENGLISH -> titleEnglish
        AppLanguage.HINDI -> titleHindi
    }

    fun getDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.BENGALI -> descriptionBengali
        AppLanguage.ENGLISH -> descriptionEnglish
        AppLanguage.HINDI -> descriptionHindi
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
