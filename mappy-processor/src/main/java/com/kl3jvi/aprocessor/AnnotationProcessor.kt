package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
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

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(MapToEntity::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.apply {
            getElementsAnnotatedWith(MapToUi::class.java).forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes can be annotated"
                    )
                    return true
                }
                processUiAnnotation(it)
            }
            getElementsAnnotatedWith(MapToEntity::class.java)
                .forEach {
                    if (it.kind != ElementKind.CLASS) {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Only classes can be annotated"
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


        /* It's adding a function to the fileBuilder. The function is called toEntity, it takes the element as a receiver,
        and returns the targetClass. It then calls addReturnFields, which adds the code to return a new instance of the
        target class with all the fields except the ones in the filter list. */
        fileBuilder.addFunction(
            FunSpec.builder("toUiModel")
                .receiver(element.asType().asTypeName())
                .returns(targetClass)
                .addReturnFields(targetClass, element, filterList)
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


        /* It's adding a function to the fileBuilder. The function is called toEntity, it takes the element as a receiver,
        and returns the targetClass. It then calls addReturnFields, which adds the code to return a new instance of the
        target class with all the fields except the ones in the filter list. */
        fileBuilder.addFunction(
            FunSpec.builder("toEntity")
                .receiver(element.asType().asTypeName())
                .returns(targetClass)
                .addReturnFields(targetClass, element, filterList)
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
    targetClass: TypeName,
    element: Element,
    filterList: Array<String>
) = apply {
    addCode("return $targetClass(")
    val listOfEnclosed = element.enclosedElements.filter {
        filterList.toList().contains(it.simpleName.toString()).not()
    }.takeWhile {
        it.kind == ElementKind.FIELD
    }.joinToString { it.simpleName }
    addCode(listOfEnclosed)
    addCode(")")
}
