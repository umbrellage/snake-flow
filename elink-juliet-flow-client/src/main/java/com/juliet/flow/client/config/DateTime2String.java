package com.juliet.flow.client.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.juliet.common.core.utils.DateUtils;
import com.juliet.common.core.utils.time.JulietTimeMemo;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DateTime2String
 *
 * @author Geweilang
 * @date 2023/7/17
 */
public class DateTime2String extends JsonSerializer<LocalDateTime> {


    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null){
            gen.writeString(JulietTimeMemo.format(value, DateUtils.YYYY_MM_DD_HH_MM_SS));
        }
    }
}
