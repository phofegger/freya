package com.samus.freya.model;

import android.util.SparseArray;
import android.widget.BaseAdapter;

/**
 * Created by samus on 03.01.2017.
 * Costum Adapter class to improve visibility and quality of code
 * Old code used lists and finding a specific object was bothersome
 */

public abstract class SparseArrayAdapter<E> extends BaseAdapter {

    private SparseArray<E> mData;
    public void setData(SparseArray<E> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public E getItem(int position) {
        return mData.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.keyAt(position);
    }
}