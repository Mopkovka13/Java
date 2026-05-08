plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.15.10")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // переопределяй при запуске: ./gradlew run -PmainClass=playground.HeapBomb
    mainClass = providers.gradleProperty("mainClass").orElse("playground.Menu")
}

// флаги JVM, которые применяются для `./gradlew run`
tasks.named<JavaExec>("run") {
    // маленькая куча и стек, чтобы быстро словить ошибки
    jvmArgs(
        "-Xmx64m",
        "-Xms64m",
        "-Xss256k",
        "-XX:MaxMetaspaceSize=64m",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-XX:HeapDumpPath=build/heap.hprof",
        "-Xlog:gc*:file=build/gc.log:time,uptime,level,tags"
    )
    standardInput = System.`in`
}
