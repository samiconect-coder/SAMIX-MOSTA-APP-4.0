
package com.samix.mosta;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private final int GREEN = Color.rgb(25, 75, 57);
    private final int GOLD = Color.rgb(225, 190, 90);
    private final int DARK = Color.rgb(5, 16, 14);
    private final int WHITE = Color.WHITE;

    private LinearLayout mainLayout;
    private MediaPlayer musicPlayer;
    private MediaRecorder recorder;
    private ToneGenerator toneGenerator;

    private boolean musicEnabled = true;
    private boolean radioOn = false;
    private boolean isRecording = false;

    private File recordingFile;
    private String morseResult = "";

    private static final int MIC_PERMISSION = 100;

    private final Map<Character, String> morse =
            new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);

        initializeMorse();
        initializeAudio();
        showHome();
    }

    private void initializeMorse() {
        String[] letters = {
                ".-", "-...", "-.-.", "-..", ".",
                "..-.", "--.", "....", "..", ".---",
                "-.-", ".-..", "--", "-.", "---",
                ".--.", "--.-", ".-.", "...", "-",
                "..-", "...-", ".--", "-..-", "-.--",
                "--.."
        };

        for (int i = 0; i < 26; i++) {
            morse.put((char) ('A' + i), letters[i]);
        }

        String[] numbers = {
                "-----", ".----", "..---", "...--",
                "....-", ".....", "-....", "--...",
                "---..", "----."
        };

        for (int i = 0; i < 10; i++) {
            morse.put((char) ('0' + i), numbers[i]);
        }
    }

    private void initializeAudio() {
        toneGenerator = new ToneGenerator(
                AudioManager.STREAM_MUSIC, 70
        );

        try {
            musicPlayer = MediaPlayer.create(
                    this, R.raw.background_music
            );

            if (musicPlayer != null) {
                musicPlayer.setLooping(true);
                musicPlayer.setVolume(0.35f, 0.35f);

                if (musicEnabled) {
                    musicPlayer.start();
                }
            }
        } catch (Exception e) {
            musicPlayer = null;
        }
    }

    private GradientDrawable roundedBackground(
            int color, int borderColor
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(24);

        if (borderColor != Color.TRANSPARENT) {
            drawable.setStroke(2, borderColor);
        }

        return drawable;
    }

    private TextView makeText(
            String value, int size, int color
    ) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.CENTER);
        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.setPadding(12, 12, 12, 12);
        return text;
    }

    private Button makeButton(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(WHITE);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setBackground(
                roundedBackground(GREEN, GOLD)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(8, 7, 8, 7);
        button.setLayoutParams(params);

        return button;
    }

    private void prepareScreen(String title) {
        ScrollView scroll = new ScrollView(this);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(16, 20, 16, 25);

        try {
            mainLayout.setBackgroundResource(
                    R.drawable.background_samix
            );
        } catch (Exception e) {
            mainLayout.setBackgroundColor(DARK);
        }

        scroll.addView(mainLayout);

        setContentView(scroll);

        TextView heading = makeText(
                title, 25, GOLD
        );

        mainLayout.addView(heading);

        TextView subtitle = makeText(
                "SAMIX MOSTA • VERSION 4.0",
                12, WHITE
        );

        mainLayout.addView(subtitle);

        View line = new View(this);
        line.setBackgroundColor(GOLD);

        LinearLayout.LayoutParams lineParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2
                );

        lineParams.setMargins(15, 12, 15, 18);
        mainLayout.addView(line, lineParams);
    }

    private void addText(String value, int size) {
        mainLayout.addView(
                makeText(value, size, WHITE)
        );
    }

    private void addButton(
            String value, Runnable action
    ) {
        Button button = makeButton(value);

        button.setOnClickListener(v -> {
            if (action != null) {
                action.run();
            }
        });

        mainLayout.addView(button);
    }

    private void showHome() {
        prepareScreen("SAMIX MOSTA");

        addText("مرحبا بك في تطبيق ساميكس", 19);
        addText("عزيز 29 • سامي 27 • دبشونة", 15);

        addButton("📻 محاكي اللاسلكي", this::showRadio);
        addButton("⌁ مترجم مورس", this::showMorse);
        addButton("🎓 أكاديمية مورس", this::showAcademy);
        addButton("🧠 اختبار المعرفة", this::showQuiz);
        addButton("⚙️ إعدادات التطبيق", this::showSettings);
        addButton("ℹ️ حول التطبيق", this::showAbout);
    }

    // ---------------- MORSE ----------------

    private void showMorse() {
        prepareScreen("مترجم مورس");

        addText("اكتب النص باللغة الإنجليزية أو الأرقام", 15);

        EditText input = new EditText(this);
        input.setTextColor(WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setHint("اكتب هنا...");
        input.setTextSize(18);
        input.setPadding(15, 15, 15, 15);
        input.setBackground(
                roundedBackground(DARK, GOLD)
        );

        mainLayout.addView(input);

        TextView output = makeText(
                "النتيجة ستظهر هنا",
                18, GOLD
        );

        mainLayout.addView(output);

        addButton("تحويل إلى مورس", () -> {
            String text = input.getText().toString()
                    .toUpperCase().trim();

            output.setText(encodeMorse(text));
        });

        addButton("تحويل من مورس إلى نص", () -> {
            String text = input.getText().toString().trim();
            output.setText(decodeMorse(text));
        });

        addButton("مسح النص", () -> {
            input.setText("");
            output.setText("النتيجة ستظهر هنا");
        });

        addButton("رجوع", this::showHome);
    }

    private String encodeMorse(String text) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                result.append(" / ");
            } else if (morse.containsKey(c)) {
                result.append(morse.get(c)).append(" ");
            } else {
                result.append("? ");
            }
        }

        return result.toString().trim();
    }

    private String decodeMorse(String text) {
        StringBuilder result = new StringBuilder();

        String[] words = text.trim().split("\\s*/\\s*");

        for (String word : words) {
            String[] symbols = word.trim().split("\\s+");

            for (String symbol : symbols) {
                boolean found = false;

                for (Map.Entry<Character, String> entry
                        : morse.entrySet()) {

                    if (entry.getValue().equals(symbol)) {
                        result.append(entry.getKey());
                        found = true;
                        break;
                    }
                }

                if (!found && !symbol.isEmpty()) {
                    result.append("?");
                }
            }

            result.append(" ");
        }

        return result.toString().trim();
    }

    // ---------------- ACADEMY ----------------

    private void showAcademy() {
        prepareScreen("أكاديمية مورس");

        addText("الدرس الأول: ما هو مورس؟", 20);
        addText(
                "نظام اتصال يستخدم النقاط والشرطات "
                        + "لتمثيل الحروف والأرقام.",
                16
        );

        addText("النقطة: .", 20);
        addText("الشرطة: -", 20);

        addText("الدرس الثاني: أمثلة", 20);
        addText("A = .-", 18);
        addText("B = -...", 18);
        addText("C = -.-.", 18);
        addText("S = ...", 18);
        addText("O = ---", 18);

        addText("الدرس الثالث: الأرقام", 20);
        addText("1 = .----", 18);
        addText("2 = ..---", 18);
        addText("3 = ...--", 18);
        addText("0 = -----", 18);

        addText(
                "ملاحظة: تفصل المسافة بين الحروف، "
                        + "وتستخدم / للفصل بين الكلمات.",
                15
        );

        addButton("فتح مترجم مورس", this::showMorse);
        addButton("رجوع", this::showHome);
    }

    // ---------------- QUIZ ----------------

    private void showQuiz() {
        prepareScreen("اختبار مورس");

        addText("ما هو الرمز الصحيح للحرف S؟", 20);

        addButton("A) .-", () ->
                showQuizResult(false)
        );

        addButton("B) ...", () ->
                showQuizResult(true)
        );

        addButton("C) ---", () ->
                showQuizResult(false)
        );

        addButton("رجوع", this::showHome);
    }

    private void showQuizResult(boolean correct) {
        prepareScreen("نتيجة الاختبار");

        if (correct) {
            addText("إجابة صحيحة! أحسنت.", 22);
        } else {
            addText("إجابة غير صحيحة. حاول مجددا.", 20);
        }

        addButton("إعادة الاختبار", this::showQuiz);
        addButton("الرئيسية", this::showHome);
    }

    // ---------------- RADIO SIMULATOR ----------------

    private void showRadio() {
        prepareScreen("محاكي اللاسلكي");

        addText("محاكي تعليمي محلي", 17);
        addText(
                "هذا المحاكي لا يتصل بأجهزة أو ترددات حقيقية.",
                14
        );

        TextView status = makeText(
                radioOn ? "الجهاز: يعمل" : "الجهاز: متوقف",
                20, GOLD
        );

        mainLayout.addView(status);

        addButton(
                radioOn ? "إيقاف الجهاز" : "تشغيل الجهاز",
                () -> {
                    radioOn = !radioOn;

                    if (radioOn) {
                        playBeep();
                    }

                    showRadio();
                }
        );

        addButton("تسجيل صوتي", this::startRecording);
        addButton("إيقاف التسجيل", this::stopRecording);
        addButton("إعادة سماع التسجيل", this::playRecording);

        addButton("صوت بداية الإرسال", () -> {
            if (radioOn) {
                playBeep();
            } else {
                showMessage("شغّل الجهاز أولا");
            }
        });

        addButton("صوت نهاية الإرسال", () -> {
            if (radioOn) {
                playEndBeep();
            } else {
                showMessage("شغّل الجهاز أولا");
            }
        });

        addButton("مؤثر تشويش صوتي", () -> {
            if (radioOn && toneGenerator != null) {
                toneGenerator.startTone(
                        ToneGenerator.TONE_SUP_ERROR, 400
                );
            } else {
                showMessage("شغّل الجهاز أولا");
            }
        });

        addButton("رجوع", this::showHome);
    }

    private void startRecording() {
        if (!radioOn) {
            showMessage("شغّل الجهاز أولا");
            return;
        }

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    MIC_PERMISSION
            );

            return;
        }

        beginRecording();
    }

    private void beginRecording() {
        try {
            if (isRecording) {
                showMessage("التسجيل يعمل بالفعل");
                return;
            }

            recordingFile = new File(
                    getCacheDir(), "samix_recording.m4a"
            );

            recorder = new MediaRecorder();

            recorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC
            );

            recorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
            );

            recorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
            );

            recorder.setOutputFile(
                    recordingFile.getAbsolutePath()
            );

            recorder.prepare();
            recorder.start();

            isRecording = true;
            playBeep();

            showMessage("بدأ التسجيل الصوتي");

        } catch (Exception e) {
            isRecording = false;
            releaseRecorder();
            showMessage("تعذر بدء التسجيل");
        }
    }

    private void stopRecording() {
        if (!isRecording || recorder == null) {
            showMessage("لا يوجد تسجيل نشط");
            return;
        }

        try {
            recorder.stop();
            showMessage("تم حفظ التسجيل محليا");
        } catch (Exception e) {
            showMessage("تعذر حفظ التسجيل");
        }

        isRecording = false;
        releaseRecorder();
        playEndBeep();
    }

    private void playRecording() {
        if (isRecording) {
            showMessage("أوقف التسجيل أولا");
            return;
        }

        if (recordingFile == null ||
                !recordingFile.exists()) {
            showMessage("لا يوجد تسجيل محفوظ");
            return;
        }

        try {
            MediaPlayer player = new MediaPlayer();

            player.setDataSource(
                    recordingFile.getAbsolutePath()
            );

            player.setOnCompletionListener(p -> p.release());

            player.prepare();
            player.start();

        } catch (Exception e) {
            showMessage("تعذر تشغيل التسجيل");
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            try {
                recorder.reset();
                recorder.release();
            } catch (Exception ignored) {
            }

            recorder = null;
        }
    }

    private void playBeep() {
        if (toneGenerator != null) {
            toneGenerator.startTone(
                    ToneGenerator.TONE_PROP_BEEP, 180
            );
        }
    }

    private void playEndBeep() {
        if (toneGenerator != null) {
            toneGenerator.startTone(
                    ToneGenerator.TONE_PROP_ACK, 250
            );
        }
    }

    // ---------------- SETTINGS ----------------

    private void showSettings() {
        prepareScreen("إعدادات التطبيق");

        addText("الموسيقى الخلفية", 20);

        addButton(
                musicEnabled
                        ? "إيقاف الموسيقى"
                        : "تشغيل الموسيقى",
                () -> {
                    musicEnabled = !musicEnabled;

                    if (musicPlayer != null) {
                        if (musicEnabled) {
                            musicPlayer.start();
                        } else {
                            musicPlayer.pause();
                        }
                    }

                    showSettings();
                }
        );

        addText("مستوى صوت الموسيقى", 16);

        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(35);

        mainLayout.addView(volume);

        volume.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar,
                            int progress,
                            boolean fromUser
                    ) {
                        if (musicPlayer != null) {
                            float level = progress / 100f;

                            musicPlayer.setVolume(
                                    level, level
                            );
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }

                    @Override
                    public void onStopTrackingTouch(
                            SeekBar seekBar
                    ) {
                    }
                }
        );

        addButton("رجوع", this::showHome);
    }

    // ---------------- ABOUT ----------------

    private void showAbout() {
        prepareScreen("حول التطبيق");

        addText("SAMIX MOSTA 4.0", 23);
        addText("محاكي مورس والاتصالات اللاسلكية", 17);
        addText("تطبيق تعليمي ترفيهي محلي.", 16);
        addText("عزيز 29 • سامي 27 • دبشونة", 16);

        addButton("رجوع", this::showHome);
    }

    private void showMessage(String message) {
        Toast.makeText(
                this, message, Toast.LENGTH_SHORT
        ).show();
    }

    // ---------------- PERMISSIONS ----------------

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults
        );

        if (requestCode == MIC_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) {

                beginRecording();

            } else {
                showMessage(
                        "يجب السماح باستخدام الميكروفون للتسجيل"
                );
            }
        }
    }

    // ---------------- LIFECYCLE ----------------

    @Override
    protected void onPause() {
        super.onPause();

        if (musicPlayer != null &&
                musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (musicEnabled && musicPlayer != null) {
            musicPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isRecording) {
            try {
                recorder.stop();
            } catch (Exception ignored) {
            }

            isRecording = false;
        }

        releaseRecorder();

        if (musicPlayer != null) {
      