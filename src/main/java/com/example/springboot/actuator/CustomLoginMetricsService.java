package com.example.springboot.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class CustomLoginMetricsService {

    private final Counter loginSuccess;
    private final Counter loginFailure;

    public CustomLoginMetricsService(MeterRegistry registry) {
        this.loginSuccess = registry.counter("counter.login.success");
        this.loginFailure = registry.counter("counter.login.failure");
    }

    public boolean login(String userName, char[] password) {
        boolean success;
        if (userName.equals("admin") && "secret".toCharArray().equals(password)) {
            loginSuccess.increment();
            success = true;
        }
        else {
            loginFailure.increment();
            success = false;
        }
        return success;
    }
}
