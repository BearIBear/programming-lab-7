package lab6.models;

import java.io.Serializable;

/**
 * Класс, представляющий координаты (x, y)
 *
 * @author Михаил
 */
public class Coordinates implements Serializable {
    private Long x; //Максимальное значение поля: 432, Поле не может быть null
    private float y;
    private static final long serialVersionUID = 1L;

    public Coordinates(Long x, float y) {
        if (x == null) {
            throw new RuntimeException("x не может быть null");
        }
        if (x > 432) {
            throw new RuntimeException("x не может превышать 432");
        }
        this.x = x;
        this.y = y;
    }
    
    public Long getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Coordinates [x=" + x + ", y=" + y + "]";
    }
    
}
