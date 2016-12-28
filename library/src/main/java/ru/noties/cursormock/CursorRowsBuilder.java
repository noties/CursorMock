package ru.noties.cursormock;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A builder to create a {@link CursorRows} instance
 *
 * @see CursorRows
 * @see CursorSchema
 */
@SuppressWarnings("WeakerAccess")
public class CursorRowsBuilder {

    private final CursorSchema mCursorSchema;
    private final int mColumnCount;
    private final List<Object[]> mRows;
    private final boolean mIsSchemaMutable;

    /**
     * This builder detects type of a column based on {@link CursorSchema}. If it contains
     * type information in {@link CursorSchema#columnType(int)} than added data will be
     * validated against it.
     *
     * If supplied {@link CursorSchema} implements {@link CursorSchemaMutable} then if a column
     * doesn't have a type information it will be set via {@link CursorSchemaMutable#columnType(int, ColumnType)}
     *
     * @param cursorSchema an instance of {@link CursorSchema}
     *
     * @see CursorSchema
     * @see CursorSchemaMutable
     */
    public CursorRowsBuilder(@NonNull CursorSchema cursorSchema) {
        mCursorSchema = cursorSchema;
        mColumnCount = cursorSchema.columnCount();
        mRows = new ArrayList<>(3);
        mIsSchemaMutable = cursorSchema instanceof CursorSchemaMutable;
    }

    /**
     *
     * Method to add new rows.
     *
     * Can be called without arguments:
     * <code>
     *     .addRow();
     * </code>
     * If called without arguments will insert a new row with all values as NULL.
     *
     * Supplied arguments do not have to match the {@link #mColumnCount} exactly, it can be
     * less. If supplied arguments length is greater than {@link #mColumnCount} then
     * exception will be thrown.
     * {@code
     *      final CursorRowsBuilder builder = CursorMockBuilder.fromColumns("id", "name", "time");
     *      builder.add(1L); // will add a row [1L, null, null]
     *      builder.add(2L, "A Name"); // will add a row [2L, "A Name", null]
     *      builder.add(null, null, "time"); // will add a row [null, null, "time]
     * }
     *
     * If {@link #mCursorSchema} returns null from a {@link CursorSchema#columnType(int)} and
     * it implements a {@link CursorSchemaMutable} then type will be taken and set based on
     * supplied value {@link CursorSchemaMutable#columnType(int, ColumnType)}
     * {@code
     *      final CursorRowsBuilder builder = CursorMockBuilder.fromColumns("id", "name", "time");
     *      builder.addRow(1L); // will set the first column to be of type {@link ColumnType#INT}
     *      builder.addRow(null, null, System.currentTimeMillis()); // will set the type of 3rd column to be {@link ColumnType#INT}
     *      // at this point type information is as follows: [INT, null, INT]
     *
     *       // this call will throw an exception as we have already set the type of the
     *       // first column to be of type INT
     *      builder.addRow("a string");
     * }
     *
     * @param args an array of objects to be inserted as a new row
     * @return instance to chain method calls
     * @throws IllegalArgumentException if supplied arguments array is greater in length
     *          than {@link #mColumnCount} obtained from {@link #mCursorSchema} {@link CursorSchema}
     */
    @SuppressLint("DefaultLocale")
    public CursorRowsBuilder addRow(Object... args) throws IllegalArgumentException {

        final int length = args != null ? args.length : 0;
        if (length == 0) {
            mRows.add(new Object[mColumnCount]);
        } else {

            if (length > mColumnCount) {
                throw new IllegalArgumentException(String.format("Supplied values greater than " +
                        "CursorScheme holds. Expected: %d, actual: %d", mColumnCount, length));
            }

            final Object[] values = new Object[mColumnCount];

            Object value;
            ColumnType type;

            for (int i = 0; i < length; i++) {
                value = args[i];
                if (value != null) {
                    type = ColumnTypeUtils.columnType(value.getClass());
                    //noinspection ConstantConditions
                    if (mIsSchemaMutable && mCursorSchema.columnType(i) == null) {
                        // if schema doesn't have columnType yet, we put it based on `value` class
                        // later checks will be performed on this type
                        ((CursorSchemaMutable) mCursorSchema).columnType(i, type);
                    } else if (mCursorSchema.columnType(i) != type) {
                        throw new IllegalArgumentException(String.format("Value `%s` at `%d` has" +
                                " type `%s`, expected: `%s`", value, i, ColumnTypeUtils.columnType(value.getClass()),
                                mCursorSchema.columnType(i)));
                    }
                }
                values[i] = value;
            }
            mRows.add(values);
        }
        return this;
    }

    /**
     * @return {@link CursorRows} based on values passed to {@link #addRow(Object...)}.
     *          If no rows were added {@link CursorRows#EMPTY} will be returned
     */
    public CursorRows build() {

        final CursorRows rows;
        if (mRows.size() == 0) {
            rows = CursorRows.EMPTY;
        } else {
            final Object[][] data = mRows.toArray(new Object[mRows.size()][]);
            rows = new CursorRowsImpl(data);
        }

        return rows;
    }
}
