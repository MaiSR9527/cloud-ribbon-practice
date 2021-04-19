package com.msr.better.rule.config;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-04-18 23:24
 **/
@Configuration
public class AnnoRuleConfig {

    @Bean
    public IRule randomRule() {
        return new RandomRule();
    }
}
