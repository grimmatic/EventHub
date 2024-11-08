package com.eventhub.eventhub.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    @Column(columnDefinition = "varchar(20) default 'ROLE_USER'")
    private String role = "ROLE_USER";

    @Column(name = "profile_image")
    private String profileImageUrl;

    @ElementCollection
    @CollectionTable(name = "user_interests",
            joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "interest")
    private Set<String> interests;

    @Column(name = "total_points")
    private Integer totalPoints = 0;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Lombok @Data anotasyonu toString(), equals() ve hashCode() metodlarını oluşturur
    // ama password gibi hassas bilgileri göstermemek için toString'i override ediyoruz
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
}