plugins {
    `kotlin-dsl`
}

group = "jp.co.yumemi.quiz.droidkaigi.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.detekt.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("detekt") {
            id = "droidkaigi.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
