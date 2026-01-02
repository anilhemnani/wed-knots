# WhatsApp Template Feature - Quick Reference Guide

## 🚀 Feature Overview

The WhatsApp message template feature allows hosts to create invitations that can be sent either as:
1. **Plain Text Messages** - Custom written messages via WhatsApp
2. **WhatsApp Templates** - Pre-approved templates from Meta Business account

Each invitation independently chooses its message type, allowing flexible communication strategies.

---

## 📋 Quick Start

### For System Administrators

#### Step 1: Configure WhatsApp API (Event-Level)
1. Go to Events → [Select Event] → WhatsApp Configuration
2. Enable "Enable WhatsApp Cloud API"
3. Enter your Meta credentials:
   - Phone Number ID
   - Business Account ID
   - Access Token
   - API Version (usually v18.0)
   - Webhook Verify Token (optional)
4. Save configuration
5. **Note:** Templates are now configured per-invitation, not here!

#### Step 2: Create Invitations with Message Type
1. Go to Events → [Select Event] → Invitations → Create New
2. Select message type:
   - **Plain Text Message**: Type your custom message
   - **WhatsApp Template**: Select pre-approved template
3. Fill invitation details
4. Save invitation

---

### For Hosts

#### Creating a Plain Text Invitation
1. Open your event
2. Click "Create Invitation"
3. Select **"Plain Text Message"** mode
4. Fill in:
   - Invitation Title
   - Invitation Type (Save the Date, Main Invitation, etc.)
   - Message body
   - Optional image URL
5. Click "Save Invitation"

**Template Fields:** Disabled (grayed out)

#### Creating a Template Invitation
1. Open your event
2. Click "Create Invitation"
3. Select **"WhatsApp Template"** mode
4. Fill in:
   - Invitation Title
   - Invitation Type
   - Template Name (must exactly match Meta Business account template)
   - Template Language (e.g., English US, Hindi, Spanish)
   - Message body (for preview/fallback)
5. Click "Save Invitation"

**Template Fields:** Enabled and required

---

## 🔧 Configuration Details

### Message Type Options

| Mode | Description | Use Case | Requires API |
|------|-------------|----------|--------------|
| **Plain Text** | Send custom message as WhatsApp text | Quick messages, personalized invites | Optional |
| **Template** | Send pre-approved template from Meta | Professional invites, branded messages | Required |

### Template Language Codes

Supported languages when using WhatsApp templates:

| Language | Code |
|----------|------|
| English (US) | `en_US` |
| English (UK) | `en_GB` |
| Hindi | `hi_IN` |
| Spanish | `es_ES` |
| French | `fr_FR` |
| German | `de_DE` |
| Portuguese (Brazil) | `pt_BR` |
| Italian | `it_IT` |
| Japanese | `ja_JP` |
| Chinese (Simplified) | `zh_CN` |

---

## 📤 Sending Invitations

### For Plain Text Invitations
1. Open invitation
2. Click "Send"
3. Select guests to send to
4. Filter by side (Bride/Groom) if needed
5. Click "Send Invitations"
6. System sends message as formatted text with title and body

**Message Format:**
```
*Invitation Title*

Message body here...

[Optional image URL]
```

### For Template Invitations
1. Open invitation
2. Click "Send"
3. Select guests to send to
4. Click "Send Invitations"
5. System sends using Meta's pre-approved template
6. Template parameters auto-filled with invitation data

---

## 📊 Invitation Logs

After sending, view delivery status:
- **SENT**: Message queued/sent successfully
- **FAILED**: Message delivery failed
- **PENDING**: Message awaiting delivery

Check logs to verify all guests received invitations.

---

## ❌ Common Issues & Solutions

### "Template name is required"
**Problem:** You selected Template mode but didn't enter template name
**Solution:** 
1. Click "Edit" on invitation
2. Enter template name exactly as it appears in Meta Business Manager
3. Save

### "WhatsApp API not configured"
**Problem:** Event doesn't have API credentials set
**Solution:**
1. Go to Event → WhatsApp Configuration
2. Enable API and add credentials
3. Now you can use templates (plain text still works without API)

### Message not showing in WhatsApp
**Problem:** Invalid phone number format or delivery status shows FAILED
**Solution:**
1. Check phone numbers include country code (e.g., +91 for India)
2. Verify phone format: +[country code][number]
3. Check delivery status in invitation logs for error messages
4. Retry failed deliveries from logs page

### Template not found
**Problem:** Selected template doesn't exist in Meta account
**Solution:**
1. Verify template name in Meta Business Manager
2. Check template is approved (not pending/rejected)
3. Ensure template language matches approved language
4. Use exact name as shown in Meta (case-sensitive)

---

## 🎯 Best Practices

### Message Type Selection

**Use Plain Text for:**
- ✅ Quick messages to specific guests
- ✅ Personalized invitations with custom text
- ✅ Testing before sending to large groups
- ✅ When you don't have pre-approved templates

**Use Templates for:**
- ✅ Branded, professional invitations
- ✅ Bulk sending to many guests
- ✅ Consistent messaging across events
- ✅ When Meta compliance is important
- ✅ Production deployments

### Invitation Management

1. **Draft Invitations**: Create in DRAFT status first
2. **Review**: Preview before activating
3. **Activate**: Change to ACTIVE when ready to send
4. **Archive**: Archive after event to keep records
5. **Track Logs**: Monitor delivery status in logs

### Template Best Practices

1. **Test First**: Create test invitation with template before bulk sending
2. **Verify Phone Numbers**: Ensure all guest phone numbers are valid
3. **Check Language**: Confirm template language matches guest region
4. **Monitor Logs**: Track delivery status and troubleshoot failures
5. **Keep Records**: Archive invitations for audit trail

---

## 🔐 Security & Privacy

- ✅ WhatsApp API credentials encrypted in database
- ✅ Phone numbers only used for WhatsApp delivery
- ✅ Delivery logs tracked for compliance
- ✅ Access control: Only admins/hosts can send
- ✅ Session-based authentication prevents unauthorized access

---

## 📱 Supported Languages

**Plain Text:** Works in any language (no restriction)

**Templates:** Use supported language codes matching your template

---

## 🚨 Limitations & Notes

1. **Template Approval**: Templates must be pre-approved in Meta Business Manager
2. **Language Matching**: Template language code must match approved template
3. **API Credentials**: Required for template sending (plain text optional)
4. **Bulk Limits**: Meta API has rate limits (usually 1000+ messages/day)
5. **Future Enhancement**: Dynamic template fetching from Meta API (coming soon)

---

## 📞 Support

### For API Setup Issues
- Check Meta Business Manager credentials
- Verify WhatsApp Business Account is active
- Contact Meta support for API issues

### For Feature Issues
- Check invitation logs for error messages
- Verify message type selection in invitation form
- Ensure required fields are filled based on message type

---

## 🔄 Workflow Example

### Scenario: Wedding Invitation Campaign

**Step 1: Event Setup**
- Create wedding event
- Configure WhatsApp API once at event level

**Step 2: Create Multiple Invitations**
- Invitation #1: "Save the Date" (Plain Text - quick announcement)
- Invitation #2: "Main Invitation" (Template - professional design)
- Invitation #3: "Reminder" (Plain Text - personal touch)
- Invitation #4: "Thank You" (Template - branded message)

**Step 3: Send Strategically**
- Send Save the Date to all guests (plain text)
- Send Main Invitation 2 weeks before (template)
- Send Reminder 1 week before (plain text)
- Send Thank You after event (template)

**Step 4: Track & Archive**
- Monitor delivery logs for each invitation
- Archive completed invitations
- Keep records for future reference

---

## 📈 Benefits

✅ **Flexibility**: Each invitation can use different message type
✅ **Cost Effective**: Plain text doesn't require expensive templates
✅ **Professional**: Templates provide branded, compliant messaging
✅ **Scalable**: Support for hundreds of guests
✅ **Trackable**: Full delivery logs and status tracking
✅ **User Friendly**: Simple UI for selecting message type
✅ **Secure**: Encrypted credentials, session-based auth

---

**Version:** 1.0  
**Last Updated:** January 2, 2026  
**Status:** Production Ready

