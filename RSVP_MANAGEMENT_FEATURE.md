# RSVP Management Feature Implementation

## Overview
Added RSVP status display and attendee management functionality to the guest edit page. When editing a guest, users can now see the RSVP status and directly access the attendee management interface.

## Changes Made

### 1. GuestWebController.java
**File:** `src/main/java/com/momentsmanager/web/GuestWebController.java`

#### Added Import
```java
import com.momentsmanager.model.RSVP;
import com.momentsmanager.repository.RSVPRepository;
```

#### Added Dependency Injection
```java
@Autowired
private RSVPRepository rsvpRepository;
```

#### Enhanced editGuest Method
The `editGuest` method now retrieves the RSVP information for the guest and includes it in the model:

```java
// Get RSVP for this guest
Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);

model.addAttribute("event", eventOpt.get());
model.addAttribute("guest", guest);
model.addAttribute("rsvp", rsvpOpt.orElse(null));
```

### 2. guest_form.html
**File:** `src/main/resources/templates/guest_form.html` and `target/classes/templates/guest_form.html`

#### Added RSVP Status Section
Inserted a new section between the card header and the form that displays:
- Current RSVP status with color-coded badge:
  - Green (Accepted)
  - Red (Declined)
  - Yellow (Pending)
- Number of attendees (when status is "Accepted")
- "Manage Attendees" button that navigates to the attendee management page

```html
<!-- RSVP Status Section - Only shown when editing existing guest -->
<div th:if="${guest.id != null && rsvp != null}" class="alert alert-info mb-4" role="alert">
    <div class="d-flex justify-content-between align-items-center">
        <div>
            <strong><i class="bi bi-card-checklist"></i> RSVP Status:</strong>
            <span th:text="${rsvp.status}" class="badge" 
                  th:classappend="${rsvp.status == 'Accepted' ? 'bg-success' : (rsvp.status == 'Declined' ? 'bg-danger' : 'bg-warning text-dark')}"></span>
            <span th:if="${rsvp.status == 'Accepted'}" class="ms-2">
                (<span th:text="${rsvp.attendeeCount}"></span> attendees)
            </span>
        </div>
        <div class="btn-group" role="group">
            <a th:href="@{/guests/{id}/rsvp/attendees(id=${guest.id})}" 
               class="btn btn-sm btn-primary" title="Manage Attendees">
                <i class="bi bi-people-fill"></i> Manage Attendees
            </a>
        </div>
    </div>
</div>
```

### 3. attendee_list.html
**File:** `src/main/resources/templates/attendee_list.html` and `target/classes/templates/attendee_list.html`

#### Updated Navigation
Changed the "Back to RSVP" button to "Back to Guest" with the correct navigation path:

**Before:**
```html
<a th:href="@{/guests/{id}/rsvp(id=${guest.id})}" class="btn btn-secondary">
    <i class="bi bi-arrow-left"></i> Back to RSVP
</a>
```

**After:**
```html
<a th:href="@{/events/{eventId}/guests/{guestId}/edit(eventId=${event.id},guestId=${guest.id})}" class="btn btn-secondary">
    <i class="bi bi-arrow-left"></i> Back to Guest
</a>
```

## Features

### RSVP Status Display
- **Visibility:** Only shown when editing an existing guest (not during creation)
- **Status Badge Colors:**
  - **Accepted** - Green badge with attendee count
  - **Declined** - Red badge
  - **Pending** - Yellow badge (default)

### Manage Attendees Button
- **Purpose:** Direct access to attendee management from guest edit page
- **Navigation:** Links to `/guests/{guestId}/rsvp/attendees`
- **Icon:** Bootstrap Icons people-fill
- **Style:** Primary button (blue)

## User Flow

1. **Navigate to Guest Edit Page**
   - Admin/Host goes to Events → Select Event → View Guests → Edit Guest

2. **View RSVP Status**
   - If guest has an RSVP record, status is displayed with color-coded badge
   - For accepted RSVPs, attendee count is shown

3. **Manage Attendees**
   - Click "Manage Attendees" button
   - Redirects to attendee list page for that guest
   - Can add, edit, or delete attendees
   - Click "Back to Guest" to return to guest edit page

4. **Edit Guest Information**
   - Update guest details as needed
   - Save changes and return to guest list

## Technical Details

### Security
- All endpoints require `ADMIN` or `HOST` role via `@PreAuthorize` annotation
- RSVP information only visible to authorized users

### Data Flow
1. Controller retrieves guest by ID
2. Controller queries RSVP repository for guest's RSVP
3. Both guest and RSVP objects passed to template
4. Template conditionally displays RSVP section if data exists

### Conditional Rendering
The RSVP section uses Thymeleaf conditional rendering:
```html
th:if="${guest.id != null && rsvp != null}"
```
This ensures:
- Section only appears for existing guests (guest.id is not null)
- RSVP data exists for the guest

## Benefits

1. **Centralized Management:** Guests and their RSVPs managed from single page
2. **Clear Status Visibility:** Color-coded badges make status immediately apparent
3. **Quick Navigation:** One-click access to attendee management
4. **Improved UX:** Streamlined workflow for event coordinators
5. **Context Awareness:** RSVP status shown while editing guest details

## Testing Checklist

- [ ] RSVP section hidden when creating new guest
- [ ] RSVP section hidden when guest has no RSVP record
- [ ] RSVP section displays correctly for guest with RSVP
- [ ] Status badge shows correct color for each status type
- [ ] Attendee count displays only for "Accepted" status
- [ ] "Manage Attendees" button navigates to correct page
- [ ] Attendee list page displays correctly
- [ ] "Back to Guest" button returns to guest edit page
- [ ] Can add/edit/delete attendees from attendee list
- [ ] Changes persist after navigation

## Future Enhancements

1. **Quick RSVP Actions:** Add Accept/Decline buttons directly on guest form
2. **Inline Attendee Preview:** Show attendee names in RSVP section
3. **RSVP History:** Track status changes over time
4. **Email Notifications:** Notify guest when RSVP is updated
5. **Bulk Operations:** Accept/decline multiple RSVPs at once

## Related Files

- `GuestWebController.java` - Controller handling guest operations
- `RSVPRepository.java` - Repository for RSVP data access
- `RSVP.java` - RSVP entity model
- `guest_form.html` - Guest edit form template
- `attendee_list.html` - Attendee management template
- `AttendeeWebController.java` - Controller for attendee operations

## Date Implemented
January 1, 2026

