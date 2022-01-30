import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
    `maven-publish`
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}
dependencies {
    implementation("io.netty:netty-all:4.1.72.Final")
    implementation("com.esotericsoftware.reflectasm:reflectasm:1.09")
    testImplementation("com.github.GolemOrg:benchmark-kt:1.0.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            // Include any other artifacts here, like javadocs
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/GolemOrg/raknet-kt")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}