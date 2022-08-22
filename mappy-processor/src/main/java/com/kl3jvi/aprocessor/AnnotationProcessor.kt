package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.annotations.MapToUi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

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
            return processForAnnotation<MapToUi>(processingEnvironment = processingEnv) { element ->
                element.processUiAnnotation()
            } || processForAnnotation<MapToEntity>(processingEnvironment = processingEnv) { element ->
                element.processEntityAnnotation()
            }
        }
    }

    private fun Element.processUiAnnotation() {
        val className = simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(this).toString()

        val fileName = "${className}UiMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)

        val (
            targetClass,
            filterList,
            editableFields,
            parameterIterable
        ) = getAnnotationFieldsForUi(MapToUi::class.java)


        filterList.forEach {
            if (editableFields.contains(it)) processingEnv.error("Mappy Error: Editable Field can not be set as an exclusive field!")
        }

        /* It's adding a function to the fileBuilder. The function is called toEntity, it takes the element as a receiver,
        and returns the targetClass. It then calls addReturnFields, which adds the code to return a new instance of the
        target class with all the fields except the ones in the filter list. */
        fileBuilder.addFunction(
            FunSpec.builder("toUiModel").receiver(this.asType().asTypeName()).addParameters(parameterIterable)
                .returns(targetClass).addReturnFields(targetClass, this, filterList, emptyArray()).build()
        )

        fileBuilder.build().writeToFile(processingEnv)
    }


    private fun Element.processEntityAnnotation() {
        val className = simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(this).toString()

        val fileName = "${className}EntityMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)

        /* It's getting the target class from the annotation. */
        val (
            targetClass,
            filterList,
            editableFields,
            parameterIterable
        ) = getAnnotationFieldsForEntity(MapToEntity::class.java)


        filterList.forEach {
            if (editableFields.contains(it)) processingEnv.error("Mappy Error: Editable Field can not be set as an exclusive field!")
        }

        fileBuilder.addFunction(
            FunSpec.builder("toEntity").receiver(this.asType().asTypeName()).addParameters(parameterIterable)
                .returns(targetClass).addReturnFields(targetClass, this, filterList, editableFields).build()
        )
        fileBuilder.build().writeToFile(processingEnv)
    }
}




