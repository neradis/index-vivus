package test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
public class HelloWorldService {

    public static String getGreeting() {
        ImmutableList<String> list = ImmutableList.of("Hello", "to", "Ruby");
        return Joiner.on(' ').join(list) + " from the Java World!";
    }
}
