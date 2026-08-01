with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

target = """        // Initialize Firebase
        com.google.firebase.FirebaseApp.initializeApp(this)"""

replacement = """        // Initialize Firebase
        val app = com.google.firebase.FirebaseApp.initializeApp(this)
        if (app == null) {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setProjectId("dummy-project-id")
                .setApplicationId("1:1234567890:android:abcdef")
                .setApiKey("dummy-api-key")
                .build()
            com.google.firebase.FirebaseApp.initializeApp(this, options)
        }"""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
