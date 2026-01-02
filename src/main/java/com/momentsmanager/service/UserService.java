package com.momentsmanager.service;

import com.momentsmanager.model.Host;
import com.momentsmanager.model.User;
import com.momentsmanager.repository.HostRepository;
import com.momentsmanager.repository.UserRepository;
import com.momentsmanager.repository.GuestRepository;
import com.momentsmanager.model.Guest;
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
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(lastName, mobile);
        if (guestOpt.isEmpty()) {
            return "GUEST_NOT_FOUND";
        }
        return "SUCCESS";
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
