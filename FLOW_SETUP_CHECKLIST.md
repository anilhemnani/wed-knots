# WhatsApp Flow Setup - Checklist

## Complete this checklist to fix the flow publishing error

---

## Phase 1: Generate Keys ✓

- [ ] Open PowerShell (Windows Key + R → powershell)
- [ ] Navigate to project: `cd C:\dev\projects\wed-knots`
- [ ] Run: `.\scripts\generate-whatsapp-keys.ps1`
- [ ] Wait for script to complete
- [ ] Script creates files:
  - [ ] `keys/private_key.pem` (keep secret!)
  - [ ] `keys/public_key.pem` (upload to Meta)
- [ ] Copy the public key displayed on screen (starts with `-----BEGIN PUBLIC KEY-----`)
- [ ] Save public key to notepad/clipboard (you'll need it soon)

---

## Phase 2: Meta Business Manager - Upload Key ✓

- [ ] Go to: https://business.facebook.com
- [ ] Login with your Meta business account
- [ ] Navigate to: **WhatsApp → Settings** (or "Accounts & Settings")
- [ ] Find your WhatsApp Business Phone Number (should see a list)
- [ ] Click on your phone number to open its details
- [ ] Look for section titled:
  - [ ] "Public Key", OR
  - [ ] "Security Settings", OR
  - [ ] "Phone Number Security", OR
  - [ ] Similar security-related option
- [ ] Click "Add Public Key" or "Upload Key" button
- [ ] Paste the public key from Phase 1:
  ```
  -----BEGIN PUBLIC KEY-----
  [entire key content]
  -----END PUBLIC KEY-----
  ```
- [ ] Click "Save" or "Upload" button
- [ ] See confirmation: "Public Key Uploaded Successfully" or similar
- [ ] Take screenshot or note: ✓ Public key is now uploaded

---

## Phase 3: Meta Business Manager - Sign Phone Number ✓

- [ ] Stay on the same phone number details page
- [ ] Look for "Sign Phone Number" button or option
- [ ] Click "Sign Phone Number"
- [ ] You may see: "Signing in progress..." or similar
- [ ] Check status - should now show:
  - [ ] Phone Number Signed: **Yes** ✓
  - [ ] Status: **Active** or **Ready** ✓
- [ ] **⏱ IMPORTANT:** Wait 5-15 minutes for Meta to fully process
- [ ] After waiting, refresh the page to confirm status

---

## Phase 4: Verify Status in Meta ✓

- [ ] Refresh Meta Business Manager page (Ctrl+F5)
- [ ] Navigate back to your phone number
- [ ] Confirm you see:
  - [ ] ✓ Public Key: **Uploaded**
  - [ ] ✓ Phone Number Signed: **Yes**
  - [ ] ✓ Status: **Active/Ready**

If any status shows "Pending":
- [ ] Wait another 10 minutes
- [ ] Refresh page again
- [ ] Check status once more

---

## Phase 5: Publish Your Flow ✓

- [ ] In Meta Business Manager, go to: **WhatsApp → Flows**
- [ ] You should see your RSVP flow in the list
- [ ] Click on your RSVP flow (named something like "Wedding RSVP" or "rsvp-flow-v7.3")
- [ ] The flow editor should open
- [ ] Look for "Publish" button (usually top-right or top-center)
- [ ] Click "Publish"
- [ ] You should see: "Flow Published Successfully" or similar confirmation
- [ ] ✓ Flow is now published!

---

## Phase 6: Copy Flow ID ✓

- [ ] Stay on the Flows page in Meta
- [ ] Find your published flow in the list
- [ ] Look for the "Flow ID" (displayed in flow details)
- [ ] Flow ID format: `1234567890123456` (16+ digit number)
- [ ] Copy the Flow ID
- [ ] Save it somewhere (you'll need it next)

---

## Phase 7: Update Application Configuration ✓

- [ ] Open file: `src/main/resources/application.yml`
- [ ] Find section:
  ```yaml
  whatsapp:
    flow:
      rsvp-flow-id: "869533252625207"
  ```
- [ ] Replace the number with your Flow ID from Phase 6:
  ```yaml
  whatsapp:
    flow:
      rsvp-flow-id: "YOUR_FLOW_ID_HERE"
  ```
  Example:
  ```yaml
  whatsapp:
    flow:
      rsvp-flow-id: "1234567890123456"
  ```
- [ ] Save the file (Ctrl+S)

---

## Phase 8: Restart Application ✓

- [ ] Stop the running application (if any)
- [ ] In PowerShell/Terminal, navigate to project:
  ```powershell
  cd C:\dev\projects\wed-knots
  ```
- [ ] Clean and rebuild:
  ```powershell
  mvn clean install
  ```
- [ ] Wait for build to complete
- [ ] Start application:
  ```powershell
  mvn spring-boot:run
  ```
- [ ] Wait for: "Started WedKnotsApplication" or similar
- [ ] Application is now running with your Flow ID

---

## Phase 9: Test the Flow ✓

- [ ] Open your application: http://localhost:8080
- [ ] Login as a host or admin user
- [ ] Navigate to your wedding event
- [ ] Find the "Send RSVP Flow" or "WhatsApp RSVP" option
- [ ] Select a guest
- [ ] Click "Send RSVP Flow"
- [ ] You should see: "RSVP flow triggered successfully"
- [ ] Check the guest's WhatsApp:
  - [ ] Guest should receive a message with "Wedding RSVP" button
  - [ ] Tapping the button opens the interactive flow
  - [ ] Guest can now fill out the RSVP through the flow

---

## Phase 10: Verify Git Ignore ✓

- [ ] Open `.gitignore` file in project root
- [ ] Ensure it contains:
  ```
  keys/
  private_key.pem
  *.pem
  ```
- [ ] This prevents accidentally committing your secret key
- [ ] If not present, add it
- [ ] Save file

---

## Troubleshooting Checklist

### If Step 5 (Publish) Fails

- [ ] Did you wait 15+ minutes after Phase 3 (sign phone number)?
- [ ] Is the status showing "Signed: Yes"?
- [ ] Try refreshing Meta page: Ctrl+F5
- [ ] Try logout/login to Meta
- [ ] Wait another 10 minutes
- [ ] Try different browser
- [ ] Check if it's the right phone number

### If Flow Not Reaching Guest

- [ ] Is guest's phone number valid?
- [ ] Is guest's number in E.164 format (+[country][number])?
- [ ] Is WhatsApp API enabled for the event?
- [ ] Did you restart the application after Phase 7?
- [ ] Check application logs for errors
- [ ] Verify Flow ID in application.yml matches Meta flow

### If Status Shows "Pending"

- [ ] Wait 5-15 minutes
- [ ] Refresh page
- [ ] Logout/login to Meta
- [ ] Try in different browser
- [ ] Check Meta status page (might be maintenance)

---

## Final Verification

Once all phases are complete, you should have:

- [ ] ✓ RSA keys generated in `keys/` folder
- [ ] ✓ Public key uploaded to Meta
- [ ] ✓ Phone number signed in Meta
- [ ] ✓ Flow published in Meta
- [ ] ✓ Flow ID in application.yml
- [ ] ✓ Application restarted
- [ ] ✓ Keys added to .gitignore
- [ ] ✓ Test flow sent to guest successfully

---

## Success Indicators

### In Meta Business Manager
- ✓ Phone number shows "Signed: Yes"
- ✓ Public Key shows "Uploaded"
- ✓ Flow shows "Published"

### In Application
- ✓ No errors in logs related to flow
- ✓ REST API returns: `{"status": "success"}`
- ✓ Guest receives WhatsApp message with flow button

### On Guest's Phone
- ✓ WhatsApp shows new message with "Wedding RSVP"
- ✓ Tapping opens interactive flow
- ✓ Guest can fill out and submit RSVP

---

## Support & Documentation

If you need more details, see:

- **Quick Summary:** `QUICK_START_FLOW_SETUP.txt`
- **Detailed Guide:** `WHATSAPP_FLOW_SIGNING_GUIDE.md`
- **Step-by-Step Instructions:** `WHATSAPP_FLOW_SETUP_STEPS.md`
- **Why This Error:** `WHATSAPP_FLOW_ERROR_EXPLAINED.md`

---

## Time Estimate

| Phase | Time |
|-------|------|
| Phase 1: Generate keys | 1 min |
| Phase 2: Upload to Meta | 2 min |
| Phase 3: Sign phone | 1 min |
| Phase 4: Wait for processing | 5-15 min |
| Phase 5: Publish flow | 1 min |
| Phase 6: Copy Flow ID | 1 min |
| Phase 7: Update config | 1 min |
| Phase 8: Restart app | 2 min |
| Phase 9: Test | 2 min |
| **Total** | **15-30 min** |

---

## ✅ COMPLETION

Once you check all boxes above, your WhatsApp RSVP flow is fully functional!

**Date Completed:** _______________
**Tested with Guest:** _______________
**Flow ID Used:** _______________

---

Questions? Check the documentation files or contact Meta support if Meta is blocking the publish.

