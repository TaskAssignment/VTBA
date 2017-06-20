package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
//import java.util.ArrayList;
import java.util.Iterator;
//import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.LogicalOperation;

public class CSVManipulations {
	//----------------------------------------------------------------------------------------------------------------------------------------
	private static final String DATASET_DIRECTORY_GH_CSV = "C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\1-CSV";
	private static final String DATASET_DIRECTORY_GH_TSV = "C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\2-TSV\\3- 13 projects + 2 project families (13 + 6 more projects)";
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static String jsonArrayToShortForm(String s, Boolean convertedCorrectly){
		String result = "";
		try{
			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(s);
			Iterator<?> it = jsonArray.iterator();
			while (it.hasNext()){
				JSONObject jsonObject = (JSONObject) it.next();
				//			System.out.println("------------" + jsonObject + "-----------");
				if (result == "")
					result = result + jsonObject.get("_id") + ":" + jsonObject.get("amount");
				else
					result = result + "," + jsonObject.get("_id") + ":" + jsonObject.get("amount");
				}//while (it.
			convertedCorrectly = true;
			}//try.
		catch(Exception e){
			e.printStackTrace();
			convertedCorrectly = false;
			}//catch(Exce....
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult cleanFile(String inputPathAndFileName, String outputPathAndFileName, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			MyUtils.println(writeMessageStep + "Reading input (csv) file, cleaning the redundant (control) characters and saving into output file:", indentationLevel);
			MyUtils.println(outputPathAndFileName, indentationLevel);
			MyUtils.println("Started ...", indentationLevel);

			BufferedReader br;
			br = new BufferedReader(new FileReader(inputPathAndFileName)); 

			//First line (title):
			String s = br.readLine();
			String[] inputTitles = s.split(",");
			String tabSeparatedTitleLine = inputTitles[0];
			for (int i=1; i< inputTitles.length; i++)
				tabSeparatedTitleLine = tabSeparatedTitleLine + "\t" + inputTitles[i];
			int totalNumberOfFields = inputTitles.length;
			
			FileWriter writer = new FileWriter(outputPathAndFileName);
			writer.append(tabSeparatedTitleLine + "\n");

			//The next lines (projects' data):
			String allFieldsTabSeparated = "";
			int numberOfRecordsAdded = 0;
			int aa = 0;
			while ((s=br.readLine())!=null){
				//Removing control characters (tab/enter/...):
				String regexPatternToExclude = "[^A-Za-z0-9,./<>?\\[\\]{};':\"`\\-=~!@#$%^&*()_+\\\\|]";
				s = s.replaceAll(regexPatternToExclude, " ");
//				System.out.println(s);

				aa++;
				System.out.println();
				if (aa>20)
					break;
				int length = s.length();
				int lastCommaIndex = -1;
				int numberOfQoutationsInThisField = 0;
				int numberOfFieldsDetected = 0;
				int i = 0;
				while (i < length){
					while ((i == length-1) && (numberOfFieldsDetected < totalNumberOfFields)){
						String s2 = br.readLine();
						s2 = s2.replaceAll(regexPatternToExclude, " ");
						s = s + s2;
						length = s.length();
					}//while ((i == length - 1) && ....
						
					switch (s.charAt(i)){
					case ',':
						if (numberOfQoutationsInThisField == 0){//:means that this field is a number or simple string (without double quotes). 
							String field = s.substring(lastCommaIndex+1, i);
							numberOfFieldsDetected++;
							if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
								allFieldsTabSeparated = field;
							else//:i.e., this is not the first field.
								allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
							lastCommaIndex = i;
						}//if (numberOfQuotesInThisField == 0).
						else	
							if (numberOfQoutationsInThisField == 2){ //:means that we detected a complete field, but this field is an empty string (shown with two double quotes).
								numberOfFieldsDetected++;
								if (numberOfFieldsDetected > 1)
									allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB; //:i.e., allFieldsTabSeparated + "\t" + ""
								//otherwise (else): allFieldsTabSeparated = "" as it has been initialized before the "for" loop.
								lastCommaIndex = i;
								numberOfQoutationsInThisField = 0;
							}//if (numberOfQuotesInThisField == 2).
							else//of if (numberOfQuotesInThisField == 2)
								if (numberOfQoutationsInThisField % 4 == 2){ //:means that we detected a complete field, but the field item is a json object (a json array for projects.csv) or an sha with three quotes in each side.
									String field = s.substring(lastCommaIndex+1, i);
									numberOfFieldsDetected++;
									if (field.startsWith("\"\"\"") && field.endsWith("\"\"\"")) //:means that the field is sha of a commit surrounded by three quotes in each side.
										field = field.substring(3, field.length()-3);//:remove the quotes.
									else{//:the field is a json object (array) with lots of items surrounded by two quotes in each side.
										field = field.substring(1, field.length()-1);//:remove the '"[' from the begining and te ']"' from the end.
										field = field.replace("\"\"", "\"");
										Boolean convertedCorrectly = true;
										field = jsonArrayToShortForm(field, convertedCorrectly);
										if (convertedCorrectly == false)
											fMR.errors++;
									}//else of if (field.startsWith("\"\"\"") && field.endsWith("\"\"\""))
									if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
										allFieldsTabSeparated = field;
									else//:i.e., this is not the first field.
										allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
									lastCommaIndex = i;
									numberOfQoutationsInThisField = 0;
								}//if (numberOfQuotesInThisField % 4 == 2).
						break;
					case '"':
						numberOfQoutationsInThisField++;
						break;
					}//switch.
				}//while (i < length).
				//If the last field is sha:
				String field = s.substring(lastCommaIndex+1, s.length());
				if ((numberOfQoutationsInThisField == 6) && field.startsWith("\"\"\"") && field.endsWith("\"\"\"")) { //:means that we detected an sha with three quotes in each side.
					numberOfFieldsDetected++;
					field = field.substring(3, field.length()-3);//:remove the quotes.
					if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
						allFieldsTabSeparated = field;
					else//:i.e., this is not the first field.
						allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
				}//if ((numberOfQuotesInThisField % 4 == 2) &&.

				writer.append(allFieldsTabSeparated + "\n");
				numberOfRecordsAdded++;
				if (numberOfRecordsAdded % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(numberOfRecordsAdded));

			}//while ((s....
			writer.flush();
			writer.close();
			br.close();

			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records written: " + Constants.integerFormatter.format(numberOfRecordsAdded) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Finished.");
			if (fMR.errors > 0)
				fMR.doneSuccessfully = 1;
		}catch (Exception e){
			e.printStackTrace();
			fMR.errors = 1;
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;

		}//cleanFile().
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult generateProjectsTSV(String inputPathAndFileName, String outputPathAndFileName, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "Reading input (csv) file and writing into output (TSV) file:");
			System.out.println(MyUtils.indent(indentationLevel) + outputPathAndFileName);
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");

			BufferedReader br;
			br = new BufferedReader(new FileReader(inputPathAndFileName)); 
			
			//First line (title):
			String s = br.readLine();
			String[] inputTitles = s.split(",");
			String tabSeparatedTitleLine = inputTitles[0];
			for (int i=1; i< inputTitles.length; i++)
				tabSeparatedTitleLine = tabSeparatedTitleLine + "\t" + inputTitles[i];
			FileWriter writer = new FileWriter(outputPathAndFileName);
			writer.append(tabSeparatedTitleLine + "\n");
			
			//The next lines (projects' data):
			String allFieldsTabSeparated = "";
			int numberOfRecordsAdded = 0;
			while ((s=br.readLine())!=null){
				int length = s.length();
				int lastCommaIndex = -1;
				int numberOfQoutationsInThisField = 0;
				int numberOfFieldsDetected = 0;
				for (int i=0; i<length; i++){
					switch (s.charAt(i)){
						case ',':
							if (numberOfQoutationsInThisField == 0){//:means that this field is a number or simple string (without double quotes). 
								String field = s.substring(lastCommaIndex+1, i);
								numberOfFieldsDetected++;
								if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
									allFieldsTabSeparated = field;
								else//:i.e., this is not the first field.
									allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
								lastCommaIndex = i;
								}//if (numberOfQuotesInThisField == 0).
							else	
								if (numberOfQoutationsInThisField == 2){ //:means that we detected a complete field, but this field is an empty string (shown with two double quotes).
									numberOfFieldsDetected++;
									if (numberOfFieldsDetected > 1)
										allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB; //:i.e., allFieldsTabSeparated + "\t" + ""
									//otherwise (else): allFieldsTabSeparated = "" as it has been initialized before the "for" loop.
									lastCommaIndex = i;
									numberOfQoutationsInThisField = 0;
								}//if (numberOfQuotesInThisField == 2).
								else//of if (numberOfQuotesInThisField == 2)
									if (numberOfQoutationsInThisField % 4 == 2){ //:means that we detected a complete field, but the field item is a json object (a json array for projects.csv) or an sha with three quotes in each side.
										String field = s.substring(lastCommaIndex+1, i);
										numberOfFieldsDetected++;
										if (field.startsWith("\"\"\"") && field.endsWith("\"\"\"")) //:means that the field is sha of a commit surrounded by three quotes in each side.
											field = field.substring(3, field.length()-3);//:remove the quotes.
										else{//:the field is a json object (array) with lots of items surrounded by two quotes in each side.
											field = field.substring(1, field.length()-1);//:remove the '"[' from the begining and te ']"' from the end.
											field = field.replace("\"\"", "\"");
											Boolean convertedCorrectly = true;
											field = jsonArrayToShortForm(field, convertedCorrectly);
											if (convertedCorrectly == false)
												fMR.errors++;
										}//else of if (field.startsWith("\"\"\"") && field.endsWith("\"\"\""))
										if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
											allFieldsTabSeparated = field;
										else//:i.e., this is not the first field.
											allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
										lastCommaIndex = i;
										numberOfQoutationsInThisField = 0;
									}//if (numberOfQuotesInThisField % 4 == 2).
							break;
						case '"':
							numberOfQoutationsInThisField++;
							break;
					}//switch.
				}//for (i....
				//If the last field is sha:
				String field = s.substring(lastCommaIndex+1, s.length());
				if ((numberOfQoutationsInThisField == 6) && field.startsWith("\"\"\"") && field.endsWith("\"\"\"")) { //:means that we detected an sha with three quotes in each side.
					numberOfFieldsDetected++;
					field = field.substring(3, field.length()-3);//:remove the quotes.
					if (numberOfFieldsDetected == 1)//:i.e., this is the first field.
						allFieldsTabSeparated = field;
					else//:i.e., this is not the first field.
						allFieldsTabSeparated = allFieldsTabSeparated + Constants.TAB + field;
				}//if ((numberOfQuotesInThisField % 4 == 2) &&.
								
				writer.append(allFieldsTabSeparated + "\n");
				numberOfRecordsAdded++;
				if (numberOfRecordsAdded % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(numberOfRecordsAdded));

			}//while ((s....
			writer.flush();
			writer.close();
			br.close();
			
			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records written: " + Constants.integerFormatter.format(numberOfRecordsAdded) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Finished.");
			if (fMR.errors > 0)
				fMR.doneSuccessfully = 1;
		}catch (Exception e){
			e.printStackTrace();
			fMR.errors = 1;
			}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//generateProjectsTSV().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	private static String extractMainLanguages(String languagesAndTheirLinesOfCode){
		//Extract languages with more than 15% contribution in them:
			//Input: [python^19218343;;javascript^7143335;;html^6310229;;css^1383894;;coffeescript^343222;;cucumber^60627;;shell^26155;;actionscript^3539;;srecode template^565;;makefile^353]
			//Output: [python^56;;javascript^21;;html^18] --> These are percentages.
		String result = "";
		languagesAndTheirLinesOfCode = languagesAndTheirLinesOfCode.substring(1,  languagesAndTheirLinesOfCode.length()-1);
		if (!languagesAndTheirLinesOfCode.equals("") && !languagesAndTheirLinesOfCode.equals(" ")){
			String[] langs = languagesAndTheirLinesOfCode.split(Constants.SEPARATOR_FOR_ARRAY_ITEMS);
			//Identifying the total LOC:
			long totalLinesOfCode = 0;
			for (int i=0; i< langs.length; i++){
				String[] langOrLOC = langs[i].split(Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM_REGEX);
				totalLinesOfCode = totalLinesOfCode + Long.parseLong(langOrLOC[1]);
			}
			//Identifying the languages with more than 15% of total LOC:
			for (int i=0; i< langs.length; i++){
				String[] langOrLOC = langs[i].split(Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM_REGEX);
				long linesOfCodeInThisLanguage = Long.parseLong(langOrLOC[1]);
				if (linesOfCodeInThisLanguage >= 0.15*totalLinesOfCode)
					if (result.equals(""))
						result = langOrLOC[0] 
								+ Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM 
								+ Long.toString(Math.round(100*(float)linesOfCodeInThisLanguage/totalLinesOfCode));
					else
						result = result + Constants.SEPARATOR_FOR_ARRAY_ITEMS + langOrLOC[0] 
								+ Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM 
								+ Long.toString(Math.round(100*(float)linesOfCodeInThisLanguage/totalLinesOfCode));
			}
			if (result.equals("")){
				String[] langOrLOC = langs[0].split(Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM_REGEX);
				result = langOrLOC[0];
			}
		}
		result = "[" + result + "]";
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult addGHTorrentIdToTheProjects_and_setMainLanguages(String tsvInputPath, String tsvInputFileName, 
			String csvInputPath, String csvInputFileName,
			String tsvOutputPath, String tsvOutputFileName,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		//This method:
			//1- Reads info of 35 projects (my data) and extract their languages (the ones with at least 15% of total lines of code). For example, for "edx/edx-platform", the percentages are as follows: [python^19218343;;javascript^7143335;;html^6310229;;css^1383894;;coffeescript^343222;;cucumber^60627;;shell^26155;;actionscript^3539;;srecode template^565;;makefile^353]. For this example, the first three languages are the main languages and the result will be stored in the output file like [python^56;;javascript^21;;html^18].
			//2- Reads projects.csv (7GB file obtained from GHTorrent website) in a HashMap<name --> GHTorrentId> 
			//3- looks for my data ("projects.tsv", gathered using GH API by Parley; the 35 projects that we need their bugs and other info) and obtains their GHTorrentId using the hashMap in step 2.
			//4- Then adds a column to my projects file; GHTorrentId
//		HashSet<String> projectsWithoutPublicIssues = new HashSet<String>();
//		projectsWithoutPublicIssues.add("mozilla-b2g/gaia");
//		projectsWithoutPublicIssues.add("scala/scala");
//		projectsWithoutPublicIssues.add("edx/edx-platform");
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			MyUtils.println(writeMessageStep + "Finding GHTorrent ID of projects and adding them to the projects file:", indentationLevel);
			MyUtils.println("Started ...", indentationLevel+1);
			
			//First, read the 35 project names in a HashSet (because we don't want to read million of projects' info in the next step and store all of them in another HashMap):
			MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep + "-1- Reading 401 project file into a HashSet:", indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);
			BufferedReader br1 = new BufferedReader(new FileReader(tsvInputPath + "\\" + tsvInputFileName + ".tsv")); 
			String s = br1.readLine(); //Skip the title line.
			HashSet<String> projectNames = new HashSet<String>();
			String fields[];
			int i = 0;
			int linesWithError = 0;
			while((s = br1.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (projectNames.contains(fields[1]))
					linesWithError++;
				else{
					projectNames.add(fields[1]); //fields[1] is owner/repo
					i++;
				}
			}
			br1.close();
			MyUtils.println("Number of projects to look for GHTorrentProjectId: " + Constants.integerFormatter.format(i), indentationLevel+2);
			if (linesWithError > 0){
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", indentationLevel+2);
				fMR.errors++;
			}
			else
				MyUtils.println("Finished.", indentationLevel+2);
			MyUtils.println("-----------------------------------", indentationLevel+1);
			
			MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep + "-2- Reading GHTorrent project file into a hashMap:", indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);
			HashMap<String, String> projectNameAndGHTorrentId = new HashMap<String, String>();
			BufferedReader br2 = new BufferedReader(new FileReader(csvInputPath + "\\" + csvInputFileName + ".csv")); 
			while((s = br2.readLine()) != null) {
				if (s.matches("\\d+,\"https://api\\.github.com/.+")){ //: This regex is to check that the line is okay and we are not dealing with a broken line from previous line.  
					fields = s.split(","); 
					//Extract user/repo:
					String projectName = fields[1].substring(30, fields[1].length()-1);//field[1] is in the form of: "https://api.github.com/repos/rails/rails"
					if (projectNames.contains(projectName))//:We just need the info of 32 projects, so why store the info of all millions of projects?! That's why we added this "if". 
						projectNameAndGHTorrentId.put(projectName, fields[0]);
//					System.out.println(projectName + "   -->   GHTorrentId: " + fields[0]);
				}
				else
					if (!(s.matches("-1,\\\\N,-1,\".+"))){ //The first line in projects.csv is like this: -1,\N,-1,"noproject","Fake entry to
						linesWithError++;
//						MyUtils.println("Error: "+ s, indentationLevel+2);
					}
				i++;
				if (i % showProgressInterval == 0)
					MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+2);
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal) 
						break;
			}
			br2.close();
			if (linesWithError > 0){
				MyUtils.println(linesWithError+" lines in \"" + csvInputPath + "\\" + csvInputFileName + ".csv\" had error (e.g., broken lines from previous projects) and skipped.", indentationLevel+2);
				fMR.errors++;
			}
			MyUtils.println("Finished.", indentationLevel+2);
			MyUtils.println("-----------------------------------", indentationLevel+1);

			MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep + "-3- Reading 401 project file and obtain the GHTorrent ids from the previously read hashMap and adding it to 401 project file:", indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+2);
			BufferedReader br3 = new BufferedReader(new FileReader(tsvInputPath + "\\" + tsvInputFileName + ".tsv")); 
			FileWriter writer = new FileWriter(tsvOutputPath + "\\" + tsvOutputFileName + ".tsv");
			s = br3.readLine();
			fields = s.split(Constants.TAB);
			String line = fields[0] + Constants.TAB + "GHTorrentProjectId"; //id	GHTorrentProjectId.
			for (int j=1; j<fields.length; j++)
				line = line + Constants.TAB + fields[j];
			line = line + "\n";
			writer.append(line);
			linesWithError = 0;
			i = 0;
			
			while((s = br3.readLine()) != null) {
				fields = s.split(Constants.TAB);
				if (projectNameAndGHTorrentId.containsKey(fields[1])){
//					if (!projectsWithoutPublicIssues.contains(fields[1])){ //: for these three projects, we do not need the information (because the bugs' info are not available). So just do not consider them.
						line = fields[0] + Constants.TAB + projectNameAndGHTorrentId.get(fields[1]); //id	GHTorrentProjectId.
						line = line + Constants.TAB + fields[1];
						line = line + Constants.TAB + fields[2];
						line = line + Constants.TAB + extractMainLanguages(fields[4]); //: This is instead of fields[3], which was an empty field before.
						line = line + Constants.TAB + fields[4];
						line = line + "\n";
						writer.append(line);
						i++;
//					}
				}
				else
					linesWithError++;
			}
			br3.close();
			writer.flush();
			writer.close();
			fMR.doneSuccessfully++;
			if (linesWithError > 0){
				MyUtils.println(linesWithError+" projects in \"" + tsvInputPath + "\\" + tsvInputFileName + ".tsv\" were not found in the csv projects list.", indentationLevel+2);
				fMR.errors++;
			}
			MyUtils.println("Number of records written: " + Constants.integerFormatter.format(i), indentationLevel+2);
			if (linesWithError > 0)
				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", indentationLevel+2);
			else
				MyUtils.println("Finished.", indentationLevel+2);
			MyUtils.println("-----------------------------------", indentationLevel+1);

			if (linesWithError > 0)
				MyUtils.println("Finished with " + linesWithError + " errors.", indentationLevel+1);
			else
				MyUtils.println("Finished.", indentationLevel+1);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			System.out.println();
		}catch (Exception e){
			e.printStackTrace();
			fMR.doneSuccessfully = 0;
			fMR.errors++;
			}
		fMR.processed++;
		return fMR;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult generateIssueEventsAndPREventsTSVsFromIssueEventsCSV(String issueEventsCSVInputPath, String issueEventsCSVInputFileName,
			String projectsTSVInputPath, String projectsTSVInputFileName, 
			String tsvOutputPath, String issueEventsTSVOutputFileName, String issuesTSVOutputFileName,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
			//This method reads issueEvents ("issue_events.csv" that is downloaded from GHTorrent website), and, 
				//if the projectId is one of my 35 projects (in "projects_complete.tsv"), then adds the record (in tsv format) to the two separate TSV output files ("issueEvents_complete.tsv" and "PREvents_complete.tsv").
		FileManipulationResult fMR = new FileManipulationResult();
//		try{
//			if (wrapOutputInLines)
//				MyUtils.println("-----------------------------------", indentationLevel);
//			MyUtils.println(writeMessageStep + "Extracting issueEvents and PREvents from csv (and save them as two separate TSVs):", indentationLevel);
//			MyUtils.println("Started ...", indentationLevel+1);
//			
//			//First, read the GHTorrentProjectId of our 35 projects: 
//			HashSet<String> projectIds = TSVManipulations.readUniqueFieldFromTSV(projectsTSVInputPath+"\\"+projectsTSVInputFileName, 1, 6, LogicalOperation.NO_CONDITION, 
//					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
//					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
//					wrapOutputInLines, indentationLevel, showProgressInterval, testOrReal, writeMessageStep+"-1");
//			
//			//Then, read the issue_events.csv file and store them 35 project names in a HashSet (because we don't want to read million of projects' info in the next step and store all of them in another HashMap):
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//			MyUtils.println(writeMessageStep + "-1- Reading 401 project file into a HashSet:", indentationLevel+1);
//			MyUtils.println("Started ...", indentationLevel+2);
//			BufferedReader br1 = new BufferedReader(new FileReader(tsvInputPath + "\\" + tsvInputFileName + ".tsv")); 
//			String s = br1.readLine(); //Skip the title line.
//			HashSet<String> projectNames = new HashSet<String>();
//			String fields[];
//			int i = 0;
//			int linesWithError = 0;
//			while((s = br1.readLine()) != null) {
//				fields = s.split(Constants.TAB);
//				if (projectNames.contains(fields[1]))
//					linesWithError++;
//				else{
//					projectNames.add(fields[1]); //fields[1] is owner/repo
//					i++;
//				}
//			}
//			br1.close();
//			MyUtils.println("Number of projects to look for GHTorrentProjectId: " + Constants.integerFormatter.format(i), indentationLevel+2);
//			if (linesWithError > 0)
//				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", indentationLevel+2);
//			else
//				MyUtils.println("Finished.", indentationLevel+2);
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//			
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//			MyUtils.println(writeMessageStep + "-2- Reading GHTorrent project file into a hashMap:", indentationLevel+1);
//			MyUtils.println("Started ...", indentationLevel+2);
//			HashMap<String, String> projectNameAndGHTorrentId = new HashMap<String, String>();
//			BufferedReader br2 = new BufferedReader(new FileReader(csvInputPath + "\\" + csvInputFileName + ".csv")); 
//			while((s = br2.readLine()) != null) {
//				if (s.matches("\\d+,\"https://api\\.github.com/.+")){ //: This regex is to check that the line is okay and we are not dealing with a broken line from previous line.  
//					fields = s.split(","); 
//					//Extract user/repo:
//					String projectName = fields[1].substring(30, fields[1].length()-1);//field[1] is in the form of: "https://api.github.com/repos/rails/rails"
//					if (projectNames.contains(projectName))//:We just need the info of 35 projects, so why store the info of all millions of projects?! That's why we added this "if". 
//						projectNameAndGHTorrentId.put(projectName, fields[0]);
////					System.out.println(projectName + "   -->   GHTorrentId: " + fields[0]);
//				}
//				else
//					if (!(s.matches("-1,\\\\N,-1,\".+"))){ //The first line in projects.csv is like this: -1,\N,-1,"noproject","Fake entry to
//						linesWithError++;
////						MyUtils.println("Error: "+ s, indentationLevel+2);
//					}
//				i++;
//				if (i % showProgressInterval == 0)
//					MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+2);
//				if (testOrReal > Constants.THIS_IS_REAL)
//					if (i >= testOrReal) 
//						break;
//			}
//			br2.close();
//			if (linesWithError > 0)
//				MyUtils.println(linesWithError+" lines in \"" + csvInputPath + "\\" + csvInputFileName + ".csv\" had error and skipped.", indentationLevel+2);
//			MyUtils.println("Finished.", indentationLevel+2);
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//			MyUtils.println(writeMessageStep + "-3- Reading 401 project file and obtain the GHTorrent ids from the previously read hashMap and adding it to 401 project file:", indentationLevel+1);
//			MyUtils.println("Started ...", indentationLevel+2);
//			BufferedReader br3 = new BufferedReader(new FileReader(tsvInputPath + "\\" + tsvInputFileName + ".tsv")); 
//			FileWriter writer = new FileWriter(tsvOutputPath + "\\" + tsvOutputFileName + ".tsv");
//			s = br3.readLine();
//			fields = s.split(Constants.TAB);
//			String line = fields[0] + Constants.TAB + "GHTorrentProjectId"; //id	GHTorrentProjectId.
//			for (int j=1; j<fields.length; j++)
//				line = line + Constants.TAB + fields[j];
//			line = line + "\n";
//			writer.append(line);
//			linesWithError = 0;
//			i = 0;
//			
//			while((s = br3.readLine()) != null) {
//				fields = s.split(Constants.TAB);
//				if (projectNameAndGHTorrentId.containsKey(fields[1])){
//					line = fields[0] + Constants.TAB + projectNameAndGHTorrentId.get(fields[1]); //id	GHTorrentProjectId.
//					line = line + Constants.TAB + fields[1];
//					line = line + Constants.TAB + fields[2];
//					line = line + Constants.TAB + extractMainLanguages(fields[4]); //: This is instead of fields[3], which was an empty field before.
//					line = line + Constants.TAB + fields[4];
//					line = line + "\n";
//					writer.append(line);
//					i++;
//				}
//				else
//					linesWithError++;
//			}
//			br3.close();
//			writer.flush();
//			writer.close();
//			fMR.doneSuccessfully++;
//			if (linesWithError > 0)
//				MyUtils.println(linesWithError+" projects in \"" + tsvInputPath + "\\" + tsvInputFileName + ".tsv\" were not found in the csv projects list.", indentationLevel+2);
//			MyUtils.println("Number of records written: " + Constants.integerFormatter.format(i), indentationLevel+2);
//			if (linesWithError > 0)
//				MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", indentationLevel+2);
//			else
//				MyUtils.println("Finished.", indentationLevel+2);
//			MyUtils.println("-----------------------------------", indentationLevel+1);
//
//		
//			
//			
//			if (linesWithError > 0)
//				MyUtils.println("Finished with " + linesWithError. + " errors.", indentationLevel+1);
//			else
//				MyUtils.println("Finished.", indentationLevel+1);
//			if (wrapOutputInLines)
//				MyUtils.println("-----------------------------------", indentationLevel);
//			System.out.println();
//		}catch (Exception e){
//			e.printStackTrace();
//			fMR.doneSuccessfully = 0;
//			fMR.errors = 1;
//			}
//		fMR.processed++;
		return fMR;
		}
		//----------------------------------------------------------------------------------------------------------------------------------------
		//----------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) { 
//		String iOPath = "C:\\Users\\Ali Sajedi\\Documents\\GitHub\\BugTriaging2\\Data";
		//cleanFile() --> for converting all invalid chars to spaces.
		
//		//Was run successfully:
//		FileManipulationResult fMR = generateProjectsTSV(iOPath+"\\projects.csv", iOPath+"\\projects.tsv", 
//				true, 5, 0, Constants.THIS_IS_REAL, "");	
//		if (fMR.errors > 0)
//			System.out.println("ERRORRRRRRRRRR! (TSV created with " + Integer.toString(fMR.errors) + " errors.)");

		//Was run or not?:
//		cleanFile();
//		FileManipulationResult fMR = cleanFile(iOPath+"\\bugs.csv", iOPath+"\\bugs2.csv", 
//				true, 5, 0, Constants.THIS_IS_REAL, "");	
//		if (fMR.errors > 0)
//			System.out.println("ERRORRRRRRRRRR! (TSV created with " + Integer.toString(fMR.errors) + " errors.)");

		addGHTorrentIdToTheProjects_and_setMainLanguages(DATASET_DIRECTORY_GH_TSV, "projects", 
				DATASET_DIRECTORY_GH_CSV, "projects", 
				DATASET_DIRECTORY_GH_TSV, "projects_complete",
				true, 2000000, 0, Constants.THIS_IS_REAL, "");
//		System.out.println(s);
	}//main().
	//----------------------------------------------------------------------------------------------------------------------------------------
}//class.
