package com.msr.better.ribbon.config;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-04-18 23:17
 **/
//@Configuration
public class GlobalRuleConfig {

//    @Bean
    public IRule ribbonRule() {
        return new RandomRule();
    }
}
