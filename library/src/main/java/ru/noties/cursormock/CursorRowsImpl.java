package ru.noties.cursormock;

import android.support.annotation.NonNull;

class CursorRowsImpl implements CursorRows {

    private final Object[][] mData;

    CursorRowsImpl(@NonNull Object[][] data) {
        mData = data;
    }

    @Override
    public int count() {
        return mData.length;
    }

    @Override
    public <T> T get(Class<T> cl, int row, int column) {
        //noinspection unchecked
        return (T) mData[row][column];
    }
}
