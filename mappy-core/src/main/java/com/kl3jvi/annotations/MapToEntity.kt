package com.kl3jvi.annotations

import kotlin.reflect.KClass

/* This annotation is used to map a Kotlin data class to a Database Entity. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapToEntity(
    val targetClass: KClass<*>,
    val excludeFields: Array<String> = [],
    val editableFields: Array<String> = []
)



