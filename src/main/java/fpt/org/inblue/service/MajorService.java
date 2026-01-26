package fpt.org.inblue.service;

import fpt.org.inblue.model.Major;

import java.util.List;

public interface MajorService {
    Major getMajorById(int id);
    Major createMajor(Major major);
    Major updateMajor(Major major);
    List<Major> getAllMajors();
    boolean deleteMajor(int id);
}
