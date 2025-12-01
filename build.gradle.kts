// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Correct syntax for specifying the JitPack repository
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.2")
        classpath("com.google.gms:google-services:4.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    }
}

tasks.register("clean", Delete::class) {
    delete(project.buildDir)  // rootProject 대신 project.buildDir 사용
}
