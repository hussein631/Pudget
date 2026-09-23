package com.masareefi.app;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class StatisticsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout page = root("الإحصائيات");
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(14), dp(16), dp(14), dp(24));

        double total = 0;
        HashMap<String, Double> map = new HashMap<>();
        try {
            JSONArray a = AppData.getExpenses(this);
            for (int i=0; i<a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                double v = o.optDouble("amount");
                total += v;
                String cat = o.optString("category", "أخرى");
                map.put(cat, map.getOrDefault(cat, 0.0) + v);
            }
        } catch(Exception ignored) {}

        TextView big = text("إجمالي كل المصروفات\n" + AppData.money(total), 24, GREEN_DARK);
        big.setGravity(Gravity.CENTER); big.setBackground(cardBackground());
        box.addView(big, new LinearLayout.LayoutParams(-1, dp(110)));

        Button all = button("📋  عرض كل المصروفات", GREEN);
        all.setOnClickListener(v -> startActivity(new android.content.Intent(this, ExpenseActivity.class)));
        LinearLayout.LayoutParams allp = new LinearLayout.LayoutParams(-1, dp(54));
        allp.setMargins(0, dp(10), 0, dp(10)); box.addView(all, allp);

        for (Map.Entry<String, Double> e : map.entrySet()) {
            TextView row = text("• " + e.getKey() + "   —   " + AppData.money(e.getValue()), 17, NAVY);
            row.setBackground(cardBackground());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
            p.setMargins(0, dp(7), 0, 0); box.addView(row, p);
        }

        scroll.addView(box);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
    }
}
