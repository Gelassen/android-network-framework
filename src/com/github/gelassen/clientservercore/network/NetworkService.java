package com.github.gelassen.clientservercore.network;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.github.gelassen.clientservercore.Info;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.ResultReceiver;
import android.util.Log;

public class NetworkService extends Service {

    private static final long KEEP_ALIVE = 1;
    
    private static final int THREADS = 4;
    
    private static final int MAX_THREAD = 6;
    
    
    private WakeLock wakeLock;
    
    private ThreadPoolExecutor pool = new ThreadPoolExecutor(THREADS, MAX_THREAD, 
            KEEP_ALIVE, TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>());
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        pool.shutdown();
        wakeLock.release();
        super.onDestroy();
        
        Log.d(Info.TAG, "Network service is destroyed");
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        processIntent(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        processIntent(intent);
        return START_NOT_STICKY;
    }
    

    private void processIntent(Intent intent) {
        final Command command = intent.getParcelableExtra(Command.COMMAND);
        final ResultReceiver resultReceiver = intent.getParcelableExtra(Command.RECEIVER);
        command.setReceiver(resultReceiver);
        pool.submit(new Runnable() {
            
            @Override
            public void run() {
                command.execute(getApplicationContext());
            }
        });
    }
    
    

}
