package com.vo.adertechaudioapp_v1;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.vo.adertechaudioapp_v1.adertech.DspChannel;
import com.vo.adertechaudioapp_v1.adertech.DSPDriver;
import com.vo.adertechaudioapp_v1.config.AppConfig;
import com.vo.adertechaudioapp_v1.config.AppConfigItem;
import com.vo.adertechaudioapp_v1.config.AppConfigModel;
import com.vo.adertechaudioapp_v1.databinding.ActivityMainBinding;
import com.vo.adertechaudioapp_v1.devices.DevicesSpec;

import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements SecondFragment.ISettingsListeners, ThirdFragment.IEditorListeners {

    private static final String TAG = "MainActivity";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private DevicesSpec devicesSpec;
    private AppConfig appConfig;
    public static DSPDriver dspDriver;

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // получаем данные из файловой системы
        devicesSpec = new DevicesSpec(this);
        appConfig = new AppConfig(this);

        FirstFragment.addAppConfigItemList(appConfig.getAppConfigModel());


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {

            hideSystemBar();

            setSupportActionBar(binding.toolbar);

            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

//            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            // регистрируем слушатели
            SecondFragment.addListeners(this);
            ThirdFragment.addListeners(this);

            dspDriver = new DSPDriver(appConfig.getAppConfigModel().getDspIpAddress(), appConfig.getAppConfigModel().getDspIpPort());
            /// наполняем драйвер каналами
            for (AppConfigItem appConfigItem : appConfig.getAppConfigModel().getAppConfigItemList()) {
                if (appConfigItem.getType() == AppConfigItem.Type.INPUT) {
                    dspDriver.addChannelInput(new DspChannel(
                            appConfigItem.getName(),
                            appConfigItem.getChannel(),
                            0.0,
                            false,
                            appConfigItem.getType(),
                            appConfigItem.getInterfaceType()
                    ));
                } else if (appConfigItem.getType() == AppConfigItem.Type.OUTPUT) {
                    dspDriver.addChannelOutput(new DspChannel(
                            appConfigItem.getName(),
                            appConfigItem.getChannel(),
                            0.0,
                            false,
                            appConfigItem.getType(),
                            appConfigItem.getInterfaceType()
                    ));

                }
            }
            /// сбрасываем роутинг на выходе
            dspDriver.resetRouting();
            /// получаем громкость всех входов
            dspDriver.getAllVolumeInputs();
            /// получить мьюты всех входов
            dspDriver.getAllMuteInputs();
            /// получаем громкость выбранного выхода
            dspDriver.getVolumeOutput();
            /// получить мьют выбранного выхода
            dspDriver.getMuteOutput();

            dspDriver.addListeners(new DSPDriver.DSPDriverListeners() {
                @Override
                public void onVolumeOutputListener(int channel, double value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dspDriver.getChannelOutput().getNumber() == channel) {
                                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
                                if (navHostFragment != null) {
                                    Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                                    if (currentFragment instanceof FirstFragment) {
                                        ((FirstFragment) currentFragment).updateVolume(value);
                                    }
                                }
                            }
                        }
                    });
                }
                @Override
                public void onLevelOutputListener(int channel, double value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dspDriver.getChannelOutput().getNumber() == channel) {
                                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
                                if (navHostFragment != null) {
                                    Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                                    if (currentFragment instanceof FirstFragment) {
                                        ((FirstFragment) currentFragment).updateLevel(value);
                                    }
                                }
                            }
                        }
                    });
                }

                @Override
                public void onMuteOutputListener(int channel, boolean value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dspDriver.getChannelOutput().getNumber() == channel) {
                                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
                                if (navHostFragment != null) {
                                    Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                                    if (currentFragment instanceof FirstFragment) {
                                        ((FirstFragment) currentFragment).updateMute(value);
                                    }
                                }
                            }
                        }
                    });
                }

                @Override
                public void onMixerSwitchListener(int channelInput, int channelOutput, boolean value) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dspDriver.getChannelOutput().getNumber() == channelOutput) {
                                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
                                if (navHostFragment != null) {
                                    Fragment currentFragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
                                    if (currentFragment instanceof FirstFragment) {
                                        ((FirstFragment) currentFragment).updateRouting(channelInput, value);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Расширить меню; это добавит элементы на панель действий, если она присутствует.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // отступить текст от иконки
        // binding.toolbar.setContentInsetStartWithNavigation(72);

        return true;
    }

    // Вызов рефлексии в onMenuOpened
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                ///  Принудительно отрисовываем иконки в меню ActionBar
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Здесь обрабатывайте клики по элементам панели действий. Панель действий будет
        // автоматически обрабатывать клики по кнопке "Домой"/"Вверх", если
        // вы укажете родительскую активность в файле AndroidManifest.xml.
        int id = item.getItemId();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            // 1. Получить NavController, где все Fragments
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            // 2. Получить текущий Destination
            NavDestination currentDestination = navController.getCurrentDestination();
            //noinspection SimplifiableIfStatement
            if (id == android.R.id.home) {

                Bundle bundle = new Bundle();
                bundle.putDouble("key_volume", dspDriver.getChannelOutput().getVolume());
                bundle.putBoolean("key_mute", dspDriver.getChannelOutput().isMute());
                bundle.putInt("key_route", dspDriver.getInputSelected());

                if (currentDestination != null && currentDestination.getId() != R.id.FirstFragment) {
                    if (currentDestination.getId() == R.id.SecondFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_SecondFragment_to_FirstFragment, bundle);
                    } else if (currentDestination.getId() == R.id.ThirdFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_ThirdFragment_to_FirstFragment, bundle);
                    }
                }
            } else if (id == R.id.action_settings) {
                // загружаем данные в фрагменты
                SecondFragment.setDeviceSpecModelList(devicesSpec.getDeviceSpecModelList());

                /// отправка данных в SecondFragment
                Bundle bundle = new Bundle();
                bundle.putParcelable("key_app_config_model", appConfig.getAppConfigModel());


                if (currentDestination != null && currentDestination.getId() != R.id.SecondFragment) {
                    if (currentDestination.getId() == R.id.FirstFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                    } else if (currentDestination.getId() == R.id.ThirdFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_ThirdFragment_to_SecondFragment, bundle);
                    }
                }
                return true;
            } else if (id == R.id.action_editor) {

                /// отправка данных в ThirdFragment
                Bundle bundle = new Bundle();
                bundle.putParcelable ("key_app_config_model", appConfig.getAppConfigModel());

                if (currentDestination != null && currentDestination.getId() != R.id.ThirdFragment) {
                    if (currentDestination.getId() == R.id.FirstFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_FirstFragment_to_ThirdFragment, bundle);
                    } else if (currentDestination.getId() == R.id.SecondFragment) {
                        NavHostFragment.findNavController(navHostFragment)
                                .navigate(R.id.action_SecondFragment_to_ThirdFragment, bundle);
                    }
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void hideSystemBar() {
        // 1. Hide the status bar and the navigation bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // 2.
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // 3. скрыть бары навигации и состояния
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());

        // 4. разрешить показать свайом бар навигации и бар состояния
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
    }

    /// слушатель из SecondFragment
    @Override
    public void onClickSaveSettingsListener(AppConfigModel appConfigModel) {
        Log.d(TAG, appConfigModel.toString());
        appConfig.saveConfiguration(appConfigModel);
        FirstFragment.addAppConfigItemList(appConfigModel);
        /// установить новые параметры в DSP socket
        dspDriver.getSocket().setIpAddress(appConfigModel.getDspIpAddress());
        dspDriver.getSocket().setIpPort(appConfigModel.getDspIpPort());

        /// наполняем драйвер каналами
        dspDriver.clearChannelInputs();
        for (AppConfigItem appConfigItem : appConfigModel.getAppConfigItemList()) {
            if (appConfigItem.getType() == AppConfigItem.Type.INPUT) {
                dspDriver.addChannelInput(new DspChannel(
                        appConfigItem.getName(),
                        appConfigItem.getChannel(),
                        0.0,
                        false,
                        appConfigItem.getType(),
                        appConfigItem.getInterfaceType()
                ));
            } else if (appConfigItem.getType() == AppConfigItem.Type.OUTPUT) {
                dspDriver.addChannelOutput(new DspChannel(
                        appConfigItem.getName(),
                        appConfigItem.getChannel(),
                        0.0,
                        false,
                        appConfigItem.getType(),
                        appConfigItem.getInterfaceType()
                ));
            }
        }
        /// сбрасываем роутинг на выходе
        dspDriver.resetRouting();
        /// получаем громкость всех входов
        dspDriver.getAllVolumeInputs();
        /// получить мьюты всех входов
        dspDriver.getAllMuteInputs();
        /// получаем громкость выбранного выхода
        dspDriver.getVolumeOutput();
        /// получить мьют выбранного выхода
        dspDriver.getMuteOutput();
    }

    @Override
    public void onClickSaveEditorListener(AppConfigModel appConfigModel) {
        Log.d(TAG, appConfigModel.toString());
        appConfig.saveConfiguration(appConfigModel);
        FirstFragment.addAppConfigItemList(appConfigModel);
    }
}