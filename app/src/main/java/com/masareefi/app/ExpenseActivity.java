package com.masareefi.app;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import com.google.android.material.snackbar.Snackbar;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExpenseActivity extends BaseActivity {
    TextView total; LinearLayout list; EditText date;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout page = root("المصروفات");
        LinearLayout main = new LinearLayout(this); main.setOrientation(LinearLayout.VERTICAL); main.setPadding(dp(14),dp(12),dp(14),dp(12));
        total = text("إجمالي كل المصروفات: " + AppData.money(allTotal()),19,GREEN_DARK); total.setGravity(Gravity.CENTER); total.setTypeface(null,Typeface.BOLD); total.setBackground(greenCardBackground());
        main.addView(total,new LinearLayout.LayoutParams(-1,dp(66)));
        LinearLayout actions = new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL); actions.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        Button add=button("مصروف ＋",GREEN), report=button("PDF 📄",NAVY);
        actions.addView(add,new LinearLayout.LayoutParams(0,dp(54),1)); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(54),1);rp.setMargins(dp(8),0,0,0);actions.addView(report,rp);
        main.addView(actions,new LinearLayout.LayoutParams(-1,dp(54)));
        ScrollView scroll=new ScrollView(this); list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list); render();
        add.setOnClickListener(v->showAddDialog()); report.setOnClickListener(v->startActivity(new Intent(this,ReportActivity.class)));
        main.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); page.addView(main,new LinearLayout.LayoutParams(-1,0,1)); setContentView(page);
    }

    private double allTotal(){ double t=0;try{JSONArray a=AppData.getExpenses(this);for(int i=0;i<a.length();i++)t+=a.getJSONObject(i).optDouble("amount");}catch(Exception ignored){}return t; }

    private void render(){
        list.removeAllViews();
        try{
            JSONArray a=AppData.getExpenses(this);ArrayList<JSONObject> items=new ArrayList<>();for(int i=0;i<a.length();i++)items.add(a.getJSONObject(i));
            items.sort((x,y)->(y.optString("date","")+y.optString("time","")).compareTo(x.optString("date","")+x.optString("time","")));
            String currentDate="";
            for(JSONObject o:items){
                String d=o.optString("date",""); if(!d.equals(currentDate)){currentDate=d;TextView day=text("📅  "+d+(o.optString("day").isEmpty()?"":"  •  "+o.optString("day")),16,GREEN_DARK);day.setTypeface(null,1);list.addView(day,new LinearLayout.LayoutParams(-1,dp(48)));}
                LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(dp(12),dp(8),dp(10),dp(8));card.setBackground(cardBackground());
                LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
                String item=o.optString("item","").trim(); if(item.isEmpty()) item=o.optString("category","مصروف");
                TextView info=text(item+"  •  "+o.optString("category","أخرى"),16,TEXT);info.setTypeface(null,Typeface.BOLD);
                TextView amount=text(AppData.money(o.optDouble("amount")),16,GREEN_DARK);amount.setGravity(Gravity.CENTER);amount.setTypeface(null,1);amount.setTextDirection(View.TEXT_DIRECTION_LTR);
                top.addView(info,new LinearLayout.LayoutParams(0,dp(44),1));top.addView(amount,new LinearLayout.LayoutParams(dp(125),dp(44)));card.addView(top);
                String notes=o.optString("notes","").trim();TextView note=text(notes.isEmpty()?"📝 لا توجد ملاحظات":"📝 "+notes,13,MUTED);card.addView(note,new LinearLayout.LayoutParams(-1,dp(34)));
                LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER_VERTICAL);
                Button details=button("📝 تفاصيل",GREEN),edit=button("✏️ تعديل",NAVY),del=button("حذف",RED);
                details.setOnClickListener(v->showDetailsDialog(o));edit.setOnClickListener(v->showEditDialog(o));del.setOnClickListener(v->deleteWithUndo(o));
                actions.addView(details,new LinearLayout.LayoutParams(0,dp(42),1));
                LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,dp(42),1);ep.setMargins(dp(6),0,0,0);actions.addView(edit,ep);
                LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(dp(72),dp(42));dp.setMargins(dp(6),0,0,0);actions.addView(del,dp);card.addView(actions);
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(132));cp.setMargins(0,0,0,dp(8));list.addView(card,cp);
            }
            if(items.isEmpty()){TextView empty=text("لا توجد مصروفات حتى الآن.\nاضغط «مصروف ＋» للبدء.",17,MUTED);empty.setGravity(Gravity.CENTER);list.addView(empty,new LinearLayout.LayoutParams(-1,dp(180)));}
        }catch(Exception ignored){}
        total.setText("إجمالي كل المصروفات: "+AppData.money(allTotal()));
    }

    private void deleteWithUndo(JSONObject original){
        if (original == null) return;
        long id = original.optLong("id", -1L);
        if (id < 0) { Toast.makeText(this, "تعذر حذف المصروف", Toast.LENGTH_SHORT).show(); return; }
        final JSONObject removed = AppData.removeExpense(this, id);
        if (removed == null) { Toast.makeText(this, "المصروف غير موجود أو تم حذفه بالفعل", Toast.LENGTH_SHORT).show(); return; }
        render();
        try {
            View anchor = findViewById(android.R.id.content);
            Snackbar sb = Snackbar.make(anchor, "تم حذف المصروف", Snackbar.LENGTH_LONG);
            sb.setAction("تراجع", v -> { AppData.restoreExpense(this, removed); render(); });
            sb.setActionTextColor(GOLD); sb.setBackgroundTint(NAVY_DARK); sb.show();
        } catch (Exception e) { Toast.makeText(this, "تم حذف المصروف", Toast.LENGTH_SHORT).show(); }
    }

    private void showAddDialog(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(8),0,dp(8),0);
        date=new EditText(this);date.setHint("التاريخ");date.setFocusable(false);date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));date.setOnClickListener(v->showDatePicker());
        EditText item=new EditText(this);item.setHint("اسم المصروف مثل: غداء");item.setSingleLine(true);
        Spinner category=new Spinner(this);ArrayList<String> cats=AppData.getCategories(this);category.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,cats));
        EditText amount=new EditText(this);amount.setHint("القيمة بالجنيه");amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText notes=new EditText(this);notes.setHint("تفاصيل / ملاحظات (اختياري)");notes.setGravity(Gravity.TOP|Gravity.RIGHT);notes.setMinLines(3);notes.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        box.addView(date);box.addView(item);box.addView(category);box.addView(amount);box.addView(notes);
        new AlertDialog.Builder(this).setTitle("إضافة مصروف").setView(box).setPositiveButton("حفظ",(d,w)->{String ds=date.getText().toString().trim();double val=0;try{val=Double.parseDouble(amount.getText().toString().trim());}catch(Exception ignored){}if(!ds.isEmpty()&&val>0){AppData.addExpense(this,ds,getDay(ds),category.getSelectedItem().toString(),val,notes.getText().toString().trim(),item.getText().toString().trim());render();}}).setNegativeButton("إلغاء",null).show();
    }

    private void showEditDialog(JSONObject expense){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(8),0,dp(8),0);
        EditText d=new EditText(this);d.setHint("التاريخ");d.setSingleLine(true);d.setText(expense.optString("date",""));
        EditText item=new EditText(this);item.setHint("اسم المصروف");item.setSingleLine(true);item.setText(expense.optString("item", ""));
        EditText cat=new EditText(this);cat.setHint("الخانة");cat.setSingleLine(true);cat.setText(expense.optString("category",""));
        EditText amount=new EditText(this);amount.setHint("القيمة بالجنيه");amount.setSingleLine(true);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);amount.setText(String.valueOf(expense.optDouble("amount",0)));
        EditText notes=new EditText(this);notes.setHint("الملاحظات");notes.setMinLines(3);notes.setGravity(Gravity.TOP|Gravity.RIGHT);notes.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);notes.setText(expense.optString("notes",""));
        box.addView(d);box.addView(item);box.addView(cat);box.addView(amount);box.addView(notes);
        new AlertDialog.Builder(this).setTitle("تعديل المصروف").setView(box).setPositiveButton("حفظ",(dialog,which)->{try{double val=Double.parseDouble(amount.getText().toString().trim());if(val<=0)return;expense.put("date",d.getText().toString().trim());expense.put("day",getDay(d.getText().toString().trim()));expense.put("item",item.getText().toString().trim());expense.put("category",cat.getText().toString().trim());expense.put("amount",val);expense.put("notes",notes.getText().toString().trim());JSONArray a=AppData.getExpenses(this);for(int i=0;i<a.length();i++)if(a.getJSONObject(i).optLong("id")==expense.optLong("id")){a.put(i,expense);break;}AppData.saveExpenses(this,a);render();}catch(Exception ignored){}}).setNegativeButton("إلغاء",null).show();
    }

    private void showDetailsDialog(JSONObject expense){EditText input=new EditText(this);input.setHint("اكتب تفاصيل المصروف هنا...");input.setGravity(Gravity.TOP|Gravity.RIGHT);input.setMinLines(4);input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);input.setText(expense.optString("notes",""));new AlertDialog.Builder(this).setTitle("تفاصيل المصروف").setView(input).setPositiveButton("حفظ",(d,w)->{try{expense.put("notes",input.getText().toString().trim());JSONArray a=AppData.getExpenses(this);for(int i=0;i<a.length();i++)if(a.getJSONObject(i).optLong("id")==expense.optLong("id")){a.put(i,expense);break;}AppData.saveExpenses(this,a);render();}catch(Exception ignored){}}).setNegativeButton("إلغاء",null).show();}
    private void showDatePicker(){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->date.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private String getDay(String ds){try{return new SimpleDateFormat("EEEE",Locale.ENGLISH).format(new SimpleDateFormat("yyyy-MM-dd",Locale.US).parse(ds));}catch(Exception e){return "";}}
}
