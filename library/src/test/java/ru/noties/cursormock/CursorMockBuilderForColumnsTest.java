package ru.noties.cursormock;

import android.database.Cursor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorMockBuilderForColumnsTest {

    @Test
    public void testNoTypeDetected() {

        final CursorMock mock = CursorMockBuilder.forColumns("id")
                .addRow()
                .addRow()
                .addRow()
                .build();

        assertTrue(mock.moveToFirst());
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.moveToNext());
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.moveToNext());
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);

        assertFalse(mock.moveToNext());
        assertTrue(mock.isAfterLast());
    }

    @Test
    public void testTypeDetected() {

        final CursorMock mock = CursorMockBuilder.forColumns("id")
                .addRow()
                .addRow(1L)
                .build();

        assertTrue(mock.moveToFirst());
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_NULL);
        assertTrue(mock.moveToNext());
        assertTrue(mock.getType(0) == Cursor.FIELD_TYPE_INTEGER);
    }

    @Test
    public void typeDetectedAndFailOnWrong() {

        try {
            CursorMockBuilder.forColumns("raw")
                    .addRow()
                    .addRow(1L)
                    .addRow("Just a string here")
                    .build();
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
