plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "tech.thatgravyboat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    targets {
        jvm {
            withJava()
            val test by testRuns.getting
            test.executionTask.configure {
                useJUnitPlatform()
            }
        }
        js {
            browser()
            nodejs()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.ktor:ktor-client-core:2.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.1.0")
            }
        }
    }
}