with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

if "workRuntimeKtx =" not in content:
    content = content.replace("[versions]", "[versions]\nworkRuntimeKtx = \"2.9.0\"\nhiltWork = \"1.2.0\"")

if "androidx-work-runtime-ktx =" not in content:
    content = content.replace("[libraries]", "[libraries]\nandroidx-work-runtime-ktx = { group = \"androidx.work\", name = \"work-runtime-ktx\", version.ref = \"workRuntimeKtx\" }\nandroidx-hilt-work = { group = \"androidx.hilt\", name = \"hilt-work\", version.ref = \"hiltWork\" }\nandroidx-hilt-compiler = { group = \"androidx.hilt\", name = \"hilt-compiler\", version.ref = \"hiltWork\" }")

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)
