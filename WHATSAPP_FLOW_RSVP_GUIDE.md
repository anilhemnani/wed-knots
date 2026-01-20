# WhatsApp Flow RSVP Implementation Guide

## Flow Version
- **WhatsApp Flow Version:** 7.3
- **Data API Version:** 3.0
- **File:** `whatsapp-rsvp-flow-v7.3.json`

---

## Flow Structure

### Screens
1. **WELCOME_SCREEN** - Initial invitation display
2. **RSVP_SCREEN** - Yes/No attendance selection
3. **ATTENDING_SCREEN** - Guest count input (if attending)
4. **ATTENDEE_COUNT_SCREEN** - Travel mode selection
5. **TRAVEL_SCREEN** - Travel details input
6. **NOT_ATTENDING_SCREEN** - Message for couple (if not attending)
7. **SUCCESS_SCREEN** - Confirmation screen (terminal)

### Routing Model
The flow uses a `routing_model` that defines allowed navigation paths between screens.

---

## Setup Instructions

### Step 1: Import Flow to Meta Business Manager

1. Go to [Meta Business Manager](https://business.facebook.com/)
2. Navigate to **WhatsApp → Flows**
3. Click **Create Flow**
4. Choose **Import from JSON**
5. Upload `whatsapp-rsvp-flow-v7.3.json`
6. **Publish** the flow
7. Copy the **Flow ID** (looks like: `1234567890123456`)

### Step 2: Configure Application

Edit `src/main/resources/application.yml`:

```yaml
whatsapp:
  flow:
    rsvp-flow-id: "YOUR_FLOW_ID_HERE"  # Paste Flow ID from Meta
```

### Step 3: Test Flow Trigger

Use the REST API to trigger the flow:

```bash
POST http://localhost:8080/api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "WEDDING_IMAGE_URL": "https://example.com/wedding.jpg"
}
```

---

## Flow Data Variables

The flow expects these variables to be passed when triggered:

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| `guest_name` | string | Guest's first name | "John" |
| `guest_family_name` | string | Guest's family name | "Smith" |
| `bride_name` | string | Bride's name | "Sarah" |
| `groom_name` | string | Groom's name | "Michael" |
| `wedding_date` | string | Wedding date | "June 15, 2026" |
| `wedding_location` | string | Venue location | "Grand Hotel, Mumbai" |
| `event_id` | string | Event ID | "123" |
| `guest_id` | string | Guest ID | "456" |

### Optional Variables
| Variable | Type | Description |
|----------|------|-------------|
| `preferred_airport` | string | Preferred airport for travel |
| `preferred_station` | string | Preferred train station |

---

## Receiving Flow Responses

### Webhook Endpoint

WhatsApp will send the completed flow data to your webhook:

```
POST /api/whatsapp/webhook
```

### Response Payload Structure

When a guest completes the flow, you'll receive:

**For Attending:**
```json
{
  "entry": [{
    "changes": [{
      "value": {
        "messages": [{
          "type": "interactive",
          "interactive": {
            "type": "nfm_reply",
            "nfm_reply": {
              "response_json": {
                "guest_id": "456",
                "event_id": "123",
                "rsvp_status": "ATTENDING",
                "attendee_count": "3",
                "travel_mode": "flight",
                "travel_details": "AI 123, arriving 2:30 PM"
              }
            }
          }
        }]
      }
    }]
  }]
}
```

**For Not Attending:**
```json
{
  "entry": [{
    "changes": [{
      "value": {
        "messages": [{
          "type": "interactive",
          "interactive": {
            "type": "nfm_reply",
            "nfm_reply": {
              "response_json": {
                "guest_id": "456",
                "event_id": "123",
                "rsvp_status": "NOT_ATTENDING",
                "message": "Congratulations! Wish I could be there."
              }
            }
          }
        }]
      }
    }]
  }]
}
```

---

## Backend Implementation

### Update WhatsAppWebhookController

Add a method to handle flow responses:

```java
@PostMapping("")
public ResponseEntity<?> handleWebhookEvent(
        @RequestBody String payload,
        @RequestHeader(name = "X-Hub-Signature-256", required = false) String signature) {
    
    // ... existing code ...
    
    // Check for flow responses
    if (changes.get("value").has("messages")) {
        JsonNode messages = changes.get("value").get("messages");
        for (JsonNode message : messages) {
            if (message.has("interactive") && 
                message.get("interactive").get("type").asText().equals("nfm_reply")) {
                handleFlowResponse(message);
            }
        }
    }
    
    return ResponseEntity.ok(Map.of("success", true));
}

private void handleFlowResponse(JsonNode message) {
    try {
        String responseJson = message.get("interactive")
            .get("nfm_reply")
            .get("response_json")
            .asText();
        
        JsonNode response = objectMapper.readTree(responseJson);
        
        Long guestId = response.get("guest_id").asLong();
        Long eventId = response.get("event_id").asLong();
        String rsvpStatus = response.get("rsvp_status").asText();
        
        if ("ATTENDING".equals(rsvpStatus)) {
            int attendeeCount = response.get("attendee_count").asInt();
            String travelMode = response.has("travel_mode") ? 
                response.get("travel_mode").asText() : null;
            String travelDetails = response.has("travel_details") ? 
                response.get("travel_details").asText() : null;
            
            // Save RSVP
            rsvpService.updateRsvp(guestId, eventId, "ATTENDING", attendeeCount);
            
            // Save travel info if provided
            if (travelMode != null) {
                travelInfoService.saveTravelInfo(guestId, travelMode, travelDetails);
            }
            
        } else if ("NOT_ATTENDING".equals(rsvpStatus)) {
            String message = response.has("message") ? 
                response.get("message").asText() : null;
            
            // Save RSVP
            rsvpService.updateRsvp(guestId, eventId, "NOT_ATTENDING", 0);
            
            // Save message if provided
            if (message != null) {
                messageService.saveGuestMessage(guestId, eventId, message);
            }
        }
        
        log.info("Flow response processed for guest {} in event {}", guestId, eventId);
        
    } catch (Exception e) {
        log.error("Error processing flow response", e);
    }
}
```

---

## Testing the Flow

### 1. Via Admin Panel (Future Enhancement)

Add a button in the event management UI:

```html
<button onclick="sendRsvpFlow(${event.id}, ${guest.id})">
    📱 Send RSVP via WhatsApp
</button>

<script>
function sendRsvpFlow(eventId, guestId) {
    fetch(`/api/whatsapp/flow/trigger-rsvp/${eventId}/${guestId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getAuthToken()
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.status === 'success') {
            alert('RSVP flow sent to ' + data.guest);
        } else {
            alert('Error: ' + data.message);
        }
    });
}
</script>
```

### 2. Via Batch Send

Send to all guests:

```bash
POST /api/whatsapp/flow/trigger-rsvp-batch/123
Content-Type: application/json

[1, 2, 3, 4, 5]  # Guest IDs
```

Response:
```json
{
  "status": "completed",
  "successful": ["John Smith", "Jane Doe"],
  "failed": ["Bob Wilson (no phone)"],
  "total_sent": 2,
  "total_failed": 1
}
```

---

## Troubleshooting

### Error: "Flow ID not found"
- Ensure you've published the flow in Meta Business Manager
- Verify the Flow ID in `application.yml` matches the published flow

### Error: "Invalid flow data"
- Check that all required variables are being passed
- Ensure variable names match exactly (case-sensitive)

### Flow not appearing in WhatsApp
- Verify WhatsApp Business Account is properly configured
- Check that the phone number has the correct permissions
- Ensure the flow is in "Published" status (not "Draft")

### Response not received
- Check webhook URL is publicly accessible
- Verify webhook signature validation is working
- Check application logs for errors

---

## Flow Features

### ✅ Advantages
- **Interactive UI** - Native WhatsApp form experience
- **Data Validation** - Built-in input validation
- **Conditional Logic** - Different paths for attending/not attending
- **Data Collection** - Structured data returned via webhook
- **User-Friendly** - Familiar WhatsApp interface

### 🎯 Use Cases
- Wedding RSVP collection
- Guest count tracking
- Travel arrangement coordination
- Guest messages collection
- Attendance confirmation

---

## Next Steps

1. **Import the flow** to Meta Business Manager
2. **Copy Flow ID** to application.yml
3. **Test** with a single guest
4. **Monitor webhook** for responses
5. **Implement** response handler
6. **Add UI** for bulk sending
7. **Track** RSVP statistics

---

## Support

For issues or questions:
- Check Meta's [WhatsApp Flow Documentation](https://developers.facebook.com/docs/whatsapp/flows)
- Review application logs: `logs/application.log`
- Test webhook with Meta's test tool

---

**Created:** January 18, 2026
**Version:** 7.3 (data_api_version 3.0)
**Status:** ✅ Ready for Production

