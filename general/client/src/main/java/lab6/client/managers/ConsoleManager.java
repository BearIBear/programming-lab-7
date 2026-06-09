package lab6.client.managers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import lab6.models.Color;
import lab6.models.Coordinates;
import lab6.models.MusicBand;
import lab6.models.MusicGenre;
import lab6.models.Person;

/**
 * Менеджер для работы с консолью (JLine), получения полей объектов от пользователя
 *
 * @author Михаил
 */
public class ConsoleManager {
    private Terminal terminal;
    private LineReader reader;


    public ConsoleManager(Terminal terminal, LineReader reader) {
        this.terminal = terminal;
        this.reader = reader;
    }

    public ConsoleManager(Terminal terminal) {
        this.terminal = terminal;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public LineReader getReader() {
        return reader;
    }

    public String askName() {
        while (true) {
            String name = reader.readLine("Введите name группы: ");
            if (name == null || name.equals("")) {
                terminal.writer().println("name не может быть null");
                terminal.flush();
                continue;
            }
            if (name.isBlank()) {
                terminal.writer().println("name не может быть пустым");
                terminal.flush();
                continue;
            }
            return name;
        }
    }

    public Long askX() {
        while (true) {
            String input = reader.readLine("Введите x группы: ");
            if (input == null || input.isBlank()) {
                terminal.writer().println("x не может быть null");
                terminal.flush();
                continue;
            }
            try {
                Long x = Long.parseLong(input);
                if (x > 432) {
                    terminal.writer().println("x не может превышать 432");
                    terminal.flush();
                    continue;
                }
                return x;
            } catch (NumberFormatException e) {
                terminal.writer().println("x должно быть типа Long");
                terminal.flush();
            }
        }
    }

    public float askY() {
        while (true) {
            String input = reader.readLine("Введите y группы: ");
            if (input == null || input.isBlank()) {
                terminal.writer().println("y не может быть null");
                terminal.flush();
                continue;
            }
            try {
                Float y = Float.parseFloat(input);
                if (y.isInfinite()) {
                    terminal.writer().println("y слишком большое!");
                    continue;
                }
                return y;
            } catch (NumberFormatException e) {
                terminal.writer().println("y должно быть типа float");
                terminal.flush();
            }
        }
    }

    public Coordinates askCoordinates() {
        Long x = askX();
        float y = askY();
        return new Coordinates(x, y);
    }

    public long askNumberOfParticipants() {
        while (true) {
            String input = reader.readLine("Введите numberOfParticipants группы: ");
            try {
                long n = Long.parseLong(input);
                if (n < 1) {
                    terminal.writer().println("numberOfParticipants должно быть больше 0");
                    terminal.flush();
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                terminal.writer().println("numberOfParticipants должно быть типа long");
                terminal.flush();
            }
        }
    }

    public long askSinglesCount() {
        while (true) {
            String input = reader.readLine("Введите singlesCount группы: ");
            try {
                long n = Long.parseLong(input);
                if (n < 1) {
                    terminal.writer().println("singlesCount должно быть больше 0");
                    terminal.flush();
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                terminal.writer().println("singlesCount должно быть типа long");
                terminal.flush();
            }
        }
    }

    public String askDescription() {
        String description = reader.readLine("Введите description группы: ");
        if (description == null || description.isBlank()) {
            return null;
        }
        return description;
    }

    public MusicGenre askMusicGenre() {
        while (true) {
            terminal.writer().println("Доступные жанры: " + Arrays.toString(MusicGenre.values()));
            terminal.flush();
            String input = reader.readLine("Введите MusicGenre: ");
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return MusicGenre.valueOf(input.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                terminal.writer().println("Такого жанра нет в списке");
                terminal.flush();
            }
        }
    }

    public String askPersonName() {
        while (true) {
            String name = reader.readLine("Введите name фронтмена: ");
            if (name == null || name.equals("")) {
                terminal.writer().println("name не может быть null");
                terminal.flush();
                continue;
            }
            if (name.isBlank()) {
                terminal.writer().println("name не может быть пустым");
                terminal.flush();
                continue;
            }
            return name;
        }
    }

    public LocalDate askBirthday() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            String input = reader.readLine("Введите birthday фронтмена (yyyy-MM-dd): ");
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                terminal.writer().println("Неверный формат даты. Используйте yyyy-MM-dd");
                terminal.flush();
            }
        }
    }

    public Color askColor() {
        while (true) {
            terminal.writer().println("Доступные цвета: " + Arrays.toString(Color.values()));
            terminal.flush();
            String input = reader.readLine("Введите eyeColor фронтмена: ");
            if (input == null || input.isBlank()) {
                return null;
            }
            try {
                return Color.valueOf(input.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                terminal.writer().println("Такого цвета нет в списке");
                terminal.flush();
            }
        }
    }

    public Person askFrontMan() {
        String name = askPersonName();
        LocalDate birthday = askBirthday();
        Color eyeColor = askColor();
        return new Person(name, birthday, eyeColor);
    }

    public MusicBand askMusicBand() {
        String name = askName();
        Coordinates coords = askCoordinates();
        long participants = askNumberOfParticipants();
        long singles = askSinglesCount();
        String description = askDescription();
        MusicGenre genre = askMusicGenre();
        Person frontMan = askFrontMan();
        return new MusicBand(name, coords, participants, singles, description, genre, frontMan);
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public void setReader(LineReader reader) {
        this.reader = reader;
    }

}
