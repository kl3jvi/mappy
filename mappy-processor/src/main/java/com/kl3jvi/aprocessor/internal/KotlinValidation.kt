package com.kl3jvi.aprocessor.internal

import com.kl3jvi.aprocessor.logger.compilerError
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

object KotlinValidation {

    // api

    fun validateDataClass(element: Element): Boolean {
        val metadata = extractKotlinMetadata(element)

        if (metadata == null) {
            errorMustBeKotlinClass(element)
            return false
        }

        if (metadata !is KotlinClassMetadata.Class) {
            //not a class, therefore not a data class as well -> error
            errorMustBeDataClass(element)
            return false
        }


        if (!metadata.isDataClass()) {
            errorMustBeDataClass(element)
            return false
        }

        return true
    }

    // internal

    private fun errorMustBeKotlinClass(element: Element) {
        compilerError("${element.simpleName}: must be a Kotlin data class.")
    }

    private fun errorMustBeDataClass(element: Element) {
        compilerError("${element.simpleName}: must be a data class.")
    }
}