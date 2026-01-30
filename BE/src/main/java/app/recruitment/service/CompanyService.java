package app.recruitment.service;

import app.auth.model.User;
import app.auth.repository.CompanyRepository;
import app.auth.repository.UserRepository;
import app.content.model.Company;
import app.recruitment.dto.request.UpdateCompanyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /**
     * Lấy thông tin công ty của Recruiter đang đăng nhập
     */
    public Company getMyCompany(Long recruiterId) {
        // Tìm công ty theo recruiterId
        // Nếu chưa có, trả về null hoặc ném lỗi tùy logic (FE sẽ cần xử lý case null)
        return companyRepository.findByRecruiterId(recruiterId).orElse(null);
    }

    /**
     * Cập nhật (hoặc tạo mới) thông tin công ty
     */
    @Transactional
    public Company updateCompany(Long recruiterId, UpdateCompanyRequest request) {
        // 1. Tìm công ty hiện tại của Recruiter
        Company company = companyRepository.findByRecruiterId(recruiterId)
                .orElseGet(() -> {
                    // 2. Nếu chưa có thì tạo mới (Create)
                    User recruiter = userRepository.findById(recruiterId)
                            .orElseThrow(() -> new RuntimeException("Recruiter không tồn tại"));
                    
                    return Company.builder()
                            .recruiter(recruiter)
                            .build();
                });

        // 3. Map dữ liệu từ Request sang Entity
        // Lưu ý: Đảm bảo class Company (Entity) đã có đủ các trường này
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setSize(request.getSize());
        company.setFoundedYear(request.getFoundedYear());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());

        // Cập nhật ảnh nếu có gửi lên (tránh ghi đè null nếu FE không gửi)
        if (request.getLogoUrl() != null) {
            company.setLogoUrl(request.getLogoUrl());
        }
        if (request.getCoverImageUrl() != null) {
            company.setCoverImageUrl(request.getCoverImageUrl());
        }

        // 4. Lưu xuống DB
        return companyRepository.save(company);
    }
}