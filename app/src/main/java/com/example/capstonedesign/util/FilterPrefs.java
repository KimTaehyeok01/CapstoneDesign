package com.example.capstonedesign.util;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;

public class FilterPrefs {
    private static final String PREFS = "filters_prefs";
    private static final String KEY_SEASONS = "selected_seasons";
    private static final String KEY_TYPES   = "selected_types";
    private static final String KEY_DIFFS   = "selected_diffs";
    private static final String KEY_REGIONS = "selected_regions";

    private static void putList(SharedPreferences.Editor editor, String key, ArrayList<String> list) {
        JSONArray arr = new JSONArray();
        if (list != null) for (String s : list) arr.put(s);
        editor.putString(key, arr.toString());
    }

    private static ArrayList<String> getList(SharedPreferences prefs, String key) {
        String json = prefs.getString(key, "[]");
        ArrayList<String> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) result.add(arr.optString(i));
        } catch (JSONException ignored) {}
        return result;
    }

    public static void save(Context ctx,
                            ArrayList<String> seasons,
                            ArrayList<String> types,
                            ArrayList<String> diffs,
                            ArrayList<String> regions) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();
        putList(ed, KEY_SEASONS, seasons);
        putList(ed, KEY_TYPES,   types);
        putList(ed, KEY_DIFFS,   diffs);
        putList(ed, KEY_REGIONS, regions);
        ed.apply();
    }

    public static ArrayList<String> loadSeasons(Context ctx) { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_SEASONS); }
    public static ArrayList<String> loadTypes(Context ctx)   { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_TYPES); }
    public static ArrayList<String> loadDiffs(Context ctx)   { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_DIFFS); }
    public static ArrayList<String> loadRegions(Context ctx) { return getList(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE), KEY_REGIONS); }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
