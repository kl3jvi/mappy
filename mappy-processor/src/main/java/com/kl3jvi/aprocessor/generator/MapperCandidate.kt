package com.kl3jvi.aprocessor.generator

import com.kl3jvi.aprocessor.internal.extractKotlinMetadata
import com.kl3jvi.aprocessor.logger.compilerError
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class MapperCandidate private constructor(val originatingElement: TypeElement) {
    companion object {
        const val UI_MAPPER_SUFFIX = "UiMapper"
        const val ENTITY_MAPPER_SUFFIX = "EntityMapper"

        fun create(element: Element): MapperCandidate? {
            return if (element is TypeElement) {
                MapperCandidate(element)
            } else {
                compilerError("${element.simpleName} is not a class")
                null
            }
        }
    }

    /* It's extracting Kotlin metadata from the originating element. */
    private val metadata: KotlinClassMetadata = extractKotlinMetadata(originatingElement)!!

    val simpleName by lazy { originatingElement.simpleName.toString() }

}