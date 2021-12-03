# Code Analyzer

Prints the call hierarchy of the methods which manipulate the given data object.

# Usage

```
java -jar target/code-analyzer-<VERSION>.jar -s (--source-folder) SOURCE_FOLDER -t (--table-name) TABLE_NAME

 -s (--source-folder) SOURCE_FOLDERS : source folder(s) for the analyzed project
 -t (--table-name) TABLE_NAME        : table name which is manipulated
```

# Example

Execute this from this project's root directory

```
$ java -jar target/code-analyzer-*.jar -s F:\Java_stuff\Freelancing\ApneSaathi\ApneSaathiBackend\developer-parvathy\VolunteerApp\src\main\java -t cases
```
