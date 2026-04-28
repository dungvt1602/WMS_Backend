package com.project.wms.customer.service;

import com.project.wms.customer.dto.PartnerRequest;
import com.project.wms.customer.dto.PartnerResponse;
import com.project.wms.customer.entity.Customer;
import com.project.wms.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Page<PartnerResponse> getAllPartners(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public PartnerResponse createPartner(PartnerRequest request) {
        if (customerRepository.findByName(request.name()).isPresent()) {
            throw new RuntimeException("Mã đối tác đã tồn tại: " + request.name());
        }

        Customer customer = Customer.builder()
                .code(request.code())
                .name(request.name())
                .type(request.type())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .active(true)
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public PartnerResponse getPartnerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác có ID: " + id));
        return mapToResponse(customer);
    }

    @Transactional
    public PartnerResponse updatePartner(Long id, PartnerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác có ID: " + id));

        if (!customer.getName().equals(request.name()) &&
                customerRepository.findByName(request.name()).isPresent()) {
            throw new RuntimeException("Mã đối tác mới đã tồn tại: " + request.name());
        }

        customer.setCode(request.code());
        customer.setName(request.name());
        customer.setType(request.type());
        customer.setPhone(request.phone());
        customer.setEmail(request.email());
        customer.setAddress(request.address());

        return mapToResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deletePartner(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đối tác có ID: " + id));
        customer.setActive(false);
        customerRepository.save(customer);
    }

    private PartnerResponse mapToResponse(Customer customer) {
        return new PartnerResponse(
                customer.getId(),
                customer.getCode(),
                customer.getName(),
                customer.getType(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.isActive(),
                customer.getCreatedAt());
    }
}
