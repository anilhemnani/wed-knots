package com.wedknots.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "host_tbl")
public class Host {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phone;
    private String provider;
    private String role;

    // Bidirectional relationship: Host belongs to Event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private WeddingEvent event;

    // Helper method to get eventId for backward compatibility
    public Long getEventId() {
        return event != null ? event.getId() : null;
    }

    // Helper method to set eventId for backward compatibility
    public void setEventId(Long eventId) {
        // This is handled through setEvent() method
        // Kept for backward compatibility but does nothing
    }
}
