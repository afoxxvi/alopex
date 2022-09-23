package com.afoxxvi.alopex.component.filter;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

import com.afoxxvi.alopex.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlopexFilter extends BaseObservable {
    private String packageName;
    private Boolean cancelFiltered;
    private Boolean notifyUnfiltered;
    private List<Match> matchList;

    public AlopexFilter(String packageName) {
        this(packageName, false, false, new ArrayList<>());
    }

    public AlopexFilter(String packageName, Boolean cancelFiltered, Boolean notifyUnfiltered, List<Match> matchList) {
        this.packageName = packageName;
        this.cancelFiltered = cancelFiltered;
        this.notifyUnfiltered = notifyUnfiltered;
        this.matchList = matchList;
    }

    @Bindable
    public String getPackageName() {
        return packageName;
    }

    @Bindable
    public String getActionText() {
        return cancelFiltered ?
                (notifyUnfiltered ? "cancel | notify" : "cancel") :
                (notifyUnfiltered ? "notify" : "none");
    }

    @Bindable
    public String getBlacklistText() {
        if (matchList == null || matchList.isEmpty()) {
            return "<>";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < matchList.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            Match match = matchList.get(i);
            builder.append(match.name).append(" (").append(match.matchCount).append(")");
        }
        return builder.toString();
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isFiltered(String title, String text, boolean doCount) {
        for (Match match : matchList) {
            if (match.matches(title, text, doCount)) {
                if (doCount) {
                    notifyPropertyChanged(BR.blacklistText);
                }
                return true;
            }
        }
        return false;
    }

    public List<Match> getMatchList() {
        return matchList;
    }

    public void setMatchList(List<Match> matchList) {
        this.matchList = matchList;
    }

    public void setCancelFiltered(Boolean cancelFiltered) {
        this.cancelFiltered = cancelFiltered;
    }

    public void setNotifyUnfiltered(Boolean notifyUnfiltered) {
        this.notifyUnfiltered = notifyUnfiltered;
    }

    @Bindable
    public Boolean getCancelFiltered() {
        return cancelFiltered;
    }

    @Bindable
    public Boolean getNotifyUnfiltered() {
        return notifyUnfiltered;
    }

    public static AlopexFilter fromJsonObject(JSONObject jsonObject) {
        String pkg = jsonObject.optString("packageName");
        Boolean b1 = jsonObject.optBoolean("cancelFiltered", false);
        Boolean b2 = jsonObject.optBoolean("notifyUnfiltered", false);
        List<Match> ls = new ArrayList<>();
        JSONArray a1 = jsonObject.optJSONArray("matchList");
        if (a1 != null) {
            for (int i = 0; i < a1.length(); i++) {
                JSONObject object = a1.optJSONObject(i);
                if (object != null) {
                    ls.add(Match.fromJsonObject(object));
                }
            }
        }
        return new AlopexFilter(pkg, b1, b2, ls);
    }

    public JSONObject toJsonObject() {
        JSONObject object = new JSONObject();
        try {
            object.put("packageName", packageName);
            object.put("cancelFiltered", cancelFiltered);
            object.put("notifyUnfiltered", notifyUnfiltered);
            JSONArray array = new JSONArray();
            for (Match match : matchList) {
                array.put(match.toJsonObject());
            }
            object.put("matchList", array);
        } catch (Exception ignored) {
        }
        return object;
    }

    public static class Match {
        private String name;
        private List<Pair<Rule, String>> ruleList;
        private Integer matchCount;

        public Match(String name) {
            this(name, new ArrayList<>(), 0);
        }

        public Match(String name, List<Pair<Rule, String>> ruleList, Integer matchCount) {
            this.name = name;
            this.ruleList = ruleList;
            this.matchCount = matchCount;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setRuleList(List<Pair<Rule, String>> ruleList) {
            this.ruleList = ruleList;
        }

        public String getName() {
            return name;
        }

        public List<Pair<Rule, String>> getRuleList() {
            return ruleList;
        }

        public Integer getMatchCount() {
            return matchCount;
        }

        public Boolean matches(String title, String text, boolean doCount) {
            for (Pair<Rule, String> pair : ruleList) {
                boolean res = false;
                switch (pair.a) {
                    case TITLE_CONTAINS:
                        res = title.contains(pair.b);
                        break;
                    case TITLE_MATCHES:
                        res = title.matches(pair.b);
                        break;
                    case TEXT_CONTAINS:
                        res = text.contains(pair.b);
                        break;
                    case TEXT_MATCHES:
                        res = text.matches(pair.b);
                        break;
                    default:
                        break;
                }
                if (!res) {
                    return false;
                }
            }
            if (doCount) {
                matchCount++;
            }
            return true;
        }

        public static Match copyOf(Match match) {
            return new Match(match.name, match.ruleList, match.matchCount);
        }

        private static Match fromJsonObject(JSONObject object) {
            JSONArray array = object.optJSONArray("ruleList");
            List<Pair<Rule, String>> list = new ArrayList<>();
            String name = object.optString("name", "unnamed");
            int matchCount = object.optInt("matchCount", 0);
            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    String[] sp = array.optString(i, "").split("\u0000");
                    if (sp.length > 1) {
                        list.add(new Pair<>(Rule.valueOf(sp[0]), sp[1]));
                    }
                }
            }
            return new Match(name, list, matchCount);
        }

        public JSONObject toJsonObject() {
            JSONObject object = new JSONObject();
            try {
                JSONArray array = new JSONArray();
                for (Pair<Rule, String> t : ruleList) {
                    array.put(t.a + "\u0000" + t.b);
                }
                object.put("name", name);
                object.put("ruleList", array);
                object.put("matchCount", matchCount);
            } catch (JSONException ignored) {
            }
            return object;
        }
    }

    public enum Rule {
        //
        TITLE_CONTAINS,
        TITLE_MATCHES,
        TEXT_CONTAINS,
        TEXT_MATCHES;

        public String display() {
            String raw = name();
            StringBuilder builder = new StringBuilder();
            String[] sp = raw.split("_");
            for (int i = 0; i < sp.length; i++) {
                if (i == 0) {
                    builder.append(sp[i].toLowerCase());
                } else {
                    builder.append(sp[i].charAt(0)).append(sp[i].substring(1).toLowerCase());
                }
            }
            return builder.toString();
        }
    }
}
