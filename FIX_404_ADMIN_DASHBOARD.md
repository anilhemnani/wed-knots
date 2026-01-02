# Fix: 404 Error on /admin/dashboard

## Problem
The `/admin/dashboard` endpoint was returning a 404 error.

## Root Cause
When updating the `AdminWebController.java` to add guest, RSVP, and attendee management endpoints, the original admin dashboard endpoint was accidentally removed.

## Solution
Added back the missing endpoint in `AdminWebController.java`:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/dashboard")
public String adminDashboard(Model model) {
    List<WeddingEvent> events = weddingEventRepository.findAll();
    model.addAttribute("events", events);
    return "admin_dashboard";
}
```

Also added back all other event management endpoints:
- `GET /admin/events/new` - New event form
- `POST /admin/events/new` - Create event
- `GET /admin/events/{id}` - View event
- `GET /admin/events/{id}/edit` - Edit event form
- `POST /admin/events/{id}/edit` - Update event
- `POST /admin/events/{id}/delete` - Delete event
- `GET /admin/events/{id}/hosts` - Manage hosts

## Files Modified
- `/home/anilhemnani/moments-manager/src/main/java/com/momentsmanager/web/AdminWebController.java`

## How to Apply Fix

### Option 1: Use the restart script
```bash
cd /home/anilhemnani/moments-manager
./restart-app.sh
```

### Option 2: Manual restart
```bash
cd /home/anilhemnani/moments-manager

# Stop existing application
pkill -9 -f "java.*moments-manager"

# Rebuild
mvn clean package -DskipTests

# Start
java -jar target/moments-manager-0.0.1-SNAPSHOT.jar
```

## Verification
After restarting the application, you should be able to access:
- ✅ `http://localhost:8080/admin/dashboard` - Admin dashboard with event list
- ✅ `http://localhost:8080/admin/events/new` - Create new event
- ✅ `http://localhost:8080/admin/events/{id}` - View event details
- ✅ All guest, RSVP, and attendee management endpoints

## Complete Endpoint List

### Admin Dashboard & Events
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/events/new` - New event form
- `POST /admin/events/new` - Create event
- `GET /admin/events/{id}` - View event
- `GET /admin/events/{id}/edit` - Edit event
- `POST /admin/events/{id}/edit` - Update event
- `POST /admin/events/{id}/delete` - Delete event

### Hosts
- `GET /admin/events/{id}/hosts` - Manage hosts

### Guests
- `GET /admin/events/{id}/guests` - List guests
- `GET /admin/events/{id}/guests/new` - Add guest form
- `POST /admin/events/{id}/guests/new` - Create guest
- `GET /admin/guests/{guestId}/edit` - Edit guest
- `POST /admin/guests/{guestId}/edit` - Update guest
- `POST /admin/guests/{guestId}/delete` - Delete guest

### RSVPs
- `GET /admin/events/{id}/rsvps` - List RSVPs

### Attendees
- `GET /admin/rsvps/{rsvpId}/attendees` - List attendees
- `GET /admin/rsvps/{rsvpId}/attendees/new` - Add attendee form
- `POST /admin/rsvps/{rsvpId}/attendees/new` - Create attendee
- `GET /admin/attendees/{attendeeId}/edit` - Edit attendee
- `POST /admin/attendees/{attendeeId}/edit` - Update attendee
- `POST /admin/attendees/{attendeeId}/delete` - Delete attendee

## Status
✅ **FIXED** - All endpoints are now properly configured in AdminWebController.java

The application needs to be rebuilt and restarted for the changes to take effect.

