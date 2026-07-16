with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()
text = text.replace("try { com.google.firebase.FirebaseApp.initializeApp(this) } catch (e: Exception) {}", "com.google.firebase.FirebaseApp.initializeApp(this)")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
