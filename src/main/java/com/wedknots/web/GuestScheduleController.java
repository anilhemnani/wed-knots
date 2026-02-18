package com.wedknots.web;

import com.wedknots.model.ActivityType;
import com.wedknots.model.EventActivity;
import com.wedknots.model.Guest;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.GuestRepository;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.EventActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/guest/schedule")
@RequiredArgsConstructor
public class GuestScheduleController {

    private final EventActivityService activityService;
    private final WeddingEventRepository weddingEventRepository;
    private final GuestRepository guestRepository;

    // ========== Guest Schedule View (Read-Only) ==========

    @PreAuthorize("hasRole('GUEST')")
    @GetMapping
    public String viewSchedule(@RequestParam(required = false) Long eventId,
                              @RequestParam(required = false) ActivityType type,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                              HttpServletRequest request,
                              Model model) {
        // Get guest from session
        Long guestId = (Long) request.getSession().getAttribute("guestId");
        if (guestId == null) {
            return "redirect:/login/guest";
        }

        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return "redirect:/login/guest";
        }

        Guest guest = guestOpt.get();
        Long resolvedEventId = eventId != null ? eventId : guest.getEventId();

        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(resolvedEventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/guest/dashboard";
        }

        WeddingEvent event = eventOpt.get();

        // Only get activities visible to guests
        List<EventActivity> activities = activityService.getVisibleActivitiesByEventId(resolvedEventId);

        // Apply filters if provided
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

        Map<LocalDate, List<EventActivity>> groupedActivities = activityService.getActivitiesGroupedByDate(resolvedEventId, true);
        List<LocalDate> availableDates = activityService.getDistinctDates(resolvedEventId);
        List<EventActivity> upcomingActivities = activityService.getUpcomingActivities(resolvedEventId);

        model.addAttribute("event", event);
        model.addAttribute("guest", guest);
        model.addAttribute("activities", activities);
        model.addAttribute("groupedActivities", groupedActivities);
        model.addAttribute("availableDates", availableDates);
        model.addAttribute("upcomingActivities", upcomingActivities);
        model.addAttribute("types", ActivityType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedDate", date);

        return "guest_schedule_view";
    }

    @PreAuthorize("hasRole('GUEST')")
    @GetMapping("/{activityId}")
    public String viewActivityDetail(@PathVariable Long activityId,
                                    HttpServletRequest request,
                                    Model model) {
        // Get guest from session
        Long guestId = (Long) request.getSession().getAttribute("guestId");
        if (guestId == null) {
            return "redirect:/login/guest";
        }

        Optional<Guest> guestOpt = guestRepository.findById(guestId);
        if (guestOpt.isEmpty()) {
            return "redirect:/login/guest";
        }

        Guest guest = guestOpt.get();

        Optional<EventActivity> activityOpt = activityService.getActivityById(activityId);
        if (activityOpt.isEmpty() || !Boolean.TRUE.equals(activityOpt.get().getVisibleToGuests())) {
            return "redirect:/guest/schedule?eventId=" + guest.getEventId();
        }

        EventActivity activity = activityOpt.get();
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(activity.getEventId());

        model.addAttribute("activity", activity);
        model.addAttribute("event", eventOpt.orElse(null));
        model.addAttribute("guest", guest);

        return "guest_activity_detail";
    }
}

