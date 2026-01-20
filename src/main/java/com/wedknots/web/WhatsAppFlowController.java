package com.wedknots.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedknots.controller.dto.WhatsAppFlowPayload;
import com.wedknots.controller.dto.whatsapp.EncryptedFlowRequest;
import com.wedknots.controller.dto.whatsapp.FlowRequest;
import com.wedknots.controller.dto.whatsapp.HealthCheckResponse;
import com.wedknots.model.Guest;
import com.wedknots.model.RSVP;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.time.Instant;
import java.util.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * Controller for triggering WhatsApp Flows (v7.3)
 * Handles RSVP flow initiation for wedding guests
 */
@Slf4j
@RestController
@RequestMapping("/api/whatsapp/flow")
@RequiredArgsConstructor
public class WhatsAppFlowController {

    private final WhatsAppService whatsAppService;
    private final GuestRepository guestRepository;
    private final WeddingEventRepository weddingEventRepository;
    private final RestTemplate restTemplate;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static class DecryptionInfo {
        public final String clearPayload;
        public final byte[] clearAesKey;

        public DecryptionInfo(String clearPayload, byte[] clearAesKey) {
            this.clearPayload = clearPayload;
            this.clearAesKey = clearAesKey;
        }
    }

    private static final int AES_KEY_SIZE = 128;
    private static final String KEY_GENERATOR_ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final String RSA_ENCRYPT_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String RSA_MD_NAME = "SHA-256";
    private static final String RSA_MGF = "MGF1";


    @Value("${whatsapp.flow.rsvp-flow-id}")
    private String rsvpFlowId;

    @Value("${whatsapp.webhook.app-secret:2572cfa8d9ad2ff2d315a034202950d6}")
    private String webhookAppSecret;

    @Value("${whatsapp.webhook.private-key-path:}")
    private String webhookPrivateKeyPath;

    /**
     * WhatsApp Manager webhook entry point (used for ping/health checks and encrypted callbacks)
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody EncryptedFlowRequest encryptedFlowRequest) {
        int responseCode;
        String response = null;
        try {
            final byte[] encrypted_flow_data = Base64.getDecoder().decode((String) encryptedFlowRequest.getEncryptedFlowData());
            final byte[] encrypted_aes_key = Base64.getDecoder().decode((String) encryptedFlowRequest.getEncryptedAesKey());
            final byte[] initial_vector = Base64.getDecoder().decode((String) encryptedFlowRequest.getInitialVector());
            final DecryptionInfo decryptionInfo = decryptRequestPayload(encrypted_flow_data, encrypted_aes_key, initial_vector);
            final String requestJson = decryptionInfo.clearPayload;
            log.info("Received {}",requestJson);
            JsonNode jsonNode = OBJECT_MAPPER.readTree(requestJson);
            switch (jsonNode.get("action").asText()) {
                case "ping": {
                    responseCode = 200;
                    response = OBJECT_MAPPER.writeValueAsString(HealthCheckResponse.builder().data(Map.of("status", "active")).build());
                    break;
                }
                case "INIT": {
                    final FlowRequest flowRequest = OBJECT_MAPPER.treeToValue(jsonNode, FlowRequest.class);
                    log.info("Received flow INIT request for screen: {}", flowRequest.getScreen());

                    // Echo back the data from the request so flow can use it
                    // This allows ${data.guest_name}, ${data.bride_name}, etc. to be interpolated
                    Map<String, Object> responseData = new LinkedHashMap<>();

                    if (flowRequest.getData() instanceof Map<?, ?>) {
                        Map<String, Object> requestData = (Map<String, Object>) flowRequest.getData();

                        // Copy all data fields for WELCOME_SCREEN rendering
                        responseData.put("data.guest_id", requestData.getOrDefault("guest_id", "123"));
                        responseData.put("data.event_id", requestData.getOrDefault("event_id", "111"));
                        responseData.put("data.guest_name", requestData.getOrDefault("guest_name", "GuestName"));
                        responseData.put("data.bride_name", requestData.getOrDefault("bride_name", "Pratibha"));
                        responseData.put("data.groom_name", requestData.getOrDefault("groom_name", "Kartik"));
                        responseData.put("data.wedding_date", requestData.getOrDefault("wedding_date", "22/12/2026"));
                        responseData.put("data.wedding_location", requestData.getOrDefault("wedding_location", "Kerala"));

                        log.info("INIT response data: guest_name='{}', bride_name='{}', groom_name='{}'",
                                responseData.get("guest_name"),
                                responseData.get("bride_name"),
                                responseData.get("groom_name"));
                    }

                    // Determine the screen name - use WELCOME_SCREEN if not provided or empty
                    String screenName = flowRequest.getScreen();
                    if (screenName == null || screenName.isEmpty() || screenName.isBlank()) {
                        screenName = "WELCOME_SCREEN";
                        log.info("INIT: No screen provided, defaulting to WELCOME_SCREEN");
                    }

                    responseCode = 200;
                    response = OBJECT_MAPPER.writeValueAsString(
                            com.wedknots.controller.dto.whatsapp.FlowDataApiResponse.builder()
                                    .screen(screenName)
                                    .data(responseData)
                                    .build()
                    );
                    log.info("INIT response: screen='{}', data keys={}", screenName, responseData.keySet());
                    break;
                }
                case "BACK": {
                    final FlowRequest flowRequest = OBJECT_MAPPER.treeToValue(jsonNode, FlowRequest.class);
                    log.info("Received flow BACK request for screen: {}", flowRequest.getScreen());

                    break;
                }
                case "data_exchange": {
                    final FlowRequest flowRequest = OBJECT_MAPPER.treeToValue(jsonNode, FlowRequest.class);
                    log.info("Received flow data_exchange from screen: {}", flowRequest.getScreen());

                    @SuppressWarnings("unchecked")
                    final Map<String, Object> requestData = flowRequest.getData() instanceof Map<?, ?>
                            ? (Map<String, Object>) flowRequest.getData()
                            : new LinkedHashMap<>();

                    Map<String, Object> responseData = new LinkedHashMap<>();
                    String currentScreen = flowRequest.getScreen();

                    log.info("Processing data_exchange from screen: {} - guest_id: {}, data: {}",
                            currentScreen, requestData.get("guest_id"), requestData);

                    // Echo back all request data so flow can continue rendering
                    responseData.putAll(requestData);

                    // Handle different screen types for logging and potential async processing
                    switch (currentScreen) {
                        case "RSVP_SCREEN": {
                            String rsvpStatus = String.valueOf(requestData.getOrDefault("rsvp_status", "pending"));
                            log.info("✅ Guest RSVP Status Received: {}", rsvpStatus);
                            break;
                        }
                        case "ATTENDING_SCREEN": {
                            String attendeeCount = String.valueOf(requestData.getOrDefault("attendee_count", "1"));
                            log.info("✅ Attendee Count Received: {}", attendeeCount);
                            break;
                        }
                        case "ATTENDEE_COUNT_SCREEN": {
                            String travelMode = String.valueOf(requestData.getOrDefault("travel_mode", ""));
                            log.info("✅ Travel Mode Received: {}", travelMode);
                            break;
                        }
                        case "TRAVEL_SCREEN": {
                            String travelDetails = String.valueOf(requestData.getOrDefault("travel_details", ""));
                            log.info("✅ Travel Details Received: {}", travelDetails);
                            break;
                        }
                        case "SUCCESS_SCREEN": {
                            log.info("✅ RSVP COMPLETED for guest: {} - Status: {}",
                                    requestData.getOrDefault("guest_name", "Unknown"),
                                    requestData.getOrDefault("rsvp_status", "Unknown"));

                            // Log complete RSVP data for audit trail
                            log.info("Final RSVP Data: {}", OBJECT_MAPPER.writeValueAsString(requestData));

                            // TODO: Async task to update guest RSVP/travel info in database
                            // For now, data is just logged for audit trail
                            break;
                        }
                        default: {
                            log.warn("Unknown screen in data_exchange: {}", currentScreen);
                        }
                    }

                    responseCode = 200;
                    response = OBJECT_MAPPER.writeValueAsString(
                            com.wedknots.controller.dto.whatsapp.FlowDataApiResponse.builder()
                                    .screen(currentScreen)
                                    .data(responseData)
                                    .build()
                    );
                    break;
                }
            }
            response = encryptAndEncodeResponse(response, decryptionInfo.clearAesKey, flipIv(initial_vector));
        } catch (Exception ex) {
            response = "Processing error: " + ex.getMessage();
            responseCode = 500;
        }
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(response);
    }

    private PrivateKey loadPrivateKey() throws Exception {
        if (webhookPrivateKeyPath == null || webhookPrivateKeyPath.isBlank()) {
            throw new IllegalStateException("whatsapp.webhook.private-key-path not configured");
        }
        byte[] pemBytes = Files.readAllBytes(Path.of(webhookPrivateKeyPath));
        String pem = new String(pemBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * Trigger RSVP flow for a guest
     * POST /api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/trigger-rsvp/{eventId}/{guestId}")
    public ResponseEntity<?> triggerRsvpFlow(
            @PathVariable Long eventId,
            @PathVariable Long guestId,
            @RequestBody(required = false) Map<String, String> customVariables) {
        try {
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
            Optional<Guest> guestOpt = guestRepository.findById(guestId);

            if (eventOpt.isEmpty() || guestOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Event or Guest not found"
                ));
            }

            WeddingEvent event = eventOpt.get();
            Guest guest = guestOpt.get();

            // Verify WhatsApp is configured
            if (!Boolean.TRUE.equals(event.getWhatsappApiEnabled()) || event.getWhatsappPhoneNumberId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "WhatsApp not configured for this event"
                ));
            }

            // Get guest phone number
            String guestPhoneNumber = guest.getContactPhone();
            if (guestPhoneNumber == null || guestPhoneNumber.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Guest phone number not found"
                ));
            }

            // Build flow data
            Map<String, Object> flowData = buildFlowData(event, guest, customVariables);

            // Send flow to guest
            sendRsvpFlow(event, guestPhoneNumber, flowData);

            log.info("RSVP flow triggered for guest {} ({}) in event {}",
                    guest.getContactName(), guestPhoneNumber, event.getName());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "RSVP flow triggered successfully",
                    "guest", guest.getContactName(),
                    "event", event.getName()
            ));

        } catch (Exception e) {
            log.error("Error triggering RSVP flow", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Generate a unique flow token
     */
    private String generateFlowToken() {
        return UUID.randomUUID().toString();
    }


    /**
     * Send RSVP flow via WhatsApp Cloud API using template message with flow button
     * Uses WhatsApp template format with "Complete flow" button action
     */
    private void sendRsvpFlow(WeddingEvent event, String toPhoneNumber, Map<String, Object> flowData) {
        try {
            String url = String.format(
                    "https://graph.facebook.com/v24.0/%s/messages",
                    event.getWhatsappPhoneNumberId()
            );

            // Build template components
            List<Map<String, Object>> components = new ArrayList<>();

            // Body component WITHOUT parameters (template has 0 body params)
            Map<String, Object> bodyComponent = new LinkedHashMap<>();
            bodyComponent.put("type", "body");
            bodyComponent.put("parameters", new ArrayList<>());
            components.add(bodyComponent);

            // Button component with flow action
            Map<String, Object> buttonComponent = new LinkedHashMap<>();
            buttonComponent.put("type", "button");
            buttonComponent.put("sub_type", "flow");
            buttonComponent.put("index", "0");

            // Flow action data as array of parameters
            List<Map<String, Object>> buttonParamsList = new ArrayList<>();
            Map<String, Object> flowActionParam = new LinkedHashMap<>();

            // Build the complete flow action data structure
            Map<String, Object> flowActionData = new LinkedHashMap<>();
            flowActionData.put("flow_id", rsvpFlowId);
            flowActionData.put("flow_token", generateFlowToken());
            flowActionData.put("mode", "published");
            flowActionData.put("flow_action", "navigate");
            flowActionData.put("screen", "WELCOME_SCREEN");
            flowActionData.put("flow_data", flowData);

            // The payload is the JSON string representation of the complete flow action data
            String payloadJson = OBJECT_MAPPER.writeValueAsString(flowActionData);
            log.info("Sending flow with data: guest_name={}, event_id={}",
                    flowData.get("guest_name"), flowData.get("event_id"));
            log.debug("Complete flow action payload: {}", payloadJson);

            flowActionParam.put("type", "payload");
            flowActionParam.put("payload", payloadJson);
            buttonParamsList.add(flowActionParam);

            buttonComponent.put("parameters", buttonParamsList);
            components.add(buttonComponent);

            // Build template structure
            Map<String, Object> template = new LinkedHashMap<>();
            template.put("name", "rsvp_flow");
            template.put("language", Map.of("code", "en"));
            template.put("components", components);

            // Build complete template message payload
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("messaging_product", "whatsapp");
            payload.put("recipient_type", "individual");
            payload.put("to", toPhoneNumber.replaceAll("[^0-9+]", ""));
            payload.put("type", "template");
            payload.put("template", template);

            log.info("Sending RSVP template to {} with flowData: {}", toPhoneNumber, flowData);
            log.info("Flow Data Details: guest_name='{}', bride_name='{}', groom_name='{}', wedding_date='{}', wedding_location='{}'",
                    flowData.get("guest_name"),
                    flowData.get("bride_name"),
                    flowData.get("groom_name"),
                    flowData.get("wedding_date"),
                    flowData.get("wedding_location"));

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(event.getWhatsappAccessToken());
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // Send request
            var response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ RSVP template flow sent successfully to {}", toPhoneNumber);
                log.info("Response: {}", response.getBody());
            } else {
                log.error("❌ Failed to send RSVP template flow: {} - {}", response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("Error sending RSVP template flow", e);
            throw new RuntimeException("Failed to send RSVP template flow: " + e.getMessage());
        }
    }

    /**
     * Helper method to create a text parameter for template body
     */
    private Map<String, Object> createTextParameter(String text) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("type", "text");
        param.put("text", text != null ? text : "");
        return param;
    }

    /**
     * Build flow data with wedding and guest information
     * Variable names use underscores to match WhatsApp Flow v7.3 schema
     */
    private Map<String, Object> buildFlowData(WeddingEvent event, Guest guest, Map<String, String> customVariables) {
        Map<String, Object> flowData = new LinkedHashMap<>();

        // Guest information (use lowercase - matches flow schema)
        flowData.put("guest_id", guest.getId().toString());
        flowData.put("event_id", event.getId().toString());
        flowData.put("guest_name", guest.getContactName());

        // Wedding information
        flowData.put("bride_name", event.getBrideName());
        flowData.put("groom_name", event.getGroomName());
        flowData.put("wedding_date", event.getDate() != null ? event.getDate().toString() : "TBD");
        flowData.put("wedding_location", event.getPlace() != null ? event.getPlace() : "TBD");

        log.debug("Built flow data for guest {} (ID: {}) - event {} (ID: {})",
                guest.getContactName(), guest.getId(),
                event.getName(), event.getId());

        // Add custom variables if provided
        if (customVariables != null) {
            flowData.putAll(customVariables);
        }

        return flowData;
    }

    /**
     * Send flow to multiple guests
     * POST /api/whatsapp/flow/trigger-rsvp-batch/{eventId}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/trigger-rsvp-batch/{eventId}")
    public ResponseEntity<?> triggerRsvpFlowBatch(
            @PathVariable Long eventId,
            @RequestBody List<Long> guestIds) {
        try {
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

            if (eventOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Event not found"
                ));
            }

            WeddingEvent event = eventOpt.get();

            if (!Boolean.TRUE.equals(event.getWhatsappApiEnabled())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "WhatsApp not configured for this event"
                ));
            }

            Map<String, Object> results = new HashMap<>();
            List<String> successful = new ArrayList<>();
            List<String> failed = new ArrayList<>();

            for (Long guestId : guestIds) {
                try {
                    Optional<Guest> guestOpt = guestRepository.findById(guestId);
                    if (guestOpt.isPresent()) {
                        Guest guest = guestOpt.get();
                        String guestPhoneNumber = guest.getContactPhone();

                        if (guestPhoneNumber != null && !guestPhoneNumber.isEmpty()) {
                            Map<String, Object> flowData = buildFlowData(event, guest, null);
                            sendRsvpFlow(event, guestPhoneNumber, flowData);
                            successful.add(guest.getContactName());
                        } else {
                            failed.add(guest.getContactName() + " (no phone)");
                        }
                    }
                } catch (Exception e) {
                    log.error("Error sending flow to guest {}", guestId, e);
                    failed.add("Guest #" + guestId);
                }
            }

            results.put("status", "completed");
            results.put("successful", successful);
            results.put("failed", failed);
            results.put("total_sent", successful.size());
            results.put("total_failed", failed.size());

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("Error triggering batch RSVP flow", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Get flow status
     * GET /api/whatsapp/flow/status/{eventId}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/status/{eventId}")
    public ResponseEntity<?> getFlowStatus(@PathVariable Long eventId) {
        try {
            Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);

            if (eventOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "Event not found"
                ));
            }

            WeddingEvent event = eventOpt.get();

            Map<String, Object> status = new HashMap<>();
            status.put("event_id", event.getId());
            status.put("event_name", event.getName());
            status.put("whatsapp_configured", event.getWhatsappApiEnabled());
            status.put("whatsapp_phone_number_id", event.getWhatsappPhoneNumberId());
            status.put("flow_id", rsvpFlowId);

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting flow status", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    private DecryptionInfo decryptRequestPayload(byte[] encrypted_flow_data, byte[] encrypted_aes_key, byte[] initial_vector) throws Exception {
        final RSAPrivateKey privateKey = readPrivateKeyFromPkcs8UnencryptedPem(this.webhookPrivateKeyPath);
        final byte[] aes_key = decryptUsingRSA(privateKey, encrypted_aes_key);
        return new DecryptionInfo(decryptUsingAES(encrypted_flow_data, aes_key, initial_vector), aes_key);
    }

    private String decryptUsingAES(final byte[] encrypted_payload, final byte[] aes_key, final byte[] iv) throws GeneralSecurityException {
        final GCMParameterSpec paramSpec = new GCMParameterSpec(AES_KEY_SIZE, iv);
        final Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aes_key, KEY_GENERATOR_ALGORITHM), paramSpec);
        final byte[] data = cipher.doFinal(encrypted_payload);
        return new String(data, StandardCharsets.UTF_8);
    }

    private byte[] decryptUsingRSA(final RSAPrivateKey privateKey, final byte[] payload) throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(RSA_ENCRYPT_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, new OAEPParameterSpec(RSA_MD_NAME, RSA_MGF, MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return cipher.doFinal(payload);
    }

    private RSAPrivateKey readPrivateKeyFromPkcs8UnencryptedPem(String filePath) throws Exception {
        final String prefix = "-----BEGIN PRIVATE KEY-----";
        final String suffix = "-----END PRIVATE KEY-----";
        String key = new String(Files.readAllBytes(new File(filePath).toPath()), StandardCharsets.UTF_8);
        if (!key.contains(prefix)) {
            throw new IllegalStateException("Expecting unencrypted private key in PKCS8 format starting with " + prefix);
        }
        String privateKeyPEM = key.replace(prefix, "").replaceAll("[\\r\\n]", "").replace(suffix, "");
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private String encryptAndEncodeResponse(final String clearResponse, final byte[] aes_key, final byte[] iv) throws GeneralSecurityException {
        final GCMParameterSpec paramSpec = new GCMParameterSpec(AES_KEY_SIZE, iv);
        final Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aes_key, KEY_GENERATOR_ALGORITHM), paramSpec);
        final byte[] encryptedData = cipher.doFinal(clearResponse.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private byte[] flipIv(final byte[] iv) {
        final byte[] result = new byte[iv.length];
        for (int i = 0; i < iv.length; i++) {
            result[i] = (byte) (iv[i] ^ 0xFF);
        }
        return result;
    }

}
