# WhatsApp Flow Publishing - Error Explained

## The Error Message
```
❌ You need to upload and sign a public key to a phone number 
   before you can send or publish a Flow.
```

---

## What This Means

This is **NOT a bug in your application code**. This is a **Meta WhatsApp Business requirement**.

### Meta's Requirement
Meta requires that every WhatsApp Business Phone Number used for sending **Flows** must:
1. Have an RSA public key uploaded to its account
2. Have that phone number cryptographically "signed" with that key
3. Both must be verified by Meta before flows can be published

### Why Does Meta Do This?
- **Security:** Ensures only legitimate business accounts can send interactive flows
- **Authentication:** Verifies the phone number owner really controls the flows
- **Fraud Prevention:** Prevents unauthorized flow sending
- **Compliance:** Meets regulatory requirements in some countries

---

## The Two Parts of the Solution

### Part 1: Key Generation (Technical)
You need to create an **RSA cryptographic key pair**:
- **Private Key:** Secret key (like a password) - you keep this secret
- **Public Key:** Public key (like your username) - you upload this to Meta

These keys are mathematically paired such that:
- Anything encrypted with the public key can ONLY be decrypted with the private key
- Anything signed with the private key can ONLY be verified with the public key

### Part 2: Meta Registration (Administrative)
You need to:
1. Paste the public key into Meta Business Manager
2. Tell Meta to "sign" your phone number with that key
3. Wait for Meta to verify and process

---

## Why You Get This Error When Trying to Publish

When you click "Publish" on a WhatsApp Flow, Meta's backend:
1. Checks if your phone number has a public key uploaded → ❌ NO
2. Checks if phone number is signed → ❌ NO
3. Rejects the publish request with the error message

---

## Solution Overview

### Flow Chart
```
┌─────────────────────────────────────┐
│  Generate RSA Key Pair              │
│  - Private key (keep secret)        │
│  - Public key (upload to Meta)      │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Login to Meta Business Manager     │
│  https://business.facebook.com      │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Find Your WhatsApp Phone Number    │
│  - Go to WhatsApp → Settings        │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Upload Public Key                  │
│  - Paste into "Public Key" field    │
│  - Click Save/Upload                │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Sign Phone Number                  │
│  - Click "Sign Phone Number"        │
│  - Wait for processing              │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Verify Status in Meta              │
│  ✓ Public Key: Uploaded             │
│  ✓ Phone Signed: Yes                │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Publish Flow in Meta               │
│  - Go to Flows                      │
│  - Click "Publish"                  │
│  - ✓ SUCCESS!                       │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Update Application Config          │
│  - Put Flow ID in application.yml   │
│  - Restart application              │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│  Send Flows to Guests               │
│  - REST API triggers flow           │
│  - Guest receives WhatsApp flow     │
└─────────────────────────────────────┘
```

---

## Important Distinctions

### Flows vs Templates
- **Templates** (text messages): NO key signing required
- **Flows** (interactive experiences): KEY SIGNING REQUIRED

### Your Application
Your Wed Knots application is **correctly configured** for flows. 
What's missing is the **Meta Business configuration**, which is outside the application.

```
┌──────────────────────────────────────────┐
│  Application (Your Code) ✓ READY         │
│  - Flow trigger endpoints implemented    │
│  - Webhook handlers ready                │
│  - Database setup complete               │
└──────────────────────────────────────────┘
                    ↑
         NOT this (application code)
                    ↓
┌──────────────────────────────────────────┐
│  Meta Business Setup ❌ MISSING           │
│  - Public key NOT uploaded               │
│  - Phone number NOT signed               │
│  - This is the problem!                  │
└──────────────────────────────────────────┘
```

---

## Step-by-Step Solution

### Quick Start (5 steps)

1. **Generate keys:**
   ```powershell
   .\scripts\generate-whatsapp-keys.ps1
   ```
   This creates:
   - `C:\dev\projects\wed-knots\keys\private_key.pem`
   - `C:\dev\projects\wed-knots\keys\public_key.pem`

2. **Copy the public key** to clipboard or notepad

3. **Login to Meta Business Manager** → https://business.facebook.com

4. **Upload public key to your phone number**
   - WhatsApp → Settings → Your Phone Number → Upload Public Key
   - Wait for confirmation (usually instant)

5. **Sign your phone number**
   - Click "Sign Phone Number" button
   - Wait 5-15 minutes for processing

6. **Publish your flow**
   - Go to WhatsApp → Flows
   - Click Publish on your RSVP flow
   - ✓ Should now work!

### Detailed Steps
See: `WHATSAPP_FLOW_SETUP_STEPS.md`

### Troubleshooting
See: `WHATSAPP_FLOW_SIGNING_GUIDE.md`

---

## What NOT To Do

❌ **Don't:**
- Commit your private key to git
- Share your private key with anyone
- Use a personal WhatsApp account (must be Business)
- Try to publish before uploading and signing
- Generate multiple key pairs (use one)
- Manually modify the JSON flow file

✓ **Do:**
- Generate keys with the provided script
- Keep private key secure
- Use your WhatsApp Business phone number
- Follow Meta's dashboard UI exactly
- Wait for Meta to process changes
- Update application.yml after flow is published

---

## Timeline Expectations

| Step | Time | What To Do |
|------|------|-----------|
| Generate keys | 1 min | Run script |
| Copy public key | 1 min | Paste into Meta |
| Upload public key | 1 min | Click upload in Meta |
| Meta processes | 1-5 min | Wait |
| Sign phone number | 1 min | Click sign button |
| Meta verifies | 5-15 min | Wait and refresh |
| Publish flow | 1 min | Click publish |
| Update application | 1 min | Edit YAML file |
| Test with guest | 1-5 min | Send test flow |

**Total time:** 20-30 minutes

---

## After Setup Complete

Once you've completed the Meta setup:

1. **Your application is ready** to send flows immediately
2. **Use the REST API** to trigger flows:
   ```bash
   POST /api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}
   ```

3. **Flows go to guests** in WhatsApp
4. **Guest responses** come back via webhook
5. **Application stores** the RSVP data

---

## Still Getting Error?

If you've followed all steps and still see the error:

### Checklist
- [ ] Did you wait 15+ minutes after signing?
- [ ] Did you refresh Meta Business Manager page?
- [ ] Did you logout and login to Meta?
- [ ] Is your phone number showing "Signed: Yes"?
- [ ] Are you uploading to the RIGHT phone number?
- [ ] Is it a WhatsApp Business number (not personal)?

### If Still Stuck
1. Try in a different browser (Chrome instead of Firefox)
2. Clear browser cache completely
3. Wait another 15 minutes
4. Contact Meta Business Support
   - Go to: business.facebook.com → Help → Support
   - Describe: "Cannot publish WhatsApp Flow after key signing"

---

## Reference Documentation

- **Generated Files:**
  - Public Key: `C:\dev\projects\wed-knots\keys\public_key.pem`
  - Private Key: `C:\dev\projects\wed-knots\keys\private_key.pem`

- **Application Config:**
  - `src/main/resources/application.yml`
  - Update: `whatsapp.flow.rsvp-flow-id`

- **Flow File:**
  - `whatsapp-rsvp-flow-v7.3.json`

- **REST Endpoints:**
  - POST `/api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}`
  - POST `/api/whatsapp/flow/trigger-rsvp-batch/{eventId}`
  - GET `/api/whatsapp/flow/status/{eventId}`

---

## Summary

| Item | Status | Next Action |
|------|--------|-------------|
| Application Code | ✓ Ready | Nothing needed |
| WhatsApp Config | ✓ Configured | Nothing needed |
| Flow JSON | ✓ Valid | Nothing needed |
| **Public Key** | ❌ Missing | Generate + Upload |
| **Phone Signing** | ❌ Missing | Sign in Meta |
| Flow Publishing | ❌ Blocked | Publish after signing |

**You are 90% of the way there!** You just need to complete the Meta Business setup.

---

**Questions?** Check the companion guides:
- `WHATSAPP_FLOW_SIGNING_GUIDE.md` - Technical guide
- `WHATSAPP_FLOW_SETUP_STEPS.md` - Step-by-step with details

