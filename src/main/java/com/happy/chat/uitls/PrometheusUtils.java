package com.happy.chat.uitls;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;

public class PrometheusUtils {
    public static void perf(CollectorRegistry registry, String name, String help, String extra1) {
        Counter counter = Counter.build(name, help).labelNames("extra1")
                .register(registry);
        counter.labels(extra1).inc();
    }

    public static void perf(CollectorRegistry registry, String name, String help, String extra1, String extra2) {
        Counter counter = Counter.build(name, help).labelNames("extra1", "extra2")
                .register(registry);
        counter.labels(extra1, extra2).inc();
    }
    public static void perf(CollectorRegistry registry, String name, String help, String extra1, String extra2, String extra3) {
        Counter counter = Counter.build(name, help).labelNames("extra1", "extra2", "extra3")
                .register(registry);
        counter.labels(extra1, extra2, extra3).inc();
    }

    public static void perf(CollectorRegistry registry, String name, String help, String extra1, String extra2, String extra3, String extra4) {
        Counter counter = Counter.build(name, help).labelNames("extra1", "extra2", "extra3", extra4)
                .register(registry);
        counter.labels(extra1, extra2, extra3, extra4).inc();
    }
}
