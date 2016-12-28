package ru.noties.cursormock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorSchemaFactoryTest {

    @Test
    public void testRawArgumentsSingle() {
        final CursorSchema schema = CursorSchemaFactory.raw("id");
        assertTrue(schema.columnCount() == 1);
        assertTrue(schema.columnNames().length == 1);
        assertEquals("id", schema.columnNames()[0]);
        assertTrue(schema.columnType(0) == null);
    }

    @Test
    public void testRawArgumentsWithOthers() {
        final CursorSchema schema = CursorSchemaFactory.raw("id", "name", "time");
        assertTrue(schema.columnCount() == 3);
        assertTrue(schema.columnNames().length == 3);
        assertArrayEquals(new String[] { "id", "name", "time" }, schema.columnNames());
        assertTrue(schema.columnType(0) == null);
        assertTrue(schema.columnType(1) == null);
        assertTrue(schema.columnType(2) == null);
    }
}
