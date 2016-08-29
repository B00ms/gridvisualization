package gridVisualization;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.WattsStrogatzGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

public class GraphGenerator {
	
	public MultiGraph generateRadialGraph(){
		
		MultiGraph graph = new MultiGraph("radial");
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");
	    graph.addAttribute("ui.stylesheet:url(mysheet.css)");
		//N vertices, k edges per vertice, beta = regularity (0 to 1) 
		Generator graphGenerator = new WattsStrogatzGenerator(207, 2, 0.8);
		graphGenerator.addSink(graph);
		
		graphGenerator.begin();
		while(graphGenerator.nextEvents()){};
		graphGenerator.end();
		
		return graph;
		
	}
	

}
