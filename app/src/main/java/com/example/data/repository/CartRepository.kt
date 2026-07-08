package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.FirebaseConfig
import com.example.data.local.CartDao
import com.example.data.local.CartEntity
import com.example.data.model.Cart
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

class CartRepository(
    private val context: Context,
    private val cartDao: CartDao
) {
    private val isFirebase = FirebaseConfig.isFirebaseAvailable(context)

    fun getCart(userId: String): Flow<Cart> {
        if (userId.isEmpty()) return flowOf(Cart())

        // In Demo mode or local sync, we read from Room
        val localCartFlow = cartDao.getCart(userId).map { entity ->
            if (entity != null) {
                Cart(userId = entity.userId, items = entity.items, totalPrice = entity.totalPrice)
            } else {
                Cart(userId = userId, items = emptyList(), totalPrice = 0.0)
            }
        }

        if (isFirebase) {
            // Setup real-time firestore listener as well
            val firestoreCartFlow = callbackFlow<Cart> {
                val db = FirebaseFirestore.getInstance()
                val listener = db.collection("cart").document(userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("CartRepository", "Firestore cart stream failed", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val cart = snapshot.toObject(Cart::class.java)
                            if (cart != null) {
                                trySend(cart)
                            }
                        }
                    }
                awaitClose { listener.remove() }
            }
            // Merge both, with preference to firestore but fallback to local
            return merge(localCartFlow, firestoreCartFlow).distinctUntilChanged()
        }

        return localCartFlow
    }

    suspend fun addToCart(userId: String, product: Product, quantity: Int = 1): Result<Unit> {
        if (userId.isEmpty()) return Result.failure(Exception("User not authenticated"))

        return try {
            val currentCart = cartDao.getCartOnce(userId)
            val currentItems = currentCart?.items?.toMutableList() ?: mutableListOf()

            val existingIndex = currentItems.indexOfFirst { it.productId == product.id }
            if (existingIndex != -1) {
                val existingItem = currentItems[existingIndex]
                currentItems[existingIndex] = existingItem.copy(
                    quantity = existingItem.quantity + quantity
                )
            } else {
                currentItems.add(
                    CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.displayPrice,
                        quantity = quantity,
                        image = product.images.firstOrNull() ?: ""
                    )
                )
            }

            val totalPrice = currentItems.sumOf { it.price * it.quantity }
            val updatedCart = Cart(userId = userId, items = currentItems, totalPrice = totalPrice)

            // Save to Room
            cartDao.saveCart(CartEntity(userId, currentItems, totalPrice))

            // Sync with Firebase
            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("cart").document(userId).set(updatedCart).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuantity(userId: String, productId: String, newQuantity: Int): Result<Unit> {
        if (userId.isEmpty() || newQuantity <= 0) return Result.failure(Exception("Invalid inputs"))

        return try {
            val currentCart = cartDao.getCartOnce(userId) ?: return Result.failure(Exception("Cart not found"))
            val currentItems = currentCart.items.toMutableList()

            val index = currentItems.indexOfFirst { it.productId == productId }
            if (index != -1) {
                currentItems[index] = currentItems[index].copy(quantity = newQuantity)
            } else {
                return Result.failure(Exception("Item not found in cart"))
            }

            val totalPrice = currentItems.sumOf { it.price * it.quantity }
            val updatedCart = Cart(userId = userId, items = currentItems, totalPrice = totalPrice)

            // Save to Room
            cartDao.saveCart(CartEntity(userId, currentItems, totalPrice))

            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("cart").document(userId).set(updatedCart).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromCart(userId: String, productId: String): Result<Unit> {
        if (userId.isEmpty()) return Result.failure(Exception("User not authenticated"))

        return try {
            val currentCart = cartDao.getCartOnce(userId) ?: return Result.success(Unit)
            val currentItems = currentCart.items.filterNot { it.productId == productId }

            val totalPrice = currentItems.sumOf { it.price * it.quantity }
            val updatedCart = Cart(userId = userId, items = currentItems, totalPrice = totalPrice)

            // Save to Room
            cartDao.saveCart(CartEntity(userId, currentItems, totalPrice))

            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("cart").document(userId).set(updatedCart).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearCart(userId: String) {
        try {
            cartDao.clearCart(userId)
            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("cart").document(userId).delete().await()
            }
        } catch (e: Exception) {
            Log.e("CartRepository", "Error clearing cart", e)
        }
    }
}
