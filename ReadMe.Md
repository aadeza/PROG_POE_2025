# PROG_POE_2025

A budgeting app designed to help users **track expenses, manage income, and monitor financial goals** efficiently.

##  Features
- **Budget Creation** – Set monthly spending limits and track progress.
- **Expense & Income Logging** – Record transactions and categorize them.
- **Pie Chart Analysis** – Visualize spending breakdowns by category.
- **Custom Date Filters** – Use "Last Minute," "Last Hour," or other presets for transaction history.
- **GitHub Actions CI/CD** – Automated testing on every push.
- **Quiz Feature** – Engage users with financial knowledge quizzes.

## 🔧 Installation
1. **Clone the repository**:
   ```sh
   git clone https://github.com/your-username/PROG_POE_2025.git

Youtube video:
https://www.youtube.com/watch?v=n-obg_rxzGg

PennyWise – Final PoE App

PennyWise is a mobile budgeting app developed for the PROG7313 Portfolio of Evidence. It helps users track expenses, manage financial goals, and improve money habits through engaging features like interactive graphs, quizzes, and smart notifications. This document outlines the app's functionality, Firebase integration, version control practices, and original features as per the project rubric.

App Purpose:

This app is used for empowering users to take control of their personal finances by providing tools that simplify budgeting, visualize spending, and gamify financial awareness. It combines real-time data management with a user-friendly interface to enhance daily money decisions.


Key Features:

User Login (Firebase Auth)- This is used for allowing users to securely register and sign in using Firebase Authentication, ensuring data privacy and session management. 
Category Creation- This is used for letting users create expense categories (e.g., Groceries, Rent), which helps organize spending. 
Expense Logging- This is used for recording expenses with details like date, description, amount, category, and an optional receipt photo. 
Monthly Goals- This is used for setting and tracking minimum and maximum spending goals per category to encourage disciplined budgeting. 
Expense History- This is used for viewing all logged expenses during a selected period, making it easy to track and audit past spending. 
Horizontal Bar Graph- This is used for visualizing the amount spent per category, with minimum and maximum goals represented as horizontal limit lines. 
Smart Progress Bars- This is used for comparing actual spending against set goals using dynamic colors: below min, within range, over max. 
Notifications Tab- This is used for showing real-time updates and reminders, such as new expenses or quiz results, fetched from Firebase. 
Financial Quiz (Gamified)- This is used for promoting financial literacy through interactive quizzes, with scores stored per user in Firestore. 
Settings Screen- This is used for letting users configure notification preferences, update profile info, and log out of the app. 
Firebase Firestore Integration- This is used for storing and syncing user data online so it can be accessed across multiple devices. 
Mobile Deployment- This is used for running the app on a real Android device to demonstrate practical usability and mobile-first design. 

Own Innovative Features:

 1. Financial Literacy Quiz
This feature is used for educating users by presenting them with randomized financial knowledge questions from Firebase. It also stores their quiz history and score to promote continuous learning.

2. Smart Visual Feedback (Progress Bars)
This feature is used for helping users understand their financial performance visually. Colored progress bars give instant feedback on how close they are to exceeding or meeting goals, motivating corrective actions.


