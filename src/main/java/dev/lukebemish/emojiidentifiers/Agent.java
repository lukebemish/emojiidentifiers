package dev.lukebemish.emojiidentifiers;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public final class Agent {
    private Agent() {}

    public static void premain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        instrumentation.addTransformer(new CharacterTransformer(), true);
        instrumentation.retransformClasses(Character.class);
        try {
            Class.forName("dev.lukebemish.emojiidentifiers.EmojiChecker", true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("emojiidentifiers agent loaded; weird behavior may ensue");
    }
}
