package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class BazarViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    
    // Repositories
    val authRepository = AuthRepository(application, database.userDao())
    val cartRepository = CartRepository(application, database.cartDao())
    val productRepository = ProductRepository(application, database.productDao(), database.categoryDao(), database.bannerDao())
    val orderRepository = OrderRepository(application, database.orderDao(), database.productDao(), cartRepository)

    // Auth States
    val currentUser: StateFlow<User?> = authRepository.currentUser
    val isDemoMode: StateFlow<Boolean> = authRepository.isDemoMode
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    // Search and Catalog Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _activeCategoryFilter = MutableStateFlow<String?>(null)
    val activeCategoryFilter: StateFlow<String?> = _activeCategoryFilter

    // Dynamic Lists based on queries
    val categories: StateFlow<List<Category>> = productRepository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val banners: StateFlow<List<BannerItem>> = productRepository.allBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products based on search and category
    val filteredProducts: StateFlow<List<Product>> = combine(
        productRepository.allProducts,
        _searchQuery,
        _activeCategoryFilter
    ) { allProds, query, catFilter ->
        allProds.filter { prod ->
            val matchesQuery = prod.name.contains(query, ignoreCase = true) || 
                               prod.description.contains(query, ignoreCase = true)
            val matchesCat = catFilter == null || prod.category.equals(catFilter, ignoreCase = true)
            matchesQuery && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State: Hot-swapped on user login
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val cart: StateFlow<Cart> = currentUser.flatMapLatest { user ->
        if (user != null) {
            cartRepository.getCart(user.uid)
        } else {
            flowOf(Cart())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Cart())

    // Orders State: Hot-swapped on user login
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userOrders: StateFlow<List<Order>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            orderRepository.getOrdersForUser(user.uid)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val adminOrders: StateFlow<List<Order>> = currentUser.flatMapLatest { user ->
        if (user != null && user.role == "admin") {
            orderRepository.getAllOrdersAdmin()
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Detailed Product Viewing and Recommendations
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    val relatedProducts: StateFlow<List<Product>> = combine(
        productRepository.allProducts,
        _selectedProduct
    ) { allProds, selected ->
        if (selected == null) {
            emptyList()
        } else {
            allProds.filter { it.category == selected.category && it.id != selected.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Checkout Flow State
    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder

    private val _orderSuccess = MutableStateFlow<Order?>(null)
    val orderSuccess: StateFlow<Order?> = _orderSuccess

    // Auth Operations
    private fun formatAuthError(throwable: Throwable, defaultMsg: String): String {
        val msg = throwable.localizedMessage ?: ""
        return when {
            msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) -> {
                "Firebase CONFIGURATION_NOT_FOUND:\n" +
                "Please enable 'Email/Password' as a sign-in provider in your Firebase Console (Build > Authentication > Sign-in method)."
            }
            msg.contains("API key not valid", ignoreCase = true) -> {
                "Invalid Firebase API Key:\n" +
                "Please verify that the API key in your 'google-services.json' matches your Firebase project."
            }
            msg.contains("user-not-found", ignoreCase = true) || msg.contains("No user record", ignoreCase = true) -> {
                "No account found with this email. Please register first."
            }
            msg.contains("wrong-password", ignoreCase = true) -> {
                "Incorrect password. Please try again."
            }
            msg.contains("email-already-in-use", ignoreCase = true) -> {
                "This email is already registered. Please sign in instead."
            }
            else -> throwable.localizedMessage ?: defaultMsg
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            authRepository.login(email, password)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _authError.value = formatAuthError(it, "Invalid login credentials.")
                }
        }
    }

    fun register(name: String, email: String, phone: String, address: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            authRepository.register(name, email, phone, address, password)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _authError.value = formatAuthError(it, "Registration failed.")
                }
        }
    }

    fun forgotPassword(email: String, onSent: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            authRepository.forgotPassword(email)
                .onSuccess {
                    _isLoading.value = false
                    onSent()
                }
                .onFailure {
                    _isLoading.value = false
                    _authError.value = formatAuthError(it, "Failed to send reset email.")
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onSuccess()
        }
    }

    fun updateProfile(name: String, phone: String, address: String, onFinished: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.updateProfile(name, phone, address)
                .onSuccess {
                    _isLoading.value = false
                    onFinished(null)
                }
                .onFailure {
                    _isLoading.value = false
                    onFinished(it.localizedMessage ?: "Failed to update profile.")
                }
        }
    }

    // Catalog Operations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryName: String?) {
        _activeCategoryFilter.value = categoryName
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Int = 1) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            cartRepository.addToCart(user.uid, product, quantity)
        }
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            cartRepository.updateQuantity(user.uid, productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            cartRepository.removeFromCart(user.uid, productId)
        }
    }

    // Checkout and Ordering
    fun placeOrder(address: String, onFinished: (Boolean, String?) -> Unit) {
        val user = currentUser.value ?: return
        val currentCart = cart.value
        if (currentCart.items.isEmpty()) return

        viewModelScope.launch {
            _isPlacingOrder.value = true
            orderRepository.placeOrder(
                userId = user.uid,
                items = currentCart.items,
                totalPrice = currentCart.totalPrice + 100.0, // base delivery charge: 100 BDT
                address = address
            ).onSuccess { order ->
                _isPlacingOrder.value = false
                _orderSuccess.value = order
                onFinished(true, null)
            }.onFailure { exception ->
                _isPlacingOrder.value = false
                exception.printStackTrace()
                onFinished(false, exception.message)
            }
        }
    }

    fun resetOrderSuccess() {
        _orderSuccess.value = null
    }

    // Admin Operations
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
        }
    }

    fun addNewProduct(
        name: String,
        price: Double,
        discountPrice: Double,
        category: String,
        description: String,
        stock: Int,
        images: List<String> = listOf("img_category_mens_1782879516668"),
        onFinished: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val newProd = Product(
                id = "prod_" + UUID.randomUUID().toString().substring(0, 8),
                name = name,
                price = price,
                discountPrice = discountPrice,
                images = images,
                category = category,
                description = description,
                stock = stock,
                createdAt = System.currentTimeMillis()
            )
            productRepository.addProduct(newProd)
                .onSuccess { onFinished(true) }
                .onFailure { onFinished(false) }
        }
    }

    fun updateProduct(
        product: Product,
        onFinished: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            productRepository.addProduct(product)
                .onSuccess { onFinished(true) }
                .onFailure { onFinished(false) }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
        }
    }
}
