Author: Igor Mordashev <erwaht@gmail.com>

Built with Scala, Akka, Akka Streams, SBT

*Requirements:
    - Java 8
    - SBT 0.13.11

*Build

> sbt assembly

A complete JAR package would be created in the following folder:

/path/to/the/project/target/scala-2.11/FyberAssignment-assembly-1.0.jar

*Run

> cd /path/to/the/jar
> java -jar FyberAssignment-assembly-1.0.jar INPUT_FILE OUTPUT_FILE [WINDOW_LENGTH_IN_SECONDS]

Where
INPUT_FILE - Path to the input file
OUTPUT_FILE - Output file would be silently overwritten
WINDOW_LENGTH_IN_SECONDS - Optional parameter. It allows to specify the sliding window length in seconds. The default value is 60 seconds.