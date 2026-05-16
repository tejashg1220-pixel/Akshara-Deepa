package com.aksharadeepa.tutor

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class MainActivity : Activity() {
    private lateinit var store: PreferenceStore
    private lateinit var firebaseAuth: FirebaseAuthService
    private var screen = "splash"
    private var quizTimer: CountDownTimer? = null
    private var quizRemainingMs = QUIZ_DURATION_MS
    private var quizStartMs = 0L
    private var quizSubject: SubjectInfo? = null
    private var quizChapter: Chapter? = null
    private var quizQuestions: List<Question> = emptyList()
    private var quizAnswers: MutableList<Int?> = mutableListOf()
    private var quizIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = PreferenceStore(this)
        firebaseAuth = FirebaseAuthService(applicationContext)
        showSplash()
    }

    override fun onDestroy() {
        quizTimer?.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        when (screen) {
            "dashboard" -> confirmExit()
            "login" -> confirmExit()
            "signup" -> showLogin()
            "syllabus" -> showDashboard()
            "quiz" -> confirmLeaveQuiz()
            "result" -> showDashboard()
            "review" -> showDashboard()
            "strength" -> showDashboard()
            else -> super.onBackPressed()
        }
    }

    private fun showSplash() {
        screen = "splash"
        window.statusBarColor = Palette.PRIMARY
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = gradient(Palette.PRIMARY, Palette.ACCENT, 0f)
        }
        val spacer = View(this)
        root.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))
        val logo = text("AD", 42f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            background = circle(0x33FFFFFF)
            alpha = 0f
        }
        root.addView(logo, LinearLayout.LayoutParams(dp(104), dp(104)))
        val title = text("Akshara-Deepa", 28f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        }
        root.addView(title, matchWrap())
        val subtitle = text("Your Self-Study Companion", 14f, 0xFFCFD8DC.toInt()).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, 0)
        }
        root.addView(subtitle, matchWrap())
        val progress = ProgressBar(this).apply {
            isIndeterminate = true
            setPadding(0, dp(36), 0, 0)
        }
        root.addView(progress, LinearLayout.LayoutParams(dp(64), dp(86)))
        root.addView(View(this), LinearLayout.LayoutParams(1, 0, 1f))
        root.addView(text("MindMatrix VTU Internship Program", 10f, 0xFFCFD8DC.toInt()).apply {
            gravity = Gravity.CENTER
        }, matchWrap())
        setContentView(root)
        logo.startAnimation(AlphaAnimation(0f, 1f).apply {
            duration = 1500
            fillAfter = true
        })
        Handler(Looper.getMainLooper()).postDelayed({
            if (store.isLoggedIn() && store.currentUserId() != null) showDashboard() else showLogin()
        }, 2500)
    }

    private fun showLogin() {
        cancelQuizTimer()
        screen = "login"
        window.statusBarColor = Palette.PRIMARY
        val (rememberedPhone, rememberedPassword) = store.rememberedCredentials()
        val root = authRoot("Welcome Back!", "Sign in to continue learning")
        val card = card().apply { setPadding(dp(20), dp(20), dp(20), dp(20)) }
        val phone = edit("Phone Number", InputType.TYPE_CLASS_PHONE).apply {
            setText(rememberedPhone)
        }
        val password = edit("Password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD).apply {
            setText(rememberedPassword)
        }
        val remember = CheckBox(this).apply {
            text = "Remember Me"
            textSize = 12f
            setTextColor(Palette.MUTED)
            isChecked = rememberedPhone.isNotBlank()
        }
        val signIn = primaryButton("SIGN IN")
        signIn.setOnClickListener {
            val phoneText = phone.text.toString().trim()
            val passwordText = password.text.toString()
            val error = loginValidationError(phoneText, passwordText)
            if (error != null) {
                toast(error)
                return@setOnClickListener
            }
            signIn.isEnabled = false
            firebaseAuth.signIn(phoneText, passwordText) { success, message ->
                runOnUiThread {
                    signIn.isEnabled = true
                    if (!success) {
                        toast(message ?: "Invalid phone number or password")
                        return@runOnUiThread
                    }
                    val user = store.findUserByPhone(phoneText)
                    if (user == null) {
                        toast("Profile not found on this device")
                        firebaseAuth.signOut()
                        return@runOnUiThread
                    }
                    store.setLoggedIn(user)
                    if (remember.isChecked) store.saveCredentials(phoneText, passwordText) else store.clearCredentials()
                    toast("Login successful")
                    showDashboard()
                }
            }
        }
        card.addView(label("Phone Number"))
        card.addView(phone, fieldParams())
        card.addView(label("Password"))
        card.addView(password, fieldParams())
        card.addView(remember, matchWrap())
        card.addView(signIn, topMargin(matchWrap(), 10))
        root.addView(card, sideMargins(matchWrap(), 20))
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(18), 0, dp(24))
        }
        row.addView(text("Don't have an account? ", 14f, Palette.MUTED))
        row.addView(link("Sign Up") { showSignUp() })
        root.addView(row, matchWrap())
        setContentView(scroll(root))
    }

    private fun showSignUp() {
        cancelQuizTimer()
        screen = "signup"
        window.statusBarColor = Palette.PRIMARY
        val root = authRoot("Create Account", "Join Akshara-Deepa today")
        val card = card().apply { setPadding(dp(20), dp(20), dp(20), dp(20)) }
        val name = edit("Enter your full name", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        val phone = edit("Enter 10-digit mobile number", InputType.TYPE_CLASS_PHONE)
        val genderGroup = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }
        listOf("Male", "Female", "Other").forEach { item ->
            genderGroup.addView(RadioButton(this).apply {
                text = item
                textSize = 13f
            })
        }
        val password = edit("Minimum 6 characters", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val confirm = edit("Re-enter your password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val create = primaryButton("CREATE ACCOUNT")
        create.setOnClickListener {
            val fullName = name.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val gender = genderGroup.findViewById<RadioButton>(genderGroup.checkedRadioButtonId)?.text?.toString().orEmpty()
            val passwordText = password.text.toString()
            val confirmText = confirm.text.toString()
            val error = signUpValidationError(fullName, phoneText, gender, passwordText, confirmText)
            if (error != null) {
                toast(error)
                return@setOnClickListener
            }
            create.isEnabled = false
            firebaseAuth.createAccount(phoneText, passwordText) { success, message ->
                runOnUiThread {
                    create.isEnabled = true
                    if (!success) {
                        toast(message ?: "Account creation failed")
                        return@runOnUiThread
                    }
                    if (store.findUserByPhone(phoneText) == null) {
                        store.addUser(fullName, phoneText, gender, "")
                    }
                    toast("Account created successfully")
                    showLogin()
                }
            }
        }
        card.addView(label("Full Name"))
        card.addView(name, fieldParams())
        card.addView(label("Phone Number"))
        card.addView(phone, fieldParams())
        card.addView(label("Gender"))
        card.addView(genderGroup, fieldParams())
        card.addView(label("Create Password"))
        card.addView(password, fieldParams())
        card.addView(label("Confirm Password"))
        card.addView(confirm, fieldParams())
        card.addView(create, topMargin(matchWrap(), 10))
        root.addView(card, sideMargins(matchWrap(), 20))
        val row = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(18), 0, dp(24))
        }
        row.addView(text("Already have an account? ", 14f, Palette.MUTED))
        row.addView(link("Sign In") { showLogin() })
        root.addView(row, matchWrap())
        setContentView(scroll(root))
    }

    private fun showDashboard() {
        cancelQuizTimer()
        screen = "dashboard"
        window.statusBarColor = Palette.PRIMARY
        val user = currentUserOrLogout() ?: return
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar("Akshara-Deepa", null, "Logout") { firebaseAuth.signOut(); store.logout(); showLogin() })
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val overall = overallProgress(user.id)
        val banner = card().apply {
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = gradient(Palette.PRIMARY, Palette.ACCENT, dp(16).toFloat())
        }
        banner.addView(text("Hello, ${user.fullName.firstName()}!", 20f, Color.WHITE, Typeface.BOLD))
        banner.addView(text("Continue your learning journey", 13f, 0xFFE0E0E0.toInt()))
        banner.addView(text("Overall Progress: $overall%", 18f, Color.WHITE, Typeface.BOLD).apply {
            setPadding(0, dp(12), 0, 0)
        })
        content.addView(banner, matchWrap())
        content.addView(text("Your Subjects", 18f, Palette.TEXT, Typeface.BOLD).apply {
            setPadding(0, dp(18), 0, dp(8))
        })
        SyllabusRepository.subjects.forEach { subject ->
            content.addView(subjectCard(user.id, subject), topMargin(matchWrap(), 8))
        }
        val quick = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(4))
        }
        quick.addView(actionCard("Strength Map", Palette.ACCENT) { showStrengthMap() }, LinearLayout.LayoutParams(0, dp(86), 1f))
        quick.addView(actionCard("Past Quizzes", Palette.GREEN) { showQuizHistory() }, LinearLayout.LayoutParams(0, dp(86), 1f).apply {
            leftMargin = dp(10)
        })
        content.addView(quick, matchWrap())
        root.addView(scroll(content), LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav("Home"))
        setContentView(root)
    }

    private fun subjectCard(userId: String, subject: SubjectInfo): View {
        val completedCount = store.completed(userId, subject).size
        val percent = ((completedCount.toFloat() / subject.totalChapters) * 100).roundToInt()
        val container = card().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setOnClickListener { showSyllabus(subject) }
        }
        val icon = text(subject.name.initials(), 16f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            background = circle(subject.color)
        }
        container.addView(icon, LinearLayout.LayoutParams(dp(50), dp(50)))
        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(12), 0)
        }
        middle.addView(text(subject.name, 16f, Palette.TEXT, Typeface.BOLD))
        middle.addView(text("$completedCount of ${subject.totalChapters} chapters completed", 12f, Palette.MUTED))
        middle.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = percent
        }, topMargin(LinearLayout.LayoutParams(-1, dp(10)), 8))
        container.addView(middle, LinearLayout.LayoutParams(0, -2, 1f))
        container.addView(text("$percent%\n>", 15f, subject.color, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
        })
        return container
    }

    private fun showSyllabus(subject: SubjectInfo) {
        cancelQuizTimer()
        screen = "syllabus"
        window.statusBarColor = subject.color
        val user = currentUserOrLogout() ?: return
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar(subject.name, "<", null) { showDashboard() }.apply {
            setBackgroundColor(subject.color)
        })
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(18))
        }
        val chapters = SyllabusRepository.chapters(subject)
        val completed = store.completed(user.id, subject)
        val header = card().apply {
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(subject.lightColor)
        }
        header.addView(text("${subject.name} Syllabus", 20f, Palette.TEXT, Typeface.BOLD))
        header.addView(text("${chapters.size} chapters - ${completed.size} completed", 14f, Palette.MUTED))
        content.addView(header, matchWrap())
        content.addView(text("Chapters", 16f, Palette.TEXT, Typeface.BOLD).apply {
            setPadding(0, dp(16), 0, dp(8))
        })
        chapters.forEach { chapter ->
            content.addView(chapterRow(user.id, subject, chapter), topMargin(matchWrap(), 8))
        }
        root.addView(scroll(content), LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun chapterRow(userId: String, subject: SubjectInfo, chapter: Chapter): View {
        val done = store.completed(userId, subject).contains(chapter.id.toString())
        val score = store.bestScore(userId, subject, chapter.id)
        val row = card().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }
        row.addView(text(if (done) "OK" else chapter.id.toString(), 13f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            background = circle(if (done) Palette.GREEN else Palette.MUTED)
        }, LinearLayout.LayoutParams(dp(42), dp(42)))
        val middle = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, dp(8), 0)
        }
        middle.addView(text(chapter.name, 15f, Palette.TEXT, Typeface.BOLD))
        middle.addView(text(if (done) "Completed - Best Score: $score%" else "Not attempted yet", 11f, if (done) Palette.GREEN else Palette.MUTED))
        row.addView(middle, LinearLayout.LayoutParams(0, -2, 1f))
        val start = Button(this).apply {
            text = if (done) "Retake" else "Quiz"
            textSize = 12f
            setTextColor(Color.WHITE)
            background = rounded(subject.color, dp(9).toFloat())
            setOnClickListener { startQuiz(subject, chapter) }
        }
        row.addView(start, LinearLayout.LayoutParams(dp(92), dp(44)))
        return row
    }

    private fun startQuiz(subject: SubjectInfo, chapter: Chapter) {
        screen = "quiz"
        quizSubject = subject
        quizChapter = chapter
        quizQuestions = store.questionsFor(chapter).shuffled()
        quizAnswers = MutableList(quizQuestions.size) { null }
        quizIndex = 0
        quizRemainingMs = QUIZ_DURATION_MS
        quizStartMs = System.currentTimeMillis()
        startTimer()
        renderQuizQuestion()
    }

    private fun startTimer() {
        quizTimer?.cancel()
        quizTimer = object : CountDownTimer(quizRemainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                quizRemainingMs = millisUntilFinished
                findViewById<TextView?>(TIMER_ID)?.text = timeLabel(millisUntilFinished)
            }

            override fun onFinish() {
                quizRemainingMs = 0L
                submitQuiz()
            }
        }.start()
    }

    private fun renderQuizQuestion() {
        val subject = quizSubject ?: return
        val chapter = quizChapter ?: return
        val question = quizQuestions[quizIndex]
        window.statusBarColor = subject.color
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar("Quiz", "<", null) { confirmLeaveQuiz() }.apply {
            setBackgroundColor(subject.color)
        })
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val meta = card().apply {
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        meta.addView(text(chapter.name, 16f, Palette.TEXT, Typeface.BOLD))
        meta.addView(text("${subject.name} - Question ${quizIndex + 1} of ${quizQuestions.size}", 13f, Palette.MUTED))
        meta.addView(text(timeLabel(quizRemainingMs), 18f, subject.color, Typeface.BOLD).apply {
            id = TIMER_ID
            setPadding(0, dp(8), 0, 0)
        })
        content.addView(meta, matchWrap())
        val qCard = card().apply {
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        qCard.addView(text(question.prompt, 18f, Palette.TEXT, Typeface.BOLD))
        val group = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        question.options.forEachIndexed { index, option ->
            group.addView(RadioButton(this).apply {
                text = option
                textSize = 15f
                setTextColor(Palette.TEXT)
                id = 1000 + index
                isChecked = quizAnswers[quizIndex] == index
                setPadding(0, dp(8), 0, dp(8))
            })
        }
        group.setOnCheckedChangeListener { _, checkedId ->
            quizAnswers[quizIndex] = checkedId - 1000
        }
        qCard.addView(group, matchWrap())
        content.addView(qCard, topMargin(matchWrap(), 12))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        }
        nav.addView(secondaryButton("Previous").apply {
            isEnabled = quizIndex > 0
            setOnClickListener {
                quizIndex--
                renderQuizQuestion()
            }
        }, LinearLayout.LayoutParams(0, dp(50), 1f))
        nav.addView(primaryButton(if (quizIndex == quizQuestions.lastIndex) "Submit" else "Next").apply {
            setOnClickListener {
                if (quizIndex == quizQuestions.lastIndex) confirmSubmit() else {
                    quizIndex++
                    renderQuizQuestion()
                }
            }
        }, LinearLayout.LayoutParams(0, dp(50), 1f).apply {
            leftMargin = dp(10)
        })
        content.addView(nav, matchWrap())
        root.addView(scroll(content), LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun confirmSubmit() {
        val answered = quizAnswers.count { it != null }
        AlertDialog.Builder(this)
            .setTitle("Submit quiz?")
            .setMessage("Answered $answered of ${quizQuestions.size} questions.")
            .setPositiveButton("Submit") { _, _ -> submitQuiz() }
            .setNegativeButton("Review", null)
            .show()
    }

    private fun submitQuiz() {
        quizTimer?.cancel()
        val subject = quizSubject ?: return
        val chapter = quizChapter ?: return
        val total = quizQuestions.size
        val correct = quizQuestions.indices.count { quizAnswers[it] == quizQuestions[it].correctIndex }
        val skipped = quizAnswers.count { it == null }
        val wrong = total - correct - skipped
        val score = ((correct.toFloat() / total) * 100).roundToInt()
        val user = currentUserOrLogout() ?: return
        val result = QuizResult(
            subject = subject.name,
            chapterId = chapter.id,
            chapterName = chapter.name,
            score = score,
            correct = correct,
            wrong = wrong,
            skipped = skipped,
            total = total,
            timeTakenMs = QUIZ_DURATION_MS - quizRemainingMs,
            timestamp = System.currentTimeMillis()
        )
        store.markCompleted(user.id, subject, chapter, score)
        store.addQuizResult(user.id, result)
        showResult(result)
    }

    private fun showResult(result: QuizResult) {
        cancelQuizTimer()
        screen = "result"
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar("Quiz Result", null, null) {})
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }
        val scoreCircle = text("${result.score}%", 42f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            background = circle(if (result.score >= 60) Palette.GREEN else Palette.RED)
        }
        content.addView(scoreCircle, LinearLayout.LayoutParams(dp(150), dp(150)))
        content.addView(text(result.chapterName, 20f, Palette.TEXT, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, dp(8))
        })
        content.addView(text("Correct: ${result.correct}  Wrong: ${result.wrong}  Skipped: ${result.skipped}", 15f, Palette.MUTED))
        content.addView(primaryButton("Review Answers").apply {
            setOnClickListener { showReview() }
        }, topMargin(matchWrap(), 22))
        content.addView(secondaryButton("Back to Dashboard").apply {
            setOnClickListener { showDashboard() }
        }, topMargin(matchWrap(), 10))
        root.addView(content, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    private fun showReview() {
        cancelQuizTimer()
        screen = "review"
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar("Review Answers", "<", null) { showDashboard() })
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        quizQuestions.forEachIndexed { index, question ->
            val selected = quizAnswers.getOrNull(index)
            val ok = selected == question.correctIndex
            val item = card().apply {
                setPadding(dp(14), dp(14), dp(14), dp(14))
            }
            item.addView(text("${index + 1}. ${question.prompt}", 15f, Palette.TEXT, Typeface.BOLD))
            item.addView(text("Your answer: ${selected?.let { question.options[it] } ?: "Skipped"}", 13f, if (ok) Palette.GREEN else Palette.RED))
            item.addView(text("Correct answer: ${question.options[question.correctIndex]}", 13f, Palette.GREEN))
            item.addView(text(question.explanation, 12f, Palette.MUTED).apply {
                setPadding(0, dp(8), 0, 0)
            })
            content.addView(item, topMargin(matchWrap(), 8))
        }
        root.addView(scroll(content), LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun showStrengthMap() {
        cancelQuizTimer()
        screen = "strength"
        val user = currentUserOrLogout() ?: return
        val root = vertical(Palette.BACKGROUND)
        root.addView(appBar("Strength Map", "<", null) { showDashboard() })
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        val scores = strengthScores(user.id)
        val chartCard = card().apply {
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        chartCard.addView(StrengthMapView(this, scores), LinearLayout.LayoutParams(-1, dp(340)))
        content.addView(chartCard, matchWrap())
        content.addView(text("Gap Areas", 18f, Palette.TEXT, Typeface.BOLD).apply {
            setPadding(0, dp(18), 0, dp(8))
        })
        scores.entries.sortedBy { it.value }.take(3).forEach {
            content.addView(text("${it.key}: ${it.value.roundToInt()}%", 15f, Palette.MUTED), topMargin(matchWrap(), 6))
        }
        root.addView(scroll(content), LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav("Strength Map"))
        setContentView(root)
    }

    private fun showQuizHistory() {
        val user = currentUserOrLogout() ?: return
        val results = store.quizResults(user.id).asReversed()
        val message = if (results.isEmpty()) {
            "No quiz attempts yet."
        } else {
            results.take(8).joinToString("\n") { "${it.subject}: ${it.chapterName} - ${it.score}%" }
        }
        AlertDialog.Builder(this)
            .setTitle("Past Quizzes")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun bottomNav(active: String): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
        }
        listOf("Home", "Strength Map", "Profile").forEach { item ->
            row.addView(TextView(this).apply {
                text = item
                gravity = Gravity.CENTER
                textSize = 13f
                setTypeface(Typeface.DEFAULT, if (item == active) Typeface.BOLD else Typeface.NORMAL)
                setTextColor(if (item == active) Palette.PRIMARY else Palette.MUTED)
                setOnClickListener {
                    when (item) {
                        "Home" -> showDashboard()
                        "Strength Map" -> showStrengthMap()
                        else -> showProfile()
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(50), 1f))
        }
        return row
    }

    private fun showProfile() {
        val user = currentUserOrLogout() ?: return
        AlertDialog.Builder(this)
            .setTitle(user.fullName)
            .setMessage("Phone: ${user.phone}\nGender: ${user.gender}\n\nProgress: ${overallProgress(user.id)}%")
            .setPositiveButton("Logout") { _, _ ->
                store.logout()
                showLogin()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun strengthScores(userId: String): Map<String, Float> {
        val results = store.quizResults(userId)
        fun avg(subject: String): Float {
            val matching = results.filter { it.subject == subject }
            return if (matching.isEmpty()) 0f else matching.map { it.score }.average().toFloat()
        }
        val totalCorrect = results.sumOf { it.correct }
        val totalQuestions = results.sumOf { it.total }
        val accuracy = if (totalQuestions == 0) 0f else totalCorrect * 100f / totalQuestions
        val consistency = min(100f, results.map { dayBucket(it.timestamp) }.distinct().size * 100f / 30f)
        val speed = if (results.isEmpty()) 0f else {
            val avgSeconds = results.map { it.timeTakenMs / 1000f / it.total }.average().toFloat()
            (100f - ((avgSeconds - 30f) / 90f * 100f)).coerceIn(0f, 100f)
        }
        return linkedMapOf(
            "Science" to avg("Science"),
            "Math" to avg("Mathematics"),
            "Social" to avg("Social Studies"),
            "Accuracy" to accuracy,
            "Consistency" to consistency,
            "Speed" to speed
        )
    }

    private fun overallProgress(userId: String): Int {
        val percentages = SyllabusRepository.subjects.map {
            store.completed(userId, it).size.toFloat() / it.totalChapters * 100f
        }
        return percentages.average().roundToInt()
    }

    private fun currentUserOrLogout(): User? {
        val id = store.currentUserId()
        val user = id?.let { store.userById(it) }
        if (user == null) {
            store.logout()
            showLogin()
        }
        return user
    }

    private fun authRoot(title: String, subtitle: String): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Palette.BACKGROUND)
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(40), dp(24), dp(28))
            background = gradient(Palette.PRIMARY, Palette.PRIMARY_LIGHT, 0f)
        }
        top.addView(text("AD", 26f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            background = circle(0x26FFFFFF)
        }, LinearLayout.LayoutParams(dp(68), dp(68)))
        top.addView(text(title, 24f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, 0)
        })
        top.addView(text(subtitle, 14f, 0xFFE0E0E0.toInt()).apply {
            gravity = Gravity.CENTER
        })
        root.addView(top, LinearLayout.LayoutParams(-1, dp(220)))
        return root
    }

    private fun appBar(title: String, left: String?, right: String?, action: () -> Unit): LinearLayout {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Palette.PRIMARY)
        }
        bar.addView(TextView(this).apply {
            text = left.orEmpty()
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setOnClickListener { if (left != null) action() }
        }, LinearLayout.LayoutParams(dp(56), dp(48)))
        bar.addView(text(title, 18f, Color.WHITE, Typeface.BOLD).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        bar.addView(TextView(this).apply {
            text = right.orEmpty()
            textSize = 13f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setOnClickListener { if (right != null) action() }
        }, LinearLayout.LayoutParams(dp(72), dp(48)))
        return bar
    }

    private fun actionCard(title: String, color: Int, onClick: () -> Unit): View =
        card().apply {
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { onClick() }
            addView(text(title, 14f, color, Typeface.BOLD).apply {
                gravity = Gravity.CENTER
            })
        }

    private fun confirmExit() {
        AlertDialog.Builder(this)
            .setTitle("Exit app?")
            .setMessage("Are you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun confirmLeaveQuiz() {
        AlertDialog.Builder(this)
            .setTitle("Leave quiz?")
            .setMessage("Your current quiz attempt will not be saved.")
            .setPositiveButton("Leave") { _, _ ->
                cancelQuizTimer()
                quizSubject?.let { showSyllabus(it) } ?: showDashboard()
            }
            .setNegativeButton("Continue", null)
            .show()
    }

    private fun loginValidationError(phone: String, password: String): String? = when {
        phone.isBlank() -> "Please enter phone number"
        !phone.matches(Regex("[6-9][0-9]{9}")) -> "Please enter a valid 10-digit phone number"
        password.isBlank() -> "Please enter password"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }

    private fun signUpValidationError(name: String, phone: String, gender: String, password: String, confirm: String): String? =
        when {
            !name.matches(Regex("[A-Za-z ]{2,}")) -> "Name is required"
            !phone.matches(Regex("[6-9][0-9]{9}")) -> "Please enter a valid 10-digit phone number"
            gender.isBlank() -> "Please select your gender"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirm -> "Passwords do not match"
            else -> null
        }

    private fun cancelQuizTimer() {
        quizTimer?.cancel()
        quizTimer = null
    }

    private fun scroll(child: View): ScrollView = ScrollView(this).apply { addView(child) }
    private fun vertical(color: Int): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(color)
    }

    private fun text(value: String, size: Float, color: Int, style: Int = Typeface.NORMAL): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            setTypeface(Typeface.DEFAULT, style)
            includeFontPadding = true
        }

    private fun label(value: String): TextView = text(value, 12f, Palette.MUTED, Typeface.BOLD).apply {
        setPadding(0, dp(8), 0, dp(4))
    }

    private fun link(value: String, onClick: () -> Unit): TextView = text(value, 14f, Palette.PRIMARY, Typeface.BOLD).apply {
        setOnClickListener { onClick() }
    }

    private fun edit(hintText: String, input: Int): EditText = EditText(this).apply {
        hint = hintText
        inputType = input
        textSize = 15f
        setSingleLine(true)
        background = stroke(Palette.DIVIDER, dp(12).toFloat(), Color.WHITE)
        setPadding(dp(14), 0, dp(14), 0)
    }

    private fun primaryButton(value: String): Button = Button(this).apply {
        text = value
        textSize = 14f
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        setTextColor(Color.WHITE)
        background = rounded(Palette.PRIMARY, dp(12).toFloat())
    }

    private fun secondaryButton(value: String): Button = Button(this).apply {
        text = value
        textSize = 14f
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        setTextColor(Palette.PRIMARY)
        background = stroke(Palette.PRIMARY, dp(12).toFloat(), Color.WHITE)
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(Palette.CARD, dp(12).toFloat())
        elevation = dp(3).toFloat()
    }

    private fun rounded(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius
        }

    private fun stroke(strokeColor: Int, radius: Float, fill: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radius
            setStroke(dp(1), strokeColor)
        }

    private fun gradient(start: Int, end: Int, radius: Float): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = radius
        }

    private fun circle(color: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(color)
        }

    private fun fieldParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(8) }

    private fun matchWrap(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(-1, -2)

    private fun sideMargins(params: LinearLayout.LayoutParams, margin: Int): LinearLayout.LayoutParams =
        params.apply {
            leftMargin = dp(margin)
            rightMargin = dp(margin)
        }

    private fun topMargin(params: LinearLayout.LayoutParams, margin: Int): LinearLayout.LayoutParams =
        params.apply { topMargin = dp(margin) }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun timeLabel(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun String.firstName(): String = trim().split(" ").firstOrNull().orEmpty().ifBlank { "Student" }

    private fun String.initials(): String =
        split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    private fun dayBucket(timestamp: Long): Long = timestamp / (24L * 60L * 60L * 1000L)

    companion object {
        private const val QUIZ_DURATION_MS = 15L * 60L * 1000L
        private const val TIMER_ID = 4501
    }
}

class StrengthMapView(
    context: android.content.Context,
    private val scores: Map<String, Float>
) : View(context) {
    private val axisColors = intArrayOf(
        Palette.SCIENCE,
        Palette.MATH,
        Palette.SOCIAL,
        Palette.PRIMARY,
        Palette.ACCENT,
        0xFFFFC107.toInt()
    )

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.DIVIDER
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFC7CAD1.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x552196F3
        style = Paint.Style.FILL
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.PRIMARY
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.TEXT
        textSize = 24f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.PRIMARY
        textSize = 22f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val ringLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Palette.MUTED
        textSize = 18f
        textAlign = Paint.Align.LEFT
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val dotStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val entries = scores.entries.toList()
        if (entries.isEmpty()) return

        val cx = width / 2f
        val cy = height / 2f - 6f
        val radius = min(width, height) * 0.28f
        val count = entries.size

        for (ring in 1..5) {
            val r = radius * ring / 5f
            val webPath = Path()
            entries.forEachIndexed { index, _ ->
                val angle = angleFor(index, count)
                val x = cx + r * cos(angle).toFloat()
                val y = cy + r * sin(angle).toFloat()
                if (index == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            canvas.drawPath(webPath, gridPaint)
            canvas.drawText("${ring * 20}%", cx + 8f, cy - r + 6f, ringLabelPaint)
        }

        val path = Path()
        val points = mutableListOf<Pair<Float, Float>>()
        entries.forEachIndexed { index, entry ->
            val angle = angleFor(index, count)
            val axisX = cx + radius * cos(angle).toFloat()
            val axisY = cy + radius * sin(angle).toFloat()
            canvas.drawLine(cx, cy, axisX, axisY, axisPaint)

            val valueRadius = radius * (entry.value.coerceIn(0f, 100f) / 100f)
            val x = cx + valueRadius * cos(angle).toFloat()
            val y = cy + valueRadius * sin(angle).toFloat()
            points.add(Pair(x, y))
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)

            val labelRadius = radius + 58f
            val labelX = cx + labelRadius * cos(angle).toFloat()
            val labelY = cy + labelRadius * sin(angle).toFloat() + 8f
            textPaint.color = axisColors[index % axisColors.size]
            canvas.drawText(entry.key, labelX, labelY, textPaint)

            val valueLabelRadius = valueRadius + 26f
            val valueX = cx + valueLabelRadius * cos(angle).toFloat()
            val valueY = cy + valueLabelRadius * sin(angle).toFloat() + 8f
            valuePaint.color = axisColors[index % axisColors.size]
            canvas.drawText("${entry.value.roundToInt()}%", valueX, valueY, valuePaint)
        }
        path.close()
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, linePaint)

        points.forEachIndexed { index, point ->
            dotPaint.color = axisColors[index % axisColors.size]
            canvas.drawCircle(point.first, point.second, 10f, dotPaint)
            canvas.drawCircle(point.first, point.second, 10f, dotStrokePaint)
        }

        drawLegend(canvas, entries, width - 170f, height - 124f)
    }

    private fun angleFor(index: Int, count: Int): Double = -PI / 2 + (2 * PI * index / count)

    private fun drawLegend(canvas: Canvas, entries: List<Map.Entry<String, Float>>, startX: Float, startY: Float) {
        val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Palette.MUTED
            textSize = 18f
            textAlign = Paint.Align.LEFT
        }
        entries.take(3).forEachIndexed { index, entry ->
            val y = startY + index * 28f
            dotPaint.color = axisColors[index % axisColors.size]
            canvas.drawCircle(startX, y - 6f, 7f, dotPaint)
            canvas.drawText("${entry.key} ${entry.value.roundToInt()}%", startX + 16f, y, legendTextPaint)
        }
    }
}
