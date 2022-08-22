package com.kl3jvi.aprocessor

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

data class AnnotationParams(
    val name: TypeName,
    val excludeFields: Array<String>,
    val editableFields: Array<String>,
    val parameterIterable: Iterable<ParameterSpec>
)