package com.example.Mapping;


public interface SQLFieldVisitor {
    void visit(SimpleField field);
    void visit(OneToOneField field);
    void visit(OneToManyField field);
}
