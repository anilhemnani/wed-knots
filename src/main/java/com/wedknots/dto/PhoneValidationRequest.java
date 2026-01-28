package com.wedknots.dto;

import lombok.Data;

@Data
public class PhoneValidationRequest {
    private String phoneNumber;
    private String countryCode;

}
