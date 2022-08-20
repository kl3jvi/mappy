package com.kl3jvi.aprocessor

import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

fun FileSpec.writeToFile(processingEnv: ProcessingEnvironment) {
    val kaptKotlinGeneratedDir = processingEnv.options[AnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME]
    writeTo(File(kaptKotlinGeneratedDir.toString()))
}
