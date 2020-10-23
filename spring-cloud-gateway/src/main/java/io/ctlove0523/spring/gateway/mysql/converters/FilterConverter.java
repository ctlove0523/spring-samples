package io.ctlove0523.spring.gateway.mysql.converters;

import io.ctlove0523.spring.gateway.JacksonUtil;
import org.springframework.cloud.gateway.filter.FilterDefinition;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

/**
 * @author chentong
 */
@Converter(autoApply = true)
public class FilterConverter implements AttributeConverter<List<FilterDefinition>, String> {
    @Override
    public String convertToDatabaseColumn(List<FilterDefinition> attribute) {
        return JacksonUtil.list2String(attribute);
    }

    @Override
    public List<FilterDefinition> convertToEntityAttribute(String dbData) {
        return JacksonUtil.string2List(dbData, FilterDefinition.class);
    }
}
