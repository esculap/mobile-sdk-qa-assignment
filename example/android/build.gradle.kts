// Plain Kotlin/JVM module: the authentication logic has no Android UI dependency,
// so its tests run in seconds without an emulator. In the real SDK the same
// test would live in the Android library module's src/test folder.
plugins {
    kotlin("jvm") version "2.0.21"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "failed") }
}
