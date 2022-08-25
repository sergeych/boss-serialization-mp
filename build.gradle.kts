@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("multiplatform") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    `maven-publish`
}

group = "net.sergeych"
version = "0.1.3-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.universablockchain.com")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(30, "seconds")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
        browser {
        }
//        useCommonJs()
    }
    ios()

    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    val publicationsFromMainHost =
        listOf(jvm(), js()).map { it.name } + "kotlinMultiplatform"

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                implementation("net.sergeych:mp_stools:[1.3.2-SNAPSHOT,)")
                // we take datetime from mp_stools
//                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
                api("com.ionspin.kotlin:bignum:0.3.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting

        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val nativeMain by getting
        val nativeTest by getting {
        }
    }

    publishing {
        publications {

            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPublication }
                    .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
            }

//            create<MavenPublication>("maven") {
//                from(components["java"])
//            }
        }
        repositories {
            maven {
                url = uri("https://maven.universablockchain.com/")
                credentials {
                    username = System.getenv("maven_user")
                    password = System.getenv("maven_password")
                }
            }
        }
    }
}


