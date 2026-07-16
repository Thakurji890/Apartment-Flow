with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()
text = text.replace("com.google.firebase.FirebaseApp.initializeApp(this)", "try { com.google.firebase.FirebaseApp.initializeApp(this) } catch (e: Exception) {}")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
