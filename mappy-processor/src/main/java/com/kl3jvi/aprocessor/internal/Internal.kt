package com.kl3jvi.aprocessor.internal

import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.aprocessor.logger.compilerError
import com.squareup.kotlinpoet.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror

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
            if (editableFields.contains(it.simpleName.toString()).not()) it.simpleName
            else "new_${it.simpleName}"
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
        .find { it.simpleName.toString() == fieldName }?.asType()?.asTypeName() ?: typeNameOf<Unit>()
}

inline fun <reified DOMAIN : Annotation, reified ENTITY : Annotation> RoundEnvironment.processForAnnotation(
    retrieveElement: (element: Element, clazz: Class<out Annotation>) -> Unit
): Boolean {

    val list = buildList {
        add(getElementsAnnotatedWith(DOMAIN::class.java) to DOMAIN::class.java)
        add(getElementsAnnotatedWith(ENTITY::class.java) to ENTITY::class.java)
    }

    list.forEach { pair ->
        val setOfElements = pair.first
        setOfElements.forEach { element ->
            if (element.kind != ElementKind.CLASS) {
                compilerError("Only classes can be annotated")
                return true
            }
            retrieveElement(element, pair.second)
        }
    }

    return false
}

fun Element.getAnnotationFieldsForUi(clazz: Class<MapToDomain>): AnnotationParams {
    val name = try {
        getAnnotation(clazz).targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }.asTypeName()

    val excludeFields = getAnnotation(clazz).excludeFields
    val editableFields = getAnnotation(clazz).editableFields

    val parameterIterable = editableFields.map {
        val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, this)).build()
        parameter
    }.asIterable()


    return AnnotationParams(name, excludeFields, editableFields, parameterIterable)
}

fun Element.getAnnotationFieldsForEntity(clazz: Class<MapToEntity>): AnnotationParams {
    val name = try {
        getAnnotation(clazz).targetClass as TypeMirror
    } catch (e: MirroredTypeException) {
        e.typeMirror
    }.asTypeName()

    val excludeFields = getAnnotation(clazz).excludeFields
    val editableFields = getAnnotation(clazz).editableFields

    val parameterIterable = editableFields.map {
        val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, this)).build()
        parameter
    }.asIterable()

    return AnnotationParams(name, excludeFields, editableFields, parameterIterable)
}


fun List<Element>.getTypeFromIndex(element: Element): Type {
    return when (this) {
        first() -> Type.Domain(MapToDomain::class.java, element)

        last() -> Type.Entity(MapToEntity::class.java, element)

        else -> {
            compilerError(this.toString())
            compilerError("Annotation Not Found")
            error("$this Annotation Not Found")
        }
    }
}




