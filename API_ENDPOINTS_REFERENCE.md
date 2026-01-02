# Moments Manager - API & UI Endpoints Reference

## Event Management Endpoints

### Dashboard
- **GET** `/admin/dashboard` - Admin dashboard with event list

### Event CRUD
- **GET** `/admin/events/new` - Form to create new event
- **POST** `/admin/events/new` - Create new event
- **GET** `/admin/events/{id}` - View event details
- **GET** `/admin/events/{id}/edit` - Form to edit event
- **POST** `/admin/events/{id}/edit` - Update event
- **POST** `/admin/events/{id}/delete` - Delete event

---

## Guest Management Endpoints

### Guest List
- **GET** `/admin/events/{id}/guests` - View all guests for event

### Guest CRUD
- **GET** `/admin/events/{id}/guests/new` - Form to add guest
- **POST** `/admin/events/{id}/guests/new` - Create new guest
- **GET** `/admin/guests/{guestId}/edit` - Form to edit guest
- **POST** `/admin/guests/{guestId}/edit` - Update guest
- **POST** `/admin/guests/{guestId}/delete` - Delete guest

### Guest Response Details
**Create Guest Request (POST /admin/events/{id}/guests/new):**
```json
{
  "familyName": "Sharma",
  "contactName": "Ravi Sharma",
  "contactEmail": "ravi@example.com",
  "contactPhone": "9876543210",
  "side": "Bride",
  "address": "Delhi",
  "maxAttendees": 5,
  "eventId": 1
}
```

**Guest Object in Database:**
- `id` - Auto-generated ID
- `familyName` - Family surname
- `contactName` - Primary contact name
- `contactEmail` - Email address
- `contactPhone` - Phone number
- `side` - Bride/Groom/Both
- `address` - Residential address
- `maxAttendees` - Maximum people they can bring
- `eventId` - Associated event
- `rsvp` - Associated RSVP (one-to-one)

---

## RSVP Management Endpoints

### RSVP List
- **GET** `/admin/events/{id}/rsvps` - View all RSVPs for event

### RSVP Response Details

**RSVP Object in Database:**
- `id` - Auto-generated ID
- `guest` - Associated guest (one-to-one)
- `eventId` - Associated event
- `status` - Response status (Pending, Accepted, Declined)
- `attendeeCount` - Number of attendees attending
- `attendees` - List of attendee objects (one-to-many)

**Status Values:**
- `Pending` - Default status when guest is created
- `Accepted` - Guest confirmed attendance
- `Declined` - Guest declined invitation

**Summary Statistics Available:**
- Total RSVPs count
- Accepted RSVPs count
- Pending RSVPs count
- Declined RSVPs count

---

## Attendee Management Endpoints

### Attendee List
- **GET** `/admin/rsvps/{rsvpId}/attendees` - View attendees for RSVP

### Attendee CRUD
- **GET** `/admin/rsvps/{rsvpId}/attendees/new` - Form to add attendee
- **POST** `/admin/rsvps/{rsvpId}/attendees/new` - Create new attendee
- **GET** `/admin/attendees/{attendeeId}/edit` - Form to edit attendee
- **POST** `/admin/attendees/{attendeeId}/edit` - Update attendee
- **POST** `/admin/attendees/{attendeeId}/delete` - Delete attendee

### Attendee Response Details

**Create Attendee Request (POST /admin/rsvps/{rsvpId}/attendees/new):**
```json
{
  "name": "Priya Sharma",
  "mobileNumber": "9876543211",
  "ageGroup": "Adult",
  "rsvpId": 1
}
```

**Attendee Object in Database:**
- `id` - Auto-generated ID
- `name` - Attendee name
- `mobileNumber` - Contact mobile number
- `ageGroup` - "Adult" or "Child"
- `rsvp` - Associated RSVP (many-to-one)

**Age Group Values:**
- `Adult` - For adults 18+
- `Child` - For children under 18

---

## Host Management Endpoints

### Host List
- **GET** `/admin/events/{id}/hosts` - View all hosts for event

### Note on Hosts
Host management UI is partially implemented. Future enhancements should include:
- Add new host form
- Edit host details
- Delete host
- Assign hosts to guests
- Track host responsibilities

---

## Service Layer Methods

### GuestService
```java
// Create guest (auto-creates RSVP with Pending status)
Guest createGuest(Guest guest)

// Update guest information
Guest updateGuest(Long id, Guest guestDetails)

// Retrieve all guests
List<Guest> getAllGuests()

// Retrieve specific guest
Optional<Guest> getGuestById(Long id)

// Get guests for event
List<Guest> getGuestsByEventId(Long eventId)

// Delete guest (cascades to RSVP and attendees)
void deleteGuest(Long id)

// Get guest's RSVP
Optional<RSVP> getGuestRSVP(Long guestId)

// Update guest RSVP status
RSVP updateGuestRSVP(Long guestId, String status, int attendeeCount)
```

### RSVPService
```java
// Retrieve all RSVPs
List<RSVP> getAllRSVPs()

// Retrieve specific RSVP
Optional<RSVP> getRSVPById(Long id)

// Retrieve RSVP by guest ID
Optional<RSVP> getRSVPByGuestId(Long guestId)

// Create RSVP
RSVP createRSVP(RSVP rsvp)

// Update RSVP status
RSVP updateRSVPStatus(Long rsvpId, String status, int attendeeCount)

// Accept RSVP
RSVP acceptRSVP(Long rsvpId, int attendeeCount)

// Decline RSVP
RSVP declineRSVP(Long rsvpId)

// Delete RSVP
void deleteRSVP(Long id)
```

### AttendeeService
```java
// Retrieve all attendees
List<Attendee> getAllAttendees()

// Retrieve specific attendee
Optional<Attendee> getAttendeeById(Long id)

// Get attendees for RSVP
List<Attendee> getAttendeesByRsvpId(Long rsvpId)

// Create attendee for RSVP
Attendee createAttendee(Long rsvpId, Attendee attendee)

// Update attendee information
Attendee updateAttendee(Long id, Attendee attendeeDetails)

// Delete attendee
void deleteAttendee(Long id)

// Delete all attendees for RSVP
void deleteAttendeesByRsvpId(Long rsvpId)
```

---

## Database Tables

### guest_tbl
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| family_name | VARCHAR(255) | |
| contact_name | VARCHAR(255) | |
| contact_email | VARCHAR(255) | |
| contact_phone | VARCHAR(255) | |
| side | VARCHAR(255) | |
| address | VARCHAR(255) | |
| max_attendees | INT | |
| event_id | BIGINT | FOREIGN KEY (wedding_event_tbl) |

### rsvp_tbl
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| guest_id | BIGINT | UNIQUE, FOREIGN KEY (guest_tbl) |
| event_id | BIGINT | |
| status | VARCHAR(50) | DEFAULT 'Pending' |
| attendee_count | INT | DEFAULT 0 |

### attendee_tbl
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT |
| name | VARCHAR(255) | |
| mobile_number | VARCHAR(20) | |
| age_group | VARCHAR(50) | |
| rsvp_id | BIGINT | FOREIGN KEY (rsvp_tbl) ON DELETE CASCADE |

---

## Liquibase Changesets

### Changeset 8: Create RSVP Table
- Creates `rsvp_tbl` with guest_id unique constraint
- Adds foreign key to guest_tbl
- Sets default status to "Pending"
- Sets default attendee_count to 0

### Changeset 9: Insert RSVPs for Existing Guests
- Creates initial RSVPs for demo guests
- All set to "Pending" status
- All have attendee_count of 0

### Changeset 11: Create Attendee Table
- Creates `attendee_tbl` for guest attendees
- Adds foreign key to rsvp_tbl with CASCADE DELETE
- Supports many-to-one relationship with RSVP

---

## Form Validations

### Guest Form
- **familyName** - Required, text input
- **contactName** - Required, text input
- **contactEmail** - Optional, email validation
- **contactPhone** - Required, phone input
- **side** - Required, dropdown (Bride/Groom/Both)
- **address** - Optional, textarea
- **maxAttendees** - Required, number input (min: 0)

### Attendee Form
- **name** - Required, text input
- **mobileNumber** - Required, phone input
- **ageGroup** - Required, dropdown (Adult/Child)

---

## HTTP Status Codes

**Successful Operations:**
- `200 OK` - GET request successful
- `302 Found` - Redirect after POST (Create/Update/Delete)

**Errors:**
- `404 Not Found` - Resource not found
- `400 Bad Request` - Invalid form data
- `403 Forbidden` - User not authorized (ADMIN role required)

---

## Security

### Role-Based Access Control
- `@PreAuthorize("hasRole('ADMIN')")` on all admin endpoints
- All guest, RSVP, and attendee management requires ADMIN role
- Spring Security filters configured

---

## UI Components Used

### Bootstrap 5 Classes
- `.card` - Card containers
- `.table table-hover` - Interactive tables
- `.badge` - Status indicators
- `.btn btn-primary/success/danger` - Action buttons
- `.alert` - Information messages
- `.form-control/.form-select` - Form inputs
- `.container` - Main content wrapper
- `.row / .col-md-*` - Grid layout
- `.navbar` - Navigation bar
- `.d-flex / .justify-content-between` - Flexbox utilities

### Custom Styling
- Color-coded status badges
- Icon integration with Bootstrap Icons
- Responsive mobile-first design
- Hover effects on table rows

---

## Browser Compatibility

- Chrome/Edge (Latest)
- Firefox (Latest)
- Safari (Latest)
- Mobile browsers (iOS Safari, Chrome Mobile)

---

## Performance Considerations

- Lazy loading for attendee lists (via Fetch strategy in Hibernate)
- Cascade operations reduce N+1 queries
- Index on foreign keys for fast lookups
- Pagination (can be added for large guest lists)

---

## Testing Checklist

- [ ] Create event
- [ ] Add guest to event
- [ ] Verify RSVP auto-created with Pending status
- [ ] Edit guest details
- [ ] Delete guest (verify cascade delete)
- [ ] View all RSVPs for event
- [ ] Check RSVP summary statistics
- [ ] Add attendee to RSVP
- [ ] View attendees with statistics
- [ ] Edit attendee
- [ ] Delete attendee
- [ ] Test form validations
- [ ] Test delete confirmations
- [ ] Test responsive design on mobile
- [ ] Verify all navigation links work
- [ ] Test logout functionality

---

## Support & Troubleshooting

### Common Issues

**Issue:** Event not showing guests
- **Solution:** Ensure event ID is correct, refresh page

**Issue:** RSVP not auto-creating
- **Solution:** Guest must be created through UI (createGuest method)

**Issue:** Attendee not appearing
- **Solution:** Verify RSVP exists and attendee is linked correctly

**Issue:** Delete not working
- **Solution:** Check for JavaScript errors, ensure confirmation accepted


