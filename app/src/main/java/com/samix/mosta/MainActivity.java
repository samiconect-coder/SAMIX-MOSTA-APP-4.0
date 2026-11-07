
package com.samix.mosta;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout root, content;

    private MediaPlayer music;
    private MediaPlayer player;
    private MediaRecorder recorder;
    private ToneGenerator toneGenerator;

    private String audioPath;

    private boolean musicOn = true;
    private boolean radioOn = false;
    private boolean recording = false;
    private boolean permissionWaiting = false;

    private int musicVolume = 55;

    private final int GOLD = Color.rgb(225, 190, 90);
    private final int GREEN = Color.rgb(19, 65, 49);
    private final int DARK = Color.rgb(5, 20, 16);
    private final int WHITE = Color.WHITE;

    private final String alphabet =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final String[] morse = {
            ".-", "-...", "-.-.", "-..", ".", "..-.",
            "--.", "....", "..", ".---", "-.-", ".-..",
            "--", "-.", "---", ".--.", "--.-", ".-.",
            "...", "-", "..-", "...-", ".--", "-..-",
            "-.--", "--..",
            "-----", ".----", "..---", "...--", "....-",
            ".....", "-....", "--...", "---..", "----."
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);

        startBackgroundMusic();
        showHome();
    }

    private GradientDrawable panel(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(25);
        drawable.setStroke(2, GOLD);
        return drawable;
    }

    private TextView makeText(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(WHITE);
        text.setGravity(Gravity.CENTER);
        text.setPadding(14, 14, 14, 14);
        return text;
    }

    private TextView makeCard(String value, int size) {
        TextView text = makeText(value, size);
        text.setBackground(panel(Color.rgb(7, 29, 22)));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(4, 7, 4, 7);
        text.setLayoutParams(params);

        return text;
    }

    private Button makeButton(String value, Runnable action) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(WHITE);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(panel(GREEN));
        button.setElevation(4);

        button.setOnClickListener(v -> {
            if (action != null) action.run();
        });

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(5, 7, 5, 7);
        button.setLayoutParams(params);

        return button;
    }

    private void setup(String pageTitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 18, 16, 24);

        try {
            root.setBackgroundResource(R.drawable.background_samix);
        } catch (Exception e) {
            root.setBackgroundColor(DARK);
        }

        scroll.addView(root);
        setContentView(scroll);

        TextView header = makeText(
                "SAMIX MOSTA\n" + pageTitle, 23);

        header.setTypeface(null, Typeface.BOLD);
        header.setBackground(
                panel(Color.rgb(5, 25, 19)));

        root.addView(header);

        TextView identity = makeText(
                "عزيز 29  •  سامي 27  •  دبشونة", 13);

        root.addView(identity);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);

        TextView footer = makeText(
                "SAMIX MOSTA  •  VERSION 4.0", 11);

        footer.setTextColor(GOLD);
        root.addView(footer);
    }

    // الموسيقى الخلفية

    private void startBackgroundMusic() {
        if (!musicOn || isFinishing()) return;

        try {
            if (music == null) {
                music = MediaPlayer.create(
                        this, R.raw.background_music);
            }

            if (music != null) {
                music.setLooping(true);
                setMusicVolume();

                if (!music.isPlaying()) {
                    music.start();
                }
            }
        } catch (Exception e) {
            releaseMusic();
        }
    }

    private void setMusicVolume() {
        if (music != null) {
            try {
                float volume = musicVolume / 100f;
                music.setVolume(volume, volume);
            } catch (Exception ignored) {
            }
        }
    }

    private void pauseMusic() {
        try {
            if (music != null && music.isPlaying()) {
                music.pause();
            }
        } catch (Exception ignored) {
        }
    }

    private void resumeMusic() {
        if (musicOn && !recording) {
            startBackgroundMusic();
        }
    }

    private void releaseMusic() {
        try {
            if (music != null) {
                if (music.isPlaying()) music.stop();
                music.release();
                music = null;
            }
        } catch (Exception ignored) {
            music = null;
        }
    }

    // الصفحة الرئيسية

    private void showHome() {
        setup("المركز الرئيسي");

        content.addView(
                makeCard("مرحباً بك في SAMIX MOSTA", 22));

        content.addView(makeCard(
                "منصة تعليمية لمحاكاة شفرة مورس والاتصالات اللاسلكية.",
                15));

        content.addView(makeButton(
                "📡 محاكي اللاسلكي", this::showRadio));

        content.addView(makeButton(
                "🔤 مترجم شفرة مورس", this::showTranslator));

        content.addView(makeButton(
                "📚 أكاديمية مورس", this::showLessons));

        content.addView(makeButton(
                "🧠 الاختبار التفاعلي", this::showQuiz));

        content.addView(makeButton(
                "🎵 إعدادات الصوت", this::showSettings));

        content.addView(makeButton(
                "ℹ️ حول التطبيق", this::showAbout));
    }

    // تشفير مورس

    private String encode(String input) {
        StringBuilder result = new StringBuilder();
        String upper = input.toUpperCase(Locale.ROOT);

        for (int i = 0; i < upper.length(); i++) {
            char character = upper.charAt(i);

            if (character == ' ') {
                result.append("/ ");
                continue;
            }

            int index = alphabet.indexOf(character);

            if (index >= 0) {
                result.append(morse[index]).append(" ");
            }
        }

        return result.toString().trim();
    }

    // فك شفرة مورس

    private String decode(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] words = input.trim().split("\\s*/\\s*");

        for (int w = 0; w < words.length; w++) {
            String word = words[w].trim();

            if (!word.isEmpty()) {
                String[] letters = word.split("\\s+");

                for (String code : letters) {
                    boolean found = false;

                    for (int i = 0; i < morse.length; i++) {
                        if (morse[i].equals(code)) {
                            result.append(alphabet.charAt(i));
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        result.append("?");
                    }
                }
            }

            if (w < words.length - 1) {
                result.append(" ");
            }
        }

        return result.toString();
    }

    private void showTranslator() {
        setup("مترجم مورس");

        EditText input = new EditText(this);
        input.setTextColor(WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setHint("اكتب النص أو شفرة مورس هنا");
        input.setMinLines(3);
        input.setGravity(Gravity.TOP);
        input.setPadding(15, 15, 15, 15);
        input.setBackground(panel(DARK));

        content.addView(input);

        TextView output = makeCard(
                "ستظهر النتيجة هنا", 18);

        content.addView(output);

        content.addView(makeButton(
                "تحويل النص إلى شفرة مورس",
                () -> {
                    String result = encode(
                            input.getText().toString());

                    output.setText(result.isEmpty()
                            ? "لم يتم إدخال نص صالح"
                            : result);
                }));

        content.addView(makeButton(
                "تحويل شفرة مورس إلى نص",
                () -> {
                    String result = decode(
                            input.getText().toString());

                    output.setText(result.isEmpty()
                            ? "أدخل شفرة مورس أولاً"
                            : result);
                }));

        content.addView(makeButton(
                "نسخ النتيجة",
                () -> {
                    ClipboardManager clipboard =
                            (ClipboardManager) getSystemService(
                                    CLIPBOARD_SERVICE);

                    if (clipboard != null) {
                        clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                        "Morse",
                                        output.getText()));

                        Toast.makeText(
                                this,
                                "تم نسخ النتيجة",
                                Toast.LENGTH_SHORT).show();
                    }
                }));

        content.addView(makeButton(
                "رجوع", this::showHome));
    }

    // أكاديمية مورس

    private void showLessons() {
        setup("أكاديمية مورس");

        content.addView(makeCard(
                "الدرس 01 — الأساسيات\nالنقطة قصيرة والشرطة أطول.",
                17));

        content.addView(makeCard(
                "الدرس 02 — الحروف\nA = .-    B = -...    C = -.-.",
                17));

        content.addView(makeCard(
                "الدرس 03 — الأرقام\n0 = -----    1 = .----",
                17));

        content.addView(makeCard(
                "الدرس 04 — تكوين الكلمات\nنفصل بين الحروف بمسافة، وبين الكلمات بالرمز /.",
                17));

        content.addView(makeButton(
                "رجوع", this::showHome));
    }

    // الاختبار التفاعلي

    private void showQuiz() {
        setup("الاختبار التفاعلي");

        content.addView(makeCard(
                "السؤال الأول\nما هو رمز الحرف A؟", 20));

        content.addView(makeButton(
                ".-", () -> answer(true)));

        content.addView(makeButton(
                "-...", () -> answer(false)));

        content.addView(makeButton(
                "--.", () -> answer(false)));

        content.addView(makeButton(
                "رجوع", this::showHome));
    }

    private void answer(boolean correct) {
        new AlertDialog.Builder(this)
                .setTitle(correct
                        ? "إجابة صحيحة"
                        : "حاول مرة أخرى")
                .setMessage(correct
                        ? "أحسنت! رمز الحرف A هو .-"
                        : "الإجابة الصحيحة هي .-")
                .setPositiveButton("موافق", null)
                .show();
    }

    // محاكي اللاسلكي

    private void showRadio() {
        setup("محاكي اللاسلكي");

        TextView status = makeCard(
                radioOn
                        ? "🟢 الجهاز يعمل"
                        : "🔴 الجهاز متوقف",
                20);

        content.addView(status);

        content.addView(makeButton(
                "تشغيل / إيقاف الجهاز",
                () -> {
                    radioOn = !radioOn;

                    status.setText(radioOn
                            ? "🟢 الجهاز يعمل"
                            : "🔴 الجهاز متوقف");
                }));

        content.addView(makeButton(
                "🎙 بدء التسجيل",
                this::startRecording));

        content.addView(makeButton(
                "⏹ إنهاء التسجيل",
                this::stopRecording));

        content.addView(makeButton(
                "▶ إعادة الاستماع",
                this::playRecording));

        content.addView(makeButton(
                "نغمة بداية الاتصال",
                () -> playBeep(ToneGenerator.TONE_PROP_BEEP)));

        content.addView(makeButton(
                "نغمة نهاية الاتصال",
                () -> playBeep(ToneGenerator.TONE_PROP_ACK)));

        content.addView(makeCard(
                "محاكي تعليمي محلي. لا يتصل بأجهزة أو ترددات حقيقية.",
                14));

        content.addView(makeButton(
                "رجوع", this::showHome));
    }

    private void startRecording() {
        if (!radioOn) {
            Toast.makeText(
                    this,
                    "شغّل الجهاز أولاً",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (recording) {
            Toast.makeText(
                    this,
                    "التسجيل يعمل بالفعل",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            permissionWaiting = true;

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    100);

            return;
        }

        beginRecording();
    }

    private void beginRecording() {
        try {
            pauseMusic();
            releaseRecorder();

            File file = new File(
                    getCacheDir(),
                    "radio_record.m4a");

            audioPath = file.getAbsolutePath();

            recorder = new MediaRecorder();

            recorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC);

            recorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4);

            recorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC);

            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();

            recording = true;

            Toast.makeText(
                    this,
                    "بدأ التسجيل",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            recording = false;
            releaseRecorder();
            resumeMusic();

            Toast.makeText(
                    this,
                    "تعذر بدء التسجيل",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (!recording || recorder == null) {
            Toast.makeText(
                    this,
                    "لا يوجد تسجيل يعمل حالياً",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            recorder.stop();

            Toast.makeText(
                    this,
                    "انتهى التسجيل",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "التسجيل قصير جداً أو لم يكتمل",
                    Toast.LENGTH_SHORT).show();

        } finally {
            recording = false;
            releaseRecorder();
            resumeMusic();
        }
    }

    private void releaseRecorder() {
        try {
            if (recorder != null) {
                recorder.reset();
                recorder.release();
            }
        } catch (Exception ignored) {
        }

        recorder = null;
    }

    private void playRecording() {
        if (recording) {
            Toast.makeText(
                    this,
                    "أنه التسجيل أولاً",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (audioPath == null ||
                !new File(audioPath).exists()) {

            Toast.makeText(
                    this,
                    "لا يوجد تسجيل بعد",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            pauseMusic();
            releasePlayer();

            player = new MediaPlayer();
            player.setDataSource(audioPath);

            player.setOnCompletionListener(mp -> {
                releasePlayer();
                resumeMusic();
            });

            player.setOnErrorListener((mp, what, extra) -> {
                releasePlayer();
                resumeMusic();
                Toast.makeText(
                        this,
                        "تعذر تشغيل التسجيل",
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            player.prepare();
            player.start();

        } catch (Exception e) {
            releasePlayer();
            resumeMusic();

            Toast.makeText(
                    this,
                    "تعذر تشغيل التسجيل",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void releasePlayer() {
        try {
            if (player != null) {
                player.release();
            }
        } catch (Exception ignored) {
        }

        player = null;
    }

    private void playBeep(int tone) {
        try {
            if (toneGenerator == null) {
                toneGenerator = new ToneGenerator(
                        AudioManager.STREAM_MUSIC,
                        75);
            }

            toneGenerator.startTone(tone, 250);

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "تعذر تشغيل النغمة",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == 100 && permissionWaiting) {
            permissionWaiting = false;

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANT