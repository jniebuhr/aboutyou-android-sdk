package de.aboutyou.internal.typeadapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.aboutyou.enums.AutocompleteType;

public class AutocompleteTypeAdapter implements JsonSerializer<AutocompleteType> {

    @Override
    public JsonElement serialize(AutocompleteType src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }

}
