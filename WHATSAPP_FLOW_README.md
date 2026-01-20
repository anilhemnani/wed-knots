# WhatsApp Flow Public Key Setup - README

## 🚨 The Error

You're getting this error when trying to publish a WhatsApp Flow:

```
❌ You need to upload and sign a public key to a phone number 
   before you can send or publish a Flow.
```

**Good news:** This is not a code issue in your application. Your application code is **completely ready**. This is a Meta Business requirement that you need to complete separately.

---

## 📋 What You Need To Do

In summary:
1. **Generate** an RSA key pair
2. **Upload** the public key to Meta Business Manager
3. **Sign** your WhatsApp Business phone number
4. **Publish** your flow
5. **Update** your application with the Flow ID

**Time required:** 15-30 minutes

---

## 🚀 Quick Start

### For The Impatient
See: **`QUICK_START_FLOW_SETUP.txt`**

- 5-minute summary
- Essential steps only
- Key commands to run

### For Complete Instructions
See: **`FLOW_SETUP_CHECKLIST.md`**

- Detailed checklist
- Every step marked
- Troubleshooting guide included

---

## 📚 Detailed Documentation

We've created comprehensive guides to help you:

### 1. **WHATSAPP_FLOW_ERROR_EXPLAINED.md**
- **What:** Explains why this error happens
- **Why:** Meta's security requirements
- **Who:** Technical overview and diagrams
- **Read this if:** You want to understand the background

### 2. **WHATSAPP_FLOW_SIGNING_GUIDE.md**
- **What:** Technical guide to generating and uploading keys
- **Why:** Step-by-step with code examples
- **Who:** Developers/technical users
- **Read this if:** You want detailed technical information

### 3. **WHATSAPP_FLOW_SETUP_STEPS.md**
- **What:** Complete step-by-step with screenshots references
- **Why:** Very detailed with test procedures
- **Who:** Non-technical and technical users
- **Read this if:** You want to follow along carefully

### 4. **FLOW_SETUP_CHECKLIST.md**
- **What:** Checklist to track progress
- **Why:** Ensures nothing is missed
- **Who:** Anyone who wants to verify completion
- **Read this if:** You're doing the setup and want to check off each step

### 5. **QUICK_START_FLOW_SETUP.txt**
- **What:** Minimal instructions
- **Why:** Quick reference
- **Who:** Experienced users
- **Read this if:** You want the shortest version

---

## 🔧 The Setup Process

### Step 1: Generate Keys (1 minute)

```powershell
cd C:\dev\projects\wed-knots
.\scripts\generate-whatsapp-keys.ps1
```

This creates:
- `keys/private_key.pem` - **KEEP SECRET** (don't share or commit to git)
- `keys/public_key.pem` - Upload this to Meta

### Step 2: Upload to Meta (2 minutes)

1. Go to: https://business.facebook.com
2. Navigate: WhatsApp → Settings
3. Select your phone number
4. Find "Public Key" section
5. Paste the public key from Step 1
6. Click "Upload"

### Step 3: Sign Phone Number (5-15 minutes)

1. On the same phone number page
2. Click "Sign Phone Number"
3. Wait for status to show "Signed: Yes"
4. **Wait 10-15 minutes** for Meta to process

### Step 4: Publish Flow (1 minute)

1. Go to: WhatsApp → Flows
2. Click "Publish" on your RSVP flow
3. ✓ Should succeed now!
4. Copy the Flow ID

### Step 5: Update Application (1 minute)

1. Edit: `src/main/resources/application.yml`
2. Find: `whatsapp.flow.rsvp-flow-id`
3. Replace with your Flow ID from Step 4
4. Restart application

### Step 6: Test (2 minutes)

1. Send test RSVP flow to a guest
2. Guest should receive WhatsApp message
3. Guest can open interactive flow

---

## 📁 Files Created

### Key Files
- `keys/private_key.pem` - Your secret key (generated)
- `keys/public_key.pem` - Upload to Meta (generated)

### Configuration
- `src/main/resources/application.yml` - Update with Flow ID

### Documentation (in project root)
- `WHATSAPP_FLOW_ERROR_EXPLAINED.md` - Why this happens
- `WHATSAPP_FLOW_SIGNING_GUIDE.md` - Technical deep dive
- `WHATSAPP_FLOW_SETUP_STEPS.md` - Step-by-step guide
- `FLOW_SETUP_CHECKLIST.md` - Checklist to follow
- `QUICK_START_FLOW_SETUP.txt` - Quick reference
- `WHATSAPP_FLOW_README.md` - This file

### Scripts
- `scripts/generate-whatsapp-keys.ps1` - Key generation script

---

## ⚠️ Important Security Notes

### Private Key
- **NEVER** commit to git
- **NEVER** share with anyone
- **NEVER** include in emails
- **NEVER** paste in public forums
- **DO** add `keys/` to `.gitignore`

### Public Key
- Safe to share
- Upload to Meta
- Use to configure application

---

## ✅ How To Know It's Working

### On Meta
- [ ] Public Key Status: **Uploaded**
- [ ] Phone Signed: **Yes**
- [ ] Flow Status: **Published**

### In Application
- [ ] REST API endpoint works: `/api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}`
- [ ] No errors in application logs

### On Guest's Phone
- [ ] Receives WhatsApp message with flow button
- [ ] Can open and fill out the interactive flow

---

## ❓ FAQs

### Q: Is my application code broken?
**A:** No! Your application is completely ready. The issue is Meta's administrative requirement.

### Q: Can I send regular messages without this?
**A:** Yes! Regular WhatsApp templates don't need key signing. Only **Flows** need it.

### Q: Where did my key pair go?
**A:** Generated in: `C:\dev\projects\wed-knots\keys\`

### Q: What if I lose my private key?
**A:** Generate a new one using the script. Upload a new public key to Meta.

### Q: Can I test without publishing?
**A:** No, Meta requires published flows to be sent to guests.

### Q: How long does Meta processing take?
**A:** Usually 5-15 minutes, sometimes up to 30 minutes.

### Q: Still getting an error?
**A:** See the **Troubleshooting** section in `WHATSAPP_FLOW_SIGNING_GUIDE.md`

---

## 🔍 Verification Steps

After completing setup, verify everything works:

```bash
# 1. Check keys exist
ls C:\dev\projects\wed-knots\keys\

# 2. Verify application.yml has Flow ID
grep "rsvp-flow-id" src/main/resources/application.yml

# 3. Start application and check logs
mvn spring-boot:run

# 4. Test API endpoint
curl -X POST http://localhost:8080/api/whatsapp/flow/trigger-rsvp/1/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json"

# 5. Check guest's phone for WhatsApp message
```

---

## 📞 Getting Help

### If Setup Not Working

1. **Review the checklist:** `FLOW_SETUP_CHECKLIST.md`
2. **Check troubleshooting:** `WHATSAPP_FLOW_SIGNING_GUIDE.md`
3. **Contact Meta Support:**
   - Go to: business.facebook.com → Help → Support
   - Describe: "Cannot publish WhatsApp Flow after key signing"

### For Application Issues

1. Check application logs for errors
2. Verify Flow ID in `application.yml`
3. Verify guest phone number format (+[country][number])
4. Check WhatsApp API is enabled in event settings

---

## 🎯 What Happens After Setup

Once everything is configured:

1. **Host sends RSVP request** from application UI
2. **Application triggers flow** via WhatsApp Cloud API
3. **Guest receives message** with interactive flow button
4. **Guest opens flow** and fills out RSVP
5. **Meta sends flow response** to application webhook
6. **Application stores RSVP** in database
7. **Host sees updated** RSVP status

All automatic! Just needed to complete the setup.

---

## 🗂️ File Organization

```
wed-knots/
├── keys/                          ← Generated key files (in .gitignore)
│   ├── private_key.pem           ← KEEP SECRET
│   └── public_key.pem            ← Upload to Meta
├── scripts/
│   └── generate-whatsapp-keys.ps1 ← Run this to generate keys
├── src/main/resources/
│   └── application.yml            ← Update Flow ID here
└── FLOW_SETUP_CHECKLIST.md       ← Follow this checklist
└── WHATSAPP_FLOW_ERROR_EXPLAINED.md
└── WHATSAPP_FLOW_SIGNING_GUIDE.md
└── WHATSAPP_FLOW_SETUP_STEPS.md
└── QUICK_START_FLOW_SETUP.txt
└── WHATSAPP_FLOW_README.md       ← This file
```

---

## 📊 Timeline

| Activity | Time |
|----------|------|
| Generate keys | 1 min |
| Upload to Meta | 2 min |
| Sign phone number | 1 min |
| Wait for processing | 5-15 min |
| Publish flow | 1 min |
| Copy Flow ID | 1 min |
| Update application | 1 min |
| Restart app | 2 min |
| **Total** | **15-30 min** |

---

## ✨ You're Almost There!

Your application is completely set up and ready. You just need to:

1. ✅ Complete the Meta Business setup (15-30 minutes)
2. ✅ Update the application.yml with your Flow ID
3. ✅ Restart the application

Then your WhatsApp RSVP flows will work perfectly!

---

## 📖 Next Steps

### Choose Your Preferred Learning Style:

- **In a hurry?** → `QUICK_START_FLOW_SETUP.txt`
- **Want a checklist?** → `FLOW_SETUP_CHECKLIST.md`
- **Need details?** → `WHATSAPP_FLOW_SETUP_STEPS.md`
- **Want to understand?** → `WHATSAPP_FLOW_ERROR_EXPLAINED.md`
- **Technical deep dive?** → `WHATSAPP_FLOW_SIGNING_GUIDE.md`

---

**Good luck! You've got this! 🚀**

