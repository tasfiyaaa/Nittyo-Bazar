package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val role: String = "user", // "user" or "admin"
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val images: List<String> = emptyList(),
    val category: String = "",
    val description: String = "",
    val stock: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val displayPrice: Double
        get() = if (discountPrice > 0 && discountPrice < price) discountPrice else price
}

@JsonClass(generateAdapter = true)
data class Category(
    val id: String = "",
    val name: String = "",
    val image: String = "" // Drawable name or URL
)

@JsonClass(generateAdapter = true)
data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val image: String = ""
)

@JsonClass(generateAdapter = true)
data class Cart(
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class Order(
    val orderId: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val address: String = "",
    val status: String = "Pending", // "Pending", "Processing", "Shipped", "Delivered"
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class BannerItem(
    val image: String = "", // Drawable resource name or URL
    val link: String = ""
)
