package com.juliet.flow.client.config;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.juliet.common.core.utils.DateUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * String2DateTime
 *
 * @author Geweilang
 * @date 2023/7/18
 */
public class String2DateTimeDes extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String date = p.getValueAsString();
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateUtils.YYYY_MM_DD_HH_MM_SS);
        return LocalDateTime.parse(date, formatter);
    }
}
