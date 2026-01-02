# WhatsApp Cloud API Configuration Feature

## Overview
Added WhatsApp Cloud API configuration support to wedding events, allowing admins and hosts to configure and use WhatsApp Business Cloud API for sending invitations directly from the application.

## Date
January 2, 2026

## Implementation Summary

### 1. Database Schema Changes

#### New Columns Added to `wedding_event_tbl`
- `whatsapp_api_enabled` (BOOLEAN) - Enable/disable WhatsApp Cloud API
- `whatsapp_phone_number_id` (VARCHAR(255)) - WhatsApp Phone Number ID
- `whatsapp_business_account_id` (VARCHAR(255)) - WhatsApp Business Account ID
- `whatsapp_access_token` (VARCHAR(512)) - API Access Token
- `whatsapp_api_version` (VARCHAR(50)) - API Version (default: v18.0)
- `whatsapp_verify_token` (VARCHAR(255)) - Webhook verification token

#### Liquibase Changeset
Added changeset `17-add-whatsapp-config-to-wedding-event` in:
- `/src/main/resources/db/changelog/db.changelog-master.xml`

### 2. Entity Model Updates

#### WeddingEvent.java
Added new fields with JPA annotations:
```java
@Column(name = "whatsapp_api_enabled")
private Boolean whatsappApiEnabled = false;

@Column(name = "whatsapp_phone_number_id")
private String whatsappPhoneNumberId;

@Column(name = "whatsapp_business_account_id")
private String whatsappBusinessAccountId;

@Column(name = "whatsapp_access_token")
private String whatsappAccessToken;

@Column(name = "whatsapp_api_version")
private String whatsappApiVersion = "v18.0";

@Column(name = "whatsapp_verify_token")
private String whatsappVerifyToken;
```

### 3. Service Layer Updates

#### WhatsAppService.java
**Enhanced Features:**
- Overloaded `sendMessage()` method to accept `WeddingEvent` parameter
- Automatic detection of Cloud API configuration from event
- Integration with WhatsApp Graph API
- Fallback to logging when Cloud API not configured
- RESTful HTTP client for API calls

**Key Methods:**
- `sendMessage(WeddingEvent event, String phoneNumber, String title, String message, String imageUrl)`
- `sendViaCloudAPI(WeddingEvent event, String phoneNumber, String message, String imageUrl)` - Private method for Cloud API integration

**API Integration:**
```java
// API Endpoint: https://graph.facebook.com/{version}/{phone-number-id}/messages
// Authentication: Bearer token
// Message format: JSON with messaging_product, to, type, text fields
```

#### InvitationLogService.java
Updated to pass event to WhatsAppService:
```java
boolean sent = whatsAppService.sendMessage(
    invitation.getEvent(), // Pass event for Cloud API config
    guest.getContactPhone(),
    invitation.getTitle(),
    invitation.getMessage(),
    invitation.getImageUrl()
);
```

### 4. Controller Updates

#### EventWebController.java
Added two new endpoints:

**GET /events/{id}/whatsapp-config**
- Shows WhatsApp configuration form
- Access: Admin and Host roles
- Returns: `whatsapp_config.html` template

**POST /events/{id}/whatsapp-config**
- Saves WhatsApp configuration
- Access: Admin and Host roles
- Validates and saves all WhatsApp fields
- Returns: Redirect with success message

### 5. UI Templates

#### whatsapp_config.html
**New comprehensive configuration page with:**

**Configuration Form:**
- Enable/Disable toggle switch
- Phone Number ID input
- Business Account ID input
- Access Token textarea (secure display)
- API Version dropdown (v18.0, v17.0, v16.0)
- Webhook Verify Token input

**Help Section:**
- Step-by-step setup guide
- Links to Meta for Developers
- Feature list and benefits

**Status Card:**
- Visual indicator (green/gray)
- Current configuration status
- Ready/Not configured status

**Features:**
- JavaScript toggle for conditional field visibility
- Bootstrap 5 styling
- Responsive design
- Success/Error message display
- Form validation

#### event_view.html
Added WhatsApp Configuration button to Quick Actions:
```html
<a th:href="@{/events/{id}/whatsapp-config(id=${event.id})}" 
   class="btn btn-outline-info">
    <i class="bi bi-whatsapp"></i> WhatsApp Configuration
</a>
```

### 6. Security & Access Control

**Role-Based Access:**
- Admin: Full access to configure WhatsApp API
- Host: Can configure WhatsApp API for their events
- Guest: No access

**Security Considerations:**
- Access tokens stored in database (consider encryption in production)
- HTTPS required for API calls
- Bearer token authentication
- Validation on phone number format

## WhatsApp Cloud API Integration Details

### Setup Requirements
1. Meta for Developers account
2. WhatsApp Business API access
3. Verified Business Account
4. Phone Number registered with WhatsApp Business

### API Credentials Needed
1. **Phone Number ID**: Unique identifier for WhatsApp business phone
2. **Business Account ID**: WhatsApp Business Account identifier
3. **Access Token**: Long-lived access token from Meta
4. **API Version**: Graph API version (v18.0 recommended)
5. **Verify Token**: Optional webhook verification token

### Message Sending Flow
1. Admin/Host configures WhatsApp Cloud API in event settings
2. User creates/selects invitation
3. User selects guests to receive invitation
4. System checks if WhatsApp API is enabled for event
5. If enabled: Sends via Cloud API
6. If disabled: Logs message (fallback mode)
7. Delivery status tracked in invitation_log_tbl

### API Call Structure
```json
POST https://graph.facebook.com/{version}/{phone-number-id}/messages
Authorization: Bearer {access-token}
Content-Type: application/json

{
  "messaging_product": "whatsapp",
  "to": "919876543210",
  "type": "text",
  "text": {
    "preview_url": true,
    "body": "*Invitation Title*\n\nMessage content..."
  }
}
```

### Response Handling
- **200 OK**: Message sent successfully → Status: SENT
- **Error**: API call failed → Status: FAILED, error logged

## Files Modified

### Java Files
1. `/src/main/java/com/momentsmanager/model/WeddingEvent.java`
   - Added WhatsApp configuration fields
   - Added getters/setters

2. `/src/main/java/com/momentsmanager/web/EventWebController.java`
   - Added GET /events/{id}/whatsapp-config
   - Added POST /events/{id}/whatsapp-config

3. `/src/main/java/com/momentsmanager/service/WhatsAppService.java`
   - Added event parameter support
   - Implemented Cloud API integration
   - Added sendViaCloudAPI method

4. `/src/main/java/com/momentsmanager/service/InvitationLogService.java`
   - Updated to pass event to WhatsAppService

### Templates
1. `/src/main/resources/templates/whatsapp_config.html` (NEW)
   - Configuration form UI
   - Help and documentation
   - Status display

2. `/src/main/resources/templates/event_view.html`
   - Added WhatsApp Configuration button

### Database
1. `/src/main/resources/db/changelog/db.changelog-master.xml`
   - Added changeset 17 for WhatsApp columns

## Usage Guide

### For Admins/Hosts

**Step 1: Access Configuration**
1. Navigate to event details page
2. Click "WhatsApp Configuration" button

**Step 2: Get API Credentials**
1. Visit https://developers.facebook.com
2. Create/Select your app
3. Add WhatsApp product
4. Copy credentials from Getting Started section

**Step 3: Configure in Application**
1. Enable "WhatsApp Cloud API" toggle
2. Paste Phone Number ID
3. Paste Business Account ID
4. Paste Access Token
5. Select API Version
6. (Optional) Add Verify Token for webhooks
7. Click "Save Configuration"

**Step 4: Send Invitations**
1. Create/Select invitation
2. Click "Send to Guests"
3. Select recipients
4. Messages will be sent via WhatsApp Cloud API
5. Check logs for delivery status

## Benefits

### For Event Organizers
- Direct WhatsApp integration (no manual sending)
- Bulk message delivery
- Delivery tracking and status
- Professional message formatting
- Centralized credential management

### For System
- Scalable message delivery
- Official API compliance
- Rate limiting support
- Webhook support for delivery receipts
- Better reliability than URL schemes

## Future Enhancements

### Planned
1. **Message Templates**: Pre-approved WhatsApp templates
2. **Media Support**: Send images/videos via Cloud API
3. **Delivery Webhooks**: Real-time delivery status updates
4. **Rate Limiting**: Automatic throttling for bulk sends
5. **Token Encryption**: Encrypt access tokens in database
6. **Multi-language**: Template support for multiple languages
7. **Analytics**: Message delivery analytics dashboard

### Security Improvements
1. Encrypt access tokens at rest
2. Token rotation support
3. IP whitelisting for webhooks
4. Audit logging for configuration changes
5. Two-factor authentication for config access

## Testing Checklist

- [ ] Database migration runs successfully
- [ ] WhatsApp config page loads without errors
- [ ] Form validation works correctly
- [ ] Configuration saves successfully
- [ ] Enable/disable toggle works
- [ ] Event view shows WhatsApp config button
- [ ] Access control enforced (Admin/Host only)
- [ ] Messages send via Cloud API when enabled
- [ ] Messages log when API disabled (fallback)
- [ ] Delivery status tracked correctly
- [ ] Error messages display appropriately
- [ ] Success messages display after save
- [ ] Bootstrap Icons display correctly
- [ ] Responsive design works on mobile
- [ ] Help documentation is clear

## Known Limitations

1. **Token Storage**: Access tokens stored in plain text (encryption recommended)
2. **No Template Support**: Currently sends freeform text (templates recommended for production)
3. **Single Configuration**: One WhatsApp config per event (may need multiple numbers)
4. **No Webhook Handler**: Delivery status not updated via webhooks yet
5. **Manual Token Management**: No automatic token refresh

## Production Considerations

### Before Going Live
1. **Secure Tokens**: Implement encryption for access tokens
2. **Use Templates**: Submit and use approved message templates
3. **Set Up Webhooks**: Implement webhook handler for delivery receipts
4. **Rate Limiting**: Implement proper rate limiting
5. **Error Handling**: Enhanced error handling and retry logic
6. **Monitoring**: Set up monitoring for API failures
7. **Backup Strategy**: Fallback to alternative delivery methods

### Compliance
- Ensure GDPR compliance for storing phone numbers
- Follow WhatsApp Business Policy
- Get user consent for WhatsApp messages
- Respect opt-out requests
- Maintain message logs for audit

## Support Resources

- [WhatsApp Cloud API Documentation](https://developers.facebook.com/docs/whatsapp/cloud-api)
- [Graph API Reference](https://developers.facebook.com/docs/graph-api)
- [WhatsApp Business Policy](https://www.whatsapp.com/legal/business-policy)
- [Message Templates Guide](https://developers.facebook.com/docs/whatsapp/message-templates)

## Changelog

**v1.0 - January 2, 2026**
- Initial WhatsApp Cloud API configuration feature
- Database schema updates
- Configuration UI
- Service layer integration
- Basic message sending support

