package gridVisualization;

import java.awt.Container;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.graphstream.algorithm.Toolkit;
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
			newNode = graph.addNode(UUID.randomUUID().toString());
			newNode.addAttribute("ui.class", "ConventionalGenerator");
			newNode.addAttribute("subType", attributesArray[1]); //node subtype
			newNode.addAttribute("nodeId", attributesArray[2]);
			newNode.addAttribute("ui.label", attributesArray[2]);
			newNode.addAttribute("lowerGenLimit", attributesArray[3]); //lower gen limit
			newNode.addAttribute("upperGenLimit", attributesArray[4]);
			newNode.addAttribute("costCoefficient", attributesArray[5]);
			newNode.addAttribute("production", "0");
			break;
		case "C":
			newNode = graph.addNode(UUID.randomUUID().toString());
			newNode.addAttribute("ui.class", "Consumer");
			newNode.addAttribute("nodeId", attributesArray[1]);
			newNode.addAttribute("ui.label", attributesArray[1]);
			newNode.addAttribute("consumptionPercentage", attributesArray[2]);
			newNode.addAttribute("load", "0");
			break;
		case "IN":
			newNode = graph.addNode(UUID.randomUUID().toString());
			newNode.addAttribute("ui.class", "InnerNode"); 
			newNode.addAttribute("ui.label", attributesArray[1]); //node subtype
			newNode.addAttribute("nodeId", attributesArray[1]); //node subtype
			break;
		case "RG":
			newNode = graph.addNode(UUID.randomUUID().toString());
			newNode.addAttribute("ui.class", "RenewableGenerator");
			newNode.addAttribute("subType", attributesArray[1]); //node subtype
			newNode.addAttribute("nodeId", attributesArray[2]); //node subtype
			newNode.addAttribute("ui.label", attributesArray[2]);
			newNode.addAttribute("maxProduction", attributesArray[3]);
			newNode.addAttribute("minProduction", "0");
			newNode.addAttribute("cuirtailmentCost", attributesArray[4]);
			newNode.addAttribute("costCoefficient", attributesArray[5]);
			newNode.addAttribute("production", "0");
			break;
		case "Storage":
			newNode = graph.addNode(UUID.randomUUID().toString());
			newNode.addAttribute("ui.class", "Storage");
			newNode.addAttribute("nodeId", attributesArray[1]);
			newNode.addAttribute("ui.label", attributesArray[1]);
			newNode.addAttribute("currentSoC", attributesArray[2]);
			newNode.addAttribute("maxSoC", attributesArray[3]);
			newNode.addAttribute("minSoC", attributesArray[4]);
			newNode.addAttribute("chMax", attributesArray[5]);
			newNode.addAttribute("chargeEfficiency", "0.87");
			newNode.addAttribute("dischargeEfficiency", "0.87");
			newNode.addAttribute("status", "N/A");
			break;
		}
		//newNode = setAttributes(newNode, attributesArray);
		Edge edge;
		if(!attributesArray[0].equals("storage") && !attributesArray[0].equals("C"))
			edge = graph.addEdge(UUID.randomUUID().toString(), newNode, node1);
		else
			edge = graph.addEdge(UUID.randomUUID().toString(), node1, newNode);
		String[] edgeAttr = ((DefaultListModel<String>)edgeList.getModel()).get(0).split("\\s");
		edge.addAttribute("edgeId", edgeAttr[1]);
		edge.addAttribute("node1Id", edgeAttr[2]);
		edge.addAttribute("node2Id", edgeAttr[3]);
		edge.addAttribute("reactance", edgeAttr[4]);
		edge.addAttribute("capacity", edgeAttr[5]);

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
		//node.addAttribute("ui.label", node.());
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
				node.addAttribute("ui.class", "RenewableGenerator"); 
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
				node.addAttribute("flow", "0");
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
			 node.addAttribute("layout.weight", 10);

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
			 edge.addAttribute("layout.weight", 2);

			 if (maxCapacity < edge.getNumber("capacity"))
				 maxCapacity =  (edge.getNumber("capacity")*0.87)*2.5;
			 else if (minCapacity > edge.getNumber("capacity"))
				 minCapacity = (edge.getNumber("capacity")*0.87)*2.5;

			 if (maxFlow < edge.getNumber("flow"))
				 maxFlow =  edge.getNumber("flow");
			 else if (minFlow > edge.getNumber("flow"))
				 minFlow = edge.getNumber("flow");
		 }

		edgesIterator = graph.getEdgeIterator();
		while (edgesIterator.hasNext()){
			Edge edge = edgesIterator.next();
			double flow = Double.parseDouble(edge.getAttribute("flow"));
			double capacity = (Double.parseDouble(edge.getAttribute("capacity"))*0.87)*2.5;

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
	    
	    if(!directory.isEmpty()){
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
	}
	
	public void loadGraph(JList<String> nodeList, JList<String> edgeList){
		graph = new MultiGraph("mygraph new graph super sexy");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
		for(int i = 0; i < nodeList.getModel().getSize(); i++){
			String[] arrayAttr = ((DefaultListModel<String>)nodeList.getModel()).get(i).split("\\s");	

		    
			//String id = String.valueOf(arrayAttr[2]);
			//graph.addNode(id);
			Node node;
			
			switch (arrayAttr[0]) {
			case "CG":
				graph.addNode(arrayAttr[2]);
				node = graph.getNode(arrayAttr[2]);
				node.addAttribute("ui.class", "ConventionalGenerator"); 
				node.addAttribute("ui.label", arrayAttr[2]);
				node.addAttribute("subType", arrayAttr[1]); //node subtype
				node.addAttribute("nodeId", arrayAttr[2]);
				node.addAttribute("lowerGenLimit", arrayAttr[3]); //lower gen limit
				node.addAttribute("upperGenLimit", arrayAttr[4]);
				node.addAttribute("costCoefficient", arrayAttr[5]);
				node.addAttribute("production", "0");
				break;
			case "C":
				graph.addNode(arrayAttr[1]);
				node = graph.getNode(arrayAttr[1]);
				node.addAttribute("ui.class", "Consumer"); 
				node.addAttribute("nodeId", arrayAttr[1]);
				node.addAttribute("ui.label", arrayAttr[1]);
				node.addAttribute("consumptionPercentage", arrayAttr[2]);
				node.addAttribute("load", "0");
				node.addAttribute("flow", "0");
				break;
			case "RG":
				//renewable gen
				graph.addNode(arrayAttr[2]);
				node = graph.getNode(arrayAttr[2]);
				node.addAttribute("ui.class", "RenewableGenerator"); 
				node.addAttribute("subType", arrayAttr[1]); //node subtype
				node.addAttribute("nodeId", arrayAttr[2]);
				node.addAttribute("ui.label", arrayAttr[2]);
				node.addAttribute("maxProduction", arrayAttr[3]);
				node.addAttribute("cuirtailmentCost", arrayAttr[4]);
				node.addAttribute("costCoefficient", arrayAttr[5]);
				node.addAttribute("production", "0");
				break;
			case "Storage":
				graph.addNode(arrayAttr[1]);
				node = graph.getNode(arrayAttr[1]);
				node.addAttribute("ui.class", "Storage"); 
				node.addAttribute("nodeId", arrayAttr[1]);
				node.addAttribute("ui.label", arrayAttr[1]);
				node.addAttribute("currentSoC", arrayAttr[2]);
				node.addAttribute("maxSoC", arrayAttr[3]);
				node.addAttribute("minSoC", arrayAttr[4]);
				node.addAttribute("chMax", arrayAttr[5]);
				node.addAttribute("chargeEfficiency", "0.87");
				node.addAttribute("dischargeEfficiency", "0.87");
				node.addAttribute("status", "N/A");
				break;
			case "IN":
				graph.addNode(arrayAttr[1]);
				node = graph.getNode(arrayAttr[1]);

				node.addAttribute("nodeId", arrayAttr[1]);
				node.addAttribute("ui.label", arrayAttr[1]);
				node.addAttribute("ui.class", "InnerNode");
				break;
			default:
				break;
			}
		}
		
		for(int i = 0; i < edgeList.getModel().getSize(); i++){
			String[] attr = ((DefaultListModel<String>)edgeList.getModel()).get(i).split("\\s");
		
/*			Iterator<Node> it = graph.getNodeIterator();
			Iterator<Node> it2 = graph.getNodeIterator();*/
			
			graph.addEdge(String.valueOf(i), attr[2], attr[3]);
			Edge edge = graph.getEdge(i);
			edge.addAttribute("edgeId", attr[1]);
			edge.addAttribute("flow", "0");
			edge.addAttribute("reactance", attr[4]);
			edge.addAttribute("capacity", attr[5]);
			edge.addAttribute("node1Id", attr[2]);
			edge.addAttribute("node2Id", attr[3]);
			edge.addAttribute("length", 1);
			
/*			while(it.hasNext()){
				Node node1 = it.next();
				it2 = graph.getNodeIterator();
				String node1Id = node1.getAttribute("nodeId");
				while(it2.hasNext()){
					Node node2 = it2.next();
					String node2Id = node2.getAttribute("nodeId");
					System.out.println(node1Id + " " + node2Id);
					if(node1Id == attr[2] && node2Id == attr[3])
						graph.addEdge(UUID.randomUUID().toString(), node1, node2);
				}
			}*/
		}
		System.out.println(graph.getEdgeCount());
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
					node.addAttribute("lowerGenLimit", arrayAttr[3]); //lower gen limit
					node.addAttribute("upperGenLimit", arrayAttr[4]);
					node.addAttribute("costCoefficient", arrayAttr[5]);
					break;
				case "C":
					node.addAttribute("ui.class", "Consumer"); 
					node.addAttribute("nodeId", arrayAttr[1]);
					node.addAttribute("consumptionPercentage", arrayAttr[2]);
					break;
				case "RG":
					//renewable gen
					node.addAttribute("ui.class", "RenewableGenerator"); 
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
	
	@SuppressWarnings("unused")
	public void calculateMetrics(){
		
		GraphMetrics gMetrics = new GraphMetrics();
		double avgDegree= Toolkit.averageDegree(graph);
		int[] degreeDistribution = Toolkit.degreeDistribution(graph);
		
		gMetrics.getBetweenessCentrality(graph);
		for(Node node : graph){
			System.out.print(node.getAttribute("nodeId").toString() + " ");
			System.out.println(node.getAttribute("Cb").toString());
		}
		//characteristic path
		double L = gMetrics.getCharacteristicPathPength(graph);
		double smallWorldProp = gMetrics.getSmallWorldProperty(graph);
		double avgClusteringCoefficient = 	Toolkit.averageClusteringCoefficient(graph);
		double diameter = Toolkit.diameter(graph);
		
		System.out.println("avgDegree " + avgDegree);
		System.out.println("degreeDist ");
		for(Integer degree : degreeDistribution)
			System.out.print(degree + " ");
		System.out.println();
		System.out.println("L " +L);
		System.out.println("small world " + smallWorldProp);
		System.out.println("clusting " + avgClusteringCoefficient);
		System.out.println("diameter " + diameter);
	}
	
	public void saveGraph(){
		
		try{
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("graph.csv"), "UTF-8"));
			
			//Iterator<Node> nodeIt = graph.getNodeIterator();
			
			TreeMap<Integer, Node> nodeTreeMap = new TreeMap<>();
			
			for(Node node:  graph){
				nodeTreeMap.put(Integer.valueOf(node.getAttribute("nodeId")), node);
			}
			
			String cGens = "";
			String consumers = "";
			String inners = "";
			String rgens = "";
			String stors = "";
			
			Iterator<Integer> nodeIt = nodeTreeMap.keySet().iterator();
			
			while(nodeIt.hasNext()){
//				Node node = nodeIt.next();
				Node node = nodeTreeMap.get(nodeIt.next());
				String subtype = "";
				String nodeId = "";
				String minGen = "";
				String maxGen = "";
				String costCoef = "";
				
				String consPerc = ""; 
				
				String cuirtCost = "";
				
				String currentSoC = "";
				String maxSoC = "";
				String minSoC = "";
				String chMax = "";
				
				switch (node.getAttribute("ui.class").toString()) {
				case "ConventionalGenerator":
					subtype = node.getAttribute("subType").toString();
					nodeId = node.getAttribute("nodeId").toString();
					minGen = node.getAttribute("lowerGenLimit").toString();
					maxGen = node.getAttribute("upperGenLimit").toString();
					costCoef = node.getAttribute("costCoefficient").toString();
					cGens += "CG,"+subtype+","+nodeId+","+minGen+","+maxGen+","+costCoef+System.lineSeparator();
					break;
				case "Consumer":
					nodeId = node.getAttribute("nodeId").toString();
					consPerc = node.getAttribute("consumptionPercentage").toString();
					consumers += "C,"+nodeId+","+consPerc+System.lineSeparator();
					break;
				case "InnerNode":
					nodeId = node.getAttribute("ui.label").toString();
					inners += "IN,"+nodeId+System.lineSeparator();
					break;
				case "RenewableGenerator":
					nodeId = node.getAttribute("nodeId").toString();
					subtype = node.getAttribute("subType").toString();
					maxGen = node.getAttribute("maxProduction").toString();
					cuirtCost = node.getAttribute("cuirtailmentCost").toString();
					costCoef = node.getAttribute("costCoefficient").toString();
					rgens += "RG,"+subtype+","+nodeId+","+maxGen+","+cuirtCost+","+costCoef+System.lineSeparator();
					break;
				case "Storage":
					nodeId = node.getAttribute("nodeId").toString();
					currentSoC = node.getAttribute("currentSoC").toString();
					maxSoC = node.getAttribute("maxSoC").toString();
					minSoC = node.getAttribute("minSoC").toString();
					chMax = node.getAttribute("chMax").toString();
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
				System.out.println(nodeOneId + " "+ nodeTwoId);
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
