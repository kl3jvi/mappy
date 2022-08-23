package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.aprocessor.internal.*
import com.kl3jvi.aprocessor.logger.Logger
import com.kl3jvi.aprocessor.logger.compilerError
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

@OptIn(DelicateKotlinPoetApi::class)
@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    override fun getSupportedAnnotationTypes() =
        setOf(
            MapToDomain::class.java.name,
            MapToEntity::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: Set<TypeElement>, roundEnv: RoundEnvironment
    ): Boolean {
        Logger.retainInstance(processingEnv, "<MAPPY>")
        return roundEnv.processForAnnotation<MapToDomain, MapToEntity> { element, annotation ->
            when (annotation) {
                MapToEntity::class.java -> element.processEntityAnnotation()
                MapToDomain::class.java -> element.processUiAnnotation()
            }
        }
    }

    /**
     * It takes the annotation fields and generates a function that returns a new instance of the target class with the
     * fields that are not in the filter list set to the values of the fields in the annotated class
     */
    private fun Element.processUiAnnotation() {
        val className = simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(this).toString()

        val fileName = "${className}DomainMapper"
        val fileBuilder = FileSpec.builder(pack, fileName)

        val (targetClass,
            filterList,
            editableFields,
            parameterIterable
        ) = getAnnotationFieldsForUi(MapToDomain::class.java)

        if (filterList.any { editableFields.contains(it) }) compilerError("Mappy Error: Editable Field can not be set as an exclusive field!")

        fileBuilder.addFunction(
            FunSpec.builder("toDomainModel").receiver(this.asType().asTypeName()).addParameters(parameterIterable)
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
        val (targetClass, filterList, editableFields, parameterIterable) = getAnnotationFieldsForEntity(MapToEntity::class.java)

        if (filterList.any { editableFields.contains(it) }) compilerError("Mappy Error: Editable Field can not be set as an exclusive field!")


        fileBuilder.addFunction(
            FunSpec.builder("toEntity").receiver(this.asType().asTypeName()).addParameters(parameterIterable)
                .returns(targetClass).addReturnFields(targetClass, this, filterList, editableFields).build()
        )
        fileBuilder.build().writeToFile(processingEnv)
    }
}
