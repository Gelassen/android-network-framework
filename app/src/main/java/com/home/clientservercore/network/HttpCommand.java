package com.home.clientservercore.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.ResultReceiver;
import android.util.Log;

import com.home.clientservercore.App;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

public abstract class HttpCommand extends Command {

    private static final AndroidHttpClient client = AndroidHttpClient.newInstance(App.TAG);

    protected Status status = new Status();
    protected HttpUriRequest httpUriRequest;

    @Override
    public void execute(Context context, ResultReceiver receiver) {
        super.execute(context, receiver);
        Log.d(App.TAG, String.format("%s is started", getClass().getSimpleName()));
        final long stime = System.currentTimeMillis();
        HttpResponse response = null;
        try {
            createRequest();
            if (isNetworkAvailable()) {
                response = client.execute(httpUriRequest);
                final int code = response.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK) {
                    processRequest(response);
                } else {
                    processError(code);
                }
            } else {
                processError(Status.FAILED_NETWORK);
            }
        } catch (IOException e) {
            Log.d(App.TAG, "Failed to execute command", e);
            processError(Status.FAILED_TO_EXECUTE_REQUEST);
        } finally {
            if (response != null) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try {
                        entity.consumeContent();
                    } catch (IOException e) {
                        Log.e(App.TAG, "Failed to consume http entity content", e);
                    }
                }
            }

        }
        Log.d(App.TAG, String.format("%s is ended, %s",
                getClass().getSimpleName(),
                System.currentTimeMillis() - stime));
        notifyListeners(status);
    }

    protected void processError(final int statusCode) {
        status.add(statusCode);
    };

    protected abstract void createRequest();

    protected abstract void processRequest(HttpResponse response);

    private boolean isNetworkAvailable() {
        NetworkInfo networkInfo = ((ConnectivityManager) context
                .getSystemService(NetworkService.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return networkInfo == null ? false : networkInfo.isAvailable() && networkInfo.isConnected();
    }
}
