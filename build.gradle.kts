@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.24"
    `maven-publish`
}

group = "net.sergeych"
version = "0.3.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.universablockchain.com")
}

//configurations.all {
//    resolutionStrategy.cacheChangingModulesFor(30, "seconds")
//}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("8"))
    }
    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        browser()
        nodejs()
//        useCommonJs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }
//    linuxX64("native")

//    val hostOs = System.getProperty("os.name")
//    if( hostOs == "Mac OS X")
//        ios()
//
//    val isMingwX64 = hostOs.startsWith("Windows")
//    val nativeTarget = when {
//        hostOs == "Mac OS X" -> macosX64("native")
//        hostOs == "Linux" -> linuxX64("native")
//        isMingwX64 -> mingwX64("native")
//        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
//    }

//    val publicationsFromMainHost =
//        listOf(jvm(), js()).map { it.name } + "kotlinMultiplatform"

    sourceSets {
        all {
//            languageSettings.optIn("kotlin.RequiresOptIn")
                languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
                languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
//                languageSettings.optIn("kotlin.contracts.ExperimentalContracts")

        }
        val commonMain by getting {
            dependencies {
                implementation("net.sergeych:mp_stools:[1.3.2-SNAPSHOT,)")
                // we take datetime from mp_stools
//                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
//                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
                api("com.ionspin.kotlin:bignum:0.3.9")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
        }
        val jvmTest by getting {
            dependencies {
                implementation("com.icodici:universa_core:3.14.7")
            }
        }

        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                // this version is needed to avoid buggy CCE on EmptySerializerModule - some inconsistency in libraries
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                implementation(kotlin("test-js"))
            }
        }
//        val nativeMain by getting {
//        }
//        val nativeTest by getting {
//            dependencies {
//                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
//            }
//        }
    }

    publishing {
//        publications {
//
//            matching { it.name in publicationsFromMainHost }.all {
//                val targetPublication = this@all
//                tasks.withType<AbstractPublishToMaven>()
//                    .matching { it.publication == targetPublication }
//                    .configureEach { onlyIf { findProperty("isMainHost") == "true" } }
//            }
//
////            create<MavenPublication>("maven") {
////                from(components["java"])
////            }
//        }
        repositories {
            maven {
                val mavenUser: String by project
                val mavenPassword: String by project
                url = uri("https://maven.universablockchain.com/")
                credentials {
                    username = mavenUser
                    password = mavenPassword
                }
            }
        }
    }
}


//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//    ).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }
