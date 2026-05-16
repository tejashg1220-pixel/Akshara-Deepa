package com.aksharadeepa.tutor

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthService(private val context: Context) {
    private fun auth(): FirebaseAuth? {
        val app = runCatching { FirebaseApp.initializeApp(context) ?: FirebaseApp.getInstance() }.getOrNull()
        return app?.let { runCatching { FirebaseAuth.getInstance(it) }.getOrNull() }
    }

    fun signIn(phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val firebaseAuth = auth()
        if (firebaseAuth == null) {
            onResult(false, "Firebase is not configured")
            return
        }
        firebaseAuth.signInWithEmailAndPassword(authEmail(phone), password)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage ?: "Login failed") }
    }

    fun createAccount(phone: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val firebaseAuth = auth()
        if (firebaseAuth == null) {
            onResult(false, "Firebase is not configured")
            return
        }
        firebaseAuth.createUserWithEmailAndPassword(authEmail(phone), password)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.localizedMessage ?: "Account creation failed") }
    }

    fun signOut() {
        auth()?.signOut()
    }

    private fun authEmail(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return "$digits@aksharadeepa.local"
    }
}