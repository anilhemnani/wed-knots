package com.wedknots.service;

import com.wedknots.model.EventActivity;
import com.wedknots.model.EventItem;
import com.wedknots.model.ItemStatus;
import com.wedknots.model.Supplier;
import com.wedknots.model.WeddingEvent;
import com.wedknots.repository.EventActivityRepository;
import com.wedknots.repository.EventItemRepository;
import com.wedknots.repository.SupplierRepository;
import com.wedknots.repository.WeddingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventItemService {

    private final EventItemRepository eventItemRepository;
    private final SupplierRepository supplierRepository;
    private final WeddingEventRepository weddingEventRepository;
    private final EventActivityRepository eventActivityRepository;

    // ========== Supplier Operations ==========

    @Transactional(readOnly = true)
    public List<Supplier> getSuppliersByEventId(Long eventId) {
        return supplierRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier, Long eventId) {
        WeddingEvent event = weddingEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        supplier.setEvent(event);
        log.info("Creating supplier '{}' for event {}", supplier.getName(), eventId);
        return supplierRepository.save(supplier);
    }

    @Transactional
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        existing.setName(updatedSupplier.getName());
        existing.setContactNumber(updatedSupplier.getContactNumber());
        existing.setCity(updatedSupplier.getCity());
        existing.setEmail(updatedSupplier.getEmail());
        existing.setNotes(updatedSupplier.getNotes());

        log.info("Updated supplier {}", id);
        return supplierRepository.save(existing);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        log.info("Deleting supplier {}", id);
        supplierRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctSupplierCities(Long eventId) {
        return supplierRepository.findDistinctCitiesByEventId(eventId);
    }

    // ========== Event Item Operations ==========

    @Transactional(readOnly = true)
    public List<EventItem> getItemsByEventId(Long eventId) {
        return eventItemRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public List<EventItem> getItemsByEventIdAndFilters(Long eventId, Long supplierId, Long activityId, String name, ItemStatus status) {
        List<EventItem> items = eventItemRepository.findByEventId(eventId);

        // Apply filters
        if (supplierId != null) {
            items = items.stream()
                    .filter(i -> i.getSupplier() != null && i.getSupplier().getId().equals(supplierId))
                    .toList();
        }

        if (activityId != null) {
            items = items.stream()
                    .filter(i -> i.getNeededForActivity() != null && i.getNeededForActivity().getId().equals(activityId))
                    .toList();
        }

        if (name != null && !name.trim().isEmpty()) {
            String lowerName = name.toLowerCase();
            items = items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(lowerName))
                    .toList();
        }

        if (status != null) {
            items = items.stream()
                    .filter(i -> i.getStatus() == status)
                    .toList();
        }

        return items;
    }

    @Transactional(readOnly = true)
    public Optional<EventItem> getItemById(Long id) {
        return eventItemRepository.findById(id);
    }

    @Transactional
    public EventItem createItem(EventItem item, Long eventId, Long supplierId, Long activityId) {
        WeddingEvent event = weddingEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        item.setEvent(event);

        if (supplierId != null) {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            item.setSupplier(supplier);
        }

        if (activityId != null) {
            EventActivity activity = eventActivityRepository.findById(activityId)
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));
            item.setNeededForActivity(activity);
        }

        log.info("Creating item '{}' for event {}", item.getName(), eventId);
        return eventItemRepository.save(item);
    }

    @Transactional
    public EventItem updateItem(Long id, EventItem updatedItem, Long supplierId, Long activityId) {
        EventItem existing = eventItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));

        existing.setName(updatedItem.getName());
        existing.setDescription(updatedItem.getDescription());
        existing.setQuantity(updatedItem.getQuantity());
        existing.setUnit(updatedItem.getUnit());
        existing.setUnitPrice(updatedItem.getUnitPrice());
        existing.setNeededByDate(updatedItem.getNeededByDate());
        existing.setStatus(updatedItem.getStatus());
        existing.setNotes(updatedItem.getNotes());
        existing.setResponsible(updatedItem.getResponsible());

        if (supplierId != null) {
            Supplier supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new RuntimeException("Supplier not found: " + supplierId));
            existing.setSupplier(supplier);
        } else {
            existing.setSupplier(null);
        }

        if (activityId != null) {
            EventActivity activity = eventActivityRepository.findById(activityId)
                    .orElseThrow(() -> new RuntimeException("Activity not found: " + activityId));
            existing.setNeededForActivity(activity);
        } else {
            existing.setNeededForActivity(null);
        }

        log.info("Updated item {}", id);
        return eventItemRepository.save(existing);
    }

    @Transactional
    public EventItem updateItemStatus(Long id, ItemStatus status) {
        EventItem item = eventItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        item.setStatus(status);
        log.info("Updated item {} status to {}", id, status);
        return eventItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        log.info("Deleting item {}", id);
        eventItemRepository.deleteById(id);
    }

    // ========== Statistics ==========

    @Transactional(readOnly = true)
    public long countItemsByStatus(Long eventId, ItemStatus status) {
        Long count = eventItemRepository.countByEventIdAndStatus(eventId, status);
        return count != null ? count : 0;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(Long eventId) {
        return eventItemRepository.getTotalCostByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getOrderedCost(Long eventId) {
        return eventItemRepository.getTotalCostByEventIdAndStatus(eventId, ItemStatus.ORDERED);
    }

    @Transactional(readOnly = true)
    public BigDecimal getDeliveredCost(Long eventId) {
        return eventItemRepository.getTotalCostByEventIdAndStatus(eventId, ItemStatus.DELIVERED);
    }

    @Transactional(readOnly = true)
    public List<EventItem> getOverdueItems(Long eventId) {
        return eventItemRepository.findOverdueItems(eventId, LocalDate.now());
    }
}

