
package com.samix.mosta;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.media.*;
import android.content.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
    MediaRecorder recorder;
    MediaPlayer player;
    String audioPath;
    boolean recording = false;
    boolean radioOn = false;

    final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    final String[] morse = {
        ".-", "-...", "-.-.", "-..", ".", "..-.",
        "--.", "....", "..", ".---", "-.-", ".-..",
        "--", "-.", "---", ".--.", "--.-", ".-.",
        "...", "-", "..-", "...-", ".--", "-..-",
        "-.--", "--..",
        "-----", ".----", "..---", "...--", "....-",
        ".....", "-....", "--...", "---..", "----."
    };

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(5, 16, 14));
        getWindow().setNavigationBarColor(Color.rgb(5, 16, 14));
        showHome();
    }

    GradientDrawable shape(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(24);
        d.setStroke(2, Color.rgb(225,190,90));
        return d;
    }

    TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(12, 12, 12, 12);
        return t;
    }

    Button button(String value, Runnable action) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackground(shape(Color.rgb(25,75,57)));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(8, 8, 8, 8);
        b.setLayoutParams(p);
        return b;
    }

    void setup(String title) {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14, 18, 14, 20);

        try {
            root.setBackgroundResource(R.drawable.background_samix);
        } catch (Exception e) {
            root.setBackgroundColor(Color.rgb(8,20,18));
        }

        scroll.addView(root);
        setContentView(scroll);

        TextView heading = text("SAMIX MOSTA  •  4.0\n" + title, 22);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setBackground(shape(Color.rgb(5,25,20)));
        root.addView(heading);

        TextView identity = text("عزيز 29  •  سامي 27  •  دبشونة", 14);
        root.addView(identity);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);
    }

    void showHome() {
        setup("محاكي مورس والاتصالات");

        content.addView(text("مرحباً بك في SAMIX MOSTA", 21));
        content.addView(text("تعلم مورس وجرب جهاز الاتصال اللاسلكي في محاكاة تعليمية.", 15));

        content.addView(button("📡 محاكي اللاسلكي", () -> showRadio()));
        content.addView(button("🔤 مترجم مورس", () -> showTranslator()));
        content.addView(button("📚 دروس مورس", () -> showLessons()));
        content.addView(button("🧠 اختبار معلومات", () -> showQuiz()));
        content.addView(button("⚙️ الإعدادات", () -> showSettings()));
        content.addView(button("ℹ️ حول التطبيق", () -> showAbout()));
    }

    String encode(String input) {
        StringBuilder result = new StringBuilder();
        String upper = input.toUpperCase(Locale.ROOT);

        for (char c : upper.toCharArray()) {
            if (c == ' ') {
                result.append("/ ");
                continue;
            }
            int index = alphabet.indexOf(c);
            if (index >= 0) result.append(morse[index]).append(" ");
        }
        return result.toString().trim();
    }

    String decode(String input) {
        StringBuilder result = new StringBuilder();
        String[] words = input.trim().split("\\s*/\\s*");

        for (String word : words) {
            String[] letters = word.trim().split("\\s+");
            for (String code : letters) {
                for (int i = 0; i < morse.length; i++) {
                    if (morse[i].equals(code)) {
                        result.append(alphabet.charAt(i));
                        break;
                    }
                }
            }
            result.append(" ");
        }
        return result.toString().trim();
    }

    void showTranslator() {
        setup("مترجم مورس");

        EditText input = new EditText(this);
        input.setHint("اكتب نصاً أو شفرة مورس");
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setBackground(shape(Color.rgb(5,25,20)));
        content.addView(input);

        TextView output = text("النتيجة ستظهر هنا", 18);
        output.setBackground(shape(Color.rgb(5,25,20)));
        content.addView(output);

        content.addView(button("تحويل النص إلى مورس", () ->
            output.setText(encode(input.getText().toString()))));

        content.addView(button("تحويل مورس إلى نص", () ->
            output.setText(decode(input.getText().toString()))));

        content.addView(button("نسخ النتيجة", () -> {
            android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(
                ClipData.newPlainText("Morse", output.getText()));
            Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT).show();
        }));

        content.addView(button("رجوع", () -> showHome()));
    }

    void showLessons() {
        setup("دروس مورس");

        String[] lessons = {
            "الدرس 1: النقطة والشرطة\nالنقطة قصيرة والشرطة أطول.",
            "الدرس 2: الحروف\nA = .-     B = -...     C = -.-.",
            "الدرس 3: الأرقام\n0 = -----     1 = .----",
            "الدرس 4: الكلمات\nنفصل بين الحروف بمسافة وبين الكلمات بالرمز /"
        };

        for (String lesson : lessons) {
            TextView t = text(lesson, 17);
            t.setBackground(shape(Color.rgb(5,25,20)));
            content.addView(t);
        }

        content.addView(button("رجوع", () -> showHome()));
    }

    void showQuiz() {
        setup("اختبار مورس");

        content.addView(text("ما هو رمز الحرف A؟", 20));

        content.addView(button(".-", () -> answer(true)));
        content.addView(button("-...", () -> answer(false)));
        content.addView(button("--.", () -> answer(false)));

        content.addView(button("رجوع", () -> showHome()));
    }

    void answer(boolean correct) {
        new AlertDialog.Builder(this)
            .setTitle(correct ? "إجابة صحيحة" : "حاول مرة أخرى")
            .setMessage(correct ? "أحسنت! الحرف A رمزه .-" :
                "الإجابة الصحيحة هي .-")
            .setPositiveButton("موافق", null)
            .show();
    }

    void showRadio() {
        setup("محاكي اللاسلكي");

        TextView status = text("الجهاز متوقف", 20);
        status.setBackground(shape(Color.rgb(5,25,20)));
        content.addView(status);

        content.addView(button("تشغيل / إيقاف الجهاز", () -> {
            radioOn = !radioOn;
            status.setText(radioOn ? "🟢 الجهاز يعمل" : "🔴 الجهاز متوقف");
        }));

        content.addView(button("بدء التسجيل", () -> startRecording()));
        content.addView(button("إنهاء التسجيل", () -> stopRecording()));
        content.addView(button("إعادة الاستماع", () -> playRecording()));
        content.addView(button("نغمة بداية الاتصال", () -> beep(900)));
        content.addView(button("نغمة نهاية الاتصال", () -> beep(500)));

        content.addView(text(
            "هذا محاكي تعليمي محلي، ولا يتصل بأجهزة أو ترددات حقيقية.", 14));

        content.addView(button("رجوع", () -> showHome()));
    }

    void startRecording() {
        if (!radioOn) {
            Toast.makeText(this, "شغّل الجهاز أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission("android.permission.RECORD_AUDIO")
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{"android.permission.RECORD_AUDIO"}, 10);
                return;
            }

            audioPath = getCacheDir().getAbsolutePath() + "/radio_record.m4a";

            if (recorder != null) recorder.release();

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();

            recording = true;
            Toast.makeText(this, "بدأ التسجيل", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "تعذر بدء التسجيل", Toast.LENGTH_SHORT).show();
        }
    }

    void stopRecording() {
        try {
            if (recorder != null && recording) {
                recorder.stop();
                recorder.release();
                recorder = null;
                recording = false;
                Toast.makeText(this, "انتهى التسجيل", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "لم يكتمل التسجيل", Toast.LENGTH_SHORT).show();
        }
    }

    void playRecording() {
        if (audioPath == null) {
            Toast.makeText(this, "لا يوجد تسجيل بعد", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (player != null) {
                player.release();
                player = null;
            }

            player = new MediaPlayer();
            player.setDataSource(audioPath);
            player.prepare();
            player.start();

        } catch (Exception e) {
            Toast.makeText(this, "تعذر تشغيل التسجيل", Toast.LENGTH_SHORT).show();
        }
    }

    void beep(int frequency) {
        try {
            final int sampleRate = 44100;
            final int duration = 180;
            final int count = sampleRate * duration / 1000;
            byte[] sound = new byte[count];

            for (int i = 0; i < count; i++) {
                sound[i] = (byte)(Math.sin(
                    2 * Math.PI * frequency * i / sampleRate) * 100);
            }

            AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC, sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT,
                count, AudioTrack.MODE_STATIC);

            track.write(sound, 0, count);
            track.play();

        } catch (Exception ignored) { }
    }

    void showSettings() {
        setup("الإعدادات");

        content.addView(text("إعدادات SAMIX MOSTA", 20));
        content.addView(text("نسخة التطبيق: 4.0", 16));
        content.addView(text("الخلفية: صورة السيارة المضافة", 16));
        content.addView(text("الصوت الموسيقي سيضاف بعد إرسال الملف الصوتي المطلوب.", 15));

        content.addView(button("رجوع", () -> showHome()));
    }

    void showAbout() {
        setup("حول التطبيق");

        content.addView(text("SAMIX MOSTA 4.0", 24));
        content.addView(text(
            "تطبيق تعليمي لمحاكاة شفرة مورس والاتصالات اللاسلكية.\n\n" +
            "تصميم جزائري بطابع أخضر وذهبي.", 17));

        content.addView(button("رجوع", () -> showHome()));
    }

    @Override
    protected void onDestroy() {
        try {
            if (recorder != null) recorder.release();
            if (player != null) player.release();
        } catch (Exception ignored) { }

        super.onDestroy();
    }
}
