import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by Talal on 20.10.16.
 */
public class Abilene {
    static ArrayList<Node> nodes = new ArrayList();
    static ArrayList<Edge> edges = new ArrayList();

    public static void main(String[] args) throws IOException {
        String filePath = args[0];
        String xFile = args[1];
        initializeRoutersLinks(filePath);
        addEdgestoNodes();
        for (Node node: nodes
             ) {
            System.out.println( node.getName()+" "+node.edges.size());
        }
    }

    public  static void initializeRoutersLinks(String filePath) throws IOException {
        boolean name = false;
        boolean xycapacity = false;
        BufferedReader inputStream = new BufferedReader(new FileReader(filePath));

        String thisLine;
        while ((thisLine = inputStream.readLine()) != null) {
            if (thisLine.startsWith("#name")) {
                name = true;
                thisLine = inputStream.readLine();
            }
            if (!Character.isUpperCase(thisLine.charAt(0))) {
                name = false;
                xycapacity = false;
            }
            if (thisLine.startsWith("#x")) {
                xycapacity = true;
                thisLine = inputStream.readLine();
            }
            if (name) {
                Node node = new Node(thisLine.split("\\s+")[0]);
                nodes.add(node);
            }
            if (xycapacity) {
                String[] ed = thisLine.split("\\s+") ;
                Edge edge = new Edge(ed[0],ed[1],Integer.parseInt(ed[2]));
                edges.add(edge);
            }
        }
    }

    public static void addEdgestoNodes(){

        for (Node node: nodes
             ) {
            for (Edge ed:edges
                 ) {
                if (ed.getX().equals(node.getName())){
                    node.add(ed);
                }
            }
        }
    }

}
