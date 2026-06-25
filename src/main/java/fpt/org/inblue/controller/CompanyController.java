package fpt.org.inblue.controller;

import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.dto.request.CreateCompanyRequest;
import fpt.org.inblue.model.dto.request.UpdateCompanyRequest;
import fpt.org.inblue.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            encoding = {@Encoding(name = "data", contentType = "application/json")})))
    public ResponseEntity<Company> addCompany(
            @RequestPart("data") CreateCompanyRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "banner", required = false) MultipartFile banner)
            throws IOException {
        Company createdCompany = companyService.create(request, logo, banner);
        return ResponseEntity.ok(createdCompany);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            encoding = {@Encoding(name = "data", contentType = "application/json")})))
    public ResponseEntity<Company> updateCompany(
            @RequestPart("data") UpdateCompanyRequest company,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "banner", required = false) MultipartFile banner)
            throws IOException {
        Company updatedCompany = companyService.update(company, logo, banner);
        return ResponseEntity.ok(updatedCompany);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getById(id);
        return ResponseEntity.ok(company);
    }

    @GetMapping
    public ResponseEntity<?> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCompanyById(@PathVariable Long id) {
        companyService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
