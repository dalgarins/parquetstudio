package com.github.jhordyhuaman.parquetstudio.service;

import com.github.jhordyhuaman.parquetstudio.Constants;
import com.github.jhordyhuaman.parquetstudio.SchemaItemTransformSerializer;
import com.github.jhordyhuaman.parquetstudio.model.ParquetData;
import com.github.jhordyhuaman.parquetstudio.model.SchemaItemTransform;
import com.github.jhordyhuaman.parquetstudio.model.SchemaStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.List;


public class DataSchemaService {
    private final Logger LOGGER = Logger.getInstance(DataSchemaService.class);
    private SchemaStructure schemaStructureOriginal;
    private SchemaStructure schemaStructureTransform;
    public File schemaFile;

    public String convertToJsonString(Object schema) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SchemaItemTransform.class, new SchemaItemTransformSerializer())
                .setPrettyPrinting()
                .create();
        return gson.toJson(schema);
    }

    public void applyConvertTypes(ParquetData data, SchemaStructure schemaStructure) {
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

    public SchemaStructure getSchemaStructureOriginal(){ return schemaStructureOriginal;}
    public SchemaStructure getSchemaStructureTransform(){ return schemaStructureTransform;}
    public void setNullSchemaTransform(){ schemaStructureTransform = null; }

    public String generateTransformSchemaString() throws Exception {
        if(schemaFile == null){
            throw new Exception("First load a schema file");
        }

        if(schemaStructureOriginal == null){
            throw new Exception("First load a file parquet");
        }
        SchemaStructure schemaStructure = SchemaStructure.schemaFromFile(schemaFile.getAbsolutePath());
        schemaStructure.changesTypesFields();

        schemaStructureTransform = schemaStructureOriginal.toTransform(schemaStructure);
        String schemaString = convertToJsonString(schemaStructureTransform);
        LOGGER.warn("Write other schema in " + Constants.SCHEMA_PANEL);
        return schemaString;
    }

    public String generateOriginalSchemaString(List<String> columnNames, List<String> columnTypes) throws Exception{
        SchemaStructure schemaStructure = SchemaStructure.schemaFromLists(columnNames, columnTypes);
        String schemString = convertToJsonString(schemaStructure);
        schemaStructureOriginal = schemaStructure;

        LOGGER.info("Write schema of parquet in " + Constants.SCHEMA_PANEL);
        return schemString;
    }

    public boolean isSameNumberOfColumns(){
        return schemaStructureOriginal.fields.size() != schemaStructureTransform.fields.size();
    }

}
