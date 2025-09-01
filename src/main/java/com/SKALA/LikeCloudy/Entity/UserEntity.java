package com.SKALA.LikeCloudy.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slackUserId;

    private String slackUserName;

    private String teamId; // 1개의 워크스페이스만 사용시 문제 없어도 사용가능 -> 다중 스페이스 사용의 경우 필요

    private LocalDateTime joinedAt;

    public void updateTeamId(String teamId) { this.teamId = teamId; }
    public void updateUserName(String userName) { this.slackUserName = userName; }
}
