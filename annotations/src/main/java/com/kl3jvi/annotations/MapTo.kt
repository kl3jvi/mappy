package com.kl3jvi.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapToEntity(
    val targetClass: KClass<*>,
    val excludeFields: Array<String> = []
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapToUi(
    val excludeFields: Array<String> = []
)



