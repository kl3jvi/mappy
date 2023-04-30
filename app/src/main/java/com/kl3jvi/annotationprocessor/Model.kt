package com.kl3jvi.annotationprocessor

import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity

@MapToEntity(targetClass = TestEntity::class)
data class Test(
    val naruto: String
)

data class TestEntity(
    val naruto: String
)

@MapToEntity(targetClass = RestaurantEntity::class)
@MapToDomain(targetClass = Restaurant::class)
data class NetworkRestaurant(
    val name: String,
    val size: Int,
    val price: String
)

fun main() {
    val a = listOf<NetworkRestaurant>()
}

data class RestaurantEntity(
    val name: String,
    val size: Int,
    val price: String? = null
)

data class Restaurant(
    val name: String,
    val size: Int,
    val price: String? = null
)
