# WhatsApp Flow Publishing - Public Key Signing Guide

## Error Message
```
You need to upload and sign a public key to a phone number before you can send or publish a Flow.
```

## Problem Explanation

This error occurs when trying to publish a WhatsApp Flow because Meta requires:
1. A public key to be uploaded to your WhatsApp Business Phone Number
2. The phone number to be "signed" with that key
3. Both must be completed before you can publish flows

This is a **Meta/WhatsApp Business security requirement**, NOT a code issue in the application.

---

## Solution Steps

### Step 1: Generate a Public-Private Key Pair

You need to create an RSA key pair. Use one of these methods:

#### Option A: Using OpenSSL (Command Line)

```bash
# Generate a 2048-bit RSA private key
openssl genrsa -out private_key.pem 2048

# Extract the public key from the private key
openssl rsa -in private_key.pem -pubout -out public_key.pem

# Display the public key (you'll need this in the next step)
cat public_key.pem
```

#### Option B: Using OpenSSL (Windows PowerShell)

```powershell
# If OpenSSL is installed via Git Bash or WSL
openssl genrsa -out private_key.pem 2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
Get-Content public_key.pem
```

#### Option C: Online Key Generation

Use an online RSA key generator:
- [RSA Key Generator](https://www.openssl.org/docs/man1.1.1/man1/openssl-genrsa.html)
- Or use Meta's own tools if available

### Step 2: Upload Public Key to Meta Business Manager

1. **Login to Meta Business Manager**
   - Go to [business.facebook.com](https://business.facebook.com)
   - Navigate to **Settings → Account Settings**

2. **Find Your WhatsApp Business Account**
   - Go to **WhatsApp → Settings**
   - Select your WhatsApp Business Account

3. **Add Public Key**
   - Look for **"Public Key"** or **"Security"** section
   - Click **"Add Public Key"** or **"Upload"**
   - Paste the entire public key content (including `-----BEGIN PUBLIC KEY-----` and `-----END PUBLIC KEY-----`)

4. **Sign the Phone Number**
   - Once the public key is uploaded, Meta will show an option to **"Sign Phone Number"**
   - Click that button
   - This signs your phone number with the uploaded key

### Step 3: Verify in Meta Business Manager

1. Go to **WhatsApp → Phone Numbers**
2. Select your phone number
3. Verify that the status shows:
   - ✅ Public Key: **Uploaded**
   - ✅ Signed: **Yes**

### Step 4: Publish Your Flow

Once both steps are complete:

1. Go to **WhatsApp → Flows**
2. Select your RSVP flow (or create a new one)
3. Click **"Publish"**
4. The flow should now publish successfully

If you still get an error, wait a few minutes for Meta to process the changes (sometimes takes 5-15 minutes).

---

## Important Notes

### Security Considerations
- **Keep your private key safe** - Never share it or commit it to version control
- Store the private key securely (e.g., in an encrypted config file or secrets manager)
- You may need the private key later if you need to sign flow responses

### Multiple Phone Numbers
If you have multiple WhatsApp Business Phone Numbers:
- Each phone number needs its own public key uploaded
- Each phone number must be signed separately

### Certificate Expiration
- RSA keys don't expire on their own
- But best practice is to rotate keys periodically (e.g., annually)

### Flow vs Regular Messages
- **Regular messages** (templates) don't need key signing
- **Flows** require this signing before publishing
- Ensure you're trying to publish a Flow, not a template

---

## Troubleshooting

### Problem: "Public Key Upload Failed"
- **Solution:** Make sure you're copying the ENTIRE public key content including the header and footer lines
  ```
  -----BEGIN PUBLIC KEY-----
  [key content here]
  -----END PUBLIC KEY-----
  ```

### Problem: "Phone Number Signing Failed"
- **Solution:** 
  - Try refreshing the Meta Business Manager page
  - Wait 5-10 minutes and try again
  - Verify the public key is fully uploaded first

### Problem: "Still Can't Publish After Signing"
- **Solution:**
  - Go to **WhatsApp → Settings → API**, clear any cached data
  - Logout and login to Meta Business Manager
  - Check that you're using the correct WhatsApp Business Phone Number ID

### Problem: "Invalid Key Format"
- **Solution:** Make sure you're using an RSA public key in PEM format:
  ```
  -----BEGIN PUBLIC KEY-----
  MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...
  -----END PUBLIC KEY-----
  ```
  - NOT in PKCS#1 format (`-----BEGIN RSA PUBLIC KEY-----`)

---

## Application Configuration

After completing the Meta setup, your application is already configured to send flows.

The flow trigger endpoint is:
```
POST /api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}
```

Example request:
```bash
curl -X POST http://localhost:8080/api/whatsapp/flow/trigger-rsvp/1/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"
```

The application will:
1. Look up the guest and event
2. Build the flow data with wedding details
3. Send the flow to the guest's phone number using the Flow ID from `application.yml`

---

## References

- [Meta WhatsApp Flow Documentation](https://developers.facebook.com/docs/whatsapp/flows/overview)
- [WhatsApp Business API - Getting Started](https://developers.facebook.com/docs/whatsapp/getting-started)
- [RSA Key Generation with OpenSSL](https://www.openssl.org/docs/man1.1.1/man1/openssl-genrsa.html)

