# Question Bank Import

The app now supports a maintainable local question-bank import path.

## File

Edit:

```text
app/src/main/assets/questions_seed.json
```

The app imports this JSON into SQLite table `question_bank` when the database is created or upgraded. If a chapter does not have curated JSON questions, the app fills the gap with generated concept-based fallback questions.

## JSON Format

```json
{
  "schemaVersion": 1,
  "questions": [
    {
      "subjectKey": "Science",
      "chapterId": 11,
      "prompt": "Electricity: What does electric current measure?",
      "options": [
        "The rate of flow of electric charge",
        "The total length of a wire",
        "The colour of insulation on a wire",
        "The amount of light falling on a surface"
      ],
      "correctIndex": 0,
      "explanation": "Electric current is defined as the rate of flow of electric charge through a conductor.",
      "difficulty": "basic"
    }
  ]
}
```

## Subject Keys

- `Science`
- `Math`
- `SocialStudies`

## Chapter IDs

Use the order shown in the app syllabus. For example, Science chapter 11 is `Electricity`, Math chapter 15 is `Probability`, and Social Studies chapter 2 is `Nationalism in India`.

## Important

If the app is already installed and you change `questions_seed.json`, uninstall/reinstall the app or bump `DB_VERSION` in `AppData.kt` so SQLite imports the new seed data.