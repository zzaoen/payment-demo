package com.example.braintree;

import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.example.braintree.dto.AuthorizeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.math.BigDecimal;

@RestController
public class TransactionController {

  @PostMapping("/authorize")
  public ResponseEntity Authorize(@RequestBody AuthorizeRequest authorizeRequest) {
    TransactionRequest request =
        new TransactionRequest().amount(new BigDecimal(authorizeRequest.getAmount()));

    if (!StringUtils.isEmpty(authorizeRequest.getNonce())) {
      request.paymentMethodNonce(authorizeRequest.getNonce());
    } else if (!StringUtils.isEmpty(authorizeRequest.getToken())) {
      request.paymentMethodToken(authorizeRequest.getToken());
    }

    request.options().submitForSettlement(false).done();

    Result<Transaction> result = Gateway.instance().transaction().sale(request);
    if (result.isSuccess()) {
      // See result.getTarget() for details
      return ResponseEntity.ok(result.getTarget());
    } else {
      // Handle errors
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
    }
  }

  @GetMapping("/authorize/{site}/{reference}")
  public ResponseEntity<Transaction> getAuth(@PathVariable String site, @PathVariable String reference) {
    Transaction transaction = Gateway.instance().transaction().find(reference);
    return ResponseEntity.ok(transaction);
  }

  @PostMapping("capture/{id}")
  public ResponseEntity capture(@PathVariable("id") String id) {
    Result<Transaction> result = Gateway.instance().transaction().submitForSettlement(id);

    if (result.isSuccess()) {
      // See result.getTarget() for details
      return ResponseEntity.ok(result.getTarget());
    } else {
      // Handle errors
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
    }
  }

  @PostMapping("/refund/{id}")
  public ResponseEntity refund(@PathVariable("id") String id) {
    Result<Transaction> result = Gateway.instance().transaction().refund(id);
    if (result.isSuccess()) {
      // See result.getTarget() for details
      return ResponseEntity.ok(result.getTarget());
    } else {
      // Handle errors
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
    }
  }

  @PostMapping("/void/{id}")
  public ResponseEntity voidTrans(@PathVariable("id") String id) {
    Result<Transaction> result = Gateway.instance().transaction().voidTransaction(id);
    if (result.isSuccess()) {
      // See result.getTarget() for details
      return ResponseEntity.ok(result.getTarget());
    } else {
      // Handle errors
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.getErrors());
    }
  }
}
