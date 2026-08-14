with open("app/src/main/java/com/example/feature/notification/presentation/list/NotificationViewModel.kt", "r") as f:
    content = f.read()

content = content.replace(
    "is Resource.Error -> {\n                            _state.value = _state.value.copy(\n                                isLoading = false,\n                                error = result.message\n                            )\n                        }",
    "is Resource.Error -> {\n                            _state.value = _state.value.copy(\n                                isLoading = false,\n                                error = result.message\n                            )\n                        }\n                        is Resource.Loading -> {}"
)

with open("app/src/main/java/com/example/feature/notification/presentation/list/NotificationViewModel.kt", "w") as f:
    f.write(content)
