with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "firebase-storage =" not in content:
    content = content.replace("[libraries]", "[libraries]\nfirebase-storage = { group = \"com.google.firebase\", name = \"firebase-storage\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "implementation(libs.firebase.storage)" not in content:
    content = content.replace("implementation(libs.firebase.firestore)", "implementation(libs.firebase.firestore)\n  implementation(libs.firebase.storage)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
