package com.example.braintree;

import com.braintreegateway.PaymentMethod;
import com.braintreegateway.PaymentMethodRequest;
import com.braintreegateway.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentMethodController {

    @PostMapping
    public ResponseEntity create(@RequestBody PaymentMethodRequest request) {
        Result<? extends PaymentMethod> result = Gateway.instance().paymentMethod().create(request);
        CommonUtils.logJson(result);
        if (result.isSuccess()) {
            // See result.getTarget() for details
            return ResponseEntity.ok(result.getTarget());
        } else {
            // Handle errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
        }
    }
}
