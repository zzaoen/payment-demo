package com.example.braintree;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Environment;
import org.thymeleaf.util.StringUtils;

import java.io.File;

public class Gateway {
    public static String DEFAULT_CONFIG_FILENAME = "config.properties";
    public static BraintreeGateway gateway = null;
    public static BraintreeGateway sbGateway = null;

    public static BraintreeGateway instance() {
        if (null == gateway) {
            synchronized (Gateway.class) {
                if (null == gateway) {
                    File configFile = new File(DEFAULT_CONFIG_FILENAME);
                    try {
                        if (configFile.exists() && !configFile.isDirectory()) {
                            gateway = BraintreeGatewayFactory.fromConfigFile(configFile);
                        } else {
                            gateway = BraintreeGatewayFactory.fromConfigMapping(System.getenv());
                        }
                    } catch (NullPointerException e) {
                        System.err.println("Could not load Braintree configuration from config file or system environment.");
                        System.exit(1);
                    }
                }
            }
        }
        return gateway;
    }
}
