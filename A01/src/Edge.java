/**
 * Created by Talal on 20.10.16.
 */
public class Edge {
    String x;
    String y;
    int capacity;
    Node nodex;
    Node nodey;

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

    public void setNodex(Node nodex) {
        this.nodex = nodex;
    }

    public Node getNodex() {
        return nodex;
    }

    public Node getNodey() {
        return nodey;
    }

    public void setNodey(Node nodey) {
        this.nodey = nodey;
    }
}
