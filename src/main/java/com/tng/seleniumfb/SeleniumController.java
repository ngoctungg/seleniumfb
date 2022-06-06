package com.tng.seleniumfb;

import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController()
@RequestMapping("/api/v1/selenium")
public class SeleniumController {

    private final WebDriver driver;

    private boolean isRunning = false;

    @Value("${fb.username}")
    public String unm;
    @Value("${fb.password}")
    public String pass;
    @Value("${fb.group.url}")
    private String groupUrl;

    @Value("${telegram.url}")
    private String telegramUrl;


    private final RestTemplate restTemplate;

    public SeleniumController(WebDriver driver, RestTemplateBuilder restTemplateBuilder) {
        this.driver = driver;
        this.restTemplate = restTemplateBuilder.build();
    }

    @GetMapping("/close")
    public ResponseEntity<String> close() {
        this.isRunning = false;
        return new ResponseEntity<>(driver.getCurrentUrl(), HttpStatus.OK);
    }

    @GetMapping("/quit")
    public ResponseEntity<String> quit() {
        driver.close();
        driver.quit();
        return new ResponseEntity<>(driver.getCurrentUrl(), HttpStatus.OK);
    }

    @GetMapping("/open")
    public ResponseEntity<String> open() {
        driver.get("https://www.facebook.com/");
        final WebDriverWait wait = new WebDriverWait(driver, Duration.of(30, ChronoUnit.SECONDS));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys(unm);
        driver.findElement(By.id("pass")).sendKeys(pass);
        driver.findElement(By.name("login")).click();

        driver.get(groupUrl);
        this.isRunning = true;
        return new ResponseEntity<>(driver.getCurrentUrl(), HttpStatus.OK);
    }


    @GetMapping("/newest")
    public ResponseEntity<String> getNewestPost() {

        return new ResponseEntity<>("is Running", HttpStatus.OK);
    }

    @Value("${lastest.post.id}")
    private String currentId = "";

    private Map<String, String> extracted() {
        Map<String, String> data = new HashMap<>();
        try {
            final WebDriverWait waitting = new WebDriverWait(driver, Duration.of(30, ChronoUnit.SECONDS));
            String className = "div[role='feed']";
            waitting.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(className)));
            WebElement feedContainer = driver.findElement(By.cssSelector(className));
            //get newest post
            String newestPost = feedContainer
                    .findElement(By.xpath("./child::*"))
                    .findElement(By.xpath("./child::*"))
                    .getText();
            data.put("newestPost", newestPost);
            // extract time
            WebElement webElement = feedContainer
                    .findElements(By.xpath("./child::*"))
                    .get(1);

            WebElement a = webElement
                    .findElements(By.cssSelector("a"))
                    .get(3);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", a);
            ((JavascriptExecutor) driver).executeScript("arguments[0].focus();", a);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", a);
            String currentUrl = a.getDomAttribute("href");
            data.put("url", currentUrl);
            data.put("id", currentUrl.split("/")[6]);
            String contentClassName = ".kvgmc6g5.cxmmr5t8.oygrvhab.hcukyx3x.c1et5uql.ii04i59q";
            String content = driver.findElement(By.cssSelector(contentClassName)).getText();
            data.put("content", content);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    @Scheduled(fixedDelay = 30000)
    public void reloadPage() throws InterruptedException {
        try {
            if (!this.isRunning) return;
            try {
                driver.navigate().refresh();
            } catch (Exception e) {
                e.printStackTrace();
                driver.get(groupUrl);
                return;
            }

            Map<String, String> extracted = extracted();
            log.info(extracted + " " + new Date().toInstant().toString());
            handleRecentPost(extracted);
        } catch (Exception e) {
            e.printStackTrace();
            this.isRunning = false;
        }
    }

    public void handleRecentPost(Map<String, String> data) {
        if (data.get("id") == null || data.get("id").equals(currentId)) return;
        this.currentId = data.get("id");
        sendMsg(data);
    }

    private void sendMsg(Map<String, String> data) {
        String exchange = restTemplate.getForObject(telegramUrl, String.class, data.toString());
        log.info(exchange);
    }
}
