
package com.example.bank_transfer_app.controller;

import com.example.bank_transfer_app.dto.TransferRequest;
import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.service.BankService;
import com.example.bank_transfer_app.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    // Display home page
    @GetMapping("/")
    public String home(Model model) {

        addHomePageData(model);

        return "bank";
    }

    // Process bank transfer
    @PostMapping("/transfer")
    public String transfer(
            @Valid @ModelAttribute("transferRequest")
            TransferRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check form validation
        if (result.hasErrors()) {

            addHomePageData(model);

            return "bank";
        }

        try {

            bankService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    request.isSimulateFailure()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfer successful!"
            );

        } catch (Exception e) {

            // Save failed transaction separately
            auditService.recordFailure(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/";
    }

    // Search customers
    @GetMapping("/search")
    public String searchCustomers(
            @RequestParam String keyword,
            Model model) {

        addHomePageData(model);

        model.addAttribute(
                "customers",
                bankService.searchCustomers(keyword)
        );

        model.addAttribute("keyword", keyword);

        return "bank";
    }

    // View account transaction history
    @GetMapping("/history/{account}")
    public String accountHistory(
            @PathVariable String account,
            Model model) {

        model.addAttribute(
                "history",
                bankService.getAccountHistory(account)
        );

        model.addAttribute("selectedAccount", account);

        return "history";
    }

    // Add new customer
    @PostMapping("/customers")
    public String createCustomer(
            @ModelAttribute Customer customer,
            RedirectAttributes redirectAttributes) {

        try {

            bankService.createCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Customer added successfully!"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/";
    }

    // Add required data to home page
    private void addHomePageData(Model model) {

        model.addAttribute(
                "customers",
                bankService.getAllCustomers()
        );

        model.addAttribute(
                "transactions",
                bankService.getLatestTransactions()
        );

        model.addAttribute(
                "totalBalance",
                bankService.getTotalBalance()
        );

        model.addAttribute(
                "successCount",
                bankService.getSuccessCount()
        );

        model.addAttribute(
                "failedCount",
                bankService.getFailedCount()
        );

        model.addAttribute(
                "transferRequest",
                new TransferRequest()
        );
    }
}
