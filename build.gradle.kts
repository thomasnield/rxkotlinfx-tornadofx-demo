plugins {
    kotlin("jvm") version "1.3.61"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
}

javafx {
    version = "13.0.2"
    modules = mutableListOf("javafx.controls", "javafx.graphics")
}

application {
    mainClassName = "app.ApplicationRunner"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:2.0.0-SNAPSHOT")
    implementation("com.github.thomasnield:rxkotlinfx:2.2.2")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.+")
    implementation("org.nield:rxkotlin-jdbc:0.4.1")
    implementation("org.controlsfx:controlsfx:11.0.1")
    implementation("org.xerial:sqlite-jdbc:3.23.1")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")

}

application {
    applicationDefaultJvmArgs = listOf(
        "--add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED"
    )
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
