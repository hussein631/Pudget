package com.masareefi.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

public class BaseActivity extends Activity {
    protected static int NAVY, NAVY_DARK, GOLD, GOLD_SOFT, GREEN, GREEN_DARK, CREAM, TEXT, MUTED, RED, CARD;

    @Override protected void onCreate(Bundle b) {
        applyPalette(AppData.getTheme(this));
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY_DARK);
        getWindow().setNavigationBarColor(NAVY_DARK);
    }

    protected void applyPalette(String theme) {
        if (AppData.THEME_DARK.equals(theme)) {
            NAVY=0xFF243447; NAVY_DARK=0xFF111820; GOLD=0xFFD5AD63; GOLD_SOFT=0xFF3A3023;
            GREEN=0xFF43A68C; GREEN_DARK=0xFF72D0B6; CREAM=0xFF12171C; TEXT=0xFFE8EDF2; MUTED=0xFF9BA8B5; RED=0xFFE06A7D; CARD=0xFF1D252D;
        } else if (AppData.THEME_BLUE.equals(theme)) {
            NAVY=0xFF1D4E89; NAVY_DARK=0xFF12365F; GOLD=0xFF4D8FD6; GOLD_SOFT=0xFFE4EFFB;
            GREEN=0xFF2374B9; GREEN_DARK=0xFF1D5D96; CREAM=0xFFF3F7FC; TEXT=0xFF1D2A38; MUTED=0xFF66778A; RED=0xFFB84B5E; CARD=0xFFFFFFFF;
        } else if (AppData.THEME_PURPLE.equals(theme)) {
            NAVY=0xFF4B3F72; NAVY_DARK=0xFF30264D; GOLD=0xFFB28AE8; GOLD_SOFT=0xFFEDE7F8;
            GREEN=0xFF7456A8; GREEN_DARK=0xFF59408C; CREAM=0xFFF7F4FB; TEXT=0xFF282332; MUTED=0xFF766F83; RED=0xFFB84D6A; CARD=0xFFFFFFFF;
        } else if (AppData.THEME_LIGHT.equals(theme)) {
            NAVY=0xFF34495E; NAVY_DARK=0xFF253545; GOLD=0xFFB88735; GOLD_SOFT=0xFFF6EBD8;
            GREEN=0xFF287D68; GREEN_DARK=0xFF176653; CREAM=0xFFF8F9FA; TEXT=0xFF24313D; MUTED=0xFF71808F; RED=0xFFB54559; CARD=0xFFFFFFFF;
        } else {
            NAVY=0xFF203040; NAVY_DARK=0xFF162330; GOLD=0xFFC79A52; GOLD_SOFT=0xFFF6EBD8;
            GREEN=0xFF147D68; GREEN_DARK=0xFF075C4D; CREAM=0xFFF7F3EC; TEXT=0xFF1C2A39; MUTED=0xFF667384; RED=0xFFB23A4F; CARD=0xFFFFFDF9;
        }
    }

    protected LinearLayout root(String title) {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(CREAM);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextView bar = text(title, 22, Color.WHITE);
        bar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        bar.setGravity(Gravity.CENTER);
        bar.setBackgroundColor(NAVY_DARK);
        page.addView(bar, new LinearLayout.LayoutParams(-1, dp(62)));
        return page;
    }

    protected TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(resolveColor(color));
        t.setGravity(Gravity.CENTER_VERTICAL); t.setPadding(dp(10), dp(6), dp(10), dp(6));
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); t.setIncludeFontPadding(true);
        return t;
    }

    private int resolveColor(int color) {
        if (color == 0xFF203040) return NAVY;
        if (color == 0xFF162330) return NAVY_DARK;
        if (color == 0xFFC79A52) return GOLD;
        if (color == 0xFFF6EBD8) return GOLD_SOFT;
        if (color == 0xFF147D68) return GREEN;
        if (color == 0xFF075C4D) return GREEN_DARK;
        if (color == 0xFFF7F3EC) return CREAM;
        if (color == 0xFF1C2A39) return TEXT;
        if (color == 0xFF667384) return MUTED;
        if (color == 0xFFB23A4F) return RED;
        return color;
    }

    protected Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label); b.setTextSize(16); b.setTextColor(Color.WHITE); b.setAllCaps(false);
        b.setGravity(Gravity.CENTER); b.setStateListAnimator(null);
        b.setBackground(rounded(resolveColor(color), 16)); b.setPadding(dp(8),0,dp(8),0);
        return b;
    }

    protected GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color); bg.setCornerRadius(dp((int)radiusDp)); return bg;
    }

    protected GradientDrawable cardBackground() { return rounded(CARD, 18); }
    protected GradientDrawable greenCardBackground() { return rounded(themeSoftGreen(), 18); }
    protected GradientDrawable budgetBackground() { return rounded(themeSoftGold(), 18); }
    protected GradientDrawable heroBackground() { return rounded(NAVY_DARK, 22); }
    protected GradientDrawable expenseBackground() { return rounded(AppData.THEME_DARK.equals(AppData.getTheme(this)) ? 0xFF352229 : 0xFFF9E9EC, 18); }
    protected GradientDrawable incomeBackground() { return rounded(AppData.THEME_DARK.equals(AppData.getTheme(this)) ? 0xFF20352F : 0xFFEAF3EF, 18); }
    protected int themeSoftGreen() { return AppData.THEME_DARK.equals(AppData.getTheme(this)) ? 0xFF20352F : blend(GREEN, CREAM, 0.88f); }
    protected int themeSoftGold() { return AppData.THEME_DARK.equals(AppData.getTheme(this)) ? 0xFF332D23 : blend(GOLD, CREAM, 0.88f); }
    private int blend(int a, int b, float ratio) {
        int ar=Color.red(a), ag=Color.green(a), ab=Color.blue(a);
        int br=Color.red(b), bg=Color.green(b), bb=Color.blue(b);
        return Color.rgb((int)(ar*ratio+br*(1-ratio)),(int)(ag*ratio+bg*(1-ratio)),(int)(ab*ratio+bb*(1-ratio)));
    }
    protected int dp(int x) { return (int)(x * getResources().getDisplayMetrics().density + 0.5f); }
}
