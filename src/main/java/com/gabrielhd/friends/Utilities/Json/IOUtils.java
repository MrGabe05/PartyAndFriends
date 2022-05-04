package com.gabrielhd.friends.Utilities.Json;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static int EOF = -1;

    public IOUtils() {
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        return copyLarge(input, output, new char[4096]);
    }

    public static long copyLarge(Reader input, Writer output, char[] buffer) throws IOException {
        long count;
        int n;
        for(count = 0L; -1 != (n = input.read(buffer)); count += n) {
            output.write(buffer, 0, n);
        }

        return count;
    }

    public static String toString(Reader input) throws IOException {
        StringBuilderWriter sw = new StringBuilderWriter();
        copy(input, sw);
        return sw.toString();
    }
}