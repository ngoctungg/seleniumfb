package com.tng.seleniumfb;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PreDestroy;

@SpringBootApplication
@EnableScheduling
public class SeleniumfbApplication implements CommandLineRunner {

    @Autowired
    private WebDriver driver;

    public static void main(String[] args) {
        SpringApplication.run(SeleniumfbApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }

    @PreDestroy
    public void onExit() {
        System.out.println("Closing browser");
        driver.close();
        driver.quit();
    }

}



