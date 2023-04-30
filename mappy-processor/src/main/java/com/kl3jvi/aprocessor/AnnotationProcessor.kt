package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToDomain
import com.kl3jvi.annotations.MapToEntity
import com.kl3jvi.aprocessor.internal.*
import com.kl3jvi.aprocessor.logger.Logger
import com.kl3jvi.aprocessor.logger.compilerError
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeName
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

    override fun getSupportedAnnotationTypes() =
        setOf(
            MapToDomain::class.java.name,
            MapToEntity::class.java.name
        )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        Logger.retainInstance(processingEnv, "<MAPPY>")

        val foundError = roundEnv.processForAnnotations(
            listOf(MapToDomain::class, MapToEntity::class)
        ) { element, clazz ->
            when (clazz) {
                MapToEntity::class.java -> element.processEntityAnnotation()
                MapToDomain::class.java -> element.processDomainAnnotation()
            }
        }

        return foundError
    }

    /**
     * It takes the annotation fields and generates a function that returns a new instance of the target class with the
     * fields that are not in the filter list set to the values of the fields in the annotated class
     */

    private fun Element.generateMapperFunction(
        annotationType: Class<out Annotation>,
        targetClass: TypeName, // Change TypeName to ClassName
        filterList: Array<String>,
        editableFields: Array<String>,
        functionName: String
    ) {
        val className = simpleName.toString()
        val packageName = processingEnv.elementUtils.getPackageOf(this).toString()
        val fileName = "${className}Mapper"

        val fileBuilder = FileSpec.builder(packageName, fileName)

        val functionBuilder = FunSpec.builder(functionName)
            .receiver(this.asType().asTypeName())
            .returns(targetClass)

        val constructorCall = CodeBlock.builder()
            .add("%T(", targetClass)

        enclosedElements
            .filter { it.kind.isField }
            .forEachIndexed { index, field ->
                val fieldName = field.simpleName.toString()

                if (fieldName !in filterList) {
                    constructorCall.add("%L = %L", fieldName, fieldName)

                    if (index < enclosedElements.size - 1) {
                        constructorCall.add(", ")
                    }
                }
            }

        constructorCall.add(")")
        functionBuilder.addStatement("return %L", constructorCall.build())

        fileBuilder.addFunction(functionBuilder.build())
        fileBuilder.build().writeToFile(processingEnv)
    }

    private fun Element.processDomainAnnotation() {
        val (targetTypeMirror, filterList, editableFields, _) = getAnnotationFieldsForUi()

        if (filterList.any { editableFields.contains(it) }) {
            compilerError("Mappy Error: Editable Field can not be set as an exclusive field!")
        }

        val targetClassName = targetTypeMirror.asTypeName()
        generateMapperFunction(
            MapToDomain::class.java,
            targetClassName,
            filterList,
            editableFields,
            "toDomainModel"
        )
    }

    private fun Element.processEntityAnnotation() {
        val (targetTypeMirror, filterList, editableFields, _) = getAnnotationFieldsForEntity()

        if (filterList.any { editableFields.contains(it) }) {
            compilerError("Mappy Error: Editable Field can not be set as an exclusive field!")
        }

        val targetClassName = targetTypeMirror.asTypeName()
        generateMapperFunction(
            MapToEntity::class.java,
            targetClassName,
            filterList,
            editableFields,
            "toEntity"
        )
    }
}
