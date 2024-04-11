package dev.lukebemish.emojiidentifiers;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;

public final class EmojiChecker {
    private static final HashSet<Integer> EMOJI_CODE_POINTS = new HashSet<>();

    static {
        try (
                InputStream stream = EmojiChecker.class.getResourceAsStream("/emoji-list.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(stream)))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                int codePoint = Integer.parseInt(line, 16);
                EMOJI_CODE_POINTS.add(codePoint);
                if (codePoint > 0xFFFF) {
                    int h = (codePoint - 0x10000) / 0x400 + 0xD800;
                    int l = (codePoint - 0x10000) % 0x400 + 0xDC00;
                    EMOJI_CODE_POINTS.add(h);
                    EMOJI_CODE_POINTS.add(l);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private EmojiChecker() {}

    public static boolean isEmoji(int codePoint) {
        return EMOJI_CODE_POINTS.contains(codePoint);
    }
}
