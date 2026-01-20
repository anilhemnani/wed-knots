# Meta Business Manager - Flow Setup Screenshots & Steps

## Complete Step-by-Step Guide

### Phase 1: Generate Keys

#### Step 1.1: Open PowerShell
```
1. Press Windows Key + R
2. Type: powershell
3. Press Enter
```

#### Step 1.2: Navigate to the project
```powershell
cd C:\dev\projects\wed-knots
```

#### Step 1.3: Run Key Generation Script
```powershell
.\scripts\generate-whatsapp-keys.ps1
```

This will:
- Generate a 2048-bit RSA private key
- Extract the public key
- Display the public key on screen
- Save keys to: `C:\dev\projects\wed-knots\keys\`

**Output will look like:**
```
========================================
PUBLIC KEY (Copy this to Meta Business Manager)
========================================

-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4f5wg5l2hKsT...
[many more characters]
-----END PUBLIC KEY-----

========================================
```

**Copy the entire public key including the header and footer lines.**

---

### Phase 2: Upload Key to Meta Business Manager

#### Step 2.1: Login to Meta Business Manager
- Go to: https://business.facebook.com
- Login with your Meta account

#### Step 2.2: Navigate to WhatsApp Settings
```
Left Navigation Menu:
  → WhatsApp
    → Settings (or "Accounts & Settings")
```

#### Step 2.3: Select Your Phone Number
```
Look for the section showing your phone numbers:
  Example: +44 1632 960123 (or your actual number)
  
Click on your phone number to select it
```

#### Step 2.4: Find the Security/Public Key Section
```
In the phone number details, look for:
  - "Security Settings"
  - "Public Key"
  - "Phone Number Signing"
  - Or similar security-related option
```

#### Step 2.5: Upload the Public Key
```
1. Click "Add Public Key" or "Upload Key"
2. You may be asked to:
   - Paste the key content directly, OR
   - Upload a file, OR
   - Enter it in a text field

3. Paste the entire public key (Step 1.3 output):
   -----BEGIN PUBLIC KEY-----
   [full key content]
   -----END PUBLIC KEY-----

4. Click "Save" or "Upload"
```

**Expected confirmation:** "Public Key Uploaded Successfully"

---

### Phase 3: Sign the Phone Number

#### Step 3.1: Sign Phone Number
```
After uploading the key, look for:
  - "Sign Phone Number" button/option
  - "Confirm Phone Number"
  - "Verify with Key"
  
Click this option
```

#### Step 3.2: Verify Signing Status
```
Once signing is complete, you should see:
  ✓ Public Key Status: Uploaded
  ✓ Phone Number Signed: Yes
  ✓ Status: Active/Ready
```

If you see any status as "Pending", wait 5-15 minutes for Meta to process.

---

### Phase 4: Publish Your Flow

#### Step 4.1: Go to Flows
```
Meta Business Manager:
  → WhatsApp
    → Flows
```

#### Step 4.2: Create or Select Your Flow
```
Option A: Create New Flow
  1. Click "Create Flow"
  2. Click "Import from JSON"
  3. Select: whatsapp-rsvp-flow-v7.3.json
  4. Click "Import"

Option B: Select Existing Flow
  1. In Flows list, find your RSVP flow
  2. Click on it to open
```

#### Step 4.3: Publish the Flow
```
1. In the flow editor, look for "Publish" button
   (Usually in top-right area)

2. Click "Publish"

3. You should see:
   - "Flow Published Successfully"
   - Or a confirmation dialog

4. The flow is now active and ready to send
```

#### Step 4.4: Copy Your Flow ID
```
After publishing:
1. In the Flows list, find your flow
2. Look for "Flow ID" (shown in details)
3. Copy the ID (looks like: 1234567890123456)
4. Save it - you'll need it in the next step
```

**Note:** If publishing still fails, try:
1. Refresh the page (Ctrl+F5)
2. Logout and login to Meta Business Manager
3. Wait 10 minutes and try again
4. Contact Meta Support if issue persists

---

### Phase 5: Configure Application

#### Step 5.1: Update application.yml
```
File: src/main/resources/application.yml

Find section:
  whatsapp:
    flow:
      rsvp-flow-id: "869533252625207"

Replace with your Flow ID from Step 4.4:
  whatsapp:
    flow:
      rsvp-flow-id: "YOUR_FLOW_ID_FROM_STEP_4_4"
```

Example:
```yaml
whatsapp:
  flow:
    # WhatsApp Flow ID for RSVP flow (v7.3)
    rsvp-flow-id: "1234567890123456"  # ← Replace with your actual Flow ID
```

#### Step 5.2: Restart Application
```powershell
# Stop the running application (if any)
# Then rebuild and restart:

cd C:\dev\projects\wed-knots
mvn clean install
mvn spring-boot:run
```

---

### Phase 6: Test the Flow

#### Step 6.1: Get Authentication Token
```bash
curl -X POST http://localhost:8080/login \
  -d "username=host1@example.com&password=host123"
```

Note the returned session token.

#### Step 6.2: Trigger Test Flow
```bash
curl -X POST http://localhost:8080/api/whatsapp/flow/trigger-rsvp/1/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json"
```

Expected response:
```json
{
  "status": "success",
  "message": "RSVP flow triggered successfully",
  "guest": "Guest Name",
  "event": "Wedding Event Name"
}
```

#### Step 6.3: Check Guest's Phone
```
The guest should receive a WhatsApp message with:
  - "Wedding RSVP" flow button
  - Option to open the interactive flow
  - If they click, they'll go through the RSVP steps
```

---

## Troubleshooting

### Issue: "Still Can't Publish After Signing"

**Check List:**
1. ✓ Did you wait 10 minutes after uploading the key?
2. ✓ Did you click "Sign Phone Number" explicitly?
3. ✓ Is the status showing "Signed: Yes"?
4. ✓ Are you using the correct phone number?
5. ✓ Is the phone number a WhatsApp Business number?

**Solutions:**
- Clear browser cache: Ctrl+Shift+Delete
- Logout completely from Meta
- Wait 15 minutes
- Try a different browser

### Issue: "Invalid Public Key Format"

**Solution:**
Make sure your key starts and ends with:
```
-----BEGIN PUBLIC KEY-----
[key content]
-----END PUBLIC KEY-----
```

NOT:
```
-----BEGIN RSA PUBLIC KEY-----  ← Wrong!
[key content]
-----END RSA PUBLIC KEY-----
```

### Issue: "Public Key Upload Failed"

**Solution:**
1. Check that you're copying the ENTIRE key (all lines)
2. Include the header and footer lines
3. Don't add extra spaces or line breaks
4. Try pasting into a text file first, then verify before uploading

### Issue: Flow Not Reaching Guest

**Check:**
1. ✓ Is WhatsApp API enabled in Event settings? (application.yml)
2. ✓ Is guest's phone number valid and in E.164 format?
3. ✓ Is the Flow ID correct in application.yml?
4. ✓ Is the flow published in Meta?
5. ✓ Check application logs for errors

---

## Common Mistakes to Avoid

1. **Using the wrong key format**
   - Generate with: `openssl genrsa` → `openssl rsa -pubout`
   - NOT with PKCS#1 format

2. **Not signing the phone number**
   - Uploading the key is NOT enough
   - You must also SIGN the phone number explicitly

3. **Using wrong phone number**
   - Make sure it's a WhatsApp Business number
   - Not a personal WhatsApp number

4. **Forgetting to update application.yml**
   - Flow ID must match the published flow
   - Must restart application after changing it

5. **Using old Flow ID**
   - If you re-import the flow, you get a NEW Flow ID
   - Must update the config with the new ID

---

## Reference URLs

- **Meta Business Manager:** https://business.facebook.com
- **WhatsApp Flows Documentation:** https://developers.facebook.com/docs/whatsapp/flows/overview
- **WhatsApp Cloud API Docs:** https://developers.facebook.com/docs/whatsapp/cloud-api/reference

---

## Security Reminders

⚠️ **IMPORTANT:**

1. **Never commit keys to git**
   - Add `keys/` to `.gitignore`
   - Never share private key with anyone

2. **Keep keys secure**
   - Store in encrypted location if possible
   - Rotate periodically (e.g., every year)
   - Don't store in email or cloud storage

3. **Only use public key for Meta**
   - Private key is NOT needed for the application
   - Private key is only used if you need to sign flow responses

---

## Next Steps After Setup

Once your flow is publishing and guests are receiving it:

1. **Test the complete flow** with a real guest
2. **Monitor webhook logs** to see flow responses
3. **Update guest record** with RSVP data from flow response
4. **Create notifications** for hosts when RSVPs are received

The application backend is already set up to:
- Trigger flows via REST API
- Receive flow completion data via webhook
- Store RSVP responses in database

