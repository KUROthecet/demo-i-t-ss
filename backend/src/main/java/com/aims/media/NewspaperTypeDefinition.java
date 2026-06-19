package com.aims.media;

import com.aims.dto.FieldSchema;
import com.aims.entity.Media;
import com.aims.entity.Newspaper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NewspaperTypeDefinition implements MediaTypeDefinition {
    @Override public String getCategory() { return "Newspaper"; }
    @Override public Class<? extends Media> getEntityClass() { return Newspaper.class; }
    @Override public List<FieldSchema> getFormSchema() { return Newspaper.FORM_SCHEMA; }
}
