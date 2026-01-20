# WhatsApp RSVP Template Implementation Guide

## Overview

This implementation provides a complete solution for sending WhatsApp RSVP invitations using WhatsApp's Flow feature (v7.3) with the Data API v3.0.

## Architecture

### Components

1. **WhatsAppFlowController** - Main controller for handling flow requests and sending template messages
2. **InvitationUIController** - UI controller for managing invitation template configuration and sending
3. **WhatsAppTemplatePayload** - DTO for template message structure
4. **invitation_template_config.html** - UI template for configuration and preview

## Flow Structure

```
WELCOME_SCREEN (Invitation Display)
    ↓
RSVP_SCREEN (Response Selection)
    ↓
ATTENDING_SCREEN (Guest Count)
    ↓
ATTENDEE_COUNT_SCREEN (Travel Mode)
    ↓
TRAVEL_SCREEN (Travel Details)
    ↓
SUCCESS_SCREEN (Confirmation)
```

## Template Message Structure

### Payload Format

```json
{
  "messaging_product": "whatsapp",
  "recipient_type": "individual",
  "to": "+1234567890",
  "type": "template",
  "template": {
    "name": "rsvp_flow",
    "language": {
      "code": "en"
    },
    "components": [
      {
        "type": "body",
        "parameters": [
          {"type": "text", "text": "John Doe"},
          {"type": "text", "text": "Alice & Bob"},
          {"type": "text", "text": "February 14, 2026"},
          {"type": "text", "text": "Grand Hotel, London"}
        ]
      },
      {
        "type": "button",
        "sub_type": "flow",
        "index": 0,
        "parameters": {
          "flow_action_data": {
            "flow_id": "YOUR_FLOW_ID",
            "flow_token": "unique_token_123",
            "mode": "published",
            "flow_action": "navigate",
            "screen": "WELCOME_SCREEN",
            "flow_data": {
              "guest_id": "123",
              "event_id": "456",
              "guest_name": "John Doe",
              "bride_name": "Alice",
              "groom_name": "Bob",
              "wedding_date": "February 14, 2026",
              "wedding_location": "Grand Hotel, London"
            }
          }
        }
      }
    ]
  }
}
```

## Body Parameters

The template body contains 4 replaceable parameters:

1. **{1}** - Guest Name
2. **{2}** - Couple Names (Bride & Groom)
3. **{3}** - Wedding Date
4. **{4}** - Wedding Location

These are passed as dynamic parameters in the API request.

## Flow Button Configuration

- **Type**: `button`
- **Sub-type**: `flow` (Complete flow action)
- **Index**: `0` (First button in the component)
- **Flow ID**: Your WhatsApp Flow ID
- **Initial Screen**: `WELCOME_SCREEN`
- **Flow Data**: Initial data available to the flow

## Data Flow Through Screens

### WELCOME_SCREEN Data Model
```json
{
  "guest_id": "123",
  "event_id": "456",
  "guest_name": "John Doe",
  "bride_name": "Alice",
  "groom_name": "Bob",
  "wedding_date": "February 14, 2026",
  "wedding_location": "Grand Hotel, London"
}
```

### RSVP_SCREEN Payload
```json
{
  "guest_id": "${data.guest_id}",
  "event_id": "${data.event_id}",
  "guest_name": "${data.guest_name}",
  "rsvp_status": "${form.rsvp_status}"
}
```

### SUCCESS_SCREEN Payload
```json
{
  "guest_id": "${data.guest_id}",
  "event_id": "${data.event_id}",
  "guest_name": "${data.guest_name}",
  "rsvp_status": "${data.rsvp_status}",
  "attendee_count": "${data.attendee_count}",
  "travel_mode": "${data.travel_mode}",
  "travel_details": "${data.travel_details}"
}
```

## Usage

### 1. Configure WhatsApp Template

First, create the `rsvp_flow` template in WhatsApp Manager:

1. Go to WhatsApp Manager > Message Templates
2. Create new template: `rsvp_flow`
3. Language: English
4. Body text:
   ```
   Hi {1}! 👋
   You're invited to the wedding of {2}
   📅 {3}
   📍 {4}
   ```
5. Add button: "Complete flow" type
6. Get the Flow ID after creating your flow

### 2. Send Single Invitation

```bash
POST /event/{eventId}/invitation/send/{guestId}
```

### 3. Send Batch Invitations

```bash
POST /event/{eventId}/invitation/send-batch
Content-Type: application/x-www-form-urlencoded

guestIds=1,2,3,4,5
```

### 4. Access Configuration UI

```
GET /event/{eventId}/invitation
```

Shows:
- Template preview
- Component configuration
- Send invitation interface
- Flow routing visualization

## API Integration Points

### WhatsApp Message Send
- **Endpoint**: `https://graph.facebook.com/v24.0/{PHONE_NUMBER_ID}/messages`
- **Method**: POST
- **Auth**: Bearer token (WhatsApp Access Token)
- **Content-Type**: application/json

### Flow Webhook Callback
- **Endpoint**: `POST /api/whatsapp/flow/webhook`
- **Receives**: Encrypted flow data (payloads from each screen)
- **Response**: Encrypted response (AES-GCM with inverted IV)

## Captured RSVP Data

When guest completes the flow, the following data is captured:

```java
{
  "guest_id": "123",
  "event_id": "456",
  "guest_name": "John Doe",
  "rsvp_status": "attending" | "not_attending",
  "attendee_count": "2",
  "travel_mode": "flight" | "train" | "car" | "other",
  "travel_details": "AI 123, arriving 2:30 PM"
}
```

### Database Updates

On successful RSVP submission:
- Update `Guest.rsvpStatus` with response
- Update `Guest.attendeeCount` if provided
- Update `Guest.travelMode` and `Guest.travelDetails`
- Log RSVP record with timestamp

## Error Handling

### Template Send Errors

| Error Code | Meaning | Solution |
|-----------|---------|----------|
| 131009 | Invalid components | Verify template format and button type |
| 131008 | Invalid parameter | Ensure all 4 body parameters provided |
| 100 | Invalid parameter | Check phone number format (with country code) |
| 470 | Rate limit | Wait before sending more messages |

### Flow Webhook Errors

- Missing encryption fields: Return 400 Bad Request
- Invalid guest_id: Return error response
- Database update failure: Return error with message
- Flow completion: Return success payload

## Configuration

### Environment Variables

```properties
whatsapp.flow.rsvp-flow-id=YOUR_FLOW_ID
whatsapp.webhook.app-secret=YOUR_APP_SECRET
whatsapp.webhook.private-key-path=/path/to/private_key.pem
```

### Wedding Event Configuration

Ensure WeddingEvent has:
- `whatsappApiEnabled = true`
- `whatsappPhoneNumberId` set
- `whatsappAccessToken` configured

## Security

### Encryption/Decryption

- Request: RSA (unwrap AES key) + AES-GCM (decrypt data)
- Response: AES-GCM (encrypt data with inverted IV)
- IV Inversion: Bitwise NOT of all IV bytes for response

### Authentication

- WhatsApp signature verification on webhook
- Bearer token authentication for sending

## Testing

### 1. Test Template Send

```bash
curl -X POST https://graph.facebook.com/v24.0/{PHONE_ID}/messages \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messaging_product": "whatsapp",
    "to": "+1234567890",
    "type": "template",
    "template": {
      "name": "rsvp_flow",
      "language": {"code": "en"},
      "components": [...]
    }
  }'
```

### 2. Test Flow Webhook

Send encrypted payload with valid signature and observe:
- Decryption succeeds
- Data is captured in database
- Encrypted response is returned

### 3. Test UI

1. Navigate to `/event/{eventId}/invitation`
2. Verify preview displays correctly
3. Send test invitations
4. Check webhook logs for data capture

## Troubleshooting

### "Parameter value is not valid" (131009)

- Verify button `sub_type` is `"flow"`
- Check `flow_action_data` structure
- Ensure all required flow fields present

### "Invalid phone number"

- Include country code (+1 for US, +44 for UK, etc.)
- Remove spaces, hyphens, or parentheses

### "Flow not published"

- Publish flow in WhatsApp Manager
- Verify flow ID matches
- Check mode is `"published"` not `"draft"`

### Webhook not receiving data

- Verify webhook URL is publicly accessible
- Check RSA private key is correct
- Verify payload encryption (IV inversion)
- Enable logs to debug decryption

## References

- [WhatsApp Cloud API - Flows](https://developers.facebook.com/docs/whatsapp/flows)
- [WhatsApp Flows - Implementing Endpoint](https://developers.facebook.com/docs/whatsapp/flows/guides/implementingyourflowendpoint)
- [WhatsApp Business API v24.0](https://developers.facebook.com/docs/whatsapp/cloud-api/reference)
- [RSVP Flow v7.3 Schema](./whatsapp-rsvp-flow-v7.3.json)

