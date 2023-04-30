package com.kl3jvi.aprocessor.internal

import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import javax.lang.model.element.Element

sealed interface Type {
    data class Domain(
        val domain: Class<MapToDomain>,
        val element: Element
    ) : Type

    data class Entity(
        val entity: Class<MapToEntity>,
        val element: Element
    ) : Type
}
