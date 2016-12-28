package ru.noties.cursormock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ColumnTypeUtilsTest {

    @Test
    public void testInt() {

        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(short.class));
        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(int.class));
        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(long.class));

        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(Short.class));
        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(Integer.class));
        assertEquals(ColumnType.INT, ColumnTypeUtils.columnType(Long.class));
    }

    @Test
    public void testFloat() {

        assertEquals(ColumnType.FLOAT, ColumnTypeUtils.columnType(float.class));
        assertEquals(ColumnType.FLOAT, ColumnTypeUtils.columnType(double.class));

        assertEquals(ColumnType.FLOAT, ColumnTypeUtils.columnType(Float.class));
        assertEquals(ColumnType.FLOAT, ColumnTypeUtils.columnType(Double.class));
    }

    @Test
    public void testText() {
        assertEquals(ColumnType.TEXT, ColumnTypeUtils.columnType(String.class));
    }

    @Test
    public void testBlob() {
        assertEquals(ColumnType.BLOB, ColumnTypeUtils.columnType(byte[].class));
    }

    @Test
    public void testWrong() {

        assertWrongTypes(
                Object.class,
                Object[].class,
                byte.class,
                Byte.class,
                Byte[].class,
                boolean.class,
                Boolean.class,
                Date.class
        );
    }

    private static void assertWrongTypes(Class<?>... classes) {
        for (Class<?> cl: classes) {
            try {
                ColumnTypeUtils.columnType(cl);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                assertTrue(true);
            }
        }
    }
}
