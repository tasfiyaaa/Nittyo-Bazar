import re

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    '''    private suspend fun seedDatabase() {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSeeded = sharedPrefs.getBoolean("is_seeded", false)
        if (true) {
            val currentProducts = productDao.getAllProducts().first()
            if (currentProducts.isEmpty()) {''',
    '''    private suspend fun seedDatabase() {
        val currentProducts = productDao.getAllProducts().first()
        if (currentProducts.isEmpty()) {'''
)

content = content.replace(
    '''            }
            sharedPrefs.edit().putBoolean("is_seeded", true).apply()
        }
    }''',
    '''        }
    }'''
)

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "w") as f:
    f.write(content)
