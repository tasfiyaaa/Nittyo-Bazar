package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.LaunchedEffect
import com.example.ui.BazarViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var composeError: Throwable? = null
        try {
            // Early ViewModel creation synchronously in onCreate (outside of setContent) to catch startup errors
            ViewModelProvider(this)[BazarViewModel::class.java]
        } catch (e: Throwable) {
            composeError = e
        }

        setContent {
            if (composeError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F0F0F))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "An unexpected error occurred",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = composeError!!.stackTraceToString(),
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                MyApplicationTheme {
                    // Initialize Central ViewModel
                    val viewModel: BazarViewModel = viewModel()
                    val navController = rememberNavController()

                    val currentUser by viewModel.currentUser.collectAsState()
                    LaunchedEffect(currentUser) {
                        if (currentUser == null) {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            if (currentRoute != null && currentRoute != "splash" && currentRoute != "login" && currentRoute != "register") {
                                navController.navigate("splash") {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "splash"
                        ) {
                        // 1. Splash Screen
                        composable("splash") {
                            SplashScreen(
                                viewModel = viewModel,
                                onNavigateToHome = {
                                    navController.navigate("main_host") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onNavigateToHome = {
                                    navController.navigate("main_host") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Register Screen
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateToLogin = {
                                    navController.popBackStack()
                                },
                                onNavigateToHome = {
                                    navController.navigate("main_host") {
                                        popUpTo("login") { inclusive = true }
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Main Bottom Tab Navigation Host
                        composable("main_host") {
                            MainTabHost(
                                viewModel = viewModel,
                                onNavigateToProductList = { category ->
                                    val safeCategory = if (category.isEmpty()) "all" else category
                                    navController.navigate("product_list/$safeCategory")
                                },
                                onNavigateToProductDetails = { product ->
                                    navController.navigate("product_details")
                                },
                                onNavigateToCheckout = {
                                    navController.navigate("checkout")
                                },
                                onNavigateToAdminPanel = {
                                    navController.navigate("admin_panel")
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("main_host") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. Product Grid List Screen
                        composable(
                            route = "product_list/{categoryName}",
                            arguments = listOf(navArgument("categoryName") { 
                                type = NavType.StringType
                                nullable = true
                            })
                        ) { backStackEntry ->
                            val catName = backStackEntry.arguments?.getString("categoryName")
                            val finalCatName = if (catName == "all") "" else catName
                            ProductListScreen(
                                viewModel = viewModel,
                                initialCategory = finalCatName,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToProductDetails = { product ->
                                    navController.navigate("product_details")
                                }
                            )
                        }

                        // 6. Detailed Item Screen
                        composable("product_details") {
                            val selectedProduct by viewModel.selectedProduct.collectAsState()
                            selectedProduct?.let { product ->
                                ProductDetailsScreen(
                                    viewModel = viewModel,
                                    product = product,
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToCart = {
                                        navController.navigate("main_host") {
                                            popUpTo("main_host") { inclusive = false }
                                        }
                                        // Wait, we need to show Cart. So inside MainTabHost we can route to tab index 2!
                                        // Let's pop back to main_host which will load
                                    }
                                )
                            }
                        }

                        // 7. Checkout Screen
                        composable("checkout") {
                            CheckoutScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToOrders = {
                                    navController.navigate("main_host") {
                                        popUpTo("main_host") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 8. Admin Control Center
                        composable("admin_panel") {
                            AdminPanelScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            try {
                val viewModel = ViewModelProvider(this)[BazarViewModel::class.java]
                viewModel.logout {}
            } catch (e: Throwable) {
                // Ignore
            }
        }
    }
}

