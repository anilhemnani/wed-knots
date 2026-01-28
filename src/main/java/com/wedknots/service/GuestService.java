package com.wedknots.service;

import com.wedknots.model.Guest;
import com.wedknots.model.GuestPhoneNumber;
import com.wedknots.model.RSVP;
import com.wedknots.model.RSVPStatus;
import com.wedknots.repository.GuestPhoneNumberRepository;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.RSVPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GuestService {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RSVPRepository rsvpRepository;

    @Autowired
    private GuestPhoneNumberRepository guestPhoneNumberRepository;

    @Transactional
    public Guest createGuest(Guest guest) {
        // Validate that the initial phone number (if any) doesn't already exist in the event
        if (guest.getEventId() != null && !guest.getPhoneNumbers().isEmpty()) {
            for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
                if (phone.getPhoneNumber() != null && !phone.getPhoneNumber().trim().isEmpty()) {
                    if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneNumberInEvent(
                            phone.getPhoneNumber(), guest.getEventId(), 0L))) {
                        Optional<GuestPhoneNumber> existingPhone = guestPhoneNumberRepository.findPhoneNumberInEvent(
                                phone.getPhoneNumber(), guest.getEventId());
                        if (existingPhone.isPresent()) {
                            Guest existingGuest = existingPhone.get().getGuest();
                            String contactName = (existingGuest.getContactFirstName() != null ? existingGuest.getContactFirstName() : "") +
                                                 " " + (existingGuest.getContactLastName() != null ? existingGuest.getContactLastName() : "");
                            throw new RuntimeException(
                                "Phone number " + phone.getPhoneNumber() + " is already registered for guest '" +
                                contactName.trim() + "' in this wedding event."
                            );
                        }
                        throw new RuntimeException(
                            "Phone number " + phone.getPhoneNumber() + " is already registered to another guest in this event."
                        );
                    }
                }
            }
        }

        // Save the guest first
        Guest savedGuest = guestRepository.save(guest);

        // Create and save RSVP with default "Pending" status
        RSVP rsvp = RSVP.builder()
                .guest(savedGuest)
                .eventId(savedGuest.getEventId())
                .status(RSVPStatus.PENDING)
                .attendeeCount(0)
                .build();

        rsvpRepository.save(rsvp);

        return savedGuest;
    }

    @Transactional
    public Guest updateGuest(Long id, Guest guestDetails) {
        Optional<Guest> guestOpt = guestRepository.findById(id);
        if (guestOpt.isPresent()) {
            Guest guest = guestOpt.get();

            // Validate phone number if it's being set directly (backward compatibility)
            String newContactPhone = guestDetails.getContactPhone();
            if (newContactPhone != null && !newContactPhone.trim().isEmpty()) {
                // Check if this phone number exists for another guest in the same event
                Long eventId = guest.getEventId() != null ? guest.getEventId() : guestDetails.getEventId();
                if (eventId != null) {
                    if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneNumberInEvent(
                            newContactPhone, eventId, id))) {
                        Optional<GuestPhoneNumber> existingPhone = guestPhoneNumberRepository.findPhoneNumberInEvent(
                                newContactPhone, eventId);
                        if (existingPhone.isPresent()) {
                            Guest existingGuest = existingPhone.get().getGuest();
                            String contactName = (existingGuest.getContactFirstName() != null ? existingGuest.getContactFirstName() : "") +
                                                 " " + (existingGuest.getContactLastName() != null ? existingGuest.getContactLastName() : "");
                            throw new RuntimeException(
                                "Phone number " + newContactPhone + " is already registered for guest '" +
                                contactName.trim() + "' in this wedding event."
                            );
                        }
                        throw new RuntimeException(
                            "Phone number " + newContactPhone + " is already registered to another guest in this event."
                        );
                    }
                }
            }

            guest.setFamilyName(guestDetails.getFamilyName());
            guest.setContactFirstName(guestDetails.getContactFirstName());
            guest.setContactLastName(guestDetails.getContactLastName());
            guest.setContactEmail(guestDetails.getContactEmail());
            guest.setPrimaryPhoneNumber(guestDetails.getPrimaryPhoneNumber());
            guest.setSide(guestDetails.getSide());
            guest.setAddressLine1(guestDetails.getAddressLine1());
            guest.setAddressLine2(guestDetails.getAddressLine2());
            guest.setCity(guestDetails.getCity());
            guest.setCountry(guestDetails.getCountry());
            guest.setPostalCode(guestDetails.getPostalCode());
            guest.setMaxAttendees(guestDetails.getMaxAttendees());
            guest.setEventId(guestDetails.getEventId());

            // Update additional phone numbers
            if (guestDetails.getPhoneNumbers() != null && !guestDetails.getPhoneNumbers().isEmpty()) {
                // Clear existing and rebuild from form data
                guest.getPhoneNumbers().clear();
                for (GuestPhoneNumber phone : guestDetails.getPhoneNumbers()) {
                    phone.setGuest(guest);
                    phone.setEventId(guest.getEventId());
                    guest.getPhoneNumbers().add(phone);
                }
            } else {
                // Clear phone numbers if none provided
                guest.getPhoneNumbers().clear();
            }

            return guestRepository.save(guest);
        }
        throw new RuntimeException("Guest not found with id: " + id);
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Optional<Guest> getGuestById(Long id) {
        return guestRepository.findById(id);
    }

    public List<Guest> getGuestsByEventId(Long eventId) {
        return guestRepository.findAll().stream()
                .filter(guest -> guest.getEventId() != null && guest.getEventId().equals(eventId))
                .toList();
    }

    @Transactional
    public void deleteGuest(Long id) {
        guestRepository.deleteById(id);
    }

    public Optional<RSVP> getGuestRSVP(Long guestId) {
        return rsvpRepository.findByGuestId(guestId);
    }

    @Transactional
    public RSVP updateGuestRSVP(Long guestId, RSVPStatus status, int attendeeCount) {
        Optional<RSVP> rsvpOpt = rsvpRepository.findByGuestId(guestId);
        if (rsvpOpt.isPresent()) {
            RSVP rsvp = rsvpOpt.get();
            rsvp.setStatus(status);
            rsvp.setAttendeeCount(attendeeCount);
            return rsvpRepository.save(rsvp);
        }
        throw new RuntimeException("RSVP not found for guest id: " + guestId);
    }

    /**
     * Add a new phone number to a guest
     * Validates that the phone number doesn't already exist for ANY guest in the same wedding event
     */
    @Transactional
    public GuestPhoneNumber addPhoneNumber(Long guestId, String phoneNumber, GuestPhoneNumber.PhoneType phoneType,
                                           String contactFirstName, String contactLastName) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Guest guest = guestOpt.get();

        // Check if phone number already exists for this guest
        if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsByGuestIdAndPhoneNumber(guestId, phoneNumber))) {
            throw new RuntimeException("Phone number already exists for this guest");
        }

        // Check if phone number exists for ANY other guest in the same event
        Long eventId = guest.getEventId();
        if (eventId != null) {
            if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneNumberInEvent(phoneNumber, eventId, guestId))) {
                Optional<GuestPhoneNumber> existingPhone = guestPhoneNumberRepository.findPhoneNumberInEvent(phoneNumber, eventId);
                if (existingPhone.isPresent()) {
                    Guest existingGuest = existingPhone.get().getGuest();
                    String contactName = (existingGuest.getContactFirstName() != null ? existingGuest.getContactFirstName() : "") +
                                        " " + (existingGuest.getContactLastName() != null ? existingGuest.getContactLastName() : "");
                    throw new RuntimeException(
                        "Phone number " + phoneNumber + " is already registered for guest '" +
                        contactName.trim() + "' in this wedding event. " +
                        "Each guest must have a unique phone number."
                    );
                }
                throw new RuntimeException(
                    "Phone number " + phoneNumber + " is already registered to another guest in this wedding event. " +
                    "Each guest must have a unique phone number."
                );
            }
        }

        // Build phone entry (no primary toggling; isPrimary kept for backward compatibility)
        GuestPhoneNumber newPhone = GuestPhoneNumber.builder()
                .guest(guest)
                .eventId(guest.getEventId())
                .phoneNumber(phoneNumber)
                .phoneType(phoneType != null ? phoneType : GuestPhoneNumber.PhoneType.PERSONAL)
                .contactFirstName(contactFirstName)
                .contactLastName(contactLastName)
                .build();

        guest.getPhoneNumbers().add(newPhone);
        return guestPhoneNumberRepository.save(newPhone);
    }

    /**
     * Update/edit a phone number for a guest
     */
    @Transactional
    public GuestPhoneNumber updatePhoneNumber(Long guestId, Long phoneNumberId, String newPhoneNumber,
                                              GuestPhoneNumber.PhoneType phoneType,
                                              String contactFirstName, String contactLastName) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Guest guest = guestOpt.get();
        Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findById(phoneNumberId);

        if (phoneOpt.isEmpty()) {
            throw new RuntimeException("Phone number not found with id: " + phoneNumberId);
        }

        GuestPhoneNumber phone = phoneOpt.get();

        // If phone number is being changed, validate it doesn't exist for another guest
        if (!phone.getPhoneNumber().equals(newPhoneNumber)) {
            // Check if new number already exists for this guest
            if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsByGuestIdAndPhoneNumber(guestId, newPhoneNumber))) {
                throw new RuntimeException("This phone number already exists for this guest");
            }

            // Check if new number exists for ANY other guest in the same event
            Long eventId = guest.getEventId();
            if (eventId != null) {
                if (Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneNumberInEvent(newPhoneNumber, eventId, guestId))) {
                    Optional<GuestPhoneNumber> existingPhone = guestPhoneNumberRepository.findPhoneNumberInEvent(newPhoneNumber, eventId);
                    if (existingPhone.isPresent()) {
                        Guest existingGuest = existingPhone.get().getGuest();
                        String contactName = (existingGuest.getContactFirstName() != null ? existingGuest.getContactFirstName() : "") +
                                            " " + (existingGuest.getContactLastName() != null ? existingGuest.getContactLastName() : "");
                        throw new RuntimeException(
                            "Phone number " + newPhoneNumber + " is already registered for guest '" +
                            contactName.trim() + "' in this wedding event."
                        );
                    }
                }
            }
        }

        // Update phone details
        phone.setPhoneNumber(newPhoneNumber);
        phone.setPhoneType(phoneType != null ? phoneType : GuestPhoneNumber.PhoneType.PERSONAL);
        phone.setContactFirstName(contactFirstName);
        phone.setContactLastName(contactLastName);

        return guestPhoneNumberRepository.save(phone);
    }

    /**
     * Remove a phone number from a guest
     */
    @Transactional
    public void removePhoneNumber(Long guestId, Long phoneNumberId) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Guest guest = guestOpt.get();
        Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findById(phoneNumberId);

        if (phoneOpt.isEmpty()) {
            throw new RuntimeException("Phone number not found with id: " + phoneNumberId);
        }

        GuestPhoneNumber phone = phoneOpt.get();

        // Prevent deletion if it's the only phone number
        if (guest.getPhoneNumbers().size() <= 1) {
            throw new RuntimeException("Cannot delete the only phone number for a guest");
        }

        // If deleting primary, set next phone as primary
        if (Boolean.TRUE.equals(phone.getIsPrimary()) && guest.getPhoneNumbers().size() > 1) {
            for (GuestPhoneNumber guestPhone : guest.getPhoneNumbers()) {
                if (!guestPhone.getId().equals(phoneNumberId)) {
                    guestPhone.setIsPrimary(true);
                    guestPhoneNumberRepository.save(guestPhone);
                    break;
                }
            }
        }

        guest.getPhoneNumbers().remove(phone);
        guestPhoneNumberRepository.deleteById(phoneNumberId);
    }

    /**
     * Set a phone number as primary
     */
    @Transactional
    public void setPrimaryPhoneNumber(Long guestId, Long phoneNumberId) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            throw new RuntimeException("Guest not found with id: " + guestId);
        }

        Guest guest = guestOpt.get();

        // Reset all to non-primary
        for (GuestPhoneNumber phone : guest.getPhoneNumbers()) {
            phone.setIsPrimary(false);
        }

        // Set the selected one as primary
        Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findById(phoneNumberId);
        if (phoneOpt.isPresent()) {
            GuestPhoneNumber phone = phoneOpt.get();
            phone.setIsPrimary(true);
            guestPhoneNumberRepository.save(phone);
        } else {
            throw new RuntimeException("Phone number not found with id: " + phoneNumberId);
        }
    }

    /**
     * Get all phone numbers for a guest
     */
    public List<GuestPhoneNumber> getGuestPhoneNumbers(Long guestId) {
        return guestPhoneNumberRepository.findByGuestIdOrderedByPrimary(guestId);
    }

    /**
     * Get primary phone number for a guest
     */
    public Optional<GuestPhoneNumber> getPrimaryPhoneNumber(Long guestId) {
        return guestPhoneNumberRepository.findPrimaryPhoneNumberByGuestId(guestId);
    }

    /**
     * Get all phone numbers for a specific event (for reporting/auditing)
     */
    public List<GuestPhoneNumber> getAllPhoneNumbersByEvent(Long eventId) {
        return guestPhoneNumberRepository.findAllByEventId(eventId);
    }

    /**
     * Check if a phone number is already used in a wedding event by another guest
     * Useful for validation before adding a phone number
     */
    public boolean isPhoneNumberUsedInEvent(String phoneNumber, Long eventId, Long currentGuestId) {
        return Boolean.TRUE.equals(guestPhoneNumberRepository.existsPhoneNumberInEvent(phoneNumber, eventId, currentGuestId));
    }

    /**
     * Get the guest who already has a specific phone number in an event
     */
    public Optional<Guest> findGuestWithPhoneInEvent(String phoneNumber, Long eventId) {
        Optional<GuestPhoneNumber> phoneOpt = guestPhoneNumberRepository.findPhoneNumberInEvent(phoneNumber, eventId);
        return phoneOpt.map(GuestPhoneNumber::getGuest);
    }
}

