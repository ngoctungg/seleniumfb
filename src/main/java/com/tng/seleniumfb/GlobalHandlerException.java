package com.tng.seleniumfb;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;

@ControllerAdvice
@Log4j2
public class GlobalHandlerException{

    private final RestTemplate restTemplate;

    public GlobalHandlerException(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e){
        log.error(e);
        String url = "https://api.telegram.org/bot5305732763:AAEyzdIXAM4-70EWALtdhUhj3DZDMu4uzjU/sendMessage?chat_id=@tngerror&text={msg}";
        String exchange = restTemplate.getForObject(url, String.class, "Error: " + e.getMessage());
        log.info(exchange);
    }
}
