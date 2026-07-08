package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.FirebaseConfig
import com.example.data.local.UserDao
import com.example.data.local.UserEntity
import com.example.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository(
    private val context: Context,
    private val userDao: UserDao
) {
    private val isFirebase = FirebaseConfig.isFirebaseAvailable(context)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _isDemoMode = MutableStateFlow(!isFirebase)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode

    init {
        // Check initial auth state
        if (isFirebase) {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    // Load user profile from firestore
                    val uid = firebaseUser.uid
                    _currentUser.value = User(
                        uid = uid,
                        name = firebaseUser.displayName ?: "User",
                        email = firebaseUser.email ?: ""
                    )
                    // Fetch details asynchronously in background
                    fetchFirebaseUserProfile(uid)
                } else {
                    _currentUser.value = null
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error reading FirebaseAuth", e)
                _isDemoMode.value = true
                loadDemoUser()
            }
        } else {
            loadDemoUser()
        }
    }

    private fun loadDemoUser() {
        // For Demo Mode, try to read the last active demo user, or set to null
        val sharedPrefs = context.getSharedPreferences("nittyo_bazar_prefs", Context.MODE_PRIVATE)
        val savedUid = sharedPrefs.getString("demo_user_uid", null)
        if (savedUid != null) {
            // We can set a temporary demo user
            val name = sharedPrefs.getString("demo_user_name", "Demo User") ?: "Demo User"
            val email = sharedPrefs.getString("demo_user_email", "demo@example.com") ?: "demo@example.com"
            val phone = sharedPrefs.getString("demo_user_phone", "+8801799929357") ?: "+8801799929357"
            val address = sharedPrefs.getString("demo_user_address", "Rajshahi, Bangladesh") ?: "Rajshahi, Bangladesh"
            val role = sharedPrefs.getString("demo_user_role", "user") ?: "user"
            _currentUser.value = User(
                uid = savedUid,
                name = name,
                email = email,
                phone = phone,
                address = address,
                role = role
            )
        } else {
            _currentUser.value = null
        }
    }

    private fun fetchFirebaseUserProfile(uid: String) {
        // 1. Immediately load cached user from Room so it shows perfectly on profile page instantly
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cachedUser = userDao.getUser(uid)?.toUser()
                if (cachedUser != null) {
                    _currentUser.value = cachedUser
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error reading local cached user", e)
            }
        }

        if (!isFirebase) return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        var user = document.toObject(User::class.java)
                        if (user != null) {
                            if (user.email.contains("admin") && user.role != "admin") {
                                user = user.copy(role = "admin")
                                db.collection("users").document(uid).set(user)
                            }
                            _currentUser.value = user
                            // Sync to Room cache
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    userDao.insertUser(UserEntity.fromUser(user))
                                } catch (e: Exception) {
                                    Log.e("AuthRepository", "Error caching fetched user", e)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AuthRepository", "Error fetching user profile", e)
                }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception in profile fetch", e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return if (!_isDemoMode.value) {
            try {
                val authResult = FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception("User is null")
                
                // Fetch user data from firestore
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(firebaseUser.uid).get().await()
                var user = doc.toObject(User::class.java) ?: User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Fashion Lover",
                    email = firebaseUser.email ?: email
                )
                
                // Ensure admin role for demo/testing
                if (user.email.contains("admin") && user.role != "admin") {
                    user = user.copy(role = "admin")
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                }
                
                _currentUser.value = user
                // Cache user locally
                userDao.insertUser(UserEntity.fromUser(user))
                Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Firebase sign in failed", e)
                Result.failure(e)
            }
        } else {
            // Demo Login
            val sharedPrefs = context.getSharedPreferences("nittyo_bazar_prefs", Context.MODE_PRIVATE)
            val savedEmail = sharedPrefs.getString("demo_user_email", "demo@example.com")
            
            // Check if user exists in local database or fallback to default
            val localUser = userDao.getUser("demo_uid_123")?.toUser() ?: User(
                uid = "demo_uid_123",
                name = "Mr. Bazar",
                email = email.ifEmpty { "demo@example.com" },
                phone = "+8801700000000",
                address = "Gulshan, Dhaka",
                role = if (email.contains("admin")) "admin" else "user" // Simple role assignment for Demo
            )
            
            if (email.isNotEmpty() && email != localUser.email) {
                // If logging in with a new email in demo mode, create dynamic demo user
                val newUser = User(
                    uid = "demo_uid_${email.hashCode()}",
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = email,
                    phone = "+8801712345678",
                    address = "Mirpur, Dhaka",
                    role = if (email.contains("admin")) "admin" else "user"
                )
                saveDemoSession(newUser)
                Result.success(newUser)
            } else {
                saveDemoSession(localUser)
                Result.success(localUser)
            }
        }
    }

    suspend fun register(name: String, email: String, phone: String, address: String, password: String, role: String = "user"): Result<User> {
        val registerPassword = password.ifEmpty { "NittyoBazar123!" }
        return if (!_isDemoMode.value) {
            try {
                // For real firebase, we use the custom password entered by the user
                val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, registerPassword).await()
                val firebaseUser = authResult.user ?: throw Exception("Registration failed")
                
                val user = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    phone = phone,
                    address = address,
                    role = role,
                    createdAt = System.currentTimeMillis()
                )
                
                // Write user details to Firestore
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(firebaseUser.uid).set(user).await()
                
                _currentUser.value = user
                // Cache user locally
                userDao.insertUser(UserEntity.fromUser(user))
                Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Firebase signup failed", e)
                Result.failure(e)
            }
        } else {
            // Demo signup
            val newUser = User(
                uid = "demo_uid_${email.hashCode()}",
                name = name,
                email = email,
                phone = phone,
                address = address,
                role = role,
                createdAt = System.currentTimeMillis()
            )
            saveDemoSession(newUser)
            Result.success(newUser)
        }
    }

    private suspend fun saveDemoSession(user: User) {
        val sharedPrefs = context.getSharedPreferences("nittyo_bazar_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            putString("demo_user_uid", user.uid)
            putString("demo_user_name", user.name)
            putString("demo_user_email", user.email)
            putString("demo_user_phone", user.phone)
            putString("demo_user_address", user.address)
            putString("demo_user_role", user.role)
            apply()
        }
        _currentUser.value = user
        userDao.insertUser(UserEntity.fromUser(user))
    }

    suspend fun forgotPassword(email: String): Result<Unit> {
        return if (!_isDemoMode.value) {
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            // Simulated Success in Demo mode
            Result.success(Unit)
        }
    }

    suspend fun logout() {
        if (!_isDemoMode.value) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Firebase logout error", e)
            }
        }
        
        // Clear cached session
        val sharedPrefs = context.getSharedPreferences("nittyo_bazar_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().apply {
            remove("demo_user_uid")
            remove("demo_user_name")
            remove("demo_user_email")
            remove("demo_user_phone")
            remove("demo_user_address")
            remove("demo_user_role")
            apply()
        }
        _currentUser.value = null
    }

    suspend fun updateProfile(name: String, phone: String, address: String): Result<User> {
        val current = _currentUser.value ?: return Result.failure(Exception("Not logged in"))
        val updated = current.copy(name = name, phone = phone, address = address)
        
        return if (!_isDemoMode.value) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(current.uid).set(updated).await()
                _currentUser.value = updated
                userDao.insertUser(UserEntity.fromUser(updated))
                Result.success(updated)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            saveDemoSession(updated)
            Result.success(updated)
        }
    }
}
