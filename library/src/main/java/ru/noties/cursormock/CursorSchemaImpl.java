package ru.noties.cursormock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Arrays;

class CursorSchemaImpl implements CursorSchema, CursorSchemaMutable {

    private final String[] mColumnNames;
    private final ColumnType[] mColumnTypes;

    CursorSchemaImpl(@NonNull String[] columnNames, @NonNull ColumnType[] columnTypes) throws IllegalArgumentException {

        if (columnNames.length != columnTypes.length) {
            throw new IllegalArgumentException(String.format("ColumnNames & ColumnTypes arrays " +
                    "must have the same length. ColumnNames: %s, ColumnTypes: %s",
                    Arrays.toString(columnNames), Arrays.toString(columnTypes)));
        }

        mColumnNames = columnNames;
        mColumnTypes = columnTypes;
    }

    @Override
    public int columnIndex(String columnName) {
        int result = -1;
        for (int i = 0, count = columnCount(); i < count; i++) {
            if (mColumnNames[i].equalsIgnoreCase(columnName)) {
                result = i;
                break;
            }
        }
        return result;
    }

    @Nullable
    @Override
    public String columnName(int columnIndex) {
        final String out;
        if (columnIndex < 0 || columnIndex>= mColumnNames.length) {
            out = null;
        } else {
            out = mColumnNames[columnIndex];
        }
        return out;
    }

    @Override
    public String[] columnNames() {
        return mColumnNames.clone();
    }

    @Override
    public int columnCount() {
        return mColumnNames.length;
    }

    @Nullable
    @Override
    public ColumnType columnType(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= mColumnTypes.length) {
            throw new IllegalArgumentException("Specified column index is out of this schema bounds");
        }
        return mColumnTypes[columnIndex];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CursorSchemaImpl that = (CursorSchemaImpl) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(mColumnNames, that.mColumnNames)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(mColumnTypes, that.mColumnTypes);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(mColumnNames);
        result = 31 * result + Arrays.hashCode(mColumnTypes);
        return result;
    }

    @Override
    public void columnType(int columnIndex, @NonNull ColumnType columnType) {
        mColumnTypes[columnIndex] = columnType;
    }
}
