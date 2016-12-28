# CursorMock

[![Maven Central](https://img.shields.io/maven-central/v/ru.noties/cursormock.svg)](http://search.maven.org/#search\|ga\|1\|g%3A%22ru.noties%22%20AND%20a%3A%22cursormock%22)

CursorMock is a library for Android that helps testing SQLite related code without need to create testing databases, managing database connections, etc in/for testing enviroment. CursorMock gives ability to build a Cursor in a fast and reliable way from manually provided values or from data objects.

```java
final Cursor cursor = CursorMockBuilder.forColumns("id", "first_name", "last_name")
        .addRow(1L, "Fu", "Bar")
        .addRow(2L, "Bar", "Fu")
        .build();
```

```java
final Cursor cursor = CursorMockBuilder.forClass(MyClass.class)
        .add(new MyClass())
        .addAll(Arrays.asList(new MyClass(), new MyClass()))
        .build();
```

## Installation
For production code
```gradle
compile 'ru.noties:cursormock:1.0.0'
```

For JUnit testing (CursorMock works great with [Robolectric](http://robolectric.org/))
```gradle
testCompile 'ru.noties:cursormock:1.0.0'
```

For Android testing
```gradle
androidTestCompile 'ru.noties:cursormock:1.0.0'
```

## Basics

### forColumns
```java
final CursorMock mock = CursorMockBuilder.forColumns("id", "name", "points")
        // adding a new row with all values = null
        .addRow()
        // adding a new row with one value specified (at index 0)
        .addRow(2L)
        // adding a new row with first 2 values specified
        .addRow(3L, "FuBar")
        // adding a new row with only one value at index 2 specified
        .addRow(null, null, 34)
        // returns CursorMock instance (which implements Cursor)
        .build();
```
So, after `build` the data can be represented as a table:

||id|name|points|
|-|--|----|------|
|1|null|null|null|
|2|2L|null|null|
|3|3L|FuBar|null|
|4|null|null|34|

CursorMock enforces type safety, so one column can have only one type of the value for all rows. For example:
```java
final CursorMock mock = CursorMockBuilder.forColumns("id")
        .addRow(1L)
        .addRow("string")
        .build();
```
will throw an exception at `.addRow("string")` as previously a long was added at that index.

### Types
CursorMock has 4 types of the data that can be added to Cursor without explicit convertion (other types will be discussed further):
|Java type|Cursor|
|---|---|
|short|INT|
|int|INT|
|long|INT|
|float|FLOAT|
|double|FLOAT|
|String|TEXT|
|byte[]|BLOB|

All numeric types can be also boxed (Short, Integer, Long, Float, Double). There is no support for boolean/Boolean, Byte[], etc.

### forClass

Another way to build a CursorMock instance is to use actual data classes.
```java
public class Item {

    long id;
    String title;
    float rating;

    public Item(long id, String title, float rating) {
        this.id = id;
        this.title = title;
        this.rating = rating;
    }
}
```
```java
final CursorMock mock = CursorMockBuilder.forClass(Item.class)
                .add(new Item(1L, "Item #1", 12))
                .add(new Item(2L, "Item #2", 36))
                .build();
```

Schema will be generated from a class definition. By default all `transient` and `static` fields are ignored and `column name` value is taken for Field name. Default behaviour also puts restrictions on using types.

#### ObjectProcessor
`ObjectProcessor` is used to:
* filter class fields
* define column name
* obtain a value from a field

```java
public interface ObjectProcessor {
    boolean ignore(Field field);
    String columnName(Field field);
    Object value(Field field, Object holder);
}
```
Please note that `value` methos should always return an object of supported type (listed above) or null.

For example we are using some ORM library and have specific annotation for a column:
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String value() default "";
}
```
If a Field is not annotated with it - we ignore it. It also has an optional `value` that we can use to change a name of the column.

```java
public class Item {

    @Column("item_id")
    long id;

    @Column
    String name;

    @Column
    int score;

    float willBeIgnored;

    public Item(long id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }
}
```

Our ObjectProcessor would look like (we can extend default ObjectProcessorImpl):
```java
public class MyObjectProcessor extends CursorMockBuilder.ObjectProcessorImpl {

    @Override
    public boolean ignore(Field field) {
        return field.getAnnotation(Column.class) == null;
    }

    @NonNull
    @Override
    public String columnName(Field field) {
        final String out;
        final Column column = field.getAnnotation(Column.class);
        if (column == null
                || TextUtils.isEmpty(column.value())) {
            out = field.getName();
        } else {
            out = column.value();
        }
        return out;
    }
}
```

In order to build a Cursor based on an object's class definition that has some specific logic:
```java
final CursorMock mock = CursorMockBuilder.forClass(Item.class, new MyObjectProcessor())
                .add(new Item(1L, "#1", 45))
                .add(new Item(2L, null, 89))
                .add(new Item(3L, "#3", -1))
                .build();
```
The data will be as follows:
||item_id|name|score|
|---|---|---|---|
|1|1L|#1|45|
|2|2L|null|89|
|3|3L|#3|-1|


## License

```
  Copyright 2016 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```