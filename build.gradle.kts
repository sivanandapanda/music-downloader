plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ws.schild:jave-core:3.3.1")
    implementation("ws.schild:jave-nativebin-linux64:3.3.1")

    implementation("io.github.cdimascio:java-dotenv:3.0.0")
}

tasks.test {
    useJUnitPlatform()
}