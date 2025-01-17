package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;

import com.evernote.android.state.StateSaver;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.broadcastreceivers.NetworkWifiStatusReceiver;
import ml.docilealligator.infinityforreddit.broadcastreceivers.WallpaperChangeReceiver;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ToggleSecureModeEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class Infinity extends Application implements LifecycleObserver {
    private AppComponent mAppComponent;
    private NetworkWifiStatusReceiver mNetworkWifiStatusReceiver;
    //private boolean lock = false;
    private boolean isSecureMode;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        mAppComponent.inject(this);

        isSecureMode = mSharedPreferences.getBoolean(SharedPreferencesUtils.SECURE_MODE, false);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (isSecureMode) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                /*if (lock && !(activity instanceof LockScreenActivity)) {
                    lock = false;
                    Intent intent = new Intent(activity, LockScreenActivity.class);
                    activity.startActivity(intent);
                }*/
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

        //ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });

        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();

        EventBus.getDefault().register(this);

        mNetworkWifiStatusReceiver =
                new NetworkWifiStatusReceiver(() -> EventBus.getDefault().post(new ChangeNetworkStatusEvent(Utils.getConnectedNetwork(getApplicationContext()))));
        registerReceiver(mNetworkWifiStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        registerReceiver(new WallpaperChangeReceiver(), new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
    }

    /*@OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground() {
        lock = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground(){

    }*/

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    @Subscribe
    public void onToggleSecureModeEvent(ToggleSecureModeEvent secureModeEvent) {
        isSecureMode = secureModeEvent.isSecureMode;
    }
}
