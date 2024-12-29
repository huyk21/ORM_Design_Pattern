package com.example.Mapping.visitor;

import com.example.Mapping.field.OneToManyField;
import com.example.Mapping.field.OneToOneField;
import com.example.Mapping.field.SimpleField;
import com.example.Mapping.field.TableMapping;

public interface SQLFieldVisitor {
    void visit(SimpleField field);
    void visit(OneToOneField field);
    void visit(OneToManyField field);
    void visit(TableMapping<?> field);
}
