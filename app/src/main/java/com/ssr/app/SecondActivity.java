package com.ssr.app;

import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.Nullable;

import com.library.ssr.bg.BaseService;

public class SecondActivity extends SsrActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_second);
        findViewById(R.id.tv_button).setOnClickListener(v -> {
            toggle();
        });

        findViewById(R.id.tv_test).setOnClickListener(v -> {
            testConnection(state);
        });
    }


    @Override
    protected void changeState(BaseService.State state, @Nullable String msg, Boolean animate) {
        Log.e("zkq_second", "--> changeState()  state=" + state + ", msg=" + msg + ", animate=" + animate);

        if (state == BaseService.State.Connected) {
            // 连接成功，开始测试是否能上网
            testConnection(state);

            // 更新状态（只是连接到 VPN 节点了，但该节点是否能上网还需要通过 HttpTester 进行测试）
            tester.getStatus().observe(this, status -> {
                Log.e("zkq_second", "--> HttpTester test status=" + status);
                status.retrieve(text -> {
                    Log.e("zkq_second", "--> HttpTester test status.text=" + text);
                    return null;
                }, errMsg -> {
                    Log.e("zkq_second", "--> HttpTester test status.errMsg=" + errMsg);
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

    @Override
    protected void updateTraffic(long txRate, long rxRate, long txTotal, long rxTotal) {
        Log.e("zkq_second", "--> updateTraffic()  " +
                "txRate=" + Formatter.formatFileSize(this, txRate) +
                ", rxRate=" + Formatter.formatFileSize(this, rxRate) +
                ", txTotal=" + Formatter.formatFileSize(this, txTotal) + "/s" +
                ", rxTotal=" + Formatter.formatFileSize(this, rxTotal) + "/s"
        );
    }

}
