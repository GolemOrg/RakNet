
plugins {
    kotlin("multiplatform") version "1.5.31"
    `maven-publish`
    java
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation("io.netty:netty-all:4.1.72.Final")
                implementation("com.github.GolemOrg:events:1.0.3")
                implementation("com.github.GolemOrg:nettyhelpers:0.0.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.github.GolemOrg:benchpress:1.0.7")
            }
        }
    }
}