package com.github.jhordyhuaman.parquetstudio;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;

import javax.xml.validation.Schema;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface DataConvertService {
    static final Logger LOGGER = Logger.getInstance(DataConvertService.class);

    public class SchemaItem {
        String name;
        Object type;

        @Override
        public String toString() {
            return "%s (%s)".formatted(name, type.toString());
        }
        public String equivalentType(String type){
            if(type == null) return "string";

            return switch (type) {
                case "timestamp_millis" -> "timestamp";
                case "int32" -> "integer";
                case "int64" -> "bigint";
                default -> type;
            };
        }
        public String getType(){
            boolean isList = type instanceof List;
            if(!isList) return type.toString();

            List<String> lisType = (List<String>) type;
            String typeFounded = lisType.stream().filter( x -> !x.equals("null")).findFirst().orElse(null);
            return equivalentType(typeFounded);
        }
    }

    public class SchemaStructure{
        List<String> partitions;
        List<SchemaItem> fields;

        public Optional<SchemaItem> getItem(String name) {
            return fields.stream().filter(item -> item.name.equals(name)).findFirst();
        }
    }

    default SchemaStructure readSchemaFile(String filePath) throws Exception {
        LOGGER.info("Loading Schema file: " + filePath);
        SchemaStructure schemaStructure;
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(filePath)) {
            schemaStructure = gson.fromJson(reader, SchemaStructure.class);
            LOGGER.info("Loaded Schema: \n" + schemaStructure.toString());
        }catch (JsonSyntaxException e){
            LOGGER.error(e.getMessage());
            throw new Exception(e.getMessage());
        }

        return schemaStructure;
    }

    default void convertTypes(ParquetData data, String filePath) throws Exception{
        SchemaStructure schemaStructure = readSchemaFile(filePath);
        List<String> columnsName = data.getColumnNames();

        for(String columName : columnsName){
            int index = columnsName.indexOf(columName);
            Optional<SchemaItem> schemaItem = schemaStructure.getItem(columName);

            if(schemaItem.isPresent()){
                String valor = data.getColumnTypes().set(index, schemaItem.get().getType());
            }
        }
    }

}
