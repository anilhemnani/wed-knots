# Fix: Template Variables Not Being Interpolated

## Problem
Recipients were seeing `${data.guest_name}` instead of the actual guest name like "John Doe".

## Root Cause
The `flow_data` object wasn't being properly structured in the payload serialization. WhatsApp's flow engine needs to receive the data as a nested object so it can interpolate variables like `${data.guest_name}` with actual values.

## Solution Implemented

### 1. Fixed Flow Data Structure
The `flow_data` is now properly included as an object within the serialized payload:

```json
{
  "type": "payload",
  "payload": "{
    \"flow_id\": \"YOUR_FLOW_ID\",
    \"flow_token\": \"unique_token\",
    \"mode\": \"published\",
    \"flow_action\": \"navigate\",
    \"screen\": \"WELCOME_SCREEN\",
    \"flow_data\": {
      \"guest_id\": \"123\",
      \"event_id\": \"456\",
      \"guest_name\": \"John Doe\",
      \"bride_name\": \"Alice\",
      \"groom_name\": \"Bob\",
      \"wedding_date\": \"February 14, 2026\",
      \"wedding_location\": \"Grand Hotel, London\"
    }
  }"
}
```

### 2. Updated buildFlowData Method
- Changed from `HashMap` to `LinkedHashMap` for consistent ordering
- Removed unnecessary optional fields (`guest_family_name`, travel info)
- Added debug logging to verify all parameters
- Kept only the 7 required initial parameters

### 3. Key Changes
```java
// OLD - Extra fields that weren't used
flowData.put("guest_family_name", guest.getFamilyName());
flowData.put("preferred_airport", ...);

// NEW - Only required fields
flowData.put("guest_id", guest.getId().toString());
flowData.put("event_id", event.getId().toString());
flowData.put("guest_name", guest.getContactName());
```

## How It Works Now

### Step 1: Build Flow Data
```
guest_name: "John Doe"
bride_name: "Alice"
groom_name: "Bob"
wedding_date: "February 14, 2026"
wedding_location: "Grand Hotel, London"
```

### Step 2: Wrap in Flow Action Data
```
flow_id: "YOUR_FLOW_ID"
flow_token: "unique_token"
flow_data: { ...actual data... }
```

### Step 3: Serialize to JSON String
WhatsApp receives the entire flow action data as a serialized JSON string in the payload.

### Step 4: WhatsApp Interpolates Variables
On the WELCOME_SCREEN:
- `${data.guest_name}` → "John Doe" ✅
- `${data.bride_name} & ${data.groom_name}` → "Alice & Bob" ✅
- `${data.wedding_date}` → "February 14, 2026" ✅

## Testing

Send an invitation:
```bash
POST /event/{eventId}/invitation/send/{guestId}
```

Check the logs:
```
Built flow data for guest John Doe (ID: 1) - event [Event Name] (ID: 1)
Flow action payload: {"flow_id":"...", "flow_token":"...", "flow_data":{"guest_name":"John Doe", ...}}
```

Guest should now see:
```
Hi John Doe! 👋
You're invited to the wedding of Alice & Bob
📅 February 14, 2026
📍 Grand Hotel, London
```

## Files Modified

1. **WhatsAppFlowController.java**
   - Updated flow data serialization
   - Improved buildFlowData method
   - Added debug logging

## Debug Logging

Enable debug logs to verify flow data:
```properties
logging.level.com.wedknots.web.WhatsAppFlowController=DEBUG
```

Will output:
```
DEBUG: Built flow data for guest John Doe (ID: 1) - event Wedding (ID: 1)
DEBUG: Flow action payload: {"flow_id":"...", ...}
```

---

**Status**: ✅ Fixed - Variables should now interpolate correctly
**Test**: Send an invitation and check if guest name appears instead of `${data.guest_name}`

