# AKSHARA-DEEPA TUTOR - COMPLETE SOP DOCUMENT

## For AntiGravity AI Development

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

### UI Design Prompt
```
Design a splash screen for an educational Android app called "Akshara-Deepa Tutor."

Background: A gradient from deep blue (#1A237E) to purple (#4A148C).

Center of screen:
- App logo: A glowing lamp (deepa/diya) icon with a book, symbolizing 
  "light of knowledge"
- Below the logo: App name "Akshara-Deepa" in white bold text, font size 
  28sp, font family: sans-serif-medium
- Below the app name: Tagline "Your Self-Study Companion" in light gray 
  (#B0BEC5), font size 14sp

Bottom of screen:
- A subtle loading animation (circular progress indicator) in white
- Text "MindMatrix VTU Internship Program" in small gray text, font size 
  10sp

Animation: The logo should fade in from 0 to 1 alpha over 1.5 seconds, 
then the tagline slides up from below over 0.5 seconds.

After 3 seconds total, auto-navigate to the next screen.
```

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

### UI Design Prompt
```
Design a login screen for the Akshara-Deepa Tutor Android app.

Background: White (#FFFFFF) with a subtle curved blue shape at the top 
occupying 30% of the screen height.

Top section (inside the blue curve):
- App logo (smaller version, 60dp x 60dp) centered
- Text "Welcome Back!" in white, bold, 24sp
- Text "Sign in to continue learning" in light white (#E0E0E0), 14sp

Middle section (white area, centered):
- A Card/Container with elevation 4dp, corner radius 16dp, padding 24dp
- Inside the card:
  
  Field 1: Phone Number
  - Label: "Phone Number" in gray (#757575), 12sp
  - Input field: Outlined text field with country code prefix "+91" 
    fixed on the left
  - Input type: Phone number (numeric keyboard)
  - Icon: Phone icon on the left inside the field
  - Max length: 10 digits
  
  Field 2: Password
  - Label: "Password" in gray (#757575), 12sp
  - Input field: Outlined text field with password dots
  - Icon: Lock icon on the left, eye/visibility toggle icon on the right
  - Input type: Password
  
  Checkbox: "Remember Me" with a small checkbox on the left
  - Font size: 12sp, color gray
  
  Button: "SIGN IN"
  - Full width of the card
  - Background: Gradient blue (#1A237E to #283593)
  - Text: White, bold, 16sp
  - Corner radius: 12dp
  - Height: 52dp
  - Elevation: 4dp

Bottom section:
- Text "Don't have an account?" in gray, 14sp
- Clickable text "Sign Up" in blue (#1A237E), bold, 14sp, right next 
  to the above text
- Both texts centered horizontally

Toast/Snackbar messages:
- "Please enter phone number" if phone field is empty
- "Please enter password" if password field is empty
- "Invalid phone number or password" if credentials don't match
- "Login successful!" on successful login
```

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
```
Collection: "users"
Document ID: auto-generated
Fields being read:
  - phoneNumber: String
  - password: String
```

### Navigation
- **On "Sign In" button click (success):** → Dashboard Screen
- **On "Sign Up" text click:** → Sign Up Screen
- **Back button:** Exit app (no previous screen)

---

## SCREEN 3: SIGN UP / REGISTRATION SCREEN

### Purpose
New user registration. Collects name, phone number, gender, and password. Stores all information in Firebase Firestore database.

### UI Design Prompt
```
Design a registration/sign-up screen for the Akshara-Deepa Tutor 
Android app.

Background: White (#FFFFFF) with the same curved blue shape at the top 
(matching login screen design for consistency).

Top section (inside the blue curve):
- Back arrow icon (white, top-left, 24dp) to go back to login
- Text "Create Account" in white, bold, 24sp, centered
- Text "Join Akshara-Deepa today!" in light white, 14sp

Middle section:
- A Card/Container with elevation 4dp, corner radius 16dp, padding 24dp
- Inside the card:

  Field 1: Full Name
  - Label: "Full Name" in gray, 12sp
  - Input field: Outlined text field
  - Icon: Person icon on the left
  - Input type: Text (capitalize words)
  - Hint: "Enter your full name"
  
  Field 2: Phone Number
  - Label: "Phone Number" in gray, 12sp
  - Input field: Outlined text field with "+91" prefix
  - Icon: Phone icon on the left
  - Input type: Phone number (numeric keyboard)
  - Max length: 10 digits
  - Hint: "Enter 10-digit mobile number"
  
  Field 3: Gender
  - Label: "Gender" in gray, 12sp
  - Three radio buttons in a horizontal row:
    ○ Male (with male icon)
    ○ Female (with female icon)  
    ○ Other
  - Default: None selected
  
  Field 4: Password
  - Label: "Create Password" in gray, 12sp
  - Input field: Outlined text field with password dots
  - Icon: Lock icon on left, visibility toggle on right
  - Hint: "Minimum 6 characters"
  - Below the field: Password strength indicator bar 
    (Red = Weak, Yellow = Medium, Green = Strong)
  
  Field 5: Confirm Password
  - Label: "Confirm Password" in gray, 12sp
  - Input field: Outlined text field with password dots
  - Icon: Lock icon on left, visibility toggle on right
  - Hint: "Re-enter your password"
  
  Button: "CREATE ACCOUNT"
  - Full width
  - Background: Gradient blue (#1A237E to #283593)
  - Text: White, bold, 16sp
  - Corner radius: 12dp
  - Height: 52dp

Bottom section:
- Text "Already have an account?" in gray, 14sp
- Clickable text "Sign In" in blue, bold, 14sp

Validation error messages (shown below each field in red, 11sp):
- "Name is required"
- "Please enter a valid 10-digit phone number"
- "Please select your gender"
- "Password must be at least 6 characters"
- "Passwords do not match"
```

### Technical Specifications
- **Layout File:** `activity_signup.xml`
- **Activity File:** `SignUpActivity.kt`
- **Input Validation (all must pass before submission):**
  - Name: Not empty, minimum 2 characters, only letters and spaces
  - Phone: Exactly 10 digits, starts with 6/7/8/9
  - Gender: One option must be selected
  - Password: Minimum 6 characters
  - Confirm Password: Must match Password field
  - Check if phone number already exists in Firestore (prevent duplicates)
- **Registration Logic:**
  - On button click, validate all fields
  - Show loading progress dialog "Creating your account..."
  - Query Firestore to check if phone number already exists
  - If exists: Show error "This phone number is already registered"
  - If not exists: Create new document in `users` collection
  - On success: Show success toast, auto-navigate to Login Screen
  - On failure: Show error Snackbar with Firebase error message

### Data Model (Firestore Write)
```
Collection: "users"
Document ID: auto-generated by Firestore
Fields being written:
{
  "fullName": "String - user's full name",
  "phoneNumber": "String - 10 digit phone number",
  "gender": "String - Male/Female/Other",
  "password": "String - user's password",
  "createdAt": "Timestamp - server timestamp",
  "profileImageUrl": "String - empty string (for future use)",
  "subjectProgress": {
    "Science": 0,
    "Math": 0,
    "Social Studies": 0
  },
  "quizHistory": [],
  "totalQuizzesTaken": 0,
  "averageScore": 0
}
```

### Navigation
- **On "Create Account" button (success):** → Login Screen (with success message)
- **On "Sign In" text click:** → Login Screen
- **On back arrow:** → Login Screen

---

## SCREEN 4: DASHBOARD SCREEN (HOME)

### Purpose
Main screen after login. Shows all subjects with progress bars indicating how much of each subject the student has completed. Acts as the central navigation hub.

### UI Design Prompt
```
Design a dashboard/home screen for the Akshara-Deepa Tutor Android app.

Top Section - App Bar:
- Background: Solid blue (#1A237E)
- Left: Hamburger menu icon (white, 24dp) OR user avatar circle
- Center: "Akshara-Deepa" text in white, 18sp, bold
- Right: Bell notification icon (white) with a small red badge if 
  there are reminders

Below App Bar - Welcome Banner:
- A rounded card (corner radius 16dp) with gradient background 
  (blue to purple)
- Padding: 16dp
- Left side:
  - "Hello, {User's First Name}! 👋" in white, bold, 20sp
  - "Continue your learning journey" in light white, 13sp
  - Small chip showing "🔥 Day Streak: 5" (track consecutive days)
- Right side:
  - A circular progress ring showing overall progress percentage
  - Inside the ring: "{X}%" in white, bold, 18sp
  - Below ring: "Overall" in white, 10sp

Section Title:
- "Your Subjects" in dark text (#212121), bold, 18sp, left-aligned
- Padding top: 16dp, left: 16dp

Subject Cards (Scrollable vertical list, 3 cards):
Each card:
- Elevation: 4dp
- Corner radius: 12dp
- Margin: 8dp horizontal, 6dp vertical
- Padding: 16dp
- Background: White
- Height: Wrap content (approximately 120dp)

Card Layout (for each subject):
- Left side: Subject icon in a colored circle (48dp x 48dp)
  - Science: Green circle (#4CAF50) with flask/beaker icon
  - Mathematics: Blue circle (#2196F3) with calculator icon
  - Social Studies: Orange circle (#FF9800) with globe icon
- Middle (next to icon, with 12dp margin):
  - Subject name in black, bold, 16sp
  - "X of Y chapters completed" in gray, 12sp
  - Horizontal progress bar below:
    - Track color: Light gray (#E0E0E0)
    - Progress color: Matches the subject icon circle color
    - Height: 8dp
    - Corner radius: 4dp
    - Animated fill
- Right side:
  - Arrow/chevron icon pointing right (gray, 24dp)
  - Small text showing percentage, e.g., "45%" in the subject color

The three subjects are:
1. Science (Green theme)
2. Mathematics (Blue theme)
3. Social Studies (Orange theme)

Bottom Section - Quick Actions Row:
- A horizontal row of 2 action cards below the subjects:
  
  Card 1: "Strength Map"
  - Icon: Spider web / radar chart icon
  - Background: Light purple tint
  - Text: "View Strength Map" in purple
  - On tap: Navigate to Strength Map screen
  
  Card 2: "Quiz History"  
  - Icon: History/clock icon
  - Background: Light green tint
  - Text: "Past Quizzes" in green
  - On tap: Navigate to Quiz History (optional)

Bottom Navigation Bar:
- 3 items:
  1. Home icon + "Home" (active/highlighted in blue)
  2. Bar chart icon + "Strength Map"
  3. Person icon + "Profile"
- Active item: Blue (#1A237E) with filled icon
- Inactive items: Gray (#9E9E9E) with outlined icons
- Background: White with top border line (light gray)
```

### Technical Specifications
- **Layout File:** `activity_dashboard.xml`
- **Activity File:** `DashboardActivity.kt`
- **ViewModel:** `DashboardViewModel.kt`
- **Data Loading:**
  - On `onCreate()`, fetch user data from Firestore using stored `userId`
  - Fetch user's name for welcome banner
  - Calculate progress for each subject from `subjectProgress` field
  - Calculate overall progress = average of all subject progresses
- **Subject Cards Logic:**
  - Use `RecyclerView` with `SubjectAdapter`
  - Each card shows subject name, completed chapters count, total chapters count, and progress percentage
  - Progress bar animates from 0 to current value on screen load
- **Progress Calculation:**
  - Science: Total 16 chapters → Progress = (completed chapters / 16) × 100
  - Math: Total 15 chapters → Progress = (completed chapters / 15) × 100
  - Social Studies: Total 20 chapters → Progress = (completed chapters / 20) × 100
  - Overall = Average of three percentages

### Data Model (Firestore Read)
```
Collection: "users"
Document: {userId}
Fields being read:
  - fullName (for welcome message)
  - subjectProgress.Science (integer: chapters completed)
  - subjectProgress.Math (integer: chapters completed)
  - subjectProgress.SocialStudies (integer: chapters completed)
```

### Navigation
- **On Subject Card tap:** → Syllabus Screen (pass subject name as intent extra)
- **On "Strength Map" card tap:** → Strength Map Screen
- **Bottom Nav "Home":** Current screen (no action)
- **Bottom Nav "Strength Map":** → Strength Map Screen
- **Bottom Nav "Profile":** → Profile Screen (optional)
- **Back button:** Show "Are you sure you want to exit?" dialog

---

## SCREEN 5: SYLLABUS / CHAPTER LIST SCREEN

### Purpose
Shows all chapters for the selected subject. User can see which chapters they've completed, and select any chapter to start a quiz.

### UI Design Prompt
```
Design a syllabus/chapter list screen for the Akshara-Deepa Tutor app.

Top - App Bar:
- Background: Color matching the subject theme
  (Green for Science, Blue for Math, Orange for Social Studies)
- Left: Back arrow (white)
- Center: "{Subject Name}" in white, bold, 18sp
- Right: Filter icon (optional, for future use)

Below App Bar - Subject Header Card:
- A card with the subject's theme color as background (lighter shade)
- Corner radius: 0dp top, 16dp bottom
- Padding: 20dp
- Content:
  - Subject icon (large, 64dp) on the left
  - Right side:
    - "{Subject Name} Syllabus" in dark text, bold, 20sp
    - "{X} Chapters" in gray, 14sp
    - "{Y} Completed" in green, 14sp with checkmark icon
    - Circular progress indicator showing completion percentage

Section Title:
- "Chapters" in dark text, bold, 16sp
- Right side: "Select a chapter to start quiz" in gray, 12sp

Chapter List (RecyclerView - vertical scrolling):
Each chapter item:
- Layout: Card with elevation 2dp, corner radius 10dp
- Margin: 8dp horizontal, 4dp vertical
- Padding: 14dp
- Height: Wrap content

Inside each card:
- Left: Chapter number in a circle 
  - If completed: Green circle with white checkmark
  - If not completed: Gray circle with the number (1, 2, 3...)
  - Size: 40dp x 40dp
- Middle:
  - Chapter name in black/dark gray, 15sp, medium weight
  - Chapter status text:
    - If completed: "Completed ✓ - Best Score: {X}%" in green, 11sp
    - If not completed: "Not attempted yet" in gray, 11sp
- Right:
  - "Start Quiz" button (small, outlined)
  - If completed: Button text changes to "Retake Quiz"
  - Button color matches subject theme
  
Divider: Light gray line between items (0.5dp)

Example chapters for Science:
1. Chemical Reactions and Equations
2. Acids, Bases and Salts
3. Metals and Non-metals
4. Carbon and its Compounds
5. Life Processes
6. Control and Coordination
7. How do Organisms Reproduce
8. Heredity and Evolution
9. Light - Reflection and Refraction
10. The Human Eye and the Colourful World
11. Electricity
12. Magnetic Effects of Electric Current
13. Our Environment
14. Sustainable Management of Natural Resources
15. Sources of Energy
16. Periodic Classification of Elements

Floating Action Button (optional):
- Bottom right
- Subject theme color
- Icon: Play/start icon
- Tooltip: "Random Quiz" (picks a random chapter)
```

### Technical Specifications
- **Layout File:** `activity_syllabus.xml`
- **Activity File:** `SyllabusActivity.kt`
- **ViewModel:** `SyllabusViewModel.kt`
- **Adapter:** `ChapterAdapter.kt`
- **Data Loading:**
  - Receive `subjectName` from intent extras
  - Load chapter list from local data source (hardcoded or Room database)
  - Load user's completion status for each chapter from Firestore
- **Chapter Data Structure:**
```kotlin
data class Chapter(
    val chapterId: Int,
    val chapterName: String,
    val subjectName: String,
    val isCompleted: Boolean,
    val bestScore: Int,
    val totalQuestions: Int
)
```

### Chapter Data (Pre-loaded)

**Science (16 chapters):**
1. Chemical Reactions and Equations
2. Acids, Bases and Salts
3. Metals and Non-metals
4. Carbon and its Compounds
5. Life Processes
6. Control and Coordination
7. How do Organisms Reproduce
8. Heredity and Evolution
9. Light - Reflection and Refraction
10. The Human Eye and the Colourful World
11. Electricity
12. Magnetic Effects of Electric Current
13. Our Environment
14. Sustainable Management of Natural Resources
15. Sources of Energy
16. Periodic Classification of Elements

**Mathematics (15 chapters):**
1. Real Numbers
2. Polynomials
3. Pair of Linear Equations in Two Variables
4. Quadratic Equations
5. Arithmetic Progressions
6. Triangles
7. Coordinate Geometry
8. Introduction to Trigonometry
9. Some Applications of Trigonometry
10. Circles
11. Constructions
12. Areas Related to Circles
13. Surface Areas and Volumes
14. Statistics
15. Probability

**Social Studies (20 chapters):**
1. The Rise of Nationalism in Europe
2. Nationalism in India
3. The Making of a Global World
4. The Age of Industrialisation
5. Print Culture and the Modern World
6. Resources and Development
7. Forest and Wildlife Resources
8. Water Resources
9. Agriculture
10. Minerals and Energy Resources
11. Manufacturing Industries
12. Lifelines of National Economy
13. Power Sharing
14. Federalism
15. Democracy and Diversity
16. Gender, Religion and Caste
17. Popular Struggles and Movements
18. Political Parties
19. Outcomes of Democracy
20. Challenges to Democracy

### Data Model (Firestore Read)
```
Collection: "users"
Document: {userId}
Sub-collection: "completedChapters"
Document: {subjectName_chapterId}
Fields:
  - chapterId: Int
  - subjectName: String
  - isCompleted: Boolean
  - bestScore: Int
  - lastAttemptDate: Timestamp
  - totalAttempts: Int
```

### Navigation
- **On "Start Quiz" / "Retake Quiz" button:** → Quiz Screen (pass subjectName + chapterId + chapterName)
- **On back arrow:** → Dashboard Screen
- **On back button:** → Dashboard Screen

---

## SCREEN 6: QUIZ SCREEN

### Purpose
The core learning feature. Displays 15 multiple-choice questions for the selected chapter with a countdown timer. User answers questions one by one and submits at the end.

### UI Design Prompt
```
Design a quiz screen for the Akshara-Deepa Tutor Android app.

Top - Custom App Bar:
- Background: White with bottom shadow
- Left: Close (X) icon in gray - shows confirmation dialog before exit
- Center: Chapter name in dark text, 14sp, single line, ellipsize end
- Right: Timer display
  - Timer icon (red) + "MM:SS" countdown in red bold text, 16sp
  - Timer starts at 15:00 (15 minutes for 15 questions)
  - When less than 2 minutes: Timer text turns red and pulses

Below App Bar - Progress Indicator:
- Horizontal progress bar showing question progress
  - Track: Light gray
  - Progress: Subject theme color
  - Shows progress as fraction: fills up as user answers
- Text below: "Question {current} of 15" in gray, 12sp, centered

Question Area (Main Content - Scrollable):
- Card with elevation 3dp, corner radius 14dp, margin 16dp
- Padding: 20dp
- Background: White

Inside the card:
  - Question number badge: Circle with number, subject theme color 
    background, white text, 28dp
  - Question text: Black, 16sp, medium weight, line spacing 1.4
    (Leave space for questions that might be 2-3 lines long)
  
  - Space: 16dp
  
  - Answer Options (4 options - A, B, C, D):
    Each option is a selectable card/button:
    - Border: 1.5dp solid gray (#BDBDBD) when unselected
    - Border: 2dp solid subject theme color when selected
    - Background: White when unselected
    - Background: Very light theme color (10% opacity) when selected
    - Corner radius: 10dp
    - Padding: 14dp
    - Margin bottom: 10dp
    - Left side: Option letter (A/B/C/D) in a small circle
      - Unselected: Gray circle with gray letter
      - Selected: Theme color circle with white letter
    - Right side: Option text in black, 14sp
    - On selection: Smooth transition animation (color change)
    - Only one option can be selected at a time (radio button behavior)

Bottom Navigation Buttons:
- Fixed at bottom (not scrolling with content)
- Background: White with top shadow
- Padding: 12dp

  Left button: "← Previous"
  - Outlined button, gray border
  - Disabled (grayed out) on first question
  - Text: Gray, 14sp
  
  Center: Question dots/indicators
  - 15 small dots in a row (or two rows if needed)
  - Gray dot: Not answered
  - Theme color dot: Answered
  - Current dot: Slightly larger with ring
  - Tappable: User can tap a dot to jump to that question
  
  Right button: "Next →"  
  - Filled button, subject theme color
  - Text: White, 14sp, bold
  - On last question (15th): Button changes to "Submit Quiz"
  - "Submit Quiz" button: Green (#4CAF50) background
  
Submit Confirmation Dialog (when "Submit Quiz" is tapped):
- Title: "Submit Quiz?"
- Message: "You have answered {X} out of 15 questions. 
  {Y} questions are unanswered. Are you sure you want to submit?"
- Button 1: "Review" (outlined) - stays on quiz
- Button 2: "Submit" (filled, green) - submits and goes to results
- If timer runs out: Auto-submit with dialog "Time's Up! 
  Your quiz has been auto-submitted."
```

### Technical Specifications
- **Layout File:** `activity_quiz.xml`
- **Activity File:** `QuizActivity.kt`
- **ViewModel:** `QuizViewModel.kt`
- **Adapter:** None (single question displayed at a time, ViewPager2 or manual fragment swap)

### Timer Logic
```
- Total time: 15 minutes (900,000 milliseconds)
- Use CountDownTimer class
- Update timer text every second
- When timer reaches 2 minutes (120,000ms): Change timer color to red, 
  start pulsing animation
- When timer reaches 0: Auto-submit quiz, show "Time's Up!" dialog
- Timer should survive screen rotation (use ViewModel to store remaining time)
```

### Question Navigation Logic
```
- Store all 15 questions in a List<QuizQuestion>
- Store user's selected answers in a Map<Int, Int> (questionIndex → selectedOptionIndex)
- "Previous" button: Decrement currentIndex, show previous question with 
  saved selection
- "Next" button: Increment currentIndex, show next question
- Dot navigation: Set currentIndex to tapped dot position
- On last question: Show "Submit Quiz" instead of "Next"
```

### Quiz Question Data Model
```kotlin
data class QuizQuestion(
    val questionId: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Int, // 0=A, 1=B, 2=C, 3=D
    val explanation: String,
    val chapterId: Int,
    val subjectName: String
)
```

### Sample Questions (Science - Chapter 1: Chemical Reactions and Equations)
```
Q1: Which of the following is an example of a combination reaction?
A) Burning of coal
B) Decomposition of vegetable matter
C) Burning of magnesium in air ✓
D) Digestion of food

Q2: What is the formula of rust?
A) Fe2O3 
B) Fe3O4
C) Fe2O3.xH2O ✓
D) FeO

Q3: A chemical reaction in which heat is absorbed is called:
A) Exothermic reaction
B) Endothermic reaction ✓
C) Combination reaction
D) Displacement reaction

(... 15 questions per chapter, pre-loaded in local database)
```

**Note: Each chapter must have a minimum of 15 questions. Total minimum questions = 51 chapters × 15 = 765 questions. These should be pre-loaded in a local Room database or in a Firestore collection.**

### Navigation
- **On Close (X) icon:** Show confirmation → if "Leave": → Syllabus Screen (no data saved)
- **On "Submit Quiz" button:** → Quiz Result Screen (pass quiz data)
- **On timer expiry:** Auto-submit → Quiz Result Screen

---

## SCREEN 7: QUIZ RESULT SCREEN

### Purpose
Shows the user's quiz results after submission. Displays score, time taken, correct/wrong/unanswered counts, and provides a "Review Answers" button.

### UI Design Prompt
```
Design a quiz result screen for the Akshara-Deepa Tutor Android app.

Background: Light gray (#F5F5F5)

Top Section - Result Banner:
- Full width card/container
- Background: Gradient based on performance
  - Score >= 80%: Green gradient (#43A047 to #66BB6A) with 🎉 
    confetti animation
  - Score 50-79%: Blue gradient (#1E88E5 to #42A5F5) with 👍 
    thumbs up icon
  - Score < 50%: Orange gradient (#F57C00 to #FFA726) with 
    💪 "Keep trying" icon
- Height: ~200dp
- Content centered:
  - Large circular score ring (120dp x 120dp)
    - Ring track: White with 30% opacity
    - Ring progress: White, animated fill
    - Inside ring: Score as "{X}/15" in white, bold, 28sp
    - Below inside ring: "{percentage}%" in white, 16sp
  - Below the ring:
    - Performance message:
      - >= 80%: "Excellent! 🌟" 
      - 60-79%: "Good Job! 👍"
      - 40-59%: "Keep Practicing! 💪"
      - < 40%: "Need More Practice 📚"
    - In white, bold, 20sp
  - Subject and Chapter name in white, 13sp

Middle Section - Statistics Cards:
- Horizontal row of 3 small cards, equally spaced
- Each card: Elevation 2dp, corner radius 10dp, padding 12dp

  Card 1 - Correct:
  - Icon: Green checkmark circle
  - Number: "{X}" in green, bold, 24sp
  - Label: "Correct" in gray, 11sp
  
  Card 2 - Wrong:
  - Icon: Red X circle
  - Number: "{Y}" in red, bold, 24sp
  - Label: "Wrong" in gray, 11sp
  
  Card 3 - Unanswered:
  - Icon: Gray question mark circle
  - Number: "{Z}" in gray, bold, 24sp
  - Label: "Skipped" in gray, 11sp

Additional Info Row:
- "Time Taken: MM:SS" with clock icon - left aligned
- "Total Questions: 15" with list icon - right aligned
- Font: 13sp, gray

Bottom Section - Action Buttons:
- Vertical stack of buttons, centered, margin 16dp

  Button 1: "📋 Review Answers"
  - Full width
  - Background: White with blue border (outlined)
  - Text: Blue (#1A237E), bold, 15sp
  - Corner radius: 12dp
  - Height: 50dp
  - Icon: Clipboard/review icon on the left
  
  Button 2: "🔄 Retake Quiz"
  - Full width
  - Background: White with green border (outlined)
  - Text: Green (#43A047), bold, 15sp
  - Corner radius: 12dp
  - Height: 50dp
  
  Button 3: "🏠 Back to Dashboard"
  - Full width
  - Background: Blue (#1A237E) solid
  - Text: White, bold, 15sp
  - Corner radius: 12dp
  - Height: 50dp

Confetti animation should play for 3 seconds if score >= 80%.
```

### Technical Specifications
- **Layout File:** `activity_quiz_result.xml`
- **Activity File:** `QuizResultActivity.kt`
- **Data received from Quiz Screen (via Intent):**
  - `subjectName`: String
  - `chapterId`: Int
  - `chapterName`: String
  - `totalQuestions`: Int (15)
  - `correctAnswers`: Int
  - `wrongAnswers`: Int
  - `unansweredQuestions`: Int
  - `timeTaken`: Long (in milliseconds)
  - `userAnswers`: ArrayList<Int> (user's selected options, -1 if unanswered)
  - `questionIds`: ArrayList<Int>

### Score Saving Logic
```
On this screen load:
1. Calculate score percentage = (correctAnswers / totalQuestions) × 100
2. Save to Firestore:
   
   Collection: "users" → Document: {userId} → Sub-collection: "quizResults"
   New Document:
   {
     "subjectName": "Science",
     "chapterId": 1,
     "chapterName": "Chemical Reactions and Equations",
     "score": 12,
     "totalQuestions": 15,
     "percentage": 80,
     "timeTaken": 540000,
     "attemptDate": Timestamp,
     "attemptNumber": 1
   }

3. Update chapter completion status:
   Collection: "users" → Document: {userId} → Sub-collection: "completedChapters"
   Document: "Science_1"
   {
     "isCompleted": true,
     "bestScore": max(previousBest, currentScore),
     "lastAttemptDate": Timestamp,
     "totalAttempts": increment by 1
   }

4. Update subject progress:
   Collection: "users" → Document: {userId}
   Update: "subjectProgress.Science" = count of completed chapters in Science

5. Update strength map data:
   Collection: "users" → Document: {userId} → Sub-collection: "strengthData"
   Document: "Science"
   {
     "subjectName": "Science",
     "averageScore": recalculated average,
     "chaptersAttempted": count,
     "lastUpdated": Timestamp
   }
```

### Navigation
- **On "Review Answers" button:** → Review Answers Screen
- **On "Retake Quiz" button:** → Quiz Screen (same chapter, fresh quiz)
- **On "Back to Dashboard" button:** → Dashboard Screen
- **On back button:** → Dashboard Screen (prevent going back to quiz)

---

## SCREEN 8: REVIEW ANSWERS SCREEN

### Purpose
Allows the user to review all 15 questions with their answers, correct answers, and explanations. Shows what they got right and wrong.

### UI Design Prompt
```
Design a review answers screen for the Akshara-Deepa Tutor Android app.

Top - App Bar:
- Background: White with bottom shadow
- Left: Back arrow (dark gray)
- Center: "Review Answers" in dark text, bold, 18sp
- Right: Text "Score: {X}/15" in blue, 14sp

Below App Bar - Summary Strip:
- Horizontal scrollable chips/tags:
  - "All (15)" - selected by default
  - "✓ Correct ({X})" in green
  - "✗ Wrong ({Y})" in red
  - "○ Skipped ({Z})" in gray
- These are filter chips: tapping one filters the question list

Question List (RecyclerView - vertical scrolling):
Each question item is an expandable card:

Card Layout:
- Elevation: 2dp
- Corner radius: 10dp
- Margin: 8dp horizontal, 6dp vertical
- Background: White
- Left border: 4dp thick colored strip
  - Green if answered correctly
  - Red if answered wrongly
  - Gray if skipped

Inside each card:
  Header Row (always visible):
  - Left: Result icon
    - ✓ Green checkmark circle if correct
    - ✗ Red X circle if wrong
    - ○ Gray circle if skipped
  - Middle: "Question {number}" in dark text, bold, 14sp
  - Right: Expand/collapse arrow (chevron down/up)

  Expanded Content (visible when card is tapped):
  - Question text in black, 15sp, medium weight
  - Space: 12dp
  
  - Options listed vertically:
    Option A to D, each showing:
    - If this option is the CORRECT answer:
      - Green background (#E8F5E9)
      - Green checkmark icon
      - Text in green bold
    - If this option was the USER'S WRONG answer:
      - Red background (#FFEBEE)
      - Red X icon
      - Text in red with strikethrough
    - If this option is neither correct nor selected:
      - White/light gray background
      - No icon
      - Normal gray text
    
    - Each option: Rounded rectangle, padding 10dp, margin bottom 6dp
  
  - Space: 12dp
  
  - Explanation Box:
    - Background: Light blue (#E3F2FD)
    - Border left: 3dp blue (#1E88E5)
    - Corner radius: 8dp
    - Padding: 12dp
    - Icon: 💡 Light bulb icon
    - Title: "Explanation" in blue, bold, 13sp
    - Text: Explanation text in dark gray, 13sp, line spacing 1.3

Bottom Fixed Bar:
- Background: White with top shadow
- Center: "Question {current} of 15" with left/right arrow buttons
  for quick navigation between questions
```

### Technical Specifications
- **Layout File:** `activity_review.xml`
- **Activity File:** `ReviewActivity.kt`
- **Adapter:** `ReviewQuestionAdapter.kt`
- **Data received from Result Screen (via Intent):**
  - All question data (questions, options, correct answers)
  - User's selected answers
  - Explanations for each question
- **Filter Logic:**
  - "All": Show all 15 questions
  - "Correct": Filter list to show only questions where userAnswer == correctAnswer
  - "Wrong": Filter where userAnswer != correctAnswer AND userAnswer != -1
  - "Skipped": Filter where userAnswer == -1
- **Card Expand/Collapse:**
  - Use `RecyclerView` with expandable items
  - Only one card expanded at a time (optional: allow multiple)
  - Smooth expand/collapse animation

### Navigation
- **On back arrow:** → Quiz Result Screen
- **On back button:** → Quiz Result Screen

---

## SCREEN 9: STRENGTH MAP SCREEN

### Purpose
Visual representation of the student's performance across all subjects using a Spider Web (Radar) Chart. Helps identify strong and weak areas.

### UI Design Prompt
```
Design a strength map screen for the Akshara-Deepa Tutor Android app 
using a Spider Web / Radar Chart.

Top - App Bar:
- Background: Deep purple (#4A148C)
- Left: Back arrow (white)
- Center: "Strength Map" in white, bold, 18sp
- Right: Info (i) icon - shows tooltip explaining the chart

Below App Bar - Header:
- Background: Gradient purple (#4A148C to #7B1FA2)
- Padding: 20dp
- "Your Performance Overview" in white, bold, 20sp
- "Track your strengths across all subjects" in light white, 13sp
- Bottom corners: Rounded (16dp)

Main Content - Spider Web / Radar Chart:
- Library: MPAndroidChart (RadarChart)
- Chart takes up approximately 60% of the screen height
- Background: White card with elevation 4dp, corner radius 16dp, 
  margin 16dp
- Padding inside: 16dp

Chart Configuration:
- Web shape: Spider web (not circle)
- Web lines: Light gray (#E0E0E0), 1dp
- Web count (concentric rings): 5 (representing 0%, 20%, 40%, 60%, 80%, 100%)
- Labels on outer edge (the axes/spokes):
  These represent categories/subjects. Use these 6 axes:
  1. "Science" 
  2. "Mathematics"
  3. "Social Studies"
  4. "Accuracy" (overall correct answer percentage)
  5. "Consistency" (streak/regular practice score)
  6. "Speed" (average time per question efficiency)
  
- Label font: Dark gray, 12sp, bold
- Each label positioned around the hexagonal chart

Data Set 1 (Current Performance):
- Fill color: Blue (#1A237E) with 30% opacity
- Line color: Blue (#1A237E), 2dp
- Data points: Filled blue circles, 4dp radius
- Values: Fetched from Firestore (each axis 0-100)

Data Set 2 (Previous Performance - optional):
- Fill color: Orange (#FF9800) with 15% opacity
- Line color: Orange (#FF9800), 1.5dp, dashed
- This shows last month's performance for comparison

Chart Legend (below chart):
- Blue dot + "Current Performance"
- Orange dot + "Previous Performance" (if applicable)

Below Chart - Subject Breakdown Cards:
A vertical list of 3 cards (one per subject):

Each card:
- Elevation: 2dp, corner radius: 10dp
- Padding: 14dp
- Horizontal layout:
  - Left: Subject color circle (40dp) with subject icon
  - Middle:
    - Subject name in black, bold, 15sp
    - "Chapters completed: X/Y" in gray, 12sp
    - Small horizontal progress bar
  - Right:
    - Average score percentage in large text
    - Color coded: Green (>=70%), Orange (40-69%), Red (<40%)
    - Small trend arrow: ↑ green if improving, ↓ red if declining

Bottom - Insight Card:
- Background: Light yellow (#FFF8E1) with left border amber
- Corner radius: 10dp
- Icon: 💡
- Title: "AI Insight" in amber, bold, 14sp
- Text: Dynamic insight based on data, e.g.:
  "You're strong in Science but need more practice in Mathematics. 
   Focus on chapters 4, 7, and 9 in Math to improve your overall score."
- Font: 13sp, dark gray
```

### Technical Specifications
- **Layout File:** `activity_strength_map.xml`
- **Activity File:** `StrengthMapActivity.kt`
- **ViewModel:** `StrengthMapViewModel.kt`
- **Chart Library:** MPAndroidChart (add to build.gradle)
  ```gradle
  implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
  ```

### Radar Chart Data Calculation
```kotlin
// For each axis of the radar chart:

// 1. Science Score = Average of all Science quiz scores (0-100)
val scienceScore = allScienceQuizScores.average()

// 2. Math Score = Average of all Math quiz scores (0-100)
val mathScore = allMathQuizScores.average()

// 3. Social Studies Score = Average of all Social Studies quiz scores (0-100)
val socialScore = allSocialStudiesQuizScores.average()

// 4. Accuracy = (Total correct answers across all quizzes / Total questions attempted) × 100
val accuracy = (totalCorrect.toFloat() / totalAttempted) * 100

// 5. Consistency = Based on daily login streak and regular practice
// Formula: min(100, (daysActive / 30) × 100)
val consistency = min(100f, (daysActive.toFloat() / 30f) * 100f)

// 6. Speed = Based on average time per question vs expected time
// Expected time per question = 60 seconds
// If avg time <= 30 sec: Speed = 100
// If avg time = 60 sec: Speed = 50
// If avg time >= 120 sec: Speed = 0
val speed = max(0f, 100f - ((avgTimePerQuestion - 30f) / 90f * 100f))
```

### Data Model (Firestore Read)
```
Collection: "users" → Document: {userId}
Read fields:
  - subjectProgress (for chapter completion data)

Collection: "users" → Document: {userId} → Sub-collection: "quizResults"
Read all documents to calculate:
  - Average scores per subject
  - Total correct / total attempted
  - Average time per question

Collection: "users" → Document: {userId} → Sub-collection: "strengthData"
Read documents for cached/pre-calculated strength values
```

### Navigation
- **On back arrow:** → Dashboard Screen
- **On back button:** → Dashboard Screen
- **On subject card tap:** → Syllabus Screen for that subject

---

# SECTION 3: COMPLETE SCREEN NAVIGATION FLOW

---

```
┌─────────────────┐
│  SPLASH SCREEN   │
│  (3 seconds)     │
└────────┬─────────┘
         │
         ├── User logged in? ──YES──→ ┌──────────────┐
         │                              │  DASHBOARD   │
         └── NO ──→ ┌────────────┐     │   SCREEN     │
                     │   LOGIN    │     └──────┬───────┘
                     │   SCREEN   │            │
                     └──────┬─────┘            ├── Tap Subject Card
                            │                  │    ↓
                    ┌───────┴──────┐     ┌─────┴──────────┐
                    │  SIGN UP     │     │   SYLLABUS      │
                    │  SCREEN      │     │   SCREEN        │
                    └──────────────┘     └─────┬───────────┘
                           │                    │
                    (After signup,              ├── Tap "Start Quiz"
                     go to Login)               │    ↓
                                          ┌─────┴──────────┐
                                          │   QUIZ SCREEN   │
                                          │   (15 questions  │
                                          │    + timer)      │
                                          └─────┬───────────┘
                                                │
                                          (Submit / Time up)
                                                │
                                          ┌─────┴───────────┐
                                          │  QUIZ RESULT     │
                                          │  SCREEN          │
                                          └─────┬───────────┘
                                                │
                                       ┌────────┴────────┐
                                       │                  │
                                 "Review Answers"   "Back to Dashboard"
                                       │                  │
                                 ┌─────┴──────┐    ┌─────┴────────┐
                                 │  REVIEW     │    │  DASHBOARD   │
                                 │  ANSWERS    │    │  SCREEN      │
                                 │  SCREEN     │    └──────────────┘
                                 └─────────────┘

Dashboard also connects to:
  ┌──────────────┐
  │ STRENGTH MAP │ ←── Bottom nav "Strength Map"
  │   SCREEN     │ ←── Quick action card
  └──────────────┘
```

### Complete Navigation Matrix

| From Screen | Action | To Screen | Data Passed |
|---|---|---|---|
| Splash | Auto (logged in) | Dashboard | userId |
| Splash | Auto (not logged in) | Login | None |
| Login | Tap "Sign Up" | Sign Up | None |
| Login | Tap "Sign In" (success) | Dashboard | userId |
| Sign Up | Tap "Create Account" (success) | Login | Success message |
| Sign Up | Tap "Sign In" link | Login | None |
| Dashboard | Tap Subject Card | Syllabus | subjectName |
| Dashboard | Tap "Strength Map" | Strength Map | userId |
| Dashboard | Bottom Nav "Strength Map" | Strength Map | userId |
| Syllabus | Tap "Start Quiz" | Quiz | subjectName, chapterId, chapterName |
| Syllabus | Back arrow | Dashboard | None |
| Quiz | Submit / Timer expires | Quiz Result | All quiz data |
| Quiz | Close (X) + Confirm | Syllabus | None |
| Quiz Result | Tap "Review Answers" | Review Answers | Questions + user answers |
| Quiz Result | Tap "Retake Quiz" | Quiz | Same chapter data |
| Quiz Result | Tap "Back to Dashboard" | Dashboard | None |
| Review Answers | Back arrow | Quiz Result | None |
| Strength Map | Back arrow | Dashboard | None |
| Strength Map | Tap subject card | Syllabus | subjectName |

---

# SECTION 4: DATABASE SCHEMA (FIREBASE FIRESTORE)

---

```
Firestore Database Structure:
│
├── users (Collection)
│   ├── {userId_1} (Document)
│   │   ├── fullName: "Rahul Kumar"
│   │   ├── phoneNumber: "9876543210"
│   │   ├── gender: "Male"
│   │   ├── password: "hashed_password"
│   │   ├── createdAt: Timestamp
│   │   ├── lastLogin: Timestamp
│   │   ├── profileImageUrl: ""
│   │   ├── totalQuizzesTaken: 12
│   │   ├── averageScore: 73.5
│   │   ├── dayStreak: 5
│   │   ├── lastActiveDate: Timestamp
│   │   ├── subjectProgress: {
│   │   │     "Science": 5,
│   │   │     "Math": 3,
│   │   │     "SocialStudies": 7
│   │   │   }
│   │   │
│   │   ├── completedChapters (Sub-collection)
│   │   │   ├── "Science_1" (Document)
│   │   │   │   ├── chapterId: 1
│   │   │   │   ├── subjectName: "Science"
│   │   │   │   ├── chapterName: "Chemical Reactions and Equations"
│   │   │   │   ├── isCompleted: true
│   │   │   │   ├── bestScore: 13
│   │   │   │   ├── bestPercentage: 86.67
│   │   │   │   ├── totalAttempts: 2
│   │   │   │   ├── lastAttemptDate: Timestamp
│   │   │   │   └── firstCompletionDate: Timestamp
│   │   │   │
│   │   │   ├── "Math_1" (Document)
│   │   │   │   └── ... (same structure)
│   │   │   └── ...
│   │   │
│   │   ├── quizResults (Sub-collection)
│   │   │   ├── {auto_id_1} (Document)
│   │   │   │   ├── subjectName: "Science"
│   │   │   │   ├── chapterId: 1
│   │   │   │   ├── chapterName: "Chemical Reactions..."
│   │   │   │   ├── score: 12
│   │   │   │   ├── totalQuestions: 15
│   │   │   │   ├── percentage: 80.0
│   │   │   │   ├── correctAnswers: 12
│   │   │   │   ├── wrongAnswers: 2
│   │   │   │   ├── skippedAnswers: 1
│   │   │   │   ├── timeTaken: 540000
│   │   │   │   ├── attemptDate: Timestamp
│   │   │   │   ├── attemptNumber: 1
│   │   │   │   └── userAnswers: [0,2,1,3,0,2,1,-1,3,0,2,1,3,0,2]
│   │   │   └── ...
│   │   │
│   │   └── strengthData (Sub-collection)
│   │       ├── "Science" (Document)
│   │       │   ├── subjectName: "Science"
│   │       │   ├── averageScore: 75.5
│   │       │   ├── chaptersAttempted: 5
│   │       │   ├── totalChapters: 16
│   │       │   ├── strongChapters: [1, 3, 5]
│   │       │   ├── weakChapters: [2, 4]
│   │       │   └── lastUpdated: Timestamp
│   │       ├── "Math" (Document)
│   │       │   └── ... (same structure)
│   │       └── "SocialStudies" (Document)
│   │           └── ... (same structure)
│   │
│   └── {userId_2} (Document)
│       └── ... (same structure as above)
│
├── questions (Collection)
│   ├── "Science_1_1" (Document - Subject_Chapter_QuestionNumber)
│   │   ├── questionId: 1
│   │   ├── subjectName: "Science"
│   │   ├── chapterId: 1
│   │   ├── chapterName: "Chemical Reactions and Equations"
│   │   ├── questionText: "Which of the following is..."
│   │   ├── optionA: "Burning of coal"
│   │   ├── optionB: "Decomposition of..."
│   │   ├── optionC: "Burning of magnesium in air"
│   │   ├── optionD: "Digestion of food"
│   │   ├── correctOption: 2 (0=A, 1=B, 2=C, 3=D)
│   │   ├── explanation: "When magnesium burns..."
│   │   └── difficulty: "Medium"
│   │
│   ├── "Science_1_2" (Document)
│   │   └── ... (next question)
│   └── ... (765+ total question documents)
│
└── appConfig (Collection - for future use)
    └── "settings" (Document)
        ├── appVersion: "1.0.0"
        ├── quizTimerMinutes: 15
        ├── questionsPerQuiz: 15
        └── maintenanceMode: false
```

---

# SECTION 5: COMPLETE PROJECT FILE STRUCTURE

---

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/aksharadeep/tutor/
│   │   │   │
│   │   │   ├── activities/
│   │   │   │   ├── SplashActivity.kt
│   │   │   │   ├── LoginActivity.kt
│   │   │   │   ├── SignUpActivity.kt
│   │   │   │   ├── DashboardActivity.kt
│   │   │   │   ├── SyllabusActivity.kt
│   │   │   │   ├── QuizActivity.kt
│   │   │   │   ├── QuizResultActivity.kt
│   │   │   │   ├── ReviewActivity.kt
│   │   │   │   └── StrengthMapActivity.kt
│   │   │   │
│   │   │   ├── adapters/
│   │   │   │   ├── SubjectAdapter.kt
│   │   │   │   ├── ChapterAdapter.kt
│   │   │   │   ├── QuizOptionAdapter.kt
│   │   │   │   └── ReviewQuestionAdapter.kt
│   │   │   │
│   │   │   ├── models/
│   │   │   │   ├── User.kt
│   │   │   │   ├── Subject.kt
│   │   │   │   ├── Chapter.kt
│   │   │   │   ├── QuizQuestion.kt
│   │   │   │   ├── QuizResult.kt
│   │   │   │   └── StrengthData.kt
│   │   │   │
│   │   │   ├── viewmodels/
│   │   │   │   ├── LoginViewModel.kt
│   │   │   │   ├── SignUpViewModel.kt
│   │   │   │   ├── DashboardViewModel.kt
│   │   │   │   ├── SyllabusViewModel.kt
│   │   │   │   ├── QuizViewModel.kt
│   │   │   │   └── StrengthMapViewModel.kt
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── QuizRepository.kt
│   │   │   │   └── StrengthRepository.kt
│   │   │   │
│   │   │   ├── utils/
│   │   │   │   ├── Constants.kt
│   │   │   │   ├── PreferenceManager.kt
│   │   │   │   ├── ValidationUtils.kt
│   │   │   │   ├── ScoreCalculator.kt
│   │   │   │   └── ChartHelper.kt
│   │   │   │
│   │   │   └── data/
│   │   │       ├── QuestionBank.kt (pre-loaded questions)
│   │   │       ├── SubjectData.kt (subject and chapter lists)
│   │   │       └── SampleData.kt (for testing)
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_splash.xml
│   │   │   │   ├── activity_login.xml
│   │   │   │   ├── activity_signup.xml
│   │   │   │   ├── activity_dashboard.xml
│   │   │   │   ├── activity_syllabus.xml
│   │   │   │   ├── activity_quiz.xml
│   │   │   │   ├── activity_quiz_result.xml
│   │   │   │   ├── activity_review.xml
│   │   │   │   ├── activity_strength_map.xml
│   │   │   │   ├── item_subject_card.xml
│   │   │   │   ├── item_chapter.xml
│   │   │   │   ├── item_quiz_option.xml
│   │   │   │   ├── item_review_question.xml
│   │   │   │   └── dialog_submit_quiz.xml
│   │   │   │
│   │   │   ├── drawable/
│   │   │   │   ├── bg_gradient_blue.xml
│   │   │   │   ├── bg_gradient_purple.xml
│   │   │   │   ├── bg_curved_top.xml
│   │   │   │   ├── bg_button_primary.xml
│   │   │   │   ├── bg_button_outlined.xml
│   │   │   │   ├── bg_card_rounded.xml
│   │   │   │   ├── bg_option_selected.xml
│   │   │   │   ├── bg_option_unselected.xml
│   │   │   │   ├── bg_option_correct.xml
│   │   │   │   ├── bg_option_wrong.xml
│   │   │   │   ├── ic_app_logo.xml
│   │   │   │   ├── ic_science.xml
│   │   │   │   ├── ic_math.xml
│   │   │   │   ├── ic_social.xml
│   │   │   │   ├── ic_quiz.xml
│   │   │   │   ├── ic_strength.xml
│   │   │   │   ├── circle_green.xml
│   │   │   │   ├── circle_blue.xml
│   │   │   │   ├── circle_orange.xml
│   │   │   │   └── circle_gray.xml
│   │   │   │
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   ├── strings.xml
│   │   │   │   ├── dimens.xml
│   │   │   │   ├── styles.xml
│   │   │   │   └── themes.xml
│   │   │   │
│   │   │   ├── values-night/
│   │   │   │   └── themes.xml (dark mode - optional)
│   │   │   │
│   │   │   └── font/
│   │   │       ├── poppins_regular.ttf
│   │   │       ├── poppins_medium.ttf
│   │   │       └── poppins_bold.ttf
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── test/ (Unit tests)
│
├── build.gradle (app-level)
├── build.gradle (project-level)
└── google-services.json (Firebase config)
```

---

# SECTION 6: DEPENDENCIES (build.gradle)

---

```gradle
// Project-level build.gradle
plugins {
    id 'com.android.application' version '8.1.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.0' apply false
    id 'com.google.gms.google-services' version '4.4.0' apply false
}

// App-level build.gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
    id 'kotlin-kapt'
}

android {
    namespace 'com.aksharadeep.tutor'
    compileSdk 34
    
    defaultConfig {
        applicationId "com.aksharadeep.tutor"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
    
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    
    // ViewModel & LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'
    
    // MPAndroidChart (for Radar/Spider Web Chart)
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // Splash Screen API
    implementation 'androidx.core:core-splashscreen:1.0.1'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3'
    
    // Lottie Animations (for confetti, loading)
    implementation 'com.airbnb.android:lottie:6.3.0'
    
    // CircularProgressBar
    implementation 'com.mikhaellopez:circularprogressbar:3.1.0'
}
```

---

# SECTION 7: COLOR SCHEME & DESIGN SYSTEM

---

```xml
<!-- colors.xml -->
<resources>
    <!-- Primary Colors -->
    <color name="primary">#1A237E</color>
    <color name="primary_dark">#0D1642</color>
    <color name="primary_light">#283593</color>
    <color name="accent">#4A148C</color>
    
    <!-- Subject Theme Colors -->
    <color name="science_primary">#4CAF50</color>
    <color name="science_light">#E8F5E9</color>
    <color name="math_primary">#2196F3</color>
    <color name="math_light">#E3F2FD</color>
    <color name="social_primary">#FF9800</color>
    <color name="social_light">#FFF3E0</color>
    
    <!-- Result Colors -->
    <color name="correct_green">#43A047</color>
    <color name="correct_light">#E8F5E9</color>
    <color name="wrong_red">#E53935</color>
    <color name="wrong_light">#FFEBEE</color>
    <color name="skipped_gray">#9E9E9E</color>
    <color name="skipped_light">#F5F5F5</color>
    
    <!-- UI Colors -->
    <color name="background">#F5F5F5</color>
    <color name="card_background">#FFFFFF</color>
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="text_hint">#BDBDBD</color>
    <color name="divider">#E0E0E0</color>
    
    <!-- Gradient Colors -->
    <color name="gradient_start">#1A237E</color>
    <color name="gradient_end">#4A148C</color>
    
    <!-- Timer Colors -->
    <color name="timer_normal">#1A237E</color>
    <color name="timer_warning">#E53935</color>
</resources>
```

### Typography
```
- Headings: Poppins Bold, 20-28sp
- Subheadings: Poppins Medium, 16-18sp
- Body text: Poppins Regular, 14-15sp
- Captions: Poppins Regular, 11-13sp
- Button text: Poppins Bold, 14-16sp
```

### Spacing System
```
- Extra Small: 4dp
- Small: 8dp
- Medium: 12dp
- Default: 16dp
- Large: 20dp
- Extra Large: 24dp
- XXL: 32dp
```

### Corner Radius
```
- Small: 8dp (chips, small buttons)
- Default: 12dp (buttons, input fields)
- Large: 16dp (cards, dialogs)
- Full: 50% (circular avatars, circles)
```

---

# SECTION 8: APP LOGIC & ALGORITHMS

---

## 8.1 Authentication Flow
```
1. User opens app → Splash Screen (3 seconds)
2. Check SharedPreferences for "isLoggedIn" flag
3. If true → Check if Firebase user session is valid
4. If valid → Go to Dashboard
5. If not → Go to Login Screen

Login Process:
1. User enters phone + password
2. Query Firestore: users WHERE phoneNumber == input
3. If document exists AND password matches → Login success
4. Save userId, isLoggedIn=true in SharedPreferences
5. If "Remember Me" checked → Save credentials in SharedPreferences
6. Navigate to Dashboard

Sign Up Process:
1. User fills all fields
2. Validate all inputs (client-side)
3. Check Firestore for existing phone number
4. If not exists → Create new user document
5. Initialize subjectProgress to all zeros
6. Navigate to Login with success message
```

## 8.2 Quiz Flow
```
1. User selects chapter → Load 15 questions for that chapter
2. Shuffle question order (randomize)
3. Shuffle option order within each question (optional)
4. Start 15-minute countdown timer
5. User navigates between questions using Next/Previous/Dots
6. Store selected answer for each question in memory
7. On Submit:
   a. Stop timer
   b. Calculate: correct, wrong, unanswered counts
   c. Calculate percentage
   d. Save result to Firestore
   e. Update chapter completion status
   f. Update subject progress
   g. Update strength data
   h. Navigate to Result Screen
8. On Timer Expiry:
   a. Auto-submit with current answers
   b. Same process as step 7
```

## 8.3 Progress Tracking Algorithm
```kotlin
fun calculateSubjectProgress(userId: String, subject: String): Float {
    val totalChapters = when(subject) {
        "Science" -> 16
        "Math" -> 15
        "SocialStudies" -> 20
        else -> 0
    }
    
    val completedChapters = getCompletedChaptersCount(userId, subject)
    return (completedChapters.toFloat() / totalChapters) * 100
}

fun calculateOverallProgress(userId: String): Float {
    val scienceProgress = calculateSubjectProgress(userId, "Science")
    val mathProgress = calculateSubjectProgress(userId, "Math")
    val socialProgress = calculateSubjectProgress(userId, "SocialStudies")
    
    return (scienceProgress + mathProgress + socialProgress) / 3
}
```

## 8.4 Strength Map Algorithm
```kotlin
fun calculateStrengthData(userId: String): RadarChartData {
    val allQuizResults = getallQuizResults(userId)
    
    // Axis 1: Science average
    val scienceAvg = allQuizResults
        .filter { it.subjectName == "Science" }
        .map { it.percentage }
        .average()
        .ifNaN(0.0)
    
    // Axis 2: Math average
    val mathAvg = allQuizResults
        .filter { it.subjectName == "Math" }
        .map { it.percentage }
        .average()
        .ifNaN(0.0)
    
    // Axis 3: Social Studies average
    val socialAvg = allQuizResults
        .filter { it.subjectName == "SocialStudies" }
        .map { it.percentage }
        .average()
        .ifNaN(0.0)
    
    // Axis 4: Accuracy
    val totalCorrect = allQuizResults.sumOf { it.correctAnswers }
    val totalQuestions = allQuizResults.sumOf { it.totalQuestions }
    val accuracy = if (totalQuestions > 0) 
        (totalCorrect.toDouble() / totalQuestions) * 100 else 0.0
    
    // Axis 5: Consistency (days active out of last 30)
    val activeDays = getActiveDaysInLast30Days(userId)
    val consistency = min(100.0, (activeDays.toDouble() / 30) * 100)
    
    // Axis 6: Speed
    val avgTimePerQuestion = allQuizResults
        .map { it.timeTaken.toDouble() / it.totalQuestions / 1000 } // seconds
        .average()
        .ifNaN(60.0)
    val speed = max(0.0, min(100.0, 100 - ((avgTimePerQuestion - 30) / 90 * 100)))
    
    return RadarChartData(
        science = scienceAvg,
        math = mathAvg,
        social = socialAvg,
        accuracy = accuracy,
        consistency = consistency,
        speed = speed
    )
}
```

---

# SECTION 9: SHARED PREFERENCES STRUCTURE

---

```kotlin
// PreferenceManager.kt

object PreferenceKeys {
    const val PREF_NAME = "akshara_deepa_prefs"
    const val IS_LOGGED_IN = "is_logged_in"           // Boolean
    const val USER_ID = "user_id"                       // String
    const val USER_NAME = "user_name"                   // String
    const val USER_PHONE = "user_phone"                 // String
    const val REMEMBER_ME = "remember_me"               // Boolean
    const val SAVED_PHONE = "saved_phone"               // String
    const val SAVED_PASSWORD = "saved_password"          // String
    const val LAST_LOGIN_DATE = "last_login_date"       // String
    const val DAY_STREAK = "day_streak"                 // Int
    const val FIRST_LAUNCH = "first_launch"             // Boolean
}

class PreferenceManager(context: Context) {
    private val prefs = context.getSharedPreferences(
        PreferenceKeys.PREF_NAME, Context.MODE_PRIVATE
    )
    
    fun setLoggedIn(isLoggedIn: Boolean, userId: String, userName: String) {
        prefs.edit().apply {
            putBoolean(PreferenceKeys.IS_LOGGED_IN, isLoggedIn)
            putString(PreferenceKeys.USER_ID, userId)
            putString(PreferenceKeys.USER_NAME, userName)
            apply()
        }
    }
    
    fun isLoggedIn(): Boolean = prefs.getBoolean(PreferenceKeys.IS_LOGGED_IN, false)
    fun getUserId(): String? = prefs.getString(PreferenceKeys.USER_ID, null)
    fun getUserName(): String? = prefs.getString(PreferenceKeys.USER_NAME, null)
    
    fun saveCredentials(phone: String, password: String) {
        prefs.edit().apply {
            putBoolean(PreferenceKeys.REMEMBER_ME, true)
            putString(PreferenceKeys.SAVED_PHONE, phone)
            putString(PreferenceKeys.SAVED_PASSWORD, password)
            apply()
        }
    }
    
    fun getSavedCredentials(): Pair<String?, String?> {
        return Pair(
            prefs.getString(PreferenceKeys.SAVED_PHONE, null),
            prefs.getString(PreferenceKeys.SAVED_PASSWORD, null)
        )
    }
    
    fun logout() {
        val rememberMe = prefs.getBoolean(PreferenceKeys.REMEMBER_ME, false)
        val savedPhone = prefs.getString(PreferenceKeys.SAVED_PHONE, null)
        val savedPassword = prefs.getString(PreferenceKeys.SAVED_PASSWORD, null)
        
        prefs.edit().clear().apply()
        
        if (rememberMe) {
            saveCredentials(savedPhone ?: "", savedPassword ?: "")
        }
    }
}
```

---

# SECTION 10: ANDROIDMANIFEST.XML

---

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.aksharadeep.tutor">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.AksharaDeepaTutor">

        <!-- Splash Screen (Launcher Activity) -->
        <activity
            android:name=".activities.SplashActivity"
            android:exported="true"
            android:theme="@style/Theme.AksharaDeepaTutor.Splash">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Login Screen -->
        <activity android:name=".activities.LoginActivity" />

        <!-- Sign Up Screen -->
        <activity android:name=".activities.SignUpActivity" />

        <!-- Dashboard -->
        <activity android:name=".activities.DashboardActivity" />

        <!-- Syllabus / Chapter List -->
        <activity android:name=".activities.SyllabusActivity" />

        <!-- Quiz Screen -->
        <activity
            android:name=".activities.QuizActivity"
            android:screenOrientation="portrait" />

        <!-- Quiz Result -->
        <activity android:name=".activities.QuizResultActivity" />

        <!-- Review Answers -->
        <activity android:name=".activities.ReviewActivity" />

        <!-- Strength Map -->
        <activity android:name=".activities.StrengthMapActivity" />

    </application>
</manifest>
```

---

# SECTION 11: TESTING CHECKLIST

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

# SECTION 12: DEPLOYMENT & FIREBASE SETUP

---

## Firebase Project Setup Steps
```
1. Go to console.firebase.google.com
2. Create new project: "Akshara-Deepa-Tutor"
3. Add Android app with package name: com.aksharadeep.tutor
4. Download google-services.json and place in app/ folder
5. Enable Firestore Database in test mode (for development)
6. Set Firestore rules for production:

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      match /completedChapters/{chapterId} {
        allow read, write: if request.auth != null;
      }
      
      match /quizResults/{resultId} {
        allow read, write: if request.auth != null;
      }
      
      match /strengthData/{subjectId} {
        allow read, write: if request.auth != null;
      }
    }
    
    // Questions are read-only for all authenticated users
    match /questions/{questionId} {
      allow read: if request.auth != null;
      allow write: if false; // Only admin can write via console
    }
    
    match /appConfig/{configId} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

---

# SECTION 13: IMPACT GOALS & SUCCESS CRITERIA

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

# SECTION 14: FUTURE ENHANCEMENTS (Phase 2 - Optional)

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

# SECTION 15: SUMMARY FOR ANTIGRAVITY

---

## Quick Start Instructions for AntiGravity AI

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

**All data flows, UI designs, database schemas, file structures, algorithms, and navigation flows are detailed above in this SOP document.**

---

