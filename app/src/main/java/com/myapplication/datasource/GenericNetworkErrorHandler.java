package com.myapplication.datasource;

import android.util.Log;

import io.reactivex.functions.Consumer;

/**
 * Created by syang on 10/5/2017.
 */

public class GenericNetworkErrorHandler<T extends Throwable> implements Consumer<T> {
    private static final String tag = GenericNetworkErrorHandler.class.getSimpleName();
    public void accept(T data) {
        Log.d(tag, data.getLocalizedMessage());
    }
}
