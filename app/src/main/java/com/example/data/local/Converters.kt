package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CartItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromCartItemList(value: List<CartItem>?): String {
        if (value == null) return "[]"
        val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
        val adapter = moshi.adapter<List<CartItem>>(type)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun toCartItemList(value: String?): List<CartItem> {
        if (value.isNullOrEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, CartItem::class.java)
        val adapter = moshi.adapter<List<CartItem>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }
}
