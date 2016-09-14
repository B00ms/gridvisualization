package gridVisualization;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class NodeListParser {

	public List<List<List<String>>> parseNodeList(String selectedFile) {
		
		List<List<List<String>>> attributes = new ArrayList<List<List<String>>>();
		List<List<String>> nodeMap = new ArrayList<List<String>>(); 
		List<List<String>> edgeMap = new ArrayList<List<String>>();
		Reader reader;
		Iterable<CSVRecord> records = null;
		try {
			reader = new FileReader(selectedFile);
			records = CSVFormat.DEFAULT.parse(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(CSVRecord record : records){
			ArrayList<String> nodeAttributes = new ArrayList<>();
			ArrayList<String> edgeAttributes = new ArrayList<>();
			
			String text = record.get(0);
			if(text.contains("#")) //Skip comments
				continue;
			
			String nodeType = record.get(0);
			nodeAttributes.add(nodeType);
			edgeAttributes.add(nodeType);
			
			String nodeSubType;
			String nodeId = null;
			String lowerGenLimit;
			String upperGenLimit;
			String costCoefficient;
			
			String consumptionPercent;
			String curtailmentCost;
			
			String currentSoC;
			String maxSoC;
			String minSoC;
			String chMax;
			
			String reactance;
			String capacity;
			String edgeId;
			
			switch(nodeType){
			case "CG":
				nodeSubType = record.get(1);
				nodeId = record.get(2);
				lowerGenLimit = record.get(3);
				upperGenLimit = record.get(4);
				costCoefficient = record.get(5);
				
				nodeAttributes.add(nodeSubType);
				nodeAttributes.add(nodeId);
				nodeAttributes.add(lowerGenLimit);
				nodeAttributes.add(upperGenLimit);
				nodeAttributes.add(costCoefficient);
				nodeMap.add(nodeAttributes);
				break;
			case "C":
				nodeId = record.get(1);
				consumptionPercent = record.get(2);
				
				nodeAttributes.add(nodeId);
				nodeAttributes.add(consumptionPercent);
				nodeMap.add(nodeAttributes);
				break;
				
			case "IN":
				nodeId = record.get(1);
				nodeAttributes.add(nodeId);
				nodeMap.add(nodeAttributes);
				break;
				
			case "RG":
				nodeSubType = record.get(1);
				nodeId = record.get(2);
				upperGenLimit = record.get(3);
				curtailmentCost = record.get(4);
				costCoefficient = record.get(5);
				
				nodeAttributes.add(nodeSubType);
				nodeAttributes.add(nodeId);
				nodeAttributes.add(upperGenLimit);
				nodeAttributes.add(curtailmentCost);
				nodeAttributes.add(costCoefficient);
				nodeMap.add(nodeAttributes);
				break;
			case "Storage":
				nodeId = record.get(1);
				currentSoC = record.get(2);
				maxSoC = record.get(3);
				minSoC = record.get(4);
				chMax = record.get(5);
				
				nodeAttributes.add(nodeId);
				nodeAttributes.add(currentSoC);
				nodeAttributes.add(maxSoC);
				nodeAttributes.add(minSoC);
				nodeAttributes.add(chMax);
				nodeMap.add(nodeAttributes);
				break;
			case "AE":
				edgeId = String.valueOf((edgeMap.size()+1));
				reactance = record.get(3);
				capacity = record.get(4);
				
				edgeAttributes.add(edgeId);
				edgeAttributes.add(reactance);
				edgeAttributes.add(capacity);
				edgeMap.add(edgeAttributes);
				break;
			}
			edgeAttributes = new ArrayList<String>(); 
			nodeAttributes = new ArrayList<String>();
		}
		
		attributes.add(nodeMap);
		attributes.add(edgeMap);
		return attributes;
	}

}
