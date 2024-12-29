package com.example.Mapping.visitor;

import com.example.Mapping.field.OneToManyField;
import com.example.Mapping.field.OneToOneField;
import com.example.Mapping.field.RelationField;
import com.example.Mapping.field.SimpleField;
import com.example.Mapping.field.TableMapping;

public class JoinVisitor implements SQLFieldVisitor {
    private String joinString;

    protected JoinVisitor() {
        joinString = "";
    }

    @Override
    public void visit(SimpleField field) {
        return;
    }

    public void visit(RelationField field) {
        if (!field.hasJoin())
            return;

        /* 
        List<SQLField> fields = field.getFields();
        SQLField idField = field.getFields().get(field.getIdFieldIndex());

        
        for (var f : fields) {
            SQLField refField = field.getFields().get();
            SQLField refIdField = refField.get
        }

        joinString += " join " + field.getTableName() + " " + field.getMappingName() 
            +  " on " + field.getParentName() + "." + field.getFieldName() + " = " + field.getTableName() + "." + field.getJoinField() + " ";
        */
    }

    @Override
    public void visit(OneToOneField field) {

    }

    @Override
    public void visit(OneToManyField field) {

    }

    @Override
    public void visit(TableMapping<?> field) {
        return;
    }

    public String getJoinString() {
        return joinString;
    }
}
