package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.data.model.Category
import com.example.data.model.User
import com.example.data.model.Order
import com.example.data.model.BannerItem

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val role: String,
    val createdAt: Long
) {
    fun toUser() = User(uid, name, email, phone, address, role, createdAt)
    companion object {
        fun fromUser(user: User) = UserEntity(user.uid, user.name, user.email, user.phone, user.address, user.role, user.createdAt)
    }
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val discountPrice: Double,
    val images: List<String>,
    val category: String,
    val description: String,
    val stock: Int,
    val createdAt: Long
) {
    fun toProduct() = Product(id, name, price, discountPrice, images, category, description, stock, createdAt)
    companion object {
        fun fromProduct(product: Product) = ProductEntity(
            product.id, product.name, product.price, product.discountPrice,
            product.images, product.category, product.description, product.stock, product.createdAt
        )
    }
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val image: String
) {
    fun toCategory() = Category(id, name, image)
    companion object {
        fun fromCategory(category: Category) = CategoryEntity(category.id, category.name, category.image)
    }
}

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey val userId: String,
    val items: List<CartItem>,
    val totalPrice: Double
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val address: String,
    val status: String,
    val createdAt: Long
) {
    fun toOrder() = Order(orderId, userId, items, totalPrice, address, status, createdAt)
    companion object {
        fun fromOrder(order: Order) = OrderEntity(
            order.orderId, order.userId, order.items, order.totalPrice, order.address, order.status, order.createdAt
        )
    }
}

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey val image: String,
    val link: String
) {
    fun toBannerItem() = BannerItem(image, link)
    companion object {
        fun fromBannerItem(item: BannerItem) = BannerEntity(item.image, item.link)
    }
}
