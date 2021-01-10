package elkaproj.config;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum LevelTile {
    WALL("Wall", '#'),
    FLOOR("Floor", '_'),
    PLAYER("Player", 'S'),
    CRATE("Crate", 'P'),
    TARGET_SPOT("Target spot", 'X');

    private static final Map<Character, LevelTile> typeCache;

    private final String name;
    private final char representation;

    static {
        HashMap<Character, LevelTile> c = new HashMap<>();
        for (LevelTile t : EnumSet.allOf(LevelTile.class)) {
            c.put(t.representation, t);
        }

        typeCache = c;
    }

    LevelTile(String name, char representation) {
        this.name = name;
        this.representation = representation;
    }

    public String getName() {
        return this.name;
    }

    public char getRepresentation() {
        return this.representation;
    }

    public static LevelTile fromRepresentation(char representation) {
        if (!typeCache.containsKey(representation)) {
            throw new IllegalArgumentException("Specified tile representation is invalid.");
        }

        return typeCache.get(representation);
    }
}
