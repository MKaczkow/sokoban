package elkaproj.config;

/**
 * Represents a power-up of some sort, which enhances player's capabilities in some way.
 */
public enum GamePowerup {

    STRENGTH("Super-strength", "STRENGTH", "Allows for pushing 2 stacked crates for limited number of moves."),
    PULL("Pull", "PULL", "Allows for pulling a crate back once."),
    GHOST("Ghost", "GHOST", "Allows for phasing through walls.");

    private final String name, id, description;

    GamePowerup(String name, String id, String description) {
        this.name = name;
        this.id = id;
        this.description = description;
    }

    /**
     * Returns the display name of the powerup.
     *
     * @return Display name of the powerup.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the ID of the powerup.
     *
     * @return ID of the powerup.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the in-game description of the powerup.
     *
     * @return In-game description of the powerup.
     */
    public String getDescription() {
        return this.description;
    }
}
