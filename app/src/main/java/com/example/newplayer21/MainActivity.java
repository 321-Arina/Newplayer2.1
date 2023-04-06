package com.example.newplayer21;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements Runnable{

    // создание полей
    private MediaPlayer mediaPlayer = new MediaPlayer(); // создание поля медиа-плеера
    private SeekBar seekBar; // создание поля SeekBar
    private boolean wasPlaying = false; // поле проигрывания аудио-файла
    private FloatingActionButton fabPlayPause; // поле кнопки проигрывания и постановки на паузу аудиофайла
    private FloatingActionButton fabBack; // поле кнопки перемотки назад аудиофайла
    private FloatingActionButton fabForward;// поле кнопки перемотки вперёд аудиофайла
    private FloatingActionButton fabRepeat; // поле кнопки повтора
    private FloatingActionButton fabNext; // поле кнопки воспроизведения следующего трека
    private TextView seekBarHint; // поле информации у SeekBar
    private TextView metaDataAudio; // создание поля вывода информации о треке
    private String metaData; // поле для записи метаданных
    private boolean isRepeat = false; // поле выключенного повтора трека
    private SeekBar volBar; // создание поля volBar
    private TextView textVolHint; // поле информации у volBar
    public String pathName; // поле пути к файлу
    public int trackNum = 0; // поле номера трека



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присваивание полям id ресурсов
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabBack = findViewById(R.id.fabBack);
        fabForward= findViewById(R.id.fabForward);
        fabRepeat = findViewById(R.id.fabRepeat);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        metaDataAudio = findViewById(R.id.metaDataAudio);
        fabNext = findViewById(R.id.fabNext);
        volBar = findViewById(R.id.volBar);
        textVolHint = findViewById(R.id.textVolHint);

        // выбор первого трека в списке
        try {
            String list[] = getAssets().list("music");
                pathName = "music/" + list[0];
            Arrays.fill(list, null);
        } catch (IOException e) {
            Log.v("Ошибка:", "нет такого пути");
        }

        // регулятор громкости
        volBar.setProgress(100);
        volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textVolHint.setVisibility(View.VISIBLE);
                textVolHint.setText("" + volBar.getProgress() + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float vol = volBar.getProgress();
                mediaPlayer.setVolume(vol/100, vol/100);

            }
        });

        // создание слушателя изменения SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // метод при перетаскивании ползунка по шкале,
            // где progress позволяет получить новое значение ползунка (позже progress назначается длина трека в миллисекундах)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE); // установление видимости seekBarHint
                //seekBarHint.setVisibility(View.INVISIBLE); // установление не видимости seekBarHint

                // Math.ceil() - округление до целого в большую сторону
                int second = (int) Math.ceil(progress/1000f); // перевод времени из миллисекунд в секунды
                int minute = second / 60; // определение количества минут
                int hour = minute / 60; // определение количества часов
                second = second % 60; // ограничение количества секунд 60 секундами
                minute = minute % 60; // ограничение количества минут 60 минутами

                // вывод на экран времени отсчёта трека

                if (hour > 0) {
                    seekBarHint.setText("" + hour + ":" + minute + ":" + String.format("%02d", second));
                }
                else {
                    seekBarHint.setText("" + minute + ":" + String.format("%02d", second));
                }

                // передвижение времени отсчёта трека
                double percentTrack = progress / (double) seekBar.getMax(); // получение процента проигранного трека (проигранное время делится на длину трека)
                // seekBar.getX() - начало seekBar по оси Х
                // seekBar.getWidth() - ширина контейнера seekBar
                // 0.92 - поправочный коэффициент (так как seekBar занимает не всю ширину своего контейнера)
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));
                // изменение прогресса при паузе
                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится
                mediaPlayer.seekTo(seekBar.getProgress());
               }
            }
            // метод при начале перетаскивания ползунка по шкале
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.INVISIBLE); // установление видимости seekBarHint
            }
            // метод при завершении перетаскивания ползунка по шкале
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                    mediaPlayer.seekTo(seekBar.getProgress()); // обновление позиции трека при изменении seekBar
                }
            }
        });

        // обработка нажатия кнопки
        fabPlayPause.setOnClickListener(listener); // создание слушателя нажатия кнопки fabPlayPause
        fabBack.setOnClickListener(listener); // создание слушателя нажатия кнопки fabBack (перемотки назад)
        fabForward.setOnClickListener(listener); // создание слушателя нажатия кнопки fabForward (перемотки вперёд)
        fabRepeat.setOnClickListener(listener); // создание слушателя нажатия кнопки fabRepeat (повтора)
        fabNext.setOnClickListener(listener); // создание слушателя нажатия кнопки fabNext (трека)

    }

    // метод выбора следующего трека из папки music
    String playFiles(AssetManager mgr, String path) {

        try {
            String list[] = mgr.list(path);
            int i = list.length;
            if (list != null && trackNum < i-1)
            {
                trackNum++;
                pathName = path + "/" + list[trackNum];

            } else
            {
                trackNum = 0;
                pathName = path + "/" + list[trackNum];

            }
        } catch (IOException e) {
            Log.v("Ошибка: ", "нет такого пути " + path);
        }
        return pathName;
    }

    // создание слушателя обработки нажания кнопки
    private View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            AssetManager myAssetManager = getApplicationContext().getAssets();
            // обработка нажатия нескольких кнопок
            switch (view.getId()) {
                case R.id.fabPlayPause:
                    playSong(); // воспроизведение музыки
                    break;
                case R.id.fabBack:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000); // перематывание трека назад на 5 секунд (5000 миллисекунд)
                    break;
                case R.id.fabForward:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000); // перематывание трека вперёд на 5 секунд (5000 миллисекунд)
                    break;
                case R.id.fabNext:
                    if (mediaPlayer != null) {
                        final AssetManager mgr = getAssets();
                        playFiles(mgr, "music"); // содержимое подпапки /assets/music
                        seekBar.setProgress(0);
                        clearMediaPlayer();
                    }

                    playSong();
                    break;

                case R.id.fabRepeat:
                    if (!isRepeat && mediaPlayer != null) {
                        mediaPlayer.setLooping(true); // включение повтора аудио
                        // назначение кнопке картинки repeat
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat));
                        isRepeat = true; // изменение значения переключения
                    } else if (isRepeat && mediaPlayer != null){
                        mediaPlayer.setLooping(false); // выключение повтора аудио
                        // назначение кнопке картинки repeat_off
                        fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.repeat_off));
                        isRepeat = false; // изменение значения переключения
                    }
                    break;
            }
        }
    };


    // метод запуска аудио-файла
    public void playSong() {
        try { // обработка исключения на случай отстутствия файла
            if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится

                wasPlaying = true;
                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                mediaPlayer.pause();
                new Thread(this).interrupt();

            }

            if (mediaPlayer != null && !wasPlaying) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится

                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                mediaPlayer.start();
                new Thread(this).start();

            }


            if (!wasPlaying) {
                if (mediaPlayer == null) { // если mediaPlayer пустой
                    mediaPlayer = new MediaPlayer(); // то выделяется для него память
                }


                    // альтернативный способ считывания файла с помощью файлового дескриптора
                    AssetFileDescriptor descriptor = getAssets().openFd(pathName);
                    // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                    // запись метаданных в окно вывода информации metaDataAudio
                    MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever(); // создание объекта специального класса для считывания метаданных
                    mediaMetadata.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength()); // считывание метаданных по имеющемуся дескриптору

                    metaData = mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); // добавление артиста
                    metaData += "\n" + mediaMetadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); // добавление названия трека

                    mediaMetadata.release(); // закрытие объекта загрузки метаданных

                    metaDataAudio.setText(metaData); // вывод метаданных в окно metaDataAudio

                    descriptor.close(); // закрытие дескриптора


                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));



                mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                //mediaPlayer.setVolume(0.7f, 0.7f); // задание уровня громкости левого и правого динамиков
                mediaPlayer.setLooping(false); // назначение отстутствия повторов
                seekBar.setMax(mediaPlayer.getDuration()); // ограниечение seekBar длинной трека

                mediaPlayer.start(); // старт mediaPlayer
                new Thread(this).start(); // запуск дополнительного потока
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }
    }

    // при уничтожении активити вызов метода остановки и очиски MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    // метод остановки и очиски MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при условии не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека

            } catch (InterruptedException e) { // вызывается в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition); // обновление seekBar текущей позицией трека

        }
    }
}