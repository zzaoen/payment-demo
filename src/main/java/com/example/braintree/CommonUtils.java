package com.example.braintree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Bruce Zhao
 * @date 2021/8/4 17:15
 */
@Component
@Slf4j
public class CommonUtils {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static void logJson(Object object) {
    try {
      log.info(MAPPER.writeValueAsString(object));
    } catch (JsonProcessingException ignored) {
    }
  }
}
