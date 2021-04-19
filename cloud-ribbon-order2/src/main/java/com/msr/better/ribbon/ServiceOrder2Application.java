package com.msr.better.ribbon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2020-07-05
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceOrder2Application {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOrder2Application.class, args);
    }
}
