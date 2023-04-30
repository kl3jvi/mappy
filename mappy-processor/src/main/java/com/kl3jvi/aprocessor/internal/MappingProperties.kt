package com.kl3jvi.aprocessor.internal

import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import javax.lang.model.element.Element
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

data class MappingProperties(
    val targetClass: TypeMirror,
    val excludeFields: Array<String>,
    val editableFields: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MappingProperties

        if (targetClass != other.targetClass) return false
        if (!excludeFields.contentEquals(other.excludeFields)) return false
        if (!editableFields.contentEquals(other.editableFields)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = targetClass.hashCode()
        result = 31 * result + excludeFields.contentHashCode()
        result = 31 * result + editableFields.contentHashCode()
        return result
    }
}

fun MapToDomain.getMappingProperties(element: Element): MappingProperties {
    val targetTypeMirror = try {
        targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }

    return MappingProperties(targetTypeMirror, excludeFields, editableFields)
}

fun MapToEntity.getMappingProperties(element: Element): MappingProperties {
    val targetTypeMirror = try {
        targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }

    return MappingProperties(targetTypeMirror, excludeFields, editableFields)
}
