package nl.ing.api.contacting.conf

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
  scanBasePackages = Array(
    "nl.ing.api.contacting.conf",
    "nl.ing.api.contacting.testcontainers")
   )
@EntityScan(
  basePackages = Array(
    "nl.ing.api.contacting.java.repository",
    "nl.ing.api.contacting.conf")
)
@EnableJpaRepositories(
  basePackages = Array(
    "nl.ing.api.contacting.java.repository",
    "nl.ing.api.contacting.conf"
  )
)
@EnableEncryptableProperties
class Main

object Main extends App {
  SpringApplication.run(classOf[Main])
}
