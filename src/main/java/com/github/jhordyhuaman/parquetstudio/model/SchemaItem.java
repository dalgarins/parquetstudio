package com.github.jhordyhuaman.parquetstudio.model;

import java.util.List;

public class SchemaItem {
    public String name;
    public Object type;

    public SchemaItem(String name, Object type) {
        this.name = name;
        this.type = type;

        applyStandartType();
    }
    @Override
    public String toString() {
        return "%s (%s)".formatted(name, type.toString());
    }
    public String equivalentType(String type){
        if(type == null) return "string";

        return switch (type.toLowerCase()) {
            case "timestamp_millis" -> "timestamp";
            case "int32" -> "integer";
            case "int64" -> "bigint";
            default -> type;
        };
    }
    public void applyStandartType(){
        boolean isList = type instanceof List;
        if(!isList) {
            type = equivalentType(type.toString());
            return;
        }

        List<String> lisType = (List<String>) type;
        String typeFounded = lisType.stream().filter( x -> !x.equals("null")).findFirst().orElse(null);
        type = equivalentType(typeFounded);
    }
}
