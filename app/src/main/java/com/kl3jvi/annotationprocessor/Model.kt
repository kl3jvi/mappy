package com.kl3jvi.annotationprocessor

import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi


@MapToEntity(targetClass = TestEntity::class)
data class Test(
    val naruto: String
)

data class TestEntity(
    val naruto: String
)

@MapToEntity(
    targetClass = RestaurantEntity::class,
    excludeFields = ["name"]
)
@MapToUi(targetClass = Restaurant::class)
data class NetworkRestaurant(
    val name: String, val size: String, val price: String
)

data class RestaurantEntity(
    val name: String, val size: String, val price: String? = null
)

data class Restaurant(
    val name: String, val size: String, val price: String? = null
)



