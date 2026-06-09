package lab6.models;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Класс, представляющий человека (например, фронтмена группы)
 *
 * @author Михаил
 */
public class Person implements Serializable {
    private String name; //Поле не может быть null, Строка не может быть пустой
    private java.time.LocalDate birthday; //Поле может быть null
    private Color eyeColor; //Поле может быть null
    private static final long serialVersionUID = 1L;

    public Person(String name, LocalDate birthday, Color eyeColor) {
        if (name == null) {
            throw new RuntimeException("name не может быть null");
        }
        if (name.isBlank()) {
            throw new RuntimeException("name не может быть пустым");
        }
        this.name = name;
        this.birthday = birthday;
        this.eyeColor = eyeColor;
    }

    public String getName() {
        return name;
    }

    public java.time.LocalDate getBirthday() {
        return birthday;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", birthday=" + birthday + ", eyeColor=" + eyeColor + "]";
    }
}