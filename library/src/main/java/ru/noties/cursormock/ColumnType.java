package ru.noties.cursormock;

import android.database.Cursor;

/**
 * An enum for supported by {@link Cursor} types.
 * It intentionally does not define NULL type.
 *
 * @see Cursor#FIELD_TYPE_INTEGER
 * @see Cursor#FIELD_TYPE_FLOAT
 * @see Cursor#FIELD_TYPE_STRING
 * @see Cursor#FIELD_TYPE_BLOB
 *
 * @see ColumnTypeUtils#columnType(Class)
 */
public enum ColumnType {

    INT     (Cursor.FIELD_TYPE_INTEGER)
    , FLOAT (Cursor.FIELD_TYPE_FLOAT)
    , TEXT  (Cursor.FIELD_TYPE_STRING)
    , BLOB  (Cursor.FIELD_TYPE_BLOB);

    final int value;

    ColumnType(int value) {
        this.value = value;
    }
}
