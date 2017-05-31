package com.colinknecht.flickrbrowserapp;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE, PROCESSING, NOT_INITIALIZED, FAILED_OR_EMPTY, OK}
/**
 * Created by colinknecht on 5/18/17.
 */

class GetRawData extends AsyncTask<String, Void, String> {
    private static final String TAG = "GetRawData";
    private DownloadStatus mDownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }
    public GetRawData(OnDownloadComplete callback) {
        this.mDownloadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (strings == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALIZED;
            return null;
        }
        
        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();

            Log.d(TAG, "doInBackground: The Response code was " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while (null != (line = reader.readLine())) {
                result.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;
            return result.toString();
        }
        catch (MalformedURLException mue) {
            Log.e(TAG, "doInBackground: Invalid Url " + mue.getMessage() );
        }
        catch (IOException ioe) {
            Log.e(TAG, "doInBackground: IO Exception " + ioe.getMessage() );
        }
        catch (SecurityException se) {
            Log.e(TAG, "doInBackground: Security Exception...Needs Permission? " + se.getMessage() );
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ioe) {
                    Log.e(TAG, "doInBackground: Error Closing Stream "+ ioe.getMessage() );
                }
            }
        }
        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }//doInBackground

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: parameter = " + s);
        if (mCallback != null) {
            mCallback .onDownloadComplete(s, mDownloadStatus);
            Log.d(TAG, "onPostExecute: Ends");
        }
    }
    void runInSameThread (String s) {
        Log.d(TAG, "runInSameThread: method starts");

//        onPostExecute(doInBackground(s));
        if (mCallback != null) {
//            String result = doInBackground(s);
//            mCallback.onDownloadComplete(result, mDownloadStatus);
            mCallback.onDownloadComplete(doInBackground(s),mDownloadStatus);
        }

        Log.d(TAG, "runInSameThread: method ends");
    }
}//getRawData Class
