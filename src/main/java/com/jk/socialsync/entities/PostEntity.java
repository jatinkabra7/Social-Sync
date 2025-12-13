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
@Table(name = "posts")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private String caption;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private Boolean isPublic;

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserEntity user;
}