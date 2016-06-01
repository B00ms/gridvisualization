package gridVisualization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.FileChooserUI;

import org.graphstream.graph.Edge;
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
	private JFrame myFrame = new JFrame("Grid Visualization");
	private JComboBox<String> timestepDropdown;
	private int timeStepSelectionIndex = 0;
	private JPanel informationPanel;

	private JPanel selectionInfoPanel;

	private JPanel convAndRewGenerationInfoPanel;
	private JPanel storageInfoPanel;
	private JPanel consumerInfoPanel;
	private JPanel edgeInfoPanel;

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
	JButton btnOpenSelectDirectory = new JButton("Select directory");
	private String selectedDirectory = "";
	
	private boolean fuckingleavethecomboboxalone = false;
	boolean viewerInit = false;
	
	private int counter = 0;

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
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setCurrentDirectory(new File("./"));

		timestepDropdown = new JComboBox<String>(fileNames);
		timestepDropdown.setFocusable(false);
		timestepDropdown.setSize(new Dimension(200, 10));
		timestepDropdown.setSelectedIndex(timeStepSelectionIndex);
		informationPanel.add(timestepDropdown);
		informationPanel.add(Box.createRigidArea(new Dimension(500, 100)));

		setupNodeAndEdgeInfoPanel();

		informationPanel.add(Box.createRigidArea(new Dimension(informationPanel.getWidth(), 800)));
		myFrame.getContentPane().add(informationPanel, BorderLayout.EAST);
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
	}
	
	private void setupInfoPanelActionListeners(){
		timestepDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(counter);
				//System.out.println(timestepDropdown.getSelectedItem().toString());
				if (e.getSource() == timestepDropdown && !fuckingleavethecomboboxalone){
					graphstateSelected(String.valueOf(timestepDropdown.getSelectedItem()));
					timeStepSelectionIndex = timestepDropdown.getSelectedIndex();
					counter++;
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
					} 
				} else if (SwingUtilities.isRightMouseButton(e)) {
					Point3 graphUnitsPoint = view.getCamera().transformPxToGu(view.getMousePosition().getX(),
							view.getMousePosition().getY());
					view.getCamera().setViewCenter(graphUnitsPoint.x, graphUnitsPoint.y, 0);
				}
			}
		});
		
		

	}
	
	private void setEdgeInformation(Node node){
		Iterator<Edge> it = node.getEdgeSet().iterator();
		edgeInfoPanel.removeAll();
		edgeInfoPanel.revalidate();
		edgeInfoPanel.repaint();
		
		while (it.hasNext()){
			Edge edge = it.next();
			JPanel edgePanel = new JPanel();
			edgePanel.setLayout(new GridLayout(3, 2));
			edgePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"<"+edge.getNode1().getId()+">---<"+edge.getNode0().getId()+">"));
			
			Label lblFlow = new Label();
			Label lblCapacity = new Label();
			Label lblReactance = new Label();
			
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
		System.out.println("grawphstateselected");
		graphLogic.loadGraph(filename);
		setupGraphStreamView();
	}

	private void setupGraphStreamView() {
		System.out.println("setupGraphStreamView");

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

		myFrame.add(informationPanel, BorderLayout.EAST);
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
