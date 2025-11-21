package com.github.jhordyhuaman.parquetstudio;

import com.google.gson.*;
import java.lang.reflect.Type;

public class SchemaItemTransformSerializer implements JsonSerializer<DataConvertService.SchemaItemTransform> {
    @Override
    public JsonElement serialize(DataConvertService.SchemaItemTransform schemaItem, Type typeOfSrc, JsonSerializationContext context){
        JsonObject json = new JsonObject();

        json.addProperty("name", schemaItem.name);
        json.add("type", schemaItem.type != null
                ? context.serialize(schemaItem.type)
                : JsonNull.INSTANCE
        );
        json.add("typeTransform", schemaItem.type2Transform != null
                ? context.serialize(schemaItem.type2Transform)
                : JsonNull.INSTANCE
        );

        return json;
    }
}
