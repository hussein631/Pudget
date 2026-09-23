package com.masareefi.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends BaseActivity {
    LinearLayout contentBox, monthsBox;

    @Override protected void onCreate(Bundle b) { super.onCreate(b); build(); }
    @Override protected void onResume() { super.onResume(); if (contentBox != null) buildContent(); }

    private void build() {
        LinearLayout page = root("مصاريفي");
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        contentBox = new LinearLayout(this);
        contentBox.setOrientation(LinearLayout.VERTICAL);
        contentBox.setPadding(dp(16), dp(14), dp(16), dp(30));
        contentBox.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        scroll.addView(contentBox);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
        buildContent();
    }

    private void buildContent() {
        contentBox.removeAllViews();
        addHero(); addFinancialCards(); addQuickActions(); addProgressCard(); addRecentSection();
    }

    private void addHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.HORIZONTAL);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        hero.setPadding(dp(16), dp(12), dp(16), dp(12));
        hero.setBackground(heroBackground());

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        titles.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView name = text("مصاريفي", 27, Color.WHITE);
        name.setTypeface(null, Typeface.BOLD);
        name.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        TextView sub = text("إدارة أموالك بشكل أبسط وأوضح", 15, GOLD_SOFT);
        sub.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        titles.addView(name, new LinearLayout.LayoutParams(-1, dp(46)));
        titles.addView(sub, new LinearLayout.LayoutParams(-1, dp(40)));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -1, 1);
        tp.setMargins(0, 0, dp(12), 0);
        hero.addView(titles, tp);

        FrameLayout iconFrame = new FrameLayout(this);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL); circle.setColor(GOLD); iconFrame.setBackground(circle);
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_wallet); icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        iconFrame.addView(icon, new FrameLayout.LayoutParams(-1, -1));
        hero.addView(iconFrame, new LinearLayout.LayoutParams(dp(76), dp(76)));

        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, dp(120));
        hp.setMargins(0, 0, 0, dp(14)); contentBox.addView(hero, hp);
    }

    private void addFinancialCards() {
        String ym = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        double spent = AppData.monthTotal(this, ym), budget = AppData.getMonthlyBudget(this);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        addMetricCard(row, "إجمالي المصروفات", AppData.money(spent), RED, false, "↓");
        addMetricCard(row, "إجمالي الدخل", budget > 0 ? AppData.money(budget) : "0.00 ج.م", GREEN_DARK, true, "↑");
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(154));
        rp.setMargins(0, 0, 0, dp(10)); contentBox.addView(row, rp);

        LinearLayout budgetCard = new LinearLayout(this);
        budgetCard.setOrientation(LinearLayout.VERTICAL); budgetCard.setGravity(Gravity.CENTER);
        budgetCard.setPadding(dp(18), dp(10), dp(18), dp(10)); budgetCard.setBackground(budgetBackground());
        TextView title = text("ميزانية الشهر", 18, NAVY_DARK);
        title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.CENTER);
        budgetCard.addView(title, new LinearLayout.LayoutParams(-1, dp(36)));
        TextView value = text(budget > 0 ? AppData.money(budget) : "حدد الميزانية من الإعدادات", budget > 0 ? 23 : 15, TEXT);
        value.setTypeface(null, Typeface.BOLD); value.setGravity(Gravity.CENTER);
        value.setTextDirection(View.TEXT_DIRECTION_LTR); value.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        value.setMaxLines(2); value.setSingleLine(false);
        budgetCard.addView(value, new LinearLayout.LayoutParams(-1, dp(58)));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(112));
        bp.setMargins(0, 0, 0, dp(12)); contentBox.addView(budgetCard, bp);
    }

    private void addMetricCard(LinearLayout row, String title, String value, int valueColor, boolean income, String symbol) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); card.setPadding(dp(8), dp(9), dp(8), dp(8));
        card.setBackground(income ? incomeBackground() : expenseBackground());

        TextView titleView = text(title, 13, TEXT);
        titleView.setTypeface(null, Typeface.BOLD); titleView.setGravity(Gravity.CENTER); titleView.setMaxLines(2);
        card.addView(titleView, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView valueView = text(value, 17, valueColor);
        valueView.setTypeface(null, Typeface.BOLD); valueView.setGravity(Gravity.CENTER);
        valueView.setTextDirection(View.TEXT_DIRECTION_LTR); valueView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        valueView.setMaxLines(2); valueView.setSingleLine(false);
        card.addView(valueView, new LinearLayout.LayoutParams(-1, dp(48)));

        TextView icon = text(symbol, 22, Color.WHITE); icon.setGravity(Gravity.CENTER);
        GradientDrawable circle = new GradientDrawable(); circle.setShape(GradientDrawable.OVAL);
        circle.setColor(symbol.equals("↑") ? GREEN : RED); icon.setBackground(circle);
        card.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(150), 1);
        cp.setMargins(dp(5), 0, dp(5), 0); row.addView(card, cp);
    }

    private void addQuickActions() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        Button add = button("مصروف   ＋", GOLD), stats = button("إحصائيات   📊", NAVY);
        row.addView(add, new LinearLayout.LayoutParams(0, dp(58), 1));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, dp(58), 1);
        sp.setMargins(dp(8), 0, 0, 0); row.addView(stats, sp);
        add.setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));
        stats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, dp(58));
        rp.setMargins(0, 0, 0, dp(10)); contentBox.addView(row, rp);

        Button settings = button("⚙   الإعدادات والميزانية", NAVY_DARK);
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.setMargins(0, 0, 0, dp(16)); contentBox.addView(settings, p);
    }

    private void addProgressCard() {
        String ym = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        double spent = AppData.monthTotal(this, ym), budget = AppData.getMonthlyBudget(this);
        double pct = budget > 0 ? Math.min(100, Math.max(0, spent * 100.0 / budget)) : 0;
        double remaining = budget > 0 ? Math.max(0, budget - spent) : 0;

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16), dp(12), dp(16), dp(14)); card.setBackground(cardBackground());
        TextView title = text("استهلاك الميزانية", 17, NAVY_DARK);
        title.setTypeface(null, Typeface.BOLD); title.setGravity(Gravity.RIGHT);
        card.addView(title, new LinearLayout.LayoutParams(-1, dp(34)));
        TextView line = text(budget > 0 ? String.format(Locale.US, "%.0f%% مستخدم   •   المتبقي %s", pct, AppData.money(remaining)) : "أضف ميزانية شهرية لعرض نسبة الاستهلاك", 13, MUTED);
        line.setGravity(Gravity.RIGHT); line.setMaxLines(2);
        card.addView(line, new LinearLayout.LayoutParams(-1, dp(40)));
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100); progress.setProgress((int)Math.round(pct));
        GradientDrawable progressBg = new GradientDrawable(); progressBg.setColor(GREEN); progressBg.setCornerRadius(dp(8)); progress.setProgressDrawable(progressBg);
        card.addView(progress, new LinearLayout.LayoutParams(-1, dp(12)));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(112));
        p.setMargins(0, 0, 0, dp(18)); contentBox.addView(card, p);
    }

    private void addRecentSection() {
        TextView heading = text("ملخص الشهر", 21, GREEN_DARK);
        heading.setTypeface(null, Typeface.BOLD); heading.setGravity(Gravity.RIGHT);
        contentBox.addView(heading, new LinearLayout.LayoutParams(-1, dp(46)));
        monthsBox = new LinearLayout(this); monthsBox.setOrientation(LinearLayout.VERTICAL);
        contentBox.addView(monthsBox); renderMonthlyTotals();
    }

    private void renderMonthlyTotals() {
        monthsBox.removeAllViews();
        String ym = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
        double monthSpent = AppData.monthTotal(this, ym);

        LinearLayout latest = new LinearLayout(this);
        latest.setOrientation(LinearLayout.VERTICAL); latest.setGravity(Gravity.CENTER_VERTICAL);
        latest.setPadding(dp(18), dp(14), dp(18), dp(14)); latest.setBackground(cardBackground());

        if (monthSpent == 0) {
            TextView empty = text("لا توجد مصروفات حتى الآن\nابدأ بإضافة أول مصروف", 16, MUTED);
            empty.setGravity(Gravity.CENTER); latest.addView(empty, new LinearLayout.LayoutParams(-1, dp(108)));
        } else {
            TextView total = text("مصروفات هذا الشهر   " + AppData.money(monthSpent), 16, GREEN_DARK);
            total.setTypeface(null, Typeface.BOLD); total.setGravity(Gravity.RIGHT);
            latest.addView(total, new LinearLayout.LayoutParams(-1, dp(48))); addRecentExpenses(latest);
        }
        monthsBox.addView(latest, new LinearLayout.LayoutParams(-1, monthSpent == 0 ? dp(132) : dp(194)));

        LinkedHashMap<String, Double> totals = AppData.monthlyTotals(this);
        if (!totals.isEmpty()) {
            TextView oldTitle = text("الشهور السابقة", 17, NAVY_DARK);
            oldTitle.setTypeface(null, Typeface.BOLD); oldTitle.setGravity(Gravity.RIGHT);
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, dp(45));
            tp.setMargins(0, dp(12), 0, 0); monthsBox.addView(oldTitle, tp);
            int count = 0;
            for (Map.Entry<String, Double> e : totals.entrySet()) {
                if (count++ >= 5) break;
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.HORIZONTAL); card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(12), dp(6), dp(12), dp(6)); card.setBackground(cardBackground());
                TextView name = text("📅  " + AppData.monthLabel(e.getKey()), 15, TEXT);
                TextView total = text(AppData.money(e.getValue()), 15, GREEN_DARK);
                total.setGravity(Gravity.CENTER); total.setTypeface(null, Typeface.BOLD); total.setTextDirection(View.TEXT_DIRECTION_LTR);
                card.addView(name, new LinearLayout.LayoutParams(0, dp(55), 1));
                card.addView(total, new LinearLayout.LayoutParams(dp(145), dp(55)));
                card.setOnClickListener(v -> startActivity(new Intent(this, ExpenseActivity.class)));
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(66));
                cp.setMargins(0, 0, 0, dp(7)); monthsBox.addView(card, cp);
            }
        }
    }

    private void addRecentExpenses(LinearLayout parent) {
        try {
            JSONArray a = AppData.getExpenses(this);
            ArrayList<JSONObject> items = new ArrayList<>();
            for (int i=0; i<a.length(); i++) items.add(a.getJSONObject(i));
            items.sort((x,y) -> y.optString("date", "").compareTo(x.optString("date", "")));
            String ym = new SimpleDateFormat("yyyy-MM", Locale.US).format(new Date());
            int shown = 0;
            for (JSONObject o : items) {
                if (shown >= 3) break;
                if (!o.optString("date", "").startsWith(ym)) continue;
                LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(8), dp(3), dp(8), dp(3));
                TextView cat = text(o.optString("category", "أخرى"), 15, TEXT);
                TextView amount = text(AppData.money(o.optDouble("amount", 0)), 15, RED);
                amount.setTypeface(null, Typeface.BOLD); amount.setGravity(Gravity.CENTER); amount.setTextDirection(View.TEXT_DIRECTION_LTR);
                row.addView(cat, new LinearLayout.LayoutParams(0, dp(38), 1));
                row.addView(amount, new LinearLayout.LayoutParams(dp(145), dp(38)));
                parent.addView(row, new LinearLayout.LayoutParams(-1, dp(40))); shown++;
            }
        } catch (Exception ignored) {}
    }
}
