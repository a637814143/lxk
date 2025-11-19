package com.example.campus.controller;

import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.service.CompanyService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/companies")
@RequiredArgsConstructor
public class PublicCompanyController {

    private final TsukiCompanyRepository companyRepository;
    private final CompanyService companyService;

    @GetMapping
    public List<CompanyResponse> listApproved(@RequestParam(name = "q", required = false) String keyword) {
        return companyRepository.findByAuditStatus("approved").stream()
                .filter(c -> keyword == null || keyword.isBlank()
                        || (c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(keyword.toLowerCase())))
                .map(c -> companyService.findById(c.getId()))
                .collect(Collectors.toList());
    }
}
