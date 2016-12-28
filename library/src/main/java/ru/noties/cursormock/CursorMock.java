package ru.noties.cursormock;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * This class is intended to be an easy way to `mock` {@link Cursor}. The main usage
 * is testing, but it can be used in production code (no external dependencies).
 * Mocking {@link Cursor} helps testing as it doesn't require any real SQLite database,
 * managing database connection, etc. It works great with Robolectric (and of cause on-device testing).
 *
 * CursorMock implements all basic {@link Cursor} methods, that are used to read the data. It's
 * implementation _should_ be pretty close to Android system {@link Cursor} behaviour.
 *
 * In order to start using CursorMock, one must have a {@link CursorSchema} and (optional)
 * {@link CursorRows}. But there is an easy heads-up way: using {@link CursorMockBuilder} that
 * _hides_ those two concepts behind a simple builder interface.
 *
 * CursorMock also implements {@link Iterable}, so it can be used in for-loop:
 *  `for (CursorMock cursor: mock) {}`
 * Please note, that there is no need to call `moveToFirst` and/or check if cursor has rows, iterator
 * will call `moveToNext` automatically. (Thus calling `moveToFirst` before iteration will skip the first element).
 * Also note that CursorMock instance returned by iterator is the same as the calling one. All
 * iterator does is simply moving current row index further. After iteration CursorMock.isAfterLast()
 * will return `true`, so if one need to iterate again (or to do other operations) make sure that
 * cursor is moved to required position.
 *
 * Please note, that these methods are not implemented in CursorMock and if called will throw an
 * exception:
 *  * void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer)
 *  * void deactivate()
 *  * boolean requery()
 *  * void registerContentObserver(ContentObserver contentObserver)
 *  * void unregisterContentObserver(ContentObserver contentObserver)
 *  * void setNotificationUri(ContentResolver contentResolver, Uri uri)
 *  * Uri getNotificationUri()
 *  * boolean getWantsAllOnMoveCalls()
 *  * void setExtras(Bundle bundle)
 *  * Bundle getExtras()
 *  * Bundle respond(Bundle bundle)
 * If any of these methods are needed, one can extend the CursorMock and implement them
 *
 * @see CursorMockBuilder
 * @see CursorSchema
 * @see CursorRows
 */
@SuppressWarnings("WeakerAccess")
public class CursorMock implements Cursor, Iterable<CursorMock> {

    private final CursorSchema mCursorSchema;
    private final CursorRows mCursorRows;

    private Set<DataSetObserver> mDataSetObservers;

    private int mIndex = -1;

    private boolean mIsClosed;

    CursorMock(@NonNull CursorSchema cursorSchema) {
        this(cursorSchema, CursorRows.EMPTY);
    }

    CursorMock(@NonNull CursorSchema cursorSchema, @NonNull CursorRows cursorRows) {
        mCursorSchema = cursorSchema;
        mCursorRows = cursorRows;
    }

    @Override
    public int getCount() {
        return mCursorRows.count();
    }

    @Override
    public int getPosition() {
        return mIndex;
    }

    @Override
    public boolean move(int i) {
        return moveToPosition(mIndex + i);
    }

    @Override
    public boolean moveToPosition(int i) {

        checkState();

        final boolean result;

        final int count = getCount();
        if (i >= count) {
            mIndex = count;
            result = false;
        } else if (i < 0) {
            mIndex = -1;
            result = false;
        } else if (i == mIndex) {
            result = true;
        } else {
            mIndex = i;
            result = true;
        }

        return result;
    }

    @Override
    public boolean moveToFirst() {
        return moveToPosition(0);
    }

    @Override
    public boolean moveToLast() {
        return moveToPosition(getCount() - 1);
    }

    @Override
    public boolean moveToNext() {
        return moveToPosition(mIndex + 1);
    }

    @Override
    public boolean moveToPrevious() {
        return moveToPosition(mIndex - 1);
    }

    @Override
    public boolean isFirst() {
        return mIndex == 0 && getCount() > 0;
    }

    @Override
    public boolean isLast() {
        final int count = getCount();
        return count > 0 && mIndex == (count - 1);
    }

    @Override
    public boolean isBeforeFirst() {
        return getCount() == 0 || mIndex == -1;
    }

    @Override
    public boolean isAfterLast() {
        final int count = getCount();
        return count == 0 || mIndex == count;
    }

    @Override
    public int getColumnIndex(String s) {
        return mCursorSchema.columnIndex(s);
    }

    @Override
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        final int index = getColumnIndex(s);
        if (index < 0) {
            throw new IllegalArgumentException("Cannot find a `" + s + "` column");
        }
        return index;
    }

    @Override
    public String getColumnName(int i) {
        return mCursorSchema.columnName(i);
    }

    @Override
    public String[] getColumnNames() {
        return mCursorSchema.columnNames();
    }

    @Override
    public int getColumnCount() {
        return mCursorSchema.columnCount();
    }

    @Override
    public byte[] getBlob(int i) {

        checkPosition();

        return mCursorRows.get(byte[].class, mIndex, i);
    }

    @Override
    public String getString(int i) {

        checkPosition();

        return mCursorRows.get(String.class, mIndex, i);
    }

    @Override
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        throw notImplemented("copyStringToBuffer(int, CharArrayBuffer)");
    }

    @Override
    public short getShort(int i) {

        checkPosition();

        final short result;

        final Number number = mCursorRows.get(Number.class, mIndex, i);
        if (number == null) {
            result = 0;
        } else {
            result = number.shortValue();
        }

        return result;
    }

    @Override
    public int getInt(int i) {

        checkPosition();

        final int result;

        final Number number = mCursorRows.get(Number.class, mIndex, i);
        if (number == null) {
            result = 0;
        } else {
            result = number.intValue();
        }

        return result;
    }

    @Override
    public long getLong(int i) {

        checkPosition();

        final long result;

        final Number number = mCursorRows.get(Number.class, mIndex, i);
        if (number == null) {
            result = 0L;
        } else {
            result = number.longValue();
        }

        return result;
    }

    @Override
    public float getFloat(int i) {

        checkPosition();

        final float result;

        final Number number = mCursorRows.get(Number.class, mIndex, i);
        if (number == null) {
            result = .0F;
        } else {
            result = number.floatValue();
        }

        return result;
    }

    @Override
    public double getDouble(int i) {

        checkPosition();

        final double result;

        final Number number = mCursorRows.get(Number.class, mIndex, i);
        if (number == null) {
            result = .0D;
        } else {
            result = number.doubleValue();
        }

        return result;
    }

    @Override
    public int getType(int i) {

        checkPosition();

        // okay, here we are going to ask CursorRows if it's NULL
        // if yes -> return NULL, else just return type from CursorSchema
        final int type;
        if (isNull(i)) {
            type = Cursor.FIELD_TYPE_NULL;
        } else {
            final ColumnType columnType = mCursorSchema.columnType(i);
            type = columnType == null
                    ? Cursor.FIELD_TYPE_NULL
                    : columnType.value;
        }

        return type;
    }

    @Override
    public boolean isNull(int i) {

        checkPosition();

        return mCursorRows.get(Object.class, mIndex, i) == null;
    }

    @Override
    @Deprecated
    public void deactivate() {
        throw notImplemented("deactivate()");
    }

    @Override
    @Deprecated
    public boolean requery() {
        throw notImplemented("requery()");
    }

    @Override
    public void close() {
        mIsClosed = true;
        if (mDataSetObservers != null) {
            for (DataSetObserver dataSetObserver: mDataSetObservers) {
                dataSetObserver.onInvalidated();
            }
        }
    }

    @Override
    public boolean isClosed() {
        return mIsClosed;
    }

    @Override
    public void registerContentObserver(ContentObserver contentObserver) {
        throw notImplemented("registerContentObserver(ContentObserver)");
    }

    @Override
    public void unregisterContentObserver(ContentObserver contentObserver) {
        throw notImplemented("unregisterContentObserver(ContentObserver)");
    }

    // will be call only `onInvalidated`
    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        if (dataSetObserver == null) {
            throw null;
        }

        if (mDataSetObservers == null) {
            mDataSetObservers = new HashSet<>(3);
        }

        mDataSetObservers.add(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        if (dataSetObserver == null) {
            throw null;
        }

        if (mDataSetObservers != null) {
            mDataSetObservers.remove(dataSetObserver);
        }
    }

    @Override
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        throw notImplemented("setNotificationUri(ContentResolver, Uri uri");
    }

    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public Uri getNotificationUri() {
        throw notImplemented("getNotificationUri()");
    }

    @Override
    public boolean getWantsAllOnMoveCalls() {
        throw notImplemented("getWantsAllOnMoveCalls()");
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void setExtras(Bundle bundle) {
        throw notImplemented("setExtras(Bundle)");
    }

    @Override
    public Bundle getExtras() {
        throw notImplemented("getExtras()");
    }

    @Override
    public Bundle respond(Bundle bundle) {
        throw notImplemented("respond(Bundle)");
    }
    
    private void checkState() throws IllegalStateException {
        if (mIsClosed) {
            throw new IllegalStateException("Cursor already closed");
        }
    }

    private void checkPosition() throws CursorIndexOutOfBoundsException {
        if (mIndex == -1 || mIndex >= getCount()) {
            throw new CursorIndexOutOfBoundsException(mIndex, getCount());
        }
    }

    @Override
    public Iterator<CursorMock> iterator() {
        checkState();
        return new CursorIterator();
    }

    private class CursorIterator implements Iterator<CursorMock> {

        @Override
        public boolean hasNext() {
            return moveToNext();
        }

        @Override
        public CursorMock next() {
            return CursorMock.this;
        }
    }

    private static IllegalStateException notImplemented(String methodName) {
        return new IllegalStateException(String.format("Method `%s` is not implemented in CursorMock", methodName));
    }
}
