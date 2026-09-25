package fpt.org.inblue.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.repository.CandidateProfileRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServiceActiveFlowTest {
    @Mock
    CandidateProfileRepository repository;

    private CandidateProfileImpl service;

    @BeforeEach
    void setUp() {
        service = new CandidateProfileImpl(repository);
    }

    @Test
    void createProfilePersistsSubmittedProfile() {
        CandidateProfile profile = new CandidateProfile();
        when(repository.save(profile)).thenReturn(profile);
        assertEquals(profile, service.createProfile(profile));
        verify(repository).save(profile);
    }

    @Test
    void getProfileByUserIdUsesUserForeignKey() {
        List<CandidateProfile> profiles = List.of(new CandidateProfile());
        when(repository.findByUser_Id(7)).thenReturn(profiles);
        assertEquals(profiles, service.getProfileByUserId(7));
    }

    @Test
    void updateProfileKeepsExistingAssociationFieldsWhenOmitted() {
        User user = new User();
        CandidateProfile existing = new CandidateProfile();
        existing.setUser(user);
        existing.setApplicationId(88L);
        CandidateProfile update = new CandidateProfile();
        update.setId(4);
        when(repository.findById(4)).thenReturn(Optional.of(existing));
        when(repository.save(any(CandidateProfile.class))).thenAnswer(i -> i.getArgument(0));
        CandidateProfile saved = service.updateProfile(update);
        assertEquals(user, saved.getUser());
        assertEquals(88L, saved.getApplicationId());
    }

    @Test
    void deleteProfileDeletesAndFlushesByUser() {
        service.deleteProfile(7);
        verify(repository).deleteByUser_Id(7);
        verify(repository).flush();
    }
}
