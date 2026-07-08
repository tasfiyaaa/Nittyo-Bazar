import re

with open("app/src/main/java/com/example/ui/screens/MainTabHost.kt", "r") as f:
    content = f.read()

content = content.replace(
    '''                1 -> CategoryScreen(
                    viewModel = viewModel,
                    onNavigateToProductList = onNavigateToProductList
                )''',
    '''                1 -> ProductListScreen(
                    viewModel = viewModel,
                    categoryName = "",
                    onNavigateToProductDetails = onNavigateToProductDetails,
                    onNavigateBack = { selectedTab = 0 }
                )'''
)

with open("app/src/main/java/com/example/ui/screens/MainTabHost.kt", "w") as f:
    f.write(content)
