package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes() = setOf(MapToUi::class.java.name, MapToEntity::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: Set<TypeElement>, roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv.apply {
            return processForAnnotation(processingEnv, MapToUi::class.java) {
                processUiAnnotation(it)
            } || processForAnnotation(processingEnv, MapToEntity::class.java) {
                processEntityAnnotation(it)
            }
        }

    }


    private fun processUiAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileName = "${className}UiMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)

        val (targetClass, filterList, editableFields) =
            element.getAnnotationFieldsForUi(MapToUi::class.java)

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
            FunSpec.builder("toUiModel").receiver(element.asType().asTypeName()).addParameters(parameterIterable)
                .returns(targetClass).addReturnFields(targetClass, element, filterList, emptyArray()).build()
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
        val (targetClass, filterList, editableFields) = element.getAnnotationFieldsForEntity(MapToEntity::class.java)


        val parameterIterable = editableFields.map {
            val parameter = ParameterSpec.builder("new_$it", getFieldClassType(it, element)).build()
            parameter
        }.asIterable()

        filterList.forEach {
            if (editableFields.contains(it))
                processingEnv.error("Mappy Error: Editable Field can not be set as an exclusive field!")
        }

        fileBuilder.addFunction(
            FunSpec.builder("toEntity")
                .receiver(element.asType().asTypeName())
                .addParameters(parameterIterable)
                .returns(targetClass)
                .addReturnFields(targetClass, element, filterList, editableFields)
                .build()
        )
        fileBuilder.build().writeToFile(processingEnv)
    }
}




