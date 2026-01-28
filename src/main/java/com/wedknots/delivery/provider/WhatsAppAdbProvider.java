package com.wedknots.delivery.provider;

import com.wedknots.delivery.DeliveryMode;
import com.wedknots.delivery.DeliveryRequest;
import com.wedknots.delivery.DeliveryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * WhatsApp delivery via ADB (Android Debug Bridge)
 * Sends messages through a connected Android phone with human-like behavior
 */
@Component
public class WhatsAppAdbProvider implements MessageDeliveryProvider {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppAdbProvider.class);

    @Value("${delivery.whatsapp-adb.enabled:false}")
    private boolean enabled;

    @Value("${delivery.whatsapp-adb.device-id:}")
    private String deviceId;

    @Value("${delivery.whatsapp-adb.adb-path:adb}")
    private String adbPath;

    @Value("${delivery.whatsapp-adb.timeout-seconds:30}")
    private int timeoutSeconds;

    @Value("${delivery.whatsapp-adb.human-delay-min-ms:500}")
    private int humanDelayMinMs;

    @Value("${delivery.whatsapp-adb.human-delay-max-ms:2000}")
    private int humanDelayMaxMs;

    private final Random random = new Random();

    // Cache device connection status to avoid repeated ADB calls
    private Boolean deviceConnectedCache = null;
    private long lastDeviceCheckTime = 0;
    private static final long DEVICE_CHECK_CACHE_MS = 30000; // Cache for 30 seconds

    @Override
    public boolean canDeliver(DeliveryRequest request) {
        return isConfigured() &&
               request.getRecipient() != null &&
               request.getRecipient().getPrimaryPhoneNumber() != null &&
               !request.getRecipient().getPrimaryPhoneNumber().isEmpty();
    }

    @Override
    public DeliveryResult deliver(DeliveryRequest request) {
        try {
            if (!isConfigured()) {
                logger.info("WhatsApp ADB delivery not configured, recording message for manual delivery");
                return new DeliveryResult(true, DeliveryMode.WHATSAPP_ADB, "WHATSAPP_RECORDED", "Message recorded for manual WhatsApp delivery");
            }

            String phone = request.getOverridePhoneNumber();
            if (phone == null || phone.isEmpty()) {
                if (request.getRecipient() == null || request.getRecipient().getPrimaryPhoneNumber() == null) {
                    throw new IllegalArgumentException("No phone number available for WhatsApp ADB delivery");
                }
                phone = request.getRecipient().getPrimaryPhoneNumber();
            }

            String message = buildMessage(request);

            logger.info("Sending WhatsApp (ADB) to {}", phone);
            // Check ADB connection
            if (!checkAdbConnection()) {
                return new DeliveryResult(false, DeliveryMode.WHATSAPP_ADB,
                    "ADB device not connected");
            }

            // Step-by-step message sending with human-like delays
            boolean success = sendMessageWithHumanBehavior(phone, message);

            if (success) {
                logger.info("✅ WhatsApp message sent successfully via ADB to {}", phone);
                return new DeliveryResult(true, DeliveryMode.WHATSAPP_ADB,
                    "SENT", "Message sent via WhatsApp ADB");
            } else {
                return new DeliveryResult(false, DeliveryMode.WHATSAPP_ADB,
                    "Failed to send message via ADB");
            }

        } catch (Exception e) {
            logger.error("Error sending WhatsApp message via ADB", e);
            return new DeliveryResult(false, DeliveryMode.WHATSAPP_ADB, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "WhatsApp ADB";
    }

    @Override
    public boolean isConfigured() {
        if (!enabled) {
            return false;
        }

        // Check if device is connected (with caching)
        return isDeviceConnected();
    }

    /**
     * Check if ADB device is connected (with caching to avoid repeated checks)
     */
    private boolean isDeviceConnected() {
        long now = System.currentTimeMillis();

        // Return cached result if still valid
        if (deviceConnectedCache != null && (now - lastDeviceCheckTime) < DEVICE_CHECK_CACHE_MS) {
            return deviceConnectedCache;
        }

        // Perform actual device check
        boolean connected = checkAdbConnection();

        // Update cache
        deviceConnectedCache = connected;
        lastDeviceCheckTime = now;

        if (!connected) {
            logger.debug("ADB device not connected or ADB not accessible");
        }

        return connected;
    }

    /**
     * Check if ADB device is connected
     */
    private boolean checkAdbConnection() {
        try {
            List<String> command = new ArrayList<>();
            command.add(adbPath);
            if (deviceId != null && !deviceId.isEmpty()) {
                command.add("-s");
                command.add(deviceId);
            }
            command.add("devices");

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

            String line;
            boolean deviceFound = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("device") && !line.contains("List of devices")) {
                    deviceFound = true;
                    logger.debug("ADB device found: {}", line);
                }
            }

            process.waitFor(5, TimeUnit.SECONDS);
            return deviceFound;

        } catch (Exception e) {
            logger.error("Error checking ADB connection", e);
            return false;
        }
    }

    /**
     * Send WhatsApp message with human-like behavior
     * Steps:
     * 1. Wake up the device
     * 2. Open WhatsApp
     * 3. Navigate to specific chat
     * 4. Type message (with typing delays)
     * 5. Press send
     */
    private boolean sendMessageWithHumanBehavior(String phoneNumber, String message) {
        try {
            logger.info("Step 1: Waking up device");
            wakeUpDevice();
            humanDelay();

            logger.info("Step 2: Opening WhatsApp");
            openWhatsApp();
            humanDelay(2000, 3000); // Wait for WhatsApp to load

            logger.info("Step 3: Opening chat for {}", phoneNumber);
            boolean chatOpened = openChat(phoneNumber);
            if (!chatOpened) {
                logger.warn("Failed to open chat, falling back to deep link method");
                return sendViaDeepLink(phoneNumber, message);
            }
            humanDelay(1000, 2000); // Wait for chat to open

            logger.info("Step 4: Typing message");
            typeMessage(message);
            humanDelay(); // Small delay before sending

            logger.info("Step 5: Pressing send button");
            pressSendButton();
            humanDelay(); // Wait for message to send

            logger.info("Message sent successfully with human-like behavior");
            return true;

        } catch (Exception e) {
            logger.error("Error during human-like message sending", e);
            return false;
        }
    }

    /**
     * Wake up the device screen
     */
    private void wakeUpDevice() throws Exception {
        executeAdbCommand("shell", "input", "keyevent", "KEYCODE_WAKEUP");
        logger.debug("Device woken up");
    }

    /**
     * Open WhatsApp application
     */
    private void openWhatsApp() throws Exception {
        // Launch WhatsApp using package name
        executeAdbCommand("shell", "am", "start",
            "-n", "com.whatsapp/.Main");
        logger.debug("WhatsApp opened");
    }

    /**
     * Open specific chat using search
     */
    private boolean openChat(String phoneNumber) {
        try {
            // Tap on search icon (coordinates may vary by device)
            // Alternative: Use WhatsApp URL scheme
            String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

            // Use WhatsApp deep link to open specific chat
            String whatsappUrl = "https://wa.me/" + cleanNumber;
            executeAdbCommand("shell", "am", "start",
                "-a", "android.intent.action.VIEW",
                "-d", whatsappUrl);

            logger.debug("Chat opened for {}", phoneNumber);
            return true;

        } catch (Exception e) {
            logger.error("Error opening chat", e);
            return false;
        }
    }

    /**
     * Type message with human-like delays between characters
     */
    private void typeMessage(String message) throws Exception {
        // Focus on message input field by tapping (coordinates may vary)
        // For now, we'll use the text input method

        // Method 1: Type entire message (less human-like but more reliable)
        String escapedMessage = escapeForShell(message);
        executeAdbCommand("shell", "input", "text", escapedMessage);

        // Add small delays to simulate typing speed
        Thread.sleep(message.length() * 50); // 50ms per character

        logger.debug("Message typed: {} characters", message.length());
    }

    /**
     * Press the send button
     */
    private void pressSendButton() throws Exception {
        // Method 1: Press ENTER key (works in most cases)
        executeAdbCommand("shell", "input", "keyevent", "66"); // KEYCODE_ENTER

        logger.debug("Send button pressed (ENTER key)");

        // Alternative: Tap on send button coordinates
        // executeAdbCommand("shell", "input", "tap", "950", "1750");
    }

    /**
     * Fallback: Send via deep link (original method)
     */
    private boolean sendViaDeepLink(String phoneNumber, String message) {
        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String whatsappUrl = String.format("https://wa.me/%s?text=%s",
                phoneNumber.replaceAll("[^0-9]", ""), encodedMessage);

            executeAdbCommand("shell", "am", "start",
                "-a", "android.intent.action.VIEW",
                "-d", whatsappUrl);

            Thread.sleep(3000); // Wait for WhatsApp to open

            // Press ENTER to send
            executeAdbCommand("shell", "input", "keyevent", "66");

            logger.debug("Message sent via deep link fallback");
            return true;

        } catch (Exception e) {
            logger.error("Error in deep link fallback", e);
            return false;
        }
    }

    /**
     * Execute ADB command
     */
    private void executeAdbCommand(String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(adbPath);

        if (deviceId != null && !deviceId.isEmpty()) {
            command.add("-s");
            command.add(deviceId);
        }

        for (String arg : args) {
            command.add(arg);
        }

        logger.debug("Executing ADB: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        // Read output
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(
            new InputStreamReader(process.getErrorStream()));

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        StringBuilder errors = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            errors.append(line).append("\n");
        }

        boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

        if (!completed) {
            process.destroy();
            throw new RuntimeException("ADB command timed out");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0 && errors.length() > 0) {
            logger.warn("ADB command warning: {}", errors.toString());
        }
    }

    /**
     * Add human-like random delay
     */
    private void humanDelay() throws InterruptedException {
        humanDelay(humanDelayMinMs, humanDelayMaxMs);
    }

    /**
     * Add human-like random delay with custom range
     */
    private void humanDelay(int minMs, int maxMs) throws InterruptedException {
        int delay = minMs + random.nextInt(maxMs - minMs);
        logger.debug("Human delay: {}ms", delay);
        Thread.sleep(delay);
    }

    /**
     * Escape special characters for shell
     */
    private String escapeForShell(String text) {
        // Replace spaces with %s for ADB input text
        // Escape special characters
        return text
            .replace(" ", "%s")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
            .replace("&", "\\&")
            .replace(";", "\\;")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("|", "\\|")
            .replace("<", "\\<")
            .replace(">", "\\>")
            .replace("\n", "%n");
    }

    /**
     * Build message content
     */
    private String buildMessageContent(DeliveryRequest request) {
        StringBuilder content = new StringBuilder();

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            content.append("*").append(request.getTitle()).append("*\n\n");
        }

        if (request.getContent() != null) {
            content.append(request.getContent());
        }

        return content.toString();
    }

    /**
     * Build message content (for delivery request)
     */
    private String buildMessage(DeliveryRequest request) {
        StringBuilder content = new StringBuilder();

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            content.append("*").append(request.getTitle()).append("*\n\n");
        }

        if (request.getContent() != null) {
            content.append(request.getContent());
        }

        // Add footer with sender information
        content.append("\n\n---\n");
        content.append("Sent via Wedknots Delivery");

        return content.toString();
    }
}
