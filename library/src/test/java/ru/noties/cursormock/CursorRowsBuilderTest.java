package ru.noties.cursormock;

import android.support.annotation.Nullable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CursorRowsBuilderTest {

    @Test
    public void testNoRows() {
        final CursorRows rows = new CursorRowsBuilder(new CursorSchemaNoOp())
                .build();
        assertTrue(rows.count() == 0);
    }

    @Test
    public void testValuesLengthGreater() {
        try {
            new CursorRowsBuilder(new CursorSchemaNoOp())
                    .addRow(1);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testColumnTypeDifferent() {
        try {
            new CursorRowsBuilder(new CursorSchemaNoOp() {
                @Override
                public int columnCount() {
                    return 1;
                }

                @Override
                public ColumnType columnType(int columnIndex) {
                    return ColumnType.TEXT;
                }
            }).addRow(34L);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testEmptyRow() {

        final CursorSchema schema = new CursorSchema() {
            @Override
            public int columnIndex(String columnName) {
                return 0;
            }

            @Nullable
            @Override
            public String columnName(int columnIndex) {
                return "id";
            }

            @Override
            public String[] columnNames() {
                return new String[] { "id" };
            }

            @Override
            public int columnCount() {
                return 1;
            }

            @Override
            public ColumnType columnType(int columnIndex) {
                return ColumnType.TEXT;
            }
        };

        final CursorRows rows = new CursorRowsBuilder(schema)
                .addRow()
                .build();
        assertTrue(rows.count() == 1);
        assertTrue(rows.get(Object.class, 0, 0) == null);
    }

    @Test
    public void testMultipleRows() {
        final CursorSchema schema = new CursorSchema() {
            @Override
            public int columnIndex(String columnName) {
                return "id".equals(columnName)
                        ? 0
                        : 1;
            }

            @Nullable
            @Override
            public String columnName(int columnIndex) {
                return columnIndex == 0
                        ? "id"
                        : "name";
            }

            @Override
            public String[] columnNames() {
                return new String[] { "id", "name" };
            }

            @Override
            public int columnCount() {
                return 2;
            }

            @Override
            public ColumnType columnType(int columnIndex) {
                return columnIndex == 0
                        ? ColumnType.INT
                        : ColumnType.TEXT;
            }
        };

        final CursorRows rows = new CursorRowsBuilder(schema)
                .addRow() // first empty row
                .addRow(12L)
                .addRow(null, "name_value")
                .addRow(14L, "14L")
                .build();

        assertTrue(rows.count() == 4);
        assertRow(rows, 0, null, null);
        assertRow(rows, 1, 12L, null);
        assertRow(rows, 2, null, "name_value");
        assertRow(rows, 3, 14L, "14L");
    }

    private static void assertRow(CursorRows rows, int row, Object... values) {
        Object rowValue;
        for (int i = 0, length = values.length; i < length; i++) {
            rowValue = rows.get(Object.class, row, i);
            assertTrue(values[i] == null ? rowValue == null : values[i].equals(rows.get(Object.class, row, i)));
        }
    }

    private static class CursorSchemaNoOp implements CursorSchema {

        @Override
        public int columnIndex(String columnName) {
            return 0;
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
