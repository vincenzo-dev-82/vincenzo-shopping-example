import com.google.protobuf.gradle.id

plugins {
    id("org.springframework.boot")
    id("com.google.protobuf")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    
    // gRPC dependencies
    api("io.grpc:grpc-kotlin-stub:1.4.1")
    api("io.grpc:grpc-protobuf:1.61.0")
    api("com.google.protobuf:protobuf-kotlin:3.25.2")
    api("io.grpc:grpc-netty-shaded:1.61.0")
    api("javax.annotation:javax.annotation-api:1.3.2")
    
    // Kafka
    api("org.springframework.kafka:spring-kafka")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.4.1:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}
