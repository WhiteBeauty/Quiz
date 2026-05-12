package com.Karyakina.Ustenko.social.service;

import org.springframework.security.core.Authentication;
import com.Karyakina.Ustenko.social.dto.FriendDto;
import com.Karyakina.Ustenko.social.dto.FriendRequestDto;
import com.Karyakina.Ustenko.social.dto.UserSearchHitDto;

import java.util.List;

public interface FriendshipService {

    List<UserSearchHitDto> searchUsers(String query, Authentication authentication);

    void sendFriendRequest(Long targetUserId, Authentication authentication);

    void acceptFriendRequest(Long friendshipId, Authentication authentication);

    void declineFriendRequest(Long friendshipId, Authentication authentication);

    void cancelOutgoingRequest(Long friendshipId, Authentication authentication);

    List<FriendDto> listFriends(Authentication authentication);

    List<FriendRequestDto> listIncomingRequests(Authentication authentication);

    List<FriendRequestDto> listOutgoingRequests(Authentication authentication);

    void removeFriend(Long friendUserId, Authentication authentication);

    boolean areFriends(Long userIdA, Long userIdB);
}
