with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    text = f.read()

target = """    // Observe activeApartmentId from viewmodel"""

replacement = """    val notifications by viewModel.notifications.collectAsState(initial = emptyList())
    var previousNotifications by remember { mutableStateOf(emptyList<com.example.data.Notification>()) }

    LaunchedEffect(notifications) {
        if (previousNotifications.isNotEmpty()) {
            val newNotifs = notifications.filter { it !in previousNotifications && !it.read }
            newNotifs.forEach { notif ->
                Toast.makeText(context, "${notif.title}: ${notif.message}", Toast.LENGTH_LONG).show()
            }
        }
        previousNotifications = notifications
    }

    // Observe activeApartmentId from viewmodel"""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(text)
