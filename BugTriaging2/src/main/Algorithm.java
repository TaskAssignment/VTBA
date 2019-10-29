package main;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import data.Assignee;
import data.Assignment;
import data.AssignmentStat;
import data.Bug;
import data.Evidence;
import data.Project;
import utils.Constants;
import utils.FileManipulationResult;
import utils.Graph;
import utils.LongClass;
import utils.MyUtils;
import utils.StringManipulations;
import utils.TSVManipulations;
import utils.Constants.ASSIGNMENT_TYPES_TO_TRIAGE;
import utils.Constants.BTOption1_whatToAddToAllBugs;
import utils.Constants.BTOption2_w;
import utils.Constants.BTOption3_TF;
import utils.Constants.BTOption4_IDF;
import utils.Constants.BTOption5_prioritizePAs;
import utils.Constants.BTOption6_whatToAddToAllCommits;
import utils.Constants.BTOption7_whenToCountTextLength;
import utils.Constants.BTOption8_recency;
import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.GeneralExperimentType;
import utils.Constants.LogicalOperation;
import utils.Constants.ProjectType;
import utils.Constants.SortOrder;

public class Algorithm {//test 9
	public static final int YES = 1;
	public static final int NO = 0;
	public static final int UNKNOWN_RANK = Integer.MAX_VALUE;
	public static final int INDEX_OF__EVIDENCE_TYPE__COMMIT = 1;
	public static final int INDEX_OF__EVIDENCE_TYPE__PR = 2;
	public static final int INDEX_OF__EVIDENCE_TYPE__BUG_COMMENT = 3;
	public static final int INDEX_OF__EVIDENCE_TYPE__COMMIT_COMMENT = 4;
	public static final int INDEX_OF__EVIDENCE_TYPE__PR_COMMENT = 5;

	// In the following method, bug assignment is experimented and the results are shown. Also summary is printed in output file <"results-"+currentDateTime.txt>.
	public static void bugAssignment(String inputPath, String nodeWeightsInputPath, String nodeWeightsFileName, 
			String additionalNodeWeightsInputPath, String additionalNodeWeightsInputFileNamePrefix, 
			String outputPath, String outputSummariesTSVFileName, 
			boolean isMainRun, int[] assignmentTypesToTriage, int[] evidenceTypes, int totalEvidenceTypes_count,  
			String experimentTitle, String experimentDetails, 
			BTOption1_whatToAddToAllBugs option1_whatToAddToAllBugs, BTOption2_w option2_w, BTOption3_TF option3_TF, BTOption4_IDF option4_IDF, BTOption5_prioritizePAs option5_prioritizePAs, BTOption6_whatToAddToAllCommits option6_whatToAddToAllCommits,  BTOption7_whenToCountTextLength option7_whenToCountTextLength, BTOption8_recency option8_recency,
			GeneralExperimentType generalExperimentType, int developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered, 
			FileManipulationResult fMR,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep) {
		MyUtils.println("-----------------------------------", indentationLevel);
		MyUtils.println(MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "Bug assignment experiment:"), indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);
		
		float loopExtraTime_readingCommunities = 0;
		float initialExtraTime__readingGraphBugsAndProjectsInfo = 0;
		float initialExtraTime__readingAssignments_and_readingAndIndexingNonAssignmentEvidence = 0;
		float loopExtraTime_readingAssignmentEvidenceIndexing = 0;
		Date d1 = new Date();
		//The following method (loadGraph) was ran successfully (2016/11/16):
		FileManipulationResult localFMR = new FileManipulationResult();
		FileManipulationResult totalFMR = new FileManipulationResult();
		MyUtils.createFolderIfDoesNotExist(outputPath, localFMR, 1, "Initial 'temp directory checking'");

		MyUtils.println(MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "1- Reading main graph and 13 project-specific graphs:"), indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);

		Graph graph = new Graph();
		graph.loadGraph(nodeWeightsInputPath, nodeWeightsFileName, ""/*no edges to read*/, localFMR, 
				wrapOutputInLines, showProgressInterval*1000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "1-1- Main graph"));
		
		Graph[] graphs = new Graph[13];
		if (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_SOURCECODE){
			for (int i=0; i<13; i++){
				graphs[i] = new Graph();
				graphs[i].loadGraph(additionalNodeWeightsInputPath, additionalNodeWeightsInputFileNamePrefix+Constants.listOf13Projects[i]+".tsv", ""/*no edges to read*/, localFMR, 
						wrapOutputInLines, showProgressInterval*1000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "1-"+Integer.toString(i+2)));
			}
		}
		MyUtils.println("Finished.", indentationLevel+1);
		
		totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
		HashSet<String> stopWords = new HashSet<String>();
		if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF 
				|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF 
				|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2 
				|| generalExperimentType == GeneralExperimentType.CALCULATE_TBA 
				|| generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH 
				|| generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE 
				|| generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_SOURCECODE){
			stopWords = TSVManipulations.readUniqueFieldFromTSV(nodeWeightsInputPath, "stopWords.tsv", 0, 1, 
					LogicalOperation.NO_CONDITION, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT,
					true, indentationLevel+1, 100000, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep,"2"));
		}
		
//		//Testing:
//		System.out.println("Testing:");
//		System.out.println("javascript: " + graph.getNodeWeight("javascript"));
//		System.out.println("javascript-->jquery: " + graph.getEdgeWeight("javascript", "jquery")); //0.33523083822630956
//		System.out.println("javascript-->html: " + graph.getEdgeWeight("javascript", "html")); //0.1845487229352718
//		System.out.println(".a-->c: " + graph.getEdgeWeight(".a", "c")); //0.1724137931034483
//		System.out.println("javascript-->android: " + graph.getEdgeWeight("javascript", "android")); //0.
		
		ArrayList<String> titlesToReturn_IS_NOT_NEEDED_AND_USED = new ArrayList<String>();
		
		TreeMap<String, String[]> projects = TSVManipulations.readUniqueKeyAndItsValueFromTSV(inputPath, "7-projects.tsv", null, 
				0, 14, "1$2$3$4$5$6$7$8$9$10$11$12$13", 
				LogicalOperation.NO_CONDITION,
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT,
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT,
				wrapOutputInLines, showProgressInterval, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "3"));
		
		TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo = TSVManipulations.readUniqueCombinedKeyAndItsValueFromTSV(
				inputPath, "1-bugs-"+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[4]+".tsv", localFMR, null, 
				"0$1",  
				9, "2$3$4$5$6$7$8", 
				LogicalOperation.NO_CONDITION, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				wrapOutputInLines, showProgressInterval*1000, indentationLevel+1, Constants.THIS_IS_REAL, "4");
		totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
		
		//Preparing the 'evidenceTypesToConsider' array for AlgPrep.calculateScoreOfDeveloperForBugAssignment():
		int evidenceTypesToConsider_count = 0;
		for (int j=0; j<totalEvidenceTypes_count; j++) 
			if (evidenceTypes[j] == YES)
				evidenceTypesToConsider_count++;
		int[] evidenceTypesToConsider = new int[evidenceTypesToConsider_count];
		int p;
		if (evidenceTypes[0] == YES){ //: assignedBug
			p = 1; //: reserve the k'th index for ASSIGNMENT_TYPES_TO_TRIAGE values in the loop.
			//evidenceTypesToConsider[0] = <0, 1, 2, 3 or 4> --> this will be set in the loop over five assignment types below.
		}
		else
			p = 0; //: means that 'evidenceTypes[0] == NO', i.e., we shouldn't use assignedBug evidence for triaging.
		for (int j=1; j<totalEvidenceTypes_count; j++) 
			if (evidenceTypes[j] == YES){
				evidenceTypesToConsider[p] = Constants.EVIDENCE_TYPE[4+j]; //: Something between Constants.EVIDENCE_TYPE_COMMIT and EVIDENCE_TYPE_PR_COMMENT. Item #4 in Constants.EVIDENCE_TYPE is the last assignment-related evidence. 
				p++;
			}
		for (int j=0; j<evidenceTypesToConsider_count; j++) 
			System.out.println("------ "+evidenceTypesToConsider[j]);

		Date d2 = new Date();
		initialExtraTime__readingGraphBugsAndProjectsInfo = (float)(d2.getTime()-d1.getTime())/1000;

		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel+1);
		MyUtils.println(MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "5- Reading needed assignment file(s):"), indentationLevel+1);
		MyUtils.println("Started ...", indentationLevel+2);

		//Defining an arrayList for all five types of assignments:
		ArrayList<TreeMap<String, ArrayList<String[]>>> projectsAndTheirAssignments__AL_forDifferentAssignmetTypes = new ArrayList<TreeMap<String, ArrayList<String[]>>>();
		for (int i=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); i<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); i++){
			TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments;
			if (assignmentTypesToTriage[i] == YES){
				projectsAndTheirAssignments = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
						inputPath, Constants.ASSIGNMENT_FILE_NAMES[i]+".tsv", localFMR, null, 
						0, SortOrder.DEFAULT_FOR_STRING, 7, "1$2$3", titlesToReturn_IS_NOT_NEEDED_AND_USED,
						LogicalOperation.NO_CONDITION, 
						0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
						0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
						wrapOutputInLines, showProgressInterval*1000, indentationLevel+2, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "5-"+Integer.toString(i+1)+"-"+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[i]));
				totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
			}
			else{
				projectsAndTheirAssignments = new TreeMap<String, ArrayList<String[]>>(); //creating an empty TreeMap, just to add to projectsAndTheirAssignments_AL. It is because we want to use static indexes for each type later in readNonAssignmentEvidence. Also in the main loop of assignment, when we iterate over assignments of a specific type.
				MyUtils.println(MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "5-"+Integer.toString(i+1)+"- \""+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[i]+"\" --> is not configured to use or run the prediction algorithm on."), indentationLevel+2);
			}
			//Now, add the created "projectsAndTheirAssignments" (which either is containing assignment information or not [based on assignmentTypesToTriage values]) to projectsAndTheirAssignments_AL:
			projectsAndTheirAssignments__AL_forDifferentAssignmetTypes.add(projectsAndTheirAssignments);
		}
		MyUtils.println("Finished.", indentationLevel+2);
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel+1);

		HashMap<String, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>> projectId_Login_Tags_TypesAndTheirEvidence =
				new HashMap<String, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>>();
		AlgPrep.readAndIndexNonAssignmentEvidence(inputPath,  
				"2-commits.tsv", "3-PRs.tsv", "4-bugComments.tsv", "5-commitComments.tsv", "6-PRComments.tsv", 
				projectsAndTheirAssignments__AL_forDifferentAssignmetTypes, assignmentTypesToTriage, 
				localFMR, ///*since the second param of this method is empty, the next param is null and won't be considered:*/
				evidenceTypes, 
				projects, null, 
				projectId_Login_Tags_TypesAndTheirEvidence, 
				graph, graphs, 
				option1_whatToAddToAllBugs, option2_w, option3_TF, option4_IDF, option5_prioritizePAs, option6_whatToAddToAllCommits, option7_whenToCountTextLength, 
				generalExperimentType, 
				wrapOutputInLines, showProgressInterval*100, indentationLevel+1, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "6"));
		totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);

		String detailedAssignmentResultsSubfolderName = AlgPrep.createFolderForResults(outputPath+"\\"+Constants.ASSIGNMENT_RESULTS_OVERAL_FOLDER_NAME, experimentTitle, isMainRun, localFMR, indentationLevel);
		totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
		Date d3 = new Date();

		if (totalFMR.errors > 0)
			MyUtils.println("There are errors! Stopping ...", indentationLevel+3);
		else{
			initialExtraTime__readingAssignments_and_readingAndIndexingNonAssignmentEvidence = (float)(d3.getTime()-d2.getTime())/1000;
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
			int TOTAL_NUMBER_OF_ASSIGNMENT_TYPES_TO_TRIAGE = 0;
			for (int j=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); j<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); j++)
				if (assignmentTypesToTriage[j] == YES)
					TOTAL_NUMBER_OF_ASSIGNMENT_TYPES_TO_TRIAGE++;
			MyUtils.println(MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "7- Running prediction algorithm for " + TOTAL_NUMBER_OF_ASSIGNMENT_TYPES_TO_TRIAGE + " assignment file(s):"), indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);

			for (int i=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); i<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); i++){
				String step = MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "7-"+(i+1));
				if (assignmentTypesToTriage[i] == YES){
					Date d4 = new Date();
					if (wrapOutputInLines)
						MyUtils.println("-----------------------------------", indentationLevel+2);
					MyUtils.println(step+"- Predicting \""+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[i]+"\" assignments:", indentationLevel+2);
					MyUtils.println("Started ...", indentationLevel+3);

					if (evidenceTypes[0] == YES) //: assignedBug
						evidenceTypesToConsider[0] = Constants.EVIDENCE_TYPE[i]; //: this is the 'assignment' evidence type (can be 0 to 4). 

					TreeMap<String, ArrayList<String[]>> projectsAndTheirCommunities = TSVManipulations.readNonUniqueKeyAndItsValueFromTSV(
							inputPath, Constants.COMMUNITY_FILE_NAMES[i]+".tsv", localFMR, null, 
							0, SortOrder.DEFAULT_FOR_STRING, 2, "1", titlesToReturn_IS_NOT_NEEDED_AND_USED,
							LogicalOperation.NO_CONDITION, 
							0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
							0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
							wrapOutputInLines, showProgressInterval*1000, indentationLevel+3, Constants.THIS_IS_REAL, step+"-2");
					//					wrapOutputInLines, showProgressInterval*1000, indentationLevel+3, Constants.THIS_IS_A_TEST, step+"-2");
					totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);

					Date d5 = new Date();
					loopExtraTime_readingCommunities = loopExtraTime_readingCommunities + (float)(d5.getTime()-d4.getTime())/1000;

					if (evidenceTypes[0] == YES){ //: assignedBug
						AlgPrep.indexAssignmentEvidence(Constants.EVIDENCE_TYPE[i], projectsAndTheirAssignments__AL_forDifferentAssignmetTypes.get(i),
								projects, projectIdBugNumberAndTheirBugInfo, projectId_Login_Tags_TypesAndTheirEvidence, 
								graph, graphs, localFMR, 
								option1_whatToAddToAllBugs, option2_w, option3_TF, option4_IDF, option5_prioritizePAs, option6_whatToAddToAllCommits, option7_whenToCountTextLength, 
								generalExperimentType, 
								wrapOutputInLines, showProgressInterval*100, indentationLevel+3, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, step+"-3"));
						totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
						if (totalFMR.errors > 0){
							MyUtils.println("There are errors! Breaking ...", indentationLevel+3);
							break;
						}
					}

					Date d6 = new Date();
					loopExtraTime_readingAssignmentEvidenceIndexing = loopExtraTime_readingAssignmentEvidenceIndexing + (float)(d6.getTime()-d5.getTime())/1000;

					String subStep = step + "-4";
					if (wrapOutputInLines)
						MyUtils.println("-----------------------------------", indentationLevel+3);
					MyUtils.println(step+"-4- Assigning:", indentationLevel+3);
					MyUtils.println("Started ...", indentationLevel+4);

					TreeMap<String, ArrayList<AssignmentStat>> projectsAndTheirAssignmentStats = new TreeMap<String, ArrayList<AssignmentStat>>();
					TreeMap<String, String> projectNamesAndTheirIds_orderedByName = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
					int projectCounter = 1;
					Random random = new Random();			
					TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments = projectsAndTheirAssignments__AL_forDifferentAssignmetTypes.get(i);
					for (String projectId: projectsAndTheirAssignments.keySet()){
						//Considering assignments of one project:
						Project project = new Project(projects, projectId, indentationLevel+4, localFMR);
						totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
						if (AlgPrep.projectType(projectId, project.owner_repo) == ProjectType.FASE_13){
							projectNamesAndTheirIds_orderedByName.put(project.owner_repo, projectId);
							if (
									(isMainRun
											&& AlgPrep.isAProjectWhichIsUsedForMainRun(projectId, project.owner_repo)
									) 
									|| (!isMainRun
											&& AlgPrep.isAProjectWhichIsUsedForTuning(projectId, project.owner_repo)
											)
									){
								if (wrapOutputInLines)
									MyUtils.println("-----------------------------------", indentationLevel+4);
								MyUtils.println(subStep+"-"+projectCounter+"- "+project.owner_repo+" (projectId: " + projectId + ")", indentationLevel+4);

								Graph updatingGraph = new Graph();
								HashMap<String, Long> occurrences = new HashMap<String, Long>(); 
								LongClass maxNumberOfOccurrencesForAKeyword = new LongClass(0);

								ArrayList<String[]> assignmentsOfThisProject = projectsAndTheirAssignments.get(projectId);
								ArrayList<String[]> community = projectsAndTheirCommunities.get(projectId);
								HashMap<String, HashMap<String, Integer>> realAssignees = new HashMap<String, HashMap<String, Integer>>(); //bugNumber --> {login --> rank}
								HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>> logins_Tags_TypesAndTheirEvidence = projectId_Login_Tags_TypesAndTheirEvidence.get(projectId);
								HashMap<String, HashMap<String, Date>> wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate //"java"--> <"bob", 1/1/1>
									= new HashMap<String, HashMap<String, Date>>();
								HashMap<String, HashMap<String, HashSet<Date>>> wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates //"java"--> <"bob", <2019/1/1, 2018/2/2, 2019/3/3, ...>>
									= new HashMap<String, HashMap<String, HashSet<Date>>>();
								
								int numberOfBugsProcessed = 0;
								HashSet<String> previousAssigneesInThisProject = new HashSet<>();
								AlgPrep.removeAssignmentsOfDevelopersWhoFixedAtLeastNBugs(assignmentsOfThisProject, 
										developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered, logins_Tags_TypesAndTheirEvidence, 
										indentationLevel+5, localFMR);
								totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
								for (int j=0; j<assignmentsOfThisProject.size(); j++){ 
									Assignment a = new Assignment(assignmentsOfThisProject, j, indentationLevel+5);
									HashMap<String, Double> scores = new HashMap<String, Double>(); 

									Bug queryBug = new Bug(projectId, a.bugNumber, projectIdBugNumberAndTheirBugInfo, localFMR);
									totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);

									int[] originalNumberOfWordsInBugText_array = new int[1];
									String bugText = AlgPrep.getBugText(project, queryBug, originalNumberOfWordsInBugText_array, option1_whatToAddToAllBugs);
									int originalNumberOfWordsInBugText = originalNumberOfWordsInBugText_array[0];
									if (generalExperimentType == GeneralExperimentType.CALCULATE_TBA)
										bugText = StringManipulations.clean(bugText.toLowerCase().replaceAll(Constants.allValidCharactersInSOURCECODE_Strict_ForRegEx, " "));
									WordsAndCounts wAC = new WordsAndCounts(bugText, option7_whenToCountTextLength, originalNumberOfWordsInBugText, stopWords);
									//
									if (wAC.size == 0)
										MyUtils.println("Warning: Empty bug text!", indentationLevel+5);
									//	

									//Calculating the term weights online, if needed:
									if (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE){
										String bugText2 = StringManipulations.clean(bugText).toLowerCase().replaceAll(Constants.allValidCharactersInSOURCECODE_Strict_ForRegEx, " ");
										String[] words1 = bugText2.split(" ");
										DataPreparation.updateOccurrences(words1, stopWords, occurrences, maxNumberOfOccurrencesForAKeyword);
										for (String keyword: words1){
											Long occurrenceOfThisKeyword;
											double nodeWeight;

											if (occurrences.containsKey(keyword)){
												occurrenceOfThisKeyword = occurrences.get(keyword);
												//calculate node weight and normalize it (by dividing it to log10(TOTAL_NUMBER_OF_SO_QUESTIONS)):
												nodeWeight = Math.log10((1+(double)maxNumberOfOccurrencesForAKeyword.get())/occurrenceOfThisKeyword) / Math.log10(1+maxNumberOfOccurrencesForAKeyword.get());
												updatingGraph.setNodeWeight(keyword, nodeWeight);
											}
										}
									}

									//Calculating the word count for idf:
									for (int k=0; k<community.size(); k++){
										String login = community.get(k)[0];
										scores.put(login, 
												AlgPrep.calculateScoreOfDeveloperForBugAssignment(
														login, a, graph, updatingGraph, 
														i, evidenceTypesToConsider, evidenceTypesToConsider_count, 
														logins_Tags_TypesAndTheirEvidence, 
														previousAssigneesInThisProject, 
														wAC, originalNumberOfWordsInBugText, 
														j+1, 
														project.overalStartingDate, 
														generalExperimentType, community.size(), wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate, wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates, 
														option2_w, option4_IDF, option5_prioritizePAs, option8_recency,
														indentationLevel+5));
									}
									//Adding this assignee to the set of assignees of this bug (will be used in measuring the accuracies):
									HashMap<String, Integer> previousAssigneesOfThisBugAndTheirRanks;
									if(realAssignees.containsKey(a.bugNumber)){//: in this case, we don't need to create a HashMap<String, Integer> as previousAssigneesOfThisBugAndTheirRanks. Just retrieve it and add the current assignee to its end, and it will be updated in the hashMap:
										previousAssigneesOfThisBugAndTheirRanks = realAssignees.get(a.bugNumber);
										for (String login: previousAssigneesOfThisBugAndTheirRanks.keySet()) // set all the ranks of the previous assignees of this bug to "unknown" because their rank for the previous assignments may be different from this new assignment (the rank will be calculated later):
											previousAssigneesOfThisBugAndTheirRanks.put(login, UNKNOWN_RANK); //: this rank will be calculated and updated later.
									}
									else{//: in this case, we need to create an HashMap<String, Integer> as previousAssigneesOfThisBugAndTheirRanks. Then put it to the realAssignees:
										previousAssigneesOfThisBugAndTheirRanks = new HashMap<String, Integer>(); 
										realAssignees.put(a.bugNumber, previousAssigneesOfThisBugAndTheirRanks);		
									}
									previousAssigneesOfThisBugAndTheirRanks.put(a.login, UNKNOWN_RANK);
									//Rank the list of all community members, then update the ranks of real assignees in realAssignees. Finally return the assignee with the best rank: 
									Assignee ra = AlgPrep.updateRankOfRealAssigneesAndReturnTheBestAssignee(realAssignees, a.bugNumber, scores, random);

									//Update the stuff used for calculating idf:
									if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF || generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF){
										for (int k=0; k<wAC.size; k++)
											if (wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.containsKey(wAC.words[k])){
												HashMap<String, Date> developers_lastUsageDate = wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.get(wAC.words[k]);
												developers_lastUsageDate.put(a.login, a.date);
											}
//												wordAndTheDevelopersUsedThemUpToNow.put(wAC.words[k], wordAndTheDevelopersUsedThemUpToNow.get(wAC.words[k])+1);
											else{
												HashMap<String, Date> developers_lastUsageDate = new HashMap<String, Date>();
												developers_lastUsageDate.put(a.login, a.date);
												wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.put(wAC.words[k], developers_lastUsageDate);
											}
									}
									if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2){
										for (int k=0; k<wAC.size; k++)
											if (wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.containsKey(wAC.words[k])){
												HashMap<String, HashSet<Date>> developers_allUsageDates = wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.get(wAC.words[k]);
												if (developers_allUsageDates.containsKey(a.login)){// : means that this developer used this keyword before:  
													HashSet<Date> allUsageDates = developers_allUsageDates.get(a.login);
													allUsageDates.add(a.date);
												}
												else{//: means that this is the first time this developer used this keyword:
													HashSet<Date> allUsageDates = new HashSet<Date>();
													allUsageDates.add(a.date);
													developers_allUsageDates.put(a.login, allUsageDates);
												}
											}
											else{
												HashSet<Date> allUsageDates = new HashSet<Date>();
												allUsageDates.add(a.date);
												HashMap<String, HashSet<Date>> developers_allUsageDates = new HashMap<String, HashSet<Date>>();
												developers_allUsageDates.put(a.login, allUsageDates);
												wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.put(wAC.words[k], developers_allUsageDates);
											}
									}

									//Adding this assignment to projectsAndTheirAssignmentStats:
									AssignmentStat assignmentStat = new AssignmentStat(a.bugNumber, a.date, ra.login, ra.rank, realAssignees.get(a.bugNumber));
									ArrayList<AssignmentStat> assignmentStatsOfThisProject;
									if (projectsAndTheirAssignmentStats.containsKey(projectId)) //: means that we had assignmentStats for this project in the current loop before, so we just retrieve it:
										assignmentStatsOfThisProject = projectsAndTheirAssignmentStats.get(projectId);
									else//: means that this is the first bug assignment in this project, so we need to create ArrayList<AssignmentSummary> object:
										assignmentStatsOfThisProject = new ArrayList<AssignmentStat>();
									assignmentStatsOfThisProject.add(assignmentStat);
									projectsAndTheirAssignmentStats.put(projectId, assignmentStatsOfThisProject);

									numberOfBugsProcessed++;
									if (numberOfBugsProcessed % showProgressInterval == 0 && numberOfBugsProcessed > 0)
										MyUtils.println(Constants.integerFormatter.format(numberOfBugsProcessed) + " bug assignments ...", indentationLevel+5);
									if (testOrReal == Constants.THIS_IS_A_TEST)
										if (j >= testOrReal)
											break; //to consider only one project in the test mode.
									if (totalFMR.errors > 0){
										MyUtils.println("There are errors! Breaking ...", indentationLevel+3);
										break;
									}
									previousAssigneesInThisProject.add(a.login); 
								}
								MyUtils.println(Constants.integerFormatter.format(numberOfBugsProcessed) + " bug assignments predicted.", indentationLevel+5);

								projectCounter++;
								if (testOrReal == Constants.THIS_IS_A_TEST)
									break; //to consider only one project in the test mode.
								//					MyUtils.println("Finished.", indentationLevel+5);
								if (wrapOutputInLines)
									MyUtils.println("-----------------------------------", indentationLevel+4);
							}
						}
					}
					MyUtils.println("Finished.", indentationLevel+4);
					if (wrapOutputInLines)
						MyUtils.println("-----------------------------------", indentationLevel+3);

					Date d7 = new Date();
					float totalRunningTimeForThisAssignmentType_inTheLoop = (float)(d7.getTime()-d4.getTime())/1000;
					AlgPrep.writeAssignmentStats(outputPath, outputSummariesTSVFileName, Constants.ASSIGNMENT_RESULTS_OVERAL_FOLDER_NAME , detailedAssignmentResultsSubfolderName, Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[i], 
							projectsAndTheirAssignmentStats, 
							projectNamesAndTheirIds_orderedByName, projectsAndTheirCommunities, 
							experimentDetails, 
							localFMR, 
							totalRunningTimeForThisAssignmentType_inTheLoop, 
							wrapOutputInLines, showProgressInterval*100, indentationLevel+3, step+"-5");
					totalFMR = MyUtils.addFileManipulationResults(totalFMR, localFMR);
					if (totalFMR.errors > 0){
						MyUtils.println("There are errors! Breaking ...", indentationLevel+3);
						break;
					}
					MyUtils.println("Finished.", indentationLevel+3);
					if (wrapOutputInLines)
						MyUtils.println("-----------------------------------", indentationLevel+2);
				}
				else
					MyUtils.println(step+"- Assignments of \""+Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[i]+"\" --> is not configured to run the prediction algorithm on.", indentationLevel+2);
			}//for (i.

			if (totalFMR.errors == 0)
				MyUtils.println("Finished.", indentationLevel+2);
			else
				MyUtils.println("There are errors! Process stopped!", indentationLevel+3);
			MyUtils.println("Finished.", indentationLevel+2);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel+1);
		}

		Date d8 = new Date();
		float totalLoopTime = (float)(d8.getTime()-d3.getTime())/1000;
		float netAssignmentTime = (float)(d8.getTime()-d3.getTime())/1000 -loopExtraTime_readingCommunities-loopExtraTime_readingAssignmentEvidenceIndexing;

		if (totalFMR.errors == 0)
			MyUtils.println("Finished.", indentationLevel+1);
		else{
			MyUtils.println("Finished with " + totalFMR.errors + " critical errors handling i/o files.", indentationLevel+1);
			MyUtils.println("ERRORS! ERRORS! ERRORS!", indentationLevel+1);
		}
		MyUtils.println("-----------------------------------", indentationLevel);
		if (totalFMR.errors == 0)
			System.out.println("Summary (time, etc.):");
		else{
			System.out.println("Summary (time, etc.), ignoring the above ERRORs:");
			fMR.errors = totalFMR.errors;
		}
		MyUtils.println("Max frequency of words in an evidence:" + AlgPrep.maxFreqOfAWordInAnEvidence + " (just FYI).", indentationLevel);
		MyUtils.println("Total time: " + Constants.floatFormatter.format((float)(d8.getTime()-d1.getTime())/1000) + " seconds.", indentationLevel);
		MyUtils.println("Initial time (Reading graph, bug and project info, before the loop): " + Constants.floatFormatter.format(initialExtraTime__readingGraphBugsAndProjectsInfo), indentationLevel+1);
		MyUtils.println("Initial time (Reading assignments, and, reading and indexing non-assignment evidence, before the loop): " + Constants.floatFormatter.format(initialExtraTime__readingAssignments_and_readingAndIndexingNonAssignmentEvidence), indentationLevel+1);
		MyUtils.println("Whole loop time: " + Constants.floatFormatter.format(totalLoopTime) + " seconds.", indentationLevel+1);
		MyUtils.println("Reading communities files (in the loop): " + Constants.floatFormatter.format(loopExtraTime_readingCommunities), indentationLevel+2);
		MyUtils.println("Reading bugs and indexing extra time (in the loop): " + Constants.floatFormatter.format(loopExtraTime_readingAssignmentEvidenceIndexing), indentationLevel+2);
		MyUtils.println("Net assignment time (in the loop): " + Constants.floatFormatter.format(netAssignmentTime) + " seconds.", indentationLevel+2);
		MyUtils.println("-----------------------------------", indentationLevel);
		MyUtils.println("-----------------------------------", indentationLevel);
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void experiment(){
		int[] assignmentTypesToTriage = new int[]{ //At least one of these items should be equal to YES:
				0/*1=YES, 0=NO*//*T1_AUTHOR*/, 
				0/*1=YES, 0=NO*//*T2_COAUTHOR*/, 
				0/*1=YES, 0=NO*//*T3_ADMIN_CLOSER*/, 
				0/*1=YES, 0=NO*//*T4_DRAFTED_A*/, 
				1/*1=YES, 0=NO*//*T5_ALL_TYPES*/, 
				}; 

		//Evidence types to consider: [bugAssignment, commit, PR, bugComment, commitComment, PRComment]
		int[] evidenceTypes = new int[]{
				1/*1=YES, 0=NO*//*assignedBug=b*/, 
				0/*1=YES, 0=NO*//*commit=c*/, 
				0/*1=YES, 0=NO*//*PR=p*/, 
				0/*1=YES, 0=NO*//*bugComment=bC*/, 
				0/*1=YES, 0=NO*//*commitComment=cC*/, 
				0/*1=YES, 0=NO*//*PRComment=pC*/
				}; 
		int totalEvidenceTypes_count = 6; //above 6 cases.
//		int[] otherAssignmentOptionsToConsider = new int[]{
//				0, //Constants.OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_PROJECT_TITLE_AND_DESCRIPTION_TO_THE_BUG 		--> concatenate "project title and description" to the bug and assignedBugEvidence
//				0, //Constants.OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_MAIN_LANGUAGES_TO_THE_BUG 						--> concatenate "project main language" to the bug and assignedBugEvidence
//				0, //Constants.OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_PROJECT_TITLE_AND_DESCRIPTION_TO_THE_COMMIT	--> concatenate "project title and description" to the commit evidence
//				0  //Constants.OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_MAIN_LANGUAGES_TO_THE_THE_COMMIT				--> concatenate "project main language" to the commit evidence
//				};
		
		boolean isMainRun = true; //: means that we are running the code for all projects.
//		boolean isMainRun = false; //: means that we are running the code for only three test projects ("adobe/brackets", "fog/fog" and "lift/framework").
		
		GeneralExperimentType generalExperimentType = GeneralExperimentType.CALCULATE_OUR_METRIC__TTBA;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.CALCULATE_TBA;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.CALCULATE_VTBA_GH;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE;
//		GeneralExperimentType generalExperimentType = GeneralExperimentType.CALCULATE_VTBA_SOURCECODE;


		//Threshold for considering assignments of developers who fixed at least N bugs: 
			//Default: All bugs should be considered (no filtering); for default status, set the following number to "1":
//		int developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered = 1435;
		int developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered = 1;
		String nodeWeightsInputPath = "";
		String nodeWeightsInputFile = "";
		String additionalNodeWeightsInputPath = "";
		String additionalNodeWeightsInputFileNamePrefix = "";
		
//for (int num=0; num<2; num++)
		for (BTOption1_whatToAddToAllBugs option1_whatToAddToAllBugs: BTOption1_whatToAddToAllBugs.values()){//: What to be added to the bugs by default.
			if (option1_whatToAddToAllBugs != BTOption1_whatToAddToAllBugs.ADD_ML)//In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption1.ADD_mL) has the best performance.
				continue;
			//We need to test just these two cases:
//			if (option1_whatToAddToAllBugs != BTOption1_whatToAddToAllBugs.JUST_USE_BUG_TD && option1_whatToAddToAllBugs != BTOption1_whatToAddToAllBugs.ADD_ML)
//				continue;
			for (BTOption2_w option2_w: BTOption2_w.values()){//: Term weighting
//				if (option2_w != BTOption2_w.USE_TERM_WEIGHTING)
//					continue;
				if (option2_w != BTOption2_w.NO_TERM_WEIGHTING)
					continue;
				for (BTOption3_TF option3_TF: BTOption3_TF.values()){//: TF formula.
//					if (option3_TF != BTOption3_TF.LOG_BASED) //In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption3.LOG_BASED) has the best performance.
//						continue;
//					if (option3_TF != BTOption3_TF.ONE) //In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption3.LOG_BASED) has the best performance.
//						continue;
					if (option3_TF != BTOption3_TF.FREQ__TOTAL_NUMBER_OF_TERMS) //In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption3.LOG_BASED) has the best performance.
						continue;
					for (BTOption4_IDF option4_IDF: BTOption4_IDF.values()){//: IDF formula.
						if (option4_IDF != BTOption4_IDF.FREQ) //In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption4.FREQ) has the best performance.
							continue;
//						if (option4_IDF != BTOption4_IDF.ONE) //In our previous experiment (results stored in Old4-DecidingAbout4Options folder) it was shown that this (BTOption4.FREQ) has the best performance.
//							continue;
						for (BTOption5_prioritizePAs option5_prioritizePAs: BTOption5_prioritizePAs.values()){//: Prioritize previous assignees.
							if (option5_prioritizePAs != BTOption5_prioritizePAs.PRIORITY_FOR_PREVIOUS_ASSIGNEES)
								continue;
//							if (option5_prioritizePAs != BTOption5_prioritizePAs.NO_PRIORITY)
//								continue;
							for (BTOption6_whatToAddToAllCommits option6_whatToAddToAllCommits: BTOption6_whatToAddToAllCommits.values()){//: What to be added to the commits by default.
								if (option6_whatToAddToAllCommits != BTOption6_whatToAddToAllCommits.JUST_USE_COMMIT_M)
									continue; //Currently, we don't need commits in our experiments. So "continue" in three cases and run in just one case.
								for (BTOption7_whenToCountTextLength option7_whenToCountTextLength: BTOption7_whenToCountTextLength.values()){//: Text length before/after non-SO terms removal.
//									if (option7_whenToCountTextLength != BTOption7_whenToCountTextLength.USE_TEXT_LENGTH_AFTER_REMOVING_NON_SO_TAGS)
//										continue; 
									if (option7_whenToCountTextLength != BTOption7_whenToCountTextLength.USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS)
										continue; 
									//The two cases of option7_whenToCountTextLength are used (and may make different results) only when at least one of option3_TF or option4_IDF above are set to their FREQ__TOTAL_NUMBER_OF_TERMS value. So we jump if that's not the case.
//									if (option7_whenToCountTextLength == BTOption7_whenToCountTextLength.USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS 
//										&& option3_TF!=BTOption3_TF.FREQ__TOTAL_NUMBER_OF_TERMS 
//										&& option4_IDF!=BTOption4_IDF.FREQ__TOTAL_NUMBER_OF_TERMS)
//										continue; 
									for (BTOption8_recency option8_recency: BTOption8_recency.values()){
										//"bTD": bugTitleDescription		"pTD: projectTitleDescription		"mL": mainLanguages
//										if (option8_recency != BTOption8_recency.NO_RECENCY)
//											continue;
										if (option8_recency != BTOption8_recency.RECENCY2)
											continue;
										String assignedBugAndUsedBugAsEvidence_Text = "bTD";
										if (evidenceTypes[0] == 1){
											if (option1_whatToAddToAllBugs == BTOption1_whatToAddToAllBugs.ADD_PTD || option1_whatToAddToAllBugs == BTOption1_whatToAddToAllBugs.ADD_PTD_ML)
												assignedBugAndUsedBugAsEvidence_Text = assignedBugAndUsedBugAsEvidence_Text + "pTD";
											if (option1_whatToAddToAllBugs == BTOption1_whatToAddToAllBugs.ADD_ML || option1_whatToAddToAllBugs == BTOption1_whatToAddToAllBugs.ADD_PTD_ML)
												assignedBugAndUsedBugAsEvidence_Text = assignedBugAndUsedBugAsEvidence_Text + "mL";
										}
										String usedCommitAsEvidence_Text = "c";
										if (evidenceTypes[1] == 1){
											if (option6_whatToAddToAllCommits == BTOption6_whatToAddToAllCommits.ADD_PTD || option6_whatToAddToAllCommits == BTOption6_whatToAddToAllCommits.ADD_PTD_mL)
												usedCommitAsEvidence_Text = usedCommitAsEvidence_Text + "pTD";
											if (option6_whatToAddToAllCommits == BTOption6_whatToAddToAllCommits.ADD_mL || option6_whatToAddToAllCommits == BTOption6_whatToAddToAllCommits.ADD_PTD_mL)
												usedCommitAsEvidence_Text = usedCommitAsEvidence_Text + "mL";
										} 
										String inputDir;
										String methodology = ""; 
										switch (generalExperimentType){
										case JUST_CALCULATE_ORIGINAL_TF_IDF:
											methodology = "OnlyOrigTFIDF";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											break;
										case JUST_CALCULATE_TIME_TF_IDF:
											methodology = "OnlyTimeTFIDF";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											break;
										case JUST_CALCULATE_TIME_TF_IDF2:
											methodology = "OnlyTimeTFIDF2";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											break;
										case CALCULATE_TBA:
											methodology = "TBA";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											break;
										case CALCULATE_VTBA_GH:
											methodology = "VTBA_GH";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN;
//											nodeWeightsInputFile = "nodeWeights2-bugs.tsv";
											nodeWeightsInputFile = "nodeWeights2-bugsAndCommitsAndPRsEtc.tsv";
											break;
										case CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE:
											methodology = "VTBA_GH_ONLINE";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN;
//											nodeWeightsInputFile = "nodeWeights2-bugs.tsv";
											nodeWeightsInputFile = "nodeWeights2-bugsAndCommitsAndPRsEtc.tsv";
											break;
										case CALCULATE_VTBA_SOURCECODE:
											methodology = "VTBA_SOURCECODE";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											additionalNodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN;
											additionalNodeWeightsInputFileNamePrefix = "nodeWeights3-sourceCode-";
											break;
										default: //CALCULATE_OUR_METRIC__TTBA
											 methodology = "OurTTBAMethod";
											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN;
											nodeWeightsInputPath = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT;
											nodeWeightsInputFile = "nodeWeights.tsv";
											break;
										}
//										if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF){
//											methodology = "OnlyOrigTFIDF";
//											inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
//										}
//										else
//											if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF){
//												methodology = "OnlyTimeTFIDF";
//												inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF;
//											}
//											else{
//												methodology = "OurTTBAMethod";
//												inputDir = Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN;
//											}
										
										switch (option2_w){//: Term weighting:
											case NO_TERM_WEIGHTING:
												methodology = methodology + "+" + "noW";
												break;
											case USE_TERM_WEIGHTING:
												methodology = methodology + "+" + "w__";
												break;
										}
										switch (option3_TF){//: TF formula:
											case ONE:
												methodology = methodology + "+" + "TF_one";
												break;
											case FREQ:
												methodology = methodology + "+" + "TF_fre";
												break;
											case FREQ__TOTAL_NUMBER_OF_TERMS:
												methodology = methodology + "+" + "TF_F_T";//TF_freq_numOfTerms
												break;
											case LOG_BASED:
												methodology = methodology + "+" + "TF_Log";
												break;
										}
										switch (option4_IDF){//: IDF formula:
											case ONE:
												methodology = methodology + "+" + "IDF_one";
												break;
											case FREQ:
												methodology = methodology + "+" + "IDF_fre";
												break;
											case FREQ__TOTAL_NUMBER_OF_TERMS:
												methodology = methodology + "+" + "IDF_F_T";
												break;
											case LOG_BASED:
												methodology = methodology + "+" + "IDF_log";
												break;
										}
										switch (option5_prioritizePAs){
											case NO_PRIORITY:
												methodology = methodology + "+" + "noP";
												break;
											case PRIORITY_FOR_PREVIOUS_ASSIGNEES:
												methodology = methodology + "+" + "pri";
												break;
										}
										switch (option7_whenToCountTextLength){
											case USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS:
												methodology = methodology + "+" + "tL_b"; //tL: text Length     b:before removing SO tags
												break;
											case USE_TEXT_LENGTH_AFTER_REMOVING_NON_SO_TAGS:
												methodology = methodology + "+" + "tL_a"; //textLength: afterRemSOTags
												break;
										}
										switch (option8_recency){
											case NO_RECENCY:
												methodology = methodology + "+" + "nR"; //nR: noRecency
												break;
											case RECENCY1:
												methodology = methodology + "+" + "r1"; //r1: recency1
												break;
											case RECENCY2:
												methodology = methodology + "+" + "r2"; //r2: recency2
												break;
										}
										//							methodology = "simpleWordCount+tf1+(wAC_count_totalNumberOfWords)+w";//"tfLog+w+r3" tfLog+wLog
										//							String methodology = "tfLog+w+r3+noPriorityToPreviousA";//"tfLog+w+r3"
										String[] evidenceTypesText = new String[]{
												assignedBugAndUsedBugAsEvidence_Text, 
												usedCommitAsEvidence_Text,
												"p",
												"bc",
												"cC",
												"pC"
										};

										//Determining the experiment title automatically based on the chosen evidence types (for the developer) and the algorithm methodology:
										String experimentTitle = "";//"bTD+pTD - bTD+c - tf+w+r1"    /* bugInfo - expertiseInfo - assignmentMethod*/
										for (int i=0; i<totalEvidenceTypes_count; i++)
											if (evidenceTypes[i] == 1)
												experimentTitle = StringManipulations.concatTwoStringsWithDelimiter(experimentTitle, evidenceTypesText[i], "+");
										experimentTitle = StringManipulations.concatTwoStringsWithDelimiter(assignedBugAndUsedBugAsEvidence_Text, experimentTitle, " - ");
										experimentTitle = experimentTitle + " - " + methodology;

										FileManipulationResult fMR = new FileManipulationResult();
//										for (int num=0; num<3; num++)

										for (int num=0; num<1; num++)
											bugAssignment(inputDir, nodeWeightsInputPath, nodeWeightsInputFile, 
												additionalNodeWeightsInputPath, additionalNodeWeightsInputFileNamePrefix, 
												Constants.DATASET_DIRECTORY_FOR_THE_ALGORITHM__EXPERIMENT_OUTPUT, "outSum",
												isMainRun, assignmentTypesToTriage, evidenceTypes, totalEvidenceTypes_count, 
												experimentTitle, "-",
												option1_whatToAddToAllBugs, option2_w, option3_TF, option4_IDF, option5_prioritizePAs, option6_whatToAddToAllCommits, option7_whenToCountTextLength, option8_recency,
												generalExperimentType, developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered, 
												fMR,
												false, 5000, 0, Constants.THIS_IS_REAL, "");		
										if (fMR.errors > 0){
											MyUtils.println("Error in experiment()!", 0);
											return;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
		//This method will be called every time to assign bugs to developers and save the results in output files:
		experiment();
	}//main().
}


























