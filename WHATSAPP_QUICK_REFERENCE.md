# Quick Reference: WhatsApp Cloud API Configuration

## 🚀 Quick Start

### 1. Access Configuration Page
```
Admin Dashboard → Select Event → WhatsApp Configuration Button
```

### 2. Required Credentials (from Meta for Developers)
- ✅ Phone Number ID
- ✅ Business Account ID  
- ✅ Access Token
- ⚙️ API Version (default: v18.0)
- 🔒 Verify Token (optional)

### 3. Get Credentials
Visit: https://developers.facebook.com
Path: Your App → WhatsApp → Getting Started

---

## 📋 Configuration Fields

| Field | Required | Example | Description |
|-------|----------|---------|-------------|
| Enable API | Yes | ☑️ Checked | Toggle to enable/disable |
| Phone Number ID | Yes | `123456789012345` | From WhatsApp Business |
| Business Account ID | Yes | `123456789012345` | Your WABA ID |
| Access Token | Yes | `EAAx...` | Long-lived token |
| API Version | Yes | `v18.0` | API version to use |
| Verify Token | No | `my_token_123` | For webhook verification |

---

## 🔄 Usage Flow

```
1. Admin/Host configures WhatsApp API
   ↓
2. Creates invitation with message
   ↓
3. Selects guests to receive invitation
   ↓
4. Clicks "Send via WhatsApp"
   ↓
5. System checks if API enabled
   ↓
6. Sends via Cloud API (or logs if disabled)
   ↓
7. Tracks delivery status in logs
```

---

## 🎯 Key URLs

| Action | URL Pattern |
|--------|-------------|
| View Config | `/events/{eventId}/whatsapp-config` |
| Save Config | POST `/events/{eventId}/whatsapp-config` |
| Send Invitation | `/events/{eventId}/invitations/{invId}/send` |
| View Logs | `/events/{eventId}/invitations/{invId}/logs` |

---

## ⚙️ API Endpoint Used

```http
POST https://graph.facebook.com/{version}/{phone-number-id}/messages
Authorization: Bearer {access-token}
Content-Type: application/json
```

---

## ✅ Testing Checklist

- [ ] Configuration page loads
- [ ] Can save credentials
- [ ] Enable/disable toggle works
- [ ] Can send test message
- [ ] Message appears in WhatsApp
- [ ] Delivery status tracked
- [ ] Error handling works

---

## 🔍 Troubleshooting

### Problem: Message not sending
**Check:**
1. Is WhatsApp API enabled? ☑️
2. Are credentials correct? 🔑
3. Is phone number valid? 📱
4. Check application logs 📋

### Problem: Can't access config page
**Check:**
1. User role (Admin or Host required) 👤
2. Event exists ✅
3. Logged in? 🔐

### Problem: Invalid credentials
**Check:**
1. Token not expired ⏰
2. Phone Number ID matches Business Account 🔗
3. API version supported 📊

---

## 📁 Files to Know

### Backend
- `WeddingEvent.java` - Entity with WhatsApp fields
- `EventWebController.java` - Config endpoints
- `WhatsAppService.java` - API integration
- `InvitationLogService.java` - Message sending

### Frontend
- `whatsapp_config.html` - Configuration UI
- `event_view.html` - Entry point button

### Database
- `db.changelog-master.xml` - Schema migration
- Table: `wedding_event_tbl` - 6 new columns

---

## 🎨 UI Features

✨ **Configuration Page**
- Toggle switch for enable/disable
- Collapsible fields (show/hide)
- Setup guide with instructions
- Visual status indicator
- Success/error messages

🎨 **Visual Indicators**
- 🟢 Green badge when enabled
- ⚪ Gray badge when disabled
- ✅ Success alerts
- ❌ Error alerts

---

## 🔐 Security Notes

⚠️ **Current Implementation:**
- Tokens stored in database (plain text)
- HTTPS recommended for production
- Role-based access control enforced

🔒 **Production Recommendations:**
- Encrypt access tokens at rest
- Use token rotation
- Implement audit logging
- Add IP whitelisting

---

## 📊 Database Schema

```sql
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_api_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_phone_number_id VARCHAR(255);
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_business_account_id VARCHAR(255);
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_access_token VARCHAR(512);
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_api_version VARCHAR(50) DEFAULT 'v18.0';
ALTER TABLE wedding_event_tbl ADD COLUMN whatsapp_verify_token VARCHAR(255);
```

---

## 📞 Support Resources

- 📖 [WhatsApp Cloud API Docs](https://developers.facebook.com/docs/whatsapp/cloud-api)
- 📖 [Graph API Reference](https://developers.facebook.com/docs/graph-api)
- 📄 Full Documentation: `WHATSAPP_CLOUD_API_FEATURE.md`

---

**Last Updated:** January 2, 2026

