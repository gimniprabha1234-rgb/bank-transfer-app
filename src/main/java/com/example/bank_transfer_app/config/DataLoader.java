
package com.example.bank_transfer_app.config;

import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {

        // Insert sample data only if the table is empty
        if (customerRepository.count() == 0) {

            Customer kamal = Customer.builder()
                    .accountNumber("100001")
                    .fullName("Kamal")
                    .email("kamal@gmail.com")
                    .balance(new BigDecimal("50000.00"))
                    .build();

            Customer nimali = Customer.builder()
                    .accountNumber("100002")
                    .fullName("Nimali")
                    .email("nimali@gmail.com")
                    .balance(new BigDecimal("25000.00"))
                    .build();

            Customer saman = Customer.builder()
                    .accountNumber("100003")
                    .fullName("Saman")
                    .email("saman@gmail.com")
                    .balance(new BigDecimal("10000.00"))
                    .build();

            customerRepository.saveAll(
                    List.of(kamal, nimali, saman)
            );

            System.out.println("Sample customers inserted!");
        }
    }
}
