with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "firebase-appcheck-playintegrity =" not in content:
    content = content.replace("[libraries]", "[libraries]\nfirebase-appcheck-playintegrity = { group = \"com.google.firebase\", name = \"firebase-appcheck-playintegrity\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "implementation(libs.firebase.appcheck.playintegrity)" not in content:
    content = content.replace("implementation(libs.firebase.appcheck.recaptcha)", "implementation(libs.firebase.appcheck.playintegrity)")
    if "implementation(libs.firebase.appcheck.playintegrity)" not in content:
        content = content.replace("implementation(libs.firebase.auth)", "implementation(libs.firebase.auth)\n  implementation(libs.firebase.appcheck.playintegrity)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
