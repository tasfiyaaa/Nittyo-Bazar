package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.BazarViewModel
import com.example.ui.components.LuxuryButton
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LuxuryGold

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.ImageLoader
import androidx.compose.ui.layout.ContentScale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: BazarViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Overview, 1 = Products, 2 = Orders, 3 = Inventory

    val products by viewModel.products.collectAsState()
    val adminOrders by viewModel.adminOrders.collectAsState()

    var showAddProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    // Add/Edit Product Form State
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var discountPrice by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Men's Fashion") }
    var description by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUrl = uri.toString()
        }
    }

    // Pre-fill form when editing
    LaunchedEffect(productToEdit) {
        if (productToEdit != null) {
            val p = productToEdit!!
            name = p.name
            price = p.price.toString()
            discountPrice = p.discountPrice.toString()
            category = p.category
            description = p.description
            stock = p.stock.toString()
            imageUrl = p.images.firstOrNull() ?: ""
            showAddProductDialog = true
        } else {
            name = ""
            price = ""
            discountPrice = ""
            category = "Men's Fashion"
            description = ""
            stock = ""
            imageUrl = ""
        }
    }

    val onDismissDialog = {
        showAddProductDialog = false
        productToEdit = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merchant Admin Dashboard", fontWeight = FontWeight.Bold, color = GoldPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Tab Header Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = GoldPrimary,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GoldPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Products Catalog", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Customer Orders", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Inventory Alerts", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Dashboard Overview Tab
                    AdminDashboardOverview(products = products, orders = adminOrders)
                }
                1 -> {
                    // Products Management Tab
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Search Field
                            var productSearchQuery by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = productSearchQuery,
                                onValueChange = { productSearchQuery = it },
                                label = { Text("Search Products") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    focusedLabelColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            val filteredProducts = if (productSearchQuery.isEmpty()) {
                                products
                            } else {
                                products.filter {
                                    it.name.contains(productSearchQuery, ignoreCase = true) ||
                                    it.category.contains(productSearchQuery, ignoreCase = true)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${filteredProducts.size} Products listed", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                                
                                Button(
                                    onClick = {
                                        productToEdit = null
                                        showAddProductDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Product", fontWeight = FontWeight.Bold)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    AdminProductRow(
                                        product = product,
                                        onEdit = { productToEdit = product },
                                        onDelete = {
                                            viewModel.deleteProduct(product.id)
                                            Toast.makeText(context, "${product.name} deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Orders Management Tab
                    if (adminOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No customer orders have been placed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(adminOrders) { order ->
                                AdminOrderRow(
                                    order = order,
                                    onUpdateStatus = { newStatus ->
                                        viewModel.updateOrderStatus(order.orderId, newStatus)
                                        Toast.makeText(context, "Order status set to $newStatus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                3 -> {
                    AdminInventoryTab(
                        products = products,
                        onEdit = { 
                            productToEdit = it
                            showAddProductDialog = true 
                        }
                    )
                }
            }
        }
    }

    // Add Product Modal Form Dialog
    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(if (productToEdit != null) "Edit Fashion Outfit" else "Add Fashion Outfit", color = GoldPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    // Image Picker Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl.isNotEmpty()) {
                            ImageLoader(
                                imagePath = imageUrl,
                                contentDescription = "Product Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, contentDescription = "Add Image", tint = GoldPrimary, modifier = Modifier.size(32.dp))
                                Text("Select Product Image", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Outfit Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Retail Price (BDT)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = discountPrice,
                        onValueChange = { discountPrice = it },
                        label = { Text("Discount Price (BDT)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Simple category selector dropdown simulation
                    Text("Select Category:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
                    val cats = listOf("Men's Fashion", "Women's Fashion", "Accessories", "Festive Wear")
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                        cats.forEach { catName ->
                            val isSelected = category == catName
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { category = catName }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(catName, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        singleLine = false,
                        maxLines = 3
                    )
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Active Stock Units") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = price.toDoubleOrNull()
                        val dP = discountPrice.toDoubleOrNull() ?: 0.0
                        val s = stock.toIntOrNull()
                        
                        if (name.isNotEmpty() && p != null && s != null) {
                            if (productToEdit != null) {
                                val updatedProduct = productToEdit!!.copy(
                                    name = name,
                                    price = p,
                                    discountPrice = dP,
                                    category = category,
                                    description = description,
                                    stock = s,
                                    images = if (imageUrl.isNotEmpty()) listOf(imageUrl) else productToEdit!!.images
                                )
                                viewModel.updateProduct(updatedProduct) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Outfit updated successfully!", Toast.LENGTH_SHORT).show()
                                        onDismissDialog()
                                    } else {
                                        Toast.makeText(context, "Failed to update item.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                viewModel.addNewProduct(name, p, dP, category, description, s, if (imageUrl.isNotEmpty()) listOf(imageUrl) else listOf("img_category_mens_1782879516668")) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Outfit listed successfully!", Toast.LENGTH_SHORT).show()
                                        onDismissDialog()
                                    } else {
                                        Toast.makeText(context, "Failed to list item.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter all valid entries", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.White)
                ) {
                    Text(if (productToEdit != null) "Update" else "Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDialog) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun AdminProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text("Cat: ${product.category} • Price: ৳${product.displayPrice.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text("Available Stock: ${product.stock} units", color = if (product.stock > 0) Color(0xFF388E3C) else MaterialTheme.colorScheme.error, fontSize = 14.sp)
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = LuxuryGold)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AdminDashboardOverview(products: List<Product>, orders: List<Order>) {
    val todayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayString = todayFormatter.format(Date())

    val todaysOrders = orders.filter { todayFormatter.format(Date(it.createdAt)) == todayString }
    val pendingOrders = orders.filter { it.status.equals("Pending", ignoreCase = true) }
    val uniqueCustomers = orders.map { it.userId }.distinct().count()
    
    val lowStockProducts = products.filter { it.stock in 1..5 }
    val outOfStockProducts = products.filter { it.stock == 0 }

    // Sort products by date descending to find "New Stock Arrived"
    val recentProducts = products.sortedByDescending { it.createdAt }.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(title = "Today's Orders", value = todaysOrders.size.toString(), modifier = Modifier.weight(1f))
                DashboardCard(title = "Pending Orders", value = pendingOrders.size.toString(), modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard(title = "Total Customers", value = uniqueCustomers.toString(), modifier = Modifier.weight(1f))
                DashboardCard(title = "Out of Stock", value = outOfStockProducts.size.toString(), modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inventory Alerts", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Low Stock Products: ${lowStockProducts.size}", color = LuxuryGold, fontWeight = FontWeight.Medium)
                    if (lowStockProducts.isNotEmpty()) {
                        Text(lowStockProducts.take(3).joinToString { it.name }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Recently Added Stock", fontWeight = FontWeight.Bold, color = GoldPrimary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (recentProducts.isEmpty()) {
                        Text("No products available", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                    } else {
                        recentProducts.forEach { prod ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(prod.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                                Text("${prod.stock} units", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = GoldPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun AdminOrderRow(order: Order, onUpdateStatus: (String) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Order: ${order.orderId.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Customer ID: ${order.userId}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }

                Box {
                    Button(
                        onClick = { showMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(order.status.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select status", modifier = Modifier.size(14.dp))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        val stages = listOf("Pending", "Processing", "Shipped", "Delivered")
                        stages.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text(stage, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    onUpdateStatus(stage)
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            // Summary text
            Text("Address: ${order.address}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            Text("Grand Total: ৳${order.totalPrice.toInt()}", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun AdminInventoryTab(products: List<Product>, onEdit: (Product) -> Unit) {
    var filter by remember { mutableStateOf("All") }
    
    val lowStockProducts = products.filter { it.stock in 1..5 }
    val outOfStockProducts = products.filter { it.stock == 0 }
    
    val displayProducts = when(filter) {
        "Low Stock" -> lowStockProducts
        "Out of Stock" -> outOfStockProducts
        else -> products.filter { it.stock <= 5 }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("All Alerts", "Low Stock", "Out of Stock")
            tabs.forEach { tabName ->
                val isSelected = filter == tabName || (filter == "All" && tabName == "All Alerts")
                val actualFilter = if (tabName == "All Alerts") "All" else tabName
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) GoldPrimary else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { filter = actualFilter }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tabName, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        if (displayProducts.isEmpty()) {
             Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                 Text("No inventory alerts.", color = MaterialTheme.colorScheme.onSurfaceVariant)
             }
        } else {
             LazyColumn(
                 modifier = Modifier.weight(1f),
                 contentPadding = PaddingValues(16.dp),
                 verticalArrangement = Arrangement.spacedBy(10.dp)
             ) {
                 items(displayProducts) { product ->
                      AdminInventoryRow(product = product, onRestock = { onEdit(product) })
                 }
             }
        }
    }
}

@Composable
fun AdminInventoryRow(product: Product, onRestock: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(
                    text = if (product.stock == 0) "Out of Stock" else "Low Stock: ${product.stock} left",
                    color = if (product.stock == 0) MaterialTheme.colorScheme.error else LuxuryGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = onRestock,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Restock", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
