with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target1 = """                                onNavigateToProductList = { category ->
                                    navController.navigate("product_list/$category")
                                },"""

replacement1 = """                                onNavigateToProductList = { category ->
                                    val safeCategory = if (category.isEmpty()) "all" else category
                                    navController.navigate("product_list/$safeCategory")
                                },"""

content = content.replace(target1, replacement1)

target2 = """                        ) { backStackEntry ->
                            val catName = backStackEntry.arguments?.getString("categoryName")
                            ProductListScreen(
                                viewModel = viewModel,
                                initialCategory = catName,"""

replacement2 = """                        ) { backStackEntry ->
                            val catName = backStackEntry.arguments?.getString("categoryName")
                            val finalCatName = if (catName == "all") "" else catName
                            ProductListScreen(
                                viewModel = viewModel,
                                initialCategory = finalCatName,"""

content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
