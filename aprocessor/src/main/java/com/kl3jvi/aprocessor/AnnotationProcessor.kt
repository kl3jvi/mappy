package com.kl3jvi.aprocessor

import com.google.auto.service.AutoService
import com.kl3jvi.annotations.MapToEntity
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
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
        roundEnv.getElementsAnnotatedWith(MapToEntity::class.java)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes can be annotated"
                    )
                    return true
                }
                processAnnotation(it)
            }
        return false
    }

    private fun processAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()


//        val variableAsElement = processingEnv.typeUtils.asElement(element.asType())
//        val fieldsInArgument = ElementFilter.fieldsIn(variableAsElement.enclosedElements)

        val targetClass = try {
            element.getAnnotation(MapToEntity::class.java).targetClass as TypeMirror // won’t even reach as TypeMirror but just for type inference
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }.asTypeName()

        val fileName = "${className}Mapper"
        val fileBuilder = FileSpec.builder(pack, fileName)
            .addFunction(
                FunSpec.builder("toEntity")
                    .receiver(element.asType().asTypeName())
                    .returns(targetClass)
                    .build()
            )

        val file = fileBuilder.build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir))
    }
}