package com.happy.chat.uitls;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

@Component
public class PrometheusUtils {

    private Counter flirtopiaCounter;

    @Autowired
    private CollectorRegistry flirtopiaRegistry;

    // 在整个运行环境中，prometheusName必须只能出现一次，register时会报错
    @PostConstruct
    public void init() {
        flirtopiaCounter = Counter.build("flirtopia", "flirtopia_help").labelNames("extra1").register(flirtopiaRegistry);
    }

    @Bean
    private Counter flirtopiaPrometheusCounter() {
        return flirtopiaCounter;
    }

    public void perf(Counter counter, String extra1) {
        counter.labels(extra1).inc();
    }

    public void perf(String extra1) {
        flirtopiaCounter.labels(extra1).inc();
    }
}
