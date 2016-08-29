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

	public HashMap<Integer, List<String>> parseNodeList(String selectedFile) {
		
		HashMap<Integer, List<String>> nodeMap = new HashMap<>();
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
			
			String text = record.get(0);
			if(text.contains("#")) //Skip comments
				continue;
			
			String nodeType = record.get(0);
			nodeAttributes.add(nodeType);
			
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
				break;
			case "C":
				nodeId = record.get(1);
				consumptionPercent = record.get(2);
				
				nodeAttributes.add(nodeId);
				nodeAttributes.add(consumptionPercent);
				break;
				
			case "IN":
				nodeId = record.get(1);
				nodeAttributes.add(nodeId);
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
				break;
			}
			System.out.println(nodeAttributes);
			nodeMap.put(Integer.parseInt(nodeId), nodeAttributes);
			nodeAttributes = new ArrayList<String>();
		}
		return nodeMap;
	}

}
