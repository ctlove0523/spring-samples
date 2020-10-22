package io.ctlove0523.spring.gateway.mysql.converters;

import io.ctlove0523.spring.gateway.JacksonUtil;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chentong
 */
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
        return JacksonUtil.string2List(dbData,List<PredicateDefinition>.getClass());
    }
}
