# AKSHARA-DEEPA TUTOR - COMPLETE SOP DOCUMENT

## Standard Operating Procedure

---

# SECTION 1: PROJECT OVERVIEW

---

## Project Title
**Project 89 - Android App Development using GenAI - Akshara-Deepa Tutor (Education)**

## Problem Statement
After-school learning in rural areas is often unguided. While students have textbooks, they lack a way to track their "Chapter Mastery" or identify which subjects they are consistently weak in. This leads to gaps in learning that only surface during final exams.

## Vision
Akshara-Deepa is a self-study companion for 10th-grade (SSLC) students. It turns the syllabus into a "Mission Map." The app doesn't just provide content; it tracks "Progress Velocity." Students mark chapters as they finish them and take simple "Self-Check" quizzes. The app then highlights the "Gap Areas" that need more attention.

## Tech Stack
- **Frontend:** Android (Kotlin/Java) with XML Layouts OR Jetpack Compose
- **Backend:** Firebase (Authentication + Firestore Database + Realtime Database)
- **Charts:** MPAndroidChart library (for Spider Web / Radar Chart)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)

---

# SECTION 2: COMPLETE SCREEN-BY-SCREEN SPECIFICATIONS

---

## SCREEN 1: SPLASH SCREEN

### Purpose
First screen the user sees when launching the app. Shows the app branding for 2-3 seconds, then navigates to either Login Screen (if not logged in) or Dashboard (if already logged in).

### Technical Specifications
- **Layout File:** `activity_splash.xml`
- **Activity File:** `SplashActivity.kt`
- **Logic:**
  - On `onCreate()`, start a 3-second `Handler` delay
  - Check `FirebaseAuth.getInstance().currentUser`
  - If `currentUser != null` → Navigate to `DashboardActivity`
  - If `currentUser == null` → Navigate to `LoginActivity`
  - Call `finish()` so user cannot go back to splash

### Data Required
- None (no database interaction)

### Navigation
- **Next Screen (if logged in):** Dashboard Screen
- **Next Screen (if not logged in):** Login Screen

---

## SCREEN 2: LOGIN SCREEN

### Purpose
Entry point for existing users. Provides options to sign in with phone number and password, or navigate to sign up for new users.

### Technical Specifications
- **Layout File:** `activity_login.xml`
- **Activity File:** `LoginActivity.kt`
- **Input Validation:**
  - Phone number must be exactly 10 digits
  - Phone number must start with 6, 7, 8, or 9
  - Password must not be empty
  - Password minimum 6 characters
- **Authentication Logic:**
  - Query Firestore collection `users` where `phoneNumber == inputPhone`
  - Compare stored password with input password (use hashing in production, plain text for prototype)
  - On success: Save login state in `SharedPreferences` (`isLoggedIn = true`, `userId = documentId`)
  - On failure: Show error Snackbar
- **Remember Me Logic:**
  - If checkbox is checked, save phone number and password in `SharedPreferences`
  - On next app launch, pre-fill the fields if data exists
  - Similar to Google's password save functionality

### Data Model (Firestore Read)
**Collection:** `users`
- **Document ID:** auto-generated
- **Fields being read:**
  - `phoneNumber`: String
  - `password`: String

### Navigation
- **On "Sign In" button click (success):** → Dashboard Screen
- **On "Sign Up" text click:** → Sign Up Screen
- **Back button:** Exit app (no previous screen)

---

## SCREEN 3: SIGN UP / REGISTRATION SCREEN

### Purpose
New user registration. Collects name, phone number, gender, and password. Stores all information in Firebase Firestore database.

### Technical Specifications
- **Layout File:** `activity_signup.xml`
- **Activity File:** `SignUpActivity.kt`
- **Input Validation:**
  - Full name must be at least 3 characters, no numbers
  - Phone number must be exactly 10 digits starting with 6, 7, 8, or 9
  - Gender must be selected
  - Password must be minimum 6 characters
  - Password and confirm password must match
- **Duplicate Check:**
  - Before saving, query Firestore to check if `phoneNumber` already exists
  - If exists: Show error toast "Phone number already registered"
  - If not exists: Proceed to save
- **Save Logic:**
  - Create new document in `users` collection
  - Generate auto `userId`
  - Save: name, phoneNumber, gender, password, createdAt timestamp
  - On success: Show success message and navigate to Login Screen
  - On failure: Show error message

### Data Model (Firestore Write)
**Collection:** `users`
- **New Document Fields:**
  - `name`: String
  - `phoneNumber`: String
  - `gender`: String (Male, Female, Other)
  - `password`: String
  - `createdAt`: Timestamp
  - `completedChapters`: Array (initially empty)
  - `quizResults`: Array (initially empty)

### Navigation
- **On "Sign Up" button click (success):** → Login Screen
- **On back arrow click:** → Login Screen

---

## SCREEN 4: DASHBOARD SCREEN

### Purpose
Hub for the student. Shows subject progress cards with chapters completed, overall progress, and navigation to each subject's syllabus.

### Technical Specifications
- **Layout File:** `activity_dashboard.xml`
- **Activity File:** `DashboardActivity.kt`
- **Data Loading:**
  - On screen load, fetch user data from Firestore `users/{userId}`
  - Fetch user's `completedChapters` array
  - Fetch `quizResults` array
  - Fetch `subjects` from separate Firestore collection
- **Progress Calculation:**
  - For each subject: count chapters marked as complete from `completedChapters`
  - Calculate percentage: (completedChapters / totalChapters) × 100
  - Display progress bar for each subject
- **Subject Cards:**
  - Three cards: Science, Mathematics, Social Studies
  - Each card shows: subject name, chapters completed / total chapters, progress bar percentage
  - Tapping a card navigates to Syllabus Screen for that subject
  - Card shows visual difference between completed and remaining chapters
- **Overall Progress:**
  - Calculate: (total chapters completed across all subjects / total chapters in all subjects) × 100
  - Display as main progress bar or percentage at top
- **Logout Button:**
  - Top-right corner
  - On click: Clear SharedPreferences login state and navigate to Login Screen

### Data Model (Firestore Read)
**Collection:** `users/{userId}`
- `name`: String
- `completedChapters`: Array of chapter IDs (e.g., ["sci_01", "sci_02", "math_05"])
- `quizResults`: Array of result IDs (for later screen)

**Collection:** `subjects`
- Document fields: subject name, chapter count, etc.

### Navigation
- **On subject card click:** → Syllabus Screen (pass subject ID)
- **On logout button click:** → Login Screen

---

## SCREEN 5: SYLLABUS SCREEN

### Purpose
Shows the complete chapter list for a selected subject. Allows student to mark chapters as complete and start quizzes for individual chapters.

### Technical Specifications
- **Layout File:** `activity_syllabus.xml`
- **Activity File:** `SyllabusActivity.kt`
- **Data Loading:**
  - Receive `subjectId` from previous screen via Intent
  - Fetch chapters from Firestore `subjects/{subjectId}/chapters` subcollection
  - Fetch user's `completedChapters` array from `users/{userId}`
  - Mark chapters as completed if their ID is in user's `completedChapters` array
- **Chapter List Display:**
  - RecyclerView showing all chapters for the subject
  - Each chapter item shows:
    - Chapter number (e.g., "Chapter 1")
    - Chapter name
    - Completion status (checkmark or empty circle)
    - "Start Quiz" button
  - Chapters marked complete show a green checkmark icon
  - Chapters not completed show an empty circle
- **Chapter Completion Logic:**
  - When user clicks "Start Quiz" and completes the quiz, chapter is automatically marked complete
  - Update Firestore `users/{userId}/completedChapters` array
  - Checkbox on chapter item can also manually mark chapter complete
- **Progress Bar:**
  - Show progress for this subject: (completed chapters / total chapters) × 100

### Data Model (Firestore Read/Write)
**Subcollection:** `subjects/{subjectId}/chapters`
- `chapterId`: String
- `chapterName`: String
- `chapterNumber`: Integer

**Document Updated:** `users/{userId}/completedChapters`

### Navigation
- **On "Start Quiz" button click:** → Quiz Screen (pass chapter ID)
- **Back button:** → Dashboard Screen

---

## SCREEN 6: QUIZ SCREEN

### Purpose
Displays 15 MCQ (Multiple Choice Questions) for a selected chapter. Timer counts down from 15 minutes. Student answers questions and submits.

### Technical Specifications
- **Layout File:** `activity_quiz.xml`
- **Activity File:** `QuizActivity.kt`
- **Data Loading:**
  - Receive `chapterId` from previous screen via Intent
  - Fetch 15 questions from Firestore `questions` collection filtered by `chapterId`
  - Each question has: `questionText`, `options` (array of 4), `correctAnswer` (index), `explanation`
- **Timer:**
  - Start at 15:00 (15 minutes) in MM:SS format
  - Display prominently at top in red when time < 2 minutes remaining
  - Countdown ticks every second
  - Auto-submit when timer reaches 00:00
  - Show warning dialog at 2:00 remaining
- **Question Display:**
  - Display one question at a time
  - Question number and text
  - Four radio button options (A, B, C, D)
  - Only one option can be selected at a time
  - Previous selections are remembered when navigating back
- **Navigation Between Questions:**
  - Previous/Next buttons at bottom
  - Dot navigation showing all 15 questions
  - Current dot highlighted
  - Tapping a dot navigates to that question
  - Ability to skip questions (not answering)
- **Visual Indicators:**
  - Answered questions: show filled dot
  - Unanswered questions: show empty dot
  - Attempted but then changed: still show filled dot
- **Submit Button:**
  - Appears at the end or always at bottom
  - On click: Show confirmation dialog with answer summary
    - "You have answered X out of 15 questions. Continue?"
  - On confirmation: Submit answers and navigate to Quiz Result Screen

### Data Model (Firestore Read)
**Collection:** `questions`
- `chapterId`: String
- `questionNumber`: Integer (1-15)
- `questionText`: String
- `options`: Array of 4 strings
- `correctAnswer`: Integer (0-3, index of correct option)
- `explanation`: String (why the answer is correct)

### Navigation
- **On submit confirmation:** → Quiz Result Screen (pass answers array, chapterId)
- **On timer expiry:** → Quiz Result Screen (auto-submit with current answers)
- **Back button during quiz:** Show warning "Unsaved progress will be lost. Are you sure?" → Dashboard if confirmed

---

## SCREEN 7: QUIZ RESULT SCREEN

### Purpose
Displays score, breakdown of correct/incorrect/skipped, and provides access to detailed answer review.

### Technical Specifications
- **Layout File:** `activity_quiz_result.xml`
- **Activity File:** `QuizResultActivity.kt`
- **Data Receiving:**
  - Receive `answers` array (selected options for each question) and `chapterId` from Quiz Screen via Intent
  - Fetch the same 15 questions for comparison
- **Score Calculation:**
  - Compare each answer with `correctAnswer` from Firestore
  - Calculate: (correct answers / 15) × 100 = score percentage
  - Determine grade: 90-100 = A, 75-89 = B, 60-74 = C, <60 = F
- **Result Display:**
  - Large score circle showing percentage (e.g., "86%")
  - Below: "Your Grade: B"
  - Breakdown in card format:
    - Correct: X/15 (green)
    - Incorrect: X/15 (red)
    - Skipped: X/15 (gray)
  - Time taken (calculated from start time in Quiz Screen)
- **Save Result:**
  - Create new document in `users/{userId}/quizResults`
  - Save: chapterId, score, answers array, timestamp, gradeAchieved
  - Update `completedChapters` array to mark this chapter as complete if not already
- **Buttons:**
  - "Review Answers" button → Review Answers Screen
  - "Back to Syllabus" button → Syllabus Screen
  - "Practice Again" button → Quiz Screen (reload same chapter)

### Data Model (Firestore Write)
**Subcollection:** `users/{userId}/quizResults`
- `resultId`: auto-generated
- `chapterId`: String
- `score`: Integer (percentage)
- `answers`: Array (selected options)
- `timestamp`: Timestamp
- `gradeAchieved`: String (A, B, C, F)
- `timeTaken`: Integer (seconds)

### Navigation
- **On "Review Answers" click:** → Review Answers Screen (pass answers array, chapterId)
- **On "Back to Syllabus" click:** → Syllabus Screen
- **On "Practice Again" click:** → Quiz Screen (same chapter)

---

## SCREEN 8: REVIEW ANSWERS SCREEN

### Purpose
Detailed breakdown of each question with correct/incorrect indication, student's answer, and explanation for correct answer.

### Technical Specifications
- **Layout File:** `activity_review_answers.xml`
- **Activity File:** `ReviewAnswersActivity.kt`
- **Data Receiving:**
  - Receive `answers` array and `chapterId` via Intent
  - Fetch 15 questions for this chapter from Firestore
- **Filter Chips (Optional):**
  - "All", "Correct", "Incorrect", "Skipped"
  - On click: Filter the displayed questions
  - Default: "All" selected
- **Question Review Layout (RecyclerView):**
  - For each question:
    - Question number and text
    - Four options displayed
    - Correct answer highlighted in green with checkmark
    - Student's selected answer highlighted in different color (or red if wrong)
    - Skipped questions show no selection highlight
    - Full explanation text for the correct answer
    - Visual indicator badge (✓ Correct / ✗ Incorrect / ⊘ Skipped) at top-right of item
- **Scrollable:**
  - Scroll through all questions
  - Bottom shows progress: "Question X of 15"

### Data Model (Firestore Read)
Same as Quiz Screen - read questions and compare with saved answers

### Navigation
- **Back button:** → Dashboard Screen OR Syllabus Screen (depending on where user came from)

---

## SCREEN 9: STRENGTH MAP SCREEN

### Purpose
Visual radar/spider chart showing student's performance across all subjects. Identifies weak areas at a glance.

### Technical Specifications
- **Layout File:** `activity_strength_map.xml`
- **Activity File:** `StrengthMapActivity.kt`
- **Data Loading:**
  - Fetch all of user's `quizResults` from Firestore
  - Group results by subject
  - Calculate average score for each subject
- **Radar Chart:**
  - Use MPAndroidChart library
  - Create radar chart with:
    - 3 axes (one for each subject: Science, Math, Social Studies)
    - Each axis scaled from 0 to 100
    - Plot point for average score in each subject
    - Connect points to form a shape
    - Color: Gradient from green (high score) to red (low score)
  - Interpretation: Larger shape = better overall performance. Uneven shape = gaps in particular subjects
- **Below Chart:**
  - Subject breakdown cards:
    - Subject name
    - Average score percentage
    - Progress bar showing performance level
    - Last quiz date for that subject
    - "View Details" or "Practice" button per subject
- **Data Visualization:**
  - Color coding: Green (80-100), Yellow (60-79), Orange (40-59), Red (<40)
  - Show number of quizzes taken per subject

### Data Model (Firestore Read)
**Read from:** `users/{userId}/quizResults`
- Group by `chapterId` (which belongs to which subject)
- Calculate averages

### Navigation
- **On "Practice" button for a subject:** → Syllabus Screen (for that subject)
- **Back button:** → Dashboard Screen

---

# SECTION 3: DATA MODELS & DATABASE STRUCTURE

---

## Firestore Collections & Documents

### Collection: `users`
**Purpose:** Store user account information

**Document Fields:**
- `userId`: String (auto-generated by Firestore)
- `name`: String
- `phoneNumber`: String
- `gender`: String (Male, Female, Other)
- `password`: String (hashed in production)
- `createdAt`: Timestamp
- `lastLoginAt`: Timestamp
- `completedChapters`: Array (e.g., ["sci_ch01", "sci_ch02", "math_ch05"])
- `quizResults`: Subcollection

### Subcollection: `users/{userId}/quizResults`
**Purpose:** Store individual quiz attempt records

**Document Fields:**
- `resultId`: String (auto-generated)
- `chapterId`: String (reference to chapter)
- `score`: Integer (0-100 percentage)
- `answers`: Array of Integers (0-3, student's selected options)
- `timestamp`: Timestamp
- `gradeAchieved`: String (A, B, C, F)
- `timeTaken`: Integer (seconds)

### Subcollection: `users/{userId}/completedChapters`
**Purpose:** Track which chapters user has finished

**Document Fields:**
- `chapterId`: String
- `completedAt`: Timestamp

---

### Collection: `subjects`
**Purpose:** Master list of subjects

**Document Fields:**
- `subjectId`: String (auto-generated)
- `subjectName`: String (Science, Mathematics, Social Studies)
- `description`: String
- `icon`: String (URL or drawable resource name)
- `totalChapters`: Integer

### Subcollection: `subjects/{subjectId}/chapters`
**Purpose:** List all chapters for a subject

**Document Fields:**
- `chapterId`: String
- `chapterNumber`: Integer (1, 2, 3...)
- `chapterName`: String
- `description`: String (optional)
- `questionCount`: Integer (default 15)

---

### Collection: `questions`
**Purpose:** Master question bank

**Document Fields:**
- `questionId`: String (auto-generated)
- `chapterId`: String (which chapter this belongs to)
- `questionNumber`: Integer (1-15 within chapter)
- `questionText`: String
- `options`: Array of 4 Strings (A, B, C, D text)
- `correctAnswer`: Integer (0-3, index of correct option)
- `explanation`: String (detailed explanation of why answer is correct)
- `difficulty`: String (Easy, Medium, Hard) - optional
- `createdAt`: Timestamp

---

### Collection: `appConfig`
**Purpose:** App configuration (read-only by users)

**Document Fields:**
- `quizDuration`: Integer (in minutes, default 15)
- `questionsPerQuiz`: Integer (default 15)
- `passingScore`: Integer (default 60)
- `version`: String

---

## Database Initialization

**Subjects to create:**
1. Science (16 chapters)
2. Mathematics (15 chapters)
3. Social Studies (20 chapters)

**Minimum questions required:**
- 15 questions per chapter
- 51 total chapters (16 + 15 + 20)
- **765 total questions minimum** in the database

---

# SECTION 4: NAVIGATION FLOW

---

## Complete User Journey

```
Splash Screen (Auto-check login state)
    ↓
    ├─ If logged in → Dashboard
    └─ If not logged in → Login Screen

Login Screen
    ├─ On successful login → Dashboard
    └─ "Sign Up" link → Sign Up Screen

Sign Up Screen
    ├─ On successful registration → Login Screen
    └─ Back arrow → Login Screen

Dashboard Screen
    ├─ On subject card click → Syllabus Screen
    ├─ On logout click → Login Screen
    └─ Bottom nav (if implemented) → Strength Map

Syllabus Screen
    ├─ On "Start Quiz" click → Quiz Screen
    ├─ Back button → Dashboard
    └─ (Mark chapter complete updates local state)

Quiz Screen
    ├─ On timer expiry → Quiz Result Screen (auto-submit)
    ├─ On submit click → Quiz Result Screen
    ├─ Back button → Dashboard (with warning)
    └─ Question navigation via dots/prev/next

Quiz Result Screen
    ├─ "Review Answers" → Review Answers Screen
    ├─ "Back to Syllabus" → Syllabus Screen
    └─ "Practice Again" → Quiz Screen (same chapter)

Review Answers Screen
    ├─ Back button → Dashboard OR Syllabus (context-dependent)
    └─ Filter chips → Re-filter same screen

Strength Map Screen
    ├─ "Practice" button → Syllabus Screen
    └─ Back button → Dashboard
```

---

# SECTION 5: TESTING CHECKLIST

---

## Functional Testing
- [ ] Splash screen displays for 3 seconds and navigates correctly
- [ ] New user can register with all required fields
- [ ] Duplicate phone number registration is prevented
- [ ] Existing user can login with correct credentials
- [ ] Wrong credentials show appropriate error message
- [ ] "Remember Me" saves and pre-fills credentials
- [ ] Dashboard shows correct user name
- [ ] Dashboard shows correct progress for each subject
- [ ] Overall progress calculation is accurate
- [ ] Subject card navigates to correct syllabus
- [ ] Syllabus shows all chapters for selected subject
- [ ] Chapter completion status displays correctly
- [ ] "Start Quiz" loads correct questions for selected chapter
- [ ] Quiz timer starts at 15:00 and counts down
- [ ] Timer auto-submits when reaching 00:00
- [ ] Questions can be navigated forward and backward
- [ ] Dot navigation works correctly
- [ ] Only one answer option can be selected per question
- [ ] Previous answers are remembered when navigating back
- [ ] Submit confirmation dialog shows correct counts
- [ ] Quiz results display correct score breakdown
- [ ] Review screen shows correct/wrong/skipped indicators
- [ ] Review screen shows explanations for each question
- [ ] Filter chips in Review screen work correctly
- [ ] Strength Map radar chart displays with correct data
- [ ] Radar chart updates after new quiz completion
- [ ] Subject progress bars update after quiz completion
- [ ] Back navigation works correctly on all screens
- [ ] App handles no internet connection gracefully
- [ ] App survives screen rotation without data loss

## Edge Cases
- [ ] Empty quiz (user submits without answering any question)
- [ ] All questions answered correctly (100% score)
- [ ] All questions answered incorrectly (0% score)
- [ ] User closes app during quiz (timer should reset)
- [ ] Very long question text wraps correctly
- [ ] Multiple rapid taps on submit button (prevent duplicate submission)
- [ ] First-time user with no quiz data (Strength Map shows zeros)
- [ ] Password with special characters

---

# SECTION 6: DEPLOYMENT & FIREBASE SETUP

---

## Firebase Project Setup Steps

1. Go to console.firebase.google.com
2. Create new project: "Akshara-Deepa-Tutor"
3. Add Android app with package name: com.aksharadeep.tutor
4. Download google-services.json and place in app/ folder
5. Enable Firestore Database in test mode (for development)
6. Enable Real-time Database (optional, for real-time sync)
7. Configure authentication methods (email/password or phone)
8. Create Firestore security rules for production

---

# SECTION 7: IMPACT GOALS & SUCCESS CRITERIA

---

## Impact Goals
1. **Quality Education:** Standardizing the self-study process for rural students
2. **Digital Literacy:** Introducing students to data-driven learning early
3. **Improved Results:** Reducing school dropout rates by catching learning gaps early

## Success Criteria for Students
- The progress bar must update instantly across the entire subject category
- The quiz must have a timer and a "Review Answer" section
- The app must work 100% offline once the syllabus and questions are loaded
- The Strength Map must visually represent performance across all subjects
- All quiz data must be saved and retrievable for review

## KPIs (Key Performance Indicators)
- User can complete registration in under 2 minutes
- Quiz loading time < 2 seconds
- Results calculated and displayed < 1 second after submission
- Radar chart renders within 1 second
- App size < 30MB
- Crash rate < 1%

---

# SECTION 8: FUTURE ENHANCEMENTS (Phase 2 - Optional)

---

1. **AI-Powered Question Generation:** Use GenAI to generate new quiz questions dynamically
2. **Voice Assistant:** Text-to-speech for questions (accessibility for rural students)
3. **Multi-language Support:** Kannada, Hindi, English
4. **Peer Comparison:** Anonymous leaderboard showing percentile ranking
5. **Parent Dashboard:** Parents can track their child's progress via a web portal
6. **Offline Mode:** Full offline support with data sync when internet is available
7. **Push Notifications:** Daily study reminders
8. **Gamification:** Badges, achievements, daily streaks
9. **Adaptive Learning:** AI adjusts question difficulty based on student performance
10. **Video Explanations:** Short video clips for difficult concepts

---

# SECTION 9: QUICK START GUIDE

---

## Quick Start Instructions

**What to build:** An Android educational app called "Akshara-Deepa Tutor" for 10th-grade SSLC students.

**Core features (9 screens):**
1. Splash Screen → Auto-navigate based on login state
2. Login Screen → Phone number + password authentication
3. Sign Up Screen → Registration with name, phone, gender, password
4. Dashboard → Subject cards with progress bars
5. Syllabus Screen → Chapter list for selected subject
6. Quiz Screen → 15 MCQ questions with 15-minute timer
7. Quiz Result Screen → Score display with statistics
8. Review Answers Screen → Detailed answer review with explanations
9. Strength Map Screen → Spider web/radar chart for performance

**Tech requirements:**
- Kotlin, Android SDK, MVVM architecture
- Firebase Firestore for database
- SharedPreferences for local login state
- MPAndroidChart for radar chart
- Material Design components
- Minimum 765 quiz questions (15 per chapter, 51 chapters)

**Three subjects with chapters:**
- Science: 16 chapters
- Mathematics: 15 chapters
- Social Studies: 20 chapters

**Key interactions:**
- User registers → logs in → sees dashboard → picks subject → picks chapter → takes quiz → sees results → reviews answers → checks strength map

**All data flows, UI designs, database schemas, and navigation flows are detailed in this SOP document.**
