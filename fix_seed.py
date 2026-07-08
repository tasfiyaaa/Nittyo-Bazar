import re

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val isSeeded = sharedPrefs.getBoolean("is_seeded", false)\n        if (!isSeeded) {\n            val currentProducts = productDao.getAllProducts().first()\n            if (currentProducts.isEmpty()) {',
    'val currentProducts = productDao.getAllProducts().first()\n        if (currentProducts.isEmpty()) {'
)

# Fix the brace matching later down
content = content.replace(
    '                sharedPrefs.edit().putBoolean("is_seeded", true).apply()\n            }\n        }',
    '                sharedPrefs.edit().putBoolean("is_seeded", true).apply()\n        }'
)

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "w") as f:
    f.write(content)
