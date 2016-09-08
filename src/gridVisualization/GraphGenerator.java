package gridVisualization;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphGenerator {
	
	public MultiGraph createModifiedWattsStrogatz(int numOuterNodes, int numInnerNodes, int numEdges, DefaultListModel<String> nodeList, DefaultListModel<String> edgeList){
		
		MultiGraph graph = new MultiGraph("radial");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
		//N vertices, k edges per vertice, beta = regularity (0 to 1) 
		Generator graphGenerator = new WattsStrogatzGenerator(numInnerNodes, 2, 0.9);

		graphGenerator.addSink(graph);
		graphGenerator.begin();
		while(graphGenerator.nextEvents()){};
		graphGenerator.end();
		
		setInnerNodes(graph);
		removeInnerNodesFromList(nodeList);
		System.out.println("Node count: " + graph.getNodeCount());
		setOuterNodes(graph, nodeList);
		setInnerEdges(graph, edgeList);
		setOuterEdges(graph, edgeList, numOuterNodes);
		
		System.out.println("Node count in graph: " + graph.getNodeCount());
		System.out.println("Edge count in graph: " + graph.getEdgeCount());
		System.out.println("Edges in input file: " + numEdges);
		System.out.println("Edges in list file: " + edgeList.size());
		return graph;
		
	}
	
	public void setInnerNodes(Graph graph){
		Iterator<Node> iter = graph.getNodeIterator();
		while(iter.hasNext()){
			Node node = iter.next();
			node.addAttribute("ui.class", "InnerNode");
		}
	}
	
	private void removeInnerNodesFromList(DefaultListModel<String> nodeList){
		
		for(int i=0; i < nodeList.size(); i++){
			if(nodeList.get(i).contains("IN ")){
				nodeList.remove(i);
				i=0;
			}
		}
	}
	
	private void setInnerEdges(Graph graph, DefaultListModel<String> edgeList){
		
		Iterator<Edge> it = graph.getEdgeIterator();
		Random random = new Random();
		
		while(it.hasNext()){
			Edge edge = it.next();
			int randomEdgeIndex = random.nextInt(edgeList.size());
			String[] edgeAttr = edgeList.get(randomEdgeIndex).split("\\s");
			edge.addAttribute("reactance", edgeAttr[1]);
			edge.addAttribute("capacity", edgeAttr[2]);
			edgeList.remove(randomEdgeIndex);
		}
	}
	
	private void setOuterNodes(Graph graph, DefaultListModel<String> nodeList){
		
		int i = 0;
		while(nodeList.size() > i){
			
			String id = String.valueOf(graph.getNodeCount()+1);
			graph.addNode(id);
			Node node1 = graph.getNode(id);
			
			String[] attr = nodeList.get(i).split("\\s");
			switch(attr[0]){
			case "CG":
				node1.addAttribute("ui.class", "ConventionalGenerator");
				node1.addAttribute("subType", attr[1]); //node subtype
			
				node1.addAttribute("lowerGenLimit", attr[2]); //lower gen limit
				node1.addAttribute("upperGenLimit", attr[3]);
				node1.addAttribute("costCoefficient", attr[4]);
				nodeList.remove(i);
				i = 0;
				break;
			case "C":
				node1.addAttribute("ui.class", "Consumer");
				node1.addAttribute("consumptionPercentage", attr[2]);
				nodeList.remove(i);
				i = 0;
				break;
			case "RG":
				node1.addAttribute("ui.class", "RewGenerator");
				node1.addAttribute("subType", attr[1]); //node subtype
				node1.addAttribute("maxGen", attr[3]);
				node1.addAttribute("cuirtailmentCost", attr[4]);
				node1.addAttribute("costCoefficient", attr[5]);
				nodeList.remove(i);
				i = 0;
				break;
			case "Storage":
				node1.addAttribute("ui.class", "Storage");
				node1.addAttribute("currentSoC", attr[2]);
				node1.addAttribute("maxSoC", attr[3]);
				node1.addAttribute("minSoC", attr[4]);
				node1.addAttribute("chMax", attr[5]);
				nodeList.remove(i);
				i = 0;
			break;
			}
		}
	}
	
	private void setOuterEdges(Graph graph, DefaultListModel<String> edgesList, int numOuterNodes){
		
		Iterator<Node> iterator = graph.getNodeIterator();
		List<Node> innerNodes = new ArrayList<Node>();
		List<Node> outerNodes = new ArrayList<Node>();
		Random rand = new Random();
		
		while(iterator.hasNext()){
			Node node = iterator.next();
			
			if(node.getAttribute("ui.class").equals("InnerNode"))
				innerNodes.add(node);
			else
				outerNodes.add(node);
				
		}
		
		for(int i=0; i < edgesList.size(); i++){
			Node node1 = null;
			Node node2 = null;
			
			int indx = rand.nextInt(outerNodes.size());
			node1 = outerNodes.get(indx);
			outerNodes.remove(indx);
			
			indx = rand.nextInt(innerNodes.size());
			node2 = innerNodes.get(indx);
			
			System.out.println(node1.getAttribute("ui.class").toString());
			System.out.println(node2.getAttribute("ui.class").toString());
			
			double minimumCapacity = 0;
			switch(node1.getAttribute("ui.class").toString()){
			case "Storage":
				minimumCapacity = (Integer.valueOf(node1.getAttribute("chMax").toString()) * 0.87);
				break;
			case "ConventionalGenerator":
				minimumCapacity = (Double.valueOf(node1.getAttribute("upperGenLimit").toString()));
				break;
			case "RewGenerator":
				minimumCapacity = (Integer.valueOf(node1.getAttribute("maxGen").toString()));
				break;
			case "Consumer":
				minimumCapacity = 0;
				break;
			}
			
			String[] edgeAttr = edgesList.get(i).split("\\s");
			int capacity = Integer.valueOf(edgeAttr[3]);
			if(capacity >= minimumCapacity && node1.getId() != node2.getId()){
				
				graph.addEdge(String.valueOf(graph.getEdgeCount()+1), node1, node2);
				edgesList.remove(i);
				i = 0;
			}
			if(outerNodes.isEmpty())
				break;
		}
		
/*		for(int i = 0; i < edgesList.size(); i++){
			int indx = rand.nextInt(innerNodes.size());
			Node node1 = innerNodes.get(indx);
			innerNodes.remove(indx);
			
			indx = rand.nextInt(innerNodes.size());
			Node node2 = innerNodes.get(indx);
			innerNodes.remove(indx);
			
			graph.addEdge(String.valueOf(graph.getEdgeCount()+1), node1, node2);
			i=0;
			edgesList.remove(i);
		}*/

	}

}
