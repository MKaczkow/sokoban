package elkaproj.httpserver.services;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * Handles and manages a PostgreSQL connection.
 */
@Service(kind = ServiceKind.TRANSIENT)
public class PostgresHandler {

    private final PostgresConfigurationProvider configurationProvider;
    private Connection connection = null;

    private PostgresHandler(PostgresConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
    }

    public synchronized void open() throws IOException, SQLException {
        if (this.connection != null)
            return;

        String url = this.configurationProvider.getUrl();
        Properties props = this.configurationProvider.getProperties();
        this.connection = DriverManager.getConnection(url, props);
    }

    public synchronized void close() throws SQLException {
        if (this.connection == null)
            return;

        this.connection.close();
        this.connection = null;
    }

    public synchronized void ensureDb() throws SQLException {
        try (Statement stmt = this.connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS highscores(" +
                    "player text not null," +
                    "level_pack text not null," +
                    "level int not null," +
                    "score int not null," +
                    "primary key (player, level_pack, level));");
        }
    }

    public int getTotalCount() throws SQLException {
        try (Statement stmt = this.connection.createStatement()) {
            try (ResultSet res = stmt.executeQuery("SELECT COUNT(*) FROM highscores;")) {
                if (res.next())
                    return res.getInt(1);
            }
        }

        return -1;
    }
}
