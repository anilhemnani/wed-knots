# Moments Manager - UI Management Guide

## Quick Start

The application is now running on **http://localhost:8080** with a fully functional UI for managing events, guests, hosts, RSVPs, and attendees.

---

## Features Implemented

### 1. Guest Management Interface

#### Guest List View (`/admin/events/{eventId}/guests`)
- Display all guests for a specific event
- Add new guest button with form
- Edit guest information
- Delete guests with confirmation
- Guest information includes:
  - Family name
  - Contact name
  - Email address
  - Phone number
  - Side (Bride/Groom/Both)
  - Address
  - Max attendees

#### Add/Edit Guest Form (`admin_guest_form.html`)
- Form validation for required fields
- Dropdown selector for bride/groom side
- Textarea for address
- Number input for max attendees
- Cancel and Save buttons
- When a guest is created, an RSVP with "Pending" status is automatically generated

---

### 2. RSVP Management Interface

#### RSVP List View (`/admin/events/{eventId}/rsvps`)
- View all RSVPs for an event
- Status display with color-coded badges:
  - Green: Accepted
  - Yellow: Pending
  - Red: Declined
- Summary statistics:
  - Total RSVPs
  - Accepted count
  - Pending count
  - Declined count
- Link to manage attendees for each RSVP
- Attendee count displayed

---

### 3. Attendee Management Interface

#### Attendee List View (`/admin/rsvps/{rsvpId}/attendees`)
- View all attendees for a specific RSVP
- Add new attendee button
- Edit attendee information
- Delete attendees with confirmation
- Summary statistics:
  - Total attendees
  - Adult count
  - Child count
- Attendee information includes:
  - Name
  - Mobile number
  - Age group (Adult/Child)

#### Add/Edit Attendee Form (`admin_attendee_form.html`)
- Form validation
- Context information showing event and guest
- Dropdown selector for age group (Adult/Child)
- Name and mobile number fields
- Cancel and Save buttons

---

## Database Relationships

### Guest → RSVP (One-to-One)
- Each guest has exactly one RSVP
- RSVP is automatically created when guest is added
- RSVP defaults to "Pending" status
- Deleting guest cascades delete to RSVP and attendees

### RSVP → Attendee (One-to-Many)
- Each RSVP can have multiple attendees
- RSVP starts with empty attendee list
- Attendees are cascade deleted when RSVP is deleted
- Attendee count is tracked in RSVP

---

## UI Components

### Navigation Bar
- Branding with "Moments Manager"
- Logout link available from all pages
- Responsive mobile menu

### Cards
- Consistent Bootstrap 5 card styling
- Color-coded headers
- Icon integration for visual clarity

### Forms
- Bootstrap form controls
- Input validation (required fields marked with *)
- Clear error messaging
- Cancel and Save action buttons

### Tables
- Responsive design
- Hover effects
- Striped rows
- Action buttons (Edit, Delete, View)
- Confirmation dialogs for delete operations

### Status Badges
- Accepted: Green
- Pending: Yellow/Warning
- Declined: Red
- Age Group Adult: Light Blue (info)
- Age Group Child: Yellow/Warning

---

## Web Controller Methods

### AdminWebController
New methods added for managing guests, RSVPs, and attendees:

**Guest Management:**
- `eventGuests()` - GET list of guests for event
- `newGuest()` - GET form to add guest
- `createGuest()` - POST create new guest
- `editGuest()` - GET form to edit guest
- `updateGuest()` - POST update guest
- `deleteGuest()` - POST delete guest

**RSVP Management:**
- `eventRsvps()` - GET list of RSVPs for event

**Attendee Management:**
- `rsvpAttendees()` - GET list of attendees for RSVP
- `newAttendee()` - GET form to add attendee
- `createAttendee()` - POST create new attendee
- `editAttendee()` - GET form to edit attendee
- `updateAttendee()` - POST update attendee
- `deleteAttendee()` - POST delete attendee

---

## Service Layer Integration

### GuestService
- Handles guest creation with automatic RSVP generation
- Guest CRUD operations
- Event-based guest filtering

### RSVPService
- RSVP status management
- Accept/Decline operations
- RSVP retrieval and filtering

### AttendeeService
- Attendee CRUD operations
- RSVP-based attendee filtering
- Cascade delete handling

---

## File Structure

### Templates Created:
```
templates/
├── admin_event_guests.html      (Guest list and management)
├── admin_guest_form.html         (Add/edit guest form)
├── admin_event_rsvps.html        (RSVP list with stats)
├── admin_rsvp_attendees.html     (Attendee list with stats)
└── admin_attendee_form.html      (Add/edit attendee form)
```

### Controllers Updated:
```
web/
└── AdminWebController.java       (New endpoints for guest/RSVP/attendee management)
```

### Services:
```
service/
├── GuestService.java              (Guest operations with auto RSVP creation)
├── RSVPService.java               (RSVP status management)
└── AttendeeService.java           (Attendee management)
```

### Repositories:
```
repository/
├── GuestRepository.java
├── RSVPRepository.java
└── AttendeeRepository.java
```

---

## Workflow Example

### Adding an Event with Guests and Attendees:

1. **Admin logs in** and goes to `/admin/dashboard`
2. **Clicks event** to view event details
3. **Clicks "Manage Guests"** quick action button
4. **Adds guests** by clicking "Add Guest" button
   - Fills in family name, contact info, side, address, max attendees
   - System automatically creates a Pending RSVP for each guest
5. **Clicks on RSVPs** to view all responses
   - Sees summary cards with acceptance statistics
6. **Clicks "Attendees"** for a guest's RSVP
7. **Adds attendees** by clicking "Add Attendee" button
   - Fills in name, mobile number, age group
   - System tracks adult vs child count
8. **Can edit or delete** attendees as needed

---

## Data Flow Diagram

```
Event
  ├── Guest 1 (Created)
  │   └── RSVP 1 (Auto-created with Pending status)
  │       ├── Attendee 1 (Adult)
  │       ├── Attendee 2 (Child)
  │       └── Attendee 3 (Adult)
  ├── Guest 2 (Created)
  │   └── RSVP 2 (Auto-created with Pending status)
  │       └── Attendee 4 (Adult)
  └── Guest 3 (Created)
      └── RSVP 3 (Auto-created with Pending status)
          └── (No attendees yet)
```

---

## Key Features Summary

✅ **Guest Management**
- Full CRUD operations
- Automatic RSVP creation on guest addition
- Family-based grouping
- Contact information tracking
- Max attendees per family

✅ **RSVP Management**
- Status tracking (Accepted, Pending, Declined)
- One-to-one mapping with guests
- Summary statistics
- Visual status indicators

✅ **Attendee Management**
- Multiple attendees per RSVP
- Age group classification (Adult/Child)
- Contact information per attendee
- Cascade delete protection
- Statistics by age group

✅ **User Interface**
- Bootstrap 5 responsive design
- Consistent navigation
- Color-coded status indicators
- Action confirmation dialogs
- Form validation
- Summary cards with statistics

✅ **Database**
- Liquibase migration support
- Foreign key constraints
- Cascade delete operations
- Orphan removal
- Entity relationships

---

## Technical Stack

- **Backend:** Spring Boot 3.2.0
- **Database:** H2 (in-memory)
- **ORM:** Hibernate 6.3.1
- **Migration:** Liquibase 4.24.0
- **View Template:** Thymeleaf
- **Frontend:** Bootstrap 5.3.2
- **Security:** Spring Security

---

## Access the Application

**URL:** http://localhost:8080

**Default Login:**
- Username: `admin`
- Password: (empty on first login, set during setup)

**Available Dashboards:**
- Admin: `/admin/dashboard`
- Host: `/host/dashboard`
- Guest: `/guest/dashboard`

---

## Next Steps (Optional Enhancements)

- Add bulk import for guests (CSV upload)
- Email notifications for RSVP status changes
- Guest list PDF export
- Attendance tracking during event
- Dietary preferences for attendees
- Seating arrangement management
- Payment/invoice tracking for guests
- Photo gallery per event
- Guest feedback/rating system


