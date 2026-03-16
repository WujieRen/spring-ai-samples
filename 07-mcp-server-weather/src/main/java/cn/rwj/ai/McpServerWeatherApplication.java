package cn.rwj.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class McpServerWeatherApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(McpServerWeatherApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("----------------------------->");
    }

}
