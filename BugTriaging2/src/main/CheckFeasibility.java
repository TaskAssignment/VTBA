package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Constants;
import utils.FileManipulationResult;
import utils.MyUtils;
import utils.StringManipulations;
import utils.TSVManipulations;
import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.LogicalOperation;

public class CheckFeasibility {
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	private static final String DATASET_DIRECTORY_SO_TSV = "C:\\2-Study\\BugTriaging2\\Data Set\\SO\\20161110\\3-TSV-Cleaned";
	private static final String DATASET_DIRECTORY_GH_TSV = "C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\2-TSV\\3- 13 projects + 2 project families (13 + 6 more projects)";
	public static final String COMBINED_KEY_SEPARATOR = Constants.COMBINED_KEY_SEPARATOR;
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	private static void fillTagsThatAreInInputString(String s, HashSet<String> allTags, HashSet<String> ioHashSet){
		String words[] = s.trim().split(" ");
		for (int i=0; i<words.length; i++)
			if (allTags.contains(words[i]))
				ioHashSet.add(words[i]);
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	private static String eliminateNonTagKeywords(String s, HashSet<String> allTags){
		//This method removes any keyword of the input string that is not a SO tag.
		String result = "";
		s = s.toLowerCase();
		String words[] = s.split(" ");
		for (int i=0; i<words.length; i++)
			if (allTags.contains(words[i]))
				result = result + " " + words[i];
		return result;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void countNumberOfTagsInEachEvidence(){
		//This method checks number of distinct tags in each issue (also in each issue + its comments) and saves in output file:
		try{
			MyUtils.println("-----------------------------------", 0);
			MyUtils.println("Counting number of tags in bugs, PRs  and comments (as well as their comments) and saving the counts in a tsv file:", 0);
			MyUtils.println("Started ...", 1);

			//1: Read all tags:
			HashSet<String> allTags = TSVManipulations.readUniqueFieldFromTSV(DATASET_DIRECTORY_SO_TSV, "occurrences.tsv", 0, 2, LogicalOperation.NO_CONDITION, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					true, 1, 50000, Constants.THIS_IS_REAL, "1");

			//2: Read all projects in the form of HashMap<id, [tags available in title + description + languages [all separated by space]]>:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("2- Reading projects into a HashMap:", 1);
			MyUtils.println("Started ...", 2);
			BufferedReader br2 = new BufferedReader(new FileReader(DATASET_DIRECTORY_GH_TSV + "\\projects_complete.tsv")); 
			String s = br2.readLine(); //Skip the title line.
			HashMap<String, String> projects = new HashMap<String, String>(); //: projects and the tags available in their title/body/listOfTopLanguages.
			String fields[];
			int i = 0;
			int linesWithError = 0;
			while((s = br2.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (fields.length == 6){
					String projectId = fields[0];
					String[] ownerAndRepoStrings = fields[2].split("/");
					String owner = ownerAndRepoStrings[0].toLowerCase();
					String repo = ownerAndRepoStrings[1].toLowerCase();
					String projectInfo = "";
					if (allTags.contains(owner))
						projectInfo = owner;
					if (allTags.contains(repo))
						projectInfo = StringManipulations.concatTwoStringsWithSpace(projectInfo, repo); 
					String description = StringManipulations.clean(fields[3]);
					String[] descriptionWords = description.split(" ");
					for (int j=0; j<descriptionWords.length; j++)//: If the words of the original description is an SO tag, keep it, otherwise just ignore it:
						if (allTags.contains(descriptionWords[j]))
							projectInfo = StringManipulations.concatTwoStringsWithSpace(projectInfo, descriptionWords[j]);
					
					String mainLanguagesPercentages = fields[4];
					if (!mainLanguagesPercentages.equals("[]")){ //:if there are at least one language in the list:
						mainLanguagesPercentages = mainLanguagesPercentages.substring(1, mainLanguagesPercentages.length()-1); //:getting rid of "[" and "]".
						String[] mainLanguages = mainLanguagesPercentages.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
						for (int j=0; j<mainLanguages.length; j++){
							int k = mainLanguages[j].indexOf(Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM);
							mainLanguages[j] = mainLanguages[j].substring(0, k); //getting rid of ^12345 and taking just the language.
							if (allTags.contains(mainLanguages[j]))
								projectInfo = StringManipulations.concatTwoStringsWithSpace(projectInfo, mainLanguages[j]);
						}
					}
					projects.put(projectId, projectInfo);
					i++;
				}
				else
					linesWithError++;
			}
			br2.close();
			MyUtils.println("Number of bugs read: " + Constants.integerFormatter.format(i), 2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", 2);
			else
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

			//3: Read all bugs in the form of HashMap<[projectId;;issueNumber], [bugTitle + bugBody]>:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("3- Reading bugs into a HashMap:", 1);
			MyUtils.println("Started ...", 2);
			BufferedReader br3 = new BufferedReader(new FileReader(DATASET_DIRECTORY_GH_TSV + "\\bugs_complete.tsv")); 
			s = br3.readLine(); //Skip the title line.
			HashMap<String, String> bugs = new HashMap<String, String>();
			i = 0;
			linesWithError = 0;
			while((s = br3.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (fields.length == 11){
					String projectId = fields[1];
					String bugNumber = fields[2];

					String bugTitle = eliminateNonTagKeywords(fields[9], allTags);
					String bugBody = eliminateNonTagKeywords(fields[10], allTags);

					bugs.put(projectId+Constants.SEPARATOR_FOR_ARRAY_ITEMS+bugNumber, 
							StringManipulations.removeRedundantSpaces(bugTitle + " " + bugBody));
					i++;
					if (i % 10000 == 0)
						MyUtils.println(Constants.integerFormatter.format(i), 2);
				}
				else
					linesWithError++;
			}
			br3.close();
			MyUtils.println("Number of bugs read: " + Constants.integerFormatter.format(i), 2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", 2);
			else
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

			//4: Read all pullRequests in the form of HashMap<[projectId;;prNumber], [prTitle + prBody]>:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("4- Reading pullResuests into a HashMap:", 1);
			MyUtils.println("Started ...", 2);
			BufferedReader br4 = new BufferedReader(new FileReader(DATASET_DIRECTORY_GH_TSV + "\\PRs_complete.tsv")); 
			s = br4.readLine(); //Skip the title line.
			HashMap<String, String> prs = new HashMap<String, String>();
			i = 0;
			linesWithError = 0;
			while((s = br4.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (fields.length == 11){
					String projectId = fields[1];
					String prNumber = fields[2];

					String prTitle = eliminateNonTagKeywords(fields[9], allTags);
					String prBody = eliminateNonTagKeywords(fields[10], allTags);

					prs.put(projectId+Constants.SEPARATOR_FOR_ARRAY_ITEMS+prNumber, 
							StringManipulations.removeRedundantSpaces(prTitle + " " + prBody));
					i++;
					if (i % 10000 == 0)
						MyUtils.println(Constants.integerFormatter.format(i), 2);
				}
				else
					linesWithError++;
			}
			br4.close();
			MyUtils.println("Number of PRs read: " + Constants.integerFormatter.format(i), 2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", 2);
			else
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

			//5: Read all commits in the form of HashMap<[projectId;;commitSHA], commitMessage>:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("5- Reading commits into a HashMap:", 1);
			MyUtils.println("Started ...", 2);
			BufferedReader br5 = new BufferedReader(new FileReader(DATASET_DIRECTORY_GH_TSV + "\\commits.tsv")); 
			s = br5.readLine(); //Skip the title line.
			HashMap<String, String> commits = new HashMap<String, String>();
			i = 0;
			linesWithError = 0;
			while((s = br5.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (fields.length == 6){
					String commitSHA = fields[0];

					String commitMessage = eliminateNonTagKeywords(fields[5], allTags);

					commits.put(commitSHA, 
							StringManipulations.removeRedundantSpaces(commitMessage));
					i++;
					if (i % 10000 == 0)
						MyUtils.println(Constants.integerFormatter.format(i), 2);
				}
				else
					linesWithError++;
			}
			br5.close();
			MyUtils.println("Number of commits read: " + Constants.integerFormatter.format(i), 2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", 2);
			else
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

			//6: Read all comments in the form of HashMap<[projectId;;issueNumber], commentBody>:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("6- Reading comments (of bugs, PR's and commits) file into a HashMap:", 1);
			MyUtils.println("Started ...", 2);
			BufferedReader br6 = new BufferedReader(new FileReader(DATASET_DIRECTORY_GH_TSV + "\\comments.tsv")); 
			s = br6.readLine(); //Skip the title line.
			HashMap<String, String> bugComments = new HashMap<String, String>();
			HashMap<String, String> prComments = new HashMap<String, String>();
			HashMap<String, String> commitComments = new HashMap<String, String>();
			i = 0;
			linesWithError = 0;
			while((s = br6.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (fields.length == 8){
					String projectId = fields[1];
					String type = fields[4]; //type is one of the two: commit or issue (and, issue means bug or PR).
					String bugOrPRNumber = fields[6];
					String commentBody = eliminateNonTagKeywords(fields[7], allTags); //:this is to eliminate redundant terms and avoid the string getting too big.
					commentBody = StringManipulations.removeRedundantSpaces(commentBody);
					String projectId_bugOrPRNumber = projectId+Constants.SEPARATOR_FOR_ARRAY_ITEMS+bugOrPRNumber;
					if (type.equals("issue")){//: means that the record is a bug comment or PR comment.
						if (bugs.containsKey(projectId_bugOrPRNumber))//: means that the record is a bug.
							if (bugComments.containsKey(projectId_bugOrPRNumber)){//: means that the issue have at least one comment stored in this HashMap. So concat the new comment to it:
								String allPreviousComments = bugComments.get(projectId_bugOrPRNumber);
								bugComments.put(projectId_bugOrPRNumber, StringManipulations.concatTwoStringsWithSpace(allPreviousComments, commentBody));
							}
							else
								bugComments.put(projectId_bugOrPRNumber, commentBody);
						else
							if (prs.containsKey(projectId_bugOrPRNumber))//: means that the record is a PR.
								if (prComments.containsKey(projectId_bugOrPRNumber)){//: means that the issue have at least one comment stored in this HashMap. So concat the new comment to it:
									String allPreviousComments = prComments.get(projectId_bugOrPRNumber);
									prComments.put(projectId_bugOrPRNumber, StringManipulations.concatTwoStringsWithSpace(allPreviousComments, commentBody));
								}
								else
									prComments.put(projectId_bugOrPRNumber, commentBody);
					}
					else //: means that the record is a commit comment
						if (type.equals("commit")){//: means that the record is a commit comment.
							String commitSHA = fields[5];
							if (commits.containsKey(commitSHA))//: means that the record is a commit.
								if (commitComments.containsKey(commitSHA)){//: means that the issue have at least one comment stored in this HashMap. So concat the new comment to it:
									String allPreviousComments = commitComments.get(commitSHA);
									commitComments.put(commitSHA, StringManipulations.concatTwoStringsWithSpace(allPreviousComments, commentBody));
								}
								else
									commitComments.put(commitSHA, commentBody);
						}
					i++;
				}
				else
					linesWithError++;
				if (i % 100000 == 0)
					MyUtils.println(Constants.integerFormatter.format(i), 2);
//				if (i ==100000)
//					break;
			}
			br6.close();
			MyUtils.println("Number of comments read: " + Constants.integerFormatter.format(i), 2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", 2);
			else
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

			//7: Checking number of tags in each bug / bug+itsComments and saving the results:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("7- Checking bugs and bugs+theirComments (the two HashMaps that have been read) and counting the number of tags in them and writing them:", 1);
			MyUtils.println("Started ...", 2);

			FileWriter writer1 = new FileWriter(DATASET_DIRECTORY_GH_TSV+"\\numberOfTagsInEachBug.tsv");
			writer1.append("projectId\tbugNumber\t#ofTagsInProjectInfo\t#ofTagsInBug\t#ofTagsInBugComments\t#ofTagsInProjectInfo+Bug\t#ofTagsInProjectInfo+Bug+CommentsOfTheBug\tprojectTitle\tbug\tbugComments\n");
			i = 0;
			int bugCommentsNotFound = 0;
			for (String projectId_bugNumber: bugs.keySet()){
				String[] twoFields = projectId_bugNumber.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS); 
				String projectId = twoFields[0];
				String bugNumber = twoFields[1];
				HashSet<String> aggregatedIncludedTags = new HashSet<String>();
				
				//First, considering the tags in project textual info:
				String projectText = projects.get(projectId);
				fillTagsThatAreInInputString(projects.get(projectId), allTags, aggregatedIncludedTags);
				int numberOfTagsInProjectInfo = aggregatedIncludedTags.size();
				
				//Then, considering the bug info (accumulated to the previous number):
				HashSet<String> includedTagsInBug = new HashSet<String>();
					fillTagsThatAreInInputString(bugs.get(projectId_bugNumber), allTags, includedTagsInBug);
					fillTagsThatAreInInputString(bugs.get(projectId_bugNumber), allTags, aggregatedIncludedTags);
				int numberOfTagsInBug = includedTagsInBug.size();
				int numberOfTagsInProjectInfoAndBug = aggregatedIncludedTags.size();
				
				//Finally, considering the comments' info (accumulated to the previous number):
				HashSet<String> includedTagsInCommentsOfABug = new HashSet<String>();
				if (bugComments.containsKey(projectId_bugNumber)){
					fillTagsThatAreInInputString(bugComments.get(projectId_bugNumber), allTags, includedTagsInCommentsOfABug);
					fillTagsThatAreInInputString(bugComments.get(projectId_bugNumber), allTags, aggregatedIncludedTags);
				}
				else
					bugCommentsNotFound++;//: this is not necessarily an error. Some bugs may have no comments.
				int numberOfTagsInCommentsOfABug = includedTagsInCommentsOfABug.size();
				int numberOfTagsInProjectInfoAndBugAndItsComments = aggregatedIncludedTags.size();
				
				String bugText = bugs.get(projectId_bugNumber);
				
				String commentsText;
				if (bugComments.containsKey(projectId_bugNumber))
					commentsText = bugComments.get(projectId_bugNumber);
				else
					commentsText = "";
				writer1.append(projectId + "\t" + bugNumber + "\t" 
						+ numberOfTagsInProjectInfo + "\t" 
						+ numberOfTagsInBug + "\t" 
						+ numberOfTagsInCommentsOfABug + "\t" 
						+ numberOfTagsInProjectInfoAndBug + "\t" 
						+ numberOfTagsInProjectInfoAndBugAndItsComments 
						
						+ "\t"
						+ projectText + "\t" 
						+ bugText + "\t"
						+ commentsText
						
						+ "\n");
				
				i++;
			}
			writer1.flush();
			writer1.close();
			
			if (bugCommentsNotFound > 0)
				MyUtils.println("There are " + Constants.integerFormatter.format(bugCommentsNotFound) + " bugs with no comment.", 2);
			MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);


			//8: Checking number of tags in each PR / PR+itsComments and saving the results:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("8- Checking PRs and PRs+theirComments (the two HashMaps that have been read) and counting the number of tags in them and writing them:", 1);
			MyUtils.println("Started ...", 2);

			FileWriter writer2 = new FileWriter(DATASET_DIRECTORY_GH_TSV+"\\numberOfTagsInEachPR.tsv");
			writer2.append("projectId\tPRNumber\t#ofTagsInPR\t#ofTagsInPRComments\t#ofTagsInPR+CommentsOfThePR\tPR\tprComments\n");
			i = 0;
			int prCommentsNotFound = 0;
			for (String projectId_prNumber: prs.keySet()){
				String[] twoFields = projectId_prNumber.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS); 
				String projectId = twoFields[0];
				String prNumber = twoFields[1];
				HashSet<String> aggregatedIncludedTags = new HashSet<String>();
				
				//First, considering the PR info
				HashSet<String> includedTagsInPR = new HashSet<String>();
					fillTagsThatAreInInputString(prs.get(projectId_prNumber), allTags, includedTagsInPR);
					fillTagsThatAreInInputString(prs.get(projectId_prNumber), allTags, aggregatedIncludedTags);
				int numberOfTagsInPR = includedTagsInPR.size();
				
				//Then, considering the comments' info (accumulated to the previous number):
				HashSet<String> includedTagsInCommentsOfAPR = new HashSet<String>();
				if (prComments.containsKey(projectId_prNumber)){
					fillTagsThatAreInInputString(prComments.get(projectId_prNumber), allTags, includedTagsInCommentsOfAPR);
					fillTagsThatAreInInputString(prComments.get(projectId_prNumber), allTags, aggregatedIncludedTags);
				}
				else
					prCommentsNotFound++;//: this is not necessarily an error. Some PRs may have no comments.
				int numberOfTagsInCommentsOfAPR = includedTagsInCommentsOfAPR.size();
				int numberOfTagsInPRAndItsComments = aggregatedIncludedTags.size();
				
				String prText = prs.get(projectId_prNumber);
				
				String commentsText;
				if (prComments.containsKey(projectId_prNumber))
					commentsText = prComments.get(projectId_prNumber);
				else
					commentsText = "";
				writer2.append(projectId + "\t" + prNumber + "\t" 
						+ numberOfTagsInPR + "\t" 
						+ numberOfTagsInCommentsOfAPR + "\t" 
						+ numberOfTagsInPRAndItsComments 
						
						+ "\t"
						+ prText + "\t"
						+ commentsText
						
						+ "\n");
				
				i++;
			}
			writer2.flush();
			writer2.close();
			
			if (prCommentsNotFound > 0)
				MyUtils.println("There are " + Constants.integerFormatter.format(prCommentsNotFound) + " PRs with no comment.", 2);
			MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);


			//9: Checking number of tags in each commit / commit+itsComments and saving the results:
			MyUtils.println("-----------------------------------", 1);
			MyUtils.println("9- Checking commits and commits+theirComments (the two HashMaps that have been read) and counting the number of tags in them and writing them:", 1);
			MyUtils.println("Started ...", 2);

			FileWriter writer3 = new FileWriter(DATASET_DIRECTORY_GH_TSV+"\\numberOfTagsInEachCommit.tsv");
			writer3.append("commitSHA\t#ofTagsInComit\t#ofTagsInCommitComments\t#ofTagsIncommit+CommentsOfTheCommit\tcommit\tcommitComments\n");
			i = 0;
			int commitCommentsNotFound = 0;
			for (String commitSHA: commits.keySet()){
				//First, considering the commit message:
				HashSet<String> includedTagsInCommit = new HashSet<String>();
				HashSet<String> aggregatedIncludedTags = new HashSet<String>();
				fillTagsThatAreInInputString(commits.get(commitSHA), allTags, includedTagsInCommit);
				fillTagsThatAreInInputString(commits.get(commitSHA), allTags, aggregatedIncludedTags);
				int numberOfTagsInCommit = includedTagsInCommit.size();
				
				//Finally, considering the comments' info (accumulated to the previous number):
				HashSet<String> includedTagsInCommentsOfACommit = new HashSet<String>();
				if (commitComments.containsKey(commitSHA)){
					fillTagsThatAreInInputString(commitComments.get(commitSHA), allTags, includedTagsInCommentsOfACommit);
					fillTagsThatAreInInputString(commitComments.get(commitSHA), allTags, aggregatedIncludedTags);
				}
				else
					commitCommentsNotFound++;//: this is not necessarily an error. Some issues may have no comments.
				int numberOfTagsInCommentsOfACommit = includedTagsInCommentsOfACommit.size();
				int numberOfTagsInCommitAndItsComments = aggregatedIncludedTags.size();
				
				String commitText = commits.get(commitSHA);

				String commentsText;
				if (commitComments.containsKey(commitSHA))
					commentsText = commitComments.get(commitSHA);
				else
					commentsText = "";
				writer3.append(commitSHA + "\t" 
						+ numberOfTagsInCommit + "\t" 
						+ numberOfTagsInCommentsOfACommit + "\t" 
						+ numberOfTagsInCommitAndItsComments 
						
						+ "\t"
						+ commitText + "\t"
						+ commentsText
						
						+ "\n");
				
				i++;
			}
			writer3.flush();
			writer3.close();
			
			if (commitCommentsNotFound > 0)
				MyUtils.println("There are " + Constants.integerFormatter.format(commitCommentsNotFound) + " commits with no comment.", 2);
			MyUtils.println("Finished.", 2);


			MyUtils.println("Finished.", 1);
			MyUtils.println("-----------------------------------", 0);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void checkCommitReferencesToBugs(){ 
		//This method checks commit messages and extracts some stats about number of references from bug commits to bug reports.
		try{
			//1: Read all commits:
			TreeMap<String, String[]> commits = TSVManipulations.readUniqueKeyAndItsValueFromTSV(
					DATASET_DIRECTORY_GH_TSV, "\\commits.tsv", null, 
					4, 6, "1$5", LogicalOperation.NO_CONDITION, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					true, 200000, 1, Constants.THIS_IS_REAL, "1");

			//2: Read all bugs in the form of HashSet<[projectId;;issueNumber]>:
			FileManipulationResult fMR = new FileManipulationResult();
			HashSet<String> bugs = TSVManipulations.readAndCombineTwoUniqueFieldsFromTSV(DATASET_DIRECTORY_GH_TSV, "bugs_complete.tsv", 
					fMR, 11, 1, 2, COMBINED_KEY_SEPARATOR, true, 50000, 1, Constants.THIS_IS_REAL, "2");

			//3: Read all pullRequests in the form of HashSet<[projectId;;prNumber]>:
			HashSet<String> prs = TSVManipulations.readAndCombineTwoUniqueFieldsFromTSV(DATASET_DIRECTORY_GH_TSV, "PRs_complete.tsv", 
					fMR, 11, 1, 2, COMBINED_KEY_SEPARATOR, true, 50000, 1, Constants.THIS_IS_REAL, "3");
			
			//4: Checking the reference numbers from commit messages:
			if (fMR.errors == 0){
				MyUtils.println("-----------------------------------", 1);
				MyUtils.println("4- Checking references from commits:", 1);
				MyUtils.println("Started ...", 2);
				int numberOfBugsReferenced = 0;
				int numberOfPRsReferenced = 0;
				int numberOfcommitsReferencingAtLeastOnePR = 0;
				int numberOfcommitsReferencingAtLeastOneBug = 0;
				int numberOfUnknownReferences = 0;
				int i = 0;
				HashSet<String> resolvedBugs = new HashSet<String>();
				for (Map.Entry<String, String[]> commitEntry: commits.entrySet()){
					String[] projectId_commitMessage = commitEntry.getValue();
					String projectId = projectId_commitMessage[0];
					String commitMessage = projectId_commitMessage[1].toLowerCase();
					boolean isReferencingToAPR = false;
					boolean isReferencingToABug = false;
					
					String regex = "(?:(?:clos|resolv)(?:e|es|ed|ing)|fix(?:es|ed|ing)?)(?:[\\s\\p{P}]*#[0-9]+)+";
					Pattern specificReferencePattern = Pattern.compile(regex);
					Matcher specificReferenceMatcher = specificReferencePattern.matcher(commitMessage);
					
					while (specificReferenceMatcher.find()){//Considering all the references (single or multiple references) from the commit:
					    String text2 = specificReferenceMatcher.group(0); //This is like "fixes #15 fix #33 resolves #79". So extracting the numbers.
						String regex2 = "[0-9]+";
						Pattern p2 = Pattern.compile(regex2);
						Matcher m2 = p2.matcher(text2);
						while (m2.find()){//Considering the bug number in a single reference (e.g., "fixed #38") or all the bug numbers in a multiple reference (e.g., "fixed #38 #77"):
							String referenceNumber = m2.group(0);  
							if (bugs.contains(projectId+COMBINED_KEY_SEPARATOR+referenceNumber)){
								isReferencingToABug = true;
								numberOfBugsReferenced++;
								resolvedBugs.add(projectId+COMBINED_KEY_SEPARATOR+referenceNumber);
							}
							else
								if (prs.contains(projectId+COMBINED_KEY_SEPARATOR+referenceNumber)){
									isReferencingToAPR = true;
									numberOfPRsReferenced++;
								}
								else
									numberOfUnknownReferences++;			
						}
					}
					if (isReferencingToABug)
						numberOfcommitsReferencingAtLeastOneBug++;
					if (isReferencingToAPR)
						numberOfcommitsReferencingAtLeastOnePR++;
					i++;
					if (i % 100000 == 0)
						MyUtils.println(Constants.integerFormatter.format(i), 2);
				}
				MyUtils.println("Number of references to bugs: " + Constants.integerFormatter.format(numberOfBugsReferenced), 2);
				MyUtils.println("Number of commits referencing to at least one bug: " + Constants.integerFormatter.format(numberOfcommitsReferencingAtLeastOneBug), 2);
				MyUtils.println("Number of references to PRs: " + Constants.integerFormatter.format(numberOfPRsReferenced), 2);
				MyUtils.println("Number of commits referencing to at least one PR: " + Constants.integerFormatter.format(numberOfcommitsReferencingAtLeastOnePR), 2);
				MyUtils.println("Number of commits checked: " + Constants.integerFormatter.format(commits.size()), 2);
				MyUtils.println("Number of distinct bugs that are referenced: " + Constants.integerFormatter.format(resolvedBugs.size()), 2);
				if (numberOfUnknownReferences > 0)
					MyUtils.println("There are " + Constants.integerFormatter.format(numberOfUnknownReferences) + " errors (references to bugs/prs that do not exist; just ignored them).", 2);

			}
			else
				MyUtils.println("Could not open the files (bugs or PRs)", 2);
				MyUtils.println("Finished.", 2);
			MyUtils.println("-----------------------------------", 1);

		}catch (Exception e){
			e.printStackTrace();
		}

	}
	//------------------------------------------------------------------------------------------------------------------------
	public static void sortArray(int[] a, int numberOfItems){
		int temp;
		for (int i=0; i<numberOfItems-1; i++)
			for (int j=0; j<numberOfItems-i-1; j++)
				if (a[j] > a[j+1]){
					temp = a[j];
					a[j] = a[j+1];
					a[j+1] = temp;
				}
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static void checkJoinedKeywords(){
		//This methods checks all bugs and finds joined consecutive keywords that make an SO tag:
			//Different combinations are considered:
				//2, 3 and 4 consecutive tags are considered. Also it checks and reduces the false positives.
		HashSet<String> tags = TSVManipulations.readUniqueFieldFromTSV(DATASET_DIRECTORY_SO_TSV, "occurrences.tsv", 0, 2, 
				LogicalOperation.NO_CONDITION, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT,
				true, 0, 100000, Constants.THIS_IS_REAL, "");
//		TreeMap<String, String[]> bugs = TSVManipulations.readUniqueKeyAndItsValueFromTSV(DATASET_DIRECTORY_GH_TSV, "bugs_complete.tsv", null, 
//				4, 11, "10", LogicalOperation.NO_CONDITION, 
//				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
//				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, true, 10000, 0, Constants.THIS_IS_REAL, "");
		
		TreeMap<String, String[]> bugs = TSVManipulations.readUniqueKeyAndItsValueFromTSV("C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\4-TSV-ATempFolder\\Temp2", "7-projects.tsv", null, 
				0, 13, "2", LogicalOperation.NO_CONDITION, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
				0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, true, 10000, 0, Constants.THIS_IS_REAL, "");
		
		HashMap<String, Integer> results = new HashMap<String, Integer>();
		int counter = 0;
		for (Map.Entry<String, String[]> entry: bugs.entrySet()){
//			String url = entry.getKey();
			String bugBody = entry.getValue()[0];
			String[] keywords = bugBody.split(" ");
			//Removing the "." at the end of sentences:
			for (int i=0; i< keywords.length; i++)
				if (keywords[i].endsWith("."))
					keywords[i] = keywords[i].substring(0, keywords[i].length()-1);

			//Checking the 2 word tags:
			for (int i=0; i< keywords.length-1; i++){
				if (DataPreparation.lengthesAreOkayToBeCombined(keywords[i], keywords[i+1])){ 
					for (int j=0; j<3; j++){
						String[] conjunction = {"", "-", "."};
						String twoKeywordsCombined = (keywords[i] + conjunction[j] + keywords[i+1]).toLowerCase();
						if (tags.contains(twoKeywordsCombined)){
							String key = keywords[i] + " + " + keywords[i+1] + " --> " + twoKeywordsCombined;
							if (results.containsKey(key))
								results.put(key, results.get(key)+1);
							else
								results.put(key, 1);
							break; //:means that this combination is a tag, so stop looking for other combinations.
						}
					}
				}
			}

//			//Checking the 3 word tags:
//			for (int i=0; i< keywords.length-2; i++){
//				if (lengthesAreOkayToBeCombined(keywords[i], keywords[i+1], keywords[i+2])){ 
//					for (int j=0; j<3; j++){
//						String[] conjunction1 = {"", "-", "."};
//						for (int k=0; k<3; k++){
//							String[] conjunction2 = {"", "-", "."};
//							String threeKeywordsCombined = (keywords[i] + conjunction1[j] + keywords[i+1] + conjunction2[k] + keywords[i+2]).toLowerCase();
//							if (tags.contains(threeKeywordsCombined)){
//								String key = keywords[i] + " + " + keywords[i+1] + " + " + keywords[i+2] + " --> " + threeKeywordsCombined;
//								if (results.containsKey(key))
//									results.put(key, results.get(key)+1);
//								else
//									results.put(key, 1);
//								break; //:means that this combination is a tag, so stop looking for other combinations.
//							}
//						}
//					}
//				}
//			}
			
//			//Checking the 4 word tags:
//			for (int i=0; i< keywords.length-3; i++){
//				if (lengthesAreOkayToBeCombined(keywords[i], keywords[i+1], keywords[i+2], keywords[i+3])){ 
//					for (int j=0; j<3; j++){
//						String[] conjunction1 = {"", "-", "."};
//						for (int k=0; k<3; k++){
//							String[] conjunction2 = {"", "-", "."};
//							for (int m=0; m<3; m++){
//								String[] conjunction3 = {"", "-", "."};
//								String fourKeywordsCombined = (keywords[i] 
//										+ conjunction1[j] + keywords[i+1] 
//												+ conjunction2[k] + keywords[i+2] 
//														+ conjunction3[m] + keywords[i+3]).toLowerCase();
//								if (tags.contains(fourKeywordsCombined)){
//									String key = keywords[i] + " + " + keywords[i+1] + " + " + keywords[i+2] + " + " + keywords[i+3] + " --> " + fourKeywordsCombined;
//									if (results.containsKey(key))
//										results.put(key, results.get(key)+1);
//									else
//										results.put(key, 1);
//									break; //:means that this combination is a tag, so stop looking for other combinations.
//								}
//							}
//						}
//					}
//				}
//			}
			counter++;
			if (counter % 1000 == 0)
				System.out.println(counter);
		}
		for (Map.Entry<String, Integer> entry: results.entrySet())
			System.out.println(entry.getKey() + "\t " + entry.getValue());
		//Map.Entry<String, ArrayList<String[]>> entry: projectIdBugNumberAndTheirEvents.entrySet()
		
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) {
//		Was ran successfully (20161229-1611):
//		checkCommitReferencesToBugs();
		
		checkJoinedKeywords();
		
//		System.out.println("Testing...");
//		String commitMessage = "This fixes #23 also #3 fixed#24 fix #25, #26 resolves #27 #28#29 resolved#30 #31 ,  #32. Also see #33";
//		String regex = "(?:(?:clos|resolv)(?:e|es|ed|ing)|fix(?:es|ed|ing)?)(?:[\\s\\p{P}]*#[0-9]+)+";
////		String regex = "clos(e|es|ed|ing)([ ,]*#[0-9]+)+ ?|fix(es|ed|ing)?([ ,]*#[0-9]+)+ ?|resolv(e|es|ed|ing)([ ,]*#[0-9]+)+ ?";
//		Pattern p = Pattern.compile(regex);
//		Matcher m = p.matcher(commitMessage);
//		while (m.find()){
//		    String text = m.group(0);
//			String regex2 = "[0-9]+";
//			Pattern p2 = Pattern.compile(regex2);
//			Matcher m2 = p2.matcher(text);
//			System.out.println("One commit:");
//			while (m2.find())
//				System.out.println(m2.group(0));
//		}
//		
		
//        String commitMessage = "This fixes #23 fixed#24 fix #25, #26 "
//                + "resolves #27 #28#29 resolved#30 #31 ,  #32. Also see #33";
//        String regexBugReference    = "(?<oneBug>#\\d+)"; 
//        String regexBugReferences   = "(?<someBugs>(\\s*,*\\s*" + regexBugReference + "\\s*)+)"; 
//        String regex = 
//                "(?<oneCase>(?<resolution>clos(e|es|ed|ing)|fix(|es|ed|ing)|resolv(e|es|ed|ing))"   
//                        + regexBugReferences
//                        + ")";
//        Pattern p = Pattern.compile(regex);
//        Matcher m = p.matcher(commitMessage);
//        while (m.find()){
//            String resolution   = m.group("resolution");
//            String someBugs     = m.group("someBugs");
//            Pattern p2 = Pattern.compile(regexBugReference);
//            Matcher m2 = p2.matcher(someBugs);
//            StringBuilder sb = new StringBuilder();
//            String comma = "";      // first time special
//            while (m2.find()) {
//                String oneBug = m2.group("oneBug");
//                sb.append(comma + oneBug);
//                comma = ", ";       // second time and onwards
//            }
//            System.out.format("%8s %s%n", resolution, sb.toString());
//        }
		
		
		
//		String s = "This fixes #23 fixed#24 fix #25, #26 resolves #27 #28#29 resolved#30 #31. Also see #444";
//		String regex = "clos(e|es|ed|ing) ?#[0-9]+" 
//				+ "|fix(es|ed|ing)? ?#[0-9]+" 
//				+ "|resolv(e|es|ed|ing) ?#[0-9]+";
//		Pattern p = Pattern.compile(regex);
//		Matcher m = p.matcher(s);
//		while (m.find()){
//			System.out.println(m.group(0));
//		}
//		System.out.println(s);
//		s = s.replaceAll(regex, "---");
//		System.out.println(s);
		
//		Pattern pp = Pattern.compile("close #[0-9]+, #|closes #[0-9]+, #|closed #[0-9]+, #|fix #[0-9]+, #|fixes #[0-9]+, #|fixed #[0-9]+, #|resolve #[0-9]+, #|resolves #[0-9]+, #|resolved #[0-9]+, #");
//		Matcher mm = pp.matcher(s);
//		if(mm.find())
//			System.out.println("Thisss: " + mm.group(0));

	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------
}
