plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk = Versions.COMPILE_SDK
    defaultConfig {
        applicationId = Versions.APPLICATION_ID
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        versionCode = Versions.versionCode
        versionName = Versions.versionName
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets["main"].java {
        srcDir("${buildDir.absolutePath}/generated/source/kaptKotlin/")
    }
}

dependencies {
    implementation(project(Modules.CORE))
    kapt(project(Modules.ANNOTATION_PROCESSOR))

    implementation(Libs.ANDROID_CORE)
    implementation(Libs.MATERIAL_LIB)
}
