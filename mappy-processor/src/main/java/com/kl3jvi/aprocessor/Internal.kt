package com.kl3jvi.aprocessor

import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.typeNameOf
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * It takes a target class, an element, and a list of fields to filter out, and returns a FunSpec.Builder with the code to
 * return a new instance of the target class with all the fields except the ones in the filter list
 *
 * @param targetClass TypeName - The class that we're creating the copy method for.
 * @param element Element - This is the element that we're currently processing.
 * @param filterList Array<String>
 */

fun FunSpec.Builder.addReturnFields(
    targetClass: TypeName, element: Element, filterList: Array<String>, editableFields: Array<String>
) = apply {
    addCode("return %T(", targetClass)
    val listOfEnclosed = element.enclosedElements.filter {
        filterList.toList().contains(it.simpleName.toString()).not()
    }.takeWhile {
        it.kind == ElementKind.FIELD
    }.joinToString {
        "${it.simpleName} = ${
            if (editableFields.contains(it.simpleName.toString()).not())
                it.simpleName
            else
                "new_${it.simpleName}"
        }"
    }
    addCode(listOfEnclosed)
    addCode(")")
}

/**
 * It takes a field name and an element, and returns the type of the field with the given name
 *
 * @param fieldName The name of the field we want to get the type of.
 * @param element The element that is being processed.
 * @return The type of the field.
 */
fun getFieldClassType(fieldName: String, element: Element): TypeName {
    return element.enclosedElements.takeWhile { it.kind == ElementKind.FIELD }
        .find { it.simpleName.toString() == fieldName }
        ?.asType()?.asTypeName() ?: typeNameOf<Unit>()
}

inline fun <reified T : Annotation> RoundEnvironment.processForAnnotation(
    processingEnvironment: ProcessingEnvironment,
    a: Class<T>,
    processAnnotation: (element: Element) -> Unit
): Boolean {
    getElementsAnnotatedWith(a).forEach {
        if (it.kind != ElementKind.CLASS) {
            processingEnvironment.error("Only classes can be annotated")
            return true
        }
        processAnnotation(it)
    }
    return false
}

fun ProcessingEnvironment.error(msg: String) {
    messager.printMessage(
        Diagnostic.Kind.ERROR, msg
    )
}

fun Element.getAnnotationFieldsForUi(clazz: Class<MapToUi>): AnnotationParams {
    val name = try {
        getAnnotation(clazz).targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }.asTypeName()
    val excludeFields = getAnnotation(clazz).excludeFields
    val editableFields = getAnnotation(clazz).editableFields

    return AnnotationParams(name, excludeFields, editableFields)
}

fun Element.getAnnotationFieldsForEntity(clazz: Class<MapToEntity>): AnnotationParams {
    val name = try {
        getAnnotation(clazz).targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }.asTypeName()

    val excludeFields = getAnnotation(clazz).excludeFields
    val editableFields = getAnnotation(clazz).editableFields

    return AnnotationParams(name, excludeFields, editableFields)
}

data class AnnotationParams(
    val name: TypeName,
    val excludeFields: Array<String>,
    val editableFields: Array<String>
)