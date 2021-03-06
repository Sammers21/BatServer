package service;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import service.repository.UserRepository;


@ComponentScan(basePackages = "service")
@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public class Application {


    private static final Logger log = Logger.getLogger(Application.class);

    public static void main(String[] args) {
        log.info("SPRING SERVER NOW WORKING");
        SpringApplication.run(Application.class, args);
    }
}