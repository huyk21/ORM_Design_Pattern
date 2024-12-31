package com.example.Mapping.visitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.example.Mapping.field.OneToManyField;
import com.example.Mapping.field.OneToOneField;
import com.example.Mapping.field.SQLField;
import com.example.Mapping.field.SimpleField;
import com.example.Mapping.field.TableMapping;

public class FieldSetVisitor implements SQLFieldVisitor {
    private ResultSet resultSet;
    private List<Object> objectStack;

    FieldSetVisitor(ResultSet resultSet, Object rootObject) {
        this.resultSet = resultSet;
        this.objectStack.add(rootObject);
    }

    @Override
    public void visit(SimpleField field) {
        int index = field.getIndex();
        try {
            field.getField().set(field, resultSet.getObject(index));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(OneToOneField field) {
        List<SQLField> fields = field.getFields();

        for (SQLField f : fields) {
            f.accept(this);
        }
    }

    @Override
    public void visit(OneToManyField field) {
        if (!field.hasJoin())
            return;
    }

    public void visit(TableMapping<?> field) {
        List<SQLField> fields = field.getSQLFields();

        for (SQLField f : fields) {
            f.accept(this);
        }
    }
}
