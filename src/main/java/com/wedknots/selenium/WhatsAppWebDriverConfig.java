package com.wedknots.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
@ConditionalOnProperty(name = "delivery.whatsapp-personal.enabled", havingValue = "true")
public class WhatsAppWebDriverConfig implements DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebDriverConfig.class);

    @Value("${delivery.whatsapp-personal.selenium.binary:}")
    private String edgeBinary;

    @Value("${delivery.whatsapp-personal.selenium.driver-path:}")
    private String driverPath;

    @Value("${delivery.whatsapp-personal.selenium.profile-dir:c:/hosting/wed-knots/edge-profile}")
    private String profileDir;

    @Value("${delivery.whatsapp-personal.selenium.headless:false}")
    private boolean headless;

    private WebDriver driver;

    @Bean(destroyMethod = "quit")
    @Scope("singleton")
    public WebDriver webDriver() {
        if (driver != null) {
            logger.debug("Reusing existing Edge WebDriver instance");
            return driver;
        }

        logger.info("Initializing Edge WebDriver for WhatsApp Web automation (SINGLETON)");

        if (driverPath != null && !driverPath.isBlank()) {
            logger.info("Using Edge driver path: {}", driverPath);
            System.setProperty("webdriver.edge.driver", driverPath);
        } else {
            logger.info("Using Selenium Manager to auto-download Edge driver");
        }

        EdgeOptions options = new EdgeOptions();

        if (edgeBinary != null && !edgeBinary.isBlank()) {
            logger.info("Using Edge binary: {}", edgeBinary);
            options.setBinary(edgeBinary);
        }

        // Reuse logged-in profile so WhatsApp Web stays authenticated
        logger.info("Using Edge profile directory: {}", profileDir);
        options.addArguments("--user-data-dir=" + profileDir);
        options.addArguments("--profile-directory=Default");

        // Keep browser visible for WhatsApp Web
        if (!headless) {
            logger.info("Running Edge in visible mode (not headless)");
            options.addArguments("--start-maximized");
        } else {
            logger.warn("Running Edge in headless mode - WhatsApp Web may not work properly");
            options.addArguments("--headless");
        }

        // Additional useful options
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--remote-allow-origins=*");
        options.setPageLoadTimeout(Duration.ofSeconds(30));

        logger.info("Creating Edge WebDriver instance...");
        driver = new EdgeDriver(options);
        logger.info("Edge WebDriver initialized successfully - this instance will be reused");

        return driver;
    }

    @Override
    public void destroy() {
        if (driver != null) {
            logger.info("Shutting down Edge WebDriver");
            try {
                driver.quit();
            } catch (Exception e) {
                logger.error("Error closing WebDriver", e);
            }
            driver = null;
        }
    }
}
