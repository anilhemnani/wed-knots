package com.wedknots.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rsvp_tbl")
public class RSVP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "guest_id", unique = true)
    @ToString.Exclude
    private Guest guest;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "status")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RSVPStatus status = RSVPStatus.PENDING;

    @Column(name = "attendee_count")
    private int attendeeCount;

    @OneToMany(mappedBy = "rsvp", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    @ToString.Exclude
    private List<Attendee> attendees = new ArrayList<>();

    @Version
    private Long version;
}
