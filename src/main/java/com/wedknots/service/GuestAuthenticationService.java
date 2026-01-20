package com.wedknots.service;

import com.wedknots.model.Guest;
import com.wedknots.repository.GuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for authenticating guests using phone numbers.
 * Allows guests to login using ANY of their registered phone numbers.
 */
@Service
public class GuestAuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(GuestAuthenticationService.class);

    @Autowired
    private GuestRepository guestRepository;

    /**
     * Validate guest login credentials (family name + any phone number)
     * Returns success if guest found with matching family name and any phone number
     */
    public GuestLoginResult validateGuestLogin(String familyName, String phoneNumber) {
        if (familyName == null || familyName.trim().isEmpty()) {
            return new GuestLoginResult(false, "Family name is required", null);
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new GuestLoginResult(false, "Phone number is required", null);
        }

        // Try new multi-phone system
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameAndAnyPhoneNumber(familyName, phoneNumber);

        // Fallback to old single-phone system for backward compatibility
        if (guestOpt.isEmpty()) {
            guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(familyName, phoneNumber);
        }

        if (guestOpt.isEmpty()) {
            logger.warn("Guest login attempt failed - no guest found with family name: {} and phone: {}",
                       familyName, phoneNumber);
            return new GuestLoginResult(false,
                "Guest not found. Please verify your family name and phone number. " +
                "You can use any of your registered phone numbers to login.", null);
        }

        Guest guest = guestOpt.get();
        logger.info("Guest authentication successful - Family: {}, Guest ID: {}, Phone: {}",
                   familyName, guest.getId(), phoneNumber);
        return new GuestLoginResult(true, "Authentication successful", guest);
    }

    /**
     * Get guest for authentication
     * Accepts any of the guest's phone numbers
     */
    public Optional<Guest> getGuestForAuthentication(String familyName, String phoneNumber) {
        // Try new multi-phone system first
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameAndAnyPhoneNumber(familyName, phoneNumber);

        // Fallback to old system
        if (guestOpt.isEmpty()) {
            guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(familyName, phoneNumber);
        }

        return guestOpt;
    }

    /**
     * Check if a phone number belongs to a guest
     * Searches across all phone numbers of the guest
     */
    public boolean isPhoneNumberRegisteredToGuest(Long guestId, String phoneNumber) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return false;
        }

        Guest guest = guestOpt.get();
        if (guest.getPhoneNumbers() == null || guest.getPhoneNumbers().isEmpty()) {
            // Fallback: check contact_phone
            return guest.getContactPhone() != null && guest.getContactPhone().equals(phoneNumber);
        }

        // Check if phone is in guest's phone numbers
        return guest.getPhoneNumbers().stream()
                .anyMatch(phone -> phone.getPhoneNumber().equals(phoneNumber));
    }

    /**
     * Get all phone numbers registered to a guest
     * Returns both new multi-phone system and backward compatible contact_phone
     */
    public java.util.List<String> getGuestPhoneNumbers(Long guestId) {
        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        Guest guest = guestOpt.get();
        java.util.List<String> phoneNumbers = new java.util.ArrayList<>();

        // Add phones from new system
        if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
            guest.getPhoneNumbers().stream()
                    .map(phone -> phone.getPhoneNumber())
                    .forEach(phoneNumbers::add);
        }

        // Add backward compatibility phone if not already in list
        if (guest.getContactPhone() != null && !phoneNumbers.contains(guest.getContactPhone())) {
            phoneNumbers.add(guest.getContactPhone());
        }

        return phoneNumbers;
    }

    /**
     * Check if guest has any phone numbers registered
     */
    public boolean hasPhoneNumbers(Guest guest) {
        if (guest == null) {
            return false;
        }

        if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
            return true;
        }

        // Check backward compatibility
        return guest.getContactPhone() != null && !guest.getContactPhone().isEmpty();
    }

    /**
     * Get primary phone number for a guest
     * Returns primary from new system or contact_phone for backward compatibility
     */
    public String getPrimaryPhoneNumber(Guest guest) {
        if (guest == null) {
            return null;
        }

        // Try new system
        if (guest.getPhoneNumbers() != null && !guest.getPhoneNumbers().isEmpty()) {
            String primaryPhone = guest.getPhoneNumbers().stream()
                    .filter(phone -> Boolean.TRUE.equals(phone.getIsPrimary()))
                    .map(phone -> phone.getPhoneNumber())
                    .findFirst()
                    .orElse(null);

            if (primaryPhone != null) {
                return primaryPhone;
            }
        }

        // Fallback to old system
        return guest.getContactPhone();
    }

    /**
     * Guest login result wrapper class
     */
    public static class GuestLoginResult {
        private final boolean success;
        private final String message;
        private final Guest guest;

        public GuestLoginResult(boolean success, String message, Guest guest) {
            this.success = success;
            this.message = message;
            this.guest = guest;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Guest getGuest() {
            return guest;
        }
    }
}

