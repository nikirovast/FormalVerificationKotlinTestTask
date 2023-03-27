plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // core
    implementation("com.github.UnitTestBot.ksmt:ksmt-core:0.4.5")
    // z3
    implementation("com.github.UnitTestBot.ksmt:ksmt-z3:0.4.5")
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}