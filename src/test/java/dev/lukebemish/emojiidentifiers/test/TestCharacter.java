package dev.lukebemish.emojiidentifiers.test;

import dev.lukebemish.emojiidentifiers.EmojiChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCharacter {
    @Test
    void testEmojiChecker() {
        assertTrue(EmojiChecker.isEmoji('⌛'));
    }

    @Test
    void testAreEmojisValidIdentifiers() {
        assertTrue(Character.isJavaIdentifierPart('⌛'));
    }

    @Test
    void ⌛() {

    }

    @Test
    void 🛠️() {

    }
}
