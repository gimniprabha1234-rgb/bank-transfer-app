
package com.example.bank_transfer_app.service;

import com.example.bank_transfer_app.model.Customer;
import com.example.bank_transfer_app.model.BankTransaction;
import com.example.bank_transfer_app.model.TransactionStatus;

import com.example.bank_transfer_app.repository.CustomerRepository;
import com.example.bank_transfer_app.repository.BankTransactionRepository;

import com.example.bank_transfer_app.exception.AccountNotFoundException;
import com.example.bank_transfer_app.exception.InsufficientBalanceException;
import com.example.bank_transfer_app.exception.BankException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    // Search customers
    public List<Customer> searchCustomers(String keyword) {
        return customerRepository.searchByName(keyword);
    }

    // Get all transactions
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    // Get account history
    public List<BankTransaction> getAccountHistory(String account) {
        return transactionRepository.findHistory(account);
    }

    // Get total balance
    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total == null ? BigDecimal.ZERO : total;
    }

    // Get successful transaction count
    public long getSuccessCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.SUCCESS);
    }

    // Get failed transaction count
    public long getFailedCount() {
        return transactionRepository.countByStatus(
                TransactionStatus.FAILED);
    }

    // Create customer
    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customerRepository.countAccounts(
                customer.getAccountNumber()) > 0) {
            throw new BankException("Account already exists");
        }

        if (customer.getEmail() != null &&
                customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new BankException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    // Transfer money
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(
            String fromAccount,
            String toAccount,
            BigDecimal amount,
            boolean simulateFailure) {

        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Amount must be greater than zero");
        }

        // Validate accounts
        if (fromAccount.equals(toAccount)) {
            throw new BankException("Accounts must be different");
        }

        customerRepository
                .getByAccountNumber(fromAccount)
                .orElseThrow(() ->
                        new AccountNotFoundException("Sender account not found"));

        customerRepository
                .getByAccountNumber(toAccount)
                .orElseThrow(() ->
                        new AccountNotFoundException("Receiver account not found"));

        // Withdraw money
        int debited = customerRepository.debit(fromAccount, amount);

        if (debited == 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance");
        }

        // Simulate system failure
        if (simulateFailure) {
            throw new BankException(
                    "All changes were rolled back");
        }

        // Deposit money
        int credited = customerRepository.credit(toAccount, amount);

        if (credited == 0) {
            throw new AccountNotFoundException(
                    "Receiver account not found");
        }

        // Save successful transaction
        BankTransaction transaction = BankTransaction.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .description("Bank transfer")
                .status(TransactionStatus.SUCCESS)
                .build();

        return transactionRepository.save(transaction);
    }
}
