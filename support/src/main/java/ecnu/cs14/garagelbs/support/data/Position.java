package ecnu.cs14.garagelbs.support.data;

/**
 * Represents a position.
 * Created by K on 2017/4/8.
 */

public final class Position {
    public int x, y, z;
    public Position(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return "(" +
                Integer.toString(x) +
                ", " +
                Integer.toString(y) +
                ", " +
                Integer.toString(z) +
                ")";
    }
}
