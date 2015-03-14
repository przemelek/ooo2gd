ClassPath is a magic name for path where Java Virtual Machine search for Java classes.

Every Java Program is a set of classes. JVM to run program needs to know code of this program, and this means JVM must to know code of classes. To this JVM needs to load classes, and for loading classes it must to know where those classes are. ClassPath describe where JVM should try to search for classes.

On ClassPath we may have directories/folders or JAR files (also ZIP files). If you put directory/folder on ClassPath JVM will try to find .class files in this directory and all sub-directories, if you put JAR file JVM will look into this file for .class files. **If you put directory with JAR files JVM will ignore JAR files.** Instead of this you must put all JAR files on ClassPath.
