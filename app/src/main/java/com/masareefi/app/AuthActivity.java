package com.masareefi.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;

public class AuthActivity extends BaseActivity {
    private static final String SALAWAT = "عليه الصلاة والسلام";
    private LinearLayout box;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showSalawatGate();
    }

    private void showSalawatGate() {
        LinearLayout page = root("مصاريفي");
        ScrollView scroll = new ScrollView(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(dp(22), dp(30), dp(22), dp(30));

        TextView title = text("صلِّ على سيدنا محمد ﷺ", 27, GREEN_DARK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        content.addView(title, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView hint = text("اكتب ردك بالصلاة والسلام على النبي للمتابعة", 17, MUTED);
        hint.setGravity(Gravity.CENTER);
        content.addView(hint, new LinearLayout.LayoutParams(-1, dp(60)));

        TextView phrase = text("صلِّ على سيدنا محمد ﷺ", 22, GREEN_DARK);
        phrase.setGravity(Gravity.CENTER);
        phrase.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        phrase.setBackground(greenCardBackground());
        content.addView(phrase, new LinearLayout.LayoutParams(-1, dp(75)));

        EditText input = new EditText(this);
        input.setHint("عليه الصلاة والسلام");
        input.setGravity(Gravity.CENTER);
        input.setSingleLine(true);
        content.addView(input, new LinearLayout.LayoutParams(-1, dp(58)));

        Button continueBtn = button("متابعة", GREEN);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(58));
        bp.setMargins(0, dp(16), 0, 0);
        content.addView(continueBtn, bp);

        continueBtn.setOnClickListener(v -> {
            String answer = input.getText().toString().trim();
            if (SALAWAT.equals(answer)) showLoveReaction(page);
            else input.setError("اكتب: عليه الصلاة والسلام");
        });

        scroll.addView(content);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
    }

    private void showLoveReaction(LinearLayout page) {
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(20), dp(20), dp(20), dp(30));
        TextView heart = text("❤", 72, RED);
        heart.setGravity(Gravity.CENTER);
        TextView thanks = text("عليه الصلاة والسلام ❤️", 23, GREEN_DARK);
        thanks.setGravity(Gravity.CENTER);
        thanks.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView next = text("بارك الله فيك\nجاري فتح مصاريفي...", 16, MUTED);
        next.setGravity(Gravity.CENTER);
        box.addView(heart, new LinearLayout.LayoutParams(-1, dp(110)));
        box.addView(thanks, new LinearLayout.LayoutParams(-1, dp(55)));
        box.addView(next, new LinearLayout.LayoutParams(-1, dp(75)));
        page.removeAllViews();
        page.addView(box, new LinearLayout.LayoutParams(-1, 0, 1));
        heart.setScaleX(0.5f); heart.setScaleY(0.5f); heart.setAlpha(0.2f);
        heart.animate().scaleX(1.12f).scaleY(1.12f).alpha(1f).setDuration(420).withEndAction(() ->
            heart.animate().scaleX(1f).scaleY(1f).setDuration(180).withEndAction(this::showPasswordScreen).start()
        ).start();
    }

    private void showPasswordScreen() {
        LinearLayout page = root("حماية مصاريفي");
        ScrollView scroll = new ScrollView(this);
        box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(26), dp(22), dp(30));

        boolean hasPassword = AppData.hasPassword(this);
        TextView title = text(hasPassword ? "أدخل كلمة المرور" : "أنشئ كلمة مرور", 25, GREEN_DARK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        box.addView(title, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView sub = text(hasPassword ? "لا يمكن فتح مصاريفي بدون كلمة المرور." : "أنشئ كلمة مرور لحماية مصروفاتك على هذا الجهاز.", 16, MUTED);
        sub.setGravity(Gravity.CENTER);
        box.addView(sub, new LinearLayout.LayoutParams(-1, dp(70)));

        EditText password = passwordField("كلمة المرور");
        box.addView(password, marginParams());

        EditText confirm = null;
        if (!hasPassword) {
            confirm = passwordField("تأكيد كلمة المرور");
            box.addView(confirm, marginParams());
        }

        Button action = button(hasPassword ? "دخول" : "حفظ كلمة المرور", GREEN);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(58));
        ap.setMargins(0, dp(16), 0, 0);
        box.addView(action, ap);

        if (hasPassword) {
            TextView note = text("لإعادة تعيين كلمة المرور، ستحتاج لمسح بيانات التطبيق من إعدادات الهاتف.", 14, MUTED);
            note.setGravity(Gravity.CENTER);
            box.addView(note, new LinearLayout.LayoutParams(-1, dp(70)));
        }

        EditText finalConfirm = confirm;
        action.setOnClickListener(v -> {
            String p = password.getText().toString();
            if (p.length() < 4) { password.setError("كلمة المرور يجب ألا تقل عن 4 أحرف أو أرقام"); return; }
            if (!hasPassword) {
                String c = finalConfirm.getText().toString();
                if (!p.equals(c)) { finalConfirm.setError("كلمتا المرور غير متطابقتين"); return; }
                AppData.setPassword(this, p);
                openApp();
            } else if (AppData.checkPassword(this, p)) openApp();
            else password.setError("كلمة المرور غير صحيحة");
        });

        scroll.addView(box);
        page.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(page);
    }

    private EditText passwordField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setSingleLine(true);
        e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        e.setPadding(dp(14), 0, dp(14), 0);
        return e;
    }

    private LinearLayout.LayoutParams marginParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.setMargins(0, dp(7), 0, dp(7));
        return p;
    }

    private void openApp() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
