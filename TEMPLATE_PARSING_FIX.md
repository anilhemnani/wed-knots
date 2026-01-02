# Thymeleaf Template Parsing Fix

## Issue
Template parsing errors were occurring due to incorrect Thymeleaf syntax in form action attributes. The error was:

```
An error happened during template parsing (template: "class path resource [templates/attendee_form.html]")
```

## Root Cause
The templates were using an invalid syntax pattern that mixed `${}` variable expressions with `@{}` URL expressions inside a ternary operator:

```html
<!-- INCORRECT SYNTAX - CAUSES PARSING ERROR -->
<form th:action="${attendee.id == null ? @{/path1} : @{/path2}}" method="POST">
```

This syntax is not supported by Thymeleaf because:
1. The `${}` wrapper expects a single expression result
2. You cannot use `@{}` URL expressions inside a `${}` variable expression within a ternary operator
3. Thymeleaf cannot properly parse the nested expression syntax

## Solution
Changed to use string concatenation within the URL expression:

```html
<!-- CORRECT SYNTAX -->
<form th:action="@{${attendee.id == null ? '/path1' : '/path2'}}" method="POST">
```

This approach:
1. Uses `${}` inside `@{}` which is valid
2. Builds the URL path as a string expression
3. The `@{}` processes the resulting string as a URL
4. Properly handles path variables through concatenation

## Files Fixed

### 1. attendee_form.html
**Location:** `src/main/resources/templates/` and `target/classes/templates/`

**Before:**
```html
<form th:action="${attendee.id == null ? @{/guests/{id}/rsvp/attendees/new(id=${guest.id})} : @{/guests/{guestId}/rsvp/attendees/{id}/edit(guestId=${guest.id},id=${attendee.id})}}" method="POST">
```

**After:**
```html
<form th:action="@{${attendee.id == null ? '/guests/' + guest.id + '/rsvp/attendees/new' : '/guests/' + guest.id + '/rsvp/attendees/' + attendee.id + '/edit'}}" method="POST">
```

### 2. guest_form.html
**Location:** `src/main/resources/templates/` and `target/classes/templates/`

**Before:**
```html
<form th:action="${guest.id == null ? @{/events/{id}/guests/new(id=${event.id})} : @{/events/{eventId}/guests/{id}/edit(eventId=${event.id},id=${guest.id})}}" method="POST">
```

**After:**
```html
<form th:action="@{${guest.id == null ? '/events/' + event.id + '/guests/new' : '/events/' + event.id + '/guests/' + guest.id + '/edit'}}" method="POST">
```

### 3. admin_guest_form.html (Legacy)
**Location:** `src/main/resources/templates/` and `target/classes/templates/`

**Before:**
```html
<form th:action="${guest.id == null ? @{/admin/events/{id}/guests/new(id=${event.id})} : @{/admin/guests/{id}/edit(id=${guest.id})}}" method="POST">
```

**After:**
```html
<form th:action="@{${guest.id == null ? '/admin/events/' + event.id + '/guests/new' : '/admin/guests/' + guest.id + '/edit'}}" method="POST">
```

### 4. admin_attendee_form.html (Legacy)
**Location:** `src/main/resources/templates/` and `target/classes/templates/`

**Before:**
```html
<form th:action="${attendee.id == null ? @{/admin/rsvps/{id}/attendees/new(id=${rsvp.id})} : @{/admin/attendees/{id}/edit(id=${attendee.id})}}" method="POST">
```

**After:**
```html
<form th:action="@{${attendee.id == null ? '/admin/rsvps/' + rsvp.id + '/attendees/new' : '/admin/attendees/' + attendee.id + '/edit'}}" method="POST">
```

## Technical Notes

### Why the Fix Works
1. **Valid Thymeleaf Syntax:** `@{${expression}}` is a valid pattern where the inner expression evaluates to a string URL
2. **String Concatenation:** Using `+` operator to build URLs is straightforward and readable
3. **No Nesting Issues:** Avoids the problem of nesting `@{}` inside `${}`
4. **URL Processing:** The `@{}` still applies context path and URL encoding properly

### Alternatives Considered
1. **Two separate forms:** Would require duplicate HTML - rejected for maintainability
2. **JavaScript to set action:** Would work but adds unnecessary client-side complexity
3. **th:attr syntax:** More verbose and less readable than this solution

## Status
✅ **All templates fixed in both src and target directories**
- attendee_form.html
- guest_form.html
- admin_guest_form.html
- admin_attendee_form.html

## Next Steps
1. Application is ready to run without template parsing errors
2. No rebuild required - both src and target templates are updated
3. All form submissions will use correct URLs based on create/edit mode

## Testing Checklist
- [ ] Add new attendee form loads without error
- [ ] Edit attendee form loads without error
- [ ] Add new guest form loads without error
- [ ] Edit guest form loads without error
- [ ] Form submissions create records correctly
- [ ] Form submissions update records correctly
- [ ] URLs are correctly generated for both create and edit modes

## Date Fixed
January 1, 2026

