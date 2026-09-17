package com.vo.adertechaudioapp_v1.localAudio;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AudioScanner {
    private static final String TAG = AudioScanner.class.getSimpleName();
    public static Map<String, List<AudioModel>> scanAudio(Context context) {
        ///  Карта, где ключ — имя папки, а значение — список песен в ней
        Map<String, List<AudioModel>> musicFolders = new HashMap<>();

        /// Ссылка на таблицу с аудиофайлами во внутреннем/внешнем общем хранилище
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        ///  Какие данные мы хотим получить
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Audio.Media.ALBUM_ID
        };

        /// Фильтр: выбираем только музыку (игнорируем системные звуки, подкасты и аудиокниги, если нужно)
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        /// Базовый URI для обложек альбомов в Android
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

        try (Cursor cursor = context.getContentResolver().query(uri, projection, selection, null, null)) {
            if (cursor != null) {

                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int bucketIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME);
                int albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleIndex);
                    String path = cursor.getString(dataIndex);
                    String rawDuration = cursor.getString(durationIndex);
                    String folderName = cursor.getString(bucketIndex);
                    long albumId = cursor.getLong(albumIdIndex);            // Получаем ID альбома

                    /// Форматируем длительность с помощью созданного ранее метода
                    String duration = formatDuration(rawDuration);

                    // Генерируем уникальный Uri обложки для этого альбома
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    String albumArtUriStr = albumArtUri.toString();

                    if (folderName == null) {
                        folderName = "Unknown Folder";
                    }

                    AudioModel audioModel = new AudioModel(title, path, duration, folderName, albumArtUriStr);

                    // Если папки еще нет в Map, создаем для нее новый список
                    if (!musicFolders.containsKey(folderName)) {
                        musicFolders.put(folderName, new ArrayList<>());
                    }

                    // Добавляем трек в список соответствующей папки
                    Objects.requireNonNull(musicFolders.get(folderName)).add(audioModel);
                }
                cursor.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve the list of audio files.", e);
        }

        return musicFolders;
    }

    public static void triggerMediaScanner(Context context) {
        // Указываем путь к общей папке с музыкой
        String musicDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

        // Запускаем сканирование этой папки
        MediaScannerConnection.scanFile(context,
                new String[]{musicDirPath},
                null,
                (path, uri) -> Log.d("MediaScanner", "Сканирование завершено для пути: " + path));
    }

    public static String formatDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return "00:00";
        }

        try {
            long millis = Long.parseLong(durationStr);
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

            if (hours > 0) {
                // Если трек длиннее часа (например, аудиокнига или микс): 01:23:45
                return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            } else {
                // Обычный формат для песен: 03:45
                return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            }
        } catch (NumberFormatException e) {
            return "00:00"; // Возвращаем дефолт, если строка не была числом
        }
    }
}
