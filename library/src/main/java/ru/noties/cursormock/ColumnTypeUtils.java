package ru.noties.cursormock;

class ColumnTypeUtils {

    /**
     *
     * Supported types are:
     *  short, Short -> INT
     *  int, Integer -> INT
     *  long, Long -> INT
     *  float, Float -> FLOAT
     *  double, Double -> FLOAT
     *  String -> TEXT
     *  byte[] -> BLOB
     *
     * All other types will throw an exception
     *
     * @param type a class to convert type
     * @return {@link ColumnType} for the specified class
     * @throws IllegalArgumentException if type is not natively supported
     */
    static ColumnType columnType(Class<?> type) throws IllegalArgumentException {

        final ColumnType columnType;

        if (Short.TYPE.equals(type)
                || Short.class.equals(type)
                || Integer.TYPE.equals(type)
                || Integer.class.equals(type)
                || Long.TYPE.equals(type)
                || Long.class.equals(type)) {
            columnType = ColumnType.INT;
        } else if (Float.TYPE.equals(type)
                || Float.class.equals(type)
                || Double.TYPE.equals(type)
                || Double.class.equals(type)) {
            columnType = ColumnType.FLOAT;
        } else if (String.class.equals(type)) {
            columnType = ColumnType.TEXT;
        } else if (byte[].class.equals(type)) {
            columnType = ColumnType.BLOB;
        } else {
            throw new IllegalArgumentException(String.format("Class `%s` is not natively" +
                    " supported by a Cursor", type));
        }

        return columnType;
    }

    private ColumnTypeUtils() {}
}
