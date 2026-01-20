# How to Send WhatsApp RSVP Requests - Quick Guide

## Prerequisites
1. WhatsApp API must be configured for your event
2. Guests must have phone numbers entered in the system

## Method 1: From Guest List (Quick Send)

### Steps:
1. Navigate to **Guests** page for your event
2. Find the guest you want to send RSVP to
3. Click the **green WhatsApp icon** (📱) next to the guest's name
4. Confirm the action when prompted
5. Wait for success message
6. Button will change to show "✅ Sent"

**Note:** WhatsApp icon is disabled (grayed out) for guests without phone numbers.

## Method 2: From Dedicated Send Page (Bulk Send)

### Steps:
1. Navigate to **Guests** page for your event
2. Click **"Send WhatsApp RSVP"** button (green, in header)
3. On the WhatsApp RSVP send page:

#### To Send to Individual Guests:
- Find the guest card
- Click the **"Send"** button on their card
- Confirm the action

#### To Send to Multiple Guests:
- Use checkboxes to select guests
  - Or click **"Select All"** to select everyone
- Click **"Send to Selected"** button
- Confirm the action
- A progress modal will show the sending status
- Results will be displayed when complete

### Filtering Guests:
Use the filter dropdown to show only:
- All Guests
- Guests with phone numbers
- Guests without phone numbers
- Bride's side only
- Groom's side only

### Searching:
- Type in the search box to filter by guest name
- Search works in real-time

## Method 3: From Host Dashboard

### Steps:
1. Go to **Host Dashboard**
2. Find your wedding event
3. Click **"Manage"** to expand event details
4. Click **"Send to Guests"** in the WhatsApp RSVP card
5. Follow steps from Method 2

## What Happens When You Send?

1. **Guest receives WhatsApp message** with wedding invitation
2. **Guest clicks to open** the RSVP flow
3. **Guest goes through flow**:
   - Views wedding invitation details
   - Confirms attendance (Yes/No)
   - If attending: provides guest count
   - If attending: selects travel mode
   - If attending: enters travel details
   - Submits RSVP
4. **System records response** automatically
5. **Host can view RSVP** in the RSVP section

## Troubleshooting

### "WhatsApp Not Configured" Warning
- Go to your event's WhatsApp configuration page
- Enter your WhatsApp Business API credentials
- Save the configuration

### WhatsApp Button is Disabled
- This guest doesn't have a phone number
- Edit the guest and add their phone number
- Then try sending again

### "Failed to Send" Error
- Check if the phone number is in correct format (international format: +countrycode number)
- Verify WhatsApp API credentials are correct
- Check if WhatsApp Phone Number ID is configured
- Ensure the guest's phone number is a valid WhatsApp number

### Guest Didn't Receive Message
- Verify the phone number is correct
- Check that the guest has WhatsApp installed
- Wait a few minutes (messages may be delayed)
- Try resending

## Tips

✅ **Best Practices:**
- Always verify phone numbers before sending
- Use bulk send for efficiency when sending to many guests
- Send reminders to guests who haven't responded
- Keep track of who has been sent RSVPs

⚠️ **Important:**
- Each send triggers an actual WhatsApp message
- Cannot unsend messages once sent
- Messages count towards your WhatsApp API quota
- Guest can respond only once per flow session

## Viewing Responses

After guests respond:
1. Go to **RSVPs** section
2. View all responses
3. See guest count, travel information, and more
4. Export reports if needed

## Support

If you encounter issues:
- Check the configuration settings
- Verify all prerequisites are met
- Contact your administrator for help with WhatsApp API setup

