package dev.lukebemish.emojiidentifiers.test.🧩;

import dev.lukebemish.emojiidentifiers.test.module.InModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPackageWithEmoji {
    @Test
    void testPackageWithEmoji() {
        assertEquals("dev.lukebemish.emojiidentifiers.test.module", InModule.class.getModule().getName());
    }
}
