package com.samix.mosta;

import android.app.*;
import android.os.*;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {

    LinearLayout root, content;
    MediaPlayer music, player;
    MediaRecorder recorder;
    String audioPath;
    boolean musicOn = true;
    boolean radioOn = false;
    boolean recording = false;
    int musicVolume = 55;

    final int GOLD = Color.rgb(225,190,90);
    final int GREEN = Color.rgb(19,65,49);
    final int DARK = Color.rgb(5,20,16);
    final int WHITE = Color.WHITE;

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

        getWindow().setStatusBarColor(DARK);
        getWindow().setNavigationBarColor(DARK);

        startBackgroundMusic();
        showHome();
    }

    GradientDrawable panel(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(28);
        d.setStroke(2, GOLD);
        return d;
    }

    TextView makeText(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(WHITE);
        t.setGravity(Gravity.CENTER);
        t.setPadding(14, 14, 14, 14);
        return t;
    }

    void addSpace(LinearLayout layout, int height) {
        View v = new View(this);
        layout.addView(v, new LinearLayout.LayoutParams(1, height));
    }

    Button makeButton(String value, Runnable action) {
        Button b = new Button(this);
        b.setText(value);
        b.setTextColor(WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(panel(GREEN));
        b.setElevation(5);
        b.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(5, 7, 5, 7);
        b.setLayoutParams(p);
        return b;
    }

    TextView makeCard(String value, int size) {
        TextView t = makeText(value, size);
        t.setBackground(panel(Color.argb(220, 5, 25, 19)));
        LinearLayout.LayoutParams p =
            new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(4, 7, 4, 7);
        t.setLayoutParams(p);
        return t;
    }

    void setup(String pageTitle) {
        ScrollView scroll = new ScrollView(this);

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
        header.setBackground(panel(Color.argb(235, 5, 25, 19)));
        root.addView(header);

        TextView identity = makeText(
            "عزيز 29  •  سامي 27  •  دبشونة", 13);
        root.addView(identity);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content);

        addSpace(root, 12);
        TextView footer = makeText("SAMIX MOSTA  •  VERSION 4.0", 11);
        footer.setTextColor(GOLD);
        root.addView(footer);
    }

    // الموسيقى الداخلية

    void startBackgroundMusic() {
        try {
            if (music == null) {
                music = MediaPlayer.create(this, R.raw.background_music);
            }

            if (music != null) {
                music.setLooping(true);
                setMusicVolume();
                if (musicOn && !music.isPlaying()) music.start();
            }
        } catch (Exception e) {
            music = null;
        }
    }

    void setMusicVolume() {
        if (music != null) {
            float volume = musicVolume / 100f;
            music.setVolume(volume, volume);
        }
    }

    void pauseMusic() {
        try {
            if (music != null && music.isPlaying()) music.pause();
        } catch (Exception ignored) { }
    }

    void resumeMusic() {
        if (musicOn) startBackgroundMusic();
    }

    void showHome() {
        setup("المركز الرئيسي");

        content.addView(makeCard("مرحباً بك في SAMIX MOSTA", 22));
        content.addView(makeCard(
            "منصة تعليمية لمحاكاة شفرة مورس والاتصالات اللاسلكية.", 15));

        content.addView(makeButton(
            "📡  محاكي اللاسلكي", () -> showRadio()));

        content.addView(makeButton(
            "🔤  مترجم شفرة مورس", () -> showTranslator()));

        content.addView(makeButton(
            "📚  أكاديمية مورس", () -> showLessons()));

        content.addView(makeButton(
            "🧠  الاختبار التفاعلي", () -> showQuiz()));

        content.addView(makeButton(
            "🎵  إعدادات الصوت", () -> showSettings()));

        content.addView(makeButton(
            "ℹ️  حول التطبيق", () -> showAbout()));
    }

    // مترجم مورس

    String encode(String input) {
        StringBuilder result = new StringBuilder();
        String upper = input.toUpperCase(Locale.ROOT);

        for (char c : upper.toCharArray()) {
            if (c == ' ') {
                result.append("/ ");
                continue;
            }

            int index = alphabet.indexOf(c);
            if (index >= 0) {
                result.append(morse[index]).append(" ");
            }
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
        input.setSingleLine(false);
        input.setTextColor(WHITE);
        input.setHintTextColor(Color.LTGRAY);
        input.setHint("اكتب النص أو شفرة مورس هنا");
        input.setPadding(16, 16, 16, 16);
        input.setBackground(panel(DARK));
        content.addView(input);

        TextView output = makeCard("ستظهر النتيجة هنا", 18);
        content.addView(output);

        content.addView(makeButton(
            "تحويل النص إلى شفرة مورس",
            () -> output.setText(encode(input.getText().toString()))));

        content.addView(makeButton(
            "تحويل شفرة مورس إلى نص",
            () -> output.setText(decode(input.getText().toString()))));

        content.addView(makeButton("نسخ النتيجة", () -> {
            android.content.ClipboardManager clipboard =
                (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);

            clipboard.setPrimaryClip(
                ClipData.newPlainText("Morse", output.getText()));

            Toast.makeText(this, "تم نسخ النتيجة",
                Toast.LENGTH_SHORT).show();
        }));

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    // الأكاديمية

    void showLessons() {
        setup("أكاديمية مورس");

        String[] lessons = {
            "الدرس 01 — الأساسيات\nالنقطة قصيرة والشرطة أطول.",
            "الدرس 02 — الحروف\nA = .-     B = -...     C = -.-.",
            "الدرس 03 — الأرقام\n0 = -----     1 = .----",
            "الدرس 04 — تكوين الكلمات\nنفصل بين الحروف بمسافة وبين الكلمات بالرمز /."
        };

        for (String lesson : lessons) {
            content.addView(makeCard(lesson, 17));
        }

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    // الاختبار

    void showQuiz() {
        setup("الاختبار التفاعلي");

        content.addView(makeCard(
            "السؤال الأول\nما هو رمز الحرف A؟", 20));

        content.addView(makeButton(".-", () -> answer(true)));
        content.addView(makeButton("-...", () -> answer(false)));
        content.addView(makeButton("--.", () -> answer(false)));

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    void answer(boolean correct) {
        new AlertDialog.Builder(this)
            .setTitle(correct ? "إجابة صحيحة" : "حاول مرة أخرى")
            .setMessage(correct
                ? "أحسنت! رمز الحرف A هو .-"
                : "الإجابة الصحيحة هي .-")
            .setPositiveButton("موافق", null)
            .show();
    }

    // محاكي اللاسلكي

    void showRadio() {
        setup("محاكي اللاسلكي");

        TextView status = makeCard(
            radioOn ? "🟢 الجهاز يعمل" : "🔴 الجهاز متوقف", 20);

        content.addView(status);

        content.addView(makeButton("تشغيل / إيقاف الجهاز", () -> {
            radioOn = !radioOn;
            status.setText(radioOn
                ? "🟢 الجهاز يعمل"
                : "🔴 الجهاز متوقف");
        }));

        content.addView(makeButton(
            "🎙 بدء التسجيل", () -> startRecording()));

        content.addView(makeButton(
            "⏹ إنهاء التسجيل", () -> stopRecording()));

        content.addView(makeButton(
            "▶ إعادة الاستماع", () -> playRecording()));

        content.addView(makeButton(
            "نغمة بداية الاتصال", () -> beep(900)));

        content.addView(makeButton(
            "نغمة نهاية الاتصال", () -> beep(500)));

        content.addView(makeCard(
            "هذا محاكي تعليمي محلي. لا يتصل بأجهزة أو ترددات حقيقية.", 14));

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    void startRecording() {
        if (!radioOn) {
            Toast.makeText(this, "شغّل الجهاز أولاً",
                Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }

        try {
            pauseMusic();

            audioPath = getCacheDir().getAbsolutePath()
                + "/radio_record.m4a";

            if (recorder != null) {
                recorder.release();
                recorder = null;
            }

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOutputFile(audioPath);
            recorder.prepare();
            recorder.start();

            recording = true;

            Toast.makeText(this, "بدأ التسجيل",
                Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            recording = false;
            resumeMusic();
            Toast.makeText(this, "تعذر بدء التسجيل",
                Toast.LENGTH_SHORT).show();
        }
    }

    void stopRecording() {
        try {
            if (recorder != null && recording) {
                recorder.stop();
                recorder.release();
                recorder = null;
                recording = false;

                Toast.makeText(this, "انتهى التسجيل",
                    Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "لم يكتمل التسجيل",
                Toast.LENGTH_SHORT).show();
        }

        resumeMusic();
    }

    void playRecording() {
        if (audioPath == null) {
            Toast.makeText(this, "لا يوجد تسجيل بعد",
                Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            pauseMusic();

            if (player != null) {
                player.release();
                player = null;
            }

            player = new MediaPlayer();
            player.setDataSource(audioPath);
            player.setOnCompletionListener(mp -> resumeMusic());
            player.prepare();
            player.start();

        } catch (Exception e) {
            resumeMusic();
            Toast.makeText(this, "تعذر تشغيل التسجيل",
                Toast.LENGTH_SHORT).show();
        }
    }

    void beep(int frequency) {
        try {
            pauseMusic();

            final int sampleRate = 44100;
            final int duration = 180;
            final int count = sampleRate * duration / 1000;
            short[] sound = new short[count];

            for (int i = 0; i < count; i++) {
                sound[i] = (short)(
                    Math.sin(2 * Math.PI * frequency * i / sampleRate)
                    * 10000);
            }

            AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                count * 2,
                AudioTrack.MODE_STATIC);

            track.write(sound, 0, count);
            track.setNotificationMarkerPosition(count);
            track.setPlaybackPositionUpdateListener(
                new AudioTrack.OnPlaybackPositionUpdateListener() {
                    @Override
                    public void onMarkerReached(AudioTrack t) {
                        t.release();
                        resumeMusic();
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack t) { }
                });

            track.play();

        } catch (Exception e) {
            resumeMusic();
        }
    }

    // إعدادات التطبيق

    void showSettings() {
        setup("إعدادات الصوت");

        TextView state = makeCard(
            musicOn ? "الموسيقى: مفعّلة" : "الموسيقى: متوقفة", 18);
        content.addView(state);

        content.addView(makeButton("تشغيل / إيقاف الموسيقى", () -> {
            musicOn = !musicOn;

            if (musicOn) {
                startBackgroundMusic();
            } else {
                pauseMusic();
            }

            state.setText(musicOn
                ? "الموسيقى: مفعّلة"
                : "الموسيقى: متوقفة");
        }));

        content.addView(makeCard("مستوى صوت الموسيقى", 17));

        SeekBar volume = new SeekBar(this);
        volume.setMax(100);
        volume.setProgress(musicVolume);
        content.addView(volume);

        TextView percent = makeText(
            musicVolume + "%", 16);
        content.addView(percent);

        volume.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(
                    SeekBar seekBar, int value, boolean user) {

                    musicVolume = value;
                    percent.setText(value + "%");
                    setMusicVolume();
                }

                @Override
                public void onStartTrackingTouch(SeekBar s) { }

                @Override
                public void onStopTrackingTouch(SeekBar s) { }
            });

        content.addView(makeCard(
            "إصدار التطبيق: 4.0\nالخلفية: صورة السيارة المضافة", 15));

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    void showAbout() {
        setup("حول التطبيق");

        content.addView(makeCard("SAMIX MOSTA 4.0", 24));

        content.addView(makeCard(
            "تطبيق تعليمي لمحاكاة شفرة مورس والاتصالات اللاسلكية.\n\n"
            + "تصميم بطابع جزائري وألوان الأخضر والذهبي.",
            17));

        content.addView(makeButton("رجوع", () -> showHome()));
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (music != null && music.isPlaying()) {
            music.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (musicOn && !recording) {
            startBackgroundMusic();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (recorder != null) {
                if (recording) recorder.stop();
                recorder.release();
            }

            if (player != null) player.release();
            if (music != null) {
                music.stop();
                music.release();
            }
        } catch (Exception ignored) { }

        super.onDestroy();
    }
}