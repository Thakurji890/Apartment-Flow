# BillsShare - Shared Apartment Expense & Bill Tracker

BillsShare is a collaborative Android application built with Jetpack Compose, Kotlin, and Firebase. It allows roommates to track expenses, manage bills, clear balances, and communicate via real-time notifications.

## Firebase & Google Sign-In Setup

To run your own instance of this application, you must configure a Firebase project and set up Google Sign-In:

1. **Create a Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project named `BillsShare` or any name of your choice.

2. **Add an Android App**:
   - Register the Android app in Firebase using your package name / applicationId (as found in `app/build.gradle.kts`).
   - Download the `google-services.json` file.
   - Place the downloaded `google-services.json` file inside the `app/` directory of this project.

3. **Enable Authentication & Database**:
   - In the Firebase Console, enable **Authentication** with **Google Sign-In** and **Anonymous Sign-In** providers.
   - Enable **Cloud Firestore** in your project.

---

## 🔒 Security Best Practices (Critical Checklist)

To ensure the security of your users' data and credentials, follow these mandatory production steps:

### 1. Restrict Your Google Cloud / Firebase API Key
Do not leave your Firebase API key unrestricted in Google Cloud Console:
- Go to the [Google Cloud Console Credentials Page](https://console.cloud.google.com/apis/credentials).
- Find the API Key automatically generated for your Firebase project (typically named `Android key (auto-created by Google Service)`).
- Under **Key restrictions**, choose **API restrictions** and limit the key to only the APIs your app uses (e.g., *Firebase Installations API*, *Identity Toolkit API*, *Cloud Firestore API*).
- Under **Application restrictions**, select **Android apps**:
  - Add your app's exact **Package Name** (e.g., `com.example` or your custom `applicationId`).
  - Add your app's **SHA-1 Fingerprint** (both debug and release fingerprints from your keystores).
  - This prevents other apps or unauthorized clients from using your Firebase credentials.

### 2. Configure Robust Firestore Security Rules
**NEVER leave your Firestore Security Rules in open/test mode in production.** 
Hiding an API key does not protect your Firestore data because the key is compiled into the public APK. The **only** line of defense is Firestore Security Rules. Ensure your rules restrict read/write access to authenticated users belonging to the same apartment.

**Recommended Production Security Rule Pattern:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }

    // Rules for apartments
    match /apartments/{apartmentId} {
      allow read, write: if isAuthenticated();
      
      // Rules for subcollections within an apartment
      match /{document=**} {
        allow read, write: if isAuthenticated();
      }
    }
    
    // Rules for users
    match /users/{userId} {
      allow read, write: if isAuthenticated() && request.auth.uid == userId;
    }
  }
}
```
