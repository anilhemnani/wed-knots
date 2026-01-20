# Mobile RSVP Implementation Guide

## Overview

A complete mobile-first RSVP system that allows guests to:
1. Access the RSVP form via a shared URL
2. Verify their phone number
3. Submit RSVP details
4. Receive confirmation

## Features

### ✅ Phase 1: Phone Number Verification
- Guest enters their mobile number
- Server validates phone number format
- Looks up guest by phone in the event
- Returns guest details (name, family name)
- Shows personalized greeting

### ✅ Phase 2: RSVP Form Submission
- Guest selects attendance status
- Conditional fields based on attendance:
  - **If Attending**: Attendee count, travel mode, travel details, dietary restrictions
  - **If Not Attending**: Special requests
- Form submission updates backend
- Shows success confirmation

### ✅ User Experience
- Mobile-optimized responsive design
- Two-step form with progress indicators
- Real-time field visibility based on selections
- Confetti animation on success
- Error messages with recovery suggestions
- Bootstrap 5 styling with custom animations

## URL Structure

```
GET /rsvp/event/{eventId}
```

**Example**: `https://wedknots.uk/rsvp/event/1`

## API Endpoints

### 1. Display RSVP Page
```
GET /rsvp/event/{eventId}
Response: HTML page with phone verification form
```

### 2. Validate Phone Number
```
POST /rsvp/event/{eventId}/validate-phone
Content-Type: application/json

Request:
{
  "phoneNumber": "+447878597720"
}

Response:
{
  "success": true,
  "guestId": 1,
  "guestName": "Ravi Sharma",
  "familyName": "Sharma",
  "eventId": 1
}
```

### 3. Submit RSVP
```
POST /rsvp/event/{eventId}/submit
Content-Type: application/x-www-form-urlencoded

Request Form Fields:
- guestId: 1
- rsvpStatus: "attending" | "not_attending"
- attendeeCount: 2 (optional, shown if attending)
- travelMode: "flight" | "train" | "car" | "bus" | "other" (optional)
- travelDetails: "..." (optional)
- dietaryRestrictions: "..." (optional)
- specialRequests: "..." (optional)

Response: Redirect to /rsvp/event/{eventId}/success
```

## File Structure

```
src/main/java/com/wedknots/
├── web/
│   └── GuestRSVPController.java          # REST & form controller
├── repository/
│   └── GuestRepository.java              # Added: findByEventAndPhoneNumber()

src/main/resources/templates/rsvp/
├── mobile_rsvp.html                      # Phone verification & RSVP form
├── success.html                          # Success confirmation
└── error.html                            # Error display
```

## Key Components

### GuestRSVPController

#### Methods:

1. **showRsvpPage(eventId, model)**
   - Displays the mobile RSVP form
   - Validates event exists
   - Passes event details to template

2. **validatePhoneNumber(eventId, request)**
   - Validates phone format
   - Finds guest by event + phone
   - Returns guest details
   - Returns error if guest not found

3. **submitRsvp(eventId, request, model)**
   - Validates request data
   - Updates guest RSVP details
   - Saves to database
   - Redirects to success page

### Repository Method

**GuestRepository.findByEventAndPhoneNumber(event, phoneNumber)**
- Joins Guest with GuestPhoneNumber entities
- Supports multiple phone numbers per guest
- Filters by event and phone number

## Data Flow

```
1. Guest clicks RSVP link: /rsvp/event/1
   ↓
2. Server displays mobile_rsvp.html
   - Shows event name
   - Phone number input form
   ↓
3. Guest enters phone number: +447878597720
   ↓
4. Client sends POST /rsvp/event/1/validate-phone
   ↓
5. Server looks up guest:
   - Find WeddingEvent (ID: 1)
   - Find Guest with this phone in this event
   - Return guest details
   ↓
6. JavaScript updates form:
   - Store guest ID
   - Show personalized greeting
   - Show RSVP form (step 2)
   ↓
7. Guest fills RSVP form:
   - Select attending/not attending
   - Enter details (conditional fields)
   ↓
8. Client sends POST /rsvp/event/1/submit
   - All form data
   - Hidden guest ID
   ↓
9. Server processes:
   - Validate guest belongs to event
   - Update guest record
   - Save to database
   ↓
10. Redirect to /rsvp/event/1/success
    ↓
11. Server displays success.html
    - Guest name
    - RSVP status
    - Confirmation message
    - Confetti animation
```

## Form Fields

### Step 1: Phone Verification
- **Phone Number** (required)
  - Format: +{country}{number}
  - Regex: `^\+?[1-9]\d{1,14}$`
  - Hint: "Include country code (e.g., +44 for UK)"

### Step 2: RSVP Details

#### Always Visible:
- **Will you be attending?** (required)
  - Radio: "Yes, I'll attend" → attending
  - Radio: "Sorry, can't attend" → not_attending

#### Conditional - If Attending:
- **Number of Guests** (optional)
  - Increment/Decrement buttons
  - Range: 1-10
  - Default: 1

- **How will you travel?** (optional)
  - Dropdown: Flight, Train, Car, Bus, Other

- **Travel Details** (optional)
  - Textarea
  - Placeholder: "E.g., Flight number, arrival time, train details..."

#### Always Visible:
- **Dietary Restrictions** (optional)
  - Textarea
  - Placeholder: "Any allergies or dietary requirements..."

- **Special Requests** (optional)
  - Textarea
  - Placeholder: "Any special requests or messages..."

## Styling

### Mobile Design
- Max width: 480px (mobile viewport)
- Full width on smaller screens
- Touch-friendly: 44px+ button heights
- Large text: 16px minimum
- Adequate spacing: 15px+ gaps

### Color Scheme
- Primary: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`
- Success: `#d4edda` (light green)
- Error: `#fee` (light red)
- Text: `#333` (dark)
- Muted: `#666` (gray)

### Animations
- Page load: slideUp (0.5s)
- Success checkmark: scaleIn (0.5s)
- Error shake: 0.3s
- Confetti fall: 2s

## Validation

### Phone Number
```
Pattern: ^\+?[1-9]\d{1,14}$
Examples:
- +447878597720 (UK)
- +919876543210 (India)
- +15551234567 (USA)
```

### RSVP Status
```
Allowed values:
- attending
- not_attending
```

### Attendee Count
```
Type: Integer
Range: 1-10
```

### Travel Mode
```
Allowed values:
- flight
- train
- car
- bus
- other
```

## Backend Updates

### Guest Model
The system updates these fields:
- `maxAttendees` - Number of guests
- `expectedAttendance` - RSVP status enum
- `updatedAt` - Timestamp

### Future Extensions
- Travel info entity for flight details
- Dietary restrictions field
- Special requests storage

## Error Handling

### Common Errors

1. **Invalid Phone Format**
   ```
   Message: "Invalid phone number format. Please include country code."
   Recovery: Show hint, focus input
   ```

2. **Guest Not Found**
   ```
   Message: "No guest found with this phone number"
   Recovery: Suggest checking phone number
   ```

3. **Event Not Found**
   ```
   Message: "Event not found"
   Recovery: Redirect to error page
   ```

4. **Database Error**
   ```
   Message: "An error occurred. Please try again."
   Recovery: Show retry button
   ```

## Security

- Phone number validation
- Event ownership verification
- Guest-to-event relationship check
- No sensitive data exposed
- CSRF protection (built-in with Thymeleaf)

## Testing

### Manual Testing Checklist

1. **Phone Verification**
   - [ ] Valid phone format accepted
   - [ ] Invalid format rejected
   - [ ] Guest found and displayed
   - [ ] Guest not found shows error
   - [ ] Event not found shows error

2. **RSVP Form**
   - [ ] Attending option shows travel fields
   - [ ] Not attending hides travel fields
   - [ ] Attendee count increment/decrement works
   - [ ] Form submission success
   - [ ] Form submission error handling

3. **Success Page**
   - [ ] Correct guest name displayed
   - [ ] Correct RSVP status displayed
   - [ ] Confetti animation plays (optional)
   - [ ] Back to home button works

4. **Mobile Experience**
   - [ ] Responsive on iPhone 12
   - [ ] Responsive on Android phone
   - [ ] Touch buttons easily clickable
   - [ ] Form submission on mobile
   - [ ] Landscape mode handling

## Browser Support

- Chrome/Edge: Latest 2 versions
- Firefox: Latest 2 versions
- Safari: Latest 2 versions
- Mobile browsers: Latest versions

## Performance

- Page load time: <2s
- Phone validation: <500ms
- Form submission: <1s
- Mobile optimized (no unnecessary scripts)

## Accessibility

- Semantic HTML
- Label associations
- Error messages linked to fields
- Keyboard navigation
- Touch target sizing (44px+)

---

**Deployment**: Copy templates to `src/main/resources/templates/rsvp/` directory
**Database**: No migrations needed (uses existing Guest entity)
**Configuration**: No additional config required

