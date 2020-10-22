package io.ctlove0523.spring.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * @author chentong
 */
public class JacksonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> String list2String(List<T> input) {
        try {
            return MAPPER.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T string2List(String input,Class<T> tClass) {
        try {
            return MAPPER.readValue(input, new TypeReference<tClass>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
