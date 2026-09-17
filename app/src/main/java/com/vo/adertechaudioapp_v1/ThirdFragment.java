package com.vo.adertechaudioapp_v1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.vo.adertechaudioapp_v1.config.AppConfigItem;
import com.vo.adertechaudioapp_v1.config.AppConfigModel;
import com.vo.adertechaudioapp_v1.databinding.FragmentThirdBinding;

import java.util.ArrayList;
import java.util.List;

public class ThirdFragment extends Fragment implements EditorAdapter.IEditorAdapterListeners {

    public interface IEditorListeners {
        void onClickSaveEditorListener(AppConfigModel appConfigModel);
    }

    public static IEditorListeners editorListeners;

    private static final String TAG = ThirdFragment.class.getSimpleName();
    final String KEY_CONFIG_MODEL = "key_app_config_model";

    private static final int FRIENDLY_NAME_LENGTH = 20;

    private FragmentThirdBinding binding;

    EditorAdapter editorAdapter;

    private AppConfigModel appConfigModel;                      // конфигурация из файла

    private List<AppConfigItem> appConfigModelListToSave;       // буфер конфигурация для записи

    private boolean isEnableSaved = false;
    public static void addListeners(ThirdFragment.IEditorListeners listeners) {
        editorListeners = listeners;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentThirdBinding.inflate(inflater, container, false);


        /// добавляем данные по ссылке из навигации
        if (getArguments() != null) {
            appConfigModel = getArguments().getParcelable(KEY_CONFIG_MODEL);

            assert appConfigModel != null;
            ///  клонируем данные для записи

            appConfigModelListToSave = new ArrayList<>();
            for (AppConfigItem item : appConfigModel.getAppConfigItemList()) {
                appConfigModelListToSave.add(new AppConfigItem(item.getName(), item.getFriendlyName(),
                        item.getType(), item.getInterfaceType(), item.getChannel(), item.isVisible()
                        ));
            }
        }

        // Настраиваем RecyclerView
        // Используем LinearLayoutManager для вертикального списка
        // Для других вариантов смотреть другие менеджеры (GridLayoutManager, StaggeredGridLayoutManager)
        binding.recyclerViewThirdFragmentSelectInput.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        );

        // Создаём и устанавливаем адаптер
        editorAdapter = new EditorAdapter(requireContext(), appConfigModel.getAppConfigItemList(), this);
        binding.recyclerViewThirdFragmentSelectInput.setAdapter(editorAdapter);

//        final Handler handler = new Handler(Looper.getMainLooper());
//
//        handler.postDelayed(new Runnable() {
//            int i = 0;
//            @Override
//            public void run() {
//                if (i <= 100) {
//                    binding.linearLayoutThird.setVisibility(View.GONE);
//                    binding.circularProgressIndicatorWaitEditor.setVisibility(View.VISIBLE);
//                    binding.circularProgressIndicatorWaitEditor.setProgress(i);
//                    i++;
//                    handler.postDelayed(this, 10);
//                } else {
//                    binding.circularProgressIndicatorWaitEditor.setVisibility(View.GONE);
//                    binding.linearLayoutThird.setVisibility(View.VISIBLE);
//                    handler.removeCallbacks(this);
//                }
//            }
//        }, 10);
//
//
//        editorAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                /// RecyclerView загружен и данные есть
//                binding.circularProgressIndicatorWaitEditor.setVisibility(View.GONE);
//                binding.linearLayoutThird.setVisibility(View.VISIBLE);
//                handler.removeCallbacks(handler.getLooper().getThread());
//            }
//        });


        return binding.getRoot();

    }

    @SuppressLint("NotifyDataSetChanged")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        loadData();

        /// устанавливаем первый источник для настройке при полной отрисовке списка
        editorAdapter.setSelectedPosition(0);
        binding.recyclerViewThirdFragmentSelectInput.scrollToPosition(0);
        binding.textInputEditTextSourceFriendlyName.setText(appConfigModelListToSave.get(0).getFriendlyName());
        binding.checkboxEditorVisibility.setChecked(appConfigModelListToSave.get(0).isVisible());

        // деактивируем кнопку записи до получения необходимых данных
        binding.buttonSaveEditor.setEnabled(isEnableSaved);

        binding.textInputEditTextSourceFriendlyName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String friendlyName = s.toString().trim();
                isEnableSaved = isValidFriendlyName(friendlyName);
                if (isEnableSaved) {
                    binding.textInputEditTextSourceFriendlyName.setError(null);
                    if (editorAdapter.getSelectedPosition() != -1) {
                        appConfigModelListToSave
                                .get(editorAdapter.getSelectedPosition()).setFriendlyName(friendlyName);
                    }
                } else {
                    binding.textInputEditTextSourceFriendlyName.setError("The number of characters exceeds " + FRIENDLY_NAME_LENGTH);
                }
                binding.buttonSaveEditor.setEnabled(isEnableSaved);
            }
        });

        binding.checkboxEditorVisibility.addOnCheckedStateChangedListener(new MaterialCheckBox.OnCheckedStateChangedListener() {
            @Override
            public void onCheckedStateChangedListener(@NonNull MaterialCheckBox checkBox, int state) {
                if (state == MaterialCheckBox.STATE_CHECKED)
                    appConfigModelListToSave.get(editorAdapter.getSelectedPosition()).setVisible(true);
                else if (state == MaterialCheckBox.STATE_UNCHECKED)
                    appConfigModelListToSave.get(editorAdapter.getSelectedPosition()).setVisible(false);
            }
        });

        binding.buttonSaveEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appConfigModel.getAppConfigItemList().clear();
                for (AppConfigItem item : appConfigModelListToSave) {
                    appConfigModel.getAppConfigItemList().add(item);
                }
                editorListeners.onClickSaveEditorListener(appConfigModel);
            }
        });

        binding.buttonCancelEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// перезаписываем значения в буфер для записи
                appConfigModelListToSave.clear();
                for (AppConfigItem item : appConfigModel.getAppConfigItemList()) {
                    appConfigModelListToSave.add(new AppConfigItem(item.getName(), item.getFriendlyName(),
                            item.getType(), item.getInterfaceType(), item.getChannel(), item.isVisible()
                    ));
                }

                if (editorAdapter.getSelectedPosition() == -1) {
                    binding.textInputEditTextSourceFriendlyName.setText(R.string.source_not_selected);
                    binding.checkboxEditorVisibility.setChecked(false);
                    binding.textInputEditTextSourceFriendlyName.setEnabled(false);
                    binding.checkboxEditorVisibility.setEnabled(false);
                } else {

//                    Log.d(TAG, "Friendly Name: " + appConfigModel
//                            .getAppConfigItemList().get(editorAdapter.getSelectedPosition()).getFriendlyName());

                    binding.textInputEditTextSourceFriendlyName.setText(appConfigModelListToSave
                            .get(editorAdapter.getSelectedPosition()).getFriendlyName()
                    );
                    binding.checkboxEditorVisibility.setChecked(appConfigModelListToSave
                            .get(editorAdapter.getSelectedPosition()).isVisible()
                    );
                    binding.textInputEditTextSourceFriendlyName.setEnabled(true);
                    binding.checkboxEditorVisibility.setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClickListener(View view, AppConfigItem configItem, int selectedPosition) {

        if (selectedPosition == -1) {
            binding.textInputEditTextSourceFriendlyName.setText(R.string.source_not_selected);
            binding.checkboxEditorVisibility.setChecked(false);
            binding.textInputEditTextSourceFriendlyName.setEnabled(false);
            binding.checkboxEditorVisibility.setEnabled(false);
        } else {
            binding.textInputEditTextSourceFriendlyName.setText(appConfigModelListToSave
                    .get(selectedPosition).getFriendlyName()
            );
            binding.checkboxEditorVisibility.setChecked(appConfigModelListToSave
                    .get(selectedPosition).isVisible()
            );
            binding.textInputEditTextSourceFriendlyName.setEnabled(true);
            binding.checkboxEditorVisibility.setEnabled(true);
        }
    }

//    private void loadData() {
//        binding.circularProgressIndicatorWaitEditor.setVisibility(View.VISIBLE);
//        binding.linearLayoutThird.setVisibility(View.GONE);
//
//        try (ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor()) {
//            executorService.schedule(() -> {
//                new Handler(Looper.getMainLooper()).post(() -> {
//                    binding.circularProgressIndicatorWaitEditor.setVisibility(View.GONE);
//                    binding.linearLayoutThird.setVisibility(View.VISIBLE);
//                });
//            }, 2, TimeUnit.SECONDS);
//        }
//    }

    private static boolean isValidFriendlyName(final String text) {
        return (text.length() <= FRIENDLY_NAME_LENGTH);
    }
}