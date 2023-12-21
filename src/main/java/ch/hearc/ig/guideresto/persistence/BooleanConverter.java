package ch.hearc.ig.guideresto.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean aBoolean) {
        if (aBoolean == null) {
            return null;
        }
        return Boolean.TRUE.equals(aBoolean) ? "T" : "F";
    }

    @Override
    public Boolean convertToEntityAttribute(String s) {
        return s == null ? null : "T".equals(s);
    }
}
