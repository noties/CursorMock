package ru.noties.cursormock;

/**
 * This class posses information about actual data that a {@link CursorMock} will have
 *
 * @see CursorRowsBuilder
 */
@SuppressWarnings("WeakerAccess")
public interface CursorRows {

    /**
     * Empty implementation that has no data
     */
    CursorRows EMPTY = new CursorRows() {
        @Override
        public int count() {
            return 0;
        }

        @Override
        public <T> T get(Class<T> cl, int row, int column) {
            return null;
        }
    };

    /**
     * @return a number of rows in this instance
     */
    int count();

    /**
     * @param cl a {@link Class} of the return value
     * @param row index of a row there value is positioned
     * @param column index of a column there value is positioned
     * @param <T> to cast the return value
     * @return an casted to T object positioned at columnIndex and rowIndex or null
     */
    <T> T get(Class<T> cl, int row, int column);
}
