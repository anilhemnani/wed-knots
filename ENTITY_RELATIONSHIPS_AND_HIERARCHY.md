# Entity Relationships and Page Hierarchy

## Overview
This document describes the aggregation relationships and hierarchical structure of entities in the Moments Manager application, along with the corresponding page navigation hierarchy.

## Entity Relationship Diagram

```
WeddingEvent (Root/Aggregate Root)
├── Host (Many-to-One, Composition)
│   └── Owned by Event
│   └── Cascade: ALL, Orphan Removal: true
│
└── Guest (Many-to-One, Composition)
    └── Owned by Event
    └── Cascade: ALL, Orphan Removal: true
    └── RSVP (One-to-One, Composition)
        └── Owned by Guest
        └── Cascade: ALL, Orphan Removal: true
        └── Attendee (Many-to-One, Composition)
            └── Owned by RSVP
            └── Cascade: ALL, Orphan Removal: true
```

## Entity Aggregation Hierarchy

### 1. WeddingEvent (Aggregate Root)
**Purpose:** Top-level entity representing a wedding event

**Owns:**
- **Hosts** - People who can manage the event
- **Guests** - People invited to the event

**Relationship Type:** Aggregation with Composition
- When an event is deleted, all associated hosts and guests are deleted
- Hosts and guests cannot exist without an event

**JPA Mapping:**
```java
@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Host> hosts;

@OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Guest> guests;
```

### 2. Host
**Purpose:** Event coordinator/manager

**Belongs To:** WeddingEvent (required, non-null)

**Relationship Type:** Strong composition - Part of Event aggregate

**JPA Mapping:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "event_id", nullable = false)
private WeddingEvent event;
```

**Lifecycle:**
- Created in context of an event
- Cannot exist without an event
- Deleted when event is deleted

### 3. Guest
**Purpose:** Invited person/family to the event

**Belongs To:** WeddingEvent (required, non-null)

**Owns:** RSVP (optional, one-to-one)

**Relationship Type:** Strong composition - Part of Event aggregate

**JPA Mapping:**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "event_id", nullable = false)
private WeddingEvent event;

@OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true)
private RSVP rsvp;
```

**Lifecycle:**
- Created in context of an event
- Cannot exist without an event
- Deleted when event is deleted
- When deleted, associated RSVP is also deleted

### 4. RSVP
**Purpose:** Response status and attendee management for a guest

**Belongs To:** Guest (required, one-to-one)

**Owns:** Attendees (multiple, one-to-many)

**Relationship Type:** Strong composition - Part of Guest aggregate

**JPA Mapping:**
```java
@OneToOne
@JoinColumn(name = "guest_id", unique = true)
private Guest guest;

@OneToMany(mappedBy = "rsvp", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Attendee> attendees;
```

**Lifecycle:**
- Created when guest responds to invitation
- Automatically deleted when guest is deleted
- When deleted, all attendees are also deleted

**Statuses:**
- Pending (default)
- Accepted
- Declined

### 5. Attendee
**Purpose:** Individual person attending the event (part of a guest's party)

**Belongs To:** RSVP (required, many-to-one)

**Relationship Type:** Strong composition - Part of RSVP aggregate

**JPA Mapping:**
```java
@ManyToOne
@JoinColumn(name = "rsvp_id")
private RSVP rsvp;
```

**Lifecycle:**
- Created in context of an RSVP
- Cannot exist without an RSVP
- Deleted when RSVP is deleted
- Deleted when parent guest is deleted
- Deleted when parent event is deleted

**Age Groups:**
- Adult
- Child

## Page Navigation Hierarchy

### Admin/Host View

```
1. Home/Login
   └── Select Role: Admin, Host, or Guest

2. Admin Dashboard
   ├── Events List
   │   └── Event Card (View/Edit/Delete)
   │       ├── Event Details Form
   │       │
   │       ├── Manage Hosts
   │       │   ├── Hosts List
   │       │   └── Add/Edit/Delete Host Form
   │       │
   │       └── Manage Guests
   │           ├── Guests List
   │           └── Guest Details
   │               ├── Guest Edit Form
   │               ├── RSVP Status Display
   │               └── Manage Attendees
   │                   ├── Attendees List
   │                   └── Add/Edit/Delete Attendee Form
   │
   └── Create New Event

3. Host Dashboard
   ├── My Events (Events where user is host)
   └── Same navigation as Admin for assigned events
```

### Guest View

```
1. Guest Login
   └── Family Name + Mobile Number

2. Guest Dashboard
   └── My Event Details
       ├── Event Information (Read-only)
       ├── RSVP Status
       └── Manage Attendees
           ├── Attendees List
           └── Add/Edit/Delete Attendee Form
```

## URL Structure

### Event Management
```
/admin/dashboard                          - Admin dashboard
/events                                   - Events list
/events/new                               - Create new event
/events/{eventId}/edit                    - Edit event
/events/{eventId}/delete                  - Delete event (POST)
```

### Host Management (in context of Event)
```
/events/{eventId}/hosts                   - List hosts for event
/events/{eventId}/hosts/new               - Add host to event
/events/{eventId}/hosts/{hostId}/edit     - Edit host
/events/{eventId}/hosts/{hostId}/delete   - Delete host (POST)
```

### Guest Management (in context of Event)
```
/events/{eventId}/guests                  - List guests for event
/events/{eventId}/guests/new              - Add guest to event
/events/{eventId}/guests/{guestId}/edit   - Edit guest (shows RSVP status)
/events/{eventId}/guests/{guestId}/delete - Delete guest (POST)
```

### Attendee Management (in context of Guest/RSVP)
```
/guests/{guestId}/rsvp/attendees              - List attendees for guest
/guests/{guestId}/rsvp/attendees/new          - Add attendee
/guests/{guestId}/rsvp/attendees/{id}/edit    - Edit attendee
/guests/{guestId}/rsvp/attendees/{id}/delete  - Delete attendee (POST)
```

## Cascade Behavior

### Deleting an Event
When an event is deleted:
1. ✅ All hosts are deleted
2. ✅ All guests are deleted
3. ✅ All RSVPs (through guest cascade) are deleted
4. ✅ All attendees (through RSVP cascade) are deleted

### Deleting a Guest
When a guest is deleted:
1. ✅ Associated RSVP is deleted
2. ✅ All attendees (through RSVP cascade) are deleted
3. ❌ Event remains intact
4. ❌ Other guests remain intact

### Deleting an RSVP
When an RSVP is deleted:
1. ✅ All attendees are deleted
2. ❌ Guest remains intact
3. ❌ Event remains intact

### Deleting an Attendee
When an attendee is deleted:
1. ❌ RSVP remains intact
2. ❌ Guest remains intact
3. ❌ Event remains intact
4. ✅ Attendee count updated

## Orphan Removal

### What is Orphan Removal?
Orphan removal ensures that when a child entity is removed from the parent's collection, it is automatically deleted from the database, even if the parent itself is not deleted.

### Applied To:
- **Event → Hosts**: `orphanRemoval = true`
  - If a host is removed from event.hosts list, it's deleted
  
- **Event → Guests**: `orphanRemoval = true`
  - If a guest is removed from event.guests list, it's deleted
  
- **Guest → RSVP**: `orphanRemoval = true`
  - If RSVP is set to null on guest, it's deleted
  
- **RSVP → Attendees**: `orphanRemoval = true`
  - If an attendee is removed from rsvp.attendees list, it's deleted

## Data Integrity Rules

### Foreign Key Constraints
1. **Host.event_id** → NOT NULL (Host must have an event)
2. **Guest.event_id** → NOT NULL (Guest must have an event)
3. **RSVP.guest_id** → UNIQUE, NOT NULL (One RSVP per guest)
4. **Attendee.rsvp_id** → NOT NULL (Attendee must have an RSVP)

### Validation Rules
1. Event must have at least one host
2. Guest maxAttendees must be >= 0
3. RSVP status must be: Pending, Accepted, or Declined
4. Attendee ageGroup must be: Adult or Child
5. RSVP.attendeeCount should match actual number of attendees

## Benefits of This Structure

### 1. Data Consistency
- Cascade operations ensure no orphaned records
- Foreign key constraints maintain referential integrity
- Deletion cascades prevent inconsistent state

### 2. Clear Ownership
- Each entity has a clear parent/owner
- Navigation follows natural hierarchy
- URLs reflect entity relationships

### 3. Simplified Operations
- Deleting an event cleans up all related data
- No need to manually delete children
- Automatic cleanup of orphaned records

### 4. Intuitive Navigation
- Page hierarchy matches data hierarchy
- Breadcrumbs follow entity relationships
- Context is always maintained in URLs

### 5. Access Control
- Security can be applied at aggregate root level
- Child entity access inherits parent permissions
- Easier to implement role-based access

## Migration Notes

### Backward Compatibility
The entities include helper methods for backward compatibility:

```java
// In Host and Guest entities
public Long getEventId() {
    return event != null ? event.getId() : null;
}

public void setEventId(Long eventId) {
    // Kept for compatibility but handled through setEvent()
}
```

### Service Layer Updates Required
Services that currently use `eventId` directly should be updated to use the relationship:

**Before:**
```java
guest.setEventId(eventId);
guestService.save(guest);
```

**After:**
```java
WeddingEvent event = eventRepository.findById(eventId).orElseThrow();
guest.setEvent(event);
guestService.save(guest);
```

## Testing Checklist

- [ ] Create event with hosts and guests
- [ ] Delete event - verify all hosts and guests deleted
- [ ] Delete guest - verify RSVP and attendees deleted
- [ ] Delete RSVP - verify attendees deleted
- [ ] Remove host from event - verify orphan removal
- [ ] Remove guest from event - verify orphan removal
- [ ] Remove attendee from RSVP - verify orphan removal
- [ ] Navigation follows hierarchy in UI
- [ ] URLs reflect entity relationships
- [ ] Foreign key constraints enforced
- [ ] Cascade operations work correctly

## Future Enhancements

1. **Soft Deletes**: Add deleted_at timestamp for archival
2. **Audit Trail**: Track changes to entities
3. **Event Templates**: Reuse event configurations
4. **Guest Groups**: Group related guests together
5. **Attendee Profiles**: More detailed attendee information

---

**Last Updated:** January 1, 2026  
**Status:** ✅ Implemented  
**Version:** 1.0

