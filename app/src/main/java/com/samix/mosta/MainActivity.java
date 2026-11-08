
package com.samix.mosta;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private static final int MIC_PERMISSION = 1001;

    private static final int COLOR_DARK = Color.rgb(7, 19, 16);
    private static final int COLOR_GREEN = Color.rgb(24, 76, 56);
    private static final int COLOR_GOLD = Color.rgb(224, 190, 103);
    private static final int COLOR_WHITE = Color.WHITE;

    private LinearLayout mainLayout;

    private MediaPlayer musicPlayer;
    private MediaRecorder recorder;
    private MediaPlayer recordingPlayer;

    private ToneGenerator toneGenerator;

    private final Handler morseHandler =
            new Handler(Looper.getMainLooper());

    private boolean musicEnabled = true;
    private boolean radioOn = false;
    private boolean isRecording = false;
    private boolean morsePlaying = false;

    private int musicVolume = 35;
    private int morseRunId = 0;

    private File recordingFile;

    private final Map<Character, String> morse = new HashMap<>();

    private static class MorseTone {
        int duration;
        int gapAfter;

        MorseTone(int duration, int gapAfter) {
            this.duration = duration;
            this.gapAfter = gapAfter;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(COLOR_DARK);
        getWindow().setNavigationBarColor(COLOR_DARK);

        initializeMorse();
        initializeToneGenerator();

        showHome();
        startMusic();
    }

    private void initializeMorse() {

        String[] letters = {
                ".-", "-...", "-.-.", "-..", ".", "..-.",
                "--.", "....", "..", ".---", "-.-", ".-..",
                "--", "-.", "---", ".--.", "--.-", ".-.",
                "...", "-", "..-", "...-", ".--", "-..-",
                "-.--", "--.."
        };

        for (int i = 0; i < letters.length; i++) {
            morse.put((char) ('A' + i), letters[i]);
        }

        String[] numbers = {
                "-----", ".----", "..---", "...--", "....-",
                ".....", "-....", "--...", "---..", "----."
        };

        for (int i = 0; i < numbers.length; i++) {
            morse.put((char) ('0' + i), numbers[i]);
        }
    }

    private String encodeMorse(String source) {

        StringBuilder result = new StringBuilder();
        String value = source.toUpperCase().trim();

        for (int i = 0; i < value.length(); i++) {

            char character = value.charAt(i);

            if (character == ' ') {
                result.append("/ ");
            } else if (morse.containsKey(character)) {
                result.append(morse.get(character)).append(' ');
            } else {
                result.append("? ");
            }
        }

        return result.toString().trim();
    }

    private String decodeMorse(String source) {

        StringBuilder result = new StringBuilder();
        String value = source.trim();

        if (value.isEmpty()) {
            return "";
        }

        String[] words = value.split("\\s*/\\s*");

        for (int wordIndex = 0;
             wordIndex < words.length;
             wordIndex++) {

            String word = words[wordIndex].trim();

            if (!word.isEmpty()) {

                String[] symbols = word.split("\\s+");

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

                    if (!found) {
                        result.append('?');
                    }
                }
            }

            if (wordIndex < words.length - 1) {
                result.append(' ');
            }
        }

        return result.toString();
    }

    private void initializeToneGenerator() {

        try {
            toneGenerator =
                    new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
        } catch (Exception exception) {
            toneGenerator = null;
        }
    }

    private void startMusic() {

        if (!musicEnabled) {
            return;
        }

        try {

            if (musicPlayer == null) {

                musicPlayer =
                        MediaPlayer.create(this, R.raw.background_music);

                if (musicPlayer != null) {
                    musicPlayer.setLooping(true);
                    applyMusicVolume();
                }
            }

            if (musicPlayer != null && !musicPlayer.isPlaying()) {
                musicPlayer.start();
            }

        } catch (Exception exception) {
            releaseMusic();
        }
    }

    private void applyMusicVolume() {

        if (musicPlayer != null) {

            try {
                float volume = musicVolume / 100.0f;

                if (morsePlaying) {
                    volume *= 0.18f;
                }

                musicPlayer.setVolume(volume, volume);

            } catch (Exception ignored) {
            }
        }
    }

    private void pauseMusic() {

        try {

            if (musicPlayer != null && musicPlayer.isPlaying()) {
                musicPlayer.pause();
            }

        } catch (Exception ignored) {
        }
    }

    private void releaseMusic() {

        if (musicPlayer != null) {

            try {
                musicPlayer.release();
            } catch (Exception ignored) {
            }

            musicPlayer = null;
        }
    }

    private void playStartBeep() {

        if (toneGenerator != null) {
            toneGenerator.startTone(
                    ToneGenerator.TONE_PROP_BEEP, 180
            );
        }
    }

    private void playEndBeep() {

        if (toneGenerator != null) {
            toneGenerator.startTone(
                    ToneGenerator.TONE_PROP_ACK, 230
            );
        }
    }

    private ArrayList<MorseTone> createMorseSequence(String code) {

        ArrayList<MorseTone> sequence = new ArrayList<>();

        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        String[] tokens = code.trim().split("\\s+");

        for (int i = 0; i < tokens.length; i++) {

            String token = tokens[i];

            if (token.equals("/")) {
                continue;
            }

            if (!token.matches("[.-]+")) {
                return null;
            }

            for (int j = 0; j < token.length(); j++) {

                char symbol = token.charAt(j);

                int duration = symbol == '.' ? 120 : 360;
                int gap = 120;

                if (j == token.length() - 1) {

                    if (i + 1 < tokens.length) {

                        if (tokens[i + 1].equals("/")) {
                            gap = 840;
                        } else {
                            gap = 360;
                        }

                    } else {
                        gap = 0;
                    }
                }

                sequence.add(new MorseTone(duration, gap));
            }
        }

        return sequence;
    }

    private void playMorseResult(String code) {

        if (toneGenerator == null) {
            showMessage("تعذر تشغيل مولد الصوت");
            return;
        }

        if (code == null || code.trim().isEmpty()
                || code.equals("ستظهر النتيجة هنا")) {

            showMessage("حوّل النص إلى مورس أولاً");
            return;
        }

        ArrayList<MorseTone> sequence =
                createMorseSequence(code);

        if (sequence == null || sequence.isEmpty()) {
            showMessage("النتيجة ليست شفرة مورس صالحة");
            return;
        }

        stopMorsePlayback();

        morsePlaying = true;
        applyMusicVolume();

        morseRunId++;

        int currentRun = morseRunId;

        showMessage("بدأ الاستماع إلى شفرة مورس");

        playMorseStep(sequence, 0, currentRun);
    }

    private void playMorseStep(
            ArrayList<MorseTone> sequence,
            int index,
            int currentRun
    ) {

        if (currentRun != morseRunId || !morsePlaying) {
            return;
        }

        if (index >= sequence.size()) {

            morsePlaying = false;

            if (toneGenerator != null) {
                toneGenerator.stopTone();
            }

            applyMusicVolume();

            showMessage("انتهى تشغيل شفرة مورس");
            return;
        }

        MorseTone event = sequence.get(index);

        if (toneGenerator != null) {
            toneGenerator.startTone(
                    ToneGenerator.TONE_PROP_BEEP,
                    event.duration
            );
        }

        int nextDelay = event.duration + event.gapAfter;

        morseHandler.postDelayed(
                () -> playMorseStep(
                        sequence,
                        index + 1,
                        currentRun
                ),
                nextDelay
        );
    }

    private void stopMorsePlayback() {

        morseRunId++;

        morseHandler.removeCallbacksAndMessages(null);

        if (morsePlaying) {

            morsePlaying = false;

            if (toneGenerator != null) {
                toneGenerator.stopTone();
            }
        }

        applyMusicVolume();
    }

    private GradientDrawable makeBackground(
            int color,
            int borderColor
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
            String value,
            int size,
            int color
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
        button.setTextColor(COLOR_WHITE);
        button.setTextSize(16);
        button.setAllCaps(false);

        button.setBackground(
                makeBackground(COLOR_GREEN, COLOR_GOLD)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(7, 7, 7, 7);

        button.setLayoutParams(params);

        return button;
    }

    private void prepareScreen(String title) {

        stopMorsePlayback();

        ScrollView scrollView = new ScrollView(this);

        mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(16, 20, 16, 28);

        try {
            mainLayout.setBackgroundResource(
                    R.drawable.background_samix
            );
        } catch (Exception exception) {
            mainLayout.setBackgroundColor(COLOR_DARK);
        }

        scrollView.addView(mainLayout);

        setContentView(scrollView);

        mainLayout.addView(
                makeText(title, 25, COLOR_GOLD)
        );

        mainLayout.addView(
                makeText("SAMIX MOSTA  •  VERSION 4.0", 12, COLOR_WHITE)
        );

        View divider = new View(this);
        divider.setBackgroundColor(COLOR_GOLD);

        LinearLayout.LayoutParams dividerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        2
                );

        dividerParams.setMargins(15, 12, 15, 18);

        mainLayout.addView(divider, dividerParams);
    }

    private void addText(String value, int size) {
        mainLayout.addView(
                makeText(value, size, COLOR_WHITE)
        );
    }

    private void addButton(String value, Runnable action) {

        Button button = makeButton(value);

        button.setOnClickListener(view -> {

            if (action != null) {
                action.run();
            }
        });

        mainLayout.addView(button);
    }

    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showHome() {

        prepareScreen("SAMIX MOSTA");

        addText("مرحبا بك في تطبيق ساميكس", 19);
        addText("عزيز 29  •  سامي 27  •  دبشونة", 15);

        addButton("📻 محاكي اللاسلكي", this::showRadio);
        addButton("⌁ مترجم مورس", this::showMorse);
        addButton("🎓 أكاديمية مورس", this::showAcademy);
        addButton("🧠 اختبار المعرفة", this::showQuiz);
        addButton("⚙️ إعدادات التطبيق", this::showSettings);
        addButton("ℹ️ حول التطبيق", this::showAbout);
    }

    private void showMorse() {

        prepareScreen("مترجم مورس");

        addText("يدعم الحروف الإنجليزية والأرقام", 15);

        EditText input = new EditText(this);

        input.setTextColor(COLOR_WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setHint("اكتب النص أو رموز مورس هنا");
        input.setTextSize(17);
        input.setPadding(14, 14, 14, 14);

        input.setBackground(
                makeBackground(COLOR_DARK, COLOR_GOLD)
        );

        mainLayout.addView(input);

        TextView output = makeText(
                "ستظهر النتيجة هنا",
                18,
                COLOR_GOLD
        );

        mainLayout.addView(output);

        addButton("تحويل النص إلى مورس", () -> {

            String value = input.getText().toString();

            output.setText(encodeMorse(value));
        });

        addButton("تحويل مورس إلى نص", () -> {

            String value = input.getText().toString();

            output.setText(decodeMorse(value));
        });

        addButton("🔊 استماع إلى نتيجة مورس", () -> {

            String result = output.getText().toString();

            playMorseResult(result);
        });

        addButton("إيقاف استماع مورس", () -> {

            stopMorsePlayback();

            showMessage("تم إيقاف استماع مورس");
        });

        addButton("مسح", () -> {

            stopMorsePlayback();

            input.setText("");
            output.setText("ستظهر النتيجة هنا");
        });

        addButton("رجوع", this::showHome);
    }

    private void showAcademy() {

        prepareScreen("أكاديمية مورس");

        addText("الدرس الأول: ما هو مورس؟", 20);

        addText(
                "شفرة تمثل الحروف والأرقام باستخدام النقاط والشرطات.",
                16
        );

        addText("النقطة: .", 20);
        addText("الشرطة: -", 20);

        addText("الدرس الثاني: الحروف", 20);

        addText("A = .-     B = -...     C = -.-.", 17);
        addText("S = ...     O = ---     T = -", 17);

        addText("الدرس الثالث: الأرقام", 20);

        addText("1 = .----     2 = ..---     3 = ...--", 17);
        addText("0 = -----     5 = .....     9 = ----.", 17);

        addText(
                "المسافة تفصل بين الحروف، والرمز / يفصل الكلمات.",
                15
        );

        addButton("تجربة المترجم", this::showMorse);
        addButton("رجوع", this::showHome);
    }

    private void showQuiz() {

        prepareScreen("اختبار المعرفة");

        addText("ما هو رمز الحرف S في شفرة مورس؟", 20);

        addButton("A) .-", () -> showQuizResult(false));
        addButton("B) ...", () -> showQuizResult(true));
        addButton("C) ---", () -> showQuizResult(false));

        addButton("رجوع", this::showHome);
    }

    private void showQuizResult(boolean correct) {

        prepareScreen("نتيجة الاختبار");

        if (correct) {
            addText("إجابة صحيحة! أحسنت.", 22);
        } else {
            addText(
                    "إجابة غير صحيحة. الإجابة الصحيحة هي ...",
                    18
            );
        }

        addButton("إعادة الاختبار", this::showQuiz);
        addButton("الرئيسية", this::showHome);
    }

    private void showRadio() {

        prepareScreen("محاكي اللاسلكي");

        addText(
                "محاكي صوتي تعليمي يعمل داخل الهاتف فقط.",
                16
        );

        addText(
                "لا يتصل بترددات أو أجهزة اتصال حقيقية.",
                14
        );

        addText(
                radioOn
                        ? "حالة الجهاز: يعمل"
                        : "حالة الجهاز: متوقف",
                20
        );

        addText(
                isRecording
                        ? "حالة التسجيل: جارٍ"
                        : "حالة التسجيل: متوقف",
                16
        );

        addButton(
                radioOn ? "إيقاف الجهاز" : "تشغيل الجهاز",
                () -> {

                    if (radioOn && isRecording) {

                        showMessage(
                                "أوقف التسجيل قبل إيقاف الجهاز"
                        );

                        return;
                    }

                    radioOn = !radioOn;

                    if (radioOn) {
                        playStartBeep();
                    }

                    showRadio();
                }
        );

        addButton("بدء التسجيل الصوتي", this::startRecording);
        addButton("إيقاف التسجيل", this::stopRecording);
        addButton("إعادة سماع التسجيل", this::playRecording);

        addButton("صوت بداية الإرسال", () -> {

            if (radioOn) {
                playStartBeep();
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

        addButton("مؤثر تشويش تجريبي", () -> {

            if (radioOn && toneGenerator != null) {

                toneGenerator.startTone(
                        ToneGenerator.TONE_SUP_ERROR,
                        350
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

        if (isRecording) {
            showMessage("التسجيل يعمل بالفعل");
            return;
        }

        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifes