package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Entity
@Table(name = "wedding_event_tbl")
public class WeddingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Aggregation: Event owns Hosts - cascade all operations, orphan removal enabled
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Host> hosts = new ArrayList<>();

    // Aggregation: Event owns Guests - cascade all operations, orphan removal enabled
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Guest> guests = new ArrayList<>();

    // Aggregation: Event owns Invitations - cascade all operations, orphan removal enabled
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Invitation> invitations = new ArrayList<>();

    private String name;

    // Use LocalDate for the event date
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @Column(name = "bride_name")
    private String brideName;

    @Column(name = "groom_name")
    private String groomName;

    @Column(name = "subdomain", unique = true, nullable = false, length = 50)
    private String subdomain;

    @Column(name = "expected_guest_arrival_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedGuestArrivalDate;

    @Column(name = "expected_guest_departure_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedGuestDepartureDate;

    @Column(name = "preferred_travel_airport")
    private String preferredTravelAirport;

    @Column(name = "preferred_travel_station")
    private String preferredTravelStation;

    private String place;

    @Column(name = "default_max_allowed_attendees")
    private Integer defaultMaxAllowedAttendees;

    @Column(name = "about_location_url", length = 255)
    private String aboutLocationUrl; // Relative URL for location info page

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "about", columnDefinition = "text")
    private String about;

    @Column(name = "travel_options", columnDefinition = "text")
    private String travelOptions;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
