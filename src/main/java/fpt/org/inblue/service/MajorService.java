package fpt.org.inblue.service;

import fpt.org.inblue.model.Major;

import java.util.List;

public interface MajorService {
    Major getQuestionMajorById(int id);
    Major createQuestionMajor(Major major);
    Major updateQuestionMajor(Major major);
    List<Major> getAllQuestionMajors();
    boolean deleteQuestionMajor(int id);
}
