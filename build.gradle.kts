plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.philosobyte"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    configureEach {
        exclude(module = "spring-boot-starter-logging")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.apache.ignite:ignite-core:2.16.0")
    implementation("org.apache.ignite:ignite-spring:2.16.0")
    implementation("org.apache.ignite:ignite-slf4j:2.16.0")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

//    testImplementation("org.apache.ignite:ignite-core:2.16.0") {
//        artifact {
//            classifier = "tests"
//        }
//    }

    // testImplementation("org.junit.vintage:junit-vintage-engine:5.11.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.apache.ignite:ignite-core:2.16")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("--add-opens", "java.base/jdk.internal.access=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.util.calendar=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED")
    jvmArgs("--add-opens", "jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED")
    jvmArgs("--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.io=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.nio=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.net=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.util.concurrent.locks=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.util.concurrent.atomic=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang.invoke=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.math=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.sql/java.sql=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.base/java.text=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.management/sun.management=ALL-UNNAMED")
    jvmArgs("--add-opens", "java.desktop/java.awt.font=ALL-UNNAMED")
}
