with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target = """                                navController.navigate("splash") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }"""

replacement = """                                navController.navigate("splash") {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }"""

content = content.replace(target, replacement)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
