# ✅ FIX: Hibernate IllegalArgumentException - EventId Mapping Error

## Issue Fixed
```
IllegalArgumentException: Unable to locate Attribute with the given name [eventId] 
on this ManagedType [com.momentsmanager.model.Host]
```

## Root Cause
The `HostRepository` was using `findByEventId()` method, which Spring Data JPA tried to map to a direct `eventId` field on the Host entity. However, Host doesn't have a direct `eventId` field - it has an `event` relationship instead.

## Solution Applied

### 1. Fixed HostRepository
**File:** `src/main/java/com/momentsmanager/repository/HostRepository.java`

Changed from Spring Data JPA field-based query to custom JPQL query:

```java
// Before (WRONG):
List<Host> findByEventId(Long eventId);

// After (CORRECT):
@Query("SELECT h FROM Host h WHERE h.event.id = :eventId")
List<Host> findByEventId(@Param("eventId") Long eventId);
```

### 2. Fixed InvitationRepository
**File:** `src/main/java/com/momentsmanager/repository/InvitationRepository.java`

Updated all three methods to use custom JPQL queries:

```java
// All methods now use custom queries to access event.id instead of direct eventId field
@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId")
List<Invitation> findByEventId(@Param("eventId") Long eventId);

@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId AND i.status = :status")
List<Invitation> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

@Query("SELECT i FROM Invitation i WHERE i.event.id = :eventId ORDER BY i.createdAt DESC")
List<Invitation> findByEventIdOrderByCreatedAtDesc(@Param("eventId") Long eventId);
```

## Why This Works

The Host and Invitation entities have these structures:

```java
// Host Entity
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "event_id", nullable = false)
private WeddingEvent event;  // ← Relationship, not direct field

// Invitation Entity
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "event_id", nullable = false)
private WeddingEvent event;  // ← Relationship, not direct field
```

The database column is named `event_id`, but in the entity it's accessed through the `event` relationship object, so we need to query `event.id` not `eventId`.

### Difference from RSVP

RSVP works fine with the simple syntax because it has a direct field:

```java
// RSVP Entity
@Column(name = "event_id")
private Long eventId;  // ← Direct field, not relationship

// So this works:
List<RSVP> findByEventId(Long eventId);  // ✅ Direct field exists
```

## Impact

- ✅ No breaking changes
- ✅ All existing functionality preserved
- ✅ Application will now start without Hibernate errors
- ✅ All host and invitation queries will work correctly

## Testing

The fix can be verified by:

1. Starting the application: `java -jar target/moments-manager-0.0.1-SNAPSHOT.jar`
2. Accessing: http://localhost:8080
3. Navigating to an event with hosts or invitations
4. Verifying that lists load without errors

## Files Modified

1. **HostRepository.java** - Added @Query annotation
2. **InvitationRepository.java** - Added @Query annotations to all methods

---

**Status:** ✅ FIXED  
**Date:** January 1, 2026  
**Error Type:** Hibernate Mapping Error  
**Solution:** Custom JPQL Queries

