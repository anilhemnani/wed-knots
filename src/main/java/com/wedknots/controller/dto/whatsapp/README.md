# WhatsApp Cloud API Data DTOs (v3.0)

This package contains Java DTO classes for WhatsApp Cloud API Data Exchange version 3.0.

## Architecture

All DTOs follow an inheritance-based design pattern to reduce code duplication:

### Base Classes

- **BaseFlowRequest**: Abstract base class for all request DTOs
  - Common fields: `version`, `action`, `flow_token`
  
- **BaseFlowResponse**: Abstract base class for all response DTOs
  - Common fields: `version`, `screen`, `data`, `error_message`

All DTOs use Lombok's `@SuperBuilder` annotation to enable builder pattern with inheritance.

## Core DTOs

### Request DTOs (extend BaseFlowRequest)

- **FlowRequest**: Base decrypted request from WhatsApp
  - Additional fields: `screen`, `data`
  
- **FlowDataApiRequest**: Standard data exchange request
  - Additional fields: `screen`, `data`
  
- **FlowDataExchange**: Data exchange between screens
  - Additional fields: `screen`, `data`

### Response DTOs (extend BaseFlowResponse)

- **FlowResponse**: Base response to WhatsApp
- **FlowDataApiResponse**: Standard data API response
- **FlowActionResponse**: Response for navigate/update_data/complete actions
- **FlowErrorResponse**: Standard error response format
  - Additional field: `error_code`
  
### Other DTOs

- **EncryptedFlowRequest**: Encrypted payload wrapper (encrypted_flow_data, encrypted_aes_key, initial_vector)
- **HealthCheckResponse**: Ping/health check response (extends BaseFlowRequest)

## Domain-Specific DTOs

### RSVP Flow

- **RsvpFlowData**: Initial data passed when launching RSVP flow
- **RsvpSubmissionData**: RSVP form submission data

## Usage Examples

### Using SuperBuilder with Inheritance

```java
// Build a FlowDataApiResponse
FlowDataApiResponse response = FlowDataApiResponse.builder()
    .version("3.0")           // from BaseFlowResponse
    .screen("SUCCESS_SCREEN") // from BaseFlowResponse
    .data(responseData)       // from BaseFlowResponse
    .build();

// Build a FlowErrorResponse
FlowErrorResponse error = FlowErrorResponse.builder()
    .version("3.0")
    .errorMessage("Invalid input")
    .errorCode("ERR_001")     // specific to FlowErrorResponse
    .build();
```

### Handling Encrypted Request

```java
@PostMapping("/webhook")
public ResponseEntity<String> handleWebhook(@RequestBody EncryptedFlowRequest request) {
    // Decrypt request
    byte[] iv = Base64.getDecoder().decode(request.getInitialVector());
    byte[] encryptedKey = Base64.getDecoder().decode(request.getEncryptedAesKey());
    byte[] encryptedData = Base64.getDecoder().decode(request.getEncryptedFlowData());
    
    // ... decrypt and process
    FlowDataApiRequest flowRequest = decrypt(encryptedData, aesKey, iv);
    
    // Build response using builder pattern
    FlowDataApiResponse response = FlowDataApiResponse.builder()
        .version("3.0")
        .screen("NEXT_SCREEN")
        .data(responseData)
        .build();
    
    // Encrypt and return
    String encryptedResponse = encrypt(response, aesKey, flipIv(iv));
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body(encryptedResponse);
}
```

### Sending RSVP Flow

```java
RsvpFlowData flowData = RsvpFlowData.builder()
    .guestId("123")
    .guestName("John Doe")
    .eventId("456")
    .brideName("Jane")
    .groomName("Jack")
    .weddingDate("2026-06-15")
    .weddingLocation("London")
    .build();

WhatsAppFlowPayload payload = WhatsAppFlowPayload.builder()
    .messagingProduct("whatsapp")
    .to("+441234567890")
    .type("interactive")
    .interactive(Interactive.builder()
        .type("flow")
        .action(Action.builder()
            .flowId("FLOW_ID")
            .flowToken(UUID.randomUUID().toString())
            .mode("published")
            .initialScreen("WELCOME_SCREEN")
            .data(flowData)
            .build())
        .build())
    .build();
```

### Handling RSVP Submission

```java
if ("data_exchange".equals(flowRequest.getAction())) {
    RsvpSubmissionData submission = objectMapper.convertValue(
        flowRequest.getData(), 
        RsvpSubmissionData.class
    );
    
    // Save RSVP
    saveRsvp(submission);
    
    // Return success response using builder
    return FlowDataApiResponse.builder()
        .version("3.0")
        .screen("SUCCESS_SCREEN")
        .data(Map.of("message", "RSVP submitted successfully"))
        .build();
}
```

## Benefits of the Refactored Design

1. **Code Reusability**: Common fields are defined once in base classes
2. **Type Safety**: Inheritance ensures all DTOs have required fields
3. **Maintainability**: Changes to common fields only need to be made once
4. **Builder Pattern**: `@SuperBuilder` enables fluent API across inheritance hierarchy
5. **Consistency**: All DTOs follow the same pattern and structure

## Encryption Details (Data API v3.0)

### Request Decryption

1. Decrypt `encrypted_aes_key` using RSA private key (OAEP SHA-256)
2. Decrypt `encrypted_flow_data` using AES-128-GCM with decrypted key and `initial_vector`

### Response Encryption

1. Flip/invert all bits of `initial_vector` to create response IV
2. Encrypt JSON response using AES-128-GCM with same AES key and flipped IV
3. Base64 encode the result (includes 16-byte auth tag)
4. Return as `text/plain`

## References

- [WhatsApp Cloud API - Flows](https://developers.facebook.com/docs/whatsapp/flows)
- [WhatsApp Flows - Data Exchange](https://developers.facebook.com/docs/whatsapp/flows/guides/implementingyourflowendpoint)
- [WhatsApp Business API v18.0](https://developers.facebook.com/docs/whatsapp/cloud-api)

