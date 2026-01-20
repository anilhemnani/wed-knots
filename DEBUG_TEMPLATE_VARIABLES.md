# Debugging Template Variable Interpolation Issue

## Problem: ${data.guest_name} Not Being Replaced

The variables in the flow (like `${data.guest_name}`) are not being interpolated with actual guest data.

## Diagnostic Steps

### 1. Check Application Logs

Enable DEBUG logging for WhatsApp Flow Controller:

**application.yml**:
```yaml
logging:
  level:
    com.wedknots.web.WhatsAppFlowController: DEBUG
    com.wedknots: INFO
```

### 2. Send a Test Invitation

```bash
curl -X POST http://localhost:8080/event/1/invitation/send-batch \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "guestIds=1,2"
```

### 3. Look for These Log Messages

**Good logs** (data is being sent correctly):
```
INFO: Flow Data Details: guest_name='John Doe', bride_name='Alice', groom_name='Bob', wedding_date='...'
INFO: ✅ RSVP template flow sent successfully to +447878597720
DEBUG: Complete flow action payload: {"flow_id":"...", "flow_data":{"guest_name":"John Doe",...}}
```

**Bad logs** (data is missing):
```
INFO: Flow Data Details: guest_name='null', bride_name='null'
ERROR: ❌ Failed to send RSVP template flow
```

---

## Potential Issues & Solutions

### Issue 1: Guest Name is NULL
**Symptom**: `guest_name='null'` in logs

**Cause**: Guest's `contactName` field is empty in database

**Solution**:
```sql
SELECT id, contact_name, contact_phone FROM guest WHERE id = 1;
-- Should show actual name, not NULL
```

Update guest if needed:
```sql
UPDATE guest SET contact_name = 'John Doe' WHERE id = 1;
```

---

### Issue 2: Variables Show in Message But Not Replaced
**Symptom**: Message shows `${data.guest_name}` literally

**Cause**: WhatsApp Flow not receiving data correctly

**Debug**:
1. Check if flow_data is in the payload:
```json
{
  "flow_data": {
    "guest_name": "John Doe",
    "bride_name": "Alice",
    "groom_name": "Bob",
    "wedding_date": "February 14, 2026",
    "wedding_location": "Grand Hotel, London"
  }
}
```

2. Verify all 7 required fields are present (not 6, not 8)

---

### Issue 3: WhatsApp API Error
**Symptom**: HTTP 400 or 401 errors

**Common causes**:
- Invalid Flow ID
- Expired Access Token
- Flow not published
- Phone Number ID incorrect

**Debug**:
```bash
# Check credentials
echo "Flow ID: $WHATSAPP_FLOW_ID"
echo "Phone ID: $WHATSAPP_PHONE_ID"
echo "Token expires: (check in Meta Business Suite)"
```

---

## Full Data Flow Diagram

```
1. buildFlowData()
   ↓ Creates map with 7 fields
   
2. Map<String, Object> flowData
   {
     "guest_id": "1",
     "event_id": "456",
     "guest_name": "John Doe",
     "bride_name": "Alice",
     "groom_name": "Bob",
     "wedding_date": "2026-02-14",
     "wedding_location": "London"
   }
   ↓
   
3. sendRsvpFlow() wraps in flow_action_data
   {
     "flow_id": "YOUR_FLOW_ID",
     "flow_token": "...",
     "mode": "published",
     "flow_action": "navigate",
     "screen": "WELCOME_SCREEN",
     "flow_data": { ...7 fields above... }
   }
   ↓
   
4. Serialize to JSON string
   "{\"flow_id\":\"...\",\"flow_data\":{\"guest_name\":\"John Doe\",...}}"
   ↓
   
5. Wrap in button parameter
   {
     "type": "payload",
     "payload": "{...serialized JSON above...}"
   }
   ↓
   
6. Send to WhatsApp API
   ↓
   
7. WhatsApp Opens Flow
   ↓
   
8. WELCOME_SCREEN Renders
   ${data.guest_name} → "John Doe" ✅ (Should work now)
```

---

## Test with curl (Raw WhatsApp API)

```bash
curl -X POST https://graph.facebook.com/v24.0/YOUR_PHONE_ID/messages \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "messaging_product": "whatsapp",
    "to": "+447878597720",
    "type": "template",
    "template": {
      "name": "rsvp_flow",
      "language": {"code": "en"},
      "components": [
        {
          "type": "body",
          "parameters": []
        },
        {
          "type": "button",
          "sub_type": "flow",
          "index": 0,
          "parameters": [
            {
              "type": "payload",
              "payload": "{\"flow_id\":\"YOUR_FLOW_ID\",\"flow_token\":\"test\",\"mode\":\"published\",\"flow_action\":\"navigate\",\"screen\":\"WELCOME_SCREEN\",\"flow_data\":{\"guest_id\":\"1\",\"event_id\":\"456\",\"guest_name\":\"John Doe\",\"bride_name\":\"Alice\",\"groom_name\":\"Bob\",\"wedding_date\":\"2026-02-14\",\"wedding_location\":\"London\"}}"
            }
          ]
        }
      ]
    }
  }'
```

---

## Verification Checklist

- [ ] Application logs show `guest_name='John Doe'` (not null)
- [ ] Flow data has exactly 7 fields
- [ ] Payload is serialized JSON string
- [ ] Flow ID is correct and published
- [ ] Access Token is valid (not expired)
- [ ] Phone Number ID matches event configuration
- [ ] Guest record has `contact_name` populated
- [ ] Wedding event has bride_name and groom_name set

---

## Next Steps

1. **Enable debug logging**:
   ```yaml
   logging.level.com.wedknots.web.WhatsAppFlowController: DEBUG
   ```

2. **Send test invitation and capture logs**

3. **Share relevant log lines** to analyze further

4. **If still not working**: Check WhatsApp Flow validation using their validator tool

---

## Reference: Expected Flow Data Schema

From `whatsapp-rsvp-flow-v7.3.json`:

```json
{
  "guest_id": "string (required)",
  "event_id": "string (required)",
  "guest_name": "string (required)",
  "bride_name": "string (required)",
  "groom_name": "string (required)",
  "wedding_date": "string (required)",
  "wedding_location": "string (required)"
}
```

All 7 fields must be present and non-null.

