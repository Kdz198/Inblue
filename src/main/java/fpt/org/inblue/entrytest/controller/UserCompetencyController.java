package fpt.org.inblue.entrytest.controller;

import fpt.org.inblue.entrytest.dto.response.UserCompetencyResponse;
import fpt.org.inblue.entrytest.service.UserCompetencyService;
import fpt.org.inblue.mapper.UserCompetencyMapper;
import fpt.org.inblue.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/competency")
@RequiredArgsConstructor
public class UserCompetencyController {
    private final UserCompetencyService userCompetencyService;
    private final SecurityUtils securityUtils;
    private final UserCompetencyMapper competencyMapper;

    @GetMapping
    public ResponseEntity<UserCompetencyResponse> getCurrentCompetency() {
        return ResponseEntity.ok(competencyMapper.toResponse(
                userCompetencyService.getCurrentCompetency(securityUtils.getCurrentUserId())));
    }
}
