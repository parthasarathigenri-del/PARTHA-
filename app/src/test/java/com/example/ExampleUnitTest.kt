package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.MockQuestion
import com.example.data.repository.JobPrepRepository
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testNegativeMarkingScoreCalculation() {
        val repo = JobPrepRepository()
        val mockTest = repo.mockTests.value.first()

        // Test with 3 correct and 1 wrong with 0.25 negative marking
        val userAnswers = mapOf(
            0 to mockTest.questions[0].correctOptionIndex,
            1 to mockTest.questions[1].correctOptionIndex,
            2 to mockTest.questions[2].correctOptionIndex,
            3 to (mockTest.questions[3].correctOptionIndex + 1) % 4 // wrong answer
        )

        val attempt = repo.submitTestAttempt(
            mockTest = mockTest,
            answers = userAnswers,
            timeSpentSeconds = 300,
            violationStrikes = 0
        )

        assertEquals(3, attempt.correctCount)
        assertEquals(1, attempt.incorrectCount)
        assertEquals(0.25, attempt.negativeDeducted, 0.001)
        assertEquals(2.75, attempt.score, 0.001)
    }

    @Test
    fun testTrilingualQuestionSupport() {
        val question = MockQuestion(
            id = 1,
            questionBengali = "পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ কোনটি?",
            questionEnglish = "Which is the highest peak of West Bengal?",
            questionHindi = "पश्चिम बंगाल की सबसे ऊँची चोटी कौन सी है?",
            optionsBengali = listOf("সান্দাকফু", "ফালোট", "সবরগ্রাম", "টাইগার হিল"),
            optionsEnglish = listOf("Sandakphu", "Phalut", "Sabargram", "Tiger Hill"),
            optionsHindi = listOf("संदकफू", "फालूट", "सबरग्राम", "टाइगर हिल"),
            correctOptionIndex = 0,
            explanationBengali = "সান্দাকফু (৩৬৩৬ মিটার)",
            explanationEnglish = "Sandakphu (3636 m)",
            explanationHindi = "संदकफू (3636 मीटर)",
            subjectBengali = "ভূগোল",
            subjectEnglish = "Geography",
            subjectHindi = "भूगोल"
        )

        assertEquals("পশ্চিমবঙ্গের সর্বোচ্চ শৃঙ্গ কোনটি?", question.getQuestionText(AppLanguage.BENGALI))
        assertEquals("Which is the highest peak of West Bengal?", question.getQuestionText(AppLanguage.ENGLISH))
        assertEquals("पश्चिम बंगाल की सबसे ऊँची चोटी कौन सी है?", question.getQuestionText(AppLanguage.HINDI))

        assertEquals("সান্দাকফু", question.getOptions(AppLanguage.BENGALI)[0])
        assertEquals("Sandakphu", question.getOptions(AppLanguage.ENGLISH)[0])
        assertEquals("संदकफू", question.getOptions(AppLanguage.HINDI)[0])
    }

    @Test
    fun testStudentAccessApprovalToggle() {
        val repo = JobPrepRepository()
        val student = repo.currentStudent.value

        assertTrue(student.isApproved)

        // Admin revokes access
        repo.setStudentApproval(student.id, false)
        assertFalse(repo.currentStudent.value.isApproved)

        // Admin restores access
        repo.setStudentApproval(student.id, true)
        assertTrue(repo.currentStudent.value.isApproved)
    }
}
