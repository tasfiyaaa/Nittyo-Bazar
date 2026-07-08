import re

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    '''        CoroutineScope(Dispatchers.IO).launch {
            seedDatabase()
            if (isFirebase) {
                syncWithFirebaseFirestore()
            }
        }''',
    '''        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedDatabase()
            } catch (e: Exception) {
                android.util.Log.e("ProductRepository", "Error seeding DB: ${e.message}", e)
            }
            if (isFirebase) {
                try {
                    syncWithFirebaseFirestore()
                } catch (e: Exception) {
                    android.util.Log.e("ProductRepository", "Error syncing Firebase: ${e.message}", e)
                }
            }
        }'''
)

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "w") as f:
    f.write(content)
