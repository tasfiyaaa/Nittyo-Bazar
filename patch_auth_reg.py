with open("app/src/main/java/com/example/data/repository/AuthRepository.kt", "r") as f:
    content = f.read()

target1 = """                // _currentUser.value = user // Do not login immediately
                // Cache user locally"""

replacement1 = """                _currentUser.value = user
                // Cache user locally"""

content = content.replace(target1, replacement1)

target2 = """            // saveDemoSession(newUser) // Do not login immediately
            Result.success(newUser)"""

replacement2 = """            saveDemoSession(newUser)
            Result.success(newUser)"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/data/repository/AuthRepository.kt", "w") as f:
    f.write(content)
