package com.aims.media;

import com.aims.dto.FieldSchema;
import com.aims.entity.Book;
import com.aims.entity.Media;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookTypeDefinition implements MediaTypeDefinition {
    @Override public String getCategory() { return "Book"; }
    @Override public Class<? extends Media> getEntityClass() { return Book.class; }
    @Override public List<FieldSchema> getFormSchema() { return Book.FORM_SCHEMA; }
}
