package gridVisualization;

import java.awt.Container;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.graphicGraph.GraphicElement;

import scala.util.Random;


public class GraphLogic {

	private static Graph graph;
	private static String directory = "";

	public GraphLogic() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		//graphStream();
	}

	public static void graphStream(){
		System.out.println("graphStream");
		graph = new MultiGraph("mygraph");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
        String file = directory;
        try {
			FileSource fileSource = FileSourceFactory.sourceFor(file);

			fileSource.addSink(graph);
			fileSource.begin(file);
			fileSource.nextStep();
			graph = setNodeStyle(graph);
			graph = setEdgeStyle(graph);
			fileSource.end();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addNode(GraphicElement gfxNode, JList<String> nodeList, JList<String> edgeList){
		Node node1 = getGraph().getNode(gfxNode.getId());
		Node newNode = null;
		String attributes = nodeList.getModel().getElementAt(0);
		String[] attributesArray = attributes.split("\\s");
		switch(attributesArray[0]){
		case "CG": 
			newNode = graph.addNode(attributesArray[2]);
			break;
		case "C":
			newNode = graph.addNode(attributesArray[1]);
			break;
		case "IN":
			newNode = graph.addNode(attributesArray[1]);
			break;
		case "RG":
			newNode = graph.addNode(attributesArray[2]);
			break;
		case "Storage":
			newNode = graph.addNode(attributesArray[1]);
			break;
		}
		newNode = setAttributes(newNode, attributesArray);
		Edge edge = graph.addEdge(UUID.randomUUID().toString(), node1, newNode);
		String[] edgeAttr = ((DefaultListModel<String>)edgeList.getModel()).get(0).split("\\s");
		edge.setAttribute("edgeId", edgeAttr[1]);
		edge.setAttribute("reactance", edgeAttr[2]);
		edge.setAttribute("capacity", edgeAttr[3]);
		((DefaultListModel<String>)edgeList.getModel()).remove(0);
		
		((DefaultListModel<String>)nodeList.getModel()).remove(0);
			/*System.out.println("read " + node.getAttributeKeySet().toString());
		System.out.println(getGraph().getNode(gfxNode.getId()).getAttributeKeySet().toString());
		System.out.println(getGraph().getNode(gfxNode.getId()).getAttribute("ui.class").toString());
		((DefaultListModel<String>)nodeList.getModel()).remove(0);*/
	}
	
	public Node assignNode(GraphicElement gfxNode, JList<String> nodeList){
		Node node = getGraph().getNode(gfxNode.getId());
		String attributes = nodeList.getModel().getElementAt(0);
		String[] attributesArray = attributes.split("\\s");
		node = setAttributes(node, attributesArray);
		System.out.println("read " + node.getAttributeKeySet().toString());
		System.out.println(getGraph().getNode(gfxNode.getId()).getAttributeKeySet().toString());
		System.out.println(getGraph().getNode(gfxNode.getId()).getAttribute("ui.class").toString());
		((DefaultListModel<String>)nodeList.getModel()).remove(0);
		
		return node;
	}
	
	public Node setAttributes(Node node, String[] attributes){
		String nodeType = attributes[0];
		node.addAttribute("ui.label", node.getId());
		//node.addAttribute("ui.style", "fill-color: rgb(0,100,255);");
		
		switch(nodeType){
			case "CG":
				System.out.println("cg");
				//conv gen				
				node.addAttribute("ui.class", "ConventionalGenerator"); 
				node.addAttribute("subType", attributes[1]); //node subtype
			
				node.addAttribute("lowerGenLimit", attributes[2]); //lower gen limit
				node.addAttribute("upperGenLimit", attributes[3]);
				node.addAttribute("costCoefficient", attributes[4]);
			break;
			
			case "C":
				//consumer
				node.addAttribute("ui.class", "Consumer"); 
				node.addAttribute("nodeId", attributes[1]);
				node.addAttribute("consumptionPercentage", attributes[2]);
			break;
			
			case "IN":
				//inner node
				node.addAttribute("ui.class", "InnerNode"); 
				node.addAttribute("nodeId", attributes[1]);
			break;
			
			case "RG":
				//renewable gen
				node.addAttribute("ui.class", "RewGenerator"); 
				node.addAttribute("subType", attributes[1]); //node subtype
				node.addAttribute("nodeId", attributes[2]);
				node.addAttribute("maxGen", attributes[3]);
				node.addAttribute("cuirtailmentCost", attributes[4]);
				node.addAttribute("costCoefficient", attributes[5]);
				node.addAttribute("production", "0");
			break;
			
			case "Storage":
				//storage
				node.addAttribute("ui.class", "Storage"); 
				node.addAttribute("nodeId", attributes[1]);
				node.addAttribute("currentSoC", attributes[2]);
				node.addAttribute("maxSoC", attributes[3]);
				node.addAttribute("minSoC", attributes[4]);
				node.addAttribute("chMax", attributes[5]);
			break;
		}
		//node.addAttribute("ui.style", "shadow-color:red;");
		//node.addAttribute("nodeId", 123);
		//System.out.println(node.getAttributeKeySet().toString());
		return node;
	}

	private static Graph setNodeStyle(Graph graph){
		 java.util.Iterator<Node> nodeIterator = graph.getNodeIterator();
		 while (nodeIterator.hasNext()){
			 Node node = nodeIterator.next();
			 node.addAttribute("ui.style", "");
			 node.addAttribute("layout.weight", 20);

			 if (node.getAttribute("ui.class").equals("ConventionalGenerator")){
				 if ((boolean) node.getAttribute("failure"))
					 node.addAttribute("ui.style", "shadow-color:red;");
				 else
					 node.addAttribute("ui.style", "shadow-color:#339900;");

			 } else if (node.getAttribute("ui.class").equals("Consumer")){
				 double load = Double.parseDouble(node.getAttribute("load"));
				 double flow = Double.parseDouble(node.getAttribute("flow"));

				 if (Math.round(load)> Math.round(flow))
					 node.addAttribute("ui.style", "shadow-color:red;");
				 else
					 node.addAttribute("ui.style", "shadow-color:#339900;");
			 	}
			 }
		 return graph;
	}

	private static Graph setEdgeStyle(Graph graph) {

		 double maxCapacity = 0;
		 double minCapacity = 0.1;
		 double maxFlow = 0;
		 double minFlow = 0;

		 java.util.Iterator<Edge> edgesIterator = graph.getEdgeIterator();
		 while (edgesIterator.hasNext()){
			 Edge edge = edgesIterator.next();

			 if (maxCapacity < edge.getNumber("capacity"))
				 maxCapacity =  edge.getNumber("capacity");
			 else if (minCapacity > edge.getNumber("capacity"))
				 minCapacity = edge.getNumber("capacity");

			 if (maxFlow < edge.getNumber("flow"))
				 maxFlow =  edge.getNumber("flow");
			 else if (minFlow > edge.getNumber("flow"))
				 minFlow = edge.getNumber("flow");
		 }

		 edgesIterator = graph.getEdgeIterator();
		while (edgesIterator.hasNext()){
			Edge edge = edgesIterator.next();
			double flow = Double.parseDouble(edge.getAttribute("flow"));
			double capacity = Double.parseDouble(edge.getAttribute("capacity"));

			if(Math.abs(flow) > capacity) //Line is being overloaded
				edge.addAttribute("ui.style", "fill-color:red;");
			else {
				if(Math.abs(flow) > maxFlow/3)
					edge.addAttribute("ui.style", "fill-color:darkgoldenrod;"); //Orange
				else if(Math.abs(flow) < maxFlow/3)
					edge.addAttribute("ui.style", "fill-color:green;"); //dark green
				else if(Math.abs(flow) == 0)
					edge.addAttribute("ui.style", "fill-color:black;");
				else
					edge.addAttribute("ui.style", "fill-color:yellow;");
			}
		}
		return graph;
	}

	public Graph getGraph(){
		return graph;
	}
	
	public void setGraph(Graph graph){
		this.graph = graph;
	}

	public void loadGraph(String filename){
		System.out.println("new graph selected");
		graph = new MultiGraph("mygraph new graph super sexy");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");

        String file = directory+"/"+filename;
        try {
			FileSource fileSource = FileSourceFactory.sourceFor(file);

			fileSource.addSink(graph);
			fileSource.begin(file);
			fileSource.nextStep();
			graph = setNodeStyle(graph);
			graph = setEdgeStyle(graph);
			fileSource.end();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setDirectory(String directory){
		this.directory = directory;
	}
	
	public void assignNodes(int nOuterNodes, DefaultListModel<String> nodeListModel){
		
		Iterator<Node> it = graph.getNodeIterator();
		int nodeCounter = 0;
		Random rnd = new Random();
		
		while(it.hasNext()){
			Node node = it.next();
			
			if(node.getEdgeSet().size() >= 2 ){
				assignInnerNodes(node, nodeListModel);
			}else{
				int index = rnd.nextInt(nOuterNodes-1);
				String nodeAttr = nodeListModel.getElementAt(index);
				String[] arrayAttr = nodeAttr.split("\\s");
				nodeListModel.remove(index);
				switch (arrayAttr[0]) {
				case "CG":
					node.addAttribute("ui.class", "ConventionalGenerator"); 
					node.addAttribute("subType", arrayAttr[1]); //node subtype
					node.addAttribute("nodeId", arrayAttr[2]);
					
				
					node.addAttribute("lowerGenLimit", arrayAttr[2]); //lower gen limit
					node.addAttribute("upperGenLimit", arrayAttr[3]);
					node.addAttribute("costCoefficient", arrayAttr[4]);
					break;
				case "C":
					node.addAttribute("ui.class", "Consumer"); 
					node.addAttribute("nodeId", arrayAttr[1]);
					node.addAttribute("consumptionPercentage", arrayAttr[2]);
					break;
				case "RG":
					//renewable gen
					node.addAttribute("ui.class", "RewGenerator"); 
					node.addAttribute("subType", arrayAttr[1]); //node subtype
					node.addAttribute("nodeId", arrayAttr[2]);
					node.addAttribute("maxGen", arrayAttr[3]);
					node.addAttribute("cuirtailmentCost", arrayAttr[4]);
					node.addAttribute("costCoefficient", arrayAttr[5]);
					break;
				case "Storage":
					node.addAttribute("ui.class", "Storage"); 
					node.addAttribute("nodeId", arrayAttr[1]);
					node.addAttribute("currentSoC", arrayAttr[2]);
					node.addAttribute("maxSoC", arrayAttr[3]);
					node.addAttribute("minSoC", arrayAttr[4]);
					node.addAttribute("chMax", arrayAttr[5]);
					break;
				default:
					break;
				}
			}
			nodeCounter++;
		}
	}
	
	private void assignInnerNodes(Node node, DefaultListModel<String> nodeListModel){
		String nodeAttr = "";
		Random rnd = new Random();
		int index =  1;
		while(!nodeAttr.contains("IN")){
			nodeAttr = nodeListModel.getElementAt(index-1);
			index++;
		}
		
		nodeListModel.remove(index-1);
		String[] arrayAttr = nodeAttr.split("\\s");
		
		node.addAttribute("ui.class", "InnerNode");
		node.addAttribute("nodeId", arrayAttr[1]);
	}
	
	
	public void saveGraph(){
		
		try{
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("graph.csv"), "UTF-8"));
			
			Iterator<Node> nodeIt = graph.getNodeIterator();
			String cGens = "";
			String consumers = "";
			String inners = "";
			String rgens = "";
			String stors = "";
			
			while(nodeIt.hasNext()){
				Node node = nodeIt.next();
				
				switch (node.getAttribute("ui.class").toString()) {
				case "ConventionalGenerator":
					String subtype = node.getAttribute("subType").toString();
					String nodeId = node.getAttribute("nodeId").toString();
					String minGen = node.getAttribute("lowerGenLimit").toString();
					String maxGen = node.getAttribute("upperGenLimit").toString();
					String costCoef = node.getAttribute("costCoefficient").toString();
					cGens += "CG,"+subtype+","+nodeId+","+minGen+","+maxGen+","+costCoef+System.lineSeparator();
					break;
				case "Consumer":
					nodeId = node.getAttribute("nodeId").toString();
					String consPerc = node.getAttribute("consumptionPercentage").toString();
					consumers += "C,"+nodeId+","+consPerc+System.lineSeparator();
					break;
				case "InnerNode":
					nodeId = node.getAttribute("ui.label").toString();
					inners += "IN,"+nodeId+System.lineSeparator();
					break;
				case "RewGenerator":
					nodeId = node.getAttribute("nodeId").toString();
					subtype = node.getAttribute("subType").toString();
					maxGen = node.getAttribute("maxProduction").toString();
					String cuirtCost = node.getAttribute("cuirtailmentCost").toString();
					costCoef = node.getAttribute("costCoefficient").toString();
					rgens += "RG,"+subtype+","+nodeId+","+maxGen+","+cuirtCost+","+costCoef+System.lineSeparator();
					break;
				case "Storage":
					nodeId = node.getAttribute("nodeId").toString();
					String currentSoC = node.getAttribute("currentSoC").toString();
					String maxSoC = node.getAttribute("maxSoC").toString();
					String minSoC = node.getAttribute("minSoC").toString();
					String chMax = node.getAttribute("chMax").toString();
					stors += "Storage,"+nodeId+","+currentSoC+","+maxSoC+","+minSoC+","+chMax+System.lineSeparator();
					break;
				default:
					break;
				}

			}
			writer.write(cGens);
			writer.write(System.lineSeparator());
			writer.write(consumers);
			writer.write(System.lineSeparator());
			writer.write(inners);
			writer.write(System.lineSeparator());
			writer.write(rgens);
			writer.write(System.lineSeparator());
			writer.write(stors);
			writer.write(System.lineSeparator());
			
			Iterator<Edge> edgeIt = graph.getEdgeIterator();
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				
				String nodeOneId = edge.getNode0().getAttribute("nodeId").toString();
				String nodeTwoId = edge.getNode1().getAttribute("nodeId").toString();
				String reactance = edge.getAttribute("reactance").toString();
				String capacity = edge.getAttribute("capacity").toString();
				String line = "AE,"+nodeOneId+","+nodeTwoId+","+reactance+","+capacity+System.lineSeparator();
				writer.write(line);
				
			}
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
