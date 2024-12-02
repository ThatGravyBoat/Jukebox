plugins {
    id("maven-publish")
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
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.ktor:ktor-client-core:2.1.0")
                implementation("io.ktor:ktor-network:2.1.0")
                implementation("io.ktor:ktor-network-tls:2.1.0")
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

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("Jukebox")
                description.set("Music service library for multiple services.")
                url.set("https://github.com/ThatGravyBoat/Jukebox")

                licenses {
                    license {
                        name.set("MIT")
                    }
                }

                scm {
                    connection.set("git:https://github.com/ThatGravyBoat/Jukebox.git")
                    developerConnection.set("git:https://github.com/ThatGravyBoat/Jukebox.git")
                    url.set("https://github.com/ThatGravyBoat/Jukebox")
                }
            }
        }
    }
    repositories {
        maven {
            setUrl("https://maven.resourcefulbees.com/repository/thatgravyboat/")

            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_PASS")
            }
        }
    }
}