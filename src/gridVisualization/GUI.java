package gridVisualization;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.SpringLayout.Constraints;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FileChooserUI;

import org.bouncycastle.asn1.cms.Time;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.stylesheet.Selector;
import org.graphstream.ui.layout.LayoutRunner;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class GUI {

	private Viewer viewer;
	private ViewPanel view;
	private GraphLogic graphLogic;
	private GraphGenerator graphGenerator = new GraphGenerator();
	private JFrame myFrame = new JFrame("Grid Visualization");
	private JComboBox<String> timestepDropdown;
	private int timeStepSelectionIndex = 0;
	private JPanel informationPanel;
	public enum VIEW_MODE {GRAPH_INSPECTION, GRAPH_GENERATION};
	VIEW_MODE viewMode = VIEW_MODE.GRAPH_INSPECTION;
	
	
	JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel selectionInfoPanel;

	private JPanel convAndRewGenerationInfoPanel;
	private JPanel storageInfoPanel;
	private JPanel consumerInfoPanel;
	private JPanel edgeInfoPanel;
	private Container generationPanel;

	// For all nodes
	private Label lblIdentifier = new Label();
	private Label lblType = new Label();

	// Renewable and conventional generator nodes
	private Label lblProduction = new Label();
	private Label lblMaxProduction = new Label();
	private Label lblMinimumProduction = new Label();
	private Label lblFailure = new Label();
	private Label lblSubType = new Label();

	// Consumer nodes
	private Label lblLoad = new Label();

	// Storage
	private Label lblCurrentCharge = new Label();
	private Label lblMaximumCharge = new Label();
	private Label lblMinimumCharge = new Label();
	private Label lblChargeEfficiency = new Label();
	private Label lblDischargeEfficiency = new Label();

	private JFileChooser fileChooser = new JFileChooser();
	private JButton btnOpenSelectDirectory = new JButton("Select directory");
	private String selectedDirectory = "";
	
	// Generation
	private JButton btnsmallWorld = new JButton("generate small world graph");
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private JScrollPane scllPaneNodes = new JScrollPane();
	private JList<String> nodeList = new JList<String>();
	private JButton btnLoadNodeList = new JButton("Load Node List");
	private JFileChooser nodeListChooser = new JFileChooser();
	private JButton btnSaveGraph = new JButton("Save Graph");
	private JButton btnAssignEdges = new JButton("Assign Edges Nodes");
	private JButton btnAssignNodes= new JButton("Assign Nodes");
	
	private DefaultListModel<String> listModelEdges = new DefaultListModel<String>();
	private JScrollPane scllPaneEdges = new JScrollPane();
	private JList<String> edgesList = new JList<String>();		
	
	private boolean fuckingleavethecomboboxalone = false;
	boolean viewerInit = false;
	
	public enum KEY_DOWN {NONE, DELETE, ADD, ADD_EDGE};
	private KEY_DOWN keydown;
	
	//Keeps tracks of the number of outer, inner and edges found in the network file that the users selects.
	//Need these numbers when generating graphs.
	int numOuterNodes, numInnerNodes, numOfEdges = 0;
	String nodeOne = "";

	public GUI(GraphLogic graphLogic) {
		System.out.println("gui construct");
		this.graphLogic = graphLogic;

		myFrame.setLayout(new BorderLayout(5, 0));
		//myFrame.add(view, BorderLayout.CENTER);
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myFrame.setLocationRelativeTo(null);
		myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		setupPanels();
		//setupActionListeners();
		setupInfoPanelActionListeners();
		myFrame.setVisible(true);
		//viewerInit = true;
	}

	public void setupPanels() {
		System.out.println("setup panels");
		informationPanel = new JPanel();
		informationPanel.setLayout(new BoxLayout(informationPanel, BoxLayout.PAGE_AXIS));

		File folder = new File("graphstate");
		File[] graphstates = folder.listFiles();

		String[] fileNames = new String[graphstates.length];
		for (int i = 0; i < graphstates.length; i++)
			fileNames[i] = graphstates[i].getName();

		NaturalOrderComparator comparator = new NaturalOrderComparator();
		Arrays.sort(fileNames, comparator);

		informationPanel.add(btnOpenSelectDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(new File("./"));

		timestepDropdown = new JComboBox<String>(fileNames);
		timestepDropdown.setFocusable(false);
		timestepDropdown.setSize(new Dimension(200, 10));
		timestepDropdown.setSelectedIndex(timeStepSelectionIndex);
		informationPanel.add(timestepDropdown);
		informationPanel.add(Box.createRigidArea(new Dimension(500, 100)));

		setupNodeAndEdgeInfoPanel();

		informationPanel.add(Box.createRigidArea(new Dimension(informationPanel.getWidth(), 800)));
		//myFrame.getContentPane().add(informationPanel, BorderLayout.EAST);
		myFrame.getContentPane().add(tabbedPane, BorderLayout.EAST);
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				if(viewMode == VIEW_MODE.GRAPH_GENERATION)
					viewMode = VIEW_MODE.GRAPH_INSPECTION;
				else if(viewMode == VIEW_MODE.GRAPH_INSPECTION)
					viewMode = VIEW_MODE.GRAPH_GENERATION;	
			}
		});
	}

	private void setupNodeAndEdgeInfoPanel() {
		// For all selections
		selectionInfoPanel = new JPanel(new GridLayout(2, 2));
		selectionInfoPanel.setBorder(BorderFactory.createTitledBorder("Selection Information"));
		selectionInfoPanel.add(new Label("Identifier: "));
		selectionInfoPanel.add(lblIdentifier);
		selectionInfoPanel.add(new Label("Type: "));
		selectionInfoPanel.add(lblType);
		selectionInfoPanel.setVisible(true);
		informationPanel.add(selectionInfoPanel);
	
		// Conventional and renewable info panel
		convAndRewGenerationInfoPanel = new JPanel(new GridLayout(3, 2));
		convAndRewGenerationInfoPanel.setBorder(BorderFactory.createTitledBorder("Generator information"));
		convAndRewGenerationInfoPanel.setSize((int) ((int) informationPanel.getMaximumSize().width * 0.8),informationPanel.getHeight());
		convAndRewGenerationInfoPanel.setName("nodeOrEdgePanel");
		convAndRewGenerationInfoPanel.add(new Label("subType: "));
		convAndRewGenerationInfoPanel.add(lblSubType);
		convAndRewGenerationInfoPanel.add(new Label("Production: "));
		convAndRewGenerationInfoPanel.add(lblProduction);
		convAndRewGenerationInfoPanel.add(new Label("Max production: "));
		convAndRewGenerationInfoPanel.add(lblMaxProduction);
		convAndRewGenerationInfoPanel.add(new Label("Min Production: "));
		convAndRewGenerationInfoPanel.add(lblMinimumProduction);
		convAndRewGenerationInfoPanel.setVisible(false);
		convAndRewGenerationInfoPanel.add(new Label("Failure: "));
		convAndRewGenerationInfoPanel.add(lblFailure);
		informationPanel.add(convAndRewGenerationInfoPanel);
	
		// Storage info panel
		storageInfoPanel = new JPanel(new GridLayout(3, 2));
		storageInfoPanel.setBorder(BorderFactory.createTitledBorder("Storage information"));
		storageInfoPanel.add(new Label("Current Charge: "));
		storageInfoPanel.add(lblCurrentCharge);
		storageInfoPanel.add(new Label("Maximum Charge: "));
		storageInfoPanel.add(lblMaximumCharge);
		storageInfoPanel.add(new Label("Minimum Discharge: "));
		storageInfoPanel.add(lblMinimumCharge);
		storageInfoPanel.add(new Label("Charge efficiency: "));
		storageInfoPanel.add(lblChargeEfficiency);
		storageInfoPanel.add(new Label("Discharge efficiency: "));
		storageInfoPanel.add(lblDischargeEfficiency);
		storageInfoPanel.setVisible(false);
		informationPanel.add(storageInfoPanel);
	
		consumerInfoPanel = new JPanel(new GridLayout(1, 2));
		consumerInfoPanel.setBorder(BorderFactory.createTitledBorder("Consumer information"));
		consumerInfoPanel.add(new Label("Load: "));
		consumerInfoPanel.add(lblLoad);
		consumerInfoPanel.setVisible(false);
		informationPanel.add(consumerInfoPanel);
	
		edgeInfoPanel = new JPanel(new GridLayout(3, 2));
		edgeInfoPanel.setLayout(new BoxLayout(edgeInfoPanel, BoxLayout.Y_AXIS));
		edgeInfoPanel.setBorder(BorderFactory.createTitledBorder("Edge information"));
	
		edgeInfoPanel.setVisible(true);
		edgeInfoPanel.add(new Label(" "));
		informationPanel.add(edgeInfoPanel);
	
		generationPanel = new JPanel(new GridBagLayout());
		generationPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		generationPanel.setVisible(true);
		
		GridBagConstraints constr = new GridBagConstraints();
			
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.gridx = 0;
		constr.gridy = 0;
		constr.insets = new Insets(0, 2, 2, 2);
		generationPanel.add(btnsmallWorld, constr);
		
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.weightx = 0.5;
		constr.gridx = 1;
		constr.gridy = 0;
		constr.insets = new Insets(0, 2, 2, 2);
		generationPanel.add(btnLoadNodeList, constr);
			
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.weightx = 0.0;
		constr.gridx = 0;
		constr.gridy = 1;
		generationPanel.add(btnAssignNodes, constr);
		
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.weightx = 0.0;
		constr.gridx = 1;
		constr.gridy = 1;
		constr.insets = new Insets(2, 2, 2, 2);
		generationPanel.add(btnAssignEdges, constr);
		
		constr.fill = GridBagConstraints.BOTH;
		constr.weightx = 1.0;
		constr.weighty = 1.0;  
		constr.gridx = 0;
		constr.gridy = 2;
		constr.gridwidth = 1;
		constr.anchor = GridBagConstraints.PAGE_END;
		generationPanel.add(scllPaneNodes, constr);

		constr.fill = GridBagConstraints.BOTH;
		constr.weightx = 1.0;
		constr.weighty = 1.0;  
		constr.gridx = 1;
		constr.gridy = 2;
		constr.gridwidth = 1;
		constr.anchor = GridBagConstraints.PAGE_END;
		generationPanel.add(scllPaneEdges, constr);
		
		constr.fill = GridBagConstraints.HORIZONTAL;
		constr.ipady = 0;       //reset to default
		constr.weighty = 0.05;   //request any extra vertical space
		constr.anchor = GridBagConstraints.PAGE_END; //bottom of space
		constr.insets = new Insets(2,2,2,2);  //top padding
		constr.gridx = 0;       //aligned with button 2
		constr.gridwidth = 3;   //2 columns wide
		constr.gridy = 3;       //third row
		generationPanel.add(btnSaveGraph, constr);

		informationPanel.add(generationPanel);
		
		tabbedPane.addTab("Inspection", informationPanel);
		tabbedPane.addTab("Generation", generationPanel);
	}

	private void setupInfoPanelActionListeners(){
		
		btnAssignNodes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				graphLogic.assignNodes(numOuterNodes+numInnerNodes, listModel);
			}
		});
		
		timestepDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println(timestepDropdown.getSelectedItem().toString());
				if (e.getSource() == timestepDropdown && !fuckingleavethecomboboxalone){
					graphstateSelected(String.valueOf(timestepDropdown.getSelectedItem()));
					timeStepSelectionIndex = timestepDropdown.getSelectedIndex();
				}
			}
		});

		btnOpenSelectDirectory.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == btnOpenSelectDirectory){
					int option = fileChooser.showOpenDialog(myFrame);
					if (option == JFileChooser.APPROVE_OPTION){
						selectedDirectory = fileChooser.getSelectedFile().getPath();
						graphLogic.setDirectory(selectedDirectory);

						timeStepSelectionIndex = 0;
						fuckingleavethecomboboxalone = true;
						timestepDropdown.setSelectedIndex(timeStepSelectionIndex);
						fuckingleavethecomboboxalone = false;
						graphstateSelected(timestepDropdown.getItemAt(timeStepSelectionIndex).toString());
						
					}
				}

			}
		});
		
		btnsmallWorld.addActionListener(new ActionListener() {		
			@Override
			public void actionPerformed(ActionEvent e) {
				Graph graph = graphGenerator.createModifiedWattsStrogatz(numOuterNodes, numInnerNodes, numOfEdges);
				graphLogic.setGraph(graph);
				setupGraphStreamView();
			}
		});
		
		btnLoadNodeList.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == btnLoadNodeList){
					listModel.removeAllElements();
					nodeList.removeAll();
					edgesList.removeAll();
					int option = nodeListChooser.showOpenDialog(myFrame);
					if(option == nodeListChooser.APPROVE_OPTION){
						String selectedFile = nodeListChooser.getSelectedFile().getPath();
						NodeListParser parser = new NodeListParser();
						List<HashMap<Integer, List<String>>> attributes =  parser.parseNodeList(selectedFile);
						
						HashMap<Integer, List<String>> nodeMap = attributes.get(0);
						Iterator<Integer> it = nodeMap.keySet().iterator();
						while(it.hasNext()){
							int key = it.next();
							ArrayList<String> node = (ArrayList<String>)nodeMap.get(key);
							//String nodeAttributes = Integer.toString(key);
							String nodeAttributes = "";
							for(String value : node){
								calculateNumberOfNodesAndEdges(value);
								if(!(nodeAttributes.equals("")))
									nodeAttributes = nodeAttributes + " " + value;
								else
									nodeAttributes += value;
							}
							listModel.addElement(nodeAttributes);
							nodeList.setModel(listModel);
							edgesList.setModel(listModelEdges);
							scllPaneNodes.setViewportView(nodeList);
							scllPaneEdges.setViewportView(edgesList);
						}
						HashMap<Integer, List<String>> edgeMap = attributes.get(1);
						Iterator<Integer> itEdge = edgeMap.keySet().iterator();

						while(itEdge.hasNext()){
							int key = itEdge.next();
							ArrayList<String> node = (ArrayList<String>)edgeMap.get(key);
							//String nodeAttributes = Integer.toString(key);
							String edgeAttributes = "";
							for(String value : node){
								calculateNumberOfNodesAndEdges(value);
								if(!(edgeAttributes.equals("")))
									edgeAttributes = edgeAttributes + " " + value;
								else
									edgeAttributes += value;
							}
							listModelEdges.addElement(edgeAttributes);
							edgesList.setModel(listModelEdges);
							scllPaneEdges.setViewportView(edgesList);
						}
					}
				}
				System.out.println(numInnerNodes+numOuterNodes);
			}
		});
		
	}
	
	private void calculateNumberOfNodesAndEdges(String value){
		
		if (value.equals("CG") || value.equals("C") || value.equals("RG") || value.equals("Storage") || value.equals("IN")){
			numOuterNodes++;	
		}else if(value.equals("AE"))
			numOfEdges++;
	}

	private void setupActionListeners() {
		
		view.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				mouseEventHandler(e);
			}
		});

		view.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseDragged(MouseEvent e) {
			}
		});
		
		view.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				keydown = KEY_DOWN.NONE;		
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
				switch(e.getKeyChar()){
				case 'd':
					keydown = KEY_DOWN.DELETE;
					break;
				case 'a':
					keydown = KEY_DOWN.ADD;
					break;
				case 'e':
					keydown = KEY_DOWN.ADD_EDGE;
					break;
					default:
						keydown = KEY_DOWN.NONE;
						break;
				}
			}
		});

		view.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}
			

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					GraphicElement gfxNode = view.findNodeOrSpriteAt(view.getMousePosition().getX(),
							view.getMousePosition().getY());

					if (gfxNode != null && gfxNode.getSelectorType().equals(Selector.Type.NODE)) {
						if(viewMode == VIEW_MODE.GRAPH_INSPECTION){
							Node node = graphLogic.getGraph().getNode(gfxNode.getId());
							lblIdentifier.setText(node.getId());
							lblType.setText(node.getAttribute("ui.class").toString());
	
							setEdgeInformation(node);
					
							switch (lblType.getText()) {
							case "ConventionalGenerator":
							case "RewGenerator":
								lblSubType.setText(node.getAttribute("subType").toString());
								lblProduction.setText(node.getAttribute("production").toString());
								lblMaxProduction.setText(node.getAttribute("maxProduction").toString());
								lblMinimumProduction.setText(node.getAttribute("minProduction").toString());
								if (lblType.getText().equals("RewGenerator"))
									lblFailure.setText("N/A");
								else
									lblFailure.setText(node.getAttribute("failure").toString());
								convAndRewGenerationInfoPanel.setVisible(true);
								storageInfoPanel.setVisible(false);
								consumerInfoPanel.setVisible(false);
								break;
							case "InnerNode":
								// Doesn't have any attributes for the moment.
								convAndRewGenerationInfoPanel.setVisible(false);
								storageInfoPanel.setVisible(false);
								consumerInfoPanel.setVisible(false);
								break;
							case "Consumer":
								lblLoad.setText(node.getAttribute("load").toString());
	
								convAndRewGenerationInfoPanel.setVisible(false);
								storageInfoPanel.setVisible(false);
								consumerInfoPanel.setVisible(true);
								break;
							case "Storage":
								lblCurrentCharge.setText(node.getAttribute("currentCharge").toString());
								lblMaximumCharge.setText(node.getAttribute("maxCharge").toString());
								lblMinimumCharge.setText(node.getAttribute("minCharge").toString());
								lblChargeEfficiency.setText(node.getAttribute("chargeEfficiency").toString());
								lblDischargeEfficiency.setText(node.getAttribute("dischargeEfficiency").toString());
	
								convAndRewGenerationInfoPanel.setVisible(false);
								storageInfoPanel.setVisible(true);
								consumerInfoPanel.setVisible(false);
								break;
							}
						} else if(viewMode == VIEW_MODE.GRAPH_GENERATION){
							System.out.println(keydown);
							if(keydown == KEY_DOWN.DELETE){
								Node node = graphLogic.getGraph().getNode(gfxNode.getId());
								graphLogic.getGraph().removeNode(node);
							} else if(keydown == KEY_DOWN.ADD){
								String id = String.valueOf(System.currentTimeMillis());
								graphLogic.getGraph().addNode(id);
								Node newNode = graphLogic.getGraph().getNode(id);								
								Node node = graphLogic.getGraph().getNode(gfxNode.getId());
								id = String.valueOf(System.currentTimeMillis());
								graphLogic.getGraph().addEdge(id, node, newNode);
							}else if(keydown == KEY_DOWN.ADD_EDGE){
								if(nodeOne.isEmpty())
									nodeOne =  gfxNode.getId();
								else{
									System.out.println(nodeOne);
									String nodeTwo =  gfxNode.getId();
									System.out.println(nodeTwo);
									String id = String.valueOf(System.currentTimeMillis());
									graphLogic.getGraph().addEdge(id, nodeOne, nodeTwo);
									nodeOne = "";
								}	
							}else{
								Node node = graphLogic.getGraph().getNode(gfxNode.getId());
								String attributes = nodeList.getModel().getElementAt(0);
								String[] attributesArray = attributes.split("\\s");
								node = setAttributes(node, attributesArray);
								System.out.println("read " + node.getAttributeKeySet().toString());
								System.out.println(graphLogic.getGraph().getNode(gfxNode.getId()).getAttributeKeySet().toString());
								System.out.println(graphLogic.getGraph().getNode(gfxNode.getId()).getAttribute("ui.class").toString());
								listModel.remove(0);
							}
						}
					}
				} else if (SwingUtilities.isRightMouseButton(e)) {
					Point3 graphUnitsPoint = view.getCamera().transformPxToGu(view.getMousePosition().getX(),
							view.getMousePosition().getY());
					view.getCamera().setViewCenter(graphUnitsPoint.x, graphUnitsPoint.y, 0);
				}
			}
		});
	}
	
	private Node setAttributes(Node node, String[] attributes){
		String nodeType = attributes[0];
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

	private void setEdgeInformation(Node node){
		Iterator<Edge> it = node.getEdgeSet().iterator();
		edgeInfoPanel.removeAll();
		edgeInfoPanel.revalidate();
		edgeInfoPanel.repaint();
		String nodeId = node.getId();

		while (it.hasNext()){
			Edge edge = it.next();
			JPanel edgePanel = new JPanel();
			edgePanel.setLayout(new GridLayout(3, 2));
			if( edge.getNode0().getId().equals(nodeId)){
				edgePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"<"+edge.getNode0().getId()+">---<"+edge.getNode1().getId()+">"));
			} else {
				edgePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"<"+edge.getNode1().getId()+">---<"+edge.getNode0().getId()+">"));
			}

			Label lblFlow = new Label();
			Label lblCapacity = new Label();
			Label lblReactance = new Label();
			
			if(node.getAttribute("ui.class").equals("Consumer")){
				double flow = Double.parseDouble(edge.getAttribute("flow"));
				if (flow > 0)
					flow = flow * -1;
				lblFlow.setText(String.valueOf(flow));
			}else if (node.getAttribute("ui.class").equals("Storage")){
				double flow = Double.parseDouble(edge.getAttribute("flow"));
				if (node.getAttribute("status").equals("charging")){
					if (flow > 0)
						flow = flow * -1;
				} else if (node.getAttribute("status").equals("discharging")){
					 flow = Double.parseDouble(edge.getAttribute("flow"));
					if (flow < 0)
						flow = Math.abs(flow);
					lblFlow.setText(String.valueOf(flow));
				} else {	
					lblFlow.setText(String.valueOf(flow));
				}
			}else
				lblFlow.setText(edge.getAttribute("flow"));
			
			lblCapacity.setText(edge.getAttribute("capacity"));
			lblReactance.setText(edge.getAttribute("reactance"));

			edgePanel.add(new Label("Flow: "));
			edgePanel.add(lblFlow);
			edgePanel.add(new Label("Capacity: "));
			edgePanel.add(lblCapacity);
			edgePanel.add(new Label("Reactance: "));
			edgePanel.add(lblReactance);
			edgePanel.setVisible(true);

			edgeInfoPanel.add(edgePanel);
		}
		edgeInfoPanel.repaint();
	}

	private void mouseEventHandler(MouseWheelEvent event) {
		double viewPercent = view.getCamera().getViewPercent();
		double wheelRotation = event.getWheelRotation() * 0.1;

		if ((viewPercent <= 4) && (viewPercent + wheelRotation > 0))
			view.getCamera().setViewPercent(view.getCamera().getViewPercent() + wheelRotation);
	}

	private void graphstateSelected(String filename) {
		graphLogic.loadGraph(filename);
		setupGraphStreamView();
	}

	private void setupGraphStreamView() {
		if(viewerInit){
			myFrame.remove(view);
			myFrame.revalidate();
			myFrame.repaint();
			viewer.close();
		}

		viewer = new Viewer(graphLogic.getGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.enableAutoLayout(); //TODO: This creates new threads which are never removed.
		view = viewer.addDefaultView(false);

		myFrame.setLayout(new BorderLayout(5, 0));
		myFrame.add(view, BorderLayout.CENTER);
		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		myFrame.add(tabbedPane, BorderLayout.EAST);
		setupActionListeners();
		myFrame.setVisible(true);
		viewerInit = true;
	}

	public static void main(String[] args) {
		Thread.currentThread().setName("main thread");
		GraphLogic graphLogic = new GraphLogic();
		GUI gui = new GUI(graphLogic);
	}

}
