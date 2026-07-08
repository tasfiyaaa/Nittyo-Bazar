package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BazarViewModel
import com.example.ui.components.LuxuryButton
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.LuxuryGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: BazarViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val context = LocalContext.current
    val cart by viewModel.cart.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isPlacingOrder by viewModel.isPlacingOrder.collectAsState()
    val orderSuccess by viewModel.orderSuccess.collectAsState()

    var deliveryAddress by remember { mutableStateOf(currentUser?.address ?: "") }
    var userPhone by remember { mutableStateOf(currentUser?.phone ?: "") }

    val baseDeliveryCharge = 100.0
    val totalAmount = cart.totalPrice + baseDeliveryCharge

    // Success Screen routing
    if (orderSuccess != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = GoldPrimary,
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ORDER PLACED!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldPrimary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Thank you for shopping at Nittyo Bazar. Your elegant fashion outfit is being packed for delivery.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Order ID: ${orderSuccess?.orderId}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(32.dp))
                LuxuryButton(
                    text = "TRACK YOUR ORDER",
                    onClick = {
                        viewModel.resetOrderSuccess()
                        onNavigateToOrders()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    } else {
        // Standard Checkout Inputs
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Checkout Details", fontWeight = FontWeight.Bold, color = GoldPrimary) },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Shipping details Title
                Text(
                    text = "SHIPPING ADDRESS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Address Inputs
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Delivery Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = false,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = userPhone,
                    onValueChange = { userPhone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contact Phone") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        focusedLabelColor = GoldPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Payment Method Card
                Text(
                    text = "PAYMENT METHOD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "COD",
                            tint = GoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Cash on Delivery (COD Only)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Pay securely in cash upon receiving your clothing order.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Order summary breakdown
                Text(
                    text = "ORDER SUMMARY",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = GoldPrimary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        cart.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.name} (x${item.quantity})",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "৳${(item.price * item.quantity).toInt()}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Text("৳${cart.totalPrice.toInt()}", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = "Shipping",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Regular Shipping", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                            Text("৳100", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total BDT", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                            Text("৳${totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = LuxuryGold, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Place Order Button
                if (isPlacingOrder) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary)
                    }
                } else {
                    LuxuryButton(
                        text = "CONFIRM & PLACE ORDER",
                        onClick = {
                            if (deliveryAddress.isEmpty() || userPhone.isEmpty()) {
                                Toast.makeText(context, "Please enter all shipping details", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.placeOrder(deliveryAddress) { success, errorMsg ->
                                    if (!success) {
                                        Toast.makeText(context, "Failed to place order: ${errorMsg ?: "Try again"}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
