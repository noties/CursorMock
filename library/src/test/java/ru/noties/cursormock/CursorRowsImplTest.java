package ru.noties.cursormock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorRowsImplTest {

    @Test
    public void testCount() {
        final CursorRows rows = new CursorRowsImpl(new Object[5][]);
        assertEquals(5, rows.count());
    }

    @Test
    public void testGet() {
        final Object[][] data = new Object[][] {
                { 13L, "hello", null, new byte[1], 44.F }
        };
        final CursorRows rows = new CursorRowsImpl(data);
        assertEquals(1, rows.count());

        assertEquals(13L, (long) rows.get(Long.class, 0, 0));
        assertEquals("hello", rows.get(String.class, 0, 1));
        assertEquals(null, rows.get(Object.class, 0, 2));
        assertArrayEquals(new byte[1], rows.get(byte[].class, 0, 3));
    }
}
