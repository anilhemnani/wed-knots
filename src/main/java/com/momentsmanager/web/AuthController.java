package com.momentsmanager.web;

import com.momentsmanager.model.Host;
import com.momentsmanager.model.User;
import com.momentsmanager.model.Guest;
import com.momentsmanager.repository.HostRepository;
import com.momentsmanager.service.UserService;
import com.momentsmanager.repository.UserRepository;
import com.momentsmanager.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private HostRepository hostRepository;
    @Autowired
    private com.momentsmanager.repository.WeddingEventRepository weddingEventRepository;

    @GetMapping("/login")
    public String login() {
        logger.info("Login page accessed");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        String selectedRole = "ROLE_GUEST";
        if (user.getRole() != null) {
            switch (user.getRole().toUpperCase()) {
                case "ADMIN": selectedRole = "ROLE_ADMIN"; break;
                case "HOST": selectedRole = "ROLE_HOST"; break;
                case "GUEST": selectedRole = "ROLE_GUEST"; break;
            }
        }
        try {
            userService.registerUser(user, selectedRole);
            logger.info("User registered successfully: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("User registration failed for {}: {}", user.getUsername(), e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/login/admin")
    public String adminLogin(Model model) {
        model.addAttribute("user", new User());
        return "login_admin";
    }

    @PostMapping("/login/admin")
    public String adminLoginPost(@ModelAttribute User user, Model model, HttpServletRequest request) {
        String status = userService.validateAdminLogin(user.getUsername(), user.getPassword());
        if ("USER_NOT_FOUND".equals(status)) {
            model.addAttribute("loginError", "Admin username does not exist.");
            return "login_admin";
        } else if ("WRONG_PASSWORD".equals(status)) {
            model.addAttribute("loginError", "Incorrect password.");
            return "login_admin";
        } else if ("SUCCESS_FIRST_TIME".equals(status)) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("firstTime", true);
            return "login_admin";
        }

        // Authenticate the user
        Optional<User> userOpt = userRepository.findByUsername(user.getUsername());
        if (userOpt.isPresent()) {
            User authenticatedUser = userOpt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authenticatedUser.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Save authentication to session
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            logger.info("Admin user {} authenticated successfully", authenticatedUser.getUsername());
        }

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/login/host")
    public String hostLogin(Model model) {
        model.addAttribute("user", new User());
        return "login_host";
    }

    @PostMapping("/login/host")
    public String hostLoginPost(@ModelAttribute User user, Model model, HttpServletRequest request) {
        String status = userService.validateHostLogin(user.getEmail(), user.getPassword());
        if ("USER_NOT_FOUND".equals(status)) {
            model.addAttribute("loginError", "Host email does not exist.");
            return "login_host";
        } else if ("WRONG_PASSWORD".equals(status)) {
            model.addAttribute("loginError", "Incorrect password.");
            return "login_host";
        } else if ("SUCCESS_FIRST_TIME".equals(status)) {
            model.addAttribute("email", user.getEmail());
            model.addAttribute("firstTime", true);
            return "login_host";
        }

        // Authenticate the user
        Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
        if (userOpt.isPresent()) {
            User authenticatedUser = userOpt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authenticatedUser.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Save authentication to session
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            logger.info("Host user {} authenticated successfully", authenticatedUser.getUsername());
        }

        // Find all events associated with this host
        java.util.List<com.momentsmanager.model.WeddingEvent> hostEvents = weddingEventRepository.findByHostEmail(user.getEmail());

        if (hostEvents.isEmpty()) {
            logger.warn("Host {} has no associated events", user.getEmail());
            return "redirect:/events/";
        } else if (hostEvents.size() == 1) {
            // Redirect to event details if only one event
            logger.info("Host {} has one event, redirecting to event details", user.getEmail());
            return "redirect:/events/" + hostEvents.get(0).getId();
        } else {
            // Redirect to event list if multiple events
            logger.info("Host {} has {} events, redirecting to event list", user.getEmail(), hostEvents.size());
            return "redirect:/events/";
        }
    }

    @GetMapping("/login/guest")
    public String guestLogin(Model model) {
        model.addAttribute("user", new User());
        return "login_guest";
    }

    @PostMapping("/login/guest")
    public String guestLoginPost(HttpServletRequest request, Model model) {
        String lastName = request.getParameter("lastname");
        String mobile = request.getParameter("mobile");
        String status = userService.validateGuestLogin(lastName, mobile);
        if ("GUEST_NOT_FOUND".equals(status)) {
            model.addAttribute("loginError", "Guest not found. Please check your family name and mobile number.");
            return "login_guest";
        }

        // Authenticate the guest
        Optional<Guest> guestOpt = guestRepository.findByFamilyNameIgnoreCaseAndContactPhone(lastName, mobile);
        if (guestOpt.isPresent()) {
            Guest guest = guestOpt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                guest.getFamilyName() + "_" + guest.getContactPhone(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Save authentication to session
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            logger.info("Guest {} authenticated successfully", guest.getFamilyName());
        }

        return "redirect:/guest/dashboard";
    }

    @PostMapping("/set-password")
    public String setPassword(HttpServletRequest request, Model model) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        boolean result = userService.setAdminPassword(username, password);
        if (!result) {
            model.addAttribute("loginError", "Failed to set password. Please try again.");
            model.addAttribute("username", username);
            model.addAttribute("firstTime", true);
            return "login_admin";
        }

        // Authenticate the user after password is set
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User authenticatedUser = userOpt.get();
            Authentication auth = new UsernamePasswordAuthenticationToken(
                authenticatedUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority(authenticatedUser.getRole()))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Save authentication to session
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            logger.info("Admin user {} authenticated successfully after password setup", authenticatedUser.getUsername());
        }

        return "redirect:/admin/dashboard";
    }

    @PostMapping("/set-password-host")
    public String setPasswordHost(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Find user by email and set password
        Optional<Host> hostOpt = hostRepository.findByEmail(email);
        if (hostOpt.isEmpty()) {
            model.addAttribute("loginError", "Failed to set password. Host not found.");
            model.addAttribute("email", email);
            model.addAttribute("firstTime", true);
            return "login_host";
        }

        Host host = hostOpt.get();
        boolean result = userService.setHostPassword(email, password);
        if (!result) {
            model.addAttribute("loginError", "Failed to set password. Please try again.");
            model.addAttribute("email", email);
            model.addAttribute("firstTime", true);
            return "login_host";
        }

        // Authenticate the user after password is set
        Authentication auth = new UsernamePasswordAuthenticationToken(
            host.getEmail(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority(host.getRole()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Save authentication to session
        request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        logger.info("Host user {} authenticated successfully after password setup", host.getUsername());

        // Find all events associated with this host
        java.util.List<com.momentsmanager.model.WeddingEvent> hostEvents = weddingEventRepository.findByHostEmail(email);

        if (hostEvents.isEmpty()) {
            logger.warn("Host {} has no associated events", email);
            return "redirect:/events/";
        } else if (hostEvents.size() == 1) {
            // Redirect to event details if only one event
            logger.info("Host {} has one event, redirecting to event details", email);
            return "redirect:/events/" + hostEvents.get(0).getId();
        } else {
            // Redirect to event list if multiple events
            logger.info("Host {} has {} events, redirecting to event list", email, hostEvents.size());
            return "redirect:/events/";
        }
    }
}
