package fpt.org.inblue.service;

import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.dto.request.CreateCompanyRequest;
import fpt.org.inblue.model.dto.request.UpdateCompanyRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CompanyService {
    Company getById(Long id);
    Company create(CreateCompanyRequest request, MultipartFile logo,
                   MultipartFile banner) throws IOException;
    Company update(UpdateCompanyRequest request, MultipartFile logo,
                   MultipartFile banner) throws IOException;
    List<Company> getAll();
        void deleteById(Long id);
}
