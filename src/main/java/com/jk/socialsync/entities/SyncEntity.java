package com.jk.socialsync.entities;

import com.jk.socialsync.types.MediaType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "syncs")
public class SyncEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long syncId;

    @Column(unique = true, updatable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime validUntil;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Boolean isArchived;

    private String userProfilePictureUrl;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserEntity user;

    @PrePersist
    public void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        validUntil = uploadedAt.plusHours(24);
    }
}
