package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class Graph {
	//------------------------------------------------------------------------------------------------------------------------
	private HashMap<String, Double> nodeWeights;
	private HashMap<String, HashMap<String, Double>> edgeWeights;
	//------------------------------------------------------------------------------------------------------------------------
	public double getNodeWeight(String node){
		if (nodeWeights.containsKey(node))
			return nodeWeights.get(node);
		else
			return 0;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public double getEdgeWeight(String node1, String node2){ //returns the edge weigh if there is an edge between the two nodes. Otherwise returns 0.
		double result;
		if (edgeWeights.containsKey(node1)){
			HashMap<String, Double> tag2AndNumber = edgeWeights.get(node1); 
			if (tag2AndNumber.containsKey(node2))
				result = tag2AndNumber.get(node2);
			else
				result = 0;
		}
		else
			result = 0;
		return result;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public boolean hasNode(String node){
		if (nodeWeights.containsKey(node))
			return true;
		else
			return false;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public void loadGraph(String inputPath, String nodesInputFileName, String edgesInputFileName, FileManipulationResult fMR, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep) {
		//This method loads the nodes and edge weights and saves them in Graph's internal data structure as follows:
		//nodeWeights: tag --> weight
		//edgeWeight: tag1 --> (tag2 --> weight)
		//But the weights are accessible through methods , .
		nodeWeights = new HashMap<String, Double>();
		edgeWeights = new HashMap<String, HashMap<String, Double>>();
		try{ 
			BufferedReader br;
			//Reading nodes and their weights:
			br = new BufferedReader(new FileReader(inputPath + "\\" + nodesInputFileName)); 
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			MyUtils.println(writeMessageStep+"- Loading the graph from files \"" + nodesInputFileName + "\" and \"" + edgesInputFileName + "\":", indentationLevel);
			MyUtils.println("Started ...", indentationLevel+1);

			//First step: Reading node weights:
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep+"-1- Reading node weights:", indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);
			int i = 0;
			int j=0;
			String s = br.readLine(); //Reading the title line.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == 2){
					if (fields[0].length()>2){ //: to ignore one and two letter tags.
						nodeWeights.put(fields[0], Double.parseDouble(fields[1]));
						j++;
					}
				}
				else{
					fMR.errors++;
					break;
				}
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}
			br.close();
			MyUtils.println(Constants.integerFormatter.format(j) + " nodes have been read.", indentationLevel+2);
			MyUtils.println("Finished.", indentationLevel+2);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
			//End of step 1:

			//Second step: Reading edge weights:
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep+"-2- Reading edge weights:", indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);
			br = new BufferedReader(new FileReader(inputPath + "\\" + edgesInputFileName)); 
			br.readLine(); //Reading the title line.
			i = 0; 
			j=0;
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == 3){
					if (fields[0].length()>2 && fields[1].length()>2){//: to ignore one and two letter tags.
						HashMap<String, Double> tempNode2AndNumbers; 
						if (edgeWeights.containsKey(fields[0])){
							tempNode2AndNumbers = edgeWeights.get(fields[0]);
							if (tempNode2AndNumbers.containsKey(fields[1])){
								fMR.errors++; //each edge can appear 0 or one time.
								break;
							}
						}
						else
							tempNode2AndNumbers = new HashMap<String, Double>();
						tempNode2AndNumbers.put(fields[1], Double.parseDouble(fields[2]));
						edgeWeights.put(fields[0], tempNode2AndNumbers);
						j++;
					}
				}
				else{
					fMR.errors++;
					break;
				}
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(i));
			}
			MyUtils.println(Constants.integerFormatter.format(j) + " edges have been read.", indentationLevel+2);
			MyUtils.println("Finished.", indentationLevel+2);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
			//End of step 2:

			if (fMR.errors > 0)
				MyUtils.println("Finished with " + fMR.errors + " errors.", indentationLevel);
			else
				MyUtils.println("Finished.", indentationLevel+1);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			br.close();
		}catch (Exception e){
			fMR.errors++;
			fMR.doneSuccessfully = 0;
			e.printStackTrace();
		}
		fMR.processed = 1;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
}
