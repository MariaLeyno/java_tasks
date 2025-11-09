package org.tasks;

import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String str = "(EQ|NE|LT|GT)\\s?(\\d+(\\.\\d+)*)";
        java.util.regex.Matcher matcher = Pattern.compile(str).matcher("EQ 100");
        while(matcher.find()) {
            System.out.println(matcher.group(2));
        }
    }
}
