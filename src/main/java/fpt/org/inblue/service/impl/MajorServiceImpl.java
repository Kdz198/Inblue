package fpt.org.inblue.service.impl;

import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.model.Major;
import fpt.org.inblue.repository.MajorRepository;
import fpt.org.inblue.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MajorServiceImpl implements MajorService {
    @Autowired
    private MajorRepository majorRepository;

    @Override
    public Major getMajorById(int id) {
        if(majorRepository.existsById(id)){
            return majorRepository.findById(id).get();
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Major createMajor(Major major) {
        return majorRepository.save(major);
    }

    @Override
    public Major updateMajor(Major major) {
        if(majorRepository.existsById(major.getId())){
            return majorRepository.save(major);
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    @Override
    public boolean deleteMajor(int id) {
        if(majorRepository.existsById(id)){
            majorRepository.deleteById(id);
            return true;
        }
        else{
            throw new CustomException("Question Major not found", HttpStatus.NOT_FOUND);
        }
    }
}
