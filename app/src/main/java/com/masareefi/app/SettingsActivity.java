package com.masareefi.app;

import android.app.*;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SettingsActivity extends BaseActivity {
    ArrayList<String> cats;
    LinearLayout list;
    EditText budgetInput;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        cats = AppData.getCategories(this);

        LinearLayout page = root("الإعدادات");
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(14), dp(16), dp(18));

        TextView title = text("إعداد الميزانية وخانات المصروفات", 21, GREEN_DARK);
        title.setTypeface(null, Typeface.BOLD);
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(52)));

        LinearLayout budgetCard = new LinearLayout(this);
        budgetCard.setOrientation(LinearLayout.VERTICAL);
        budgetCard.setPadding(dp(14), dp(8), dp(14), dp(8));
        budgetCard.setBackground(greenCardBackground());
        TextView bt = text("💰 الميزانية الشهرية", 17, GREEN_DARK);
        bt.setTypeface(null, Typeface.BOLD);
        budgetCard.addView(bt, new LinearLayout.LayoutParams(-1, dp(34)));
        budgetInput = new EditText(this);
        budgetInput.setHint("مثال: 10000");
        budgetInput.setSingleLine(true);
        budgetInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        double saved = AppData.getMonthlyBudget(this);
        if (saved > 0) budgetInput.setText(String.valueOf(saved));
        budgetCard.addView(budgetInput, new LinearLayout.LayoutParams(-1, dp(48)));
        box.addView(budgetCard, new LinearLayout.LayoutParams(-1, dp(112)));

        Button saveBudget = button("💾 حفظ الميزانية", GREEN);
        LinearLayout.LayoutParams sbp = new LinearLayout.LayoutParams(-1, dp(50));
        sbp.setMargins(0, dp(8), 0, dp(10)); box.addView(saveBudget, sbp);
        saveBudget.setOnClickListener(v -> saveBudget());

        TextView catTitle = text("خانات المصروفات", 19, GREEN_DARK);
        catTitle.setTypeface(null, Typeface.BOLD); box.addView(catTitle, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView help = text("يمكنك التمرير لأعلى ولأسفل داخل القائمة إذا كانت الخانات كثيرة.", 13, MUTED);
        box.addView(help, new LinearLayout.LayoutParams(-1, dp(42)));

        ScrollView categoryScroll = new ScrollView(this);
        categoryScroll.setFillViewport(true);
        list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(2), dp(2), dp(8)); categoryScroll.addView(list);
        render(); box.addView(categoryScroll, new LinearLayout.LayoutParams(-1, 0, 1));

        Button add = button("➕ إضافة خانة جديدة", GREEN);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(54));
        ap.setMargins(0, dp(8), 0, 0); box.addView(add, ap);
        add.setOnClickListener(v -> {
            EditText input = new EditText(this); input.setHint("مثال: اشتراكات"); input.setSingleLine(true);
            new AlertDialog.Builder(this).setTitle("إضافة خانة مصروف").setView(input)
                    .setPositiveButton("إضافة", (d,w) -> {
                        String s = input.getText().toString().trim();
                        if (!s.isEmpty()) { cats.add(s); AppData.saveCategories(this, cats); render(); }
                    }).setNegativeButton("إلغاء", null).show();
        });

        TextView themeTitle = text("🎨 المظهر والثيم", 19, GREEN_DARK);
        themeTitle.setTypeface(null, Typeface.BOLD); box.addView(themeTitle, new LinearLayout.LayoutParams(-1, dp(42)));

        Spinner themeSpinner = new Spinner(this);
        String[] themeNames = {"💚 أخضر", "☀️ فاتح", "🌙 داكن", "🔵 أزرق", "🟣 بنفسجي"};
        String[] themeValues = {AppData.THEME_GREEN, AppData.THEME_LIGHT, AppData.THEME_DARK, AppData.THEME_BLUE, AppData.THEME_PURPLE};
        themeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, themeNames));
        int selectedTheme = 0; String savedTheme = AppData.getTheme(this);
        for (int i=0;i<themeValues.length;i++) if (themeValues[i].equals(savedTheme)) selectedTheme=i;
        themeSpinner.setSelection(selectedTheme);
        box.addView(themeSpinner, new LinearLayout.LayoutParams(-1, dp(54)));
        TextView themeHelp = text("يتحفظ اختيارك ويطبق على كل شاشات Pudget عند فتحها.", 13, MUTED);
        box.addView(themeHelp, new LinearLayout.LayoutParams(-1, dp(38)));
        themeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String value = themeValues[position];
                if (!value.equals(AppData.getTheme(SettingsActivity.this))) {
                    AppData.setTheme(SettingsActivity.this, value); recreate();
                }
            }
        });

        Button pdf = button("📄 تقارير المصروفات و PDF", NAVY);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, dp(54));
        pp.setMargins(0, dp(10), 0, dp(8)); box.addView(pdf, pp);
        pdf.setOnClickListener(v -> startActivity(new android.content.Intent(this, ReportActivity.class)));

        page.addView(box, new LinearLayout.LayoutParams(-1, 0, 1)); setContentView(page);
    }

    private void saveBudget() {
        double value = 0;
        try { value = Double.parseDouble(budgetInput.getText().toString().trim()); } catch (Exception ignored) {}
        if (value < 0) { budgetInput.setError("أدخل رقماً صحيحاً"); return; }
        AppData.setMonthlyBudget(this, value);
        Toast.makeText(this, "تم حفظ الميزانية الشهرية", Toast.LENGTH_SHORT).show();
    }

    private void render() {
        list.removeAllViews();
        for (int i=0; i<cats.size(); i++) {
            final int index = i;
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(8), dp(4), dp(7), dp(4));
            row.setBackground(cardBackground());
            TextView t = text("•  " + cats.get(i), 16, TEXT);
            Button del = button("حذف", RED);
            row.addView(t, new LinearLayout.LayoutParams(0, dp(54), 1));
            row.addView(del, new LinearLayout.LayoutParams(dp(78), dp(44)));
            del.setOnClickListener(v -> {
                if (cats.size() > 1) {
                    new AlertDialog.Builder(this).setTitle("حذف الخانة؟")
                            .setMessage("سيتم حذفها من قائمة الاختيار للمصروفات الجديدة.")
                            .setPositiveButton("حذف", (d,w) -> { cats.remove(index); AppData.saveCategories(this, cats); render(); })
                            .setNegativeButton("إلغاء", null).show();
                } else Toast.makeText(this, "لا يمكن حذف آخر خانة", Toast.LENGTH_SHORT).show();
            });
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(64));
            p.setMargins(0, 0, 0, dp(7)); list.addView(row, p);
        }
    }
}
