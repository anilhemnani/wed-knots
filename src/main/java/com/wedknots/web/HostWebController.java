package com.wedknots.web;

import com.wedknots.model.Host;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.HostRepository;
import com.wedknots.repository.WeddingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/events/{eventId}/hosts")
public class HostWebController {

    @Autowired
    private WeddingEventRepository weddingEventRepository;

    @Autowired
    private HostRepository hostRepository;

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping
    public String listHosts(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("hosts", hostRepository.findByEventId(eventId));
        return "host_list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/new")
    public String newHost(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("host", new Host());
        return "host_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/new")
    public String createHost(@PathVariable Long eventId, @ModelAttribute Host host) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/events";
        }
        host.setEvent(eventOpt.get());
        hostRepository.save(host);
        return "redirect:/events/" + eventId + "/hosts";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{hostId}")
    public String viewHost(@PathVariable Long eventId, @PathVariable Long hostId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Host> hostOpt = hostRepository.findById(hostId);
        if (eventOpt.isEmpty() || hostOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/hosts";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("host", hostOpt.get());
        return "host_view";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @GetMapping("/{hostId}/edit")
    public String editHost(@PathVariable Long eventId, @PathVariable Long hostId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Host> hostOpt = hostRepository.findById(hostId);
        if (eventOpt.isEmpty() || hostOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/hosts";
        }
        model.addAttribute("event", eventOpt.get());
        model.addAttribute("host", hostOpt.get());
        return "host_form";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{hostId}/edit")
    public String updateHost(@PathVariable Long eventId, @PathVariable Long hostId, @ModelAttribute Host host) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Host> existingHostOpt = hostRepository.findById(hostId);
        if (eventOpt.isEmpty() || existingHostOpt.isEmpty()) {
            return "redirect:/events/" + eventId + "/hosts";
        }
        host.setId(hostId);
        host.setEvent(eventOpt.get());
        hostRepository.save(host);
        return "redirect:/events/" + eventId + "/hosts";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HOST')")
    @PostMapping("/{hostId}/delete")
    public String deleteHost(@PathVariable Long eventId, @PathVariable Long hostId) {
        hostRepository.deleteById(hostId);
        return "redirect:/events/" + eventId + "/hosts";
    }
}

