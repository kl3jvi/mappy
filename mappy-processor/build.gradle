plugins {
    id("java-library")
    id("kotlin")
    id("kotlin-kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(Modules.CORE))
    implementation(Libs.KOTLIN_POET)
    implementation(Libs.AUTO_SERVICE_GOOGLE)
    implementation(Libs.METADATA_KOTLIN)
    kapt(Libs.AUTO_SERVICE_GOOGLE)
}