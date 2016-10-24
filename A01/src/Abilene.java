import java.io.*;
import java.util.*;


/**
 * Created by Talal on 20.10.16.
 */
public class Abilene {
    static ArrayList<Node> nodes = new ArrayList();
    static ArrayList<Edge> edges = new ArrayList();
    static int i = 0;
    static SortedMap<String, String> map = new TreeMap<String, String>() {
    };

    public static void main(String[] args) throws IOException {
        String topoFilePath = args[0];
        String xFilePath = args[1];
        String demandeFilePath = args[2];
        initializeHashMap(demandeFilePath);
        initializeRoutersLinks(topoFilePath);
        addEdgestoNodes();
        addNodestoEdges();
        addNodesInOut();
        // generate models files

        generateModel(xFilePath);

        //initializeArray(xFilePath);

    }

//    private static void initializeArray(String xFile) throws IOException {
//        BufferedReader inputStream = new BufferedReader(new FileReader(xFile));
//        String[] array;
//        String line = inputStream.readLine();
//        array = line.split("\\s+");
//
//        for (int i = 1, j = 0; i <= array.length - 1; i = i + 5, j = j + 1) {
//            demandArray[j] = String.valueOf(Double.valueOf(array[i]).longValue());
//
//        }
//        inputStream.close();

//    }

    private static void generateModel(String filepath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String[] array;

        String thisLine;
        while ((thisLine = reader.readLine()) != null) {
            array = thisLine.split("\\s+");
            String[] demandArray = new String[144];
            for (int i = 1, j = 0; i <= array.length - 1; i = i + 5, j = j + 1) {
                demandArray[j] = String.valueOf(Double.valueOf(array[i]).longValue());
            }
            updateMap(demandArray);

            BufferedWriter bufw;
            bufw = new BufferedWriter(createFile());
            bufw.write("Minimize\n");
            bufw.write("\tobj: z\n");
            bufw.write("Subject To\n");
            //demand constrain
            demandConstrain(bufw);
            //flow preservation
            bufw.write("\\preservation\n");
            preservationConstrain(bufw);
            //capacity constrains
            bufw.write("\\capacity\n");
            capacityConstrain(bufw);
            //non negative constrains
            bufw.write("\\Non-Negative\n");
            bufw.write("Bounds\n");
            nonNegativeConstrain(bufw);
            bufw.write("\tz >= 0\n");
            bufw.write("End");


            bufw.close();
        }
        reader.close();





    }

    private static void nonNegativeConstrain(BufferedWriter bufw) throws IOException {
        String entr;
        String start;
        String dest;
        for (Map.Entry<String, String> entry : map.entrySet()
                ) {
            entr = entry.getKey();
            String[] split = entr.split(",");
            start = split[0];
            dest = split[1];
            if (!(start.equals(dest))) {
                for (Edge edge : edges
                        ) {
                    bufw.write("\tflow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY() + " >= 0\n");
                }
            }
        }
    }

    private static void capacityConstrain(BufferedWriter bufw) throws IOException {
        String entr;
        String start;
        String dest;
        for (Edge edge : edges
                ) {
            bufw.write("\tC_" + edge.getX() + "_" + edge.getY() + " :");
            for (Map.Entry<String, String> entry : map.entrySet()
                    ) {
                entr = entry.getKey();
                String[] split = entr.split(",");
                start = split[0];
                dest = split[1];
                if (!(start.equals(dest))) {
                    bufw.write(" + flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY());
                }
            }
            bufw.write(" - z <= " + edge.capacity);
            bufw.write("\n");
        }
    }

    private static void preservationConstrain(BufferedWriter bufw) throws IOException {
        String entr;
        String start;
        String dest;

        for (Map.Entry<String, String> entry : map.entrySet()
                ) {
            entr = entry.getKey();
            String[] split = entr.split(",");
            start = split[0];
            dest = split[1];
            if (!(start.equals(dest))) {
                int i = 0;
                for (Node node : nodes
                        ) {
                    if (!((node.getName().equals(start)) || node.getName().equals(dest))) {
                        bufw.write("\tP_flow_" + start + "_" + dest + "_" + i + ": ");
                        for (Edge edge : edges
                                ) {
                            if (edge.getX().equals(node.getName())) {
                                bufw.write(" - flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY());
                            }
                        }
                        for (Edge edge : edges) {
                            if (edge.getY().equals(node.getName())) {
                                bufw.write(" + flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY());
                            }
                        }
                        bufw.write(" = 0 \n");
                        i++;

                    }

                }
                bufw.write("\tP_flow_" + start + "_" + dest + "_sd: ");
                for (Edge edge : edges
                        ) {
                    if (edge.getY().equals(dest)) {
                        bufw.write(" - flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY());
                    }
                }
                for (Edge edge : edges
                        ) {
                    if (edge.getX().equals(start)) {
                        bufw.write(" + flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY());
                    }
                }
                bufw.write(" = 0\n");
            }
        }
    }


    private static void demandConstrain(BufferedWriter bufw) throws IOException {
        String entr;
        String start;
        String dest;
        for (Map.Entry<String, String> entry : map.entrySet()
                ) {
            entr = entry.getKey();
            String[] split = entr.split(",");
            start = split[0];
            dest = split[1];
            if (!(start.equals(dest))) {
                bufw.write("\tD_flow_" + start + "_" + dest + ": ");
                for (Node node : nodes
                        ) {
                    if (node.getName().equals(start)) {
                        for (Edge edge : node.getEdges()
                                ) {
                            bufw.write("flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY() + " + ");
                        }

                    }
                }
                bufw.write("  0 = " + entry.getValue());
                bufw.write("\n");

                bufw.write("\tR_flow_" + start + "_" + dest + ": ");
                for (Edge edge : edges
                        ) {
                    if (dest.equals(edge.getY())) {
                        bufw.write("flow_" + start + "_" + dest + "_" + edge.getX() + "_" + edge.getY() + " + ");
                    }
                }
                bufw.write("  0 = " + entry.getValue());
                bufw.write("\n");

            }
        }
    }

    private static void initializeHashMap(String demandeFile) throws IOException {
        BufferedReader inputStream = new BufferedReader(new FileReader(demandeFile));
        String thisLine;
        inputStream.readLine();
        while ((thisLine = inputStream.readLine()) != null) {
            String[] dem = thisLine.split("\\s+");
            map.put(dem[0], dem[1]);
        }
        inputStream.close();
    }


    private static void addNodesInOut() {
        for (Node nodea : nodes
                ) {
            for (Node nodeb : nodes
                    ) {
                for (Edge edge : nodea.getEdges()) {
                    if (nodeb.getName().equals(edge.getY())) {
                        nodea.addout(nodeb);
                        nodeb.addIn(nodea);
                    }
                }

            }
        }
    }

    private static void updateMap(String[] demandArray) {
        int i = 0;
        for (String entry : map.keySet()
                ) {
            map.put(entry, demandArray[i]);
            i++;
        }

    }

    private static FileWriter createFile() throws IOException {
        File file = new File("flowOutPut/"+i+"maxFlow.lp");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        i++;
        return fw;

    }

    public static void initializeRoutersLinks(String filePath) throws IOException {
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
                String[] ed = thisLine.split("\\s+");
                Edge edge = new Edge(ed[0], ed[1], Integer.parseInt(ed[2]));
                edges.add(edge);
            }
        }
        inputStream.close();
    }

    public static void addEdgestoNodes() {

        for (Node node : nodes
                ) {
            for (Edge ed : edges
                    ) {
                if (ed.getX().equals(node.getName())) {
                    node.add(ed);
                }
            }
        }
    }


    public static void addNodestoEdges() {
        for (Edge edge : edges
                ) {
            for (Node node : nodes
                    ) {
                if (edge.getX().equals(node.getName())) {
                    edge.setNodex(node);
                }
                if (edge.getY().equals(node.getName())) {
                    edge.setNodey(node);
                }

            }
        }
    }

    public static void printInOut() {
        for (Node node : nodes
                ) {
            for (Node n : node.getNodesout()
                    ) {
                System.out.println("Node " + node.getName() + " -> " + n.getName());

            }
        }

        for (Node node : nodes) {
            for (Node n : node.getNodesin()
                    ) {
                System.out.println("Node " + node.getName() + "<-" + n.getName());
            }
        }
    }
}
