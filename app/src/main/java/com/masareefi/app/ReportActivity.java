package com.masareefi.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.core.content.FileProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportActivity extends BaseActivity {
    private int reportType = 0;
    private String from = "", to = "";
    private TextView rangeLabel;
    private Spinner categorySpinner;
    private String selectedCategory = "كل التصنيفات";
    private final String[] types = {"اليوم", "هذا الأسبوع", "هذا الشهر", "نطاق مخصص"};

    private static final int PAGE_W = 595, PAGE_H = 842;
    private static final int LEFT = 42, RIGHT = 553;
    private static final int NAVY = 0xFF203040, GOLD = 0xFFC79A52, TEXT = 0xFF263746;
    private static final int MUTED = 0xFF6F7B87, LINE = 0xFFE2E6EA, ROW_ALT = 0xFFF7F9FA, GREEN = 0xFF168C67;
    private static final int ITEM_X = 52, ITEM_W = 205, CAT_X = 267, CAT_W = 145, AMOUNT_X = 420, AMOUNT_W = 123;
    private static final int TABLE_TOP = 214, ROW_H = 50;

    @Override protected void onCreate(Bundle b) { super.onCreate(b); build(); }

    private void build() {
        LinearLayout page = root("تقارير المصروفات");
        ScrollView scroll = new ScrollView(this);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(18), dp(16), dp(24));

        TextView title = text("📄 تقارير المصروفات", 22, GREEN_DARK);
        title.setTypeface(null, Typeface.BOLD);
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView help = text("أنشئ تقرير PDF مرتب باستخدام المصروفات الحقيقية المحفوظة في Pudget.", 14, MUTED);
        help.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        box.addView(help, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView periodTitle = text("الفترة الزمنية", 15, TEXT);
        periodTitle.setTypeface(null, Typeface.BOLD);
        box.addView(periodTitle, new LinearLayout.LayoutParams(-1, dp(34)));

        Spinner spinner = new Spinner(this);
        spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, types));
        box.addView(spinner, new LinearLayout.LayoutParams(-1, dp(55)));

        TextView categoryTitle = text("التصنيف", 15, TEXT);
        categoryTitle.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams ctp = new LinearLayout.LayoutParams(-1, dp(34));
        ctp.setMargins(0, dp(8), 0, 0);
        box.addView(categoryTitle, ctp);

        categorySpinner = new Spinner(this);
        ArrayList<String> reportCategories = new ArrayList<>();
        reportCategories.add("كل التصنيفات");
        for (String cat : AppData.getCategories(this)) {
            if (cat != null && !cat.trim().isEmpty() && !reportCategories.contains(cat.trim())) reportCategories.add(cat.trim());
        }
        categorySpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, reportCategories));
        box.addView(categorySpinner, new LinearLayout.LayoutParams(-1, dp(55)));
        categorySpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                selectedCategory = reportCategories.get(pos); updateRangeLabel();
            }
        });

        rangeLabel = text("", 14, TEXT); rangeLabel.setGravity(Gravity.CENTER); rangeLabel.setBackground(cardBackground());
        box.addView(rangeLabel, new LinearLayout.LayoutParams(-1, dp(58)));

        Button custom = button("📅 اختيار نطاق مخصص", NAVY);
        box.addView(custom, new LinearLayout.LayoutParams(-1, dp(52)));
        custom.setOnClickListener(v -> { reportType = 3; spinner.setSelection(3); pickCustomRange(); });

        Button generate = button("إنشاء تقرير PDF", GREEN);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, dp(58));
        gp.setMargins(0, dp(16), 0, dp(8)); box.addView(generate, gp);

        Button share = button("مشاركة آخر تقرير", GOLD);
        box.addView(share, new LinearLayout.LayoutParams(-1, dp(54)));
        share.setOnClickListener(v -> {
            String path = getSharedPreferences("report", 0).getString("last_path", "");
            if (path.isEmpty()) Toast.makeText(this, "أنشئ تقريرًا أولًا", Toast.LENGTH_SHORT).show();
            else shareFile(new File(path));
        });

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            public void onNothingSelected(android.widget.AdapterView<?> p) {}
            public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                reportType = pos; updateRangeLabel();
            }
        });
        generate.setOnClickListener(v -> generatePdf());
        scroll.addView(box); page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page); updateRangeLabel();
    }

    private void updateRangeLabel() {
        String period;
        if (reportType == 3 && !from.isEmpty() && !to.isEmpty()) period = "الفترة: " + displayDate(from) + "  →  " + displayDate(to);
        else if (reportType == 3) period = "الفترة: اختر تاريخ البداية ثم تاريخ النهاية";
        else period = "الفترة: " + types[reportType];
        String category = selectedCategory == null || selectedCategory.isEmpty() ? "كل التصنيفات" : selectedCategory;
        rangeLabel.setText(period + "\nالتصنيف: " + category);
    }

    private String displayDate(String iso) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso);
            return new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(d);
        } catch (Exception e) { return iso; }
    }

    private void pickCustomRange() { pickDate(true); }

    private void pickDate(boolean first) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            String s = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            if (first) { from = s; pickSecondDate(); } else { to = s; updateRangeLabel(); }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickSecondDate() {
        Calendar c = Calendar.getInstance();
        try { Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(from); if (startDate != null) c.setTime(startDate); } catch (Exception ignored) {}
        DatePickerDialog dialog = new DatePickerDialog(this, (v, y, m, d) -> {
            to = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d); updateRangeLabel();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        try { Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(from); if (startDate != null) dialog.getDatePicker().setMinDate(startDate.getTime()); } catch (Exception ignored) {}
        dialog.show();
    }

    private ArrayList<JSONObject> selectedExpenses() {
        ArrayList<JSONObject> result = new ArrayList<>();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        Calendar now = Calendar.getInstance();
        String start = today, end = today;
        if (reportType == 1) {
            Calendar s = (Calendar) now.clone();
            s.set(Calendar.DAY_OF_WEEK, s.getFirstDayOfWeek());
            start = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(s.getTime());
        } else if (reportType == 2) {
            start = new SimpleDateFormat("yyyy-MM-01", Locale.US).format(now.getTime());
        } else if (reportType == 3) {
            if (from.isEmpty() || to.isEmpty()) return result;
            start = from; end = to;
        }
        try {
            JSONArray a = AppData.getExpenses(this);
            for (int i=0; i<a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                String d = o.optString("date", "").trim();
                if (d.isEmpty() || d.compareTo(start) < 0 || d.compareTo(end) > 0) continue;
                String category = o.optString("category", "أخرى").trim();
                if (selectedCategory != null && !selectedCategory.isEmpty() && !"كل التصنيفات".equals(selectedCategory) && !selectedCategory.equals(category)) continue;
                result.add(o);
            }
        } catch (Exception ignored) {}
        result.sort((x,y) -> (y.optString("date", "") + y.optString("time", "")).compareTo(x.optString("date", "") + x.optString("time", "")));
        return result;
    }

    private void generatePdf() {
        if (reportType == 3 && (from.isEmpty() || to.isEmpty())) { Toast.makeText(this, "اختر نطاق التاريخ أولًا", Toast.LENGTH_SHORT).show(); return; }
        ArrayList<JSONObject> expenses = selectedExpenses();
        String stamp = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        File base = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (base == null) { Toast.makeText(this, "تعذر الوصول إلى مساحة التخزين الآمنة", Toast.LENGTH_LONG).show(); return; }
        File dir = new File(base, "PudgetReports");
        if (!dir.exists() && !dir.mkdirs()) { Toast.makeText(this, "تعذر إنشاء مجلد التقارير", Toast.LENGTH_LONG).show(); return; }
        String time = new SimpleDateFormat("HHmmss", Locale.US).format(new Date());
        File file = new File(dir, "Pudget_Expense_Receipt_" + stamp + "_" + time + ".pdf");

        PdfDocument doc = new PdfDocument();
        PdfDocument.Page page = null; Canvas canvas = null; int pageNo = 0; float y = TABLE_TOP; double total = 0;
        try {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            for (JSONObject expense : expenses) {
                if (page == null || y + ROW_H > PAGE_H - 105) {
                    if (page != null) finishPage(doc, page, paint, pageNo);
                    pageNo++;
                    page = doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create());
                    canvas = page.getCanvas(); drawHeader(canvas, paint, pageNo, expenses.size()); y = TABLE_TOP;
                }
                if (((int)((y - TABLE_TOP) / ROW_H)) % 2 == 1) {
                    paint.setColor(ROW_ALT); canvas.drawRoundRect(LEFT, y, RIGHT, y + ROW_H, 7, 7, paint);
                }
                String item = expense.optString("item", "").trim(); if (item.isEmpty()) item = "مصروف";
                String category = expense.optString("category", "أخرى").trim(); if (category.isEmpty()) category = "أخرى";
                String amount = String.format(Locale.US, "%.2f EGP", expense.optDouble("amount", 0));
                drawCell(canvas, item, ITEM_X, y + 8, ITEM_W, 13, TEXT, false);
                drawCell(canvas, category, CAT_X, y + 8, CAT_W, 13, TEXT, false);
                drawCell(canvas, amount, AMOUNT_X, y + 8, AMOUNT_W, 13, NAVY, true);
                paint.setColor(LINE); paint.setStrokeWidth(1); canvas.drawLine(LEFT + 4, y + ROW_H, RIGHT - 4, y + ROW_H, paint);
                y += ROW_H; total += expense.optDouble("amount", 0);
            }
            if (page == null) {
                pageNo++; page = doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create());
                canvas = page.getCanvas(); drawHeader(canvas, paint, pageNo, 0); y = TABLE_TOP;
            }
            if (expenses.isEmpty()) { drawText(canvas, "لا توجد مصروفات في الفترة المحددة", LEFT, y + 35, RIGHT - LEFT, 16, MUTED, true, Layout.Alignment.ALIGN_CENTER); y += 100; }
            if (y + 90 > PAGE_H - 70) {
                finishPage(doc, page, paint, pageNo); pageNo++;
                page = doc.startPage(new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create());
                canvas = page.getCanvas(); drawHeader(canvas, paint, pageNo, expenses.size()); y = TABLE_TOP;
            }
            drawTotalCard(canvas, paint, y + 14, total); finishPage(doc, page, paint, pageNo);
            try (FileOutputStream out = new FileOutputStream(file)) { doc.writeTo(out); }
            doc.close();
            getSharedPreferences("report", 0).edit().putString("last_path", file.getAbsolutePath()).apply();
            Toast.makeText(this, "تم إنشاء التقرير بنجاح", Toast.LENGTH_LONG).show();
            shareFile(file);
        } catch (Exception e) {
            try { doc.close(); } catch (Exception ignored) {}
            if (file.exists()) file.delete();
            Toast.makeText(this, "تعذر إنشاء PDF. حاول مرة أخرى.", Toast.LENGTH_LONG).show();
        }
    }

    private void drawHeader(Canvas c, Paint p, int pageNo, int count) {
        p.setColor(NAVY); c.drawRect(0, 0, PAGE_W, 112, p);
        p.setColor(GOLD); c.drawRoundRect(LEFT, 25, LEFT + 52, 77, 12, 12, p);
        drawText(c, "P", LEFT, 25, 52, 26, NAVY, true, Layout.Alignment.ALIGN_CENTER);
        drawText(c, "PUDGET", 116, 27, 150, 22, 0xFFFFFFFF, true, Layout.Alignment.ALIGN_NORMAL);
        drawText(c, "تقرير المصروفات", 116, 58, 210, 15, 0xFFE8EDF2, false, Layout.Alignment.ALIGN_OPPOSITE);
        String generated = new SimpleDateFormat("dd MMM yyyy  •  hh:mm a", Locale.ENGLISH).format(new Date());
        drawText(c, generated, 350, 35, 185, 10, 0xFFD8E0E7, false, Layout.Alignment.ALIGN_OPPOSITE);
        p.setColor(0xFFE9EEF2); c.drawRoundRect(LEFT, 132, RIGHT, 181, 10, 10, p);
        drawText(c, "الفترة", LEFT + 15, 142, 65, 11, MUTED, true, Layout.Alignment.ALIGN_OPPOSITE);
        drawText(c, periodText(), LEFT + 85, 142, 300, 11, NAVY, true, Layout.Alignment.ALIGN_CENTER);
        drawText(c, "عدد المصروفات: " + count, LEFT + 390, 142, 148, 10, MUTED, false, Layout.Alignment.ALIGN_OPPOSITE);
        p.setColor(NAVY); c.drawRoundRect(LEFT, 192, RIGHT, 216, 7, 7, p);
        drawText(c, "الصنف", ITEM_X, 193, ITEM_W, 11, 0xFFFFFFFF, true, Layout.Alignment.ALIGN_CENTER);
        drawText(c, "التصنيف", CAT_X, 193, CAT_W, 11, 0xFFFFFFFF, true, Layout.Alignment.ALIGN_CENTER);
        drawText(c, "المبلغ", AMOUNT_X, 193, AMOUNT_W, 11, 0xFFFFFFFF, true, Layout.Alignment.ALIGN_CENTER);
    }

    private String periodText() {
        String period;
        if (reportType == 0) period = "اليوم";
        else if (reportType == 1) period = "هذا الأسبوع";
        else if (reportType == 2) period = "هذا الشهر";
        else period = displayDate(from) + "  →  " + displayDate(to);
        if (!"كل التصنيفات".equals(selectedCategory)) period += "  •  " + selectedCategory;
        return period;
    }

    private void drawTotalCard(Canvas c, Paint p, float top, double total) {
        p.setColor(0xFFEAF5F0); c.drawRoundRect(LEFT, top, RIGHT, top + 86, 12, 12, p);
        drawText(c, "الإجمالي", LEFT + 18, top + 17, 110, 15, GREEN, true, Layout.Alignment.ALIGN_OPPOSITE);
        drawText(c, String.format(Locale.US, "%.2f EGP", total), 325, top + 16, 210, 21, NAVY, true, Layout.Alignment.ALIGN_OPPOSITE);
        drawText(c, "إجمالي المصروفات المحددة في التقرير", LEFT + 18, top + 49, 300, 9, MUTED, false, Layout.Alignment.ALIGN_OPPOSITE);
    }

    private void finishPage(PdfDocument doc, PdfDocument.Page page, Paint p, int pageNo) {
        p.setColor(0xFFD9DEE2); p.setStrokeWidth(1);
        page.getCanvas().drawLine(LEFT, PAGE_H - 48, RIGHT, PAGE_H - 48, p);
        drawText(page.getCanvas(), "Pudget  •  Expense Report  •  Page " + pageNo, LEFT, PAGE_H - 39, RIGHT - LEFT, 9, 0xFF98A3AD, false, Layout.Alignment.ALIGN_NORMAL);
        doc.finishPage(page);
    }

    private void drawCell(Canvas canvas, String value, float x, float y, float width, float size, int color, boolean bold) {
        drawText(canvas, value, x, y, width, size, color, bold, Layout.Alignment.ALIGN_CENTER);
    }

    private void drawText(Canvas canvas, String value, float x, float y, float width, float size, int color, boolean bold, Layout.Alignment alignment) {
        TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tp.setColor(color); tp.setTextSize(size);
        tp.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL)); tp.setSubpixelText(true);
        StaticLayout layout = new StaticLayout(value == null ? "" : value, tp, (int) width, alignment, 1.0f, 0f, false);
        canvas.save(); canvas.translate(x, y); layout.draw(canvas); canvas.restore();
    }

    private void shareFile(File file) {
        if (file == null || !file.exists()) { Toast.makeText(this, "التقرير غير موجود", Toast.LENGTH_SHORT).show(); return; }
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent i = new Intent(Intent.ACTION_SEND); i.setType("application/pdf");
            i.putExtra(Intent.EXTRA_STREAM, uri); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "مشاركة تقرير PDF"));
        } catch (Exception e) { Toast.makeText(this, "تعذر مشاركة الملف", Toast.LENGTH_SHORT).show(); }
    }
}
