package com.example.braintree.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizeRequest {
    private String token;
    private String nonce;
    private String amount;
}
