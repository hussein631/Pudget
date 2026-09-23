package com.masareefi.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.security.MessageDigest;
import java.util.*;

public class AppData {
    private static final String PREFS = "masareefi_data";
    private static final String EXPENSES = "expenses";
    private static final String CATEGORIES = "categories";
    private static final String PASSWORD_HASH = "password_hash";
    private static final String MONTHLY_BUDGET = "monthly_budget";
    private static final String THEME = "theme";
    private static final String CATEGORY_MIGRATION_V2 = "category_migration_v2";

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_GREEN = "green";
    public static final String THEME_BLUE = "blue";
    public static final String THEME_PURPLE = "purple";

    public static final String[] DEFAULT_CATEGORIES = {
            "مواصلات", "أكل وشرب", "مصروف شخصي", "فواتير",
            "مشتريات", "صحة", "تعليم", "ترفيه", "جمعيات", "ديون", "أخرى"
    };

    public static JSONArray getExpenses(Context c) {
        try {
            String s = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(EXPENSES, "[]");
            return new JSONArray(s);
        } catch (Exception e) { return new JSONArray(); }
    }

    public static void saveExpenses(Context c, JSONArray a) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(EXPENSES, a.toString()).apply();
    }

    public static void addExpense(Context c, String date, String day, String category, double amount) {
        addExpense(c, date, day, category, amount, "", "");
    }

    public static void addExpense(Context c, String date, String day, String category, double amount, String notes) {
        addExpense(c, date, day, category, amount, notes, "");
    }

    public static void addExpense(Context c, String date, String day, String category, double amount, String notes, String item) {
        try {
            JSONArray a = getExpenses(c);
            JSONObject o = new JSONObject();
            o.put("id", System.currentTimeMillis());
            o.put("date", date);
            o.put("day", day);
            o.put("time", new java.text.SimpleDateFormat("HH:mm", Locale.US).format(new Date()));
            o.put("category", category);
            o.put("item", item == null ? "" : item);
            o.put("amount", amount);
            o.put("notes", notes == null ? "" : notes);
            a.put(o);
            saveExpenses(c, a);
        } catch (Exception ignored) {}
    }

    public static JSONObject removeExpense(Context c, long id) {
        if (id < 0) return null;
        try {
            JSONArray old = getExpenses(c);
            JSONArray fresh = new JSONArray();
            JSONObject removed = null;
            for (int i = 0; i < old.length(); i++) {
                JSONObject o = old.optJSONObject(i);
                if (o == null) continue;
                if (removed == null && o.optLong("id", -1L) == id) removed = new JSONObject(o.toString());
                else fresh.put(o);
            }
            if (removed == null) return null;
            saveExpenses(c, fresh);
            return removed;
        } catch (Exception ignored) { return null; }
    }

    public static void restoreExpense(Context c, JSONObject expense) {
        if (expense == null) return;
        try {
            JSONArray a = getExpenses(c);
            a.put(new JSONObject(expense.toString()));
            saveExpenses(c, a);
        } catch (Exception ignored) {}
    }

    public static void deleteExpense(Context c, long id) { removeExpense(c, id); }

    public static ArrayList<String> getCategories(Context c) {
        ArrayList<String> list = new ArrayList<>();
        try {
            SharedPreferences p = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (!p.contains(CATEGORIES)) {
                Collections.addAll(list, DEFAULT_CATEGORIES);
                saveCategories(c, list);
                return list;
            }
            JSONArray a = new JSONArray(p.getString(CATEGORIES, "[]"));
            for (int i = 0; i < a.length(); i++) list.add(a.getString(i));
            if (!p.getBoolean(CATEGORY_MIGRATION_V2, false)) {
                boolean changed = false;
                if (!list.contains("جمعيات")) { list.add("جمعيات"); changed = true; }
                if (!list.contains("ديون")) { list.add("ديون"); changed = true; }
                if (changed) saveCategories(c, list);
                p.edit().putBoolean(CATEGORY_MIGRATION_V2, true).apply();
            }
        } catch (Exception e) { Collections.addAll(list, DEFAULT_CATEGORIES); }
        return list;
    }

    public static void saveCategories(Context c, ArrayList<String> list) {
        JSONArray a = new JSONArray();
        for (String s : list) a.put(s);
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CATEGORIES, a.toString()).apply();
    }

    public static double getMonthlyBudget(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getFloat(MONTHLY_BUDGET, 0f);
    }

    public static void setMonthlyBudget(Context c, double budget) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putFloat(MONTHLY_BUDGET, (float) Math.max(0, budget)).apply();
    }

    public static String getTheme(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(THEME, THEME_GREEN);
    }

    public static void setTheme(Context c, String theme) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(THEME, theme).apply();
    }

    public static double monthTotal(Context c) {
        return monthTotal(c, new java.text.SimpleDateFormat("yyyy-MM", Locale.US).format(new Date()));
    }

    public static double monthTotal(Context c, String yearMonth) {
        double total = 0;
        try {
            JSONArray a = getExpenses(c);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (o.optString("date", "").startsWith(yearMonth)) total += o.optDouble("amount", 0);
            }
        } catch (Exception ignored) {}
        return total;
    }

    public static LinkedHashMap<String, Double> monthlyTotals(Context c) {
        HashMap<String, Double> raw = new HashMap<>();
        try {
            JSONArray a = getExpenses(c);
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                String date = o.optString("date", "");
                if (date.length() >= 7) {
                    String ym = date.substring(0, 7);
                    raw.put(ym, raw.getOrDefault(ym, 0.0) + o.optDouble("amount", 0));
                }
            }
        } catch (Exception ignored) {}
        ArrayList<String> keys = new ArrayList<>(raw.keySet());
        Collections.sort(keys, Collections.reverseOrder());
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        for (String k : keys) result.put(k, raw.get(k));
        return result;
    }

    public static String monthLabel(String yearMonth) {
        try {
            Date d = new java.text.SimpleDateFormat("yyyy-MM", Locale.US).parse(yearMonth);
            return new java.text.SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(d);
        } catch (Exception e) { return yearMonth; }
    }

    public static String money(double n) { return String.format(Locale.US, "%.2f ج.م", n); }

    public static boolean hasPassword(Context c) { return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(PASSWORD_HASH); }
    public static void setPassword(Context c, String password) { c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(PASSWORD_HASH, sha256(password)).apply(); }
    public static boolean checkPassword(Context c, String password) {
        String saved = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PASSWORD_HASH, "");
        return !saved.isEmpty() && saved.equals(sha256(password));
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format(Locale.US, "%02x", b));
            return sb.toString();
        } catch (Exception e) { return value; }
    }
}
