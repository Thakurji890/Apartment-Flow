with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if line.strip() == "composable(" and i + 1 < len(lines) and lines[i+1].strip() == "composable(":
        continue
    new_lines.append(line)

with open("app/src/main/java/com/example/navigation/AppNavigation.kt", "w") as f:
    f.writelines(new_lines)
