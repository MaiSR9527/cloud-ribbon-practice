package com.msr.better.ribbon;

import com.msr.better.ribbon.annotation.AvoidScan;
import com.msr.better.ribbon.config.AnnoRuleConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2020-07-05
 */
@SpringBootApplication
@EnableDiscoveryClient
@RibbonClient(name = "ribbon-service-b", configuration = AnnoRuleConfig.class)
//@RibbonClients(value = {
//		@RibbonClient(name = "client-a", configuration = TestConfiguration.class),
//		@RibbonClient(name = "client-b", configuration = TestConfiguration.class)
//})
// 组件扫描排除
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = {AvoidScan.class})})
public class ServiceAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
