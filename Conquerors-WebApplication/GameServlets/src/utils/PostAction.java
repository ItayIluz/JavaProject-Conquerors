package utils;

import com.google.gson.JsonObject;

import java.util.HashMap;

public class PostAction {
    private String action;
    private JsonObject values;

    public String getAction() {
        return action;
    }

    public JsonObject getValues() {
        return values;
    }
}
