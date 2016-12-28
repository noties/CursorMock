package ru.noties.cursormock;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorMockTest {

    @Test
    public void testNotImplementedMethods() {

        final CursorMock mock = new CursorMock(new SchemaNoOp());

        assertNotImplemented(mock, "copyStringToBuffer", toArray(int.class, CharArrayBuffer.class), toArray(0, null));
        assertNotImplemented(mock, "deactivate", null, null);
        assertNotImplemented(mock, "requery", null, null);
        assertNotImplemented(mock, "registerContentObserver", toArray(ContentObserver.class), new Object[1]);
        assertNotImplemented(mock, "unregisterContentObserver", toArray(ContentObserver.class), new Object[1]);
        assertNotImplemented(mock, "setNotificationUri", toArray(ContentResolver.class, Uri.class), toArray(null, null));
        assertNotImplemented(mock, "getNotificationUri", null, null);
        assertNotImplemented(mock, "getWantsAllOnMoveCalls", null, null);
        assertNotImplemented(mock, "setExtras", toArray(Bundle.class), new Object[1]);
        assertNotImplemented(mock, "getExtras", null, null);
        assertNotImplemented(mock, "respond", toArray(Bundle.class), new Object[1]);
    }

    @Test
    public void testEmpty() {

        final CursorMock mock = new CursorMock(new SchemaNoOp());

        assertFalse(mock.isClosed());

        assertTrue(mock.getPosition() == -1);
        assertTrue(mock.getCount() == 0);

        assertFalse(mock.moveToPosition(-1));
        assertFalse(mock.moveToPosition(0));
        assertFalse(mock.moveToPosition(1));
        assertFalse(mock.isFirst());
        assertFalse(mock.isLast());

        assertTrue(mock.isBeforeFirst());
        assertTrue(mock.isAfterLast());

        assertTrue(mock.getColumnIndex("") == -1);
        assertTrue(mock.getColumnIndex("whatever") == -1);
        assertTrue(mock.getColumnIndex(null) == -1);

        try {
            mock.getColumnIndexOrThrow("throw");
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        assertTrue(mock.getColumnCount() == 0);
        assertTrue(mock.getColumnNames().length == 0);

        assertGet(true, mock, "getBlob", 0);
        assertGet(true, mock, "getString", 0);
        assertGet(true, mock, "getShort", 0);
        assertGet(true, mock, "getInt", 0);
        assertGet(true, mock, "getLong", 0);
        assertGet(true, mock, "getFloat", 0);
        assertGet(true, mock, "getDouble", 0);

        assertGet(true, mock, "getType", 0);
        assertGet(true, mock, "isNull", 0);
    }

    @Test
    public void testObservers() {

        class Observer extends DataSetObserver {
            boolean invalidated;
        };
        final Observer observer = new Observer() {
            @Override
            public void onInvalidated() {
                invalidated = true;
            }
        };

        final CursorMock mock = new CursorMock(new SchemaNoOp());

        mock.registerDataSetObserver(observer);
        mock.close();
        assertTrue(observer.invalidated);

        observer.invalidated = false;
        mock.unregisterDataSetObserver(observer);
        mock.close();

        assertFalse(observer.invalidated);
    }

    @Test
    public void testOneRow() {

        final CursorMock mock = CursorMockBuilder.forColumns("id", "text")
                .addRow(1L, "hello test")
                .build();

        assertTrue(mock.getCount() == 1);
        assertTrue(mock.getColumnCount() == 2);
        assertTrue(mock.isBeforeFirst());

        assertFalse(mock.isAfterLast());

        assertTrue(mock.getColumnIndex("id") == 0);
        assertTrue(mock.getColumnIndex("text") == 1);
        assertTrue(mock.getColumnIndexOrThrow("id") == 0);
        assertTrue(mock.getColumnIndexOrThrow("text") == 1);

        assertTrue(mock.moveToFirst());
        assertFalse(mock.isNull(0));
        assertFalse(mock.isNull(1));
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_INTEGER);
        assertTrue(mock.getType(1) == Cursor.FIELD_TYPE_STRING);

        assertEquals(1L, mock.getLong(0));
        assertEquals("hello test", mock.getString(1));

        assertTrue(mock.getColumnIndex("id") == 0);
        assertTrue(mock.getColumnIndex("text") == 1);
        assertTrue(mock.getColumnIndexOrThrow("id") == 0);
        assertTrue(mock.getColumnIndexOrThrow("text") == 1);

        assertFalse(mock.moveToNext());

        assertTrue(mock.isAfterLast());
    }

    @Test
    public void testEmptyIterator() {
        for (CursorMock mock: new CursorMock(new SchemaNoOp())) {
            assertTrue(false);
        }
    }

    @Test
    public void testIteratorAtLastPosition() {
        final CursorMock mock = CursorMockBuilder.forColumns("id")
                .addRow()
                .build();
        mock.moveToLast();
        for (CursorMock cursorMock: mock) {
            assertTrue(false);
        }
    }

    @Test
    public void testManyRows() {

        final CursorMock mock = CursorMockBuilder.forColumns("id", "text", "time")
                .addRow(1L, "text1")
                .addRow(null, "text2", 123.F)
                .addRow()
                .addRow(null, null, 321.F)
                .build();

        assertTrue(mock.getCount() == 4);
        assertTrue(mock.isBeforeFirst());

        assertFalse(mock.isAfterLast());

        int iterations = 0;

        for (CursorMock cursor: mock) {

            assertTrue(cursor.getColumnCount() == 3);

            assertTrue(cursor.getColumnIndex("id") == 0);
            assertTrue(cursor.getColumnIndex("text") == 1);
            assertTrue(cursor.getColumnIndex("time") == 2);

            assertTrue(cursor.getColumnIndexOrThrow("id") == 0);
            assertTrue(cursor.getColumnIndexOrThrow("text") == 1);
            assertTrue(cursor.getColumnIndexOrThrow("time") == 2);

            iterations += 1;
        }

        assertEquals(4, iterations);

        assertTrue(mock.isAfterLast());
        assertTrue(mock.moveToFirst());

        // first row
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_INTEGER);
        assertTrue(mock.getType(1) == Cursor.FIELD_TYPE_STRING);
        assertTrue(mock.getType(2) == Cursor.FIELD_TYPE_NULL);
        assertEquals(mock.getLong(0), 1L);
        assertEquals(mock.getString(1), "text1");
        assertEquals(mock.getFloat(2), .0F, .0001F);

        // second row
        assertTrue(mock.moveToNext());

        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.getType(1) == Cursor.FIELD_TYPE_STRING);
        assertTrue(mock.getType(2) == Cursor.FIELD_TYPE_FLOAT);
        assertEquals(mock.getLong(0), 0L);
        assertEquals(mock.getString(1), "text2");
        assertEquals(mock.getFloat(2), 123.F, .0001F);

        // third row (all nulls)
        assertTrue(mock.moveToNext());

        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.getType(1) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.getType(2) == Cursor.FIELD_TYPE_NULL);
        assertEquals(mock.getLong(0), 0L);
        assertEquals(mock.getString(1), null);
        assertEquals(mock.getFloat(2), .0F, .0001F);

        // forth row
        assertTrue(mock.moveToNext());

        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.getType(1) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.getType(2) == Cursor.FIELD_TYPE_FLOAT);
        assertEquals(mock.getLong(0), 0L);
        assertEquals(mock.getString(1), null);
        assertEquals(mock.getFloat(2), 321.F, .0001F);

        assertFalse(mock.moveToNext());
        assertTrue(mock.isAfterLast());
    }

    @Test
    public void testClosedIterator() {
        final CursorMock mock = new CursorMock(new SchemaNoOp());
        assertFalse(mock.isClosed());
        mock.close();
        assertTrue(mock.isClosed());

        try {
            for (CursorMock cursorMock: mock);
            assertTrue(false);
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }

    // asserts calls to `methodName` throws/not-throws
    private static void assertGet(boolean shouldThrow, CursorMock mock, String methodName, int column) {
        try {
            final Method method = CursorMock.class.getMethod(methodName, int.class);
            try {
                method.invoke(mock, column);
                assertTrue(!shouldThrow);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null && e.getCause() instanceof CursorIndexOutOfBoundsException) {
                    assertTrue(shouldThrow);
                } else {
                    throw new RuntimeException(e);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertNotImplemented(CursorMock mock, String methodName, Class<?>[] parameters, Object[] args) {
        try {
            final Method method = CursorMock.class.getDeclaredMethod(methodName, parameters);
            try {
                method.invoke(mock, args);
                assertTrue(false);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null && e.getCause() instanceof IllegalStateException) {
                    assertTrue(true);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SafeVarargs
    private static <T> T[] toArray(T... args) {
        return args;
    }

    private static class SchemaNoOp implements CursorSchema {

        @Override
        public int columnIndex(String columnName) {
            return -1;
        }

        @Nullable
        @Override
        public String columnName(int columnIndex) {
            return null;
        }

        @Override
        public String[] columnNames() {
            return new String[0];
        }

        @Override
        public int columnCount() {
            return 0;
        }

        @Nullable
        @Override
        public ColumnType columnType(int columnIndex) {
            return null;
        }
    }
}