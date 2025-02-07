package com.ashelin.auth.jwt.service;

import com.ashelin.auth.jwt.dto.SearchUser;
import com.ashelin.auth.jwt.dto.UserRequest;
import com.ashelin.auth.jwt.dto.UserResponse;
import com.ashelin.auth.jwt.enums.UserRole;
import com.ashelin.auth.jwt.exception.UserNotFoundException;
import com.ashelin.auth.jwt.model.User;
import com.ashelin.auth.jwt.repository.RefreshTokenRepository;
import com.ashelin.auth.jwt.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<Void> createUser(UserRequest userRequest) {

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .userRole(UserRole.USER)
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .build();

        userRepository.save(newUser);

        log.info("User with id:{} successfully created", newUser.getId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<Set<UserResponse>> searchUsers(SearchUser searchUser) {
        Set<User> users = userRepository.search(
                searchUser.getFirstName(),
                searchUser.getLastName(),
                searchUser.getId(),
                searchUser.getEmail());

        if(users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Set<UserResponse> userResponses = users.stream().map(this::mapToUserResponse).collect(Collectors.toSet());
        return ResponseEntity.ok().body(userResponses);
    }

    @Transactional
    public ResponseEntity<Void> updateUser(Long id, UserRequest userRequest) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id:" + id + " not found"));

        assignValueFromRequestToEntityIfNotNull(userRequest.getFirstName(), existingUser::setFirstName);
        assignValueFromRequestToEntityIfNotNull(userRequest.getLastName(), existingUser::setLastName);
        assignValueFromRequestToEntityIfNotNull(userRequest.getEmail(), existingUser::setEmail);
        assignValueFromRequestToEntityIfNotNull(userRequest.getPassword(), password -> existingUser.setPassword(passwordEncoder.encode(password)));
        assignValueFromRequestToEntityIfNotNull(userRequest.getUserRole(), existingUser::setUserRole);

        userRepository.save(existingUser);

        log.info("User with id:{} successfully updated", id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Transactional
    public ResponseEntity<Void> deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id:" + id + " not found");
        }

        refreshTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);

        log.info("User with id:{} successfully deleted", id);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private <T> void assignValueFromRequestToEntityIfNotNull(T requestValue, Consumer<T> consumer) {

        if (requestValue != null) {
            consumer.accept(requestValue);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRole(user.getUserRole())
                .email(user.getEmail())
                .creationTimestamp(user.getCreationTimestamp())
                .modificationTimestamp(user.getModificationTimestamp())
                .build();
    }
}