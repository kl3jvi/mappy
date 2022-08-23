package com.kl3jvi.aprocessor.internal

import com.kl3jvi.aprocessor.AnnotationProcessor
import com.squareup.kotlinpoet.FileSpec
import java.io.File
import javax.annotation.processing.ProcessingEnvironment

fun FileSpec.writeToFile(processingEnv: ProcessingEnvironment) {
    val kaptKotlinGeneratedDir = processingEnv.options[AnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME]
    writeTo(File(kaptKotlinGeneratedDir.toString()))
}
