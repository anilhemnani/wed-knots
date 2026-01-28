package com.wedknots.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service@ConditionalOnProperty(name = "delivery.whatsapp-personal.enabled", havingValue = "true")
public class WhatsAppSeleniumService {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppSeleniumService.class);

    private final WebDriver driver;
    private boolean isInitialized = false;

    public WhatsAppSeleniumService(WebDriver driver) {
        this.driver = driver;
    }

    public void sendMessage(String phone, String message) {
        try {
            logger.info("Sending WhatsApp message to {} - Message: {}", phone, message);

            // Check if we need to navigate to WhatsApp Web
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains("web.whatsapp.com")) {
                logger.info("Not on WhatsApp Web, navigating...");
                isInitialized = false;
            }

            String url = "https://web.whatsapp.com/send?phone=" + phone + "&text=" +
                    URLEncoder.encode(message, StandardCharsets.UTF_8);

            if (!isInitialized || !currentUrl.equals(url)) {
                logger.info("Opening WhatsApp Web URL: {}", url);
                driver.get(url);
                isInitialized = true;
            } else {
                logger.info("Already on WhatsApp Web, reusing session");
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(45));

            // Wait for page to load - look for the main app container
            logger.info("Waiting for WhatsApp Web to load...");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("app")));

            // Give extra time for chat to initialize
            Thread.sleep(3000);
            logger.info("WhatsApp Web loaded, looking for message input box...");

            // Try multiple selectors for the message input box
            WebElement msgBox = null;
            String[] selectors = {
                "div[contenteditable='true'][data-tab='10']",
                "div[contenteditable='true'][data-tab='6']",
                "div[contenteditable='true'][role='textbox']",
                "div[title='Type a message']",
                "div.lexical-rich-text-input",
                "div[data-lexical-editor='true']"
            };

            for (String selector : selectors) {
                try {
                    logger.debug("Trying selector: {}", selector);
                    msgBox = wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector(selector)));
                    if (msgBox != null && msgBox.isDisplayed()) {
                        logger.info("Found message box with selector: {}", selector);
                        break;
                    }
                } catch (Exception e) {
                    logger.debug("Selector {} did not work: {}", selector, e.getMessage());
                }
            }

            if (msgBox == null) {
                throw new RuntimeException("Could not find WhatsApp message input box");
            }

            // Click on the message box to ensure it's focused
            logger.info("Clicking on message box to focus...");
            msgBox.click();
            Thread.sleep(500);

            // Clear any existing text and type the message
            logger.info("Typing message into input box...");
            msgBox.clear();
            msgBox.sendKeys(message);
            Thread.sleep(1000);

            // Look for and click the send button
            logger.info("Looking for send button...");
            WebElement sendButton = null;
            String[] sendButtonSelectors = {
                "button[aria-label='Send']",
                "button[data-testid='send']",
                "span[data-icon='send']",
                "button span[data-icon='send']"
            };

            for (String selector : sendButtonSelectors) {
                try {
                    logger.debug("Trying send button selector: {}", selector);
                    List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
                    for (WebElement btn : buttons) {
                        if (btn.isDisplayed() && btn.isEnabled()) {
                            sendButton = btn;
                            logger.info("Found send button with selector: {}", selector);
                            break;
                        }
                    }
                    if (sendButton != null) break;
                } catch (Exception e) {
                    logger.debug("Send button selector {} did not work: {}", selector, e.getMessage());
                }
            }

            if (sendButton != null) {
                logger.info("Clicking send button...");
                sendButton.click();
            } else {
                // Fallback: press Enter
                logger.info("Send button not found, pressing Enter as fallback...");
                msgBox.sendKeys(Keys.ENTER);
            }

            // Wait a moment to ensure message is sent
            Thread.sleep(2000);
            logger.info("✅ Message sent successfully to {}", phone);

        } catch (Exception e) {
            logger.error("❌ Failed to send WhatsApp message to {}", phone, e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }
}
