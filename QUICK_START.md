# WhatsApp RSVP Implementation - Quick Start Guide

## 🎯 Quick Overview

This implementation allows you to send WhatsApp RSVP invitations using a complete flow (v7.3) that captures guest responses including attendance confirmation, guest count, travel mode, and travel details.

## 🚀 5-Minute Setup

### Step 1: WhatsApp Manager Setup
1. Go to **Meta Business Suite** → **WhatsApp Manager**
2. Navigate to **Message Templates**
3. Create new template:
   - **Template Name**: `rsvp_flow`
   - **Category**: Marketing
   - **Language**: English
   - **Body Text**: (Leave empty or use simple text - no parameters)
     ```
     You're invited to a wedding! Tap the button below to respond.
     ```
   - **Button**: "Complete flow" type
   - Click **Create**

4. Get your **Flow ID** (displayed after creation)
5. Get your **Phone Number ID** (from settings)
6. Generate **Access Token** (App Roles → Token)

**Note**: The template does NOT use body parameters. All guest-specific details (name, dates, location) are passed through the flow data and displayed on the WELCOME_SCREEN within the flow.

### Step 2: Application Configuration
1. Update `application.yml` or `application-prod.yml`:
   ```yaml
   whatsapp:
     flow:
       rsvp-flow-id: YOUR_FLOW_ID_HERE
     webhook:
       app-secret: YOUR_APP_SECRET_HERE
       private-key-path: /path/to/your/private_key.pem
   ```

2. Ensure your `WeddingEvent` entity has:
   - `whatsappApiEnabled = true`
   - `whatsappPhoneNumberId` set to your Phone Number ID
   - `whatsappAccessToken` set to your Access Token

### Step 3: Test the UI
```
http://localhost:8080/event/{eventId}/invitation
```

You should see:
- Invitation preview
- Template configuration details
- Send invitation form

### Step 4: Send Invitations

**Single Guest:**
```
POST /event/{eventId}/invitation/send/{guestId}
```

**Multiple Guests:**
```
POST /event/{eventId}/invitation/send-batch
guestIds=1,2,3,4,5
```

## 📊 Data Flow

```
Guest receives WhatsApp message
    ↓ Taps "Start RSVP" button
WELCOME_SCREEN (shows invitation details)
    ↓ Taps "Continue to RSVP"
RSVP_SCREEN (select attending/not attending)
    ↓ Taps "Continue"
ATTENDING_SCREEN (enter guest count)
    ↓ Taps "Next"
ATTENDEE_COUNT_SCREEN (select travel mode)
    ↓ Taps "Continue"
TRAVEL_SCREEN (enter travel details)
    ↓ Taps "Submit RSVP"
SUCCESS_SCREEN (confirmation message)
    ↓
Webhook receives encrypted data
    ↓
Data is decrypted & stored in database
    ↓
Guest RSVP status updated
```

## 💾 Captured Data

When guest completes the RSVP flow:

```json
{
  "guest_id": "123",
  "event_id": "456",
  "rsvp_status": "attending",
  "attendee_count": "2",
  "travel_mode": "flight",
  "travel_details": "AI 123, arriving 2:30 PM"
}
```

**Stored in Database:**
- `Guest.rsvpStatus` ← attendance confirmation
- `Guest.attendeeCount` ← number of guests
- `Guest.travelMode` ← transport method
- `Guest.travelDetails` ← travel info
- Timestamp automatically recorded

## 🔐 Encryption

The system automatically handles:
- **Request**: RSA decrypt AES key + AES-GCM decrypt payload
- **Response**: AES-GCM encrypt with inverted IV + Base64 encode

All encryption happens transparently in the webhook handler.

## 🛠️ Key Classes

| File | Purpose |
|------|---------|
| `InvitationUIController.java` | UI endpoints & invitation management |
| `WhatsAppFlowController.java` | Flow webhook handling & encryption |
| `WhatsAppTemplatePayload.java` | DTO for template message format |
| `invitation_template_config.html` | Web UI for configuration |

## ✅ Checklist

- [ ] Flow ID obtained from WhatsApp Manager
- [ ] Phone Number ID configured
- [ ] Access Token generated
- [ ] Environment variables set
- [ ] WeddingEvent has WhatsApp details
- [ ] Private key file (`.pem`) is accessible
- [ ] Webhook URL is publicly accessible
- [ ] Tested single invitation send
- [ ] Tested batch invitation send
- [ ] Verified webhook receives data
- [ ] Checked database for captured RSVP

## 🔍 Testing

### Test 1: UI Display
```
GET /event/1/invitation
Expected: Configuration UI with preview
```

### Test 2: Single Send
```
POST /event/1/invitation/send/1
Expected: Success message
```

### Test 3: Batch Send
```
POST /event/1/invitation/send-batch
Data: guestIds=1,2,3
Expected: Invitations sent to all guests
```

### Test 4: Webhook Response
When guest completes RSVP:
- Webhook receives encrypted payload
- Data is decrypted successfully
- Status code 200 returned
- Database updated

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| 131009 Error | Verify template format, button type is "Complete flow" |
| Phone rejected | Include country code (+44 for UK) |
| No webhook data | Check webhook URL, IP whitelist, SSL cert |
| Decryption fails | Verify private key file path, format is PKCS8 |
| Flow not opening | Ensure flow is published, check flow ID |

## 📞 Common Tasks

### Send Invitations to All Event Guests
```bash
curl -X POST http://localhost:8080/event/1/invitation/send-batch \
  -d "guestIds=1,2,3,4,5"
```

### Check RSVP Status
```sql
SELECT id, contact_name, rsvp_status, attendee_count, travel_mode 
FROM guest 
WHERE wedding_event_id = 1;
```

### Resend to Non-Responding Guests
```bash
curl -X POST http://localhost:8080/event/1/invitation/send-batch \
  -d "guestIds=2,5,7"
```

## 📚 Additional Resources

- Full Implementation Guide: `WHATSAPP_RSVP_IMPLEMENTATION.md`
- Flow Schema: `whatsapp-rsvp-flow-v7.3.json`
- Meta Docs: https://developers.facebook.com/docs/whatsapp/flows

---

**Ready to go!** 🎉

Start by visiting `/event/{eventId}/invitation` in your browser.

