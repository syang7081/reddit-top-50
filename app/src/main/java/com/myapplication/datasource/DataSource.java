package com.myapplication.datasource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapplication.model.LinkInfo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by syang on 10/5/2017.
 */

public class DataSource {
    private static final String TAG = DataSource.class.getSimpleName();
    private static DataSource INSTANCE = new DataSource();

    private DataSource() {

    }

    public static DataSource getInstance() {
        return INSTANCE;
    }

    public Observable<List<LinkInfo>> getRedditLinks(final String url) {
        return Observable.create(new ObservableOnSubscribe<List<LinkInfo>>() {
                public void subscribe(ObservableEmitter<List<LinkInfo>> emitter) {
                    try {
                        String jsonStr = getJsonString(url);

                        final ObjectMapper mapper = new ObjectMapper();

                        List<LinkInfo> linkInfos = new ArrayList<LinkInfo>();

                        // We can build a data model to hold the entire json response. For simplicity,
                        // here the json string is parsed into a node tree and the "children" node/array is
                        // retrieved, and individual "data" objects (TreeNode) in the children are converted
                        // to LinkInfo.

                        TreeNode treeNode = mapper.readTree(jsonStr).get("data").get("children");
                        for (int j = 0; j < treeNode.size(); j++) {
                            TreeNode dataNode = treeNode.get(j);
                            dataNode = dataNode.get("data");
                            LinkInfo linkInfo = mapper.convertValue(dataNode, LinkInfo.class);
                            linkInfos.add(linkInfo);
                        }
                        emitter.onNext(linkInfos);
                    }
                    catch (Exception e) {
                        if (emitter.isDisposed()) {
                            Log.e(TAG, e.getLocalizedMessage());
                        } else {
                            emitter.onError(new Exception("Failed to parse Reddit json response. e: " + e.getLocalizedMessage()));
                        }
                    }
                }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private String getJsonString(String urlStr) throws Exception {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                int statusCode = urlConnection.getResponseCode();
                if (statusCode == 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    bufferedReader.close();
                    return sb.toString();
                } else {
                    throw new Exception("Failed to get response from the server, status code = " + statusCode
                                        + ", url = " + urlStr);
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception: ", e);
            throw e;
        }
    }

    /**
     * Need a cache mechanism here. May switch to Fresco or Piccaso
     * @param urlStr
     * @return
     */
    public Observable<Bitmap> loadImage(final String urlStr) {
        return Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                try {
                    Log.d(TAG, "Image url: " + urlStr);
                    URL url = new URL(urlStr);
                    InputStream inputStream = url.openConnection().getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                    emitter.onNext(bmp);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (!emitter.isDisposed()) {
                        emitter.onError(e);
                    }
                }
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}
