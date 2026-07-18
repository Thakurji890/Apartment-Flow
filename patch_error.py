with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "r") as f:
    text = f.read()

target = """                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = context.getString(R.string.login_google_error)
                                        Log.e("LoginScreen", "Google Sign-In Error", e)
                                    }"""

replacement = """                                    } catch (e: Exception) {
                                        isLoading = false
                                        val errorMsg = e.localizedMessage ?: e.message ?: e.toString()
                                        errorMessage = "${context.getString(R.string.login_google_error)}\n$errorMsg"
                                        Log.e("LoginScreen", "Google Sign-In Error", e)
                                    }"""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/ui/screens/LoginScreen.kt", "w") as f:
    f.write(text)
