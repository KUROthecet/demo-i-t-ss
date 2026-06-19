package com.aims.media;

import com.aims.dto.FieldSchema;
import com.aims.entity.CD;
import com.aims.entity.Media;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CdTypeDefinition implements MediaTypeDefinition {
    @Override public String getCategory() { return "CD"; }
    @Override public Class<? extends Media> getEntityClass() { return CD.class; }
    @Override public List<FieldSchema> getFormSchema() { return CD.FORM_SCHEMA; }
}
