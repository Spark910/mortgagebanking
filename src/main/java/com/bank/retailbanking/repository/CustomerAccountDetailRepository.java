package com.bank.retailbanking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bank.retailbanking.entity.Customer;
import com.bank.retailbanking.entity.CustomerAccountDetail;

@Repository
public interface CustomerAccountDetailRepository extends JpaRepository<CustomerAccountDetail, Long> {

	Optional<CustomerAccountDetail> findByCustomerId(Customer customer);

	@Query("select u from CustomerAccountDetail u WHERE CAST(u.accountNumber AS string) LIKE %:accountNumber%")
	List<CustomerAccountDetail> findAllByAccountNumber(@Param("accountNumber") String accountNumber);

	Optional<CustomerAccountDetail> findByCustomerIdAndAccountType(Optional<Customer> customer, String string);

	Optional<CustomerAccountDetail> findByCustomerIdAndAccountType(Customer customer, String transactionPurpose);

	Optional<CustomerAccountDetail> findByCustomerIdAndAccountType(CustomerAccountDetail customerAccountDetail,
			String savingAccount);

	Optional<CustomerAccountDetail> findByAccountNumberAndAccountType(Long debitedAccount,
			String SAVINGSACCOUNT_MESSAGE);

	Optional<CustomerAccountDetail> findByCustomerIdAndAccountTypeAndAccountNumber(Customer customer,
			String mORTGAGE_MESSAGE, Long creditedAccount);

	Optional<CustomerAccountDetail> findByAccountTypeAndAccountNumber(String SAVINGSACCOUNT_MESSAGE,
			Long creditedAccount);


	List<CustomerAccountDetail> findAllByCustomerId(Customer customer);


	Optional<CustomerAccountDetail> findByAccountNumberAndAccountTypeNot(Long creditedAccount, String MORTGAGE_MESSAGE);

	Optional<CustomerAccountDetail> findByAccountNumber(Long accountNumber);

	Optional<CustomerAccountDetail> findTop10ByCustomerIdAndAccountTypeOrderByCustomerIdDesc(Customer customer,
			String string);


}
