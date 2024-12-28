package com.example.Mapping;

import java.util.List;

public class FieldSetVisitor implements SQLFieldVisitor {
    private List<Object> objects;
    private List<Object> objectStack;

    FieldSetVisitor(List<Object> queryObject, Object rootObject) {
        this.objects = queryObject;
        this.objectStack.add(rootObject);
    }

    @Override
    public void visit(SimpleField field) {
        int index = field.getIndex();
        try {
            field.getField().set(field, objects.get(index));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
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
        List<SQLField> fields = field.getFields();

        for (SQLField f : fields) {
            f.accept(this);
        }
    }

    public boolean needNewObject() {
        return objects.size() == 0;
    }
}
