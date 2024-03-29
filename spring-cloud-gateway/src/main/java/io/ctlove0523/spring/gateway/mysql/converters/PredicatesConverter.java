package io.ctlove0523.spring.gateway.mysql.converters;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.ctlove0523.spring.gateway.util.JacksonUtil;

import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;

/**
 * @author chentong
 */
@Converter(autoApply = true)
public class PredicatesConverter implements AttributeConverter<List<PredicateDefinition>, String> {

    @Override
    public String convertToDatabaseColumn(List<PredicateDefinition> attribute) {
        String jsonFormatString = JacksonUtil.list2String(attribute);
        return jsonFormatString == null ? "" : jsonFormatString;
    }

    @Override
    public List<PredicateDefinition> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        return JacksonUtil.string2List(dbData, PredicateDefinition.class);
    }
}
