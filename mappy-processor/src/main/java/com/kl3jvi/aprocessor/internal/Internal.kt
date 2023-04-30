package com.kl3jvi.aprocessor.internal

import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.aprocessor.logger.compilerError
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import kotlin.reflect.KClass

/**
 * It takes a field name and an element, and returns the type of the field with the given name
 *
 * @param fieldName The name of the field we want to get the type of.
 * @param element The element that is being processed.
 * @return The type of the field.
 */
fun getFieldClassType(fieldName: String, element: Element): TypeName {
    return element.enclosedElements.takeWhile { it.kind == ElementKind.FIELD }
        .find { it.simpleName.toString() == fieldName }?.asType()?.asTypeName()
        ?: typeNameOf<Unit>()
}

fun RoundEnvironment.processForAnnotations(
    annotationClasses: List<KClass<out Annotation>>,
    retrieveElement: (element: Element, clazz: Class<out Annotation>) -> Unit
): Boolean {
    var errorOccurred = false
    val elementsAnnotatedWith = mutableSetOf<Element>()

    annotationClasses.forEach { annotationClass ->
        val clazz = annotationClass.java
        elementsAnnotatedWith.addAll(getElementsAnnotatedWith(clazz))
    }

    elementsAnnotatedWith.forEach { element ->
        if (element.kind != ElementKind.CLASS) {
            compilerError("Only classes can be annotated")
            errorOccurred = true
        } else {
            annotationClasses.forEach { annotationClass ->
                val clazz = annotationClass.java
                val annotation = element.getAnnotation(clazz)
                if (annotation != null) {
                    retrieveElement(element, clazz)
                }
            }
        }
    }

    return errorOccurred
}

fun Element.getAnnotationFields(mappingProperties: MappingProperties): AnnotationParams {
    val targetTypeMirror = mappingProperties.targetClass
    val excludeFields = mappingProperties.excludeFields
    val editableFields = mappingProperties.editableFields

    val parameterIterable = editableFields.map {
        val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, this)).build()
        parameter
    }.asIterable()

    return AnnotationParams(targetTypeMirror, excludeFields, editableFields, parameterIterable)
}

fun Element.getAnnotationFieldsForUi(): AnnotationParams {
    val mappingProperties = getAnnotation(MapToDomain::class.java)
        .getMappingProperties(this)
    return getAnnotationFields(mappingProperties)
}

fun Element.getAnnotationFieldsForEntity(): AnnotationParams {
    val mappingProperties = getAnnotation(MapToEntity::class.java)
        .getMappingProperties(this)
    return getAnnotationFields(mappingProperties)
}
