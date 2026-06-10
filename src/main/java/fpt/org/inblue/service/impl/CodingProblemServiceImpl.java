package fpt.org.inblue.service.impl;

import fpt.org.inblue.model.CodingProblem;
import fpt.org.inblue.repository.CodingProblemsRepository;
import fpt.org.inblue.service.CodingProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CodingProblemServiceImpl implements CodingProblemService {
    @Autowired
    private CodingProblemsRepository codingProblemsRepository;
    @Override
    public Optional<CodingProblem> findCodingProblemById(Long id) {
        return codingProblemsRepository.findById(id);
    }

    @Override
    public CodingProblem save(CodingProblem codingProblem) {
        return codingProblemsRepository.save(codingProblem);
    }

    @Override
    public List<CodingProblem> findAllCodingProblems() {
        return codingProblemsRepository.findAll();
    }
}
