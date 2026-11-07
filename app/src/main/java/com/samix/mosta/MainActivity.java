
package com.samix.mosta;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(8, 35, 29));
        getWindow().setNavigationBarColor(Color.rgb(8, 35, 29));

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(22, 30, 22, 20);
        layout.setBackgroundColor(Color.rgb(8, 20, 18));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        TextView title = new TextView(this);
        title.setText("🇩🇿 SAMIX MOSTA");
        title.setTextColor(Color.rgb(225, 190, 90));
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(10, 25, 10, 15);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("محاكي مورس والاتصالات اللاسلكية\nالإصدار 4.0");
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(10, 5, 10, 30);
        layout.addView(subtitle);

        addButton("📡 محاكي اللاسلكي", "radio");
        addButton("🔠 مترجم شفرة مورس", "morse");
        addButton("📚 دروس مورس", "lessons");
        addButton("🎯 اختبار المعرفة", "quiz");
        addButton("⚙️ الإعدادات", "settings");
        addButton("ℹ️ حول التطبيق", "about");

        setContentView(scroll);
    }

    void addButton(String name, String page) {
        Button button = new Button(this);
        button.setText(name);
        button.setTextColor(Color.WHITE);
        button.setTextSize(17);
        button.setAllCaps(false);
        button.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                Color.rgb(25, 75, 57)
            )
        );

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 65
            );
        params.setMargins(0, 8, 0, 8);
        layout.addView(button, params);

        button.setOnClickListener(v -> openPage(page));
    }

    void openPage(String page) {
        String message;

        switch (page) {
            case "radio":
                message = "محاكي الاتصالات اللاسلكية";
                break;
            case "morse":
                message = "مترجم شفرة مورس";
                break;
            case "lessons":
                message = "دروس تعلم شفرة مورس";
                break;
            case "quiz":
                message = "اختبار شفرة مورس";
                break;
            case "settings":
                message = "إعدادات التطبيق";
                break;
            default:
                message = "SAMIX MOSTA — الإصدار 4.0";
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
