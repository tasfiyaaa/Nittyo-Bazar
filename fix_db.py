import re

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "r") as f:
    content = f.read()

content = content.replace(
    'val currentProducts = productDao.getAllProducts().first()\n        if (currentProducts.isEmpty()) {',
    'if (true) {'
)

with open("app/src/main/java/com/example/data/repository/ProductRepository.kt", "w") as f:
    f.write(content)
