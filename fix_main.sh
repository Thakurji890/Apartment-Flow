sed -i '/prefilledAmountStr = amount.toString()/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/},/d' app/src/main/java/com/example/MainActivity.kt
sed -i '/onNavigateToInvite = {/,/showSettleDialog = true/d' app/src/main/java/com/example/MainActivity.kt
