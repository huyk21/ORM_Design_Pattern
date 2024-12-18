package com.example;

import java.util.List;

import com.example.Mapping.TableMapping;

public interface QueryStrategy {
    public String toString(List<TableMapping> tables);
}
