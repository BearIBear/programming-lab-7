package lab6.server.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lab6.models.*;

/**
 * Менеджер для работы с базой данных PostgreSQL
 *
 * @author Михаил
 */
public class DatabaseManager {
    private static final Logger log = LogManager.getLogger(DatabaseManager.class);
    private Connection connection;
    private String host;
    private int port;
    private String dbName;
    private String user;
    private String password;

    public DatabaseManager() {
        loadProperties();
        connect();
        createTables();
    }

    private void loadProperties() {
        Properties properties = new Properties();
        File propFile = new File("db.properties");
        if (!propFile.exists()) {
            // Если файла нет, поищем в подкаталоге server
            propFile = new File("server/db.properties");
        }

        if (!propFile.exists()) {
            log.info("db.properties not found. Creating default db.properties");
            properties.setProperty("db.host", "localhost");
            properties.setProperty("db.port", "5432");
            properties.setProperty("db.name", "studs");
            properties.setProperty("db.user", "postgres");
            properties.setProperty("db.password", "postgres");
            try (FileOutputStream out = new FileOutputStream("db.properties")) {
                properties.store(out, "Database Connection Settings");
            } catch (IOException e) {
                log.error("Failed to write default db.properties: " + e.getMessage());
            }
        } else {
            try (FileInputStream in = new FileInputStream(propFile)) {
                properties.load(in);
            } catch (IOException e) {
                log.error("Failed to load db.properties: " + e.getMessage());
            }
        }

        this.host = properties.getProperty("db.host", "localhost");
        this.port = Integer.parseInt(properties.getProperty("db.port", "5432"));
        this.dbName = properties.getProperty("db.name", "studs");
        this.user = properties.getProperty("db.user", "postgres");
        this.password = properties.getProperty("db.password", "postgres");
    }

    private void connect() {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        try {
            // Явное указание драйвера
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url, user, password);
            log.info("Connected to database successfully: " + url);
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL driver not found: " + e.getMessage());
        } catch (SQLException e) {
            log.error("Failed to connect to database: " + e.getMessage());
        }
    }

    private void createTables() {
        if (connection == null) {
            log.error("Cannot create tables: connection is null");
            return;
        }

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username VARCHAR(100) PRIMARY KEY," +
                "password_hash VARCHAR(64) NOT NULL" +
                ");";

        String createBandsTable = "CREATE TABLE IF NOT EXISTS music_bands (" +
                "id BIGSERIAL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL CHECK (name <> '')," +
                "coordinate_x BIGINT NOT NULL CHECK (coordinate_x <= 432)," +
                "coordinate_y REAL NOT NULL," +
                "creation_date TIMESTAMP NOT NULL," +
                "number_of_participants BIGINT NOT NULL CHECK (number_of_participants > 0)," +
                "singles_count BIGINT NOT NULL CHECK (singles_count > 0)," +
                "description TEXT," +
                "genre VARCHAR(50)," +
                "frontman_name VARCHAR(255) NOT NULL CHECK (frontman_name <> '')," +
                "frontman_birthday DATE," +
                "frontman_eye_color VARCHAR(50)," +
                "owner_username VARCHAR(100) NOT NULL REFERENCES users(username) ON DELETE CASCADE" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createBandsTable);
            log.info("Database tables verified/created successfully.");
        } catch (SQLException e) {
            log.error("Failed to create tables: " + e.getMessage());
        }
    }

    public synchronized boolean registerUser(String username, String password) {
        if (connection == null)
            return false;
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, PasswordHasher.hashPassword(password));
            stmt.executeUpdate();
            log.info("User registered successfully: " + username);
            return true;
        } catch (SQLException e) {
            log.warn("Failed to register user (might already exist): " + e.getMessage());
            return false;
        }
    }

    public synchronized boolean validateUser(String username, String password) {
        if (connection == null)
            return false;
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    return hash.equals(PasswordHasher.hashPassword(password));
                }
            }
        } catch (SQLException e) {
            log.error("Error validating user " + username + ": " + e.getMessage());
        }
        return false;
    }

    public synchronized void loadCollection(CollectionManager collectionManager) {
        if (connection == null)
            return;
        String sql = "SELECT * FROM music_bands";
        int count = 0;
        long maxId = 0;
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            collectionManager.clearCollection();

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                long x = rs.getLong("coordinate_x");
                float y = rs.getFloat("coordinate_y");
                Timestamp creationTimestamp = rs.getTimestamp("creation_date");
                java.util.Date creationDate = new java.util.Date(creationTimestamp.getTime());
                long numberOfParticipants = rs.getLong("number_of_participants");
                long singlesCount = rs.getLong("singles_count");
                String description = rs.getString("description");
                String genreStr = rs.getString("genre");
                MusicGenre genre = (genreStr != null) ? MusicGenre.valueOf(genreStr) : null;

                String frontmanName = rs.getString("frontman_name");
                Date frontmanBirthdayDate = rs.getDate("frontman_birthday");
                LocalDate frontmanBirthday = null;
                if (frontmanBirthdayDate != null) {
                    frontmanBirthday = new java.sql.Date(frontmanBirthdayDate.getTime()).toLocalDate();
                }
                String eyeColorStr = rs.getString("frontman_eye_color");
                Color eyeColor = (eyeColorStr != null) ? Color.valueOf(eyeColorStr) : null;

                String ownerUsername = rs.getString("owner_username");

                Coordinates coordinates = new Coordinates(x, y);
                Person frontMan = new Person(frontmanName, frontmanBirthday, eyeColor);

                // Создаем временный объект
                MusicBand band = new MusicBand(name, coordinates, numberOfParticipants, singlesCount, description,
                        genre, frontMan);
                band.setId(id);
                band.setOwnerUsername(ownerUsername);

                // Задаем дату создания из БД (вместо автогенерируемой в конструкторе)
                try {
                    java.lang.reflect.Field creationDateField = MusicBand.class.getDeclaredField("creationDate");
                    creationDateField.setAccessible(true);
                    creationDateField.set(band, creationDate);
                } catch (Exception e) {
                    log.error("Failed to restore creationDate via reflection: " + e.getMessage());
                }

                collectionManager.addLoadedElement(band);
                maxId = Math.max(id, maxId);
                count++;
            }
            CollectionManager.setNextId(maxId + 1);
            log.info("Loaded " + count + " elements from database. nextId set to " + (maxId + 1));
        } catch (SQLException e) {
            log.error("Failed to load collection from database: " + e.getMessage());
        }
    }

    public synchronized boolean addBand(MusicBand band, String username) {
        if (connection == null)
            return false;
        String sql = "INSERT INTO music_bands (name, coordinate_x, coordinate_y, creation_date, " +
                "number_of_participants, singles_count, description, genre, frontman_name, " +
                "frontman_birthday, frontman_eye_color, owner_username) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, band.getName());
            stmt.setLong(2, band.getCoordinates().getX());
            stmt.setFloat(3, band.getCoordinates().getY());
            stmt.setTimestamp(4, new Timestamp(band.getCreationDate().getTime()));
            stmt.setLong(5, band.getNumberOfParticipants());
            stmt.setLong(6, band.getSinglesCount());
            stmt.setString(7, band.getDescription());
            stmt.setString(8, band.getGenre() != null ? band.getGenre().name() : null);
            stmt.setString(9, band.getFrontMan().getName());
            stmt.setDate(10,
                    band.getFrontMan().getBirthday() != null ? Date.valueOf(band.getFrontMan().getBirthday()) : null);
            stmt.setString(11,
                    band.getFrontMan().getEyeColor() != null ? band.getFrontMan().getEyeColor().name() : null);
            stmt.setString(12, username);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    band.setId(id);
                    band.setOwnerUsername(username);
                    return true;
                }
            }
        } catch (SQLException e) {
            log.error("Error inserting band: " + e.getMessage());
        }
        return false;
    }

    public synchronized boolean updateBand(long id, MusicBand band, String username) {
        if (connection == null)
            return false;
        String sql = "UPDATE music_bands SET name = ?, coordinate_x = ?, coordinate_y = ?, " +
                "number_of_participants = ?, singles_count = ?, description = ?, genre = ?, " +
                "frontman_name = ?, frontman_birthday = ?, frontman_eye_color = ? " +
                "WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, band.getName());
            stmt.setLong(2, band.getCoordinates().getX());
            stmt.setFloat(3, band.getCoordinates().getY());
            stmt.setLong(4, band.getNumberOfParticipants());
            stmt.setLong(5, band.getSinglesCount());
            stmt.setString(6, band.getDescription());
            stmt.setString(7, band.getGenre() != null ? band.getGenre().name() : null);
            stmt.setString(8, band.getFrontMan().getName());
            stmt.setDate(9,
                    band.getFrontMan().getBirthday() != null ? Date.valueOf(band.getFrontMan().getBirthday()) : null);
            stmt.setString(10,
                    band.getFrontMan().getEyeColor() != null ? band.getFrontMan().getEyeColor().name() : null);
            stmt.setLong(11, id);
            stmt.setString(12, username);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error updating band: " + e.getMessage());
        }
        return false;
    }

    public synchronized boolean removeBand(long id, String username) {
        if (connection == null)
            return false;
        String sql = "DELETE FROM music_bands WHERE id = ? AND owner_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setString(2, username);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error deleting band: " + e.getMessage());
        }
        return false;
    }

    public synchronized boolean clearBands(String username) {
        if (connection == null)
            return false;
        String sql = "DELETE FROM music_bands WHERE owner_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error clearing bands: " + e.getMessage());
        }
        return false;
    }
}
