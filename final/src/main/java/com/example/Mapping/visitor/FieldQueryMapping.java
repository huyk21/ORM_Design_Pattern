package com.example.Mapping.visitor;

import java.util.ArrayList;
import java.util.List;

import com.example.Mapping.field.OneToManyField;
import com.example.Mapping.field.OneToOneField;
import com.example.Mapping.field.ParentInterface;
import com.example.Mapping.field.SQLField;
import com.example.Mapping.field.SimpleField;
import com.example.Mapping.field.TableMapping;
import com.example.annotation.Column;
import com.example.annotation.OneToOne;
import com.example.annotation.Table;


public class FieldQueryMapping {
    private NameMapping nameMapping;

    protected FieldQueryMapping(FieldNameMapping nameMapping) {
        this.nameMapping = new NameMapping(nameMapping);
    }

    public String getName(SQLField field) {
        return nameMapping.getName(field);
    }

    public void setIndexMapping(TableMapping<?> mapping) {
        IndexMapping indexMapping = new IndexMapping();
        indexMapping.setIndex(mapping);
    }

    public String getQueryString(TableMapping<?> mapping) {
        QueryStringMapping queryMapping = new QueryStringMapping(nameMapping);
        return queryMapping.toQueryString(mapping);
    }

    public String getFromString(TableMapping<?> mapping) {
        FromStringMapping fromMapping = new FromStringMapping(nameMapping);
        return fromMapping.toFromString(mapping);
    }
}


class NameMapping implements SQLFieldVisitor {
    private FieldNameMapping nameMapping;
    private String returnString;

    protected NameMapping(FieldNameMapping nameMapping) {
        this.nameMapping = nameMapping;
    }

    public String getName(SQLField field) {
        field.accept(this);
        return returnString;
    }

    @Override
    public void visit(SimpleField field) {
        var annotation = field.getField().getAnnotation(Column.class);
        if (annotation == null) {
            returnString = nameMapping.map(field.getField().getName());
            return;
        }

        returnString = annotation.name();
        if (returnString == "") {
            returnString = nameMapping.map(field.getField().getName());
        }
    }

    private String getTableName(Class<?> clazz) {
        var annotation = clazz.getAnnotation(Table.class);
        if (annotation == null) {
            return nameMapping.map(clazz.getSimpleName());
        }

        String returnString = annotation.name();
        if (returnString == "") {
            return nameMapping.map(clazz.getSimpleName());
        }

        return returnString;
    }

    @Override
    public void visit(OneToOneField field) {
        returnString = getTableName(field.getFieldClass());
    }

    @Override
    public void visit(OneToManyField field) {
        returnString = getTableName(field.getFieldClass());
    }

    @Override
    public void visit(TableMapping<?> field) {
        returnString = getTableName(field.getClazz());
    }
}

class IndexMapping implements SQLFieldVisitor {
    private int index;

    public void setIndex(TableMapping<?> mapping) {
        index = 1;
        mapping.accept(this);
    }

    @Override
    public void visit(SimpleField field) {
        field.setIndex(index);
        index++;
    }

    private void traverse(List<SQLField> fields) {
        for (var field : fields) {
            field.accept(this);
        }
    }

    @Override
    public void visit(OneToOneField field) {
        traverse(field.getFields());
    }

    @Override
    public void visit(OneToManyField field) {
        traverse(field.getFields());
    }

    @Override
    public void visit(TableMapping<?> field) {
        traverse(field.getSQLFields());
    }    
}

class QueryStringMapping implements SQLFieldVisitor {
    private List<SimpleField> queryList;
    private NameMapping nameMapping;

    protected QueryStringMapping(NameMapping nameMapping) {
        queryList = new ArrayList<>();
        this.nameMapping = nameMapping;
    }

    public String toQueryString(TableMapping<?> mapping) {
        queryList.clear();
        visit(mapping);
        
        String returnString = "";

        queryList.sort((SimpleField a, SimpleField b) -> {
            return a.getIndex() - b.getIndex();
        });

        for (var item : queryList) {
            returnString += item.getParent().getMappingName() + "." + nameMapping.getName(item) + ", ";
        }

        return returnString.substring(0, returnString.length() - 2);
    }

    @Override
    public void visit(SimpleField field) {
        queryList.add(field);
    }

    private void traverse(List<SQLField> fields) {
        for (var field : fields) {
            field.accept(this);
        }
    }

    @Override
    public void visit(OneToOneField field) {
        if (!field.hasJoin()) {
            return;
        }

        traverse(field.getFields());
    }

    @Override
    public void visit(OneToManyField field) {
        if (!field.hasJoin()) {
            return;
        }

        traverse(field.getFields());
    }

    @Override
    public void visit(TableMapping<?> field) {
        traverse(field.getSQLFields());
    }    
}

class FromStringMapping implements SQLFieldVisitor {
    private String returnString;
    private NameMapping nameMapping;

    protected FromStringMapping(NameMapping nameMapping) {
        this.nameMapping = nameMapping;
    }

    public String toFromString(TableMapping<?> mapping) {
        returnString = "";
        visit(mapping);
        
        return returnString;
    }

    @Override
    public void visit(SimpleField field) {}

    @Override
    public void visit(OneToOneField field) {
        ParentInterface parent = field.getParent();
        String tableName = nameMapping.getName(field);
        String mappingName = field.getMappingName();

        returnString += " join " + tableName + " " + mappingName;

        if (field.hasJoin()) {
            if (field.getField().getAnnotation(OneToOne.class).refInOther()) {
                returnString += " on " + parent.getMappingName() + "." + parent.getIdColumnName() 
                    + " = " + mappingName + "." + field.getRefColumnName();
            }
            else {
                returnString += " join " + tableName + " " + mappingName 
                    + " on " + parent.getMappingName() + "." + field.getRefColumnName() 
                    + " = " + mappingName + "." + field.getIdColumnName();
            }     
        }
    }

    @Override
    public void visit(OneToManyField field) {
        if (field.hasJoin()) {
            ParentInterface parent = field.getParent();
            String tableName = field.getTableName();
            String mappingName = field.getMappingName();

            returnString += " join " + tableName + " " + mappingName
                    + " on " + parent.getMappingName() + "." + parent.getIdColumnName() 
                    + " = " + mappingName + "." + field.getRefColumnName();
                
        }
    }

    @Override
    public void visit(TableMapping<?> field) {}    
}