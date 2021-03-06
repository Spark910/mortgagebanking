package com.bank.retailbanking.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.retailbanking.constants.ApplicationConstants;
import com.bank.retailbanking.dto.FundTransferRequestDto;
import com.bank.retailbanking.dto.FundTransferResponseDto;
import com.bank.retailbanking.dto.MortgageAccountSummaryResponse;
import com.bank.retailbanking.dto.MortgageAccountSummaryResponsedto;
import com.bank.retailbanking.dto.TransactionResponse;
import com.bank.retailbanking.dto.TransactionResponsedto;
import com.bank.retailbanking.entity.Customer;
import com.bank.retailbanking.entity.CustomerAccountDetail;
import com.bank.retailbanking.entity.CustomerTransaction;
import com.bank.retailbanking.exception.AmountInvalidException;
import com.bank.retailbanking.exception.CreditAccountNotFoundException;
import com.bank.retailbanking.exception.DebitAccountNotFoundException;
import com.bank.retailbanking.exception.GeneralException;
import com.bank.retailbanking.exception.MinimumBalanceException;
import com.bank.retailbanking.exception.SameAccountNumberException;
import com.bank.retailbanking.repository.CustomerAccountDetailRepository;
import com.bank.retailbanking.repository.CustomerRepository;
import com.bank.retailbanking.repository.CustomerTransactionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CustomerAccountDetailRepository customerAccountDetailRepository;

	@Autowired
	CustomerTransactionRepository customerTransactionRepository;
	/**
	 * This method enables to transfer funds b/w two accounts
	 * 
	 * @author Muthu
	 * @param fundTransferRequestDto
	 * @return FundTransferResponseDto
	 * @throws MinimumBalanceException
	 * @throws AmountInvalidException
	 * @throws DebitAccountNotFoundException
	 * @throws CreditAccountNotFoundException
	 * @throws SameAccountNumberException
	 */
	@Override
	public Optional<FundTransferResponseDto> fundTransfer(FundTransferRequestDto fundTransferRequestDto)
			throws DebitAccountNotFoundException, AmountInvalidException, MinimumBalanceException,
			CreditAccountNotFoundException, SameAccountNumberException {
		FundTransferResponseDto fundTransferResponseDto = new FundTransferResponseDto();
		Optional<CustomerAccountDetail> customerAccountDetail = customerAccountDetailRepository
				.findByAccountNumberAndAccountType(fundTransferRequestDto.getDebitedAccount(),
						ApplicationConstants.SAVINGSACCOUNT_MESSAGE);
		if (customerAccountDetail.isPresent()) {
			Optional<Customer> customerDetail = customerRepository
					.findByCustomerId(customerAccountDetail.get().getCustomerId().getCustomerId());
			if (customerDetail.isPresent()) {
				Optional<CustomerAccountDetail> creditAccount = customerAccountDetailRepository
						.findByCustomerIdAndAccountTypeAndAccountNumber(customerDetail.get(),
								ApplicationConstants.MORTGAGE_ACCOUNT, fundTransferRequestDto.getCreditedAccount());
				if (creditAccount.isPresent()) {
					Double availableBalance = customerAccountDetail.get().getAvailableBalance();
					Double transactionAmount = fundTransferRequestDto.getAmount();
					Double creditBeforeAmount = creditAccount.get().getAvailableBalance();
					if (transactionAmount < availableBalance) {
						Double afterTransactionBalance = availableBalance - transactionAmount;
						if (afterTransactionBalance > ApplicationConstants.MINIMUMBALANCE) {
							CustomerTransaction debitAction = new CustomerTransaction();
							debitAction.setAccountNumber(customerAccountDetail.get());
							Double debitBalance = availableBalance - transactionAmount;
							creditAccount.get().setAvailableBalance(debitBalance);
							debitAction.setTransactionAmount(transactionAmount);
							debitAction.setTransactionComments(fundTransferRequestDto.getRemarks());
							debitAction.setTransactionDate(LocalDate.now());
							debitAction.setTransactionStatus(ApplicationConstants.SUCCESSFULLY_CREDITED+creditAccount.get().getAccountNumber());
							debitAction.setTransactionType(ApplicationConstants.DEBIT);
							customerTransactionRepository.save(debitAction);
							customerAccountDetail.get().setAvailableBalance(afterTransactionBalance);
							customerAccountDetailRepository.save(customerAccountDetail.get());

							CustomerTransaction creditAction = new CustomerTransaction();
							creditAction.setAccountNumber(creditAccount.get());
							creditAction.setTransactionAmount(transactionAmount);
							creditAction.setTransactionComments(fundTransferRequestDto.getRemarks());
							creditAction.setTransactionDate(LocalDate.now());
							creditAction.setTransactionStatus(ApplicationConstants.SUCCESSFULLY_DEBITED+customerAccountDetail.get().getAccountNumber());
							creditAction.setTransactionType(ApplicationConstants.CREDIT);
							customerTransactionRepository.save(creditAction);
							Double creditBalance = creditBeforeAmount + transactionAmount;
							creditAccount.get().setAvailableBalance(creditBalance);
							customerAccountDetailRepository.save(creditAccount.get());
							return (Optional.of(fundTransferResponseDto));
						}
						throw new MinimumBalanceException(ApplicationConstants.AMOUNT_MINIMUMBALANCE);
					}
					throw new AmountInvalidException(ApplicationConstants.AMOUNT_AVAILABLEBALANCE);
				}
				Optional<CustomerAccountDetail> creditedAccount = customerAccountDetailRepository
						.findByAccountTypeAndAccountNumber(ApplicationConstants.SAVINGSACCOUNT_MESSAGE,
								fundTransferRequestDto.getCreditedAccount());
				if (creditedAccount.isPresent()) {
					if (customerAccountDetail.get().getAccountNumber() != creditedAccount.get().getAccountNumber()) {
						Double availableBalance = customerAccountDetail.get().getAvailableBalance();
						Double transactionAmount = fundTransferRequestDto.getAmount();
						Double creditBeforeAmount = creditedAccount.get().getAvailableBalance();
						if (transactionAmount < availableBalance) {
							Double afterTransactionBalance = availableBalance - transactionAmount;
							if (afterTransactionBalance > ApplicationConstants.MINIMUMBALANCE) {
								CustomerTransaction debitAction = new CustomerTransaction();
								debitAction.setAccountNumber(customerAccountDetail.get());
								debitAction.setTransactionAmount(transactionAmount);
								debitAction.setTransactionComments(fundTransferRequestDto.getRemarks());
								debitAction.setTransactionDate(LocalDate.now());
								debitAction.setTransactionStatus(ApplicationConstants.SUCCESSFULLY_CREDITED+creditedAccount.get().getAccountNumber());
								debitAction.setTransactionType(ApplicationConstants.DEBIT);
								customerTransactionRepository.save(debitAction);
								customerAccountDetail.get().setAvailableBalance(afterTransactionBalance);
								customerAccountDetailRepository.save(customerAccountDetail.get());

								CustomerTransaction creditAction = new CustomerTransaction();
								creditAction.setAccountNumber(creditedAccount.get());
								creditAction.setTransactionAmount(transactionAmount);
								creditAction.setTransactionComments(fundTransferRequestDto.getRemarks());
								creditAction.setTransactionDate(LocalDate.now());
								creditAction.setTransactionStatus(ApplicationConstants.SUCCESSFULLY_DEBITED+customerAccountDetail.get().getAccountNumber());
								creditAction.setTransactionType(ApplicationConstants.CREDIT);
								customerTransactionRepository.save(creditAction);
								Double creditBalance = creditBeforeAmount + transactionAmount;
								creditedAccount.get().setAvailableBalance(creditBalance);
								customerAccountDetailRepository.save(creditedAccount.get());
								return (Optional.of(fundTransferResponseDto));
							}
							throw new MinimumBalanceException(ApplicationConstants.AMOUNT_MINIMUMBALANCE);
						}
						throw new AmountInvalidException(ApplicationConstants.AMOUNT_AVAILABLEBALANCE);
					}
					throw new SameAccountNumberException(ApplicationConstants.SAMEACCOUNT_MESSAGE);
				}
				throw new CreditAccountNotFoundException(ApplicationConstants.CREDITACCOUNT_NOTFOUND);
			}
			throw new DebitAccountNotFoundException(ApplicationConstants.DEBITACCOUNT_NOTFOUND);
		}
		throw new DebitAccountNotFoundException(ApplicationConstants.DEBITACCOUNT_NOTFOUND);
	}
	/**
	 * @author Bindushree H N
	 * @Description This method is used to get the customer account summary which
	 *              includes account details of savings and mortgage account
	 * @param customerId Eg:{1001}
	 * @return MortgageAccountSummaryResponsedto
	 * @throws GeneralException
	 */
	@Override
	public MortgageAccountSummaryResponsedto getAccountSummary(Long customerId) throws GeneralException {
		log.info("Entering into AccountSummaryServiceImplementation--------getAccountSummary() Method");
		Optional<Customer> customerDetails = customerRepository.findById(customerId);
		MortgageAccountSummaryResponsedto mortgageAccountSummaryResponsedto = new MortgageAccountSummaryResponsedto();
		if (!customerDetails.isPresent()) {
			throw new GeneralException("Invalid customer");
		}
		List<CustomerAccountDetail> customerAccountDetails = customerAccountDetailRepository
				.findAllByCustomerId(customerDetails.get());

		List<MortgageAccountSummaryResponse> mortgageAccountSummaryResponses = new ArrayList<>();

		for (CustomerAccountDetail customerAccountDetail : customerAccountDetails) {
			MortgageAccountSummaryResponse mortgageAccountSummaryResponse = new MortgageAccountSummaryResponse();
			mortgageAccountSummaryResponse.setAccountBalance(customerAccountDetail.getAvailableBalance());
			mortgageAccountSummaryResponse.setAccountNumber(customerAccountDetail.getAccountNumber());
			mortgageAccountSummaryResponse.setAccountType(customerAccountDetail.getAccountType());
			mortgageAccountSummaryResponses.add(mortgageAccountSummaryResponse);
		}
		mortgageAccountSummaryResponsedto.setAccountDetails(mortgageAccountSummaryResponses);
		return mortgageAccountSummaryResponsedto;
	}

	/**
	 * @author Chethana
	 * 
	 * This Method is used to get all the transactions done for an particular account
	 * @param customerId TransactionResponsedto returns list of transaction details
	 * @return TransactionResponsedto
	 * @throws GeneralException
	 * 
	 */
	public TransactionResponsedto getTransactionSummary(Long customerId) throws GeneralException {
		log.info("Entering into TransactionServiceImpl--------getTransactionSummary() Method");
		Optional<Customer> customerDetails = customerRepository.findById(customerId);
		TransactionResponsedto transactionResponsedto = new TransactionResponsedto();
		if (!customerDetails.isPresent()) {
			throw new GeneralException("Invalid customer");
		}
		Optional<CustomerAccountDetail> customerAccountDetails = customerAccountDetailRepository
				.findTop10ByCustomerIdAndAccountTypeOrderByCustomerIdDesc(customerDetails.get(), "Savings");
		if (!customerAccountDetails.isPresent()) {
			throw new GeneralException("No valid Account is available for the customer");
		}

		List<TransactionResponse> transactionResponseList = new ArrayList<>();

		List<CustomerTransaction> customerTransactionsList = customerTransactionRepository
				.findByAccountNumber(customerAccountDetails.get());
		customerTransactionsList.forEach(customerTransaction -> {
			TransactionResponse transactionResponse = new TransactionResponse();
			BeanUtils.copyProperties(customerTransaction, transactionResponse);
			transactionResponseList.add(transactionResponse);
		});
		transactionResponsedto.setTransactions(transactionResponseList);

		return transactionResponsedto;
	}

}
