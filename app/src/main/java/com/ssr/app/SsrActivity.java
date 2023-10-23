package com.ssr.app;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.library.ssr.Core;
import com.library.ssr.aidl.IShadowsocksService;
import com.library.ssr.aidl.ShadowsocksConnection;
import com.library.ssr.aidl.TrafficStats;
import com.library.ssr.bg.BaseService;
import com.library.ssr.net.HttpsTest;
import com.library.ssr.utils.StartService;

public abstract class SsrActivity extends AppCompatActivity implements ShadowsocksConnection.Callback {
    protected BaseService.State state = BaseService.State.Idle;

    protected final ShadowsocksConnection connection = new ShadowsocksConnection(true);

    protected final ActivityResultLauncher<Void> connect = registerForActivityResult(new StartService(), bool -> {
        if (bool) {
            Toast.makeText(this, R.string.vpn_permission_denied, Toast.LENGTH_SHORT).show();
        }
    });

    protected final HttpsTest tester = new HttpsTest();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // reset everything to init state
        changeState(BaseService.State.Idle, null, false);
        connection.connect(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connection.setBandwidthTimeout(500);
    }

    @Override
    protected void onStop() {
        connection.setBandwidthTimeout(0);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.disconnect(this);
    }

    protected void toggle() {
        if (state.getCanStop()) {
            Core.INSTANCE.stopService();
        } else {
            connect.launch(null);
        }
    }

    protected void testConnection(BaseService.State state) {
        Log.e("SsrActivity", "--> testConnection()  state=" + state);
        if (state == BaseService.State.Connected) {
            tester.testConnection();
        }
    }


    protected void changeState(BaseService.State state, @Nullable String msg, Boolean animate) {
        Log.e("SsrActivity", "--> changeState()  state=" + state + ", msg=" + msg + ", animate=" + animate);

        if (state == BaseService.State.Connected) {
            // 连接成功，开始测试是否能上网
            testConnection(state);

            // 更新状态（只是连接到 VPN 节点了，但该节点是否能上网还需要通过 HttpTester 进行测试）
            tester.getStatus().observe(this, status -> {
                Log.e("SsrActivity", "--> HttpTester test status=" + status);
                if (status instanceof HttpsTest.Status.Idle) { // 测试准备完成

                } else if (status instanceof HttpsTest.Status.Testing) { // 测试中

                } else if (status instanceof HttpsTest.Status.Error) { // 测试失败

                } else { // HttpsTest.Status.Success 测试成功

                }
                status.retrieve(text -> {
                    Log.e("SsrActivity", "--> HttpTester test status.text=" + text);
                    return null;
                }, errMsg -> {
                    Log.e("SsrActivity", "--> HttpTester test status.errMsg=" + errMsg);
                    return null;
                });
            });
        } else {
            tester.getStatus().removeObservers(this);
            if (state != BaseService.State.Idle) {
                tester.invalidate();
            }
            // 更新状态
            switch (state) {
                case Connecting:
                    break;
                case Stopping:
                    break;
                default: // Idle / Stopped
                    break;
            }
        }

        this.state = state;

    }

    protected void updateTraffic(long txRate, long rxRate, long txTotal, long rxTotal) {
        Log.e("SsrActivity", "--> updateTraffic()  " +
                "txRate=" + Formatter.formatFileSize(this, txRate) +
                ", rxRate=" + Formatter.formatFileSize(this, rxRate) +
                ", txTotal=" + Formatter.formatFileSize(this, txTotal) + "/s" +
                ", rxTotal=" + Formatter.formatFileSize(this, rxTotal) + "/s"
        );
    }

    /*================  ShadowsocksConnection.Callback  ===============*/


    @Override
    public void stateChanged(@NonNull BaseService.State state, @Nullable String profileName, @Nullable String msg) {
        Log.e("SsrActivity", "--> stateChanged()  state=" + state);
        changeState(state, msg, true);
    }

    @Override
    public void trafficUpdated(long profileId, @NonNull TrafficStats stats) {
        Log.e("SsrActivity", "--> trafficUpdated()  " +
                "profileId=" + profileId +
                ", stats=" + stats
        );
        updateTraffic(stats.getTxRate(), stats.getRxRate(), stats.getTxTotal(), stats.getRxTotal());
    }



    @Override
    public void trafficPersisted(long profileId) {
        Log.e("SsrActivity", "--> trafficPersisted()  " +
                "profileId=" + profileId
        );
    }

    @Override
    public void onServiceConnected(@NonNull IShadowsocksService service) {
        Log.e("SsrActivity", "--> onServiceConnected()  " +
                "service=" + service
        );

        try {
            int state = service.getState();
            changeState(BaseService.State.values()[state], null, true);
        } catch (RemoteException e) {
            e.printStackTrace();
            changeState(BaseService.State.Idle, null, true);
        }
    }

    @Override
    public void onServiceDisconnected() {
        Log.e("SsrActivity", "--> onServiceDisconnected()");
        changeState(BaseService.State.Idle, null, true);
    }

    @Override
    public void onBinderDied() {
        Log.e("SsrActivity", "--> onBinderDied()");
        connection.disconnect(this);
        connection.connect(this, this);
    }
}
