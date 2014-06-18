package com.home.clientservercore.network;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.home.clientservercore.App;
import com.home.clientservercore.Params;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ResultReceiver;
import android.util.Log;

public class NetworkService extends Service {

    private static final String NAMESPACE = NetworkService.class.getName();

    private static final long KEEP_ALIVE = 1;
    private static final int THREADS = 4;
    private static final int MAX_THREAD = 6;
    
    private WakeLock wakeLock;

    private final ConcurrentLinkedQueue<Integer> startIdQueue = new ConcurrentLinkedQueue<Integer>();
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS, MAX_THREAD, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    /*package*/ final static void start(final Context context, final Command command, final ResultReceiver receiver) {
        context.startService(
                new Intent(context, NetworkService.class)
                    .putExtra(Params.Intent.EXTRA_COMMAND, command)
                    .putExtra(Params.Intent.EXTRA_RECEIVER, receiver)
        );
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAMESPACE);
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        pool.shutdown();
        wakeLock.release();
        super.onDestroy();

        Log.d(App.TAG, "Network service is destroyed");
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        processIntent(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processIntent(intent, startId);
        return START_NOT_STICKY;
    }
    

    private void processIntent(final Intent intent, int startId) {
        startIdQueue.add(startId);
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Command command = intent.getParcelableExtra(Params.Intent.EXTRA_COMMAND);
                    final ResultReceiver resultReceiver = intent.getParcelableExtra(Params.Intent.EXTRA_RECEIVER);
                    if (command != null) {
                        command.execute(getApplicationContext(), resultReceiver);
                    }
                } finally {
                    stopSelf(startIdQueue.remove());
                }

            }
        });
    }
    
    

}
