package com.wedknots.web;

import com.wedknots.model.EventActivity;
import com.wedknots.model.EventItem;
import com.wedknots.model.ItemStatus;
import com.wedknots.model.Supplier;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.WeddingEventRepository;
import com.wedknots.service.EventActivityService;
import com.wedknots.service.EventItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/admin/events/{eventId}/items")
@RequiredArgsConstructor
public class AdminEventItemController {

    private final EventItemService eventItemService;
    private final EventActivityService eventActivityService;
    private final WeddingEventRepository weddingEventRepository;

    // ========== Item List ==========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listItems(@PathVariable Long eventId,
                           @RequestParam(required = false) Long supplierId,
                           @RequestParam(required = false) Long activityId,
                           @RequestParam(required = false) String name,
                           @RequestParam(required = false) ItemStatus status,
                           Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }

        WeddingEvent event = eventOpt.get();
        List<EventItem> items = eventItemService.getItemsByEventIdAndFilters(eventId, supplierId, activityId, name, status);
        List<Supplier> suppliers = eventItemService.getSuppliersByEventId(eventId);
        List<EventActivity> activities = eventActivityService.getActivitiesByEventId(eventId);

        // Statistics
        long pendingCount = eventItemService.countItemsByStatus(eventId, ItemStatus.PENDING);
        long orderedCount = eventItemService.countItemsByStatus(eventId, ItemStatus.ORDERED);
        long deliveredCount = eventItemService.countItemsByStatus(eventId, ItemStatus.DELIVERED);
        long notNeededCount = eventItemService.countItemsByStatus(eventId, ItemStatus.NOT_NEEDED);

        model.addAttribute("event", event);
        model.addAttribute("items", items);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("activities", activities);
        model.addAttribute("statuses", ItemStatus.values());
        model.addAttribute("selectedSupplierId", supplierId);
        model.addAttribute("selectedActivityId", activityId);
        model.addAttribute("selectedName", name);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("orderedCount", orderedCount);
        model.addAttribute("deliveredCount", deliveredCount);
        model.addAttribute("notNeededCount", notNeededCount);
        model.addAttribute("totalCost", eventItemService.getTotalCost(eventId));
        model.addAttribute("overdueItems", eventItemService.getOverdueItems(eventId));
        model.addAttribute("isAdmin", true);

        return "admin/event_items_list";
    }

    // ========== Item CRUD ==========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String newItem(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("item", new EventItem());
        model.addAttribute("suppliers", eventItemService.getSuppliersByEventId(eventId));
        model.addAttribute("activities", eventActivityService.getActivitiesByEventId(eventId));
        model.addAttribute("statuses", ItemStatus.values());
        model.addAttribute("isNew", true);
        model.addAttribute("isAdmin", true);

        return "admin/event_item_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createItem(@PathVariable Long eventId,
                            @ModelAttribute EventItem item,
                            @RequestParam(required = false) Long supplierId,
                            @RequestParam(required = false) Long activityId,
                            RedirectAttributes redirectAttributes) {
        try {
            eventItemService.createItem(item, eventId, supplierId, activityId);
            redirectAttributes.addFlashAttribute("success", "Item created successfully!");
        } catch (Exception e) {
            log.error("Error creating item", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create item: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{itemId}/edit")
    public String editItem(@PathVariable Long eventId, @PathVariable Long itemId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<EventItem> itemOpt = eventItemService.getItemById(itemId);

        if (eventOpt.isEmpty() || itemOpt.isEmpty()) {
            return "redirect:/admin/events/" + eventId + "/items";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("item", itemOpt.get());
        model.addAttribute("suppliers", eventItemService.getSuppliersByEventId(eventId));
        model.addAttribute("activities", eventActivityService.getActivitiesByEventId(eventId));
        model.addAttribute("statuses", ItemStatus.values());
        model.addAttribute("isNew", false);
        model.addAttribute("isAdmin", true);

        return "admin/event_item_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{itemId}/edit")
    public String updateItem(@PathVariable Long eventId,
                            @PathVariable Long itemId,
                            @ModelAttribute EventItem item,
                            @RequestParam(required = false) Long supplierId,
                            @RequestParam(required = false) Long activityId,
                            RedirectAttributes redirectAttributes) {
        try {
            eventItemService.updateItem(itemId, item, supplierId, activityId);
            redirectAttributes.addFlashAttribute("success", "Item updated successfully!");
        } catch (Exception e) {
            log.error("Error updating item", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update item: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{itemId}/delete")
    public String deleteItem(@PathVariable Long eventId,
                            @PathVariable Long itemId,
                            RedirectAttributes redirectAttributes) {
        try {
            eventItemService.deleteItem(itemId);
            redirectAttributes.addFlashAttribute("success", "Item deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting item", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete item: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{itemId}/status")
    public String updateItemStatus(@PathVariable Long eventId,
                                  @PathVariable Long itemId,
                                  @RequestParam ItemStatus status,
                                  RedirectAttributes redirectAttributes) {
        try {
            eventItemService.updateItemStatus(itemId, status);
            redirectAttributes.addFlashAttribute("success", "Status updated successfully!");
        } catch (Exception e) {
            log.error("Error updating item status", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items";
    }

    // ========== Supplier CRUD ==========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/suppliers")
    public String listSuppliers(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("suppliers", eventItemService.getSuppliersByEventId(eventId));
        model.addAttribute("isAdmin", true);

        return "admin/event_suppliers_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/suppliers/new")
    public String newSupplier(@PathVariable Long eventId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            return "redirect:/admin/events";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("isNew", true);
        model.addAttribute("isAdmin", true);

        return "admin/event_supplier_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/suppliers/new")
    public String createSupplier(@PathVariable Long eventId,
                                @ModelAttribute Supplier supplier,
                                RedirectAttributes redirectAttributes) {
        try {
            eventItemService.createSupplier(supplier, eventId);
            redirectAttributes.addFlashAttribute("success", "Supplier created successfully!");
        } catch (Exception e) {
            log.error("Error creating supplier", e);
            redirectAttributes.addFlashAttribute("error", "Failed to create supplier: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items/suppliers";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/suppliers/{supplierId}/edit")
    public String editSupplier(@PathVariable Long eventId, @PathVariable Long supplierId, Model model) {
        Optional<WeddingEvent> eventOpt = weddingEventRepository.findById(eventId);
        Optional<Supplier> supplierOpt = eventItemService.getSupplierById(supplierId);

        if (eventOpt.isEmpty() || supplierOpt.isEmpty()) {
            return "redirect:/admin/events/" + eventId + "/items/suppliers";
        }

        model.addAttribute("event", eventOpt.get());
        model.addAttribute("supplier", supplierOpt.get());
        model.addAttribute("isNew", false);
        model.addAttribute("isAdmin", true);

        return "admin/event_supplier_form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/suppliers/{supplierId}/edit")
    public String updateSupplier(@PathVariable Long eventId,
                                @PathVariable Long supplierId,
                                @ModelAttribute Supplier supplier,
                                RedirectAttributes redirectAttributes) {
        try {
            eventItemService.updateSupplier(supplierId, supplier);
            redirectAttributes.addFlashAttribute("success", "Supplier updated successfully!");
        } catch (Exception e) {
            log.error("Error updating supplier", e);
            redirectAttributes.addFlashAttribute("error", "Failed to update supplier: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items/suppliers";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/suppliers/{supplierId}/delete")
    public String deleteSupplier(@PathVariable Long eventId,
                                @PathVariable Long supplierId,
                                RedirectAttributes redirectAttributes) {
        try {
            eventItemService.deleteSupplier(supplierId);
            redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting supplier", e);
            redirectAttributes.addFlashAttribute("error", "Failed to delete supplier: " + e.getMessage());
        }
        return "redirect:/admin/events/" + eventId + "/items/suppliers";
    }
}

