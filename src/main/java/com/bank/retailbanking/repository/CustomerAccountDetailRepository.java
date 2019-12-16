package com.bank.retailbanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bank.retailbanking.entity.Customer;
import com.bank.retailbanking.entity.CustomerAccountDetail;

@Repository
public interface CustomerAccountDetailRepository extends JpaRepository<CustomerAccountDetail, Long> {

	List<CustomerAccountDetail> findAllByCustomerId(Customer customer);

	Optional<CustomerAccountDetail> findByAccountNumber(Long accountNumber);

	List<CustomerAccountDetail> findAllByAccountNumber(String accountNumber);

}
