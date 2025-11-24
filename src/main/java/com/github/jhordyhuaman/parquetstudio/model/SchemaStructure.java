package com.github.jhordyhuaman.parquetstudio.model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.diagnostic.Logger;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SchemaStructure {
    private static final Logger LOGGER = Logger.getInstance(SchemaStructure.class);

    public List<String> partitions;
    public List<SchemaItem> fields;

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

    public static SchemaStructure schemaFromLists(List<String> listNames, List<String> listTypes) throws Exception {
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

    public static SchemaStructure schemaFromFile(String filePath) throws Exception {
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
}
