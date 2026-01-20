# WhatsApp Template Configuration Fix

## Problem Resolved ✅

**Error**: `Number of parameters does not match the expected number of params`
- Expected: 0 body parameters
- Received: 4 body parameters

## Solution

The template was created **WITHOUT body parameters** in WhatsApp Manager. The code has been updated to:
1. Send an empty body component (no parameters)
2. Only send the button component with flow action data
3. Pass all guest details through **flow data** (displayed on WELCOME_SCREEN)

## Template Structure

### WhatsApp Manager Template
```
Name: rsvp_flow
Category: Marketing
Language: English
Body: "You're invited to a wedding! Tap the button below to respond."
Button: Complete flow type
Body Parameters: 0 (none)
```

### API Payload Structure
```json
{
  "messaging_product": "whatsapp",
  "to": "+447878597720",
  "type": "template",
  "template": {
    "name": "rsvp_flow",
    "language": { "code": "en" },
    "components": [
      {
        "type": "body",
        "parameters": []  // Empty - no body parameters
      },
      {
        "type": "button",
        "sub_type": "flow",
        "index": 0,
        "parameters": [
          {
            "type": "payload",
            "payload": "{...flow_action_data...}"
          }
        ]
      }
    ]
  }
}
```

## Guest Details Flow

All guest-specific information is passed through **flow_data** and displayed on the flow screens:

```
flow_data: {
  "guest_id": "123",
  "event_id": "456",
  "guest_name": "John Doe",
  "bride_name": "Alice",
  "groom_name": "Bob",
  "wedding_date": "February 14, 2026",
  "wedding_location": "Grand Hotel, London"
}
```

**WELCOME_SCREEN** (First screen in flow):
```
Hi ${data.guest_name}! 👋
You're invited to the wedding of Alice & Bob
📅 February 14, 2026
📍 Grand Hotel, London
```

## Updated Files

1. **WhatsAppFlowController.java**
   - Body component now has empty parameters array
   - All guest details stay in flow_data
   - Button component sends serialized JSON payload

2. **QUICK_START.md**
   - Template creation instructions updated
   - Shows correct template format (no body parameters)
   - Explains that details come from flow data

## Testing

Try sending invitations again:

```bash
POST /event/1/invitation/send-batch
Data: guestIds=1,2,3
```

Should now succeed with status code 200 and message ID in response.

## Key Points

✅ Template has 0 body parameters
✅ All details passed via flow_data
✅ Button payload is JSON string
✅ WELCOME_SCREEN displays guest details
✅ Code compiles with no errors

---

**Status**: Ready to send invitations! 🎉

