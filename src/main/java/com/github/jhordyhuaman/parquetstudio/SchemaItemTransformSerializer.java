package com.github.jhordyhuaman.parquetstudio;

import com.github.jhordyhuaman.parquetstudio.model.SchemaItemTransform;
import com.google.gson.*;
import java.lang.reflect.Type;

public class SchemaItemTransformSerializer implements JsonSerializer<SchemaItemTransform> {
    @Override
    public JsonElement serialize(SchemaItemTransform schemaItem, Type typeOfSrc, JsonSerializationContext context){
        JsonObject json = new JsonObject();

        json.addProperty("name", schemaItem.name);
        json.addProperty("type", String.valueOf(schemaItem.type));
        json.addProperty("typeTransform", String.valueOf(schemaItem.typeTransform));

        return json;
    }
}
