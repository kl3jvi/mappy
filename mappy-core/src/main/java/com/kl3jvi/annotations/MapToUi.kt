package com.kl3jvi.annotations

import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class MapToDomain(
    val targetClass: KClass<*>,
    val excludeFields: Array<String> = [],
    val editableFields: Array<String> = []
)
