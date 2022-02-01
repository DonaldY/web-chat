package donald.im;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;


@SpringBootApplication(scanBasePackages = {"donald.im"})
@ServletComponentScan(basePackages = {"donald.lecture.controller"})
public class Starter extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        return application.sources(Starter.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Starter.class, args);
    }

}
