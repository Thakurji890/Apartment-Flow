with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for line in lines:
    if "val creditor = roommates.find { it.id == selectedCreditorId }" in line:
        skip = True
        continue
    
    # Also skip fragments if any
    if skip:
        if "Text(\"Pay via UPI\"," in line:
            # next line is }
            pass
        elif line.strip() == "}" and "Text(\"Pay via UPI\"," in lines[lines.index(line)-1]:
            skip = False
            continue
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "w") as f:
    f.writelines(new_lines)
