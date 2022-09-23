package com.afoxxvi.alopex.component.filter;

import static com.afoxxvi.alopex.AlopexView.TAG;

import android.content.Context;
import android.util.Log;

import com.afoxxvi.alopex.component.notify.Notify;
import com.afoxxvi.alopex.util.FileUtils;
import com.afoxxvi.alopex.util.Triplet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlopexFilterManager {
    private AlopexFilterManager() {
    }

    private static AlopexFilterManager inst;

    public static AlopexFilterManager getInstance() {
        if (inst == null) {
            inst = new AlopexFilterManager();
        }
        return inst;
    }

    public void init(Context context) {
        filters = new ArrayList<>();
        File file = new File(context.getFilesDir(), "filter.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String doc = FileUtils.inputFile(context, "filter.json");
            try {
                JSONArray array = new JSONArray(doc);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.optJSONObject(i);
                    if (object != null) {
                        filters.add(AlopexFilter.fromJsonObject(object));
                    }
                }
            } catch (JSONException ignored) {
            }
        }
    }

    public void save(Context context) {
        JSONArray array = new JSONArray();
        for (AlopexFilter filter : filters) {
            array.put(filter.toJsonObject());
        }
        FileUtils.outputFile(context, "filter.json", array.toString());
        Log.i(TAG, "filter saved");
    }

    private List<AlopexFilter> filters;

    public List<AlopexFilter> getFilters() {
        return filters;
    }


    public Triplet<Boolean, Boolean, Boolean> isFiltered(String packageName, Notify notify, boolean count) {
        return isFiltered(packageName, notify.getTitle(), notify.getText(), count);
    }

    /**
     * @return do notify, do cancel, is filtered
     */
    public Triplet<Boolean, Boolean, Boolean> isFiltered(String packageName, String title, String text, boolean count) {
        for (AlopexFilter filter : filters) {
            if (filter.getPackageName().equals(packageName)) {
                if (filter.isFiltered(title, text, count)) {
                    return new Triplet<>(false, filter.getCancelFiltered(), true);
                } else {
                    return new Triplet<>(filter.getNotifyUnfiltered(), false, false);
                }
            }
        }
        return new Triplet<>(false, false, false);
    }
}
