sed -i '/fun AddBillDialog/,/}/ {
    s/\.fillMaxWidth()/\.fillMaxWidth()\n                        \.verticalScroll(rememberScrollState())/g
}' app/src/main/java/com/example/ui/screens/BillsScreen.kt
sed -i '/import androidx.compose.foundation.layout.*/a \import androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.verticalScroll' app/src/main/java/com/example/ui/screens/BillsScreen.kt
