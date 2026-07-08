package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.FirebaseConfig
import com.example.data.local.*
import com.example.data.model.BannerItem
import com.example.data.model.Category
import com.example.data.model.Product
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val context: Context,
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val bannerDao: BannerDao
) {
    private val isFirebase = FirebaseConfig.isFirebaseAvailable(context)

    // Flow of items
    val allProducts: Flow<List<Product>> = productDao.getAllProducts().map { list ->
        list.map { it.toProduct() }
    }

    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories().map { list ->
        list.map { it.toCategory() }
    }

    val allBanners: Flow<List<BannerItem>> = bannerDao.getAllBanners().map { list ->
        list.map { it.toBannerItem() }
    }

    init {
        // Run seed in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedDatabase()
            } catch (e: Exception) {
                android.util.Log.e("ProductRepository", "Error seeding DB: ${e.message}", e)
            }
            if (isFirebase) {
                try {
                    syncWithFirebaseFirestore()
                } catch (e: Exception) {
                    android.util.Log.e("ProductRepository", "Error syncing Firebase: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun seedDatabase() {

        
            val currentProducts = productDao.getAllProducts().first()
            if (currentProducts.isEmpty()) {
                Log.d("ProductRepository", "Seeding database with default fashion items...")

                // 1. Seed Categories with Custom Generated Drawables
                val defaultCategories = listOf(
                    Category("cat_mens", "Men's Fashion", "img_category_mens_1782879516668"),
                    Category("cat_womens", "Women's Fashion", "img_category_womens_1782879530397"),
                    Category("cat_accessories", "Accessories", "img_app_logo_1782884845927"), // fallback
                    Category("cat_festive", "Festive Wear", "img_banner_clothing_ethnic_1782879506686")
                )
                categoryDao.insertCategories(defaultCategories.map { CategoryEntity.fromCategory(it) })

                // 2. Seed Banners
                val defaultBanners = listOf(
                    BannerItem("img_banner_clothing_summer_1782879497131", "cat_mens"),
                    BannerItem("img_banner_clothing_ethnic_1782879506686", "cat_festive")
                )
                bannerDao.insertBanners(defaultBanners.map { BannerEntity.fromBannerItem(it) })

                // 3. Seed Products
                val defaultProducts = listOf(
                    Product(
                        id = "prod_linen_shirt",
                        name = "Classic Linen Summer Shirt",
                        price = 1500.0,
                        discountPrice = 1200.0,
                        images = listOf("img_category_mens_1782879516668"),
                        category = "Men's Fashion",
                        description = "Stay cool and comfortable in this premium breathable linen shirt. Perfect for casual summer outings.",
                        stock = 15,
                        createdAt = System.currentTimeMillis() - 86400000 * 2
                    ),
                    Product(
                        id = "prod_floral_dress",
                        name = "Chic Contemporary Floral Dress",
                        price = 2500.0,
                        discountPrice = 1999.0,
                        images = listOf("img_category_womens_1782879530397"),
                        category = "Women's Fashion",
                        description = "Beautiful pastel-pink floral dress made with high-quality georgette fabric. Ideal for daytime brunches and semi-formal wear.",
                        stock = 20,
                        createdAt = System.currentTimeMillis() - 86400000 * 1
                    ),
                    Product(
                        id = "prod_cotton_kurti",
                        name = "Traditional Indigo Cotton Kurti",
                        price = 1800.0,
                        discountPrice = 1450.0,
                        images = listOf("img_banner_clothing_ethnic_1782879506686"),
                        category = "Festive Wear",
                        description = "Elegant handcrafted indigo print cotton kurti with sophisticated embroidery around the neck and sleeves.",
                        stock = 10,
                        createdAt = System.currentTimeMillis() - 86400000 * 3
                    ),
                    Product(
                        id = "prod_denim_jacket",
                        name = "Slim Fit Classic Denim Jacket",
                        price = 3200.0,
                        discountPrice = 2800.0,
                        images = listOf("img_category_mens_1782879516668"),
                        category = "Men's Fashion",
                        description = "Heavy-duty rugged denim jacket featuring double stitching and metallic buttons. Timeless styling with double breast pockets.",
                        stock = 8,
                        createdAt = System.currentTimeMillis() - 86400000 * 4
                    ),
                    Product(
                        id = "prod_silk_saree",
                        name = "Handwoven Jamdani Silk Saree",
                        price = 8500.0,
                        discountPrice = 7200.0,
                        images = listOf("img_banner_clothing_ethnic_1782879506686"),
                        category = "Festive Wear",
                        description = "An exquisite, premium handloomed Jamdani silk saree with beautiful golden zari work. Perfect for glamorous festive celebrations.",
                        stock = 5,
                        createdAt = System.currentTimeMillis() - 86400000 * 5
                    ),
                    Product(
                        id = "prod_messenger_bag",
                        name = "Genuine Leather Messenger Bag",
                        price = 3500.0,
                        discountPrice = 2950.0,
                        images = listOf("img_app_logo_1782884845927"),
                        category = "Accessories",
                        description = "Premium genuine leather bag with multiple secure compartments. Fits a 14-inch laptop. Designed for both utility and contemporary style.",
                        stock = 12,
                        createdAt = System.currentTimeMillis() - 86400000 * 6
                    ),
                    Product(
                        id = "prod_sunglasses",
                        name = "Classic Retro Polarized Sunglasses",
                        price = 1200.0,
                        discountPrice = 999.0,
                        images = listOf("img_app_logo_1782884845927"),
                        category = "Accessories",
                        description = "UV400 protection polarized sunglasses with a durable matte black polycarbonate frame. Timeless vintage look suitable for all face shapes.",
                        stock = 30,
                        createdAt = System.currentTimeMillis() - 86400000 * 7
                    )
                )
                productDao.insertProducts(defaultProducts.map { ProductEntity.fromProduct(it) })
                Log.d("ProductRepository", "Database seed completed successfully.")
        }
    }

    private suspend fun syncWithFirebaseFirestore() {
        try {
            val db = FirebaseFirestore.getInstance()
            
            // Sync categories from Firestore
            val catSnap = db.collection("categories").get().await()
            if (!catSnap.isEmpty) {
                val firestoreCats = catSnap.documents.mapNotNull { doc ->
                    val cat = doc.toObject(Category::class.java)
                    cat?.copy(id = doc.id)
                }
                categoryDao.insertCategories(firestoreCats.map { CategoryEntity.fromCategory(it) })
            }

            // Sync products from Firestore
            val prodSnap = db.collection("products").get().await()
            if (!prodSnap.isEmpty) {
                val firestoreProds = prodSnap.documents.mapNotNull { doc ->
                    val prod = doc.toObject(Product::class.java)
                    prod?.copy(id = doc.id)
                }
                productDao.insertProducts(firestoreProds.map { ProductEntity.fromProduct(it) })
            }
        } catch (e: Exception) {
            Log.w("ProductRepository", "Firestore sync skipped or failed: ${e.message}")
        }
    }

    suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toProduct()
    }

    fun getProductsByCategory(category: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(category).map { list ->
            list.map { it.toProduct() }
        }
    }

    // Admin Panel Methods
    suspend fun addProduct(product: Product): Result<Unit> {
        val entity = ProductEntity.fromProduct(product)
        return try {
            productDao.insertProducts(listOf(entity))
            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(product.id.ifEmpty { db.collection("products").document().id }).set(product).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            productDao.deleteProduct(productId)
            if (isFirebase) {
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(productId).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStock(productId: String, newStock: Int) {
        productDao.updateStock(productId, newStock)
        if (isFirebase) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(productId).update("stock", newStock).await()
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error updating Firestore stock", e)
            }
        }
    }
}
