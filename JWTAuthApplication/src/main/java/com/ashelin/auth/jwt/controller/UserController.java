package com.ashelin.auth.jwt.controller;

import com.ashelin.auth.jwt.dto.SearchUser;
import com.ashelin.auth.jwt.dto.UserRequest;
import com.ashelin.auth.jwt.dto.UserResponse;
import com.ashelin.auth.jwt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/create")
    public ResponseEntity<Void> createUser(@RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Set<UserResponse>> searchUsers(@ModelAttribute SearchUser searchUser) {
        return userService.searchUsers(searchUser);
    }

    @PatchMapping(value = "/update/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        return userService.updateUser(id, userRequest);
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}