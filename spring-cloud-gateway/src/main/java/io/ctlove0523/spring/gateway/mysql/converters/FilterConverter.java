package io.ctlove0523.spring.gateway.mysql.converters;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import io.ctlove0523.spring.gateway.util.JacksonUtil;

import org.springframework.cloud.gateway.filter.FilterDefinition;

/**
 * @author chentong
 */
@Converter(autoApply = true)
public class FilterConverter implements AttributeConverter<List<FilterDefinition>, String> {
    @Override
    public String convertToDatabaseColumn(List<FilterDefinition> attribute) {
        if (attribute ==null || attribute.isEmpty()) {
            return null;
        }
        return JacksonUtil.list2String(attribute);
    }

    @Override
    public List<FilterDefinition> convertToEntityAttribute(String dbData) {
        if (dbData == null|| dbData.isEmpty()) {
            return new ArrayList<>();
        }
        return JacksonUtil.string2List(dbData, FilterDefinition.class);
    }
}
