package com.wedknots.web;

import com.wedknots.model.ActivityType;
import com.wedknots.model.EventActivity;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.EventActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/events/{eventId}/schedule")
@RequiredArgsConstructor
public class EventScheduleController {

    private final EventActivityService activityService;
    private final WeddingEventRepository weddingEventRepository;

    // ========== Schedule List ==========

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listActivities(@PathVariable Long eventId,
                                @RequestParam(required = false) ActivityType type,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(required = false) Boolean visibleOnly,
                                Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        WeddingEvent event = eventOpt.get();
        List<EventActivity> activities = activityService.getActivitiesByEventIdAndFilters(eventId, type, date, visibleOnly);
        Map<LocalDate, List<EventActivity>> groupedActivities = activityService.getActivitiesGroupedByDate(eventId, false);
        List<LocalDate> availableDates = activityService.getDistinctDates(eventId);

        // Statistics
        long totalCount = activityService.countActivities(eventId);
        long ceremonyCount = activityService.countByType(eventId, ActivityType.CEREMONY);
        long activityCount = activityService.countByType(eventId, ActivityType.ACTIVITY);
        long mealCount = activityService.countByType(eventId, ActivityType.MEAL);

        model.addAttribute("event", event);
        model.addAttribute("activities", activities);
        model.addAttribute("groupedActivities", groupedActivities);
        model.addAttribute("availableDates", availableDates);
        model.addAttribute("types", ActivityType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedDate", date);
        model.addAttribute("visibleOnly", visibleOnly);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("ceremonyCount", ceremonyCount);
        model.addAttribute("activityCount", activityCount);
        model.addAttribute("mealCount", mealCount);

        return "event_schedule_list";
    }

    // ========== Activity CRUD ==========

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/new")
    public String newActivity(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("activity", new EventActivity());
        model.addAttribute("types", ActivityType.values());
        model.addAttribute("isNew", true);

        return "event_activity_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/new")
    public String createActivity(@PathVariable Long eventId,
                                @ModelAttribute EventActivity activity,
                                RedirectAttributes redirectAttributes) {
        try {
            activityService.createActivity(activity, eventId);
            redirectAttributes.addFlashAttribute("success", "Activity created successfully!");
        } catch (Exception e) {
            log.error("Error creating activity", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create activity: " + e.getMessage());
        }
        return "redirect:/events/" + eventId + "/schedule";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{activityId}/edit")
    public String editActivity(@PathVariable Long eventId, @PathVariable Long activityId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<EventActivity> activityOpt = activityService.getActivityById(activityId);

        if (eventOpt.isEmpty() || activityOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/schedule";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("activity", activityOpt.get());
        model.addAttribute("types", ActivityType.values());
        model.addAttribute("isNew", false);

        return "event_activity_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{activityId}/edit")
    public String updateActivity(@PathVariable Long eventId,
                                @PathVariable Long activityId,
                                @ModelAttribute EventActivity activity,
                                RedirectAttributes redirectAttributes) {
        try {
            activityService.updateActivity(activityId, activity);
            redirectAttributes.addFlashAttribute("success", "Activity updated successfully!");
        } catch (Exception e) {
            log.error("Error updating activity", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update activity: " + e.getMessage());
        }
        return "redirect:/events/" + eventId + "/schedule";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{activityId}/delete")
    public String deleteActivity(@PathVariable Long eventId,
                                @PathVariable Long activityId,
                                RedirectAttributes redirectAttributes) {
        try {
            activityService.deleteActivity(activityId);
            redirectAttributes.addFlashAttribute("success", "Activity deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting activity", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete activity: " + e.getMessage());
        }
        return "redirect:/events/" + eventId + "/schedule";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{activityId}/toggle-visibility")
    public String toggleVisibility(@PathVariable Long eventId,
                                  @PathVariable Long activityId,
                                  RedirectAttributes redirectAttributes) {
        try {
            EventActivity activity = activityService.toggleVisibility(activityId);
            String status = Boolean.TRUE.equals(activity.getVisibleToGuests()) ? "visible to guests" : "hidden from guests";
            redirectAttributes.addFlashAttribute("success", "Activity is now " + status);
        } catch (Exception e) {
            log.error("Error toggling visibility", e);
            redirectAttributes.addFlashAttribute("error", "Failed to toggle visibility: " + e.getMessage());
        }
        return "redirect:/events/" + eventId + "/schedule";
    }
}

