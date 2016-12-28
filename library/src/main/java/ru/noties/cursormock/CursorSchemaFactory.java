package ru.noties.cursormock;

import android.support.annotation.NonNull;

@SuppressWarnings("WeakerAccess")
public class CursorSchemaFactory {

    /**
     * @param firstColumn a non-null value for the first column name
     * @param otherColumns optional other columnNames
     * @return an instance of {@link CursorSchema} that also implements {@link CursorSchemaMutable}
     *
     * @see CursorMockBuilder#forColumns(String, String...)
     */
    public static CursorSchema raw(@NonNull String firstColumn, String... otherColumns) {
        final String[] columns;
        final int otherColumnsLength = otherColumns != null ? otherColumns.length : 0;
        if (otherColumnsLength > 0) {
            columns = new String[otherColumnsLength + 1];
            columns[0] = firstColumn;
            System.arraycopy(otherColumns, 0, columns, 1, otherColumnsLength);
        } else {
            columns = new String[] { firstColumn };
        }
        return new CursorSchemaImpl(columns, new ColumnType[columns.length]);
    }

    private CursorSchemaFactory() {}
}
