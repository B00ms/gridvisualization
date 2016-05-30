package gridVisualization;

import java.io.IOException;
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


public class GraphLogic {
	
	private static Graph graph;
	
	public GraphLogic() {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		graphStream();
	}
	
	public static void graphStream(){
		System.out.println("graphStream");
		graph = new MultiGraph("mygraph"); 
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");    
        String file = "graphstate/graphstate0.dgs";
        try {
			FileSource fileSource = FileSourceFactory.sourceFor("E:/eclipse workspace/master thesis/GridVisualization/"+file);

			fileSource.addSink(graph);
			fileSource.begin("E:/eclipse workspace/master thesis/GridVisualization/"+file);
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
	
	public void loadGraph(String filename){
		System.out.println("new graph selected");
		graph = new MultiGraph("mygraph new graph super sexy"); 
	    graph.addAttribute("ui.quality");
	    graph.addAttribute("ui.antialias");    

        String file = "graphstate/"+filename;
        try {
			FileSource fileSource = FileSourceFactory.sourceFor("E:/eclipse workspace/master thesis/GridVisualization/"+file);

			fileSource.addSink(graph);
			fileSource.begin("E:/eclipse workspace/master thesis/GridVisualization/"+file);
			fileSource.nextStep();
			graph = setNodeStyle(graph);
			graph = setEdgeStyle(graph);
			fileSource.end();		
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
