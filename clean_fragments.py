import re

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip_count = 0
for i, line in enumerate(lines):
    if skip_count > 0:
        skip_count -= 1
        continue
    
    if "val note = android.net.Uri.encode" in line:
        skip_count = 17 # Skip this line and the next 17 lines which constitute the broken button fragment
        continue
    
    if "if (creditor != null && creditor.upiId.isNotBlank()) {" in line:
        skip_count = 24 # Skip the full button block
        continue

    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "w") as f:
    f.writelines(new_lines)
