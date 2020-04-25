package com.example.offlinemap;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectionsValues {
    public final String id;
    public final String mapFile;

    public SelectionsValues(String id, String mapFile) {
        this.id = id;
        this.mapFile = mapFile;
    }

    public static void loadFiles(Context c) {
        ITEMS = new ArrayList<>();
        ITEM_MAP = new HashMap<>();
        File dir = c.getExternalFilesDir(null);
        File[] files = dir.listFiles();
        ArrayList<File> allFiles = new ArrayList<>();
        for(int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                allFiles.addAll(Arrays.asList(files[i].listFiles()));
            } else {
                allFiles.add(files[i]);
            }
        }
        for(int i = 0; i < allFiles.size(); i++) {
            Log.d("MAP FILES", "Loading file: "+allFiles.get(i).getAbsolutePath());
            addItem(new SelectionsValues(String.valueOf(i), allFiles.get(i).getName()));
        }
    }

    public static void delete(Context c, String id) {
        Log.d("MAP FILE", "Deletion request for id"+id);
        File f = new File(c.getExternalFilesDir(null), ITEM_MAP.get(id).mapFile);
        if(!f.exists()) {
            f = new File(c.getExternalFilesDir(null), "germany/"+ITEM_MAP.get(id).mapFile);
        }
        boolean succ = f.delete();
        Log.d("MAP FILE", "Trying to delete: "+f.getAbsolutePath() + " suceeded: "+succ);
    }

    @Override
    public String toString() {
        return this.mapFile;
    }

    /**
     * A map of sample items, by ID.
     */
    public static Map<String, SelectionsValues> ITEM_MAP = new HashMap<String, SelectionsValues>();

    /**
     * An array of sample Selections.
     */
    public static List<SelectionsValues> ITEMS = new ArrayList<SelectionsValues>();


    private static void addItem(SelectionsValues item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    //protected SelectionsDummyValues() {
        // no-op
    //}
}
