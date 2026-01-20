# Template Variable Interpolation - Debugging Action Plan

## Issue: `${data.guest_name}` Still Showing Instead of "John Doe"

## What I've Changed

Updated `WhatsAppFlowController.java`:

1. ✅ Ensured `flow_data` is a proper nested object (not string)
2. ✅ Added comprehensive logging to trace data flow
3. ✅ Verified all 7 required fields are included
4. ✅ Payload is properly serialized as JSON string for button parameter

## Steps to Diagnose

### Step 1: Enable Debug Logging

Edit `application.yml` or `application-prod.yml`:

```yaml
logging:
  level:
    com.wedknots.web.WhatsAppFlowController: DEBUG
    com.wedknots: INFO
```

### Step 2: Send Test Invitation

```bash
POST http://localhost:8080/event/1/invitation/send-batch
Content-Type: application/x-www-form-urlencoded

guestIds=1
```

### Step 3: Check Application Logs

Look for these messages:

**✅ GOOD - Data is being sent:**
```
INFO: Flow Data Details: guest_name='John Doe', bride_name='Alice', groom_name='Bob', wedding_date='2026-02-14', wedding_location='Grand Hotel, London'
DEBUG: Complete flow action payload: {"flow_id":"...","flow_data":{"guest_id":"1","event_id":"456","guest_name":"John Doe",...}}
INFO: ✅ RSVP template flow sent successfully to +447878597720
```

**❌ BAD - Data is missing:**
```
INFO: Flow Data Details: guest_name='null', bride_name='null'
```

---

## Possible Root Causes

### 1. Guest Data Not in Database
Check if guest has data:
```sql
SELECT id, contact_name, contact_phone FROM guest WHERE id = 1;
```

If `contact_name` is NULL, update it:
```sql
UPDATE guest SET contact_name = 'John Doe' WHERE id = 1;
```

### 2. Wedding Event Data Not Set
Check event details:
```sql
SELECT id, name, bride_name, groom_name, date, place FROM wedding_event WHERE id = 456;
```

If any field is NULL, update it:
```sql
UPDATE wedding_event 
SET bride_name = 'Alice', groom_name = 'Bob', date = '2026-02-14', place = 'Grand Hotel, London'
WHERE id = 456;
```

### 3. Flow Configuration Issue
Verify in WhatsApp Manager:
- [ ] Flow ID is correct
- [ ] Flow is PUBLISHED (not draft)
- [ ] Flow version is 7.3
- [ ] WELCOME_SCREEN uses `${data.guest_name}` syntax
- [ ] Data fields in WELCOME_SCREEN match: guest_id, event_id, guest_name, bride_name, groom_name, wedding_date, wedding_location

### 4. Access Token Issue
Check token validity:
```
Is token expired?
Is it for the correct WhatsApp Business Account?
Does it have the right permissions?
```

---

## Expected Behavior After Fix

When guest receives message:
1. Button text: "You're invited to a wedding! Tap the button below to respond."
2. Guest taps button → Opens flow
3. WELCOME_SCREEN shows:
   ```
   Hi John Doe! 👋
   You're invited to the wedding of Alice & Bob
   📅 2026-02-14
   📍 Grand Hotel, London
   ```

**NOT**:
```
Hi ${data.guest_name}! 👋
```

---

## Quick Verification

From the logs, answer these:

1. **Is guest_name populated?**
   - ✅ YES (see "Flow Data Details: guest_name='John Doe'")
   - ❌ NO (see "guest_name='null'")

2. **Is payload sent successfully?**
   - ✅ YES (HTTP 200, see "✅ RSVP template flow sent")
   - ❌ NO (HTTP 4xx/5xx error)

3. **Does recipient see literal `${data.guest_name}`?**
   - If YES + step 1 YES + step 2 YES → Issue is WhatsApp Flow rendering
   - If YES + step 1 NO → Issue is database data missing
   - If NO → Issue is resolved! ✅

---

## Next Action

**Collect these logs and share:**

1. Enable DEBUG logging
2. Send one test invitation
3. Copy-paste the logs showing:
   - "Flow Data Details: ..."
   - "Complete flow action payload: ..."
   - "✅ RSVP template flow sent..." or error message

Then we can identify the exact issue!

---

## Code Changes Made

### buildFlowData() Method
- Now uses `LinkedHashMap` for consistent ordering
- Only includes 7 required fields
- Added debug logging

### sendRsvpFlow() Method
- Logs all flow data values before sending
- Logs complete payload JSON
- Shows success/failure with clear indicators (✅ / ❌)

### Payload Structure (Correct)
```json
{
  "type": "button",
  "sub_type": "flow",
  "index": 0,
  "parameters": [
    {
      "type": "payload",
      "payload": "{\"flow_id\":\"...\",\"flow_token\":\"...\",\"mode\":\"published\",\"flow_action\":\"navigate\",\"screen\":\"WELCOME_SCREEN\",\"flow_data\":{\"guest_id\":\"1\",\"event_id\":\"456\",\"guest_name\":\"John Doe\",\"bride_name\":\"Alice\",\"groom_name\":\"Bob\",\"wedding_date\":\"2026-02-14\",\"wedding_location\":\"London\"}}"
    }
  ]
}
```

---

**Status**: Ready to diagnose
**Action**: Enable logging and send test invitation
**Timeline**: We should see the issue in the logs

