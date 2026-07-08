package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.ui.BazarViewModel
import com.example.ui.components.*
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LuxuryGold

@Composable
fun HomeScreen(
    viewModel: BazarViewModel,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProductDetails: (Product) -> Unit,
    onNavigateToSearchList: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val banners by viewModel.banners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()

    // Derived sections
    val featuredProducts = remember(products) {
        products.take(4) // First 4 items
    }
    val newArrivals = remember(products) {
        products.sortedByDescending { it.createdAt }.take(4)
    }
    val popularProducts = remember(products) {
        products.sortedBy { it.stock }.take(4) // Simulated popular based on low stock / high demand
    }

    val orangeGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF7F0), // Very light soft orange
            Color(0xFFFFE3D1), // Middle warm peach
            Color(0xFFFFC09F)  // Deeper warm orange
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(orangeGradient)
    ) {
        // App title header with warm personalized greeting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (currentUser != null) "Assalamu Alaikum, ${currentUser?.name?.substringBefore(" ")}! 👋" else "Assalamu Alaikum! 👋",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                )
                Text(
                    text = "Nittyo Bazar",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFFFF6F00) // Beautiful brand orange color
                    )
                )
            }
            IconButton(
                onClick = { /* Navigate to Notifications or general Info if desired */ },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications icon"
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Live Search Bar
            PremiumSearchBar(
                query = searchQuery,
                onQueryChange = {
                    viewModel.setSearchQuery(it)
                }
            )

            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No products found for \"$searchQuery\"", color = MaterialTheme.colorScheme.onBackground)
                    }
                } else {
                    val chunked = filteredProducts.chunked(2)
                    chunked.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            for (item in rowItems) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCard(
                                        product = item,
                                        onProductClick = {
                                            viewModel.selectProduct(item)
                                            onNavigateToProductDetails(item)
                                        },
                                        onAddToCartClick = {
                                            viewModel.addToCart(item)
                                            Toast.makeText(context, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {

            // Dynamic Banner Slider
            if (banners.isNotEmpty()) {
                PremiumBannerSlider(
                    banners = banners,
                    onBannerClick = { categoryId ->
                        val matchingCat = categories.find { it.id == categoryId }
                        if (matchingCat != null) {
                            viewModel.selectCategory(matchingCat.name)
                            onNavigateToCategory(matchingCat.name)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Categories Section
            SectionHeader(
                title = "Categories",
                onSeeAllClick = {
                    // Navigate to Category Tab / page
                    onNavigateToCategory("")
                }
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(categories) { category ->
                    CategoryPill(
                        category = category,
                        onClick = {
                            viewModel.selectCategory(category.name)
                            onNavigateToCategory(category.name)
                        }
                    )
                }
            }

            // Featured Products Section (Light Green background, Dark Green text)
            if (featuredProducts.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2F3E5)) // Light green background under featured products
                        .padding(bottom = 12.dp)
                ) {
                    SectionHeader(
                        title = "Featured Products",
                        titleColor = Color(0xFF0A5C36) // Dark green text for Section Header
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(featuredProducts) { product ->
                             ProductCard(
                                 product = product,
                                 onProductClick = {
                                     viewModel.selectProduct(product)
                                     onNavigateToProductDetails(product)
                                 },
                                 onAddToCartClick = {
                                     viewModel.addToCart(product)
                                     Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                                 },
                                 nameColor = Color(0xFF0A5C36), // Dark green product title text
                                 priceColor = Color(0xFF032213), // Dark green price text
                                 containerColor = Color(0xFFF1FAF3), // Very soft fresh mint background for the card inside
                                 buttonColor = Color(0xFF0A5C36) // Dark green add-to-cart button
                             )
                        }
                    }
                }
            }

            // New Arrivals Section
            if (newArrivals.isNotEmpty()) {
                SectionHeader(title = "New Arrivals")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(newArrivals) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = {
                                viewModel.selectProduct(product)
                                onNavigateToProductDetails(product)
                            },
                            onAddToCartClick = {
                                viewModel.addToCart(product)
                                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Popular Products Section
            if (popularProducts.isNotEmpty()) {
                SectionHeader(title = "Popular Outfits")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(popularProducts) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = {
                                viewModel.selectProduct(product)
                                onNavigateToProductDetails(product)
                            },
                            onAddToCartClick = {
                                viewModel.addToCart(product)
                                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            }
            Spacer(modifier = Modifier.height(80.dp)) // Extra padding for bottom bars
        }
    }
}

@Composable
fun CategoryPill(
    category: Category,
    onClick: () -> Unit
) {
    val containerColor = remember(category.name) {
        val nameLower = category.name.lowercase()
        when {
            nameLower.contains("women") -> Color(0xFFFFD8E4)
            nameLower.contains("men") -> Color(0xFFDAE2FF)
            nameLower.contains("shoe") || nameLower.contains("foot") -> Color(0xFFD3EABC)
            else -> Color(0xFFEADDFF)
        }
    }

    val contentColor = remember(category.name) {
        val nameLower = category.name.lowercase()
        when {
            nameLower.contains("women") -> Color(0xFF31111D)
            nameLower.contains("men") -> Color(0xFF001945)
            nameLower.contains("shoe") || nameLower.contains("foot") -> Color(0xFF101F01)
            else -> Color(0xFF21005D)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(containerColor)
                .border(0.5.dp, Color(0xFFCAC4D0).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            ImageLoader(
                imagePath = category.image,
                contentDescription = category.name,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
