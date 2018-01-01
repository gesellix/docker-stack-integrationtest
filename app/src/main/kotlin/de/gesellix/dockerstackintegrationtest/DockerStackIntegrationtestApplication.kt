package de.gesellix.dockerstackintegrationtest

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class DockerStackIntegrationtestApplication

fun main(args: Array<String>) {
    SpringApplication.run(DockerStackIntegrationtestApplication::class.java, *args)
}
