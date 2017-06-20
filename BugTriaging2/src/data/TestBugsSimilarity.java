package data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import utils.Constants;
import utils.MyUtils;
import utils.TSVManipulations;
import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.LogicalOperation;
import utils.Constants.SortOrder;
import utils.FileManipulationResult;
import utils.Graph;

public class TestBugsSimilarity {
	public static int numberOfSharedNonSOWords(String ss1, String ss2, Graph graph, HashSet<String> stopWords){
		String[] s1 = ss1.split(" ");
		String[] s2 = ss2.split(" ");
		int result = 0;
		HashSet<String> hs = new HashSet<String>();
		for (int i=0; i<s1.length; i++)
			for (int j=0; j<s2.length; j++)
				if (s1[i].equals(s2[j]) && !graph.hasNode(s1[i]) && !stopWords.contains(s1[i]) && s1[i].length()>2){
					if (!hs.contains(s1[i])){
						result++;
						hs.add(s1[i]);
						break;
					}
				}
		return result;
	}
	///////////////////////////////////////////////
	public static int numberOfSharedWords(String ss1, String ss2, Graph graph, HashSet<String> stopWords){
		String[] s1 = ss1.split(" ");
		String[] s2 = ss2.split(" ");
		int result = 0;
		HashSet<String> hs = new HashSet<String>();
		for (int i=0; i<s1.length; i++)
			for (int j=0; j<s2.length; j++)
				if (s1[i].equals(s2[j]) && !stopWords.contains(s1[i]) && s1[i].length()>2){
					if (!hs.contains(s1[i])){
						result++;
						hs.add(s1[i]);
						break;
					}
				}
		return result;
	}
	///////////////////////////////////////////////
	public static HashSet<String> sharedNonSOWords(String ss1, String ss2, Graph graph, HashSet<String> stopWords){
		String[] s1 = ss1.split(" ");
		String[] s2 = ss2.split(" ");
		String result = "";
		HashSet<String> hs = new HashSet<String>();
		for (int i=0; i<s1.length; i++)
			for (int j=0; j<s2.length; j++)
				if (s1[i].equals(s2[j]) && !graph.hasNode(s1[i]) && !stopWords.contains(s1[i]) && s1[i].length()>2){
					if (!hs.contains(s1[i])){
						hs.add(s1[i]);
						break;
					}
				}
		return hs;
	}
	///////////////////////////////////////////////
	public static HashSet<String> sharedWords(String ss1, String ss2, Graph graph, HashSet<String> stopWords){
		String[] s1 = ss1.split(" ");
		String[] s2 = ss2.split(" ");
		String result = "";
		HashSet<String> hs = new HashSet<String>();
		for (int i=0; i<s1.length; i++)
			for (int j=0; j<s2.length; j++)
				if (s1[i].equals(s2[j]) && !stopWords.contains(s1[i]) && s1[i].length()>2){
					if (!hs.contains(s1[i])){
						hs.add(s1[i]);
						break;
					}
				}
		return hs;
	}
	///////////////////////////////////////////////
	public static int numberOfTags(String str, Graph graph){
		String[] s = str.split(" ");
		int count = 0;
		for (int i=0; i<s.length; i++)
			if (graph.hasNode(s[i]))
				count++;
		return count;
	}
	///////////////////////////////////////////////
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
//		String s1 = "abc de de ef gh i jkl";
//		String s2 = "a bc de f gh ij klm n opq";
//		System.out.println(numberOfSharedWords(s1, s2));
//		System.out.println(sharedWords(s1, s2));
		HashSet<String> stopWords = new HashSet<String>();
		stopWords.add("the");
		stopWords.add("i.e");
		stopWords.add("not");
		stopWords.add("and");
		stopWords.add("with");
		stopWords.add("should");
		stopWords.add("there");
		stopWords.add("but");
		stopWords.add("for");
		stopWords.add("that");
		stopWords.add("now");
		stopWords.add("are");
		stopWords.add("have");
		stopWords.add("will");
		stopWords.add("then");
		stopWords.add("from");
		stopWords.add("into");
		stopWords.add("while");
		stopWords.add("one");
		stopWords.add("all");
		stopWords.add("here");
		stopWords.add("been");
		stopWords.add("you");
		stopWords.add("also");
		stopWords.add("what");
		stopWords.add("just");
		stopWords.add("might");
		stopWords.add("other");
		stopWords.add("would");
		stopWords.add("goes");
		stopWords.add("again");
		stopWords.add("how");
		stopWords.add("even");
		stopWords.add("could");
		stopWords.add("more");
		stopWords.add("think");
		stopWords.add("some");
		stopWords.add("way");
		stopWords.add("when");
		stopWords.add("");
		stopWords.add("");
		stopWords.add("");
		stopWords.add("");
		stopWords.add("");
		stopWords.add("");
		stopWords.add("");
		
		FileManipulationResult localFMR = new FileManipulationResult();
		boolean wrapOutputInLines = true;
		int showProgressInterval = 10;
		int indentationLevel = 0;
		String writeMessageStep = "";

		Graph graph = new Graph();
		graph.loadGraph(Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT, "nodeWeights.tsv", "edgeWeights.tsv", localFMR, 
				wrapOutputInLines, showProgressInterval*10000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "1"));

		ArrayList<String> titlesToReturn_IS_NOT_NEEDED_AND_USED = new ArrayList<String>();
		TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments;
		projectsAndTheirAssignments = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
				"D:\\2-Study\\BugTriaging2\\Data Set\\Main\\GH\\AtLeastUpTo20161001\\4A3-TSV", "9-ASSIGNMENTS_T5_ALL_TYPES.tsv", localFMR, null, 
				0, SortOrder.DEFAULT_FOR_STRING, 7, "1$2$3", titlesToReturn_IS_NOT_NEEDED_AND_USED,
				LogicalOperation.NO_CONDITION, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				wrapOutputInLines, showProgressInterval*1000, indentationLevel+2, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, Integer.toString(5)+"-"+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[4]));
		System.out.println(projectsAndTheirAssignments.size());
		
		TreeMap<String, ArrayList<String[]>> projectsAndTheirBugs;
		projectsAndTheirBugs = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
				"D:\\2-Study\\BugTriaging2\\Data Set\\Main\\GH\\AtLeastUpTo20161001\\4A3-TSV", "1-bugs-T5_ALL_TYPES.tsv", localFMR, null, 
				0, SortOrder.DEFAULT_FOR_STRING, 9, "1$2$3$5$7", titlesToReturn_IS_NOT_NEEDED_AND_USED,
				LogicalOperation.NO_CONDITION, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				wrapOutputInLines, showProgressInterval*1000, indentationLevel+2, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, Integer.toString(5)+"-"+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[4]));
		System.out.println(projectsAndTheirBugs.size());
		System.out.println("==========================================================");
		int sum = 0;
//		String projectId = "1420493";{
		for (String projectId: projectsAndTheirBugs.keySet()){
			HashMap<String, HashSet<String>> h = new HashMap<String, HashSet<String>>();
			ArrayList<String[]> bugs = projectsAndTheirBugs.get(projectId);
			
			
			
			
			ArrayList<String[]> assignments = projectsAndTheirAssignments.get(projectId);
			HashMap<String, HashSet<String>> bugNumbersAndTheirAssignees = new HashMap<String, HashSet<String>>();
			for (int i=0; i<assignments.size(); i++){
				String assignee = assignments.get(i)[2];
				String bugNumber = assignments.get(i)[0];
//				if (bugNumber.equals("1068"))
//					System.out.println(bugNumber);
				if (bugNumbersAndTheirAssignees.containsKey(bugNumber)){
					HashSet<String> assignees = bugNumbersAndTheirAssignees.get(bugNumber);
					assignees.add(assignee);
				}
				else{
					HashSet<String> assignees = new HashSet<String>();
					assignees.add(assignee);
					bugNumbersAndTheirAssignees.put(bugNumber, assignees);
				}
			}
				
			
			
			
			System.out.println("==========================================================");
			System.out.println("==========================================================");
			System.out.println("==========================================================");
			System.out.println("==========================================================");
			System.out.println("project id: " + projectId + "\t" + bugs.size() + " bugs: ");
			int max = 0;
			HashSet<String> sWords = new HashSet<String>();
			String bugNumber1 = "", bugNumber2 = "";
			for (int i=0; i<bugs.size(); i++){
				String bugI = bugs.get(i)[3] + " " + bugs.get(i)[4]; 
//				System.out.println(bugI);
				for (int j=i+1; j<bugs.size(); j++){
					String bugJ = bugs.get(j)[3] + " " + bugs.get(j)[4];
					if (bugI.toLowerCase().contains("Steps to Reproduce Issue".toLowerCase()) || bugJ.toLowerCase().contains("Steps to Reproduce Issue".toLowerCase()))
						break;
					bugNumber1 = bugs.get(i)[0];
					bugNumber2 = bugs.get(j)[0];
					int diff = Math.abs(Integer.parseInt(bugNumber1)-Integer.parseInt(bugNumber2));
					if (bugI.length()<900 && bugJ.length()<900 && diff < 100 && diff > 2){
						int number = numberOfSharedNonSOWords(bugI, bugJ, graph, stopWords);
						if (number > 8 && number > bugI.length()/100 && number > bugJ.length()/100 /*&& number <20*/){
							//New-begin
							HashSet<String> assignees1 = bugNumbersAndTheirAssignees.get(bugNumber1);
							HashSet<String> assignees2 = bugNumbersAndTheirAssignees.get(bugNumber2);
							if (assignees1.size() < 2 || assignees2.size() < 2)
								break;
							for (String a: assignees1){
								if (assignees2.contains(a)) //: this is a shared assignee, just ignore it: 
									break;
							}		
							if (numberOfTags(bugI, graph) < 4 || numberOfTags(bugJ, graph) < 4)
								break;
							//New-end.
							max = number;
							sWords = sharedNonSOWords(bugI,  bugJ, graph, stopWords);
							h.put("projectId:"+projectId+ "\tmaxSharedWords:"+max+"\tbugs<"+bugNumber1+"-"+bugNumber2+">", sWords);
						}
					}
//					if (max > 30)
//						break;
				}
//				if (max > 30)
//					break;
			}
			for (String str:h.keySet()){
				System.out.println(str);
				sWords = h.get(str);
				String ss = "";
				for (String s: sWords)
					ss = ss + " " + s;
				System.out.println("Shared words: " + ss);
				System.out.println();
			}
			
			sum = sum + projectsAndTheirBugs.get(projectId).size();
		}
		System.out.println("==========================================================");
		System.out.println("Total bugs in all projects: " + sum);
	}

}





























