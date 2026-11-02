package com.example.data.firebase

/**
 * ══════════════════════════════════════════════════════════════════════════════
 *                    FIREBASE CLOUD FIRESTORE & STORAGE CONFIGURATION
 * ══════════════════════════════════════════════════════════════════════════════
 * 
 * PASTE YOUR FIREBASE SPARK PLAN CONFIGURATION KEYS BELOW:
 * - Go to Firebase Console -> Project Settings -> General -> Your apps -> Web/Android apps
 * - Copy and paste your project details into the placeholder slots below.
 * 
 * NOTE: The app includes full offline local persistence fallback.
 * If these keys are left as default placeholders, the application runs
 * seamlessly in robust local-memory/SharedPreferences mode without any crashes!
 */
object FirebaseConfig {

    // [SLOT 1] Your Firebase Web / Android API Key
    const val API_KEY = "AIzaSyYOUR_FIREBASE_API_KEY_HERE"

    // [SLOT 2] Your Firebase Project ID (e.g., "competitive-pathshala-123")
    const val PROJECT_ID = "YOUR_FIREBASE_PROJECT_ID_HERE"

    // [SLOT 3] Your Firebase Application ID (e.g., "1:1234567890:android:abcde12345")
    const val APP_ID = "1:000000000000:android:00000000000000"

    // [SLOT 4] Your Cloud Storage Bucket (e.g., "your-project.appspot.com")
    const val STORAGE_BUCKET = "YOUR_PROJECT_ID.appspot.com"

    // [SLOT 5] Realtime Database / Firestore endpoint URL
    const val DATABASE_URL = "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com"

    /**
     * Checks if real Firebase credentials have been configured by the user.
     */
    val isConfigured: Boolean
        get() = !API_KEY.contains("YOUR_FIREBASE_API_KEY") &&
                !PROJECT_ID.contains("YOUR_FIREBASE_PROJECT_ID")
}
