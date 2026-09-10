with open("app/build.gradle.kts", "r") as f:
    content = f.read()

if "isMinifyEnabled = false" in content:
    content = content.replace("isMinifyEnabled = false", "isMinifyEnabled = true")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
