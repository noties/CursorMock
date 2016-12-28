package ru.noties.cursormock;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Basic implementation for fast {@link CursorMock} building. There are 2 ways to build a CursorMock:
 * by providing columns names manually and from a class definition.
 *
 * @see #forColumns(String, String...)
 * @see #forClass(Class)
 * @see #forClass(Class, ObjectProcessor)
 */
@SuppressWarnings("WeakerAccess")
public abstract class CursorMockBuilder {

    /**
     * Interface that is used when building a CursorMock from a class definition.
     * @see ObjectProcessorImpl
     */
    public interface ObjectProcessor {

        /**
         * Filtering of {@link Field} of a class.
         *
         * @param field to inspect
         * @return boolean indicating if supplied `field` should be ignored
         *
         * @see ObjectProcessorImpl#ignore(Field)
         */
        boolean ignore(Field field);

        /**
         * A way to `name` a {@link Field} of a class.
         *
         * @param field to inspect
         * @return column name for specified `field`
         */
        @NonNull
        String columnName(Field field);

        /**
         * Obtaining a value for an object. This *must* return an object of supported
         * by {@link android.database.Cursor} type.
         * If a field has not supported type its value should be serialized here.
         *
         * @param field to inspect
         * @param holder an object that contains `field`
         * @return an object of natively supported by {@link android.database.Cursor} type
         *
         * @see ColumnType
         * @see ColumnTypeUtils
         */
        @Nullable
        Object value(Field field, Object holder);
    }

    /**
     * Factory method to start building a {@link CursorMock} based on column names
     *
     * {@code
     *     final CursorMock mock = CursorMockBuilder.forColumns(\"id\", \"name\")
     *          .addRow(1L, \"Jimmy\")
     *          .build();
     * }
     *
     * @param firstColumn non-null name of first column
     * @param otherColumns optional other columns
     * @return a {@link ForColumns} builder
     *
     * @see ForColumns
     * @see CursorSchemaFactory#raw(String, String...)
     */
    public static ForColumns forColumns(@NonNull String firstColumn, String... otherColumns) {
        return new ForColumns(CursorSchemaFactory.raw(firstColumn, otherColumns));
    }

    /**
     * @see ObjectProcessorImpl
     * @see #forClass(Class, ObjectProcessor)
     */
    public static <T> ForClass<T> forClass(@NonNull Class<T> cl) throws IllegalArgumentException {
        return new ForClass<>(cl, new ObjectProcessorImpl());
    }

    /**
     * Factory method to start build a {@link CursorMock} based on class definition.
     *
     * {@code
     *     final CursorMock mock = CursorMockBuilder.forClass(MyClass.class)
     *          .add(new MyClass())
     *          .addAll(Arrays.asList(new MyClass(), new MyClass()))
     *          .build();
     * }
     *
     * @param cl a {@link Class} to build {@link CursorSchema} from
     * @param processor a {@link ObjectProcessor} to manipulate data (required)
     * @param <T> for type safety (will allow adding objects only of `<T>` instance)
     * @return a {@link ForClass} instance
     * @throws IllegalArgumentException if supplied `cl` has no fields, or all fields were filtered
     *          by {@link ObjectProcessor#ignore(Field)}
     *
     * @see ForClass#add(Object)
     * @see ForClass#addAll(Collection)
     */
    public static <T> ForClass<T> forClass(@NonNull Class<T> cl, @NonNull ObjectProcessor processor) throws IllegalArgumentException {
        return new ForClass<>(cl, processor);
    }

    protected final CursorSchema mCursorSchema;
    protected final CursorRowsBuilder mRowsBuilder;

    protected CursorMockBuilder(@NonNull CursorSchema schema) {
        mCursorSchema = schema;
        mRowsBuilder = new CursorRowsBuilder(mCursorSchema);
    }

    /**
     * @return an instance of {@link CursorMock}
     */
    public CursorMock build() {
        return new CursorMock(mCursorSchema, mRowsBuilder.build());
    }

    /**
     * Helper class to build a {@link CursorMock} from column names.
     * Please note that column types will be detected in runtime based
     * on values passed.
     *
     * @see #addRow(Object...)
     * @see CursorRowsBuilder
     */
    public static class ForColumns extends CursorMockBuilder {

        ForColumns(@NonNull CursorSchema schema) {
            super(schema);
        }

        /**
         * Simply passes array of objects to {@link CursorRowsBuilder}
         * @param rowValues object values to put in a new cursor row
         * @return self to chain calls
         */
        public ForColumns addRow(Object... rowValues) {
            mRowsBuilder.addRow(rowValues);
            return this;
        }
    }

    /**
     * A class for building {@link CursorMock} from a class definition.
     * A {@link CursorSchema} will be build in constructor with deferred type information.
     * Actual types of columns will be detected with adding of data through {@link #add(Object)}
     * and {@link #addAll(Collection)}
     *
     * Supported types are listed in {@link ColumnType}, code that detects type in: {@link ColumnTypeUtils#columnType(Class)}
     */
    public static class ForClass<T> extends CursorMockBuilder {

        private final ObjectProcessor mProcessor;

        ForClass(@NonNull Class<T> cl, @NonNull ObjectProcessor processor) throws IllegalArgumentException {
            super(buildSchema(cl, processor));
            mProcessor = processor;
        }

        /**
         * @param object object to be `flattened` to row values (as object array). Parameter
         *               can be NULL (en empty row will be inserted).
         * @return instance of this builder to chain calls
         *
         * @see CursorRowsBuilder
         * @see CursorRowsBuilder#addRow(Object...)
         * @see ObjectProcessor#value(Field, Object)
         */
        public ForClass<T> add(@Nullable T object) {
            if (object == null) {
                // all null values in a row for a null object
                mRowsBuilder.addRow();
            } else {
                final List<Field> fields = ((CursorSchemaFiltered) mCursorSchema).fields();
                final int size = fields.size();
                final Object[] values = new Object[size];
                for (int i = 0; i < size; i++) {
                    values[i] = mProcessor.value(fields.get(i), object);
                }
                mRowsBuilder.addRow(values);
            }
            return this;
        }

        /**
         * Method to add a collection of objects of type `<T>`
         * @param collection non-null collection of T (can contain 0 items, no rows will be inserted)
         * @return instance for chaining
         */
        public ForClass<T> addAll(@NonNull Collection<T> collection) {
            for (T object: collection) {
                add(object);
            }
            return this;
        }

        private static CursorSchemaFiltered buildSchema(Class<?> cl, ObjectProcessor processor) throws IllegalArgumentException {

            final Field[] fields = cl.getDeclaredFields();

            if (fields == null
                    || fields.length == 0) {
                throw new IllegalArgumentException("Supplied object has no fields, class: " + cl.getName());
            }

            // here we will only filter our fields that we are not interested in
            // columnTypes must be detected when adding values as rows

            final List<Field> filteredFields = new ArrayList<>();
            final List<String> columnNames = new ArrayList<>();

            for (Field field: fields) {

                field.setAccessible(true);

                if (processor.ignore(field)) {
                    continue;
                }

                filteredFields.add(field);
                columnNames.add(processor.columnName(field));
            }

            final int size = columnNames.size();
            if (size == 0) {
                throw new IllegalArgumentException("All fields from `" + cl.getName() + "` were" +
                        " filtered");
            }

            final String[] columns = columnNames.toArray(new String[size]);
            return new CursorSchemaFiltered(filteredFields, columns, new ColumnType[size]);
        }

        private static class CursorSchemaFiltered extends CursorSchemaImpl {

            private final List<Field> mFields;

            CursorSchemaFiltered(
                    @NonNull List<Field> fields,
                    @NonNull String[] columnNames,
                    @NonNull ColumnType[] columnTypes
            ) throws IllegalArgumentException {
                super(columnNames, columnTypes);
                mFields = Collections.unmodifiableList(fields);
            }

            List<Field> fields() {
                return mFields;
            }
        }
    }

    /**
     * Basic implementation of {@link ObjectProcessor}.
     * Default behaviour:
     *  * ignores all `static` and `transient` fields
     *  * uses field name as a column name
     *  * simply returns an object that field is holding
     */
    public static class ObjectProcessorImpl implements ObjectProcessor {

        @Override
        public boolean ignore(Field field) {
            final int modifiers = field.getModifiers();
            return Modifier.isStatic(modifiers)
                    || Modifier.isTransient(modifiers);
        }

        @Override
        @NonNull
        public String columnName(Field field) {
            return field.getName();
        }

        @Nullable
        @Override
        public Object value(Field field, Object holder) {
            try {
                return field.get(holder);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
