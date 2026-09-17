package com.vo.adertechaudioapp_v1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vo.adertechaudioapp_v1.localAudio.AudioModel;
import com.vo.adertechaudioapp_v1.localAudio.AudioScanner;
import com.vo.adertechaudioapp_v1.config.AppConfigItem;
import com.vo.adertechaudioapp_v1.config.AppConfigModel;
import com.vo.adertechaudioapp_v1.databinding.FragmentFirstBinding;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FirstFragment extends Fragment implements RouteAdapter.IRouteAdapterListeners {

    private static final String TAG = FirstFragment.class.getSimpleName();

    private static final int PERMISSION_REQ_CODE = 101;

    enum IconButtonTrackRepeat {
        STATE_NO_REPEAT,
        STATE_ONE_REPEAT,
        STATE_ALL_REPEAT
    }

    final String KEY_VOLUME = "key_volume";
    final String KEY_MUTE = "key_mute";
    final String KEY_ROUTE = "key_route";
    private final int DELAY = 120;                              // Интервал отправки команды с кнопки при удержании
    private FragmentFirstBinding binding;
    private Handler handler;
    private Runnable runnable;
    RouteAdapter routeAdapter;
    static List<AppConfigItem> appConfigItemList;
    static String outputFriendlyName;
    private IconButtonTrackRepeat iconButtonTrackRepeat;

    // Создаем пул потоков для фоновых задач (I/O)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /// Регистрируем лаунчер прямо в поле класса фрагмента
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    /// Разрешение получено, загружаем музыку
                    loadMusicAsync();
                } else {
                    /// В разрешении отказано
                    Toast.makeText(getContext(), "Access to music denied!", Toast.LENGTH_SHORT).show();
                }
            });

    public static void addAppConfigItemList(AppConfigModel modelList) {
        appConfigItemList = new ArrayList<>();
        /// добавляем только входы
        for (AppConfigItem appConfigItem : modelList.getAppConfigItemList()) {
            if (appConfigItem.getType() == AppConfigItem.Type.INPUT) {
                /// наполняем только видимыми пунктами
                if (appConfigItem.isVisible()) appConfigItemList.add(appConfigItem);
            } else if (appConfigItem.getType() == AppConfigItem.Type.OUTPUT) {
                outputFriendlyName = appConfigItem.getFriendlyName();
            }
        }
    }

    public void updateVolume(double volume) {
        binding.textViewCurrentSource.setText(
                String.format("%s: ", outputFriendlyName));
        binding.textViewCurrentVolume.setText(
                String.format(Locale.US,"%.1f dB", volume));
    }

    public void updateLevel(double level) {
        binding.progressBarVolumeLevel.setProgress((int) (level * 100));
    }

    public void updateMute(boolean mute) {
        if (mute) {
            binding.buttonVolumeMute.setText(R.string.mute);
        } else {
            binding.buttonVolumeMute.setText(R.string.unmute);
        }
        binding.buttonVolumeMute.setChecked(mute);
    }

    public void updateRouting(int channelInput, boolean value) {
        if (channelInput < 0) return;
        for (int i = 0; i < appConfigItemList.size(); i++) {
            if (appConfigItemList.get(i).getChannel() == channelInput) {
                if (value) routeAdapter.setSelectedPosition(i);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);

        handler = new Handler(Looper.getMainLooper());

        // Настраиваем RecyclerView
        // Используем LinearLayoutManager для вертикального списка
        // Для других вариантов смотреть другие менеджеры (GridLayoutManager, StaggeredGridLayoutManager)
        binding.recyclerViewFirstFragmentSelectInput.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        );

        // Создаём и устанавливаем адаптер
        routeAdapter = new RouteAdapter(requireContext(), appConfigItemList, this);
        binding.recyclerViewFirstFragmentSelectInput.setAdapter(routeAdapter);

        if (getArguments() != null) {
            if (getArguments().containsKey(KEY_VOLUME)) {
                updateVolume(getArguments().getDouble(KEY_VOLUME));
            }
            if (getArguments().containsKey(KEY_MUTE)) {
                updateMute(getArguments().getBoolean(KEY_MUTE));
            }
            if (getArguments().containsKey(KEY_ROUTE)) {
                updateRouting(getArguments().getInt(KEY_ROUTE), true);
            }
        }

//        binding.progressBarVolumeLevel.setProgressDrawable(
//                ContextCompat.getDrawable(this.requireContext(), R.drawable.gradient_progress_level)
//        );

        this.iconButtonTrackRepeat = IconButtonTrackRepeat.STATE_NO_REPEAT;

        return binding.getRoot();
    }

    private void startAction(String direction) {
        if (runnable != null) return;
        runnable = new Runnable() {
            @Override
            public void run() {
                if (direction.equals("up")) {
                    // TODO: установить, если управляем выходом
                    MainActivity.dspDriver.setVolumeOutputUp();

                    // TODO: установить, если управляем входами
//                    MainActivity.dspDriver.setVolumeInputUp();

                } else if (direction.equals("down")) {
                    // TODO: установить, если управляем выходом
                    MainActivity.dspDriver.setVolumeOutputDown();

                    // TODO: установить, если управляем входами
//                    MainActivity.dspDriver.setVolumeInputDown();
                }
                // TODO: установить, если управляем выходом
                binding.textViewCurrentVolume.setText(
                        String.format(Locale.US, "%.1f dB",
                                MainActivity.dspDriver.getChannelOutput().getVolume()
                        )
                );

                // TODO: установить, если управляем входами
//                binding.textViewCurrentVolume.setText(
//                                    String.format(Locale.US, "%.1f dB",
//                                            MainActivity.dspDriver.getChannelInputList().get(MainActivity.dspDriver.getInputSelected()).getVolume()
//                                    )
//                            );
                handler.postDelayed(this, DELAY);
            }
        };
        handler.post(runnable);
    }

    private void stopAction() {
        handler.removeCallbacks(runnable);
        runnable = null;
    }

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO: установить, если управляем выходом
        binding.buttonVolumeUp.setEnabled(true);
        binding.buttonVolumeDown.setEnabled(true);
        binding.buttonVolumeMute.setEnabled(true);
//        binding.progressBarVolumeLevel.setProgress(-3200);

        if (routeAdapter != null) routeAdapter.notifyDataSetChanged();

        // Проверяем и запрашиваем разрешение при создании View
        checkPermissionsAndScan();

        binding.buttonVolumeUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startAction("up");
                        binding.buttonVolumeUp.setPressed(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopAction();
                        binding.buttonVolumeUp.setPressed(false);
                        return true;
                }
                return false;
            }
        });

        binding.buttonVolumeDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startAction("down");
                        binding.buttonVolumeDown.setPressed(true);
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopAction();
                        binding.buttonVolumeDown.setPressed(false);
                        return true;
                }
                return false;
            }
        });

        binding.buttonVolumeMute.setOnClickListener(v -> {
            boolean mute = binding.buttonVolumeMute.isChecked();
            if (mute) {
                binding.buttonVolumeMute.setText(R.string.mute);
            } else {
                binding.buttonVolumeMute.setText(R.string.unmute);
            }

            // TODO: установить, если управляем выходом
            MainActivity.dspDriver.setMuteOutput(mute);

            // TODO: установить, если управляем входами
//            MainActivity.dspDriver.setMuteInput(mute);
        });

        binding.materialButtonTrackRepeat.setOnClickListener(v -> {
            // переключаем состояние по порядку
            switch (iconButtonTrackRepeat) {
                case STATE_NO_REPEAT:
                    iconButtonTrackRepeat = IconButtonTrackRepeat.STATE_ONE_REPEAT;
                    binding.materialButtonTrackRepeat.setIconResource(R.drawable.outline_repeat_one_24);
                    binding.materialButtonTrackRepeat.setChecked(true);
                    break;
                case STATE_ONE_REPEAT:
                    iconButtonTrackRepeat =IconButtonTrackRepeat.STATE_ALL_REPEAT;
                    binding.materialButtonTrackRepeat.setIconResource(R.drawable.outline_repeat_24);
                    binding.materialButtonTrackRepeat.setChecked(true);
                    break;
                case STATE_ALL_REPEAT:
                    iconButtonTrackRepeat = IconButtonTrackRepeat.STATE_NO_REPEAT;
                    binding.materialButtonTrackRepeat.setIconResource(R.drawable.outline_repeat_24);
                    binding.materialButtonTrackRepeat.setChecked(false);
                    break;
            }
        });

        binding.iconButtonMenuAddFavorite.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.menu_add_favorite, popupMenu.getMenu());

            // Показываем иконки в всплывающем меню
            try {
                @SuppressLint("DiscouragedPrivateApi") Field field = popupMenu.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                Object mPopup = field.get(popupMenu);
                assert mPopup != null;
                Method method = mPopup.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
                method.setAccessible(true);
                method.invoke(mPopup, true);
                Log.d(TAG, "Click menu add favorite\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.item_addFavorite) {
                        Log.d(TAG, "Click item add favorite\n");
                        return true;
                    } else if (id == R.id.item_addPlaylist) {
                        Log.d(TAG, "Click item add playlist\n");
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            // Показываем меню на экране
            popupMenu.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClickListener(View view, AppConfigItem configItem, int selectedPosition) {

        // TODO: установить, если управляем выходом


        binding.textViewCurrentSource.setText(
                String.format("%s: ", outputFriendlyName)
        );

        updateVolume(MainActivity.dspDriver.getChannelOutput().getVolume());

        /// сразу синхронизируем кнопку mute
        updateMute(MainActivity.dspDriver.getChannelOutput().isMute());


        if (selectedPosition == -1) {
            // TODO: установить, если управляем входами
//            binding.textViewCurrentSource.setText("");
//            binding.textViewCurrentVolume.setText("");
//            binding.buttonVolumeUp.setEnabled(false);
//            binding.buttonVolumeDown.setEnabled(false);
//            binding.buttonVolumeMute.setEnabled(false);

            MainActivity.dspDriver.setRoute(selectedPosition);
        } else {
            // TODO: установить, если управляем входами
//            binding.textViewCurrentSource.setText(configItem.getFriendlyName());
//            binding.textViewCurrentVolume.setText(
//                    String.format(Locale.US, "%.1f dB", MainActivity.dspDriver.getChannelInputList().get(selectedPosition).getVolume())
//            );
//            binding.buttonVolumeUp.setEnabled(true);
//            binding.buttonVolumeDown.setEnabled(true);
//            binding.buttonVolumeMute.setEnabled(true);
//            /// сразу синхронизируем кнопку mute
//            binding.buttonVolumeMute.setChecked(MainActivity.dspDriver.getChannelInputList().get(selectedPosition).isMute());

            MainActivity.dspDriver.setRoute(configItem.getChannel());
        }
//        MaterialButton materialButton = (MaterialButton) view;

    }

    private void checkPermissionsAndScan() {
        /// Проверяем контекст на null (безопасность во фрагментах)
        if (getContext() == null) return;

        String permission = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            /// Если разрешение уже есть
            loadMusicAsync();
        } else {
            /// Если разрешения нет — запускаем лаунчер
            requestPermissionLauncher.launch(permission);
        }
    }

    private void loadMusicAsync() {
        if (getContext() == null) return;

        // TODO: Показываем ProgressBar (индикатор загрузки)
        // progressView.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            // Этот блок кода выполняется ВНЕ главного потока
            if (getContext() == null) return;

            // Тяжелая операция сканирования памяти
            Map<String, List<AudioModel>> foldersMap = AudioScanner.scanAudio(getContext());
//            AudioScanner.triggerMediaScanner(getContext());

            // Возвращаем результат в Главный (UI) поток для обновления интерфейса
            if (getActivity() != null) {
                ContextCompat.getMainExecutor(getContext()).execute(() -> {
                    // Этот блок выполняется снова в ГЛАВНОМ потоке
                    onMusicLoaded(foldersMap);
                });
            }
        });
    }

    // Метод, который принимает данные и работает с UI
    private void onMusicLoaded(Map<String, List<AudioModel>> foldersMap) {
        if (getContext() == null) return;

        // TODO: Скрываем ProgressBar загрузки
        // progressView.setVisibility(View.GONE);

        for (Map.Entry<String, List<AudioModel>> entry : foldersMap.entrySet()) {
            Log.d("MusicApp", "Folder: " + entry.getKey() + " (files: " + entry.getValue().size() + ")\n");
            for (AudioModel audio : entry.getValue()) {
                Log.d("MusicApp", "  └─ Track: " + audio.getTitle() + " | Path: " + audio.getPath() + " | Duration: " + audio.getDuration() + "\n");
            }
        }
        // TODO: Передаем данные в адаптер RecyclerView
        // myAdapter.updateData(songs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Обязательно закрываем ExecutorService, чтобы избежать утечек памяти
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}