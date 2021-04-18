package com.msr.better.ribbon.config;

import com.msr.better.ribbon.annotation.AvoidScan;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author MaiShuRen
 * @site https://www.maishuren.top
 * @since 2021-04-18 23:24
 **/
@Configuration
@AvoidScan
public class AnnoRuleConfig {
    @Autowired
    IClientConfig config;

    @Bean
    public IRule ribbonRule(IClientConfig config) {
        return new RandomRule();
    }
}
