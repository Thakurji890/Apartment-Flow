with open("app/src/main/java/com/example/core/sync/SyncManager.kt", "r") as f:
    content = f.read()

if "import androidx.work.WorkRequest" not in content:
    content = content.replace("import androidx.work.WorkManager", "import androidx.work.WorkManager\nimport androidx.work.WorkRequest")

with open("app/src/main/java/com/example/core/sync/SyncManager.kt", "w") as f:
    f.write(content)
