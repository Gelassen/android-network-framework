package com.github.gelassen.clientservercore.network;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;

import com.github.gelassen.clientservercore.Info;

public abstract class Command implements Parcelable{

    public static final String COMMAND = "COMMAND";
    
    public static final String RECEIVER = "RECEIVER";
    
    private static final AndroidHttpClient client = AndroidHttpClient.newInstance(Info.TAG);
    
    
    protected Bundle resultData;
    protected ResultReceiver resultReceiver;
    protected HttpUriRequest request;
    protected Context context;
    
    public Command() {
        resultData = new Bundle();
        updateCommandStatus(true, HttpStatus.SC_OK, null);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public void setContext(Context context) {
        this.context = context;
    }

    public void setReceiver(ResultReceiver resultReceiver) {
        this.resultReceiver = resultReceiver;
    }
    
    public void start(Context context, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, NetworkService.class);
        intent.putExtra(COMMAND, this);
        intent.putExtra(RECEIVER, resultReceiver);
        context.startService(intent);
    }
    
    public void execute(Context context) {
        Log.d(Info.TAG, String.format("%s is started", this.getClass().getSimpleName()));
        final long stime = System.currentTimeMillis();
        try {
            setContext(context);
            initCommand();
            if (isNetworkAvailable()) {
                HttpResponse response = client.execute(request);
                final int code = response.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK) {
                    processRequest(response);
                } else {
                    processError(code);
                }
            }
        } catch (IOException e) {
            Log.d(Info.TAG, "Failed to execute command", e);
        } finally {
            Log.d(Info.TAG, String.format("%s is ended, %s", 
                    this.getClass().getSimpleName(),
                        System.currentTimeMillis() - stime));
        }

    }

    protected abstract void initCommand();
    
    protected abstract void processRequest(HttpResponse response);
    
    protected void processError(final int errorCode) {
        Log.e(Info.TAG, String.format("Server returns %s", errorCode));
        updateCommandStatus(false, errorCode, null);
    }
    
    protected void updateCommandStatus(final boolean status, final int code, final Bundle data) {
        resultData.putBoolean(BundleContract.SUCCESS, status);
        resultData.putInt(BundleContract.CODE, code);
        resultData.putBundle(BundleContract.PAYLOAD, data == null ? new Bundle() : data);
    }

    protected void notifyListeners() {
        final int resultCode = 0;
        if (resultReceiver != null) {
            resultReceiver.send(resultCode, resultData);
        }
    }
    
    private boolean isNetworkAvailable() {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(NetworkService.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
        return networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
