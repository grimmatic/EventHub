package com.eventhub.eventhub.service;

import com.eventhub.eventhub.entity.Event;
import com.eventhub.eventhub.model.Location;
import com.eventhub.eventhub.repository.EventRepository;
import org.springframework.stereotype.Service;

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

    public List<Map<String, Object>> getOtherEventLocations(Long currentEventId) {
        List<Event> events = eventRepository.findByApprovedIsTrue();
        List<Map<String, Object>> eventLocations = new ArrayList<>();

        for (Event event : events) {
            if (!event.getId().equals(currentEventId)) {
                try {
                    Location loc = getLocationFromAddress(event);
                    if (isValidLocation(loc)) {
                        Map<String, Object> eventInfo = new HashMap<>();
                        eventInfo.put("id", event.getId());
                        eventInfo.put("name", event.getName());
                        eventInfo.put("latitude", loc.getLatitude());
                        eventInfo.put("longitude", loc.getLongitude());
                        eventInfo.put("address", event.getLocation());
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
}