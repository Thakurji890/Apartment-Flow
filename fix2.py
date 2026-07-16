import re

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "r") as f:
    text = f.read()

# I will use a regex to replace the fragments
pattern = r'                            val name = android\.net\.Uri\.encode\(creditor\.name\).*?Text\("Pay via UPI", color = MaterialTheme\.colorScheme\.onTertiary\)\n                    }\n                }'

text = re.sub(pattern, '', text, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/SettleUpDialog.kt", "w") as f:
    f.write(text)

