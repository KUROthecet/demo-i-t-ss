package com.aims.media;

import com.aims.dto.FieldSchema;
import com.aims.entity.DVD;
import com.aims.entity.Media;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DvdTypeDefinition implements MediaTypeDefinition {
    @Override public String getCategory() { return "DVD"; }
    @Override public Class<? extends Media> getEntityClass() { return DVD.class; }
    @Override public List<FieldSchema> getFormSchema() { return DVD.FORM_SCHEMA; }
}
