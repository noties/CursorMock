package ru.noties.cursormock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorSchemaImplTest {

    @Test
    public void testDifferentLengths() {
        try {
            new CursorSchemaImpl(new String[2], new ColumnType[3]);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testColumnIndex() {

        final CursorSchema schema
                = new CursorSchemaImpl(new String[] { "id", "text" }, new ColumnType[2]);

        assertEquals(2, schema.columnCount());

        assertEquals(0, schema.columnIndex("id"));
        assertEquals(1, schema.columnIndex("text"));

        assertEquals(-1, schema.columnIndex("not present"));
        assertEquals(-1, schema.columnIndex(null));
    }

    @Test
    public void testColumnName() {
        final CursorSchema schema = new CursorSchemaImpl(new String[] { "id", "text" }, new ColumnType[2]);
        assertEquals("id", schema.columnName(0));
        assertEquals("text", schema.columnName(1));
    }

    @Test
    public void testColumnNameNotPresent() {
        final CursorSchema schema = new CursorSchemaImpl(new String[] {"id"}, new ColumnType[1]);
        assertNull(schema.columnName(1));
        assertNull(schema.columnName(-1));
        assertNull(schema.columnName(100));
    }

    @Test
    public void testColumnNames() {
        final CursorSchema schema = new CursorSchemaImpl(new String[] { "id", "text" }, new ColumnType[2]);
        assertArrayEquals(new String[] { "id", "text" }, schema.columnNames());
    }

    @Test
    public void testColumnType() {
        final CursorSchema schema = new CursorSchemaImpl(new String[2], new ColumnType[] { ColumnType.INT, ColumnType.BLOB });
        assertEquals(ColumnType.INT, schema.columnType(0));
        assertEquals(ColumnType.BLOB, schema.columnType(1));
    }
}
