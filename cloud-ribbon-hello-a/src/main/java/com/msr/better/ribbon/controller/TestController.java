package com.msr.better.ribbon.controller;

import com.netflix.loadbalancer.IRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2020-07-05
 */
@RestController
@RequestMapping("ribbon")
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/test")
    public String test(){
        String body = restTemplate.getForEntity("http://RIBBON-SERVICE-B/test", String.class).getBody();
        return body;
    }

    @GetMapping("/test2")
    public String test2(){
        String body = restTemplate.getForEntity("http://RIBBON-SERVICE-ORDER/test", String.class).getBody();
        return body;
    }

}
