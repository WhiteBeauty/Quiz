package com.Karyakina.Ustenko.social.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.Karyakina.Ustenko.social.dto.FriendDto;
import com.Karyakina.Ustenko.social.dto.FriendRequestDto;
import com.Karyakina.Ustenko.social.dto.UserSearchHitDto;
import com.Karyakina.Ustenko.social.service.FriendshipService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchHitDto>> search(
            @RequestParam("q") String q,
            Authentication authentication) {
        return ResponseEntity.ok(friendshipService.searchUsers(q, authentication));
    }

    @PostMapping("/requests")
    public ResponseEntity<Void> sendRequest(
            @RequestBody Map<String, Long> body,
            Authentication authentication) {
        Long targetUserId = body.get("targetUserId");
        if (targetUserId == null) {
            throw new IllegalArgumentException("targetUserId обязателен");
        }
        friendshipService.sendFriendRequest(targetUserId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id, Authentication authentication) {
        friendshipService.acceptFriendRequest(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable Long id, Authentication authentication) {
        friendshipService.declineFriendRequest(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/requests/{id}")
    public ResponseEntity<Void> cancelOutgoing(@PathVariable Long id, Authentication authentication) {
        friendshipService.cancelOutgoingRequest(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendDto>> listFriends(Authentication authentication) {
        return ResponseEntity.ok(friendshipService.listFriends(authentication));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestDto>> incoming(Authentication authentication) {
        return ResponseEntity.ok(friendshipService.listIncomingRequests(authentication));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestDto>> outgoing(Authentication authentication) {
        return ResponseEntity.ok(friendshipService.listOutgoingRequests(authentication));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Long userId, Authentication authentication) {
        friendshipService.removeFriend(userId, authentication);
        return ResponseEntity.noContent().build();
    }
}
