package com.eventhub.eventhub.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String gender;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role = UserRole.ROLE_USER;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_updated_at")
    private LocalDate locationUpdatedAt; // Konumun en son güncellendiği tarih

    @Column(name = "interests")
    private String interests;

    public String[] getInterests() {
        return interests != null ? interests.split(",") : new String[0];
    }

    public void setInterests(String[] interests) {
        this.interests = interests != null ? String.join(",", interests) : null;
    }

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "profile_image")
    private String profileImageUrl;
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;
    @Transient
    private MultipartFile profileImage;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getValue()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Event> createdEvents = new ArrayList<>();

    public int getCreatedEventsCount() {
        if (createdEvents == null) {
            return 0;
        }
        return (int) createdEvents.stream()
                .filter(event -> Boolean.TRUE.equals(event.getApproved()))
                .count();
    }
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Participant> participatedEvents = new ArrayList<>();

    public int getParticipatedEventsCount() {
        if (participatedEvents == null) {
            return 0;
        }
        return (int) participatedEvents.stream()
                .map(Participant::getEvent)
                .filter(event -> Boolean.TRUE.equals(event.getApproved()))
                .count();
    }
}
