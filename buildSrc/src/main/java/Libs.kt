object Libs {
    const val ANDROID_CORE = "androidx.core:core-ktx:${Versions.CORE}"
    const val MATERIAL_LIB = "com.google.android.material:material:${Versions.MATERIAL}"

    const val KOTLIN_POET = "com.squareup:kotlinpoet:${Versions.KOTLIN_POET}"
    const val AUTO_SERVICE_GOOGLE = "com.google.auto.service:auto-service:${Versions.AUTO_SERVICE}"
    const val METADATA_KOTLIN = "org.jetbrains.kotlinx:kotlinx-metadata-jvm:${Versions.KOTLIN_METADATA}"
}

object Modules {
    const val ANNOTATION_PROCESSOR = ":mappy-processor"
    const val CORE = ":mappy-core"
}