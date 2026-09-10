with open("app/build.gradle.kts", "r") as f:
    content = f.read()

deps = """
  implementation(libs.androidx.work.runtime.ktx)
  implementation(libs.androidx.hilt.work)
  ksp(libs.androidx.hilt.compiler)
"""

if "androidx.work.runtime.ktx" not in content:
    content = content.replace("implementation(libs.androidx.core.ktx)", "implementation(libs.androidx.core.ktx)" + deps)
    
with open("app/build.gradle.kts", "w") as f:
    f.write(content)
