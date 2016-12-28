package ru.noties.cursormock;

import android.support.annotation.Nullable;

/**
 * Schema information to be used in {@link CursorMock}.
 * Contains basic information about columns (type, name, etc).
 *
 * A simple instance can be build by {@link CursorSchemaFactory#raw(String, String...)}
 *
 * @see CursorSchemaFactory
 *
 * @see CursorMock
 * @see CursorMockBuilder
 * @see CursorSchemaMutable
 */
@SuppressWarnings("WeakerAccess")
public interface CursorSchema {

    /**
     * @param columnName column name to be get information about
     * @return index of column or `-1` if no column with such a name present in this schema
     */
    int columnIndex(String columnName);

    /**
     * @param columnIndex to get column info
     * @return column name of a column positioned at specified index, or null if specified index
     *          is out of this schema bounds
     */
    @Nullable
    String columnName(int columnIndex);

    /**
     * @return an array of underlying column names
     */
    String[] columnNames();

    /**
     * @return length of underlying columns
     */
    int columnCount();

    /**
     *
     * @param columnIndex column index to get column info at
     * @return {@link ColumnType} or null if no type information is present
     *
     * @see CursorSchemaMutable
     * @see CursorRowsBuilder#addRow(Object...)
     */
    @Nullable
    ColumnType columnType(int columnIndex);
}
