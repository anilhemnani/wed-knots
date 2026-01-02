# Travel Info Feature - Quick Reference

## 🎯 What Was Added

A comprehensive **Travel Information Management** system that allows tracking arrival and departure details for each attendee.

## 📋 Files Created

### Backend
1. **TravelInfo.java** - Entity model for travel information
2. **TravelInfoRepository.java** - Data access layer
3. **TravelInfoService.java** - Business logic layer

### Frontend
4. **travel_info_form.html** - User interface for managing travel info

### Database
5. **Changeset #12** in `db.changelog-master.xml` - Creates `travel_info_tbl`

### Documentation
6. **TRAVEL_INFO_FEATURE_COMPLETE.md** - Complete feature documentation
7. **build-and-restart-travel-info.sh** - Build and deployment script

## 📦 Files Modified

1. **Attendee.java** - Added `TravelInfo` relationship
2. **AttendeeWebController.java** - Added travel info endpoints
3. **attendee_list.html** - Added "Manage Travel" button

## 🚀 How to Deploy

### Option 1: Use the Build Script
```bash
cd /home/anilhemnani/moments-manager
./build-and-restart-travel-info.sh
```

### Option 2: Manual Steps
```bash
cd /home/anilhemnani/moments-manager

# Stop existing app
kill $(cat app.pid)

# Build
./mvnw clean package -DskipTests

# Start
nohup java -jar target/moments-manager-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
echo $! > app.pid
```

## 🧭 How to Use

### For Admin/Host
1. Navigate to **Events** → Select Event
2. Click **Guests** tab
3. Click **Edit** on a guest
4. Click **Manage Attendees**
5. For each attendee, click **🛫 Manage** (Travel Info button)
6. Fill in travel details and save

### For Guests
1. Login with family name + mobile number
2. View your RSVP
3. Click **Manage Attendees**
4. For each attendee, click **🛫 Manage**
5. Enter travel information

## 📊 Features

### Arrival Information
- ✈️ Flight details (number, airport, time)
- 🚂 Train details (number, station, time)
- 🚗 Car/Bus/Other options
- 📅 Date and time picker

### Departure Information
- Same options as arrival
- Independent mode selection

### Additional Options
- ☑️ Needs Pickup checkbox
- ☑️ Needs Drop checkbox
- 📝 Special Requirements (wheelchair, child seat, etc.)
- 📋 Additional Notes

## 🔍 Testing Checklist

After deployment, test these scenarios:

### Basic Functionality
- [ ] Open travel info form for an attendee
- [ ] Select "Flight" mode and see flight fields appear
- [ ] Select "Train" mode and see train fields appear
- [ ] Fill in arrival details
- [ ] Fill in departure details
- [ ] Check "Needs Pickup"
- [ ] Add special requirements
- [ ] Save and verify data persists
- [ ] Edit existing travel info
- [ ] Verify changes are saved

### Navigation
- [ ] Access from attendee list
- [ ] Cancel button returns to attendee list
- [ ] Save redirects to attendee list
- [ ] Breadcrumb shows: Event → Guest → Attendee

### Data Integrity
- [ ] Delete attendee removes travel info
- [ ] Edit attendee preserves travel info
- [ ] Special characters in notes work
- [ ] Empty form submission works
- [ ] Very long text in notes field

### Access Control
- [ ] Admin can access all travel info
- [ ] Host can access their event's travel info
- [ ] Guest can only access their own attendees

## 🛠 Database Changes

### New Table: travel_info_tbl

```sql
CREATE TABLE travel_info_tbl (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendee_id BIGINT UNIQUE NOT NULL,
    arrival_mode VARCHAR(50),
    arrival_date_time VARCHAR(255),
    arrival_flight_number VARCHAR(50),
    arrival_train_number VARCHAR(50),
    arrival_airport VARCHAR(255),
    arrival_station VARCHAR(255),
    departure_mode VARCHAR(50),
    departure_date_time VARCHAR(255),
    departure_flight_number VARCHAR(50),
    departure_train_number VARCHAR(50),
    departure_airport VARCHAR(255),
    departure_station VARCHAR(255),
    needs_pickup BOOLEAN DEFAULT FALSE,
    needs_drop BOOLEAN DEFAULT FALSE,
    special_requirements VARCHAR(500),
    notes VARCHAR(1000),
    FOREIGN KEY (attendee_id) REFERENCES attendee_tbl(id) ON DELETE CASCADE
);
```

**Migration:** Handled automatically by Liquibase on startup

## 🔗 New API Endpoints

### GET `/guests/{guestId}/rsvp/attendees/{attendeeId}/travel-info`
- **Purpose:** Display travel info form
- **Security:** ADMIN, HOST, GUEST roles
- **Returns:** travel_info_form.html with data

### POST `/guests/{guestId}/rsvp/attendees/{attendeeId}/travel-info`
- **Purpose:** Save travel information
- **Security:** ADMIN, HOST, GUEST roles
- **Action:** Create or update travel info
- **Redirect:** Back to attendee list

## 📱 UI Components

### Attendee List Enhancement
- Added "Travel Info" column
- **🛫 Manage** button for each attendee
- Consistent with existing UI theme

### Travel Info Form
- **Sections:**
  1. Context (Event, Guest, Attendee names)
  2. Arrival Information
  3. Departure Information
  4. Additional Requirements
- **Dynamic Fields:** Show/hide based on mode selection
- **Responsive:** Works on mobile and desktop
- **Icons:** Clear visual indicators

## 🐛 Troubleshooting

### Build Issues
```bash
# Clean and retry
./mvnw clean
./mvnw package -DskipTests
```

### Port Already in Use
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### Database Not Created
```bash
# Check logs for Liquibase errors
tail -100 app.log | grep -i liquibase
```

### Form Not Showing
- Clear browser cache
- Check browser console for errors
- Verify file is in `target/classes/templates/`

## 📈 Future Enhancements

Potential additions (not yet implemented):
- Email notifications for travel plans
- Bulk import from CSV
- Travel reports by date/airport
- Real-time flight status
- Integration with maps
- Document attachments (tickets)

## 📞 Support

### Check Logs
```bash
tail -f /home/anilhemnani/moments-manager/app.log
```

### Verify Table Created
```bash
# Connect to H2 console: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:file:./momentsmanager
# Check if travel_info_tbl exists
```

### Verify Files Compiled
```bash
ls -la target/classes/com/momentsmanager/model/TravelInfo.class
ls -la target/classes/templates/travel_info_form.html
```

## ✅ Success Indicators

After deployment, you should see:
1. **Database:** `travel_info_tbl` created in database
2. **UI:** "Travel Info" column in attendee list
3. **UI:** "🛫 Manage" button for each attendee
4. **Form:** Travel info form opens when clicking Manage
5. **Data:** Travel info saves and persists
6. **Logs:** No errors related to TravelInfo

## 📝 Notes

- **Zero Downtime:** Feature adds new functionality without breaking existing
- **Backward Compatible:** Existing attendees have no travel info initially
- **Optional:** All travel fields are optional
- **Cascading:** Deleting attendee automatically removes travel info
- **Security:** Respects existing role-based access control

---

**Created:** January 1, 2026  
**Status:** Ready for deployment  
**Next Step:** Run `./build-and-restart-travel-info.sh`

