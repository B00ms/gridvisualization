package gridVisualization;

import java.util.Iterator;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;


public class GraphMetrics {

	/**
	 * As defined by 'The topological and electrical structure of power grids' by Hines, P. in 2010
	 * @param graph
	 * @return The Characteristic Path Length of the graph
	 */
	public double getCharacteristicPathPength(Graph graph){
		double L = 1 / ((double)graph.getNodeCount()*((double)graph.getNodeCount()-1));
		
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
		dijkstra.init(graph);
		
		for(Node node : graph){
			dijkstra.setSource(node);
			dijkstra.compute();
		}
		double sumPathLength = 0;
		for (Node node : graph){
			if(dijkstra.getPathLength(node) != Double.POSITIVE_INFINITY)
			sumPathLength += dijkstra.getPathLength(node);
		}
					
		L = L * sumPathLength;
		dijkstra.clear();
		return L;
	}
	
	/**
	 * As defined by 'The Ubiquity of Small-World Networks' by Telesford in 2011
	 * @param L
	 * @param graph
	 * @return
	 */
	public double getSmallWorldProperty(Graph graph){
			
		double LRand = 0;
		double CRand = 0;
		Graph randomGraph;
		
		for(int j = 0; j < 100; j++){
			randomGraph = new SingleGraph("Random graph");
			Generator gen = new RandomGenerator(Toolkit.averageDegree(graph));
			gen.addSink(randomGraph);
			gen.begin();
			for (int i = 0; i < graph.getNodeCount(); i++)
				gen.nextEvents();
			gen.end();
			
			Iterator<Edge> edgeIt = randomGraph.getEdgeIterator();
			while(edgeIt.hasNext()){
				Edge edge = edgeIt.next();
				edge.addAttribute("length",1);
			}
		
			LRand += this.getCharacteristicPathPength(randomGraph);
			CRand += Toolkit.averageClusteringCoefficient(randomGraph);
			
			if(randomGraph.getNodeCount() > graph.getNodeCount()){
				int limit = randomGraph.getNodeCount()-graph.getNodeCount();
				int i= 0;
				while(i < limit){
					Node node = Toolkit.randomNode(randomGraph);
					if(node.getDegree() > 1){
						randomGraph.removeNode(node);
						i++;
					}
				}
			}
			if(randomGraph.getEdgeCount() != graph.getEdgeCount()){
				Node node1 = null;
				Node node2 = null;
				int edgeLimit = randomGraph.getEdgeCount() - graph.getEdgeCount();
				
				if(edgeLimit < 0){
					//We've got to add some edges!
					while(edgeLimit != 0){
						while(node1 == node2){
							node1 = Toolkit.randomNode(randomGraph);
							node2 = Toolkit.randomNode(randomGraph);	
						}
						try{
							randomGraph.addEdge(String.valueOf(randomGraph.getEdgeCount()+1), node1, node2);
							edgeLimit +=1;
						}catch(Exception e){
							//bad code :( but easy code :),
							//You get enter this catch when trying to add an edge between two nodes that all ready exists.
							node1 = null;
							node2 = null;
						}
						
					}
				}else if(edgeLimit > 0){
					//We've got to remove some edges!	
					while(edgeLimit != 0){
						Edge edge = Toolkit.randomEdge(randomGraph);
						randomGraph.removeEdge(edge);
						edgeLimit -= 1;
					}
				}
			}
			randomGraph.clear();
		}
		LRand = LRand/20;
		CRand = CRand/20;
		double L = this.getCharacteristicPathPength(graph);
		double C = Toolkit.averageClusteringCoefficient(graph);
		
		double smallW = (C/CRand)/(L/LRand);	
		
		return smallW;
	}
	
	
	public void getBetweenessCentrality(Graph graph){
		BetweennessCentrality bcb = new BetweennessCentrality();
		bcb.setUnweighted();
		bcb.init(graph);
		bcb.compute();
	}

}
