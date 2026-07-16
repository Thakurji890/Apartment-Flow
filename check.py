with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt") as f:
    lines = f.readlines()

depth = 0
for i, line in enumerate(lines):
    depth += line.count('{') - line.count('}')
    if depth < 0:
        print(f"Error at line {i+1}: {line.strip()}, depth={depth}")
        break
print(f"Final depth: {depth}")
