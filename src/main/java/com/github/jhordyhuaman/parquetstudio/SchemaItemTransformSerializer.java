package com.github.jhordyhuaman.parquetstudio;

import com.google.gson.*;
import java.lang.reflect.Type;

public class SchemaItemTransformSerializer implements JsonSerializer<DataConvertService.SchemaItemTransform> {
    @Override
    public JsonElement serialize(DataConvertService.SchemaItemTransform schemaItem, Type typeOfSrc, JsonSerializationContext context){
        JsonObject json = new JsonObject();

        json.addProperty("name", schemaItem.name);
        json.addProperty("type", String.valueOf(schemaItem.type));
        json.addProperty("typeTransform", String.valueOf(schemaItem.typeTransform));

        return json;
    }
}
