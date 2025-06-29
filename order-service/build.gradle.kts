plugins {
    id("org.springframework.boot")
    kotlin("plugin.jpa")
}

dependencies {
    implementation(project(":common"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    runtimeOnly("com.mysql:mysql-connector-j")
    
    // gRPC
    implementation("net.devh:grpc-server-spring-boot-starter:3.0.0.RELEASE")
    implementation("net.devh:grpc-client-spring-boot-starter:3.0.0.RELEASE")
    
    testImplementation("com.h2database:h2")
}
