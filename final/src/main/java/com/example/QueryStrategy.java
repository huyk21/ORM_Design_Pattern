package com.example;

import java.util.List;

import com.example.Mapping.field.TableMapping;

public interface QueryStrategy {
    public String toString(List<TableMapping> tables);
}
