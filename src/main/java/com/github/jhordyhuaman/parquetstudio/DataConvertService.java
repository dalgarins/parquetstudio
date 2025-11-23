package com.github.jhordyhuaman.parquetstudio;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


public interface DataConvertService {
    static final Logger LOGGER = Logger.getInstance(DataConvertService.class);

    public class SchemaItem {
        String name;
        Object type;

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

    public class SchemaItemTransform extends SchemaItem {
        Object typeTransform;

        public SchemaItemTransform(String name, Object type, Object typeDestiny) {
            super(name, type);
            this.typeTransform = typeDestiny;
        }
        @Override
        public String toString() {
            return "%s (%s -> %s)".formatted(name, type.toString(), typeTransform.toString());
        }
    }

    public class SchemaStructure{
        List<String> partitions;
        List<SchemaItem> fields;

        public SchemaItem getItem(String name) {
            return fields.stream().filter(item -> item.name.equals(name)).findFirst().orElse(null);
        }
        public void changesTypesFields(){
            fields.forEach(SchemaItem::applyStandartType);
        }

        public SchemaStructure toTransform(SchemaStructure schemaDestiny){
            SchemaStructure schema = this;
            schema.fields = new ArrayList<>(fields.stream().map(item -> {
                SchemaItem founded = schemaDestiny.getItem(item.name);
                return new SchemaItemTransform(item.name, item.type, founded != null ? founded.type : null);
            }).toList());
            LOGGER.warn("Generate schema with type to transform.");

            return schema;
        }
    }

    default public SchemaStructure schemaFromLists(List<String> listNames, List<String> listTypes) throws Exception {
        if(listNames.size() != listTypes.size()){
            LOGGER.warn("The lists of names and type no haven't the same size.");
            throw new Exception("The lists of names and type no haven't the same size.");
        }
        SchemaStructure schema = new SchemaStructure();
        schema.partitions = List.of();
        schema.fields = IntStream.range(0, listNames.size())
                .mapToObj( i -> new SchemaItem(listNames.get(i), listTypes.get(i)) )
                .toList();

        return schema;
    }

    default SchemaStructure schemaFromFile(String filePath) throws Exception {
        LOGGER.warn("Loading Schema file: " + filePath);
        SchemaStructure schemaStructure;
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            schemaStructure = gson.fromJson(reader, SchemaStructure.class);
            LOGGER.warn("Loaded Schema: \n" + schemaStructure.toString());
        }catch (JsonSyntaxException e){
            LOGGER.error(e.getMessage());

            throw new Exception(e.getMessage());
        }

        return schemaStructure;
    }

    default String convertToJsonString(Object schema) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SchemaItemTransform.class, new SchemaItemTransformSerializer())
                .setPrettyPrinting()
                .create();
        return gson.toJson(schema);
    }

    default void applyConvertTypes(ParquetData data, SchemaStructure schemaStructure) {
        List<String> columnsName = data.getColumnNames();

        for(String columName : columnsName){
            int index = columnsName.indexOf(columName);
            SchemaItemTransform schemaItem = (SchemaItemTransform) schemaStructure.getItem(columName);

            LOGGER.warn("Column %s | %s -> %s".formatted(
                    columName,
                    data.getColumnTypes().get(index),
                    schemaItem != null ? schemaItem.typeTransform : null)
            );

            if(schemaItem != null){
                if(String.valueOf(schemaItem.typeTransform).equals("null")) continue;

                data.getColumnTypes().set(index, String.valueOf(schemaItem.typeTransform));
            }
        }
    }

}
