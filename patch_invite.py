import re

with open("app/src/main/java/com/example/ui/screens/InviteScreen.kt", "r") as f:
    text = f.read()

text = text.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState")

text = text.replace(".padding(24.dp),", ".padding(24.dp)\n                .verticalScroll(rememberScrollState()),")

with open("app/src/main/java/com/example/ui/screens/InviteScreen.kt", "w") as f:
    f.write(text)
