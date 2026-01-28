package com.wedknots.template;

import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.model.Attendee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template Variable Processor
 * Replaces template variables with actual values from entities
 *
 * Supported variables:
 * - {{event.name}}, {{event.bride}}, {{event.groom}}, {{event.date}}, {{event.location}}
 * - {{guest.name}}, {{guest.email}}, {{guest.phone}}, {{guest.familyName}}
 * - {{attendee.count}}, {{attendee.names}}
 */
@Component
public class TemplateVariableProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TemplateVariableProcessor.class);

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Process template and replace all variables
     */
    public String process(String template, WeddingEvent event, Guest guest) {
        if (template == null) {
            return null;
        }

        Map<String, String> variables = buildVariableMap(event, guest);
        return replaceVariables(template, variables);
    }

    /**
     * Process template with custom variables
     */
    public String process(String template, Map<String, Object> customVariables) {
        if (template == null) {
            return null;
        }

        Map<String, String> stringVariables = new HashMap<>();
        for (Map.Entry<String, Object> entry : customVariables.entrySet()) {
            stringVariables.put(entry.getKey(),
                entry.getValue() != null ? entry.getValue().toString() : "");
        }

        return replaceVariables(template, stringVariables);
    }

    /**
     * Build map of all available variables
     */
    private Map<String, String> buildVariableMap(WeddingEvent event, Guest guest) {
        Map<String, String> variables = new HashMap<>();

        // Event variables
        if (event != null) {
            variables.put("event.name", safeString(event.getName()));
            variables.put("event.bride", safeString(event.getBrideName()));
            variables.put("event.groom", safeString(event.getGroomName()));
            variables.put("event.date", formatDate(event.getDate()));
            variables.put("event.location", safeString(event.getPlace()));
            variables.put("event.venue", safeString(event.getPlace())); // Same as location for compatibility
            variables.put("event.subdomain", safeString(event.getSubdomain()));
        }

        // Guest variables
        if (guest != null) {
            String fullName = (guest.getContactFirstName() != null ? guest.getContactFirstName() : "") +
                             " " + (guest.getContactLastName() != null ? guest.getContactLastName() : "");
            variables.put("guest.name", safeString(fullName.trim()));
            variables.put("guest.first_name", safeString(guest.getContactFirstName()));
            variables.put("guest.last_name", safeString(guest.getContactLastName()));
            variables.put("guest.family_name", safeString(guest.getFamilyName()));
            variables.put("guest.email", safeString(guest.getContactEmail()));
            variables.put("guest.phone", safeString(guest.getPrimaryPhoneNumber()));

            // Attendee variables (through RSVP)
            if (guest.getRsvp() != null && guest.getRsvp().getAttendees() != null
                    && !guest.getRsvp().getAttendees().isEmpty()) {
                variables.put("attendee.count", String.valueOf(guest.getRsvp().getAttendees().size()));
                variables.put("attendee.names", getAttendeeNames(guest));
                variables.put("attendee.first", getFirstAttendeeName(guest));
            } else {
                variables.put("attendee.count", "0");
                variables.put("attendee.names", "");
                variables.put("attendee.first", "");
            }
        }

        return variables;
    }

    /**
     * Replace all variables in template
     */
    private String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String fullMatch = matcher.group(0);  // {{variable}}
            String variableName = matcher.group(1).trim();  // variable

            String replacement = variables.get(variableName);
            if (replacement != null) {
                result = result.replace(fullMatch, replacement);
                logger.debug("Replaced variable '{}' with '{}'", variableName, replacement);
            } else {
                logger.warn("Variable '{}' not found, leaving as-is", variableName);
            }
        }

        return result;
    }

    /**
     * Get list of all available variables for given context
     */
    public Map<String, String> getAvailableVariables(WeddingEvent event, Guest guest) {
        return buildVariableMap(event, guest);
    }

    /**
     * Extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Get comma-separated list of attendee names
     */
    private String getAttendeeNames(Guest guest) {
        if (guest.getRsvp() == null || guest.getRsvp().getAttendees() == null
                || guest.getRsvp().getAttendees().isEmpty()) {
            return "";
        }

        StringBuilder names = new StringBuilder();
        for (Attendee attendee : guest.getRsvp().getAttendees()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(attendee.getName());
        }
        return names.toString();
    }

    /**
     * Get first attendee name
     */
    private String getFirstAttendeeName(Guest guest) {
        if (guest.getRsvp() == null || guest.getRsvp().getAttendees() == null
                || guest.getRsvp().getAttendees().isEmpty()) {
            return "";
        }
        return guest.getRsvp().getAttendees().get(0).getName();
    }

    /**
     * Format date to readable string
     */
    private String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
    }

    /**
     * Safe string conversion
     */
    private String safeString(Object value) {
        return value != null ? value.toString() : "";
    }
}

