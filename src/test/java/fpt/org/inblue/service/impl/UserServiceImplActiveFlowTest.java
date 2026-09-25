package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.UserRepository;
import fpt.org.inblue.service.ApiClient;
import fpt.org.inblue.service.CandidateProfileService;
import fpt.org.inblue.utils.SecurityUtils;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplActiveFlowTest {
    @Mock
    UserRepository repository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    CloudinaryService cloudinary;

    @Mock
    ApiClient apiClient;

    @Mock
    CandidateProfileService profileService;

    @Mock
    SecurityUtils securityUtils;

    @Mock
    PasswordEncoder encoder;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(
                repository, publisher, cloudinary, apiClient, profileService, securityUtils, encoder);
    }

    @Test
    void getAllReturnsRepositoryUsers() {
        User user = User.builder().id(1).build();
        when(repository.findAll()).thenReturn(List.of(user));
        assertEquals(List.of(user), service.getAll());
    }

    @Test
    void getByIdRejectsUnknownUser() {
        when(repository.findById(9)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(9));
    }

    @Test
    void changePasswordRejectsWrongOldPassword() {
        User user = User.builder().id(7).password("hash").build();
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        when(repository.findById(7)).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hash")).thenReturn(false);
        assertEquals(
                400,
                assertThrows(CustomException.class, () -> service.changePassword("wrong", "new"))
                        .getStatus()
                        .value());
    }

    @Test
    void changePasswordEncodesAndSavesValidPassword() {
        User user = User.builder().id(7).password("old-hash").build();
        when(securityUtils.getCurrentUserId()).thenReturn(7);
        when(repository.findById(7)).thenReturn(Optional.of(user));
        when(encoder.matches("old", "old-hash")).thenReturn(true);
        when(encoder.encode("new")).thenReturn("new-hash");
        service.changePassword("old", "new");
        assertEquals("new-hash", user.getPassword());
        verify(repository).save(user);
    }
}
