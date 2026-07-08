with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """                                onNavigateToHome = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                        popUpTo("register") { inclusive = true }
                                    }
                                }"""

replacement = """                                onNavigateToHome = {
                                    navController.navigate("main_host") {
                                        popUpTo("login") { inclusive = true }
                                        popUpTo("register") { inclusive = true }
                                    }
                                }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
