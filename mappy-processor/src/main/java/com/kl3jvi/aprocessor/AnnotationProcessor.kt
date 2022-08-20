package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {
    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    /**
     * It returns a set of annotation types that the processor supports.
     *
     * @return A set of strings that represent the annotations that this processor will process.
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            MapToUi::class.java.name, MapToEntity::class.java.name
        )
    }

    /**
     * This annotation processor supports the latest version of the Java language.
     */
    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    /**
     * For each element annotated with `@MapToUi` or `@MapToEntity`, if the element is a class, process the annotation
     *
     * @param annotations This is the set of annotations that the processor supports. In our case, we only support the
     * MapToUi and MapToEntity annotations.
     * @param roundEnv This is the environment for the current round of annotation processing.
     * @return Boolean
     */
    override fun process(
        annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.apply {
            getElementsAnnotatedWith(MapToUi::class.java).forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR, "Only classes can be annotated"
                    )
                    return true
                }
                processUiAnnotation(it)
            }
            getElementsAnnotatedWith(MapToEntity::class.java).forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR, "Only classes can be annotated"
                    )
                    return true
                }
                processEntityAnnotation(it)
            }
        }
        return false
    }


    private fun processUiAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileName = "${className}UiMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)
//        val classBuilder = TypeSpec.classBuilder(fileName)


        /* It's getting the target class from the annotation. */
        val targetClass = try {
            element.getAnnotation(MapToUi::class.java).targetClass as TypeMirror // won’t even reach as TypeMirror but just for type inference
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }.asTypeName()

        val filterList = element.getAnnotation(MapToUi::class.java).excludeFields
        val editableFields = element.getAnnotation(MapToUi::class.java).editableFields

        /* It's creating a list of parameters for the function. */
        val parameterIterable = editableFields.map {
            val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, element)).build()
            parameter
        }.asIterable()

        filterList.forEach {
            if (editableFields.contains(it)) processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR, "Mappy Error: Editable Field can not be set as an exclusive field!"
            )
        }

        /* It's adding a function to the fileBuilder. The function is called toEntity, it takes the element as a receiver,
        and returns the targetClass. It then calls addReturnFields, which adds the code to return a new instance of the
        target class with all the fields except the ones in the filter list. */
        fileBuilder.addFunction(
            FunSpec.builder("toUiModel")
                .receiver(element.asType().asTypeName())
                .addParameters(parameterIterable)
                .returns(targetClass)
                .addReturnFields(targetClass, element, filterList, emptyArray())
                .build()
        )

        val file = fileBuilder.build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir.toString()))

    }


    private fun processEntityAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileName = "${className}EntityMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)
//        val classBuilder = TypeSpec.classBuilder(fileName)


        /* It's getting the target class from the annotation. */
        val targetClass = try {
            element.getAnnotation(MapToEntity::class.java).targetClass as TypeMirror // won’t even reach as TypeMirror but just for type inference
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }.asTypeName()

        val filterList = element.getAnnotation(MapToEntity::class.java).excludeFields
        val editableFields = element.getAnnotation(MapToEntity::class.java).editableFields

        val parameterIterable = editableFields.map {
            val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, element)).build()
            parameter
        }.asIterable()

        filterList.forEach {
            if (editableFields.contains(it)) processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR, "Mappy Error: Editable Field can not be set as an exclusive field!"
            )
        }

        /* It's adding a function to the fileBuilder. The function is called toEntity, it takes the element as a receiver,
        and returns the targetClass. It then calls addReturnFields, which adds the code to return a new instance of the
        target class with all the fields except the ones in the filter list. */
        fileBuilder.addFunction(
            FunSpec.builder("toEntity")
                .receiver(element.asType().asTypeName())
                .addParameters(parameterIterable)
                .returns(targetClass)
                .addReturnFields(targetClass, element, filterList, editableFields)
                .build()
        )

        val file = fileBuilder.build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir.toString()))
    }
}

/**
 * It takes a target class, an element, and a list of fields to filter out, and returns a FunSpec.Builder with the code to
 * return a new instance of the target class with all the fields except the ones in the filter list
 *
 * @param targetClass TypeName - The class that we're creating the copy method for.
 * @param element Element - This is the element that we're currently processing.
 * @param filterList Array<String>
 */
private fun FunSpec.Builder.addReturnFields(
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

fun getFieldClassType(fieldName: String, element: Element): TypeName {
    return element.enclosedElements.takeWhile { it.kind == ElementKind.FIELD }
        .find { it.simpleName.toString() == fieldName }?.asType()?.asTypeName() ?: typeNameOf<String>()
}
