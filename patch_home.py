with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

target1 = """    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()"""

replacement1 = """    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()"""

content = content.replace(target1, replacement1)

target2 = """            if (searchQuery.isNotEmpty()) {
                val filteredProducts by viewModel.filteredProducts.collectAsState()
                Spacer(modifier = Modifier.height(16.dp))"""

replacement2 = """            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
