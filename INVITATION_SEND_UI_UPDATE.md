# Invitation Send UI Update - List View

## Update Summary
Updated the Send Invitation page to display guests as a table/list instead of card-based layout for better scalability and easier scanning.

## Date
January 2, 2026

## Changes Made

### 1. Layout Change: Cards to Table
**Before:** Guests were displayed as cards in a responsive grid (3-4 cards per row)
**After:** Guests are displayed in a table format with clear columns

### 2. Table Structure
The new table includes the following columns:
- **Checkbox Column** - With "Select All" checkbox in header
- **Family Name** - Primary identifier (bold)
- **Contact Person** - Contact person's name
- **Phone** - Phone number with icon
- **Email** - Contact email (muted text)
- **Side** - Badge showing Bride/Groom/Both
- **Status** - Visual badge showing Sent/Pending status

### 3. Enhanced Features

#### Select All Checkbox
- Added a checkbox in the table header to select/deselect all guests at once
- Shows indeterminate state when some (but not all) guests are selected
- Automatically updates based on individual checkbox changes
- Disabled guests (already sent) are excluded from "Select All"

#### Visual States
- **Hover Effect**: Subtle background color change on row hover
- **Selected State**: Blue background for selected rows
- **Already Sent**: Greyed out with strikethrough text for guests who already received the invitation

#### Improved UX
- More compact display allows viewing more guests at once
- Easier to scan through guest information
- Clear visual distinction between sent and pending invitations
- Better mobile responsiveness with table-responsive wrapper

### 4. CSS Updates
```css
.guest-row {
    transition: background-color 0.2s ease;
}
.guest-row:hover {
    background-color: #f8f9fa;
}
.guest-row.selected {
    background-color: #e7f3ff;
}
.guest-row.already-sent {
    opacity: 0.7;
    background-color: #f8f9fa;
}
.guest-row.already-sent td {
    text-decoration: line-through;
}
```

### 5. JavaScript Enhancements

#### New Functions
- `updateSelectAllCheckbox()` - Updates the header checkbox state (checked/unchecked/indeterminate)
- `toggleSelectAll(checkbox)` - Handles select all/none from header checkbox

#### Updated Functions
- `updateSelectedCount()` - Now updates row styling instead of card styling
- All existing functions (selectAll, selectNone, selectUnsent) continue to work

### 6. Benefits

1. **Better Scalability**: Can display many more guests on screen at once
2. **Easier Scanning**: Column-based layout makes it easier to find specific information
3. **Clearer Status**: Status column makes it immediately obvious who has/hasn't received invitation
4. **More Professional**: Table format is more standard for data management interfaces
5. **Better Performance**: Lighter DOM structure compared to multiple nested card divs
6. **Improved Accessibility**: Table structure is better for screen readers

## Files Modified
- `/home/anilhemnani/moments-manager/src/main/resources/templates/invitation_send.html`

## Testing Checklist
- [ ] Guest list displays correctly in table format
- [ ] Checkboxes work for individual guests
- [ ] "Select All" checkbox in header works
- [ ] Indeterminate state shows when some guests selected
- [ ] Already-sent guests are disabled and styled differently
- [ ] Quick action buttons (Select All, Deselect All, Select Unsent) work
- [ ] Selected count updates correctly
- [ ] Send button enables/disables based on selection
- [ ] Row hover effects work
- [ ] Selected rows highlight in blue
- [ ] Filter by side still works
- [ ] Mobile responsiveness maintained
- [ ] Icons display correctly (Bootstrap Icons)

## Compatibility
- Works with existing backend logic (no changes needed)
- Maintains all existing functionality
- Backward compatible with current data structure
- No database changes required

## Future Enhancements (Optional)
- Add sorting capabilities to table columns
- Add search/filter within the table
- Add pagination for very large guest lists
- Add bulk actions dropdown
- Export guest list to CSV
- Show RSVP status in the table

