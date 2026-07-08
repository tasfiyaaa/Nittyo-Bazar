package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.FirebaseConfig
import com.example.data.local.OrderDao
import com.example.data.local.OrderEntity
import com.example.data.local.ProductDao
import com.example.data.model.CartItem
import com.example.data.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrderRepository(
    private val context: Context,
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val cartRepository: CartRepository
) {
    private val isFirebase = FirebaseConfig.isFirebaseAvailable(context)

    fun getOrdersForUser(userId: String): Flow<List<Order>> {
        if (userId.isEmpty()) return flowOf(emptyList())

        val localFlow = orderDao.getOrdersForUser(userId).map { list ->
            list.map { it.toOrder() }
        }

        if (isFirebase) {
            val firestoreFlow = callbackFlow<List<Order>> {
                val db = FirebaseFirestore.getInstance()
                val listener = db.collection("orders")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("OrderRepository", "Firestore orders listener failed", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val orders = snapshot.documents.mapNotNull { doc ->
                                val order = doc.toObject(Order::class.java)
                                order?.copy(orderId = doc.id)
                            }
                            trySend(orders.sortedByDescending { it.createdAt })
                        }
                    }
                awaitClose { listener.remove() }
            }
            return merge(localFlow, firestoreFlow).distinctUntilChanged()
        }

        return localFlow
    }

    fun getAllOrdersAdmin(): Flow<List<Order>> {
        val localFlow = orderDao.getAllOrdersAdmin().map { list ->
            list.map { it.toOrder() }
        }

        if (isFirebase) {
            val firestoreFlow = callbackFlow<List<Order>> {
                val db = FirebaseFirestore.getInstance()
                val listener = db.collection("orders")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("OrderRepository", "Firestore admin orders listener failed", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val orders = snapshot.documents.mapNotNull { doc ->
                                val order = doc.toObject(Order::class.java)
                                order?.copy(orderId = doc.id)
                            }
                            trySend(orders.sortedByDescending { it.createdAt })
                        }
                    }
                awaitClose { listener.remove() }
            }
            return merge(localFlow, firestoreFlow).distinctUntilChanged()
        }

        return localFlow
    }

    suspend fun placeOrder(
        userId: String,
        items: List<CartItem>,
        totalPrice: Double,
        address: String
    ): Result<Order> {
        if (userId.isEmpty() || items.isEmpty()) return Result.failure(Exception("Invalid checkout details"))

        val orderId = "order_" + UUID.randomUUID().toString().substring(0, 8)
        val newOrder = Order(
            orderId = orderId,
            userId = userId,
            items = items,
            totalPrice = totalPrice,
            address = address,
            status = "Pending",
            createdAt = System.currentTimeMillis()
        )

        return try {
            // 1. Insert order locally
            orderDao.insertOrder(OrderEntity.fromOrder(newOrder))

            // 2. Update stock for each product locally and globally
            for (item in items) {
                val productEntity = productDao.getProductById(item.productId)
                if (productEntity != null) {
                    val newStock = (productEntity.stock - item.quantity).coerceAtLeast(0)
                    productDao.updateStock(item.productId, newStock)
                    
                    // Sync stock to Firestore if active
                    if (isFirebase) {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            db.collection("products").document(item.productId).update("stock", newStock).await()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Ignore stock update failures (e.g. permission denied or missing document)
                        }
                    }
                }
            }

            // 3. Sync order to Firestore if active
            if (isFirebase) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("orders").document(orderId).set(newOrder).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // If Firestore fails, we still consider the order placed locally
                }
            }

            // 4. Clear the shopping cart
            cartRepository.clearCart(userId)

            Result.success(newOrder)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            orderDao.updateOrderStatus(orderId, newStatus)
            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("orders").document(orderId).update("status", newStatus).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
