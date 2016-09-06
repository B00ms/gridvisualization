package gridVisualization;

import java.io.IOException;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.commons.math3.ml.neuralnet.sofm.NeighbourhoodSizeFunction;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.w3c.dom.NodeList;

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
				System.out.println(nodeAttr);
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
	
	private void assignEdges(int nEdges){
		
	}
	
}
