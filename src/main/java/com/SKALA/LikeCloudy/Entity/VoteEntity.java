package com.SKALA.LikeCloudy.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "Vote")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private LocalDate voteDate;
    private LocalDateTime voteAt;

    @PrePersist
    public void prePersist() {
        this.voteAt = LocalDateTime.now();
        if (this.voteDate == null) {
            this.voteDate = LocalDate.now();
        }
    }
}