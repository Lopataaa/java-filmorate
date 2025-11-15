package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Integer userId;
    private Integer friendId;
    private FriendshipStatus status;
    private LocalDateTime createdAt;

    public enum FriendshipStatus {
        PENDING,    // неподтверждённая
        CONFIRMED   // подтверждённая
    }
}
