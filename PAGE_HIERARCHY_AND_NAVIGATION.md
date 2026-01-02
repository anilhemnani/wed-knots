# 📊 Moments Manager - Page Hierarchy & Navigation Diagram

## Date: January 1, 2026

---

## 🏗️ Application Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     MOMENTS MANAGER                              │
│                  Wedding Event Management System                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │   Landing Page    │
                    │   (/)             │
                    └─────────┬─────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
    ┌───▼────┐           ┌────▼────┐          ┌────▼────┐
    │ ADMIN  │           │  HOST   │          │  GUEST  │
    │ Portal │           │ Portal  │          │ Portal  │
    └────────┘           └─────────┘          └─────────┘
```

---

## 🎯 Complete Page Hierarchy

### Level 0: Entry Points

```
/ (Landing Page - index.html)
│
├── /login/admin      → Admin Login
├── /login/host       → Host Login  
└── /login/guest      → Guest Login
```

---

### Level 1: Role-Based Dashboards

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  ADMIN PORTAL    │     │   HOST PORTAL    │     │  GUEST PORTAL    │
│  /admin/dashboard│     │  /host/dashboard │     │ /guest/dashboard │
└──────────────────┘     └──────────────────┘     └──────────────────┘
         │                        │                         │
    (Event Mgmt)             (Event View)            (RSVP Mgmt)
```

---

## 🔐 Authentication Flow

```mermaid
graph TD
    A[Landing Page /] --> B{Select Role}
    B -->|Admin| C[/login/admin]
    B -->|Host| D[/login/host]
    B -->|Guest| E[/login/guest]
    
    C --> F{First Time?}
    F -->|Yes| G[Set Password Modal]
    F -->|No| H[Admin Dashboard]
    G --> H
    
    D --> I{First Time?}
    I -->|Yes| J[Set Password Modal]
    I -->|No| K[Host Dashboard]
    J --> K
    
    E --> L{Guest Found?}
    L -->|Yes| M[Guest Dashboard]
    L -->|No| N[Error: Guest Not Found]
    
    style A fill:#e1f5ff
    style H fill:#d4edda
    style K fill:#d4edda
    style M fill:#d4edda
    style N fill:#f8d7da
```

---

## 📱 ADMIN Portal - Complete Navigation

### Admin Dashboard → Events → Guests → Attendees → Travel Info

```
/admin/dashboard (admin_dashboard.html)
│
├─ Shows: List of all events
│
└─ Events Management
   │
   ├─ /events (event_list.html)
   │  │
   │  ├─ /events/new (event_form.html)
   │  │  └─ POST /events/new → Create Event → Redirect to /events
   │  │
   │  ├─ /events/{id} (event_view.html)
   │  │  │
   │  │  ├─ Tabs: Guests | Hosts
   │  │  │
   │  │  └─ Actions: View, Edit, Delete
   │  │
   │  ├─ /events/{id}/edit (event_form.html)
   │  │  └─ POST /events/{id}/edit → Update → Redirect to /events
   │  │
   │  └─ POST /events/{id}/delete → Delete → Redirect to /events
   │
   ├─ /events/{eventId}/guests (guest_list.html)
   │  │
   │  ├─ /events/{eventId}/guests/new (guest_form.html)
   │  │  └─ POST /events/{eventId}/guests/new → Create Guest → Redirect to guest list
   │  │
   │  ├─ /events/{eventId}/guests/{guestId}/edit (guest_form.html)
   │  │  │
   │  │  ├─ Shows: Guest details + RSVP status
   │  │  │
   │  │  ├─ POST /events/{eventId}/guests/{guestId}/edit → Update → Redirect
   │  │  │
   │  │  └─ Button: "Manage Attendees" → /guests/{guestId}/rsvp/attendees
   │  │
   │  └─ POST /events/{eventId}/guests/{guestId}/delete → Delete → Redirect
   │
   ├─ /events/{eventId}/hosts (host_list.html)
   │  │
   │  ├─ /events/{eventId}/hosts/new (host_form.html)
   │  │  └─ POST → Create Host → Redirect
   │  │
   │  ├─ /events/{eventId}/hosts/{hostId}/edit (host_form.html)
   │  │  └─ POST → Update Host → Redirect
   │  │
   │  └─ POST /events/{eventId}/hosts/{hostId}/delete → Delete → Redirect
   │
   └─ /guests/{guestId}/rsvp/attendees (attendee_list.html)
      │
      ├─ /guests/{guestId}/rsvp/attendees/new (attendee_form.html)
      │  └─ POST → Create Attendee → Redirect to attendee list
      │
      ├─ /guests/{guestId}/rsvp/attendees/{attendeeId}/edit (attendee_form.html)
      │  └─ POST → Update Attendee → Redirect
      │
      ├─ /guests/{guestId}/rsvp/attendees/{attendeeId}/travel-info (travel_info_form.html) ⭐ NEW
      │  │
      │  ├─ Shows: Arrival & Departure details form
      │  │
      │  └─ POST → Save Travel Info → Redirect to attendee list
      │
      └─ POST /guests/{guestId}/rsvp/attendees/{attendeeId}/delete → Delete → Redirect
```

---

## 🏠 HOST Portal - Navigation

```
/host/dashboard (host_dashboard.html)
│
├─ Shows: Events where user is a host
│
└─ Event Management (Read-Only + Guest Management)
   │
   ├─ /events/{id} (event_view.html)
   │  │
   │  └─ View event details
   │
   └─ /events/{eventId}/guests (guest_list.html)
      │
      ├─ Same guest management as Admin
      │
      └─ /guests/{guestId}/rsvp/attendees
         │
         └─ Same attendee & travel info management as Admin
```

---

## 👤 GUEST Portal - Navigation

```
/guest/dashboard (guest_dashboard.html)
│
├─ Shows: Events guest is invited to
│
└─ RSVP Management
   │
   └─ /guests/{guestId}/rsvp/attendees (attendee_list.html)
      │
      ├─ View/Manage own attendees
      │
      ├─ /guests/{guestId}/rsvp/attendees/new
      │  └─ Add family members
      │
      ├─ /guests/{guestId}/rsvp/attendees/{id}/edit
      │  └─ Edit attendee details
      │
      └─ /guests/{guestId}/rsvp/attendees/{attendeeId}/travel-info ⭐ NEW
         │
         └─ Manage travel information for each attendee
```

---

## 🗺️ Visual Navigation Map

### Admin Workflow

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                   │
│  [Admin Login] → [Admin Dashboard]                               │
│                         │                                         │
│                         ▼                                         │
│                  ┌─────────────┐                                 │
│                  │   Events    │                                 │
│                  └──────┬──────┘                                 │
│                         │                                         │
│         ┌───────────────┼───────────────┐                        │
│         ▼               ▼               ▼                        │
│    ┌────────┐     ┌─────────┐     ┌────────┐                   │
│    │ Create │     │  View   │     │  Edit  │                   │
│    │ Event  │     │  Event  │     │ Event  │                   │
│    └────────┘     └────┬────┘     └────────┘                   │
│                        │                                         │
│              ┌─────────┴─────────┐                              │
│              ▼                   ▼                              │
│         ┌─────────┐         ┌────────┐                         │
│         │ Guests  │         │ Hosts  │                         │
│         └────┬────┘         └────────┘                         │
│              │                                                   │
│    ┌─────────┼─────────┐                                       │
│    ▼         ▼         ▼                                       │
│ [Create] [View/Edit] [Delete]                                  │
│              │                                                   │
│              ▼                                                   │
│        ┌──────────┐                                             │
│        │Attendees │                                             │
│        └─────┬────┘                                             │
│              │                                                   │
│    ┌─────────┼─────────┐                                       │
│    ▼         ▼         ▼                                       │
│ [Create] [Edit]  [Travel Info] ⭐                              │
│                       │                                         │
│                       ▼                                         │
│              ┌──────────────────┐                              │
│              │  Arrival Info    │                              │
│              │  Departure Info  │                              │
│              │  Pickup/Drop     │                              │
│              │  Special Needs   │                              │
│              └──────────────────┘                              │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Host Workflow

```
┌──────────────────────────────────────────────────────┐
│                                                       │
│  [Host Login] → [Host Dashboard]                     │
│                       │                               │
│                       ▼                               │
│                ┌────────────┐                        │
│                │ My Events  │                        │
│                └──────┬─────┘                        │
│                       │                               │
│                       ▼                               │
│                ┌────────────┐                        │
│                │   Guests   │                        │
│                └──────┬─────┘                        │
│                       │                               │
│                       ▼                               │
│                ┌────────────┐                        │
│                │ Attendees  │                        │
│                └──────┬─────┘                        │
│                       │                               │
│                       ▼                               │
│              [Travel Info Mgmt] ⭐                   │
│                                                       │
└──────────────────────────────────────────────────────┘
```

### Guest Workflow

```
┌──────────────────────────────────────────────────────┐
│                                                       │
│  [Guest Login] → [Guest Dashboard]                   │
│   (Family Name       │                               │
│    + Mobile)         ▼                               │
│              ┌──────────────┐                        │
│              │   My RSVP    │                        │
│              └───────┬──────┘                        │
│                      │                               │
│                      ▼                               │
│              ┌──────────────┐                        │
│              │  Attendees   │                        │
│              │ (Add Family) │                        │
│              └───────┬──────┘                        │
│                      │                               │
│          ┌───────────┼──────────┐                   │
│          ▼           ▼          ▼                   │
│       [Add]      [Edit]   [Travel Info] ⭐         │
│                                │                     │
│                                ▼                     │
│                    ┌───────────────────┐            │
│                    │ My Travel Details │            │
│                    │ - Arrival         │            │
│                    │ - Departure       │            │
│                    │ - Pickup Needed?  │            │
│                    └───────────────────┘            │
│                                                       │
└──────────────────────────────────────────────────────┘
```

---

## 🔗 URL Structure & Patterns

### Pattern Analysis

```
Authentication:
/                                  → Landing page
/login/admin                       → Admin login
/login/host                        → Host login
/login/guest                       → Guest login
/set-password                      → Set password (admin)
/set-password-host                 → Set password (host)

Dashboards:
/admin/dashboard                   → Admin dashboard
/host/dashboard                    → Host dashboard
/guest/dashboard                   → Guest dashboard

Events (Admin only):
/events                            → List all events
/events/new                        → Create event form
/events/{id}                       → View event
/events/{id}/edit                  → Edit event form
/events/{id}/delete                → Delete event (POST)

Guests (Admin, Host):
/events/{eventId}/guests           → List guests for event
/events/{eventId}/guests/new       → Create guest form
/events/{eventId}/guests/{guestId}/edit    → Edit guest form
/events/{eventId}/guests/{guestId}/delete  → Delete guest (POST)

Hosts (Admin, Host):
/events/{eventId}/hosts            → List hosts for event
/events/{eventId}/hosts/new        → Create host form
/events/{eventId}/hosts/{hostId}/edit      → Edit host form
/events/{eventId}/hosts/{hostId}/delete    → Delete host (POST)

Attendees (Admin, Host, Guest):
/guests/{guestId}/rsvp/attendees                          → List attendees
/guests/{guestId}/rsvp/attendees/new                      → Create attendee
/guests/{guestId}/rsvp/attendees/{attendeeId}/edit        → Edit attendee
/guests/{guestId}/rsvp/attendees/{attendeeId}/delete      → Delete attendee (POST)
/guests/{guestId}/rsvp/attendees/{attendeeId}/travel-info → Manage travel info ⭐ NEW
```

---

## 📊 Entity Relationships & Page Context

```
┌────────────────────────────────────────────────────────────────┐
│                        ENTITY HIERARCHY                         │
└────────────────────────────────────────────────────────────────┘

Event (Top Level)
  │
  ├── Hosts (Many)
  │     │
  │     └── Can manage guests and attendees for their event
  │
  └── Guests (Many)
        │
        ├── Has 1 RSVP (Auto-created)
        │     │
        │     └── Status: Pending, Accepted, Declined
        │
        └── Attendees (Many)
              │
              ├── Name, Mobile, Age Group
              │
              └── Travel Info (One-to-One) ⭐ NEW
                    │
                    ├── Arrival: Mode, DateTime, Flight/Train, Airport/Station
                    ├── Departure: Mode, DateTime, Flight/Train, Airport/Station
                    ├── Pickup/Drop: Boolean flags
                    └── Notes: Special requirements
```

---

## 🎨 Page Template Mapping

### Templates Location: `/src/main/resources/templates/`

```
Authentication Pages:
├── index.html                    → Landing page with 3 buttons (Admin/Host/Guest)
├── login_admin.html              → Admin login (username + password or set password)
├── login_host.html               → Host login (email + password or set password)
└── login_guest.html              → Guest login (family name + mobile, no password)

Dashboard Pages:
├── admin_dashboard.html          → Events list for admin
├── host_dashboard.html           → Events for specific host
└── guest_dashboard.html          → RSVP status for guest

Event Management:
├── event_list.html               → All events (admin)
├── event_form.html               → Create/Edit event
└── event_view.html               → View event details with tabs

Guest Management:
├── guest_list.html               → Guests for an event
└── guest_form.html               → Create/Edit guest (shows RSVP status if editing)

Host Management:
├── host_list.html                → Hosts for an event
└── host_form.html                → Create/Edit host

Attendee Management:
├── attendee_list.html            → Attendees for a guest (shows travel info button)
└── attendee_form.html            → Create/Edit attendee

Travel Info Management: ⭐ NEW
└── travel_info_form.html         → Manage arrival/departure/pickup/special needs

Shared:
└── _bootstrap_head.html          → Common Bootstrap CSS/JS includes
```

---

## 🔐 Access Control Matrix

| Page/URL Pattern | Admin | Host | Guest |
|-----------------|-------|------|-------|
| `/` | ✅ | ✅ | ✅ |
| `/login/*` | ✅ | ✅ | ✅ |
| `/admin/dashboard` | ✅ | ❌ | ❌ |
| `/host/dashboard` | ❌ | ✅ | ❌ |
| `/guest/dashboard` | ❌ | ❌ | ✅ |
| `/events` | ✅ | ❌ | ❌ |
| `/events/new` | ✅ | ❌ | ❌ |
| `/events/{id}` | ✅ | ✅ | ❌ |
| `/events/{id}/edit` | ✅ | ❌ | ❌ |
| `/events/{id}/guests` | ✅ | ✅ | ❌ |
| `/events/{id}/hosts` | ✅ | ✅ | ❌ |
| `/guests/{id}/rsvp/attendees` | ✅ | ✅ | ✅ |
| `/guests/{id}/rsvp/attendees/{id}/travel-info` ⭐ | ✅ | ✅ | ✅ |

---

## 🔄 User Journey Examples

### Journey 1: Admin Creates Event with Guests

```
1. Login → /login/admin
2. Dashboard → /admin/dashboard
3. Click "Add Event" → /events/new
4. Fill form, submit → POST /events/new
5. Redirected → /events (see new event)
6. Click event name → /events/1
7. Click "Guests" tab → /events/1/guests
8. Click "Add Guest" → /events/1/guests/new
9. Fill form, submit → POST /events/1/guests/new
10. Redirected → /events/1/guests (see new guest)
11. Click "Edit" on guest → /events/1/guests/1/edit
12. Click "Manage Attendees" → /guests/1/rsvp/attendees
13. Click "Add Attendee" → /guests/1/rsvp/attendees/new
14. Fill form, submit → POST /guests/1/rsvp/attendees/new
15. Redirected → /guests/1/rsvp/attendees (see new attendee)
16. Click "🛫 Manage" → /guests/1/rsvp/attendees/1/travel-info ⭐
17. Fill travel details → POST /guests/1/rsvp/attendees/1/travel-info
18. Redirected → /guests/1/rsvp/attendees (travel info saved)
```

### Journey 2: Guest Adds Travel Info

```
1. Login → /login/guest (family name + mobile)
2. Dashboard → /guest/dashboard
3. View RSVP status
4. Navigate → /guests/{myId}/rsvp/attendees
5. See family members (attendees)
6. Click "🛫 Manage" for child → /guests/{myId}/rsvp/attendees/{childId}/travel-info ⭐
7. Select "Flight" mode
8. Enter flight number, airport, date/time
9. Check "Needs Pickup"
10. Add note: "Traveling with wheelchair"
11. Submit → POST
12. Redirected → /guests/{myId}/rsvp/attendees
13. See updated attendee with travel info saved
```

### Journey 3: Host Coordinates Pickups

```
1. Login → /login/host (email + password)
2. Dashboard → /host/dashboard
3. Click event → /events/1
4. Click "Guests" tab → /events/1/guests
5. Browse guests
6. Click "Edit" on guest → /events/1/guests/3/edit
7. Click "Manage Attendees" → /guests/3/rsvp/attendees
8. Review attendee list
9. Click "🛫 Manage" → /guests/3/rsvp/attendees/5/travel-info ⭐
10. View arrival details
11. See "Needs Pickup: ✅"
12. Note arrival time and airport
13. Use info to coordinate pickup schedule
```

---

## 📱 Breadcrumb Navigation

Each page shows breadcrumb trail:

```
Landing Page:
Home

Admin Dashboard:
Home → Admin Dashboard

Event List:
Home → Admin Dashboard → Events

Event View:
Home → Admin Dashboard → Events → {Event Name}

Guest List:
Home → Admin Dashboard → Events → {Event Name} → Guests

Guest Edit:
Home → Admin Dashboard → Events → {Event Name} → Guests → Edit {Guest Name}

Attendee List:
Home → Admin Dashboard → Events → {Event Name} → Guests → {Guest Name} → Attendees

Attendee Edit:
Home → ... → Attendees → Edit {Attendee Name}

Travel Info: ⭐ NEW
Home → ... → Attendees → {Attendee Name} → Travel Information
```

---

## 🎯 Navigation Buttons & Actions

### Common Navigation Patterns

```
List Pages:
┌─────────────────────────────────────┐
│ [+ Add New]           [Back to ...] │
│                                      │
│ Table with:                          │
│   - View icon                        │
│   - Edit icon                        │
│   - Delete icon (with confirmation)  │
└─────────────────────────────────────┘

Form Pages:
┌─────────────────────────────────────┐
│ Form Fields                          │
│                                      │
│ [Cancel]  [Save]                    │
└─────────────────────────────────────┘

Travel Info Form: ⭐
┌─────────────────────────────────────┐
│ Context: Event > Guest > Attendee    │
│                                      │
│ Arrival Section:                     │
│   - Mode dropdown (triggers fields)  │
│   - Conditional fields               │
│                                      │
│ Departure Section:                   │
│   - Mode dropdown                    │
│   - Conditional fields               │
│                                      │
│ Requirements:                        │
│   - Checkboxes                       │
│   - Text areas                       │
│                                      │
│ [Cancel]  [Save Travel Information] │
└─────────────────────────────────────┘
```

---

## 🔄 HTTP Methods Used

```
GET Requests (View/Display):
- All list pages
- All view pages
- All form pages (empty or pre-filled)

POST Requests (Actions):
- Create operations (/new endpoints)
- Update operations (/{id}/edit endpoints)
- Delete operations (/{id}/delete endpoints)
- Login/authentication
- Password setting
```

---

## 📊 Page Count Summary

```
Total Pages: 26

Authentication: 4
├── index.html
├── login_admin.html
├── login_host.html
└── login_guest.html

Dashboards: 3
├── admin_dashboard.html
├── host_dashboard.html
└── guest_dashboard.html

Events: 3
├── event_list.html
├── event_form.html
└── event_view.html

Guests: 2
├── guest_list.html
└── guest_form.html

Hosts: 2
├── host_list.html
└── host_form.html

Attendees: 2
├── attendee_list.html
└── attendee_form.html

Travel Info: 1 ⭐ NEW
└── travel_info_form.html

Shared: 1
└── _bootstrap_head.html

Other: 8 (RSVP-related, deprecated, or utility pages)
```

---

## 🎨 UI Component Hierarchy

```
Every Page Structure:
┌────────────────────────────────────────┐
│  Navigation Bar                        │
│  ├── App Name: "Moments Manager"      │
│  ├── Links (role-dependent)           │
│  └── Logout                            │
├────────────────────────────────────────┤
│  Page Content                          │
│  ├── Page Title                        │
│  ├── Breadcrumb (optional)            │
│  ├── Action Buttons                    │
│  ├── Main Content                      │
│  │   ├── Tables (list pages)          │
│  │   ├── Forms (edit pages)           │
│  │   └── Details (view pages)         │
│  └── Footer Actions                    │
└────────────────────────────────────────┘
```

---

## 🚀 Quick Reference: Find Any Page

**Want to manage travel for an attendee?**
```
Admin/Host: Dashboard → Events → Guests → Edit Guest → Manage Attendees → 🛫 Manage
Guest: Dashboard → Manage Attendees → 🛫 Manage
```

**Want to create an event?**
```
Admin: Dashboard → Events → + Add Event
```

**Want to add a guest?**
```
Admin/Host: Dashboard → Events → Select Event → Guests → + Add Guest
```

**Want to view RSVP status?**
```
Admin/Host: Dashboard → Events → Guests → See RSVP column
Guest: Dashboard → See own RSVP status
```

---

**Document Version:** 1.0  
**Last Updated:** January 1, 2026  
**Includes:** Travel Info Management Feature ⭐

