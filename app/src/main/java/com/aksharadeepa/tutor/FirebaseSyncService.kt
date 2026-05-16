package com.aksharadeepa.tutor

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirebaseSyncService(private val context: Context) {
    private fun firestore(): FirebaseFirestore? {
        val app = runCatching { FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance() }.getOrNull()
        if (app == null) {
            Log.i(TAG, "Firebase is not configured yet. Add app/google-services.json to enable cloud sync.")
            return null
        }
        return runCatching { FirebaseFirestore.getInstance(app) }.getOrNull()
    }

    fun syncUser(user: User) {
        val db = firestore() ?: return
        val payload = mapOf(
            "fullName" to user.fullName,
            "phoneNumber" to user.phone,
            "gender" to user.gender,
            "profileImageUrl" to "",
            "source" to "android-local",
            "updatedAt" to System.currentTimeMillis(),
        )
        db.collection("users")
            .document(user.id)
            .set(payload, SetOptions.merge())
            .addOnFailureListener { Log.w(TAG, "User sync failed", it) }
    }

    fun syncChapterProgress(userId: String, subject: SubjectInfo, chapter: Chapter, bestScore: Int) {
        val db = firestore() ?: return
        val payload = mapOf(
            "subjectKey" to subject.storageKey,
            "subjectName" to subject.name,
            "chapterId" to chapter.id,
            "chapterName" to chapter.name,
            "bestScore" to bestScore,
            "completed" to true,
            "updatedAt" to System.currentTimeMillis(),
        )
        db.collection("users")
            .document(userId)
            .collection("completedChapters")
            .document("${subject.storageKey}_${chapter.id}")
            .set(payload, SetOptions.merge())
            .addOnFailureListener { Log.w(TAG, "Progress sync failed", it) }
    }

    fun syncQuizResult(userId: String, result: QuizResult) {
        val db = firestore() ?: return
        val payload = mapOf(
            "subject" to result.subject,
            "chapterId" to result.chapterId,
            "chapterName" to result.chapterName,
            "score" to result.score,
            "correct" to result.correct,
            "wrong" to result.wrong,
            "skipped" to result.skipped,
            "total" to result.total,
            "timeTakenMs" to result.timeTakenMs,
            "timestamp" to result.timestamp,
        )
        db.collection("users")
            .document(userId)
            .collection("quizResults")
            .document("${result.chapterId}_${result.timestamp}")
            .set(payload)
            .addOnFailureListener { Log.w(TAG, "Quiz result sync failed", it) }
    }

    companion object {
        private const val TAG = "FirebaseSyncService"
    }
}
