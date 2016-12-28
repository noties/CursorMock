package ru.noties.cursormock;

import android.support.annotation.NonNull;

/**
 * An interface that allows to change to type of a column. The main usage in
 * {@link CursorRowsBuilder} to detect type of a column based on supplied values
 */
@SuppressWarnings("WeakerAccess")
public interface CursorSchemaMutable extends CursorSchema {
    /**
     * @param columnIndex column index at which to change the column type
     * @param columnType to set the info about column type
     */
    void columnType(int columnIndex, @NonNull ColumnType columnType);
}
