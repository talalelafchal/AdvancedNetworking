import java.util.ArrayList;

/**
 * Created by Talal on 20.10.16.
 */
public class Node {
    String name;
    ArrayList<Edge> edges = new ArrayList();

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }


    public void add(Edge edge) {
        edges.add(edge);
    }
}
