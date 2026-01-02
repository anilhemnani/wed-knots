# Moments Manager - Architecture & System Design

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    WEB BROWSER (Bootstrap 5 UI)              │
│  ┌──────────────┬──────────────┬──────────────┬─────────────┐
│  │ Guest List   │ Guest Form   │ RSVP List    │ Attendee    │
│  │ Management   │ (Add/Edit)   │ Management   │ Management  │
│  └──────────────┴──────────────┴──────────────┴─────────────┘
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP Request
┌─────────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION LAYER                   │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         AdminWebController (Request Handler)          │  │
│  │  ┌──────────────┬──────────────┬────────────────────┐ │  │
│  │  │ Guest        │ RSVP         │ Attendee           │ │  │
│  │  │ Endpoints    │ Endpoints    │ Endpoints          │ │  │
│  │  │              │              │                    │ │  │
│  │  │ /guests/*    │ /rsvps/*     │ /attendees/*       │ │  │
│  │  └──────────────┴──────────────┴────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                              ↓                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         SERVICE LAYER (Business Logic)                │  │
│  │  ┌──────────────┬──────────────┬────────────────────┐ │  │
│  │  │ GuestService │ RSVPService  │ AttendeeService    │ │  │
│  │  │              │              │                    │ │  │
│  │  │ - CRUD       │ - Status     │ - CRUD             │ │  │
│  │  │ - Auto RSVP  │ - Accept/    │ - Manage           │ │  │
│  │  │   creation   │   Decline    │   attendees        │ │  │
│  │  │ - Event      │ - Filtering  │ - Event filtering  │ │  │
│  │  │   filtering  │              │                    │ │  │
│  │  └──────────────┴──────────────┴────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                              ↓                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │      REPOSITORY LAYER (Data Access)                   │  │
│  │  ┌──────────────┬──────────────┬────────────────────┐ │  │
│  │  │ GuestRepo    │ RSVPRepo     │ AttendeeRepo       │ │  │
│  │  │              │              │                    │ │  │
│  │  │ - findAll()  │ - findAll()  │ - findByRsvpId()  │ │  │
│  │  │ - findById() │ - findById() │ - findAll()        │ │  │
│  │  │ - save()     │ - save()     │ - save()           │ │  │
│  │  │ - delete()   │ - delete()   │ - delete()         │ │  │
│  │  └──────────────┴──────────────┴────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                              ↓                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              ENTITY LAYER (Models)                    │  │
│  │  ┌──────────────┬──────────────┬────────────────────┐ │  │
│  │  │ Guest        │ RSVP         │ Attendee           │ │  │
│  │  │              │              │                    │ │  │
│  │  │ - id         │ - id         │ - id               │ │  │
│  │  │ - name       │ - guest (1:1)│ - name             │ │  │
│  │  │ - email      │ - status     │ - mobile           │ │  │
│  │  │ - phone      │ - attendees  │ - ageGroup         │ │  │
│  │  │ - rsvp (1:1) │   (1:many)   │ - rsvp (M:1)       │ │  │
│  │  │ - event      │ - event      │                    │ │  │
│  │  └──────────────┴──────────────┴────────────────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
│                              ↓                               │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (H2)                       │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  guest_tbl   │───→│  rsvp_tbl    │───→│attendee_tbl  │  │
│  │              │    │              │    │              │  │
│  │  id          │    │  id          │    │  id          │  │
│  │  family_name │    │  guest_id    │    │  name        │  │
│  │  contact_*   │    │  event_id    │    │  mobile_#    │  │
│  │  side        │    │  status      │    │  age_group   │  │
│  │  address     │    │  attendee_ct │    │  rsvp_id (FK)│  │
│  │  max_attend  │    │              │    │              │  │
│  │  event_id    │    │              │    │              │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Liquibase Migrations (11 Changesets)        │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagram

### Creating a Guest
```
1. User clicks "Add Guest"
        ↓
2. Form displayed (guest_form.html)
        ↓
3. User fills form and submits
        ↓
4. POST /admin/events/{id}/guests/new
        ↓
5. AdminWebController.createGuest()
        ↓
6. GuestService.createGuest(guest)
        ├─→ GuestRepository.save(guest)
        │        ↓
        │   INSERT INTO guest_tbl
        │
        └─→ RSVPService.createRSVP(rsvp)
                 ↓
            RSVPRepository.save(rsvp)
                 ↓
            INSERT INTO rsvp_tbl (status='Pending')
        ↓
7. Redirect to guest list
        ↓
8. Display "Guest added successfully"
```

### Viewing RSVPs
```
1. User clicks "View RSVPs"
        ↓
2. GET /admin/events/{id}/rsvps
        ↓
3. AdminWebController.eventRsvps()
        ↓
4. RSVPRepository.findAll()
        ↓
5. Database filters by eventId
        ↓
6. Service calculates statistics
        ├─→ Count by status='Accepted'
        ├─→ Count by status='Pending'
        └─→ Count by status='Declined'
        ↓
7. Template renders with data
        ├─→ Table rows with RSVP data
        ├─→ Status badges
        ├─→ Summary cards
        └─→ Attendee links
        ↓
8. Browser displays HTML
```

### Adding Attendee
```
1. User clicks "Add Attendee"
        ↓
2. Form displayed with RSVP context
        ↓
3. User fills form and submits
        ↓
4. POST /admin/rsvps/{rsvpId}/attendees/new
        ↓
5. AdminWebController.createAttendee()
        ↓
6. AttendeeService.createAttendee()
        ├─→ RSVPService.getRSVPById(rsvpId)
        │
        └─→ AttendeeRepository.save(attendee)
                 ↓
            INSERT INTO attendee_tbl
        ↓
7. Redirect to attendee list
        ↓
8. Statistics recalculated
        ├─→ Total attendees count
        ├─→ Adult count
        └─→ Child count
        ↓
9. Display updated list
```

---

## Entity Relationship Diagram (ERD)

```
┌──────────────────────┐
│   wedding_event_tbl  │
├──────────────────────┤
│ id (PK)              │
│ name                 │
│ date                 │
│ status               │
│ bride_name           │
│ groom_name           │
│ place                │
│ ...                  │
└──────────────────────┘
          │ (1)
          │
          │ (has many)
          │
          ↓
┌──────────────────────┐
│     guest_tbl        │
├──────────────────────┤
│ id (PK)              │
│ family_name          │
│ contact_name         │
│ contact_email        │
│ contact_phone        │
│ side                 │
│ address              │
│ max_attendees        │
│ event_id (FK)        │
└──────────────────────┘
          │ (1)
          │ (has 1)
          │
          ↓
┌──────────────────────┐
│      rsvp_tbl        │
├──────────────────────┤
│ id (PK)              │
│ guest_id (FK, UNIQUE)│
│ event_id             │
│ status               │◄── Values: Pending, Accepted, Declined
│ attendee_count       │
└──────────────────────┘
          │ (1)
          │ (has many)
          │
          ↓
┌──────────────────────┐
│    attendee_tbl      │
├──────────────────────┤
│ id (PK)              │
│ name                 │
│ mobile_number        │
│ age_group            │◄── Values: Adult, Child
│ rsvp_id (FK)         │
└──────────────────────┘
```

---

## Class Diagram - Service Layer

```
┌─────────────────────────────────────┐
│      GuestService                   │
├─────────────────────────────────────┤
│ - guestRepository: GuestRepository  │
│ - rsvpService: RSVPService          │
├─────────────────────────────────────┤
│ + createGuest(guest): Guest         │
│ + updateGuest(id, details): Guest   │
│ + deleteGuest(id): void             │
│ + getGuestById(id): Optional        │
│ + getAllGuests(): List              │
│ + getGuestsByEventId(id): List      │
│ + getGuestRSVP(guestId): Optional   │
│ + updateGuestRSVP(id, status): RSVP│
└─────────────────────────────────────┘
            │ creates
            ↓
┌─────────────────────────────────────┐
│      RSVPService                    │
├─────────────────────────────────────┤
│ - rsvpRepository: RSVPRepository    │
├─────────────────────────────────────┤
│ + createRSVP(rsvp): RSVP            │
│ + getRSVPById(id): Optional         │
│ + getRSVPByGuestId(id): Optional    │
│ + getAllRSVPs(): List               │
│ + updateRSVPStatus(id, st): RSVP    │
│ + acceptRSVP(id, count): RSVP       │
│ + declineRSVP(id): RSVP             │
│ + deleteRSVP(id): void              │
└─────────────────────────────────────┘
            │ contains
            ↓
┌─────────────────────────────────────┐
│     AttendeeService                 │
├─────────────────────────────────────┤
│ - attendeeRepository: AttendeeRep.  │
│ - rsvpService: RSVPService          │
├─────────────────────────────────────┤
│ + createAttendee(rsvpId, att): Att. │
│ + getAttendeeById(id): Optional     │
│ + getAllAttendees(): List           │
│ + getAttendeesByRsvpId(id): List    │
│ + updateAttendee(id, det): Attendee│
│ + deleteAttendee(id): void          │
│ + deleteAttendeesByRsvpId(id): void│
└─────────────────────────────────────┘
```

---

## UI Navigation Map

```
┌─────────────────────────────────────────────────────────┐
│              Admin Dashboard                            │
│          /admin/dashboard (Event List)                  │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        ↓                         ↓
┌──────────────┐        ┌──────────────┐
│ Event View   │        │ Event Form   │
│ /events/{id} │        │ /events/new  │
└────┬─────────┘        └──────────────┘
     │
     │  Quick Actions Panel
     │
   ┌─┴─────────────────────────┬──────────────┬──────────────┐
   ↓                           ↓              ↓              ↓
┌──────────────┐      ┌──────────────┐  ┌──────────────┐  ┌──────────┐
│ Manage Hosts │      │Manage Guests │  │ View RSVPs   │  │ (Other)  │
│ /hosts       │      │ /guests      │  │ /rsvps       │  └──────────┘
└──────────────┘      └────┬─────────┘  └────┬─────────┘
                           │                  │
              ┌────────────┴────┐             │
              ↓                 ↓             │
        ┌──────────────┐  ┌─────────────┐    │
        │Guest List    │  │ Guest Form  │    │
        │ /guests      │  │ /guests/new │    │
        └──────────────┘  └─────────────┘    │
                                             │
                                ┌────────────┴─────┐
                                ↓                  ↓
                        ┌──────────────┐   ┌────────────────┐
                        │ RSVP List    │   │ Add/Edit RSVP  │
                        │ /rsvps       │   │ (Internal)     │
                        └────┬─────────┘   └────────────────┘
                             │
                    ┌────────┴────────┐
                    ↓                 ↓
            ┌──────────────┐   ┌────────────────┐
            │Attendee List │   │ Attendee Form  │
            │ /attendees   │   │ /attendees/new │
            └──────────────┘   └────────────────┘
```

---

## Request-Response Flow

### GET Guest List
```
User Request:
  GET /admin/events/1/guests
  
Server Processing:
  1. Check @PreAuthorize("hasRole('ADMIN')")
  2. Call eventGuests(1)
  3. Query WeddingEvent repository for ID 1
  4. Query Guest repository, filter by event_id=1
  5. Prepare model data
  6. Render admin_event_guests.html template

HTTP Response:
  Status: 200 OK
  Content-Type: text/html
  Body: HTML page with guest table
```

### POST Create Guest
```
User Request:
  POST /admin/events/1/guests/new
  Form Data:
    - familyName: "Sharma"
    - contactName: "Ravi Sharma"
    - contactPhone: "9876543210"
    - side: "Bride"
    - maxAttendees: 5

Server Processing:
  1. Check @PreAuthorize("hasRole('ADMIN')")
  2. Validate form data
  3. Call GuestService.createGuest()
     a. Save Guest entity
     b. Create RSVP with status='Pending'
     c. Return created guest
  4. Redirect to guest list

HTTP Response:
  Status: 302 Found
  Location: /admin/events/1/guests
  
User Sees:
  Guest list page with new guest added
```

### GET Attendee List
```
User Request:
  GET /admin/rsvps/1/attendees

Server Processing:
  1. Check @PreAuthorize("hasRole('ADMIN')")
  2. Call rsvpAttendees(1)
  3. Query RSVP for ID 1
  4. Query Attendee repository for rsvp_id=1
  5. Calculate statistics
     - Total count
     - Adult count
     - Child count
  6. Prepare model data
  7. Render admin_rsvp_attendees.html

HTTP Response:
  Status: 200 OK
  Content-Type: text/html
  Body: HTML page with attendee statistics and table
```

---

## Error Handling Flow

```
User Action
    ↓
Web Controller
    ↓
Input Validation
    ├─→ If invalid → Show form with errors
    │
    └─→ If valid → Service Layer
                ↓
            Database Operation
                ↓
            Success?
            ├─→ Yes → Commit transaction
            │         ↓
            │         Redirect with success message
            │
            └─→ No → Rollback transaction
                     ↓
                     Show error page/message
```

---

## Technology Integration

```
┌─────────────────────────────────────────────────┐
│           Spring Boot Framework                  │
├─────────────────────────────────────────────────┤
│ ┌──────────┐  ┌──────────┐  ┌──────────────┐   │
│ │ Security │  │ Web MVC  │  │ Data (JPA)   │   │
│ └──────────┘  └──────────┘  └──────────────┘   │
└─────────────────────────────────────────────────┘
                     ↓
        ┌──────────────────────────┐
        │   Hibernate ORM          │
        └──────────────────────────┘
                     ↓
        ┌──────────────────────────┐
        │   Liquibase Migration    │
        └──────────────────────────┘
                     ↓
        ┌──────────────────────────┐
        │   H2 Database            │
        │ (In-Memory)              │
        └──────────────────────────┘
```

---

## Deployment Architecture

```
Developer Machine
    ↓
Maven Build
    ↓
├─→ Compile
├─→ Test (skipped)
├─→ Package JAR
└─→ Create Docker image (optional)
    ↓
Production Server
    ↓
Java Runtime
    ↓
Spring Boot Application
    ├─→ Tomcat Server (Port 8080)
    ├─→ H2 Database (In-Memory)
    └─→ Spring Security
    ↓
Client Browser
    ↓
Bootstrap 5 UI
    └─→ Responsive Design
```

---

## Summary

This architecture ensures:
- ✅ **Separation of Concerns** - Clean layering
- ✅ **Scalability** - Service-based design
- ✅ **Maintainability** - Clear responsibility boundaries
- ✅ **Testability** - Mockable components
- ✅ **Security** - Role-based access control
- ✅ **Data Integrity** - Cascading operations
- ✅ **User Experience** - Responsive Bootstrap UI


