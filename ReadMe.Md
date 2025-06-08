📱 PennyWise 
A comprehensive, user-friendly budgeting app built for the PROG7313 Portfolio of Evidence.

🌟 Purpose
PennyWise is designed to empower users to take control of their finances by simplifying the budgeting process. It helps users visualize spending, maintain control over monthly goals, and develop better money habits using interactive tools like financial quizzes, progress feedback, and smart notifications.

🧠 Core Features
Feature	Description
🔐 Secure Login	Firebase Authentication ensures users can securely register and log in.
🏷️ Custom Categories	Create personalized categories for expenses like Groceries, Rent, etc.
💸 Expense Logging	Record amount, date, description, category, and optional receipt photo.
🎯 Budget Goals	Set monthly goals per category (min & max) to maintain financial discipline.
📆 History Filtering	View past expenses across selected time periods for easier auditing.
📊 Bar Graph Insights	Visualize category spending vs. goals with color-coded bars and limits.
🧩 Gamified Quiz	Interactive financial quiz (Optional Feature) stored in Firestore to promote money literacy.
🔔 Notification Center	(Optional Feature) Real-time reminders and feedback fetched from Firebase Cloud Messaging.
⚙️ Settings Screen	(Optional Feature) – Manage profile, toggle notifications, and log out.
☁️ Cloud Syncing	Firebase Firestore stores user data, accessible across multiple devices.
📱 Mobile-First Design	Designed for a smooth and responsive Android experience.

🚀 Innovative Features

🎓 1. Gamified Financial Quiz
Randomized quiz pulled from Firestore.
Scores saved per user for ongoing tracking.
Boosts user engagement & financial awareness.

🔔 2. Notification System
Firebase Cloud Messaging used for real-time alerts.
Reminds users to log expenses, try quizzes, or avoid overspending.

⚙️ 3. Advanced Settings Panel
Not part of the original rubric – added by developers.
Lets users:
Update profile info
Log out securely
Manage app notification preferences
Enhances user control and experience with a clean UI.

📱 How to Use PennyWise
Register or Login
Create an account or log in securely using Firebase Auth.

Set Up Categories
Create or select spending categories like Food, Rent, or Transport.

Add Transactions
Log expenses or income by providing amount, category, and optional receipt photo.

Set Budget Goals
Define min/max limits for each category per month.

Track Your Spending
Use the dashboard, graphs, and colored progress bars to monitor financial health.

Stay Informed
Check the Notification tab for reminders or activity alerts.

Learn & Earn
Take financial literacy quizzes to boost your budgeting IQ.

Adjust Settings
Customize your preferences anytime in the optional Settings screen.

🛠️ Development Stack
Language: Kotlin
Platform: Android
Database: Firebase Firestore
Auth: Firebase Authentication
Storage: Firebase Storage (for receipt images)
Notifications: Firebase Cloud Messaging
CI/CD: GitHub Actions

🧑‍💻 GitHub & GitHub Actions
📂 Version Control
The PennyWise team utilized GitHub for:
Branching strategies
Merge reviews and collaboration
Issue tracking for features and bugs

🔄 GitHub Actions
CI/CD is automated using GitHub Actions
Every push runs unit and UI tests
Build status shown directly on PRs
Ensures stable deployments
yaml
Copy
Edit
name: Android CI
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK
      uses: actions/setup-java@v3
      with:
        java-version: '17'
    - name: Build with Gradle
      run: ./gradlew build
📷 Suggested Images to Include
Image Purpose	File Name Suggestion	Description
App Banner	banner.png	Clean image with app name + slogan
Dashboard Screenshot	dashboard.png	Show bar graphs with colored progress indicators
Quiz in Action	quiz.png	One or two questions with options and score UI
Notifications Page	notifications.png	Show notification reminders or updates
Expense Logging Screen	add_expense.png	Form where user logs a new transaction
Settings Screen	settings.png	UI showing notification toggles, update profile, and logout options
GitHub Actions Workflow UI	ci_cd_github_actions.png	Screenshot of GitHub Actions build status or YAML workflow in action

📺 Demo Video
Watch the app in action on YouTube:
https://youtu.be/QvAx7np2zYQ

📌 Contributors
Ade-Eza 
Reaobaka 
Itumeleng
Lusanda



