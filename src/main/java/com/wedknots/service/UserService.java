package com.wedknots.service;

import com.wedknots.model.Host;
import com.wedknots.model.User;
import com.wedknots.repository.HostRepository;
import com.wedknots.repository.UserRepository;
import com.wedknots.repository.GuestRepository;
import com.wedknots.model.Guest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private HostRepository hostRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registerUser(User user, String role) {
        // Registration logic stub
    }

    public String validateAdminLogin(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "USER_NOT_FOUND";
        }
        User user = userOpt.get();
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // First time login: don't set password yet, let the modal handle it
            return "SUCCESS_FIRST_TIME";
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "WRONG_PASSWORD";
        }
        return "SUCCESS";
    }

    public String validateGuestLogin(String lastName, String mobile) {
        // Try to find guest by family name and ANY of their phone numbers
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameAndAnyPhoneNumber(lastName, mobile);

        if (guestOpt.isEmpty()) {
            // For backward compatibility, also check the old contact_phone field
            // (in case there are guests without the new phoneNumbers relationship)
            guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(lastName, mobile);
            if (guestOpt.isEmpty()) {
                return "GUEST_NOT_FOUND";
            }
        }

        return "SUCCESS";
    }

    /**
     * Get guest for authentication using family name and any phone number
     * Tries the new multi-phone system first, then falls back to old system
     */
    public Optional<Guest> getGuestForAuthentication(String lastName, String mobile) {
        // Try new multi-phone system first
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameAndAnyPhoneNumber(lastName, mobile);

        // Fallback to old single-phone system for backward compatibility
        if (guestOpt.isEmpty()) {
            guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(lastName, mobile);
        }

        return guestOpt;
    }

    public String validateHostLogin(String email, String password) {
        Optional<Host> hostOpt = hostRepository.findByEmail(email);
        if (hostOpt.isEmpty()) {
            return "HOST_NOT_FOUND";
        }
        Host host = hostOpt.get();
        if (host.getPassword() == null || host.getPassword().isEmpty()) {
            // First time login: don't set password yet, let the modal handle it
            return "SUCCESS_FIRST_TIME";
        }
        if (!passwordEncoder.matches(password, host.getPassword())) {
            return "WRONG_PASSWORD";
        }
        return "SUCCESS";
    }

    public boolean setAdminPassword(String username, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public boolean setHostPassword(String email, String newPassword) {
        Optional<Host> hostOpt = hostRepository.findByEmail(email);
        if (hostOpt.isEmpty()) {
            return false;
        }
        Host host = hostOpt.get();
        host.setPassword(passwordEncoder.encode(newPassword));
        hostRepository.save(host);
        return true;
    }
}
