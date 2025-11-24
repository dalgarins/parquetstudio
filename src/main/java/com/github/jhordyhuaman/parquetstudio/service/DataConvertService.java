package com.github.jhordyhuaman.parquetstudio.service;

import com.github.jhordyhuaman.parquetstudio.SchemaItemTransformSerializer;
import com.github.jhordyhuaman.parquetstudio.model.ParquetData;
import com.github.jhordyhuaman.parquetstudio.model.SchemaItemTransform;
import com.github.jhordyhuaman.parquetstudio.model.SchemaStructure;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;
import java.util.List;


public interface DataConvertService {
    static final Logger LOGGER = Logger.getInstance(DataConvertService.class);

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
