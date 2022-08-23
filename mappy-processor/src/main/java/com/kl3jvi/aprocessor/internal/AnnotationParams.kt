package com.kl3jvi.aprocessor.internal

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName

data class AnnotationParams(
    val name: TypeName,
    val excludeFields: Array<String>,
    val editableFields: Array<String>,
    val parameterIterable: Iterable<ParameterSpec>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AnnotationParams

        if (name != other.name) return false
        if (!excludeFields.contentEquals(other.excludeFields)) return false
        if (!editableFields.contentEquals(other.editableFields)) return false
        if (parameterIterable != other.parameterIterable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + excludeFields.contentHashCode()
        result = 31 * result + editableFields.contentHashCode()
        result = 31 * result + parameterIterable.hashCode()
        return result
    }
}