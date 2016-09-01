package gridVisualization;

import java.util.Iterator;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphGenerator {
	
	public MultiGraph createModifiedWattsStrogatz(int numOuterNodes, int numInnerNodes, int numEdges){
		
		MultiGraph graph = new MultiGraph("radial");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
		//N vertices, k edges per vertice, beta = regularity (0 to 1) 
		Generator graphGenerator = new WattsStrogatzGenerator(252, 2, 0.9);
		graphGenerator.addSink(graph);
		
		graphGenerator.begin();
		while(graphGenerator.nextEvents()){};
		graphGenerator.end();
		
		graph = ensureGraphIsViable(graph, numOuterNodes, numInnerNodes, numEdges);
		
		return graph;
		
	}
	
	
	private MultiGraph ensureGraphIsViable(MultiGraph graph, int numOuterNodes, int numInnerNodes, int numEdges) {
		
		Iterator<Node> it = graph.getNodeIterator();
		System.out.println(numOuterNodes);
		System.out.println(numInnerNodes);
		System.out.println(numEdges);
		
		int innerNodes = 0;
		int outerNodes = 0; //this has the equal the sum of the amount of storage, generators, consumers nodes.
		while(it.hasNext()){
			Node node = it.next();
			
			if (node.getEdgeSet().size() < 2){
				outerNodes++;
			} else{
				innerNodes++;
			}
		}
		
		System.out.println(outerNodes);
		System.out.println(innerNodes);
		
		if(outerNodes < numOuterNodes){
			//TODO: not enough outter nodes.
		}
			
		return graph;	
	}
	

}
