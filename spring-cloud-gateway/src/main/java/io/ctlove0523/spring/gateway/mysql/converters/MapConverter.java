package io.ctlove0523.spring.gateway.mysql.converters;

import io.ctlove0523.spring.gateway.util.JacksonUtil;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chentong
 */
@Converter(autoApply = true)
public class MapConverter implements AttributeConverter<Map<String,Object>, String> {
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return JacksonUtil.map2String(attribute);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        return JacksonUtil.string2Map(dbData);
    }
}
