package fpt.org.inblue.service.impl;


import lombok.RequiredArgsConstructor;
import fpt.org.inblue.cloudinary.CloudinaryService;
import fpt.org.inblue.exception.CustomException;
import fpt.org.inblue.mapper.CompanyMapper;
import fpt.org.inblue.model.Company;
import fpt.org.inblue.model.dto.request.CreateCompanyRequest;
import fpt.org.inblue.model.dto.request.UpdateCompanyRequest;
import fpt.org.inblue.repository.CompanyRepository;
import fpt.org.inblue.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = Logger.getLogger(CompanyServiceImpl.class.getName());

    private final CompanyRepository companyRepository;

    private final CompanyMapper companyMapper;

    private final CloudinaryService cloudinaryService;

    @Override
    public Company getById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy công ty với ID: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public Company create(CreateCompanyRequest request, MultipartFile logo,
                          MultipartFile banner) throws IOException {
        if (request == null) {
            throw new CustomException("CreateCompanyRequest không được để trống", HttpStatus.BAD_REQUEST);
        }
        Company company = companyMapper.toEntity(request);
        if (logo != null && !logo.isEmpty()) {
            try {
                if (cloudinaryService.validate(logo)) {
                    Map<String, String> logoResult = cloudinaryService.uploadImg(logo);
                    company.setLogoUrl(logoResult.get("secure_url"));
                } else {
                    throw new CustomException("File logo không hợp lệ", HttpStatus.BAD_REQUEST);
                }
            } catch (IOException e) {
                logger.severe("Error uploading logo: " + e.getMessage());
                throw new CustomException("Lỗi tải lên logo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        if (banner != null && !banner.isEmpty()) {
            try {
                if (cloudinaryService.validate(banner)) {
                    Map<String, String> bannerResult = cloudinaryService.uploadImg(banner);
                    company.setBannerUrl(bannerResult.get("secure_url"));
                } else {
                    throw new CustomException("File logo không hợp lệ", HttpStatus.BAD_REQUEST);
                }
            } catch (IOException e) {
                logger.severe("Error uploading banner: " + e.getMessage());
                throw new CustomException("Lỗi tải lên banner: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return companyRepository.save(company);
    }

    @Override
    public Company update(UpdateCompanyRequest request, MultipartFile logo,
    MultipartFile banner) throws IOException {
        Long companyId = request.getId();
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException("Không tìm thấy công ty với ID: " + companyId, HttpStatus.NOT_FOUND));
        companyMapper.updateCompanyFromRequest(request, company);

        if (logo != null && !logo.isEmpty()) {
            try {
                if (cloudinaryService.validate(logo)) {
                    if (company.getLogoUrl() != null && !company.getLogoUrl().isEmpty()) {
                        String oldLogoPublicId = extractPublicIdFromUrl(company.getLogoUrl());
                        if (oldLogoPublicId != null) {
                            try {
                                cloudinaryService.deleteImage(oldLogoPublicId);
                            } catch (IOException e) {
                                logger.warning("Failed to delete old logo: " + e.getMessage());
                            }
                        }
                    }
                    Map<String, String> logoResult = cloudinaryService.uploadImg(logo);
                    company.setLogoUrl(logoResult.get("secure_url"));
                    logger.info("New logo uploaded successfully");
                } else {
                    logger.warning("Logo validation failed, skipping logo upload");
                }
            } catch (IOException e) {
                logger.severe("Error uploading logo: " + e.getMessage());
                throw new CustomException("Lỗi tải lên logo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        if (banner != null && !banner.isEmpty()) {
            try {
                if (cloudinaryService.validate(banner)) {
                    if (company.getBannerUrl() != null && !company.getBannerUrl().isEmpty()) {
                        String oldBannerPublicId = extractPublicIdFromUrl(company.getBannerUrl());
                        if (oldBannerPublicId != null) {
                            try {
                                cloudinaryService.deleteImage(oldBannerPublicId);
                                logger.info("Old banner deleted: " + oldBannerPublicId);
                            } catch (IOException e) {
                                logger.warning("Failed to delete old banner: " + e.getMessage());
                            }
                        }
                    }
                    Map<String, String> bannerResult = cloudinaryService.uploadImg(banner);
                    company.setBannerUrl(bannerResult.get("secure_url"));
                    logger.info("New banner uploaded successfully");
                } else {
                    logger.warning("Banner validation failed, skipping banner upload");
                }
            } catch (IOException e) {
                logger.severe("Error uploading banner: " + e.getMessage());
                throw new CustomException("Lỗi tải lên banner: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return companyRepository.save(company);
    }

    @Override
    public List<Company> getAll() {
        return companyRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CustomException("Không tìm thấy công ty với ID: " + id, HttpStatus.NOT_FOUND));
        company.setIsDeleted(true);
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            String[] parts = url.split("/upload/");
            if (parts.length > 1) {
                String publicIdWithExtension = parts[1];
                return publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf('.'));
            }
        } catch (Exception e) {
            logger.warning("Failed to extract public_id from URL: " + url);
        }
        return null;
    }
}
