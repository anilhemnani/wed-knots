package com.wedknots.service;

import com.wedknots.model.ActivityType;
import com.wedknots.model.EventActivity;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.EventActivityRepository;
import com.wedknots.repository.WeddingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventActivityService {

    private final EventActivityRepository activityRepository;
    private final WeddingEventRepository weddingEventRepository;

    // ========== Activity Operations ==========

    @Transactional(readOnly = true)
    public List<EventActivity> getActivitiesByEventId(Long eventId) {
        return activityRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventActivity> getVisibleActivitiesByEventId(Long eventId) {
        return activityRepository.findByEventIdAndVisibleToGuests(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventActivity> getActivitiesByEventIdAndFilters(Long eventId, ActivityType type, LocalDate date, Boolean visibleOnly) {
        List<EventActivity> activities = activityRepository.findByEventId(eventId);

        // Apply filters
        if (type != null) {
            activities = activities.stream()
                    .filter(a -> a.getActivityType() == type)
                    .toList();
        }

        if (date != null) {
            activities = activities.stream()
                    .filter(a -> a.getStartTime() != null && a.getStartTime().toLocalDate().equals(date))
                    .toList();
        }

        if (Boolean.TRUE.equals(visibleOnly)) {
            activities = activities.stream()
                    .filter(a -> Boolean.TRUE.equals(a.getVisibleToGuests()))
                    .toList();
        }

        return activities;
    }

    @Transactional(readOnly = true)
    public Optional<EventActivity> getActivityById(Long id) {
        return activityRepository.findById(id);
    }

    @Transactional
    public EventActivity createActivity(EventActivity activity, Long eventId) {
        WeddingEvent event = weddingEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        activity.setEvent(event);

        // Set default sort order if not provided
        if (activity.getSortOrder() == null) {
            Long count = activityRepository.countByEventId(eventId);
            activity.setSortOrder(count.intValue() + 1);
        }

        log.info("Creating activity '{}' for event {}", activity.getName(), eventId);
        return activityRepository.save(activity);
    }

    @Transactional
    public EventActivity updateActivity(Long id, EventActivity updatedActivity) {
        EventActivity existing = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));

        existing.setName(updatedActivity.getName());
        existing.setDescription(updatedActivity.getDescription());
        existing.setLocation(updatedActivity.getLocation());
        existing.setStartTime(updatedActivity.getStartTime());
        existing.setEndTime(updatedActivity.getEndTime());
        existing.setActivityType(updatedActivity.getActivityType());
        existing.setVisibleToGuests(updatedActivity.getVisibleToGuests());
        existing.setDressCode(updatedActivity.getDressCode());
        existing.setNotes(updatedActivity.getNotes());
        existing.setSortOrder(updatedActivity.getSortOrder());

        log.info("Updated activity {}", id);
        return activityRepository.save(existing);
    }

    @Transactional
    public EventActivity toggleVisibility(Long id) {
        EventActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found: " + id));
        activity.setVisibleToGuests(!Boolean.TRUE.equals(activity.getVisibleToGuests()));
        log.info("Toggled visibility for activity {} to {}", id, activity.getVisibleToGuests());
        return activityRepository.save(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        log.info("Deleting activity {}", id);
        activityRepository.deleteById(id);
    }

    // ========== Statistics & Grouping ==========

    @Transactional(readOnly = true)
    public long countActivities(Long eventId) {
        Long count = activityRepository.countByEventId(eventId);
        return count != null ? count : 0;
    }

    @Transactional(readOnly = true)
    public long countByType(Long eventId, ActivityType type) {
        Long count = activityRepository.countByEventIdAndType(eventId, type);
        return count != null ? count : 0;
    }

    @Transactional(readOnly = true)
    public List<EventActivity> getUpcomingActivities(Long eventId) {
        return activityRepository.findUpcomingActivities(eventId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, List<EventActivity>> getActivitiesGroupedByDate(Long eventId, boolean visibleOnly) {
        List<EventActivity> activities = visibleOnly
                ? activityRepository.findByEventIdAndVisibleToGuests(eventId)
                : activityRepository.findByEventId(eventId);

        return activities.stream()
                .filter(a -> a.getStartTime() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getStartTime().toLocalDate(),
                        Collectors.toList()
                ));
    }

    @Transactional(readOnly = true)
    public List<LocalDate> getDistinctDates(Long eventId) {
        List<EventActivity> activities = activityRepository.findByEventId(eventId);
        return activities.stream()
                .filter(a -> a.getStartTime() != null)
                .map(a -> a.getStartTime().toLocalDate())
                .distinct()
                .sorted()
                .toList();
    }
}

