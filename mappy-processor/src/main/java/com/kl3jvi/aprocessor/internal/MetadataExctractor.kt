package com.kl3jvi.aprocessor.internal

import kotlinx.metadata.Flag
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

/**
 * It extracts the Kotlin class header from the Kotlin class file
 *
 * @param element Element - the element to be processed
 * @return KotlinClassHeader
 */
fun exctractKotlinClassHeader(element: Element): KotlinClassHeader? {
    val annotation: Metadata? = element.getAnnotation(Metadata::class.java)
    return annotation?.run {
        KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
    }
}

/**
 * It reads the Kotlin metadata from the class file
 *
 * @param element The element to extract the metadata from.
 * @return KotlinClassMetadata
 */
fun extractKotlinMetadata(element: Element): KotlinClassMetadata? {
    return exctractKotlinClassHeader(element)?.let {
        KotlinClassMetadata.read(it)
    }
}


/* Checking if the class is a data class. */
fun KotlinClassMetadata.Class.isDataClass(): Boolean {
    return Flag.Class.IS_DATA(this.toKmClass().flags)
}