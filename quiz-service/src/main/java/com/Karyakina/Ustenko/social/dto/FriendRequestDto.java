package com.Karyakina.Ustenko.social.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRequestDto {
    private Long friendshipId;
    private Long otherUserId;
    private String otherUsername;
    private String direction;
}
