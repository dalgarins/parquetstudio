package com.github.jhordyhuaman.parquetstudio.model;

public class SchemaItemTransform extends SchemaItem {
    public Object typeTransform;

    public SchemaItemTransform(String name, Object type, Object typeDestiny) {
        super(name, type);
        this.typeTransform = typeDestiny;
    }
    @Override
    public String toString() {
        return "%s (%s -> %s)".formatted(name, type.toString(), typeTransform.toString());
    }
}
