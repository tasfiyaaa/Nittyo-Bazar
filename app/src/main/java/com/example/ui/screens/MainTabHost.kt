package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.BazarViewModel
import com.example.ui.theme.GoldPrimary

@Composable
fun MainTabHost(
    viewModel: BazarViewModel,
    initialTab: Int = 0,
    onNavigateToProductList: (String) -> Unit,
    onNavigateToProductDetails: (Product) -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF1D192B),
        selectedTextColor = Color(0xFF1D192B),
        indicatorColor = Color(0xFFE8DEF8),
        unselectedIconColor = Color(0xFF49454F),
        unselectedTextColor = Color(0xFF49454F)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF3EDF7),
                contentColor = GoldPrimary
            ) {
                // Home
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = navItemColors
                )
 
                // Categories
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Category else Icons.Outlined.Category,
                            contentDescription = "Categories"
                        )
                    },
                    label = { Text("Shop", fontSize = 11.sp) },
                    colors = navItemColors
                )
 
                // Cart
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.ShoppingBag else Icons.Outlined.ShoppingBag,
                            contentDescription = "Cart"
                        )
                    },
                    label = { Text("Bag", fontSize = 11.sp) },
                    colors = navItemColors
                )
 
                // Orders
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                            contentDescription = "Orders"
                        )
                    },
                    label = { Text("Orders", fontSize = 11.sp) },
                    colors = navItemColors
                )
 
                // Profile
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontSize = 11.sp) },
                    colors = navItemColors
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCategory = { cat ->
                        onNavigateToProductList(cat)
                    },
                    onNavigateToProductDetails = onNavigateToProductDetails,
                    onNavigateToSearchList = {
                        onNavigateToProductList("")
                    }
                )
                1 -> ProductListScreen(
                    viewModel = viewModel,
                    initialCategory = "",
                    onNavigateToProductDetails = onNavigateToProductDetails,
                    onNavigateBack = { selectedTab = 0 }
                )
                2 -> CartScreen(
                    viewModel = viewModel,
                    onNavigateToCheckout = onNavigateToCheckout,
                    onNavigateToBrowse = { selectedTab = 0 }
                )
                3 -> OrdersScreen(
                    viewModel = viewModel,
                    onNavigateToBrowse = { selectedTab = 0 }
                )
                4 -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAdminPanel = onNavigateToAdminPanel,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}
