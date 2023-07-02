package com.example.braintree;

import com.braintreegateway.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class CustomerController {

    @Autowired
    PaymentMethodController paymentMethodController;

    @RequestMapping(value = "/customer", method = RequestMethod.GET)
    public String index(Model model) {
        String clientToken = Gateway.instance().clientToken().generate();
        log.info("clientToken: {}", clientToken.substring(0, 20));
        model.addAttribute("clientToken", clientToken);
        return "payment/add";
    }

    @RequestMapping(value = "/customer/new", method = RequestMethod.GET)
    public String indexNew(Model model) {
        String clientToken = Gateway.instance().clientToken().generate();
        log.info("clientToken: {}", clientToken.substring(0, 20));
        model.addAttribute("clientToken", clientToken);
        return "payment/addNew";
    }

    @RequestMapping(value = "/customer", method = RequestMethod.POST)
    public RedirectView create(@RequestParam("payment_method_nonce") String nonce, @RequestParam("threeDs") String threeDS, RedirectAttributes redir) {

        PaymentMethodNonce paymentMethodNonce = Gateway.instance().paymentMethodNonce().find(nonce);
        ThreeDSecureInfo threeDSecureInfo =  paymentMethodNonce.getThreeDSecureInfo();
        String customerId = UUID.randomUUID().toString();
        CustomerRequest customerRequest = new CustomerRequest();
        log.info("create customer with customer id : {}", customerId);
        customerRequest
                .customerId(customerId)
                .firstName("Mark")
                .lastName("Jones")
                .company("Jones Co.")
                .email("mark.jones@example.com")
                .fax("419-555-1234")
                .phone("614-555-1234")
                .website("http://example.com");

        Result<Customer> result = Gateway.instance().customer().create(customerRequest);
        if (result.isSuccess()) {
            PaymentMethodRequest paymentMethodRequest = new PaymentMethodRequest();
            paymentMethodRequest.customerId(result.getTarget().getId());
            paymentMethodRequest.paymentMethodNonce(nonce);
            ResponseEntity<PaymentMethod> paymentMethodResponseEntity = paymentMethodController.create(paymentMethodRequest);
//            Map threeDSecureInfo = null;
//            if (null != threeDS) {
//                try {
//                    threeDSecureInfo = new ObjectMapper().readValue(threeDS, Map.class);
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//            }

            RedirectView redirectView = new RedirectView("/customer/" + result.getTarget().getId(), true);
            if (null != redir) {
                redir.addFlashAttribute("threeDSInfo", threeDSecureInfo);
            }
            return redirectView;
        } else {
            // Handle errors
            RedirectView redirectView = new RedirectView("/customer/", true);
            return redirectView;
        }
    }

    @RequestMapping(value = "/customer/{id}", method = RequestMethod.GET)
    public String get(@PathVariable("id") String id, Model model) {

        Customer customer = Gateway.instance().customer().find(id);
        CreditCard creditCard = (CreditCard) customer.getPaymentMethods().get(0);
        if (null != creditCard.getVerification()) {
            ThreeDSecureInfo threeDSInfo = creditCard.getVerification().getThreeDSecureInfo();
            model.addAttribute("threeDSInfo", threeDSInfo);
        }

        model.addAttribute("creditCard", creditCard);
        model.addAttribute("customer", customer);

        return "payment/show";
    }
}
