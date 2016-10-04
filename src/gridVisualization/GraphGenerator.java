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
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.PreferentialAttachmentGenerator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphGenerator {
	
	public SingleGraph createModifiedWattsStrogatz(int numOuterNodes, int numInnerNodes, int numEdges, DefaultListModel<String> nodeList, DefaultListModel<String> edgeList){
		
		SingleGraph graph = new SingleGraph("radial");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
		//N vertices, k edges per vertice, beta = regularity (0 to 1) 
		Generator graphGenerator = new WattsStrogatzGenerator(numInnerNodes, 2, 0.5);

		graphGenerator.addSink(graph);
		graphGenerator.begin();
		while(graphGenerator.nextEvents()){};
		graphGenerator.end();
		
		setInnerNodes(graph, nodeList);
		System.out.println("Node count: " + graph.getNodeCount());
		setOuterNodes(graph, nodeList);
		setInnerEdges(graph, edgeList);
		setOuterEdges(graph, edgeList, numOuterNodes);
		
		Iterator<Edge> edgeIt = graph.getEdgeIterator();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			edge.addAttribute("length", 1);
		}
		
		System.out.println("Node count in graph: " + graph.getNodeCount());
		System.out.println("Edge count in graph: " + graph.getEdgeCount());
		System.out.println("Edges in input file: " + numEdges);
		System.out.println("Edges in list file: " + edgeList.size());
		return graph;
		
	}
	
	public SingleGraph createPreferentialAttachmentGraph(int numOuterNodes, int numInnerNodes, int numEdges, DefaultListModel<String> nodeList, DefaultListModel<String> edgeList){
		
		SingleGraph graph = new SingleGraph("Preferential");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
	    
	    Generator gen = new BarabasiAlbertGenerator(1);
	    
	    gen.addSink(graph);
	    gen.begin();
	    
	    for(int i = 0; i < numInnerNodes-2; i++){
	    	gen.nextEvents();
	    }
	    gen.end();
	    System.out.println(graph.getNodeCount());
	    System.out.println(graph.getEdgeCount());
	    setInnerNodes(graph, nodeList);
	    setOuterNodes(graph, nodeList);
	    setInnerEdges(graph, edgeList);
	    setOuterEdges(graph, edgeList, numOuterNodes);
	    
		

		return graph;
	}
	
	public void setInnerNodes(Graph graph, DefaultListModel<String> nodeList){
		
		List<String> innerList = new ArrayList<String>();
		for(int i=0; i < nodeList.size(); i++){
			if(nodeList.get(i).contains("IN ")){
				String[] edgeAttr = nodeList.get(i).split("\\s");
				innerList.add(edgeAttr[1]);
				nodeList.remove(i);
				i=0;
			}
		}
		
		int innerIndex = 0;
		Iterator<Node> iter = graph.getNodeIterator();
		while(iter.hasNext()){
			Node node = iter.next();
			node.addAttribute("ui.label", innerList.get(innerIndex));
			node.addAttribute("nodeId", innerList.get(innerIndex));
			node.addAttribute("ui.class", "InnerNode");
			innerIndex++;
		}
	}
	
	private void setInnerEdges(Graph graph, DefaultListModel<String> edgeList){
		Iterator<Edge> it = graph.getEdgeIterator();
		Random random = new Random();
		List<String> innerEdges = new ArrayList<String>();
		for(int i = 0; i < edgeList.size(); i++){
			String[] edgeAttr = edgeList.get(i).split("\\s");
			if(!edgeAttr[4].equals("0.0001"))
				innerEdges.add(edgeList.get(i));
		}
		
		List<String> addedEdges = new ArrayList<String>();
		while(it.hasNext()){
				Edge edge = it.next();
				if(innerEdges.size() ==0)
					break;
				
				int randomEdgeIndex = random.nextInt(innerEdges.size());
				String[] edgeAttr = innerEdges.get(randomEdgeIndex).split("\\s");
				edge.addAttribute("edgeId", edgeAttr[1]);
				edge.addAttribute("flow", "0");
				edge.addAttribute("reactance", edgeAttr[2]);
				edge.addAttribute("capacity", edgeAttr[3]);
				addedEdges.add(innerEdges.get(randomEdgeIndex));
				innerEdges.remove(randomEdgeIndex);
		}
		System.out.println("used edges:");
		for(int i =0; i < edgeList.size(); i++){
			String edgeAttr = edgeList.get(i).split("\\s")[1];
			for(int j =0; j < addedEdges.size(); j++){
				if(edgeAttr.equals(addedEdges.get(j).split("\\s")[1])){
					edgeList.remove(i);
					i = 0;
					break;
				}
			}
		}
		System.out.println("END USED EDGES:");
	}
	
	private void setOuterNodes(Graph graph, DefaultListModel<String> nodeList){
		
		int i = 0;
		while(nodeList.size() > i){
			
			String id = String.valueOf(graph.getNodeCount());
			graph.addNode(id);
			Node node1 = graph.getNode(id);
			String[] attr = nodeList.get(i).split("\\s");
			switch(attr[0]){
			case "CG":
				node1.addAttribute("ui.class", "ConventionalGenerator");
				node1.addAttribute("subType", attr[1]); //node subtype
				node1.addAttribute("nodeId", attr[2]);
				node1.addAttribute("ui.label", attr[2]);
				node1.addAttribute("lowerGenLimit", attr[3]); //lower gen limit
				node1.addAttribute("upperGenLimit", attr[4]);
				node1.addAttribute("costCoefficient", attr[5]);
				node1.addAttribute("production", "0");
				nodeList.remove(i);
				i = 0;
				break;
			case "C":
				node1.addAttribute("ui.class", "Consumer");
				node1.addAttribute("nodeId", attr[1]);
				node1.addAttribute("ui.label", attr[1]);
				node1.addAttribute("consumptionPercentage", attr[2]);
				node1.addAttribute("load", "0");
				nodeList.remove(i);
				i = 0;
				break;
			case "RG":
				node1.addAttribute("ui.class", "RenewableGenerator");
				node1.addAttribute("subType", attr[1]); //node subtype
				node1.addAttribute("nodeId", attr[2]); //node subtype
				node1.addAttribute("ui.label", attr[2]);
				node1.addAttribute("maxProduction", attr[3]);
				node1.addAttribute("minProduction", "0");
				node1.addAttribute("cuirtailmentCost", attr[4]);
				node1.addAttribute("costCoefficient", attr[5]);
				node1.addAttribute("production", "0");
				nodeList.remove(i);
				i = 0;
				break;
			case "Storage":
				node1.addAttribute("ui.class", "Storage");
				node1.addAttribute("nodeId", attr[1]);
				node1.addAttribute("ui.label", attr[1]);
				node1.addAttribute("currentSoC", attr[2]);
				node1.addAttribute("maxSoC", attr[3]);
				node1.addAttribute("minSoC", attr[4]);
				node1.addAttribute("chMax", attr[5]);
				node1.addAttribute("chargeEfficiency", "0.87");
				node1.addAttribute("dischargeEfficiency", "0.87");
				node1.addAttribute("status", "N/A");
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
		
		List<String> usedEdges = new ArrayList<String>();
		
		while(iterator.hasNext()){
			Node node = iterator.next();
			
			if(node.getAttribute("ui.class").equals("InnerNode"))
				innerNodes.add(node);
			else
				outerNodes.add(node);
				
		}
		
		List<String> dummyEdges = new ArrayList<String>();
		for(int i = 0; i < edgesList.getSize(); i++){
			String[] edgeAttr = edgesList.get(i).split("\\s");
			if(edgeAttr[4].equals("0.0001")){
				dummyEdges.add(edgesList.get(i));
			}
		}
		
		
		for(int j = 0; j < outerNodes.size(); j++){
			Node node1 = outerNodes.get(j);
			for(int i=0; i < dummyEdges.size(); i++){
			
				Node node2 = null;
				
				int indx = rand.nextInt(outerNodes.size());
				indx = rand.nextInt(innerNodes.size());
				node2 = innerNodes.get(indx);
		
				double minimumCapacity = 0;
				switch(node1.getAttribute("ui.class").toString()){
				case "Storage":
					minimumCapacity = (Integer.valueOf(node1.getAttribute("chMax").toString()) * 0.87);
					break;
				case "ConventionalGenerator":
					minimumCapacity = (Double.valueOf(node1.getAttribute("upperGenLimit").toString()));
					break;
				case "RenewableGenerator":
					minimumCapacity = (Integer.valueOf(node1.getAttribute("maxProduction").toString()));
					break;
				case "Consumer":
					minimumCapacity = 800;
					break;
				}
				
				String[] edgeAttr = dummyEdges.get(i).split("\\s");
				int capacity = Integer.valueOf(edgeAttr[5]);
				
				if((capacity == minimumCapacity && node1.getId() != node2.getId()) ){
					Edge edge = null;
					if(node1.getAttribute("ui.class").toString().equals("Consumer")){
						edge = graph.addEdge(String.valueOf(graph.getEdgeCount()+1), node2, node1);
						edge.addAttribute("node1Id", edgeAttr[2]);
						edge.addAttribute("node2Id", edgeAttr[1]);
					}else{
						edge = graph.addEdge(String.valueOf(graph.getEdgeCount()+1), node1, node2);
						edge.addAttribute("node1Id", edgeAttr[1]);
						edge.addAttribute("node2Id", edgeAttr[2]);
					}
					
					edge.addAttribute("edgeId", edgeAttr[1]);
					edge.addAttribute("flow", "0");
					edge.addAttribute("reactance", edgeAttr[4]);	
					edge.addAttribute("capacity", edgeAttr[5]);
					usedEdges.add(dummyEdges.get(i));
					dummyEdges.remove(i);		
					i = 0;
					break;
				}else if(node1.getAttribute("ui.class").toString().equals("Storage") && capacity >= minimumCapacity && node1.getId() != node2.getId()){
					Edge edge = null;
					edge = graph.addEdge(String.valueOf(graph.getEdgeCount()+1), node1, node2);
					
					edge.addAttribute("edgeId", edgeAttr[1]);
					edge.addAttribute("flow", "0");
					edge.addAttribute("reactance", edgeAttr[4]);
					edge.addAttribute("capacity", edgeAttr[5]);
					usedEdges.add(dummyEdges.get(i));
					dummyEdges.remove(i);			
					i = 0;
					break;
				}
			}
			if(outerNodes.isEmpty())
				break;
			
		}
				
		for(int i = 0; i < edgesList.size(); i++){
			for(int j = 0; j < usedEdges.size(); j++){
				if(edgesList.get(i).equals(usedEdges.get(j))){
					edgesList.remove(i);
					i = 0;
					break;
				}
			}
		}
	}

}
