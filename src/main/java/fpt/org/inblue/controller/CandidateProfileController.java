package fpt.org.inblue.controller;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.service.CandidateProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidate-profiles")
public class CandidateProfileController {
    @Autowired
    private CandidateProfileService candidateProfileService;

    @GetMapping
    public ResponseEntity<List<CandidateProfile>> getAllProfile(){
        return ResponseEntity.ok(candidateProfileService.getAllProfiles());
    }

    @GetMapping("{userId}")
    public ResponseEntity<CandidateProfile> getByUserId(@PathVariable int userId){
        return ResponseEntity.ok(candidateProfileService.getProfileByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<CandidateProfile> createProfile(@RequestBody CandidateProfile candidateProfile){
        return ResponseEntity.ok(candidateProfileService.createProfile(candidateProfile));
    }

    @PutMapping
    public ResponseEntity<CandidateProfile> updateProfile(@RequestBody CandidateProfile candidateProfile){
        return ResponseEntity.ok(candidateProfileService.updateProfile(candidateProfile));}
}
