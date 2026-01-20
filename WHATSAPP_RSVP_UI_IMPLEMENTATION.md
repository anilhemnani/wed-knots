# WhatsApp RSVP Send UI - Implementation Summary

## Overview
Added comprehensive UI for hosts to send WhatsApp RSVP requests to guests, triggering the RSVP flow on WhatsApp.

## Features Implemented

### 1. Dedicated WhatsApp RSVP Send Page
**File:** `src/main/resources/templates/host/whatsapp_rsvp_send.html`

**Features:**
- **Visual Guest Cards**: Display all guests in a card-based layout
- **Bulk Selection**: Select/deselect all guests with one click
- **Individual Send**: Send to individual guests with one button click
- **Batch Send**: Send to multiple selected guests at once
- **Filters**: 
  - All Guests
  - With Phone Number
  - Without Phone Number
  - Bride's Side
  - Groom's Side
- **Search**: Real-time search by guest name
- **WhatsApp Configuration Check**: Shows warning if WhatsApp is not configured
- **Progress Modal**: Shows progress when sending to multiple guests
- **Toast Notifications**: Success/error feedback for each action
- **Phone Number Indicators**: Shows if guest has multiple phone numbers
- **Disabled State**: Automatically disables buttons for guests without phone numbers

### 2. Controller Endpoints

#### GuestWebController
**File:** `src/main/java/com/wedknots/web/GuestWebController.java`
- Added `GET /events/{eventId}/guests/whatsapp-rsvp-send` endpoint
- Accessible by HOST and ADMIN roles
- Loads event, guests, and WhatsApp configuration status

#### AdminGuestController
**File:** `src/main/java/com/wedknots/web/AdminGuestController.java`
- Added `GET /admin/events/{eventId}/guests/whatsapp-rsvp-send` endpoint
- Accessible by ADMIN role only
- Same functionality as host version

### 3. Enhanced Guest List Pages

#### Host Guest List
**File:** `src/main/resources/templates/guest_list.html`
- Added "Send WhatsApp RSVP" button in header
- Added WhatsApp icon button for each guest in table
- JavaScript to handle individual send requests
- Real-time status updates (sent/failed)

#### Admin Guest List
**File:** `src/main/resources/templates/admin_event_guests.html`
- Added "Send WhatsApp RSVP" button in header
- Added WhatsApp icon button for each guest in table
- JavaScript to handle individual send requests
- Real-time status updates (sent/failed)

### 4. Host Dashboard Integration
**File:** `src/main/resources/templates/host_dashboard.html`
- Added "Send RSVP" quick action card
- Direct link to WhatsApp RSVP send page

## API Endpoints Used

### Backend API (Already Exists)
The UI uses the existing WhatsApp Flow API endpoints:

1. **Send to Individual Guest**
   - `POST /api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}`
   - Sends RSVP flow to a single guest

2. **Send to Multiple Guests (Batch)**
   - `POST /api/whatsapp/flow/trigger-rsvp-batch/{eventId}`
   - Body: `[guestId1, guestId2, ...]`
   - Sends RSVP flow to multiple guests

3. **Get Flow Status**
   - `GET /api/whatsapp/flow/status/{eventId}`
   - Check WhatsApp configuration status

## User Experience Flow

### From Guest List Page:
1. Click individual WhatsApp icon next to any guest
2. Confirm the action
3. Guest receives WhatsApp RSVP flow immediately
4. Button updates to show "Sent" status

### From WhatsApp RSVP Send Page:
1. Navigate from Guest List or Host Dashboard
2. View all guests in card layout
3. Apply filters or search for specific guests
4. Select multiple guests using checkboxes
5. Click "Send to Selected" or individual "Send" buttons
6. Progress modal shows bulk sending progress
7. Toast notification shows results

## Technical Details

### JavaScript Features
- **Async/Await**: Modern JavaScript for API calls
- **Fetch API**: RESTful communication with backend
- **Bootstrap 5**: Modal, toast, and responsive components
- **Real-time Filtering**: Client-side filtering for performance
- **Error Handling**: Comprehensive try-catch blocks

### Security
- **@PreAuthorize**: Role-based access control
- **CSRF Protection**: Spring Security default protection
- **Input Validation**: Backend validates guest/event existence

### Accessibility
- **Disabled States**: Buttons disabled for guests without phone numbers
- **Visual Feedback**: Icons, colors, and badges for status
- **Tooltips**: Helpful hints on hover
- **Confirmations**: User confirmation before sending

## WhatsApp Flow Integration

When a host sends an RSVP request:

1. **Flow Triggered**: WhatsApp Flow v7.3 is sent to guest's phone
2. **Initial Data**: 
   - Guest name, ID
   - Event details (bride/groom names, date, location)
   - Event ID
3. **User Journey**:
   - Guest opens WhatsApp message
   - Clicks to start flow
   - Sees wedding invitation (WELCOME_SCREEN)
   - Selects attendance (RSVP_SCREEN)
   - Provides attendee count (ATTENDING_SCREEN)
   - Selects travel mode (ATTENDEE_COUNT_SCREEN)
   - Enters travel details (TRAVEL_SCREEN)
   - Confirms submission (SUCCESS_SCREEN)
4. **Backend Processing**: Flow completion webhook processes the response

## Routes Summary

| Route | Access | Purpose |
|-------|--------|---------|
| `/events/{eventId}/guests/whatsapp-rsvp-send` | HOST, ADMIN | WhatsApp RSVP send page (host) |
| `/admin/events/{eventId}/guests/whatsapp-rsvp-send` | ADMIN | WhatsApp RSVP send page (admin) |
| `/api/whatsapp/flow/trigger-rsvp/{eventId}/{guestId}` | HOST, ADMIN | Send to individual guest |
| `/api/whatsapp/flow/trigger-rsvp-batch/{eventId}` | HOST, ADMIN | Send to multiple guests |

## Visual Indicators

- 🟢 **Green WhatsApp Icon**: Ready to send (has phone number)
- 🔴 **Disabled Button**: No phone number available
- ✅ **Check Circle**: Successfully sent
- ⏳ **Hourglass**: Sending in progress
- 📞 **Phone Icon**: Primary phone number
- ℹ️ **Badge**: Multiple phone numbers indicator

## Testing Checklist

- [ ] Host can access WhatsApp RSVP send page
- [ ] Admin can access WhatsApp RSVP send page
- [ ] Filters work correctly
- [ ] Search filters guests in real-time
- [ ] Select all/deselect all works
- [ ] Individual send works for guest with phone
- [ ] Individual send disabled for guest without phone
- [ ] Bulk send works for multiple guests
- [ ] Progress modal shows during bulk send
- [ ] Toast notifications appear on success/error
- [ ] Button states update after sending
- [ ] WhatsApp configuration warning shows when not configured
- [ ] Guest receives WhatsApp flow message
- [ ] Flow data includes correct guest/event information

## Future Enhancements

1. **Send History**: Track when RSVP was sent to each guest
2. **Resend Capability**: Allow resending to guests who haven't responded
3. **Scheduling**: Schedule RSVP sends for future date/time
4. **Templates**: Custom message templates for different guest groups
5. **Analytics**: Track open rates, response rates
6. **Reminders**: Automated reminders for guests who haven't responded
7. **Multiple Phone Numbers**: UI to select which phone number to use when guest has multiple

## Notes

- Phone numbers are managed through `GuestPhoneNumber` entity (one-to-many)
- Primary phone number is used for WhatsApp sending
- Guest must have at least one phone number to receive WhatsApp
- WhatsApp API must be configured for the event before sending
- Flow ID is configured in `application.yml` as `whatsapp.flow.rsvp-flow-id`

