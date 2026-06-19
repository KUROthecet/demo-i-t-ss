package com.aims.media;

import com.aims.dto.FieldSchema;
import com.aims.entity.Media;

import java.util.List;

public interface MediaTypeDefinition {
    String getCategory();
    Class<? extends Media> getEntityClass();
    List<FieldSchema> getFormSchema();
}
