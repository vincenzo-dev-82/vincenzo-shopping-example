plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    
    // gRPC dependencies
    api("io.grpc:grpc-kotlin-stub:1.4.1")
    api("io.grpc:grpc-protobuf:1.61.0")
    api("com.google.protobuf:protobuf-kotlin:3.25.2")
    api("io.grpc:grpc-netty-shaded:1.61.0")
    
    // Kafka
    api("org.springframework.kafka:spring-kafka")
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
