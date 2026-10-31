package com.example.util

import com.example.data.model.AppLanguage

object Strings {
    fun appTitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "COMPETITIVE PATHSHALA"
        AppLanguage.ENGLISH -> "COMPETITIVE PATHSHALA"
        AppLanguage.HINDI -> "COMPETITIVE PATHSHALA"
    }

    fun instituteSubtitle(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "ভদ্রেশ্বর • চাকরির প্রস্তুতি প্রতিষ্ঠান"
        AppLanguage.ENGLISH -> "Bhadreswar • Since-2021"
        AppLanguage.HINDI -> "भद्रेश्वर • तैयारी संस्थान"
    }

    fun navHome(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "হোম"
        AppLanguage.ENGLISH -> "Home"
        AppLanguage.HINDI -> "होम"
    }

    fun navMockTests(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "মক টেস্ট"
        AppLanguage.ENGLISH -> "Mock Tests"
        AppLanguage.HINDI -> "मॉक टेस्ट"
    }

    fun navLibrary(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "লাইব্রেরি"
        AppLanguage.ENGLISH -> "Library"
        AppLanguage.HINDI -> "लाइब्रेरी"
    }

    fun navLiveClass(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "লাইভ ক্লাস"
        AppLanguage.ENGLISH -> "Live Class"
        AppLanguage.HINDI -> "लाइव क्लास"
    }

    fun navAnalytics(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "অ্যানালাইটিক্স"
        AppLanguage.ENGLISH -> "Analytics"
        AppLanguage.HINDI -> "एनालिटिक्स"
    }

    fun startExam(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "পরীক্ষা শুরু করুন"
        AppLanguage.ENGLISH -> "Start Exam"
        AppLanguage.HINDI -> "परीक्षा शुरू करें"
    }

    fun submitExam(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "সাবমিট করুন"
        AppLanguage.ENGLISH -> "Submit Exam"
        AppLanguage.HINDI -> "सबमिट करें"
    }

    fun reviewAnswers(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "উত্তর ও ব্যাখ্যা পর্যালোচনা (Review)"
        AppLanguage.ENGLISH -> "Review Answers & Solutions"
        AppLanguage.HINDI -> "उत्तर व व्याख्या समीक्षा (Review)"
    }

    fun explainShortcut(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "💡 এক্সপ্লেন করুন (Explain)"
        AppLanguage.ENGLISH -> "💡 Explain Solution"
        AppLanguage.HINDI -> "💡 समझाएं (Explain)"
    }

    fun chatbotAssistant(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "চ্যাটবোর্ড ডাউট সহকারী"
        AppLanguage.ENGLISH -> "Chat Assistant Doubt Solver"
        AppLanguage.HINDI -> "चैटबॉट संदेह निवारक"
    }

    fun negativeMarkNotice(rate: Double, lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "নেগেটিভ মার্কিং: -$rate প্রতি ভুলের জন্য"
        AppLanguage.ENGLISH -> "Negative Marking: -$rate per wrong answer"
        AppLanguage.HINDI -> "नेगेटिव मार्किंग: -$rate प्रत्येक गलत उत्तर पर"
    }

    fun lockdownActive(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "লকডাউন পরীক্ষা মোড সক্রিয় (কপি ও উইন্ডো বদল নিষিদ্ধ)"
        AppLanguage.ENGLISH -> "Lockdown Exam Mode Active (Copy & Tab Switch Blocked)"
        AppLanguage.HINDI -> "लॉकडाउन परीक्षा मोड सक्रिय (कॉपी व विंडो बदलना प्रतिबंधित)"
    }

    fun reviewModeAllowed(lang: AppLanguage) = when (lang) {
        AppLanguage.BENGALI -> "রিভিউ মোড: ব্যাকগ্রাউন্ড রেফারেন্স উইন্ডো ব্যবহারের অনুমতি সক্রিয়"
        AppLanguage.ENGLISH -> "Review Mode: Background reference window permission active"
        AppLanguage.HINDI -> "रिव्यू मोड: बैकग्राउंड रेफरेंस विंडो उपयोग की अनुमति सक्रिय"
    }
}
