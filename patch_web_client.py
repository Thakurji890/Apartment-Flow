import re

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

text = text.replace("266945182431-70r532i6v7o9804jcbna732b1o4h8mvl.apps.googleusercontent.com", "266945182431-oessbuar90es73ri7cevu46pjo1co77p.apps.googleusercontent.com")

error_str = 'errorMessage = "Unexpected credential type received"'
replacement = 'errorMessage = "Unexpected credential: ${credential.javaClass.simpleName}"'
text = text.replace(error_str, replacement)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
