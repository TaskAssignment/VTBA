package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import data.Assignee;
import data.Assignment;
import data.AssignmentStat;
import data.AssignmentStatSummary;
import data.Bug;
import data.Evidence;
import data.Project;
import utils.Constants;
import utils.Constants.ASSIGNMENT_TYPES_TO_TRIAGE;
import utils.Constants.BTOption1_whatToAddToAllBugs;
import utils.Constants.BTOption2_w;
import utils.Constants.BTOption3_TF;
import utils.Constants.BTOption4_IDF;
import utils.Constants.BTOption5_prioritizePAs;
import utils.Constants.BTOption6_whatToAddToAllCommits;
import utils.Constants.BTOption7_whenToCountTextLength;
import utils.Constants.BTOption8_recency;
import utils.Constants.GeneralExperimentType;
import utils.Constants.ProjectType;
import utils.FileManipulationResult;
import utils.Graph;
import utils.MyUtils;
import utils.StringManipulations;

public class AlgPrep {
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	private static final String TAB = Constants.TAB;
	public static int maxFreqOfAWordInAnEvidence = 0;
	public static final String ALL_PROJECTS = "ALL_PROJECTS";
	public static Random random = new Random();			
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//The following method returns project type (one of the 13 main FASE projects, 3 other projects, project families or other (unknown)).
	public static ProjectType projectType(String projectId, String owner_repo){
		String[] listOf13Projects__owner_repo = {"rails/rails", "yui/yui3", "lift/framework", "fog/fog", "julialang/julia", "angular/angular.js", "elastic/elasticsearch", 
				"travis-ci/travis-ci", "saltstack/salt", "khan/khan-exercises", "adobe/brackets", "html5rocks/www.html5rocks.com", "tryghost/ghost", };
		String[] listOf13Projects__id = {"8514", "85670", "1295197", "203666", "1644196", "460078", "507775", 
				"1420493", "1390248", "1723225", "2935735", "5238231", "9852918", };
		
		String[] listOf3ProjectsWithNoPublicBugs__owner_repo = {"scala/scala", "mozilla-b2g/gaia", "edx/edx-platform"}; 
		String[] listOf3ProjectsWithNoPublicBugs__id = {"2888818", "2317369", "10391073"}; 
		
		String[] listOfFamiliesOfTwoProjects__owner_repo = {"rails/activeresource", "rails/arel", "rails/sprockets", "rails/jquery-rails", 
				"rails/execjs", "rails/sass-rails", "rails/jbuilder", "rails/strong_parameters", "rails/sprockets-rails", 
				"rails/protected_attributes", "rails/spring", "rails/web-console", "rails/globalid", 
				"angular/angular-seed", "angular/angularjs.org", "angular/angular-phonecat", 
				"angular/protractor", "angular/dgeni-packages", "angular/material"}; 
		String[] listOfFamiliesOfTwoProjects__id = {"3711416", "337788", "32104924", "1795951", 
				"32104914", "1795273", "2861056", "3710607", "1784628", 
				"5674986", "7362671", "12496351", "22991474", 
				"1195004", "1343653", "1452079", 
				"7639232", "16757508", "21399598"}; 

		for (int i=0; i<listOf13Projects__owner_repo.length; i++)
			if (owner_repo.equals(listOf13Projects__owner_repo[i]) || projectId.equals(listOf13Projects__id[i]))
				return ProjectType.FASE_13;
		for (int i=0; i<listOfFamiliesOfTwoProjects__owner_repo.length; i++)
			if (owner_repo.equals(listOfFamiliesOfTwoProjects__owner_repo[i]) || projectId.equals(listOfFamiliesOfTwoProjects__id[i]))
				return ProjectType.FASE_13_EXTENSION__PROJECT_FAMILIES_OF_TWO_PROJECTS;
		for (int i=0; i<listOf3ProjectsWithNoPublicBugs__owner_repo.length; i++)
			if (owner_repo.equals(listOf3ProjectsWithNoPublicBugs__owner_repo[i]) || projectId.equals(listOf3ProjectsWithNoPublicBugs__id[i]))
				return ProjectType.FASE_3__NO_PUBLIC_BUGS;
		return ProjectType.OTHERS_UNKNOWN;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//The following method returns true if the project is one of the three projects considered for tuning. Otherwise returns false.
	public static boolean isAProjectWhichIsUsedForTuning(String projectId, String owner_repo){
		String[] listOf3ProjectsForTuning__owner_repo = {"lift/framework", "fog/fog", "adobe/brackets"}; 
		String[] listOf3ProjectsForTuning__id = {"1295197", "203666", "2935735"}; 
		
		for (int i=0; i<listOf3ProjectsForTuning__owner_repo.length; i++)
			if (owner_repo.equals(listOf3ProjectsForTuning__owner_repo[i]) || projectId.equals(listOf3ProjectsForTuning__id[i]))
				return true;
		return false;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static boolean isAProjectWhichIsUsedForMainRun(String projectId, String owner_repo){
		String[] listOf10ProjectsForMainRun__owner_repo = {"rails/rails", "yui/yui3", "julialang/julia", "angular/angular.js", "elastic/elasticsearch", 
				"travis-ci/travis-ci", "saltstack/salt", "khan/khan-exercises", "html5rocks/www.html5rocks.com", "tryghost/ghost"}; 
		String[] listOf10ProjectsForMainRun__id = {"8514", "85670", "1644196", "460078", "507775", 
				"1420493", "1390248", "1723225", "5238231", "9852918"}; 
		
		for (int i=0; i<listOf10ProjectsForMainRun__owner_repo.length; i++)
			if (owner_repo.equals(listOf10ProjectsForMainRun__owner_repo[i]) || projectId.equals(listOf10ProjectsForMainRun__id[i]))
				return true;
		return false;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static Assignee updateRankOfRealAssigneesAndReturnTheBestAssignee(HashMap<String, HashMap<String, Integer>> realAssignees, 
			String bugNumber, HashMap<String, Double> scores, Random random){
		//Rank the list of all community members, then update the ranks of real assignees in realAssignees. Finally return the assignee with the best rank: 
		//This method checks the score of all real assignees of the given bug number against all the other scores.
			//Then updates the ranks of real assignees in realAssignees. Finally returns the assignee with the highest rank.
				//Note1: if two developers has the same score, considers a random ordering.
				//Note2: It does not necessarily sort the scores. For each real assignee, we just want to obtain a and b; 
					//a: how many scores are higher than the score of those real assignees, and b: how many are equal to them.
					//Then the best rank of real assignee is somewhere between a+1 and a+b+1.
		Assignee topRA = new Assignee("", Constants.AN_EXTREMELY_POSITIVE_INT);
		
		HashMap<String, Integer> realAssigneesOfThisBug = realAssignees.get(bugNumber);
		HashSet<Integer> bookedRanks = new HashSet<Integer>();
		for (String login: realAssigneesOfThisBug.keySet()){//:for each of the real assignees of this bug, we need to count a and b:
			//First count the number of developers with higher score than this "real assignee developer"'s score (and also the number of developers equal to it):
			int a = 0;
			int b = 0;
			Double scoreOfThisRA = scores.get(login);
			for (Double aScore: scores.values()){
				if (aScore > scoreOfThisRA)
					a++;
				else
					if (aScore.equals(scoreOfThisRA))
						b++; // this value will be at least one, because the score of a developer is at least equal to his score.
			}
			//Now, obtain the ranks for these "real assignee developer"s: 
			//rank of this assignee = 
				//fairRandomRank = a + random.nextInt(b)+1
					//but we need to make sure there is no other real assignees with the same fairRandomRank.
					//so we check in bookedRanks and if there is any, assign another rank until we find a non-existing fairRandomRank:
			int fairRandomRank;
			if (b == 1) //: if there is no other developer with this rank:
				fairRandomRank = a + 1;
			else{
				fairRandomRank = a + random.nextInt(b) + 1;
				while (bookedRanks.contains(fairRandomRank))//: guarantee that no other assignee gets the same fairRandomRank:
					fairRandomRank = a + random.nextInt(b) + 1;
				bookedRanks.add(fairRandomRank); //: this fairRandomRank is reserved for this real assignee. Record it so for the next real assignees we can check against.
			}
			realAssigneesOfThisBug.put(login, fairRandomRank); //: this is to affect realAssigneesOfThisBug and hence realAssignees. At the end, all the real assignees of this bug are set with their ranks regarding their scores for that bug.
			if (fairRandomRank < topRA.rank){//: set the topRA to be returned at the end:
				topRA.rank = fairRandomRank;
				topRA.login = login;
			}
		}
		return topRA;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static double calculateScoreOfDeveloperForBugAssignment(String login, Assignment a, 
			Graph graph, Graph updatingGraph, 
			int assignmentTypeToTriage, int[] evidenceTypesToConsider, int evidenceTypesToConsider_count, 
			HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>> logins_Tags_TypesAndTheirEvidence_InAProject,
			HashSet<String> previousAssigneesInThisProject, 
			WordsAndCounts wAC, int originalNumberOfWordsInBugText, 
			int seqNum, //seqNum is the sequence number of the bug. It is used for determining the recency based on FASE paper formula (number of bugs between a bugAssignmentEvidence and the current bug). 
			Date beginningDateOfProject, 
			GeneralExperimentType generalExperimentType, int numberOfCommunityMembers, HashMap<String, HashMap<String, Date>> wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate, //"java"--> <"bob", 1/1/1>
			HashMap<String, HashMap<String, HashSet<Date>>> wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates /*"java"--> <"bob", <2019/1/1, 2018/2/2, 2019/3/3, ...>>*/, 
			BTOption2_w option2_w, BTOption4_IDF option4_IDF, BTOption5_prioritizePAs option5_prioritizePAs, BTOption8_recency option8_recency, 
			int indentationLevel){
		//This method calculates the score of developer "login" for assignment "a". 
		//		It considers the evidence of expertise from beginning of project until the time of "a". 
		//			Later, it also considers the evidence in other projects (the project family experiment) using projectsAndTheirAssignments.
		Double score = 0.0;
		Double subScore = 0.0;
		if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF || generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF || generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2){
			
//			HashMap<String, HashMap<String, Date>> hh = new HashMap<String, HashMap<String, Date>>();
//			hh.get("a").size()
			
					
			int error_A = 0;
			if (logins_Tags_TypesAndTheirEvidence_InAProject.containsKey(login)){
				HashMap<String, HashMap<Integer, ArrayList<Evidence>>> tags_TypesAndTheirEvidence_ForADeveloperInAProject = logins_Tags_TypesAndTheirEvidence_InAProject.get(login);
				for (int i=0; i<wAC.size; i++){//: Iterating over keywords (tags) of bug.
					if (tags_TypesAndTheirEvidence_ForADeveloperInAProject.containsKey(wAC.words[i])){//: means that if this user has an evidence including this tag.
						HashMap<Integer, ArrayList<Evidence>> typesAndEvidenceOfThisDeveloperForATag = tags_TypesAndTheirEvidence_ForADeveloperInAProject.get(wAC.words[i]); //: this is assuming that the non-SO-tag keywords are removed from the text of a bug.
						for (int et_index=0; et_index<evidenceTypesToConsider_count; et_index++){//et: "evidence type"
							int et = evidenceTypesToConsider[et_index];
							if (typesAndEvidenceOfThisDeveloperForATag.containsKey(et)){
								ArrayList<Evidence> type_x_evidenceOfADeveloperForATag = typesAndEvidenceOfThisDeveloperForATag.get(et); //: get specific evidence types (e.g., bug title, bug description, commit message, etc.)
								int numberOfType_x_evidence = type_x_evidenceOfADeveloperForATag.size();
								for (int j=0; j<numberOfType_x_evidence; j++){//: Iterating over all evidence (of the current user in the current project) for this tag.
									Evidence e = type_x_evidenceOfADeveloperForATag.get(j);
									if (e.date.compareTo(a.date) < 0){ //: Only consider the evidence before the date of assignment "a".  
										if (wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.containsKey(wAC.words[i])){
											int numberOfDevelopersUsedTheTerm = wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.get(wAC.words[i]).size();
											HashMap<String, Date> developers_lastUsageDate = wordsAnd_theDevelopersUsedThemUpToNow_lastUsageDate.get(wAC.words[i]);
											if (developers_lastUsageDate.containsKey(login)){					
												if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF){
													subScore = subScore + wAC.counts[i] * e.tf * Math.log(numberOfCommunityMembers / numberOfDevelopersUsedTheTerm); //subScore = subScore + tf(term, bug) * idf(term, allDevs)
												}
												else{//means that generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF or generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2:
													if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF){
														int dayDiff = MyUtils.getDifferenceInDays(a.date, developers_lastUsageDate.get(login)); //dayDiff: time difference between "date of usage of the term by the developer" and "date of the new bug". 
														if (dayDiff == 0) //If the two dates above are the same, dayDiff becomes 0. So, we will consider it as 1, since it is going to the denominator, in the recency formula of Time-TF-IDF
															dayDiff = 1;
														Double recency_for_Time_TF_IDF = 1.0/numberOfDevelopersUsedTheTerm + 1.0/(Math.sqrt(dayDiff));
														subScore = subScore + recency_for_Time_TF_IDF * wAC.counts[i] * e.tf * Math.log(numberOfCommunityMembers / numberOfDevelopersUsedTheTerm); //subScore = subScore + tf(term, bug) * idf(term, allDevs)
													}
												}
											}
											else{
												error_A++;
												break;
											}
										}
										else
											if (wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.containsKey(wAC.words[i])){ //: This is used for GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2
												int numberOfDevelopersUsedTheTerm = wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.get(wAC.words[i]).size();
												HashMap<String, HashSet<Date>> developers_allUsageDates = wordsAnd_theDevelopersUsedThemUpToNow_allUsageDates.get(wAC.words[i]);
												if (developers_allUsageDates.containsKey(login)){					
													if (generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2){
														Double recency_for_Time_TF_IDF = 1.0/numberOfDevelopersUsedTheTerm;
														HashSet<Date> allUsageDates = developers_allUsageDates.get(login);
														for (Date d: allUsageDates){
															int dayDiff = MyUtils.getDifferenceInDays(a.date, d); //dayDiff: time difference between "date of usage of the term by the developer" and "date of the new bug". 
															if (dayDiff == 0) //If the two dates above are the same, dayDiff becomes 0. So, we will consider it as 1, since it is going to the denominator, in the recency formula of Time-TF-IDF
																dayDiff = 1;
															recency_for_Time_TF_IDF = recency_for_Time_TF_IDF + 1.0/(Math.sqrt(dayDiff));
														}
														subScore = subScore + recency_for_Time_TF_IDF * wAC.counts[i] * e.tf * Math.log(numberOfCommunityMembers / numberOfDevelopersUsedTheTerm); //subScore = subScore + tf(term, bug) * idf(term, allDevs)
													}
												}
												else{
													error_A++;
													break;
												}
											}
											else{
												error_A++;
												break;
										}
									}
								}
							}
						}
					}
				}			
			}

			if (error_A > 0)
				System.out.println(error_A + " ERRORS-A in calculateScoreOfDeveloperForBugAssignment(): the word entry is missing for calculating idf!");
			score = subScore;
			if (option5_prioritizePAs == BTOption5_prioritizePAs.PRIORITY_FOR_PREVIOUS_ASSIGNEES)//: Prioritize previous assignees
				if (previousAssigneesInThisProject.contains(login))
					score = score + 10000;
		}
		else{
			Double termWeight;
			int errors1 = 0;
			int errors2 = 0;
			int errors3_possibly = 0;
			int errors4_bothAreOne = 0;
			if (logins_Tags_TypesAndTheirEvidence_InAProject.containsKey(login)){ //: means that if this user has an evidence ever!
				//			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				Date assignmentDate = a.date;
				//			Date beginningDate = dateFormat.parse(beginningDateOfProject);
				Date evidenceDate;
				double recency2 = 1;
				HashMap<String, HashMap<Integer, ArrayList<Evidence>>> tags_TypesAndTheirEvidence_ForADeveloperInAProject = logins_Tags_TypesAndTheirEvidence_InAProject.get(login);
				for (int i=0; i<wAC.size; i++){//: Iterating over keywords (tags) of bug.
					if (tags_TypesAndTheirEvidence_ForADeveloperInAProject.containsKey(wAC.words[i])){//: means that if this user has an evidence including this tag.
						HashMap<Integer, ArrayList<Evidence>> typesAndEvidenceOfThisDeveloperForATag = tags_TypesAndTheirEvidence_ForADeveloperInAProject.get(wAC.words[i]); //: this is assuming that the non-SO-tag keywords are removed from the text of a bug.
						if (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE)
							termWeight = updatingGraph.getNodeWeight(wAC.words[i]);
						else
							termWeight = graph.getNodeWeight(wAC.words[i]);
						//Considering all different types of evidence (0: Constants.EVIDENCE_TYPE__BUG_TITLE to Constants.EVIDENCE_TYPES__COUNT-1):
						subScore = 0.0;
						for (int et_index=0; et_index<evidenceTypesToConsider_count; et_index++){//et: "evidence type"
							int et = evidenceTypesToConsider[et_index];
							if (typesAndEvidenceOfThisDeveloperForATag.containsKey(et)){
								ArrayList<Evidence> type_x_evidenceOfADeveloperForATag = typesAndEvidenceOfThisDeveloperForATag.get(et); //: get specific evidence types (e.g., bug title, bug description, commit message, etc.)
								int numberOfType_x_evidence = type_x_evidenceOfADeveloperForATag.size();
								for (int j=0; j<numberOfType_x_evidence; j++){//: Iterating over all evidence (of the current user in the current project) for this tag.
									Evidence e = type_x_evidenceOfADeveloperForATag.get(j);
									if (e.date.compareTo(a.date) < 0){ //: Only consider the evidence before the date of assignment "a".  
										evidenceDate = e.date;
										if (et < Constants.NUMBER_OF_ASSIGNEE_TYPES){ //(case #1): 0 to 4, which are the assignment types.
											if (e.bASeqNum >= seqNum)
												errors1++;
											if (e.bASeqNum == Constants.SEQ_NUM____THIS_IS_NOT__B_A_EVIDENCE)
												System.out.println("ERROR!");
											if (option8_recency == BTOption8_recency.RECENCY2) //note: here, we just calculate recency2. recency1 is the same for case #1 and case #2 (will be calculated directly in the subScore formula later).
												recency2 = 1.0/(seqNum - e.bASeqNum); //case #1: This is the recency for bug assignment evidence.
										}
										else{//(case #2): 11 (Constants.EVIDENCE_TYPE_COMMIT) to 15 (EVIDENCE_TYPE_PR_COMMENT).
											if (e.nonBA_virtualSeqNum[assignmentTypeToTriage] > seqNum)
												errors2++;
											if (e.nonBA_virtualSeqNum[assignmentTypeToTriage] == seqNum){
												errors3_possibly++;
												System.out.println("seqNum: " + seqNum);
												if (e.nonBA_virtualSeqNum[assignmentTypeToTriage] == 1)
													errors4_bothAreOne++;
											}
											//											System.out.println("ERROR! The sequence number of the bug is smaller than the sequence number of the evidence!");
											if (e.nonBA_virtualSeqNum[assignmentTypeToTriage] == Constants.SEQ_NUM____NO_NEED_TO_TRIAGE_THIS_TYPE___OR___THIS_IS_NOT__NON_B_A_EVIDENCE)
												System.out.println("ERROR!");
											if (option8_recency == BTOption8_recency.RECENCY2) //note: here, we just calculate recency2. recency1 is the same for case #1 and case #2 (will be calculated directly in the subScore formula later).
												recency2 = 1.0/(seqNum - e.nonBA_virtualSeqNum[assignmentTypeToTriage]); //case #2: This is the recency for other types of evidence.
										}

										//option3_TF is included in the Evidence indexing (addToIndex() and indexAssignmentEvidence() and readAndIndexNonAssignmentEvidence()). Here, we just read the value:
										switch (option8_recency){
										case NO_RECENCY:
											subScore = subScore + e.tf;
											break;
										case RECENCY1: //subScore = subScore + e.tf * recency1:
											subScore = subScore + e.tf * ((double)(evidenceDate.getTime()-beginningDateOfProject.getTime())/(long)(assignmentDate.getTime()-beginningDateOfProject.getTime()));
											break;
										case RECENCY2: //subScore = subScore + e.tf * recency2:
											subScore = subScore + e.tf*recency2; //: This is the recency that is calculated based on one of the two cases above (case #1 and case #2).
											break;
										}
										//									subScore = subScore + e.tf; //*recency*context or *recency*e.type...
										//									subScore = subScore + e.freq*recency1; //*recency*context or *recency*e.type...
										//									subScore = subScore + e.tf*recency1*Constants.TYPE_SIMILARITY[et]; //*recency*context or *recency*e.type...
										//									subScore = subScore + e.tf*recency3*Constants.TYPE_SIMILARITY[et]; //*recency*context or *recency*e.type...
										//									subScore = subScore + e.tf*recency2; //*recency*context or *recency*e.type...

										//									recency1 = ((double)(evidenceDate.getTime()-beginningDateOfProject.getTime())/(long)(assignmentDate.getTime()-beginningDateOfProject.getTime()));
										//									recency2 = 1.2 - java.lang.Math.log10(99+(assignmentDate.getTime()-evidenceDate.getTime())/1000)/10;
									}
									else //: Since the evidence are ordered by date, break if the date is not before the date of assignment:
										break;				
								} //for (j
							}
						}
						switch (option2_w){//: Term weighting
						case NO_TERM_WEIGHTING:
							switch (option4_IDF){//: IDF formula
							case ONE:
								score = score + subScore;
								break;
							case FREQ:
								score = score + subScore * wAC.counts[i];
								break;
							case FREQ__TOTAL_NUMBER_OF_TERMS:
								score = score + subScore * wAC.counts[i]/wAC.totalNumberOfWords;
								break;
							case LOG_BASED:
								score = score + subScore * (1+Math.log10(wAC.counts[i]));
								break;
							}
						case USE_TERM_WEIGHTING:
							switch (option4_IDF){//: IDF formula
							case ONE:
								score = score + subScore * termWeight;
								break;
							case FREQ:
								score = score + subScore * termWeight * wAC.counts[i];
								break;
							case FREQ__TOTAL_NUMBER_OF_TERMS:
								score = score + subScore * termWeight * wAC.counts[i]/wAC.totalNumberOfWords;
								break;
							case LOG_BASED:
								score = score + subScore * termWeight * (1+Math.log10(wAC.counts[i]));
								break;
							}
						}
						//					score = score + termWeight * subScore * wAC.counts[i]/wAC.totalNumberOfWords; //
						//					score = score + subScore * wAC.counts[i];

						//					score = score + subScore;
						//					score = score + subScore;
						//					if (wAC.counts[i] == 1)
						//						score = score + termWeight * subScore;
						//					else
						//						score = score + termWeight * subScore * (1+Math.log10(wAC.counts[i]));

						//					System.out.println(1+Math.log(wAC.counts[i]));

						//testing if the termWeight is wrong:
						//						score = score + subScore/termWeight;
					}
				}//for (i
			}
			if (errors1>0)
				System.out.println(errors1 + " ERRORS1 in seqNum1: The sequence number of the assignment evidence is greater than the sequence number of the bug!");
			if (errors2>0)
				System.out.println(errors2 + " ERRORS2 in seqNum2: The sequence number of the non-assignment evidence is greater than the sequence number of the bug!");
			if (errors3_possibly>0)
				System.out.println(errors3_possibly + " Possible ERRORS3 in seqNum2: The sequence number of the non-assignment evidence is equal to the sequence number of the bug!");
			if (errors4_bothAreOne>0)
				System.out.println(errors4_bothAreOne + " Possible ERRORS4 in seqNum2: The sequence number of the non-assignment evidence is equal to the sequence number of the bug and both are equal to 1!");
			if (errors1>0 || errors2>0 || errors3_possibly>0 || errors4_bothAreOne>0)
				System.out.println("ERROR");

			//		for (int i=0; i<10; i++)
			//			if (score > Constants.highScores[i]){
			//				Constants.highScores[i] = score;
			//				break;
			//			}

			if (option5_prioritizePAs == BTOption5_prioritizePAs.PRIORITY_FOR_PREVIOUS_ASSIGNEES){//: Prioritize previous assignees
				if (previousAssigneesInThisProject.contains(login))
					score = score + 10000;
			}
		} //else of if (justCalculateOriginalTFIDF).
		return score;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void writeAssignmentStats(String outputPath, String overalSummariesOutputTSVFileName, String assignmentResultsOveralFolderName, String detailedAssignmentResultsSubfolderName, String detailedSummaryOutputFileNameSuffix,
			TreeMap<String, ArrayList<AssignmentStat>> projectsAndTheirAssignmentStats, 
			TreeMap<String, String> projectNamesAndTheirIds, TreeMap<String, ArrayList<String[]>> projectsAndTheirCommunities, //these two params are for getting project id (ordered by project title) and also the total number of developers in each project.
			String experimentDetails, 
			FileManipulationResult fMR,
			float totalRunningTime, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, String writeMessageStep){
		if (wrapOutputInLines) 
			MyUtils.println("-----------------------------------", indentationLevel);
		MyUtils.println(writeMessageStep+"- Summarizing assignment statistics and writing:", indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);
		try {
			//Detailed stats:
			MyUtils.println(writeMessageStep+"-1- Detailed stats (in 5 files in a separate folder) ...", indentationLevel+1);
			FileWriter writer1 = new FileWriter(outputPath+"\\"+assignmentResultsOveralFolderName+"\\"+detailedAssignmentResultsSubfolderName+"\\"+detailedAssignmentResultsSubfolderName+" - "+detailedSummaryOutputFileNameSuffix+".tsv");
			writer1.append("project" + TAB + "bugNumber" + TAB + "assignmentDate" + TAB + "ourTopRecommendedRealAssignee" 
					+ TAB + "ourTopRecommendedRealAssigneeRank" + TAB + "totalCommunityMembers" + TAB + "realAssigneesTillNow" + "\n");
			int totalNOA = 0; //: total number of assignments.
			int min = Integer.MAX_VALUE; //: minNumberOfCommunityMembersInOneProject
			int max = 0; //: maxNumberOfCommunityMembersInOneProject
			HashMap<String, AssignmentStatSummary> projectsAndTheirAssignmentStatSummaries = new HashMap<String, AssignmentStatSummary>(); 
			AssignmentStatSummary aST_overal = new AssignmentStatSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
			for (String owner_repo: projectNamesAndTheirIds.keySet()){
				String projectId = projectNamesAndTheirIds.get(owner_repo);
				if (projectsAndTheirAssignmentStats.containsKey(projectId)){
					int m = projectsAndTheirCommunities.get(projectId).size(); //: Community size.
					if (m > max)
						max = m;
					if (m < min)
						min = m;
					//Adding up to the sum values for this project (will be divided by n later):
						//In fact, these are not the accuracies, etc., but the sum values. 
					ArrayList<AssignmentStat> assignmentStatsOfOneProject = projectsAndTheirAssignmentStats.get(projectId);
					AssignmentStatSummary a = new AssignmentStatSummary(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
					a.n = assignmentStatsOfOneProject.size(); //: number of bug assignments in this project.
					for (int i=0; i<assignmentStatsOfOneProject.size(); i++){
						AssignmentStat as = assignmentStatsOfOneProject.get(i);
						//Writing detailed assignment and prediction info:
						writer1.append(owner_repo + TAB + as.bugNumber + TAB + as.assignmentDate + TAB + as.ourTopRecommendedAssignee
								+ TAB + as.ourTopRecommendedAssigneeRank + TAB + m + TAB + as.getRealAssignees() + "\n");

						//calculating the high level statistics (to be written later [out of this loop]):
						if (as.ourTopRecommendedAssigneeRank < 11){//: <=10
							a.sumTop10++;
							if (as.ourTopRecommendedAssigneeRank < 6){//: <=5
								a.sumTop5++;
								if (as.ourTopRecommendedAssigneeRank == 1){
									a.sumTop1++;
									a.sumPAt1 = a.sumPAt1 + 1; //: "precision at 1" is actually the same as "top 1 accuracy".
									a.sumRAt1 = a.sumRAt1 + 1.0/as.realAssigneesAndTheirRanksForThisAssignment.size();
								}
							}
						}
						double RR = 0; //Reciprocal Rank 
						int topRank = Constants.AN_EXTREMELY_POSITIVE_INT;
						for (int rank: as.realAssigneesAndTheirRanksForThisAssignment.values()){
							RR = RR + 1.0/rank;
							if (rank < topRank)
								topRank = rank;
						}
						RR = RR / as.realAssigneesAndTheirRanksForThisAssignment.size();//: get the average over all real assignees for this assignment.
//						a.sumRR = a.sumRR + RR; //: adding to sumRR, to be divided by total number of bugs later.
						a.sumRR = a.sumRR + 1.0/topRank; //Because MRR only cares about the top assignee's rank. So I commented the above statement and added this line.
						
						//AP (and its sum for different assignments; sumAP) needs to take into account average precision, which needs precision at certain points (i.e., at points that there is an assignee). And precision needs to know #ofRecommendationsMade before a position:
						double AP = 0; //Average Precision
						int numOfRAsInTop5 = 0; //: number of real assignees in top 5 ranks.
						int numOfRAsInTop10 = 0; //: number of real assignees in top 10 ranks.
						if (as.realAssigneesAndTheirRanksForThisAssignment.size() == 1){
							AP = RR;
							int rank = (int)as.realAssigneesAndTheirRanksForThisAssignment.values().toArray()[0];
//							for (int r: as.realAssigneesAndTheirRanksForThisAssignment.values())
//								rank = r; //: there is only one item in the hashMap. So the loop does not repeat!
							if (rank < 11){
								numOfRAsInTop10++;
								if (rank < 6)
									numOfRAsInTop5++;
							}
						}
						else{ //: if there are more than one assignee, we need to process them in order (from the best rank to the worst):
							ArrayList<Integer> ranks = new ArrayList<Integer>();
							for (int rank: as.realAssigneesAndTheirRanksForThisAssignment.values())
								ranks.add(rank);
							Collections.sort(ranks);
							for (int j=0; j<ranks.size(); j++){
								AP = AP + (j+1.0)/ranks.get(j); //: "j+1" is the number of of assignees (hits or appropriate recommendations). "ranks.get(j)" is the rank of real assignee which includes number of (right and wrong) predictions (or number of recommendations made). 
								if (ranks.get(j) < 11){
									numOfRAsInTop10++;
									if (ranks.get(j) < 6)
										numOfRAsInTop5++;
								}
								else //: if this rank is 11+, then the next ranks are worse than that!
									break;
							}
							AP = AP / ranks.size(); //: to obtain the "Average" precision.
						}
						a.sumAP = a.sumAP + AP; 
						a.sumPAt5 = a.sumPAt5 + numOfRAsInTop5/5.0;
						a.sumRAt5 = a.sumRAt5 + (double)numOfRAsInTop5/as.realAssigneesAndTheirRanksForThisAssignment.size();
						a.sumPAt10 = a.sumPAt10 + numOfRAsInTop10/10.0;
						a.sumRAt10 = a.sumRAt10 + (double)numOfRAsInTop10/as.realAssigneesAndTheirRanksForThisAssignment.size();
					}
					totalNOA = totalNOA + a.n; //: total number of assignments.
					projectsAndTheirAssignmentStatSummaries.put(projectId, a); 

					//Adding up to the sum values to be considered later for ALL_PROJECTS (will be divided by n later):
						//In fact, these are not the accuracies, etc., but the sum values. 
					aST_overal.n = aST_overal.n + a.n;
					aST_overal.sumTop1 = aST_overal.sumTop1 + a.sumTop1;
					aST_overal.sumTop5 = aST_overal.sumTop5 + a.sumTop5;
					aST_overal.sumTop10 = aST_overal.sumTop10 + a.sumTop10;
					aST_overal.sumRR = aST_overal.sumRR + a.sumRR;
					aST_overal.sumAP = aST_overal.sumAP + a.sumAP;
					aST_overal.sumPAt1 = aST_overal.sumPAt1 + a.sumPAt1;
					aST_overal.sumRAt1 = aST_overal.sumRAt1 + a.sumRAt1;
					aST_overal.sumPAt5 = aST_overal.sumPAt5 + a.sumPAt5;
					aST_overal.sumRAt5 = aST_overal.sumRAt5 + a.sumRAt5;
					aST_overal.sumPAt10 = aST_overal.sumPAt10 + a.sumPAt10;
					aST_overal.sumRAt10 = aST_overal.sumRAt10 + a.sumRAt10;
				}
				else //: means that there is no assignment in this projec. So just write an empty line.
					writer1.append(owner_repo + TAB + "-" + TAB + "-" + TAB + "-"
							+ TAB + "-" + TAB + "-" + TAB + "-" + "\n");
			}
			writer1.flush();
			writer1.close();
			
			//Overal stats:
			MyUtils.println(writeMessageStep+"-2- Overal stats (in 5 files in the main output folder) ...", indentationLevel+1);
			String outputFileName2_overalStat = outputPath+"\\"+overalSummariesOutputTSVFileName+" - "+detailedSummaryOutputFileNameSuffix+".tsv";
			File file2 = new File(outputFileName2_overalStat);
			boolean needToWriteHeader2 = true;
			if (file2.exists())
				needToWriteHeader2 = false;
			//Writer for overall stats (one separate file for each assignment type):
			FileWriter writer2 = new FileWriter(outputFileName2_overalStat, true);
			//Title line(s):
			String overalTitle;
			if (needToWriteHeader2){
				//First line of the two line title:
				overalTitle = " " + TAB + " " + TAB + "project:" + TAB + "ALL" + TAB + "ALL" + TAB + "#ofBugs:" + TAB + totalNOA 
						+ TAB + "#ofCommunityMembers:" + TAB + min+" - "+max + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "";
				for (String owner_repo: projectNamesAndTheirIds.keySet()){
					String projectId = projectNamesAndTheirIds.get(owner_repo);
					if (projectsAndTheirAssignmentStats.containsKey(projectId)){
						int nOA = projectsAndTheirAssignmentStats.get(projectId).size(); //:number of assignments.
						int nOCM = projectsAndTheirCommunities.get(projectId).size(); //: number of community members.
						overalTitle = overalTitle + "project:"+ TAB + owner_repo + TAB + projectId + TAB + "#ofAssignments:" + TAB + nOA + TAB + "#ofCommunityMembers:" 
								+ TAB + nOCM + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "";
					}
				}				
				overalTitle = overalTitle + "\n";
				//Second line of the two line title:
				overalTitle = overalTitle + "Experiment title" + TAB + "TIME" + TAB + "MRR" + TAB + "MAP" + TAB + "Top 1" + TAB + "Top 5" + TAB + "Top 10" 
						+ TAB + "p@1" + TAB + "r@1" + TAB + "p@5" + TAB + "r@5" + TAB + "p@10" + TAB + "r@10" + TAB + "Any comments for the experiment";
				for (String owner_repo: projectNamesAndTheirIds.keySet()){
					String projectId = projectNamesAndTheirIds.get(owner_repo);
					if (projectsAndTheirAssignmentStats.containsKey(projectId)){
						overalTitle = overalTitle + TAB + "MRR" + TAB + "MAP" + TAB + "Top 1" + TAB + "Top 5" + TAB + "Top 10" 
								+ TAB + "p@1" + TAB + "r@1" + TAB + "p@5" + TAB + "r@5" + TAB + "p@10" + TAB + "r@10";
					}
				}				
				writer2.append(overalTitle + "\n");
			}
			//Adding one body line:
			overalTitle = detailedAssignmentResultsSubfolderName 
					+ TAB + Constants.floatFormatter.format(totalRunningTime)+"Sec"
					+ TAB + Constants.floatPercentageFormatter.format((double)aST_overal.sumRR/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumAP/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop10/aST_overal.n)
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt10/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt10/aST_overal.n)
					+ TAB + experimentDetails;
			for (String owner_repo: projectNamesAndTheirIds.keySet()){
				String projectId = projectNamesAndTheirIds.get(owner_repo);
				if (projectsAndTheirAssignmentStats.containsKey(projectId)){
					AssignmentStatSummary a = projectsAndTheirAssignmentStatSummaries.get(projectId);
					overalTitle = overalTitle 
							+ TAB + Constants.floatPercentageFormatter.format((double)a.sumRR/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumAP/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop1/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop5/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop10/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt1/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt1/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt5/a.n)
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt5/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt10/a.n) 
							+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt10/a.n);
				}
				else
					overalTitle = overalTitle 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-" 
							+ TAB + "-";
			}
			writer2.append(overalTitle + TAB + "\n");
			writer2.flush();			writer2.close();

			//Writer for overall stats (for all assignment types in a single file):
			MyUtils.println(writeMessageStep+"-2- Summary overal stats (in one file in the main output folder) ...", indentationLevel+1);
			String outputFileName3_overalStat = outputPath+"\\"+overalSummariesOutputTSVFileName+" - ALL_ASSIGNED_TYPES.tsv";
			File file3 = new File(outputFileName3_overalStat);
			boolean needToWriteHeader3 = true;
			if (file3.exists())
				needToWriteHeader3 = false;
			//Writer for overall stats (one separate file for each assignment type):
			FileWriter writer3 = new FileWriter(outputFileName3_overalStat, true);
			//Title line(s):
			if (needToWriteHeader3){//
				//First line of the two line title:
				overalTitle = " " + TAB + " " + TAB + "project:" + TAB + "ALL" + TAB + "ALL" + TAB + "#ofBugs:" + TAB + totalNOA 
						+ TAB + "#ofCommunityMembers:" + TAB + min+" - "+max + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "";
				for (String owner_repo: projectNamesAndTheirIds.keySet()){
					String projectId = projectNamesAndTheirIds.get(owner_repo);
					if (projectsAndTheirAssignmentStats.containsKey(projectId)){
						int nOA = projectsAndTheirAssignmentStats.get(projectId).size(); //:number of assignments.
						int nOCM = projectsAndTheirCommunities.get(projectId).size(); //: number of community members.
						overalTitle = overalTitle + "project:"+ TAB + owner_repo + TAB + projectId + TAB + "#ofAssignments:" + TAB + nOA + TAB + "#ofCommunityMembers:" 
								+ TAB + nOCM + TAB + "" + TAB + "" + TAB + "" + TAB + "" + TAB + "";
					}
					else{
						MyUtils.println("Error! The first time you are running the program you should select \"isMainRun\"=true. Please delete everything in the output folder and re-run the code with that option to create the titles. Then you can set that option to 'false' and run the code again.", indentationLevel+1);
						fMR.errors++;
					}
				}			
				overalTitle = overalTitle + "\n";
				//Second line of the two line title:
				overalTitle = overalTitle + "Experiment title" + TAB + "TIME" + TAB + "Assigned bug type" + TAB + "MRR" + TAB + "MAP" + TAB + "Top 1" + TAB + "Top 5" + TAB + "Top 10" 
						+ TAB + "p@1" + TAB + "r@1" + TAB + "p@5" + TAB + "r@5" + TAB + "p@10" + TAB + "r@10" + TAB + "Any comments for the experiment";
				int numberOfProjects = projectNamesAndTheirIds.size();
				for (int i=0; i<numberOfProjects; i++)
					overalTitle = overalTitle + TAB + "MRR" + TAB + "MAP" + TAB + "Top 1" + TAB + "Top 5" + TAB + "Top 10" 
							+ TAB + "p@1" + TAB + "r@1" + TAB + "p@5" + TAB + "r@5" + TAB + "p@10" + TAB + "r@10";
				
				writer3.append(overalTitle + "\n");
			}
			//Adding one body line:
			String aLineOfBody = detailedAssignmentResultsSubfolderName 
					+ TAB + Constants.floatFormatter.format(totalRunningTime)+"Sec"
					+ TAB + detailedSummaryOutputFileNameSuffix
					+ TAB + Constants.floatPercentageFormatter.format((double)aST_overal.sumRR/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumAP/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumTop10/aST_overal.n)
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt1/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt5/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumPAt10/aST_overal.n) 
					+ TAB + Constants.floatPercentageFormatter.format((double)100*aST_overal.sumRAt10/aST_overal.n)
					+ TAB + experimentDetails;
			for (String owner_repo: projectNamesAndTheirIds.keySet()){
				String projectId = projectNamesAndTheirIds.get(owner_repo);
				if (projectsAndTheirAssignmentStats.containsKey(projectId)){
					AssignmentStatSummary a = projectsAndTheirAssignmentStatSummaries.get(projectId);
					aLineOfBody = aLineOfBody 
						+ TAB + Constants.floatPercentageFormatter.format((double)a.sumRR/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumAP/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop1/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop5/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumTop10/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt1/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt1/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt5/a.n)
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt5/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumPAt10/a.n) 
						+ TAB + Constants.floatPercentageFormatter.format((double)100*a.sumRAt10/a.n);
				}
				else{
					aLineOfBody = aLineOfBody 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + ""
							+ TAB + "" 
							+ TAB + "" 
							+ TAB + "";
				}
			}
			writer3.append(aLineOfBody + TAB + "\n");			
			writer3.flush();	
			writer3.close();
			fMR.doneSuccessfully = 1;
		} catch (IOException e) {
			e.printStackTrace();
			fMR.errors = 1;
		}
		MyUtils.println("Finished.", indentationLevel+1);
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void addToIndex(String evidenceText, int evidenceType, int seqNum, int[] virtualSeqNum, //seqNum is the sequence number of the evidence. If it is an assignment, the row number in the assignments file (first assignment in the project is 1 and the next one increment by one). If it is not an assignment (e.g., it is a commit), the seqNum of the last assignment before that evidence will be considered.
			int originalNumberOfWordsInTheText, 
			Graph graph, Graph[] graphs, String projectId, String projectName, String login, String date, 
			HashMap<String, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>> projectId_Login_Tags_TypesAndTheirEvidence, 
			BTOption3_TF option3_TF, BTOption7_whenToCountTextLength option7_whenToCountTextLength,  
			GeneralExperimentType generalExperimentType, 
			FileManipulationResult fMR){
		int indexOfProjectInTheListOf13Projects = Constants.indexOfProjectInTheListOf13Projects(projectName);
		String[] words = evidenceText.split(" ");
		for (int j=0; j<words.length; j++){
			if (!words[j].equals("") 
					&& (
							generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF  
							|| (generalExperimentType == GeneralExperimentType.CALCULATE_OUR_METRIC__TTBA && graph.hasNode(words[j]))
							|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF 
							|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2 
							|| generalExperimentType == GeneralExperimentType.CALCULATE_TBA 
							|| (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH) 
							|| (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH__CALCULATE_WEIGHS_ONLINE) 
							|| (generalExperimentType != GeneralExperimentType.CALCULATE_VTBA_SOURCECODE && graph.hasNode(words[j]))
							|| (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_SOURCECODE && graphs[indexOfProjectInTheListOf13Projects].hasNode(words[j]))
							)
//					&& (
//							generalExperimentType == GeneralExperimentType.JUST_CALCULATE_ORIGINAL_TF_IDF  
//							|| (generalExperimentType == GeneralExperimentType.CALCULATE_OUR_METRIC__TTBA && graph.hasNode(words[j]) && words[j].length() > 2)
//							|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF 
//							|| generalExperimentType == GeneralExperimentType.JUST_CALCULATE_TIME_TF_IDF2 
//							|| generalExperimentType == GeneralExperimentType.CALCULATE_TBA 
//							|| (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_GH && words[j].length() > 2) 
//							|| (generalExperimentType != GeneralExperimentType.CALCULATE_VTBA_SOURCECODE && graph.hasNode(words[j]))
//							|| (generalExperimentType == GeneralExperimentType.CALCULATE_VTBA_SOURCECODE && graphs[indexOfProjectInTheListOf13Projects].hasNode(words[j]) && words[j].length() > 2)
//							)
					&& (
							generalExperimentType != GeneralExperimentType.CALCULATE_TBA
							|| (
									!words[j].matches(Constants.startsWithNumber_ForRegEx)
//									&& words[j].length()>3
//									&& graph.hasNode(words[j])
									)
							)
					){ //: means that if this word is an SO tag or one of the thesaurus keywords.
				//First, start by projectId:
				
				HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>> login_Tags_TypesAndTheirEvidence;
				if (projectId_Login_Tags_TypesAndTheirEvidence.containsKey(projectId))
					login_Tags_TypesAndTheirEvidence = projectId_Login_Tags_TypesAndTheirEvidence.get(projectId);
				else{
					login_Tags_TypesAndTheirEvidence = new HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>();
					projectId_Login_Tags_TypesAndTheirEvidence.put(projectId, login_Tags_TypesAndTheirEvidence);
				}
				//Then move on to login:
				HashMap<String, HashMap<Integer, ArrayList<Evidence>>> tags_TypesAndTheirEvidence;
				if (login_Tags_TypesAndTheirEvidence.containsKey(login))
					tags_TypesAndTheirEvidence = login_Tags_TypesAndTheirEvidence.get(login);
				else{
					tags_TypesAndTheirEvidence = new HashMap<String, HashMap<Integer, ArrayList<Evidence>>>();
					login_Tags_TypesAndTheirEvidence.put(login, tags_TypesAndTheirEvidence);
				}
				//After that, check the tags:
				HashMap<Integer, ArrayList<Evidence>> typesAndTheirEvidence;
				if (tags_TypesAndTheirEvidence.containsKey(words[j])) //: means that if there is already at least one evidence containing this tag (for this user and this project): 
					typesAndTheirEvidence = tags_TypesAndTheirEvidence.get(words[j]);
				else{
					typesAndTheirEvidence = new HashMap<Integer, ArrayList<Evidence>>();
					tags_TypesAndTheirEvidence.put(words[j], typesAndTheirEvidence);
				}
				//Now, check the specific type evidence:
				ArrayList<Evidence> type_x_Evidence;
				if (typesAndTheirEvidence.containsKey(evidenceType))
					type_x_Evidence = typesAndTheirEvidence.get(evidenceType);
				else{
					type_x_Evidence = new ArrayList<Evidence>();
					typesAndTheirEvidence.put(evidenceType, type_x_Evidence);
				}
				//And hereby create the evidence and insert it in the arrayList type_x_Evidence:
				//We need the frequency of this word (so we need to count it):
				int freq = 1;
				for (int k=j+1; k<words.length; k++)
					if (words[k].equals(words[j])){
						freq++;
						words[k] = "";
					}
				//TODO: Delete this later (no need to save and print max.... It is just for my own knowledge):
				if (freq > maxFreqOfAWordInAnEvidence)
					maxFreqOfAWordInAnEvidence = freq; //just checking!
				//.
				
				Evidence e;
				try {
					int numberOfWordsInTheText;
					switch (option7_whenToCountTextLength){
						case USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS:
							numberOfWordsInTheText = originalNumberOfWordsInTheText;
							break;
						case USE_TEXT_LENGTH_AFTER_REMOVING_NON_SO_TAGS:
						default:
							numberOfWordsInTheText = words.length;
							break;
					}
					e = new Evidence(Constants.dateFormat.parse(date), seqNum, virtualSeqNum, freq, numberOfWordsInTheText, option3_TF);
					type_x_Evidence.add(e);
				} catch (ParseException e1) {
					fMR.errors++;
					e1.printStackTrace();
				}
			}
		}
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void indexAssignmentEvidence(int evidenceType, TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments, 
			TreeMap<String, String[]> projects, TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo, 
			HashMap<String, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>> projectId_Login_Tags_TypesAndTheirEvidence, 
			//HashMap<projId, HashMap<login, HashMap<tag, ArrayList<Evidence>>>>
			Graph graph, Graph[] graphs, FileManipulationResult fMR, 
			BTOption1_whatToAddToAllBugs option1, BTOption2_w option2_w, BTOption3_TF option3_TF, BTOption4_IDF option4_IDF, BTOption5_prioritizePAs option5_prioritizePAs, BTOption6_whatToAddToAllCommits option6_whatToAddToAllCommits, BTOption7_whenToCountTextLength option7_whenToCountTextLength, 
			GeneralExperimentType generalExperimentType, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, String writeMessageStep){
		//This method reads the input files that are not empty ("") in parameter projectId_Login_TagsAndTheirEvidence. 
			//The arrayList should be sorted based on date. 
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel+1);
		MyUtils.println(writeMessageStep + "- Iterating over assignment evidence of type \"" + Constants.ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[evidenceType] + "\" (in a project by project basis) and adding them to the evidence index:", indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);

		try {
			for (Map.Entry<String, ArrayList<String[]>> entry : projectsAndTheirAssignments.entrySet()){
				ArrayList<String[]> assignmentsOfOneProject = entry.getValue();
				String projectId = entry.getKey();
				Project project = null;
				int i = 0;
				for (i=0; i<assignmentsOfOneProject.size(); i++){
					String[] fields = assignmentsOfOneProject.get(i);
					String bugNumber = fields[0];
					String date = fields[1];
					String login = fields[2];//this is the assignee.
					Bug bug = new Bug(projectId, bugNumber, projectIdBugNumberAndTheirBugInfo, fMR);
					//						String labels = bugInfo[2];
					//						String title = bugInfo[3];
					//						String body = bugInfo[4];

					String text;
					project = new Project(projects, projectId, indentationLevel+2, fMR);
					String projectName = project.owner_repo.substring(project.owner_repo.indexOf("/")+1);
					int[] originalNumberOfWordsInText_array = new int[1];
					text = getBugText(project, bug, originalNumberOfWordsInText_array, option1);
					if (generalExperimentType == GeneralExperimentType.CALCULATE_TBA)
						text = StringManipulations.clean(text.toLowerCase().replaceAll(Constants.allValidCharactersInSOURCECODE_Strict_ForRegEx, " "));
					int originalNumberOfWordsInText = originalNumberOfWordsInText_array[0];

					if (text.length() > 2){
						//This is an assignment event, so set the virtualSeqNum (the sequence number of non-assignmentt evidence) to AN_EXTREMELY_NEGATIVE_INT:
						int[] virtualSeqNum = new int[Constants.NUMBER_OF_ASSIGNEE_TYPES];
						for (int j=0; j<Constants.NUMBER_OF_ASSIGNEE_TYPES; j++)
							virtualSeqNum[j] = Constants.SEQ_NUM____NO_NEED_TO_TRIAGE_THIS_TYPE___OR___THIS_IS_NOT__NON_B_A_EVIDENCE;
						addToIndex(text, evidenceType, i+1, virtualSeqNum, originalNumberOfWordsInText, 
								graph, graphs, projectId, projectName, login, date, projectId_Login_Tags_TypesAndTheirEvidence, option3_TF, option7_whenToCountTextLength, generalExperimentType, fMR);
					}
					else{
//						fMR.errors++;
//						System.out.println("Error! Assignment evidence length is zero!");
					}
					if (i+1 % showProgressInterval == 0)
						MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+2);
				}
				if (i > 0)
					MyUtils.println(project.owner_repo + " (projectId: " + projectId + "): \texpertise of " + i + " bug assignments indexed.", indentationLevel+1);
				else
					MyUtils.println(project.owner_repo + " (projectId: " + projectId + "): \tWarning: Nothing indexed! No bug assignments to index their expertise!", indentationLevel+1);
			}
		} catch (Exception e) {
			fMR.errors++;
			System.out.println("ERROR!");
			e.printStackTrace();
		} 

		MyUtils.println("Finished.", indentationLevel+1);
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void readAndIndexNonAssignmentEvidence(String inputPath,  
			String commitsInputFileName, String pRsInputFileName, String bugCommentsInputFileName, String commitCommentsInputFileName, String pRCommentsInputFileName,
			ArrayList<TreeMap<String, ArrayList<String[]>>> projectsAndTheirAssignments__AL_forDifferentAssignmetTypes, int[] assignmentTypesToTriage, 
			FileManipulationResult fMR,
			int[] evidenceTypes, 
			TreeMap<String, String[]> projects, TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo, 
			HashMap<String, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>>> projectId_Login_Tags_TypesAndTheirEvidence, 
			//HashMap<projId, HashMap<login, HashMap<tag, ArrayList<Evidence>>>>
			Graph graph, Graph[] graphs, 
			BTOption1_whatToAddToAllBugs option1, BTOption2_w option2_w, BTOption3_TF option3_TF, BTOption4_IDF option4_IDF, BTOption5_prioritizePAs option5_prioritizePAs, BTOption6_whatToAddToAllCommits option6_whatToAddToAllCommits, BTOption7_whenToCountTextLength option7_whenToCountTextLength, 
			GeneralExperimentType generalExperimentType, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, String writeMessageStep){
		//This method reads the input files that are not empty ("") in parameter projectId_Login_TagsAndTheirEvidence. 
			//The arrayList should be sorted based on date. 
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel+1);
		MyUtils.println(writeMessageStep + "- Reading non-assignment evidence in \"" + commitsInputFileName + "\" and indexing them:", indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);

		//TODO: Complete this (this is currently just reading commits' info, but should read all of the evidence):
		try {
			//Commits:
			if (evidenceTypes[Algorithm.INDEX_OF__EVIDENCE_TYPE__COMMIT] == Algorithm.YES){
				if (!commitsInputFileName.equals("")){
					BufferedReader br;
					br = new BufferedReader(new FileReader(inputPath + "\\" + commitsInputFileName));
					int i=0;
					String s;
					br.readLine(); //header.
					while ((s=br.readLine())!=null){
						String[] fields = s.split("\t");
						if (fields.length == 6){ 
//							String sha = fields[0];
							String projectId = fields[1];
							String committer = fields[2];
							String date = fields[3];
							String commitMessage = fields[4];
							int originalNumberOfWordsInText = Integer.parseInt(fields[5]);
							
							if (!committer.equals(" ")){
								String text = "";
								Project project = new Project(projects, projectId, indentationLevel+2, fMR);
								String projectName = project.owner_repo.substring(project.owner_repo.indexOf("/")+1);
								int[] originalNumberOfWordsInText_array = new int[]{1};
								text = getCommitText(project, commitMessage, originalNumberOfWordsInText_array, option6_whatToAddToAllCommits);
								originalNumberOfWordsInText = originalNumberOfWordsInText_array[0];
								//Determining the commit's virtualSeqNum[] values based on assignments in different projectsAndTheirAssignments in projectsAndTheirAssignments_AL. 
								int[] virtualSeqNum = new int[Constants.NUMBER_OF_ASSIGNEE_TYPES];
								for (int j=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); j<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); j++){
									TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments;
									if (assignmentTypesToTriage[j] == Algorithm.YES){
										projectsAndTheirAssignments = projectsAndTheirAssignments__AL_forDifferentAssignmetTypes.get(j);
										ArrayList<String[]> assignments = projectsAndTheirAssignments.get(projectId);
										virtualSeqNum[j] = MyUtils.specialBinarySearch2(assignments, 1/*the date field is index 1*/, date) + 1; //: "Plus one"; because binary search returns index, not sequenceNumber
									}
									else
										virtualSeqNum[j] = Constants.SEQ_NUM____NO_NEED_TO_TRIAGE_THIS_TYPE___OR___THIS_IS_NOT__NON_B_A_EVIDENCE;
								}
								if (text.length() > 2)
									addToIndex(text, Constants.EVIDENCE_TYPE_COMMIT, Constants.SEQ_NUM____THIS_IS_NOT__B_A_EVIDENCE, virtualSeqNum, originalNumberOfWordsInText, 
											graph, graphs, projectId, projectName, committer, date, projectId_Login_Tags_TypesAndTheirEvidence, option3_TF, option7_whenToCountTextLength, generalExperimentType, fMR);
								else{
//									fMR.errors++;
//									System.out.println("Error! Non-assignment evidence length is zero!");
								}
							}
						}
						else
							fMR.errors++;
						i++;
						if (i % showProgressInterval == 0)
							MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+2);
					}
					br.close();
				}
			}
			//PRs: 
//			if (evidenceTypes[Algorithm.INDEX_OF__EVIDENCE_TYPE__COMMIT] == Algorithm.YES){
//				if (!commitsInputFileName.equals("")){
//					BufferedReader br;
//					br = new BufferedReader(new FileReader(inputPath + "\\" + commitsInputFileName));
//					int i=0;
//					String s;
//					br.readLine(); //header.
//					while ((s=br.readLine())!=null){
//						String[] fields = s.split("\t");
//						if (fields.length == 6){ 
////							String sha = fields[0];
//							String projectId = fields[1];
//							String committer = fields[2];
//							String date = fields[3];
//							String commitMessage = fields[4];
//							int originalNumberOfWordsInText = Integer.parseInt(fields[5]);
//							
//							if (!committer.equals(" ")){
//								String text = "";
//								Project project = new Project(projects, projectId, indentationLevel+2, fMR);
//								int[] originalNumberOfWordsInText_array = new int[]{1};
//								text = getCommitText(project, commitMessage, originalNumberOfWordsInText_array, option6_whatToAddToAllCommits);
//								originalNumberOfWordsInText = originalNumberOfWordsInText_array[0];
//								//Determining the commit's virtualSeqNum[] values based on assignments in different projectsAndTheirAssignments in projectsAndTheirAssignments_AL. 
//								int[] virtualSeqNum = new int[Constants.NUMBER_OF_ASSIGNEE_TYPES];
//								for (int j=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); j<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); j++){
//									TreeMap<String, ArrayList<String[]>> projectsAndTheirAssignments;
//									if (assignmentTypesToTriage[j] == Algorithm.YES){
//										projectsAndTheirAssignments = projectsAndTheirAssignments__AL_forDifferentAssignmetTypes.get(j);
//										ArrayList<String[]> assignments = projectsAndTheirAssignments.get(projectId);
//										virtualSeqNum[j] = MyUtils.specialBinarySearch2(assignments, 1/*the date field is index 1*/, date) + 1; //: "Plus one"; because binary search returns index, not sequenceNumber
//									}
//									else
//										virtualSeqNum[j] = Constants.SEQ_NUM____NO_NEED_TO_TRIAGE_THIS_TYPE___OR___THIS_IS_NOT__NON_B_A_EVIDENCE;
//								}
//								if (text.length() > 2)
//									addToIndex(text, Constants.EVIDENCE_TYPE_COMMIT, Constants.SEQ_NUM____THIS_IS_NOT__B_A_EVIDENCE, virtualSeqNum, originalNumberOfWordsInText, graph, projectId, committer, date, projectId_Login_Tags_TypesAndTheirEvidence, option3_TF, option7_whenToCountTextLength, fMR);
//								else{
////									fMR.errors++;
////									System.out.println("Error! Non-assignment evidence length is zero!");
//								}
//							}
//						}
//						else
//							fMR.errors++;
//						i++;
//						if (i % showProgressInterval == 0)
//							MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+2);
//					}
//					br.close();
//				}
//			}
			//bugComments:
			
		} catch (Exception e) {
			System.out.println("ERROR!");
			e.printStackTrace();
			fMR.errors++;
		} 

		MyUtils.println("Finished.", indentationLevel+1);
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static String createFolderForResults(String outputPath, String experimentTitle, boolean isMainRun, FileManipulationResult fMR, int indentationLevel){
		//This method browses all the folders (in outputPath) starting with a number, dash and space. 
			//Then identifies the largest "starting number" in them and increases it by one (=n), then creates a folder with that name plus other required suffixes (for the results to be put in) and returns it.
		MyUtils.createFolderIfDoesNotExist(outputPath, fMR, 0, "Initial 'temp directory checking'");
		File file = new File(outputPath);
		String[] directories = file.list(new FilenameFilter() {
			  @Override
			  public boolean accept(File current, String name) {
			    return new File(current, name).isDirectory();
			  }
			});
		int maxFolderPrefixNumber = 0;
		int folderPrefixNumber;
		for (String s: directories)
			if (s.matches("[0-9]+\\-\\s\\S.*")){//:if the folder name starts with a number, then dash followed by a space and at least one non-space (and anything else afterwards).
				String regex = "[0-9]+";//: get the starting number (the rest of numbers [if any] are uesless).
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(s);
				if (m.find()){
					folderPrefixNumber = Integer.parseInt(m.group(0));
					if (folderPrefixNumber > maxFolderPrefixNumber)
						maxFolderPrefixNumber = folderPrefixNumber;
				}
			}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String mainOrTest;
		if (isMainRun)
			mainOrTest = "13m";
		else
			mainOrTest = "3t";
		String outputFolderName = Integer.toString(maxFolderPrefixNumber+1) + "- (" + experimentTitle + " - " + mainOrTest + "P) - " + sdf.format(new Date());
		if (!(new File(outputPath+"\\"+outputFolderName).mkdirs())){
			fMR.errors = 1;
			MyUtils.println("Error creating output folder!", indentationLevel);
		}
		return outputFolderName;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static boolean randomlySelectWithChance(int percentage){
		//This method is used to select a bug randomly with a "percentage" chance (e.g., when we want to triage n% of the bugs):
		if (random.nextInt(100) < percentage)
			return true;
		else
			return false;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static String getMainLanguages(String mainLanguagesPercentages, int[] originalNumberOfLanguages_array){
		//This method gets the mainLanguagesPercentages in the form of "[julia^66;;c^19]" 
			//(each item in it is a language followed by "^" and its percentage; all languages with more than 15% are presented there), 
			//then extracts the main languages and returns a space-delimited string containing all those languages.
				//every 15% usage adds up another mention to the name of that language.
		//it is assumed that the input includes at least one language. So the input is not like "[]".
		String result = "";
		String[] languages = mainLanguagesPercentages.substring(1, mainLanguagesPercentages.length()-1).split(";;");
		for (int i=0; i<languages.length; i++){
			result = StringManipulations.concatTwoStringsWithDelimiter(result, languages[i].substring(0, languages[i].indexOf("^")), " ");
		}
		originalNumberOfLanguages_array[0] = languages.length; 
		return result;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static String getBugText(Project project, Bug bug, int[] originalNumberOfWordsInText_array, BTOption1_whatToAddToAllBugs option1){
		//This method concatenates the project title, description and main languages to the bug title and description (all separated by space) and returns the resulting string
			//Also updates the originaltextLength_array[0] with the length of original text (which is the length of its elements before removing the non-SO keywords).
				//For example, if it adds bug.title and bug.body, then the value of originaltextLength_array[0] will be bug.title_numberOfWords+bug.body_numberOfWords. 
		switch (option1){
		case ADD_PTD:
			originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords + project.description_numberOfWords;
			return (bug.title + " " + bug.body + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
		case ADD_ML:
			if (project.mainLanguagePercentages.equals("[]")){
				originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords;
				return (bug.title + " " + bug.body).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
			}
			else{
				int[] originalNumberOfLanguages_array = new int[1];
				String mainLanguages = getMainLanguages(project.mainLanguagePercentages, originalNumberOfLanguages_array);
				int originalNumberOfLanguages = originalNumberOfLanguages_array[0];
				originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords + originalNumberOfLanguages;
				return (bug.title + " " + bug.body + " " + mainLanguages).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
			}
		case ADD_PTD_ML:
			if (project.mainLanguagePercentages.equals("[]")){
				originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords + project.description_numberOfWords;
				return (bug.title + " " + bug.body + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
			}
			else{
				int[] originalNumberOfLanguages_array = new int[1];
				String mainLanguages = getMainLanguages(project.mainLanguagePercentages, originalNumberOfLanguages_array);
				int originalNumberOfLanguages = originalNumberOfLanguages_array[0];
				originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords + project.description_numberOfWords + originalNumberOfLanguages;
				return (bug.title + " " + bug.body + " " + project.description + " " + mainLanguages).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
			}
		case JUST_USE_BUG_TD:
		default:
			originalNumberOfWordsInText_array[0] = bug.title_numberOfWords + bug.body_numberOfWords;
			return (bug.title + " " + bug.body).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
		}
				
//		if ((option1 == BTOption1.ADD_PTD || option1 == BTOption1.ADD_PTD_mL) && !project.description.equals(" "))
//			if ((option1 == BTOption1.ADD_mL || option1 == BTOption1.ADD_PTD_mL) && !project.mainLanguagePercentages.equals("[]"))
//				return (bug.title + " " + bug.body + " " + project.description + " " + getMainLanguages(project.mainLanguagePercentages)).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
//			else
//				return (bug.title + " " + bug.body + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
//		else
//			if ((option1 == BTOption1.ADD_mL || option1 == BTOption1.ADD_PTD_mL) && !project.mainLanguagePercentages.equals("[]"))
//				return (bug.title + " " + bug.body + " " + getMainLanguages(project.mainLanguagePercentages)).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
//			else
//				return (bug.title + " " + bug.body).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static String getCommitText(Project project, String commitMessage, int[] originalNumberOfWordsInText_array, BTOption6_whatToAddToAllCommits option6_whatToAddToAllCommits){
		//This method ... (like the getBugText() method).
			//Also updates originalAddedTextLength_array[0] to contain the number of words added to commitMessage (not the commitMessage itself).
		switch (option6_whatToAddToAllCommits){
		case ADD_PTD:
			originalNumberOfWordsInText_array[0] = originalNumberOfWordsInText_array[0] + project.description_numberOfWords;
			return (commitMessage + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
		case ADD_mL:
			if (project.mainLanguagePercentages.equals("[]")){
				originalNumberOfWordsInText_array[0] = originalNumberOfWordsInText_array[0]; //no change.
				return commitMessage;
			}
			else{
				int[] originalNumberOfLanguages_array = new int[1];
				String mainLanguages = getMainLanguages(project.mainLanguagePercentages, originalNumberOfLanguages_array);
				int originalNumberOfLanguages = originalNumberOfLanguages_array[0];
				originalNumberOfWordsInText_array[0] = originalNumberOfWordsInText_array[0] + originalNumberOfLanguages;
				return (commitMessage + " " + mainLanguages).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
			}
		case ADD_PTD_mL:
			if (project.mainLanguagePercentages.equals("[]")){
				originalNumberOfWordsInText_array[0] = originalNumberOfWordsInText_array[0] + project.description_numberOfWords;
				return (commitMessage + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
			}
			else{
				int[] originalNumberOfLanguages_array = new int[1];
				String mainLanguages = getMainLanguages(project.mainLanguagePercentages, originalNumberOfLanguages_array);
				int originalNumberOfLanguages = originalNumberOfLanguages_array[0];
				originalNumberOfWordsInText_array[0] = originalNumberOfWordsInText_array[0] + project.description_numberOfWords + originalNumberOfLanguages;
				return (commitMessage + " " + project.description + " " + mainLanguages).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
			}
		case JUST_USE_COMMIT_M:
		default:
			return commitMessage;
		}
		
//		if ((option6_whatToAddToAllCommits == BTOption6.ADD_PTD || option6_whatToAddToAllCommits == BTOption6.ADD_PTD_mL) && !project.description.equals(" "))
//			if ((option6_whatToAddToAllCommits == BTOption6.ADD_mL || option6_whatToAddToAllCommits == BTOption6.ADD_PTD_mL) && !project.mainLanguagePercentages.equals("[]"))
//				return (commitMessage + " " + project.description + " " + getMainLanguages(project.mainLanguagePercentages)).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
//			else
//				return (commitMessage + " " + project.description).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces.
//		else
//			if ((option6_whatToAddToAllCommits == BTOption6.ADD_mL || option6_whatToAddToAllCommits == BTOption6.ADD_PTD_mL) && !project.mainLanguagePercentages.equals("[]"))
//				return (commitMessage + " " + getMainLanguages(project.mainLanguagePercentages)).replaceAll("\\s{2,}", " ").trim(); //removing extra spaces (that may be added right now by concatenating).
//			else
//				return commitMessage;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void removeAssignmentsOfDevelopersWhoFixedAtLeastNBugs(ArrayList<String[]> assignmentsOfThisProject, 
			int developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered, HashMap<String, HashMap<String, HashMap<Integer, ArrayList<Evidence>>>> logins_Tags_TypesAndTheirEvidence, 
			int indentationLevel, FileManipulationResult fMR){
		HashMap<String, Integer> loginsAndTheirNumberOfAssignments = new HashMap<String, Integer>();
		//Counting:
		for (int i=0; i<assignmentsOfThisProject.size(); i++){ 
			Assignment a = new Assignment(assignmentsOfThisProject, i, indentationLevel);
			if (loginsAndTheirNumberOfAssignments.containsKey(a.login))
				loginsAndTheirNumberOfAssignments.put(a.login, loginsAndTheirNumberOfAssignments.get(a.login)+1);
			else
				loginsAndTheirNumberOfAssignments.put(a.login, 1);
		}//for.
		
		//Filtering:
		for (int i=assignmentsOfThisProject.size()-1; i>=0; i--){ 
			Assignment a = new Assignment(assignmentsOfThisProject, i, indentationLevel);
			if (loginsAndTheirNumberOfAssignments.containsKey(a.login)){
				if (loginsAndTheirNumberOfAssignments.get(a.login) < developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered){
					assignmentsOfThisProject.remove(i);
					logins_Tags_TypesAndTheirEvidence.remove(a.login);
				}
			}
			else{
				MyUtils.println("Error in assignments", indentationLevel);
				fMR.errors++;
				break;
			}
		}//for.

	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
}


















