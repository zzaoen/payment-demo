package com.example.braintree;

import com.braintreegateway.*;
import com.braintreegateway.Transaction.Status;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
@Controller
public class CheckoutController {
  @Autowired PaymentMethodController paymentMethodController;

  @Autowired CustomerController customerController;

  private BraintreeGateway gateway = Gateway.instance();
  ObjectMapper om = new ObjectMapper();

  private Status[] TRANSACTION_SUCCESS_STATUSES =
      new Status[] {
        Transaction.Status.AUTHORIZED,
        Transaction.Status.AUTHORIZING,
        Transaction.Status.SETTLED,
        Transaction.Status.SETTLEMENT_CONFIRMED,
        Transaction.Status.SETTLEMENT_PENDING,
        Transaction.Status.SETTLING,
        Transaction.Status.SUBMITTED_FOR_SETTLEMENT
      };

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String root(Model model) {
    return "redirect:checkouts";
  }

  @RequestMapping(value = "/checkouts", method = RequestMethod.GET)
  public String checkout(Model model) {
    String clientToken = gateway.clientToken().generate();
    model.addAttribute("clientToken", clientToken);
    return "checkouts/new";
  }

  @RequestMapping(value = "/checkouts/applepay", method = RequestMethod.GET)
  public String checkoutApplePay(Model model) {
    String clientToken = gateway.clientToken().generate();
    model.addAttribute("clientToken", clientToken);
    return "checkouts/applepay";
  }

  /**
   * Non drop-in ApplePay, work in sandbox
   *
   * @param model
   * @return
   */
  @RequestMapping(value = "/checkouts/applepay2", method = RequestMethod.GET)
  public String checkoutApplePay2(Model model) {
    String clientToken =
        gateway.clientToken().generate(new ClientTokenRequest().merchantAccountId("VIagogoUSD"));
    model.addAttribute("clientToken", clientToken);
    return "checkouts/applepay2";
  }

  @RequestMapping(value = "/checkouts/googlepay", method = RequestMethod.GET)
  public String checkoutGoolePay(Model model) {
    String clientToken = gateway.clientToken().generate();
    model.addAttribute("clientToken", clientToken);
    return "checkouts/googlepay";
  }

  @RequestMapping(value = "/checkouts/googlepay2", method = RequestMethod.GET)
  public String checkoutGoolePay2(Model model) {
    String clientToken = gateway.clientToken().generate();
    model.addAttribute("clientToken", clientToken);
    return "checkouts/googlepay2";
  }

  @RequestMapping(value = "/checkouts/creditcard", method = RequestMethod.GET)
  public String checkoutCreditCard(Model model) {
    String clientToken =
        gateway.clientToken().generate(new ClientTokenRequest().merchantAccountId("VIagogoUSD"));
    model.addAttribute("clientToken", clientToken);
    return "checkouts/creditcard";
  }

  @RequestMapping(value = "/checkouts", method = RequestMethod.POST)
  public String postForm(
      @RequestParam("amount") String amount,
      @RequestParam("payment_method_nonce") String nonce,
      Model model,
      final RedirectAttributes redirectAttributes)
      throws JsonProcessingException {
    BigDecimal decimalAmount;
    try {
      decimalAmount = new BigDecimal(amount);
    } catch (NumberFormatException e) {
      redirectAttributes.addFlashAttribute(
          "errorDetails", "Error: 81503: Amount is an invalid format.");
      return "redirect:checkouts";
    }

    //        Result<PaymentMethodNonce> result =
    // gateway.paymentMethodNonce().create("PAYMENT_METHOD_TOKEN");
    //        String nonce = result.getTarget().getNonce();
    //        customerController.create(nonce, null, null);
    PaymentMethodNonce paymentMethodNonce = gateway.paymentMethodNonce().find(nonce);
    log.info("nonce: {}", nonce);
    log.info("paymentMethodNonce: {}", om.writeValueAsString(paymentMethodNonce));

    // ThreeDSecureInfo info = paymentMethodNonce.getThreeDSecureInfo();
    if (decimalAmount.compareTo(BigDecimal.TEN) >= 0) {
      return "redirect:checkouts/applepay2";
    }

    TransactionRequest request =
        new TransactionRequest()
            .amount(decimalAmount)
            // .paymentMethodToken(paymentMethodResponseEntity.getBody().getToken())
            // .merchantAccountId("sh_usd")
            .paymentMethodNonce(nonce)
            .options()
            .submitForSettlement(true)
            .threeDSecure()
            .required(false)
            .done()
            .done();

    Result<Transaction> result = gateway.transaction().sale(request);

    try {
      Transaction transaction2;
      if (result.isSuccess()) {
        transaction2 = gateway.transaction().find(result.getTarget().getId());
      } else {
        transaction2 = gateway.transaction().find(result.getTransaction().getId());
      }
      ThreeDSecureInfo info2 = transaction2.getThreeDSecureInfo();
      if (info2 == null) {
        log.info("info2 is null");
      }
    } catch (Exception e) {
      log.error("error");
    }

    if (result.isSuccess()) {
      Transaction transaction = result.getTarget();
      return "redirect:checkouts/" + transaction.getId();
    } else if (result.getTransaction() != null) {
      Transaction transaction = result.getTransaction();
      return "redirect:checkouts/" + transaction.getId();
    } else {
      String errorString = "";
      for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
        errorString += "Error: " + error.getCode() + ": " + error.getMessage() + "\n";
      }
      redirectAttributes.addFlashAttribute("errorDetails", errorString);
      return "redirect:checkouts";
    }
  }

  @RequestMapping(value = "/apple", method = RequestMethod.GET)
  public String apple(Model model) {
    String clientToken = gateway.clientToken().generate();
    model.addAttribute("clientToken", clientToken);
    //        String nonce = paymentMethodController.getNonce().getNonce();
    //        model.addAttribute("nonce", nonce);
    //        String bin = paymentMethodController.getNonce().getDetails().getBin();
    //        model.addAttribute("bin", bin);
    return "checkouts/apple";
  }

  @RequestMapping(value = "/select", method = RequestMethod.GET)
  public String select(Model model) {
    String clientToken = gateway.clientToken().generate();
    Result<PaymentMethodNonce> result = gateway.paymentMethodNonce().create("b5n2d8b");
    String nonce = result.getTarget().getNonce();
    String bin = result.getTarget().getDetails().getBin();
    model.addAttribute("clientToken", clientToken);
    model.addAttribute("nonce", nonce);
    log.info("method=select nonce={}, clientToken={}", nonce, clientToken);
    model.addAttribute("bin", bin);
    return "checkouts/exist";
  }

  @RequestMapping(value = "/selects", method = RequestMethod.POST)
  public String selectForm(
      @RequestParam("amount") String amount,
      @RequestParam("payment_method_nonce") String nonce,
      Model model,
      final RedirectAttributes redirectAttributes) {
    BigDecimal decimalAmount;
    try {
      decimalAmount = new BigDecimal(amount);
    } catch (NumberFormatException e) {
      redirectAttributes.addFlashAttribute(
          "errorDetails", "Error: 81503: Amount is an invalid format.");
      return "redirect:checkouts";
    }

    TransactionRequest request =
        new TransactionRequest()
            .amount(decimalAmount)
            .paymentMethodNonce(nonce)
            .options()
            .submitForSettlement(true)
            .done();

    Result<Transaction> result = gateway.transaction().sale(request);

    if (result.isSuccess()) {
      Transaction transaction = result.getTarget();
      return "redirect:checkouts/" + transaction.getId();
    } else if (result.getTransaction() != null) {
      Transaction transaction = result.getTransaction();
      return "redirect:checkouts/" + transaction.getId();
    } else {
      String errorString = "";
      for (ValidationError error : result.getErrors().getAllDeepValidationErrors()) {
        errorString += "Error: " + error.getCode() + ": " + error.getMessage() + "\n";
      }
      redirectAttributes.addFlashAttribute("errorDetails", errorString);
      return "redirect:checkouts";
    }
  }

  @RequestMapping(value = "/checkouts/{transactionId}")
  public String getTransaction(@PathVariable String transactionId, Model model) {
    Transaction transaction;
    CreditCard creditCard;
    Customer customer;
    ThreeDSecureInfo threeDSInfo;
    try {
      transaction = gateway.transaction().find(transactionId);
      creditCard = transaction.getCreditCard();
      customer = transaction.getCustomer();
      threeDSInfo = transaction.getThreeDSecureInfo();
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      return "redirect:/checkouts";
    }

    if (null != threeDSInfo) {
      log.info(
          "3DS info {} {} {} {}",
          threeDSInfo.getEnrolled(),
          threeDSInfo.isLiabilityShifted(),
          threeDSInfo.isLiabilityShiftPossible(),
          threeDSInfo.getStatus());
    }
    model.addAttribute(
        "isSuccess", Arrays.asList(TRANSACTION_SUCCESS_STATUSES).contains(transaction.getStatus()));
    model.addAttribute("transaction", transaction);
    model.addAttribute("creditCard", creditCard);
    model.addAttribute("customer", customer);
    model.addAttribute("threeDSInfo", threeDSInfo);
    return "checkouts/show";
  }
}
