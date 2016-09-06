package gridVisualization;

import java.util.Iterator;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import scala.util.Random;

public class GraphGenerator {
	
	public MultiGraph createModifiedWattsStrogatz(int numOuterNodes, int numInnerNodes, int numEdges){
		
		MultiGraph graph = new MultiGraph("radial");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet", "url(mySheet.css)");
		//N vertices, k edges per vertice, beta = regularity (0 to 1) 
		Generator graphGenerator = new WattsStrogatzGenerator(217, 2, 0.9);
		//Generator generator = new RandomE
		graphGenerator.addSink(graph);
		
		graphGenerator.begin();
		while(graphGenerator.nextEvents()){};
		graphGenerator.end();
		
		//makeGraphViable(graph, numOuterNodes, numInnerNodes);
		//graph.addn
			
		return graph;
		
	}
	
	private void makeGraphViable(MultiGraph graph, int numOuterNodes, int numInnerNodes) {
		
		Iterator<Node> it = graph.getNodeIterator();
		
		int nInner = 0;
		int nOuter = 0;
		
		while(it.hasNext()){
			Node node = it.next();
			
			if(node.getEdgeSet().size() >= 2)
				nInner++;
			else
				nOuter++;
		}
		
		if(nInner > numInnerNodes){
			it = graph.getNodeIterator();
			Random rand = new Random();
			int index;
			while(it.hasNext()){
				if(nInner == numInnerNodes)
					break;
				
				Node node = it.next();
				if(node.getEdgeSet().size() >= 2){
					while(graph.getNode(index = rand.nextInt(graph.getNodeCount())).getEdgeSet().size() <= 1);
					Node node2 = graph.getNode(index);
					
					graph.removeNode(node);
					graph.addEdge(String.valueOf(System.currentTimeMillis()/rand.nextFloat()), node, node2);
					nInner--;
				}
			}
		}
		
	}
}
