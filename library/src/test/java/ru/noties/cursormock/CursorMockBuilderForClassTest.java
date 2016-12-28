package ru.noties.cursormock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorMockBuilderForClassTest {

    @Test
    public void testAllSupportedTypes() {
        final AllTypes allTypes = new AllTypes() {{
            shortValue = 1;
            intValue = 2;
            longValue = 3;
            floatValue = 4;
            doubleValue = 5;
            stringValue = "6";
            byteArrayValue = new byte[] { 1, 0, 1 };
        }};
        final CursorMock mock = CursorMockBuilder.forClass(AllTypes.class)
                .add(allTypes)
                .build();

        assertEquals(1, mock.getCount());
        assertEquals(7, mock.getColumnCount());

        try {
            mock.getColumnIndexOrThrow("shortValue");
            mock.getColumnIndexOrThrow("intValue");
            mock.getColumnIndexOrThrow("longValue");
            mock.getColumnIndexOrThrow("floatValue");
            mock.getColumnIndexOrThrow("doubleValue");
            mock.getColumnIndexOrThrow("stringValue");
            mock.getColumnIndexOrThrow("byteArrayValue");
            assertTrue(true);
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        }

        assertEquals(true, mock.moveToFirst());

        assertEquals((short) 1, mock.getShort(mock.getColumnIndex("shortValue")));
        assertEquals(2, mock.getInt(mock.getColumnIndex("intValue")));
        assertEquals(3L, mock.getLong(mock.getColumnIndex("longValue")));
        assertEquals(4.F, mock.getFloat(mock.getColumnIndex("floatValue")), .0005F);
        assertEquals(5.D, mock.getDouble(mock.getColumnIndex("doubleValue")), .0005D);
        assertEquals("6", mock.getString(mock.getColumnIndex("stringValue")));

        assertArrayEquals(new byte[] { 1, 0, 1 }, mock.getBlob(mock.getColumnIndex("byteArrayValue")));
    }

    @Test
    public void testAllSupportedTypesBoxed() {
        final AllTypesBoxed allTypesBoxed = new AllTypesBoxed() {{
            shortValue = 1;
            intValue = 2;
            longValue = 3L;
            floatValue = 4.F;
            doubleValue = 5.D;
        }};
        final CursorMock mock = CursorMockBuilder.forClass(AllTypesBoxed.class)
                .add(allTypesBoxed)
                .build();

        assertEquals(1, mock.getCount());
        assertEquals(5, mock.getColumnCount());

        try {
            mock.getColumnIndexOrThrow("shortValue");
            mock.getColumnIndexOrThrow("intValue");
            mock.getColumnIndexOrThrow("longValue");
            mock.getColumnIndexOrThrow("floatValue");
            mock.getColumnIndexOrThrow("doubleValue");
            assertTrue(true);
        } catch (IllegalArgumentException e) {
            assertTrue(false);
        }

        assertEquals(true, mock.moveToFirst());

        assertEquals((short) 1, mock.getShort(mock.getColumnIndex("shortValue")));
        assertEquals(2, mock.getInt(mock.getColumnIndex("intValue")));
        assertEquals(3L, mock.getLong(mock.getColumnIndex("longValue")));
        assertEquals(4.F, mock.getFloat(mock.getColumnIndex("floatValue")), .0005F);
        assertEquals(5.D, mock.getDouble(mock.getColumnIndex("doubleValue")), .0005D);
    }

    @Test
    public void testAllFieldsFiltered() {
        final CursorMockBuilder.ObjectProcessor ignoreAll = new CursorMockBuilder.ObjectProcessor() {
            @Override
            public boolean ignore(Field field) {
                return true;
            }

            @NonNull
            @Override
            public String columnName(Field field) {
                throw null;
            }

            @Nullable
            @Override
            public Object value(Field field, Object holder) {
                throw null;
            }
        };

        try {
            CursorMockBuilder.forClass(AllTypes.class, ignoreAll);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testNoFields() {
        try {
            CursorMockBuilder.forClass(Object.class);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testObjectProcessorImpl() {

        // test impl independently first, them test if it's get called by a builder
        final CursorMockBuilder.ObjectProcessorImpl impl = new CursorMockBuilder.ObjectProcessorImpl();
        try {

            // all types
            {
                final Class<?> cl = AllTypes.class;

                assertFalse(impl.ignore(cl.getDeclaredField("shortValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("intValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("longValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("floatValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("doubleValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("stringValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("byteArrayValue")));
            }

            // all types boxed
            {
                final Class<?> cl = AllTypesBoxed.class;
                assertFalse(impl.ignore(cl.getDeclaredField("shortValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("intValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("longValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("floatValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("doubleValue")));
            }

            // with ignored fields
            {
                final Class<?> cl = IgnoredFields.class;
                assertFalse(impl.ignore(cl.getDeclaredField("shortValue")));
                assertFalse(impl.ignore(cl.getDeclaredField("floatValue")));

                assertTrue(impl.ignore(cl.getDeclaredField("intValue")));
                assertTrue(impl.ignore(cl.getDeclaredField("longValue")));
            }

            // column names
            {
                final Class<?> cl = AllTypes.class;
                assertEquals("shortValue", impl.columnName(cl.getDeclaredField("shortValue")));
                assertEquals("intValue", impl.columnName(cl.getDeclaredField("intValue")));
                assertEquals("longValue", impl.columnName(cl.getDeclaredField("longValue")));
                assertEquals("floatValue", impl.columnName(cl.getDeclaredField("floatValue")));
                assertEquals("doubleValue", impl.columnName(cl.getDeclaredField("doubleValue")));
                assertEquals("stringValue", impl.columnName(cl.getDeclaredField("stringValue")));
                assertEquals("byteArrayValue", impl.columnName(cl.getDeclaredField("byteArrayValue")));
            }

            // values
            {
                final Class<?> cl = AllTypes.class;
                final AllTypes allTypes = new AllTypes() {{
                    shortValue = 1;
                    intValue = 2;
                    longValue = 3;
                    floatValue = 4;
                    doubleValue = 5;
                    stringValue = "6";
                    byteArrayValue = new byte[] { 7 };
                }};

                assertEquals((short) 1, impl.value(cl.getDeclaredField("shortValue"), allTypes));
                assertEquals(2, impl.value(cl.getDeclaredField("intValue"), allTypes));
                assertEquals(3L, impl.value(cl.getDeclaredField("longValue"), allTypes));
                assertEquals(4.F, impl.value(cl.getDeclaredField("floatValue"), allTypes));
                assertEquals(5.D, impl.value(cl.getDeclaredField("doubleValue"), allTypes));
                assertEquals("6", impl.value(cl.getDeclaredField("stringValue"), allTypes));

                assertArrayEquals(new byte[] { 7 }, (byte[]) impl.value(cl.getDeclaredField("byteArrayValue"), allTypes));
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Test
    public void testNotSupported() {

        // should not fail before calls to `add*`
        final CursorMockBuilder.ForClass<NotSupported> builder = CursorMockBuilder.forClass(NotSupported.class);

        // should not fail if NULL row values are added
        builder.add(null);

        try {
            // boolean
            builder.add(new NotSupported() {{ booleanValue = true; }});
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            builder.add(new NotSupported() {{ byteValue = 2; }});
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            builder.add(new NotSupported() {{ charValue = '3'; }});
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testNotSupportedWithSerialization() {

        final CursorMockBuilder.ObjectProcessor processor = new CursorMockBuilder.ObjectProcessorImpl() {
            @Nullable
            @Override
            public Object value(Field field, Object holder) {
                final Object value = super.value(field, holder);
                final Object out;
                if (value == null) {
                    out = null;
                } else if (value instanceof Boolean) {
                    out = ((Boolean) value) ? 1 : 0;
                } else if (value instanceof Number) {
                    out = ((Number) value).intValue();
                } else if (value instanceof Character) {
                    out = (int) ((Character) value);
                } else {
                    throw new RuntimeException("Unknown value type: " + value.getClass());
                }
                return out;
            }
        };

        final CursorMock mock = CursorMockBuilder.forClass(NotSupported.class, processor)
                .add(new NotSupported() {{ booleanValue = true; }})
                .add(new NotSupported() {{ byteValue = 2; }})
                .add(new NotSupported() {{ charValue = 3; }})
                .add(new NotSupported() {{ booleanValue = true; byteValue = 2; charValue = 3; }})
                .build();

        assertEquals(4, mock.getCount());
        assertEquals(3, mock.getColumnCount());

        final int booleanIndex = mock.getColumnIndexOrThrow("booleanValue");
        final int byteIndex = mock.getColumnIndexOrThrow("byteValue");
        final int charIndex = mock.getColumnIndexOrThrow("charValue");

        assertTrue(mock.moveToFirst());

        assertEquals(1, mock.getInt(booleanIndex));
        assertEquals(0, mock.getInt(byteIndex));
        assertEquals(0, mock.getInt(charIndex));

        assertTrue(mock.moveToNext());

        assertEquals(0, mock.getInt(booleanIndex));
        assertEquals(2, mock.getInt(byteIndex));
        assertEquals(0, mock.getInt(charIndex));

        assertTrue(mock.moveToNext());

        assertEquals(0, mock.getInt(booleanIndex));
        assertEquals(0, mock.getInt(byteIndex));
        assertEquals(3, mock.getInt(charIndex));

        assertTrue(mock.moveToNext());

        assertEquals(1, mock.getInt(booleanIndex));
        assertEquals(2, mock.getInt(byteIndex));
        assertEquals(3, mock.getInt(charIndex));
    }

    @Test
    public void testAddAllEmptyCollection() {

        // if collection is empty, then nothing should happen (no rows will be added)

        //noinspection unchecked
        final CursorMock mock = CursorMockBuilder.forClass(AllTypes.class)
                .addAll(Collections.EMPTY_LIST)
                .build();

        assertEquals(0, mock.getCount());
    }

    @Test
    public void testAddAllCollection() {

        final CursorMock mock = CursorMockBuilder.forClass(AllTypes.class)
                .addAll(Arrays.asList(new AllTypes(), new AllTypes()))
                .addAll(Collections.singleton(new AllTypes()))
                .build();

        assertEquals(3, mock.getCount());
    }

    private static class AllTypes {
        short shortValue;
        int intValue;
        long longValue;
        float floatValue;
        double doubleValue;
        String stringValue;
        byte[] byteArrayValue;
    }

    private static class AllTypesBoxed {
        Short shortValue;
        Integer intValue;
        Long longValue;
        Float floatValue;
        Double doubleValue;
    }

    private static class IgnoredFields {
        short shortValue;
        transient int intValue;
        static long longValue;
        Float floatValue;
    }

    private static class NotSupported {
        boolean booleanValue;
        byte byteValue;
        char charValue;
    }
}
