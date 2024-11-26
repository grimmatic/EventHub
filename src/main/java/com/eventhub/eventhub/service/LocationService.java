package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.model.Location;
import com.eventhub.eventhub.repository.EventRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {
    private final EventRepository eventRepository;
    private static final double DEFAULT_LAT = 41.0082;  // İstanbul
    private static final double DEFAULT_LNG = 28.9784;  // İstanbul

    public LocationService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventLocationDTO> getOtherEventLocations(Long currentEventId) {
        List<Event> events = eventRepository.findByApprovedIsTrue();
        List<EventLocationDTO> eventLocations = new ArrayList<>();

        for (Event event : events) {
            if (!event.getId().equals(currentEventId)) {
                try {
                    Location loc = getLocationFromAddress(event);
                    if (isValidLocation(loc)) {
                        EventLocationDTO eventInfo = new EventLocationDTO();
                        eventInfo.setId(event.getId());
                        eventInfo.setName(event.getName());
                        eventInfo.setDescription(event.getDescription());
                        eventInfo.setStartDate(event.getStartDate());
                        eventInfo.setLatitude(loc.getLatitude());
                        eventInfo.setLongitude(loc.getLongitude());
                        eventInfo.setAddress(event.getLocation());
                        eventLocations.add(eventInfo);
                    }
                } catch (Exception e) {
                    // Hata durumunda bu etkinliği atla
                }
            }
        }
        return eventLocations;
    }

    public Location getLocationFromAddress(Event event) {
        Location location = new Location();
        if (event.getLatitude() != null && event.getLongitude() != null) {
            location.setLatitude(event.getLatitude());
            location.setLongitude(event.getLongitude());
            location.setAddress(event.getLocation());
        } else {
            // Fallback olarak İstanbul koordinatları
            location.setLatitude(DEFAULT_LAT);
            location.setLongitude(DEFAULT_LNG);
            location.setAddress(event.getLocation() != null ? event.getLocation() : "İstanbul");
        }
        return location;
    }

    private Location getDefaultLocation() {
        Location location = new Location();
        location.setLatitude(DEFAULT_LAT);
        location.setLongitude(DEFAULT_LNG);
        location.setAddress("İstanbul");
        return location;
    }

    private boolean isValidLocation(Location location) {
        return location != null &&
                location.getLatitude() != null &&
                location.getLongitude() != null &&
                location.getLatitude() >= -90 &&
                location.getLatitude() <= 90 &&
                location.getLongitude() >= -180 &&
                location.getLongitude() <= 180;
    }
    @Getter
    @Setter
    public static class EventLocationDTO {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime startDate;
        private Double latitude;
        private Double longitude;
        private String address;
    }
}