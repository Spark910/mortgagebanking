package com.bank.retailbanking.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDto {
	private Integer statusCode;
	private String message;
	private Long customerID;
	private String customerName;
	private String roleType;
}
