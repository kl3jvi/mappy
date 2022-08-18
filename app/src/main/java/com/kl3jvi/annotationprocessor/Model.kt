package com.kl3jvi.annotationprocessor

import com.kl3jvi.annotations.MapToEntity


@MapToEntity(targetClass = TestEntity::class)
data class Test(
    val naruto: String
)

data class TestEntity(
    val naruto: String
)


