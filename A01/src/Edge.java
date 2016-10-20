/**
 * Created by Talal on 20.10.16.
 */
public class Edge {
    String x;
    String y;
    int capacity;

    public Edge(String x, String y, int capacity) {
        this.x = x;
        this.y = y;
        this.capacity = capacity;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public int getCapacity() {
        return capacity;
    }


}
