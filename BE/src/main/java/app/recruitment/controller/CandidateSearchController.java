package app.recruitment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.recruitment.service.CandidateSearchService;
import app.auth.entity.User;

import java.util.List;

@RestController
@RequestMapping("/api/recruitment/candidates")
@RequiredArgsConstructor
public class CandidateSearchController {

    private final CandidateSearchService candidateSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<User>> search(@RequestParam(required = false) String skill,
                                             @RequestParam(required = false) Double minGpa,
                                             @RequestParam(required = false) String name) {
        List<User> result = candidateSearchService.searchCandidates(skill, minGpa, name);
        return ResponseEntity.ok(result);
    }
}
