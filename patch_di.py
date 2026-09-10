import re

with open("app/src/main/java/com/example/di/AppModule.kt", "r") as f:
    content = f.read()

providers = """    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
"""

content = content.replace(providers, "")

with open("app/src/main/java/com/example/di/AppModule.kt", "w") as f:
    f.write(content)
