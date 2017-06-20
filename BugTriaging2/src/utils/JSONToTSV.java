package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.LogicalOperation;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//--------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------
public class JSONToTSV {
	//--------------------------------------------------------------------------------------------------------------------------------------------
	private static final String DATASET_DIRECTORY_GH_JSON = "C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\1-JSON\\3- 13 projects + 2 project families (13 + 6 more projects)";
	private static final String DATASET_DIRECTORY_GH_TSV = "C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\2-TSV\\3- 13 projects + 2 project families (13 + 6 more projects)";
	//--------------------------------------------------------------------------------------------------------------------------------------------
	static String getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(JSONObject jsO, String fieldName, String fileName, AtomicBoolean itIsTheFirstFieldInTheLine){
		String fieldValue;
		if (jsO.get(fieldName) == null)
			fieldValue = "";
		else
			fieldValue = jsO.get(fieldName).toString();
		//Remove everything that are not in the valid characters:
		if (Constants.USEFUL_FIELDS_IN_JSON_FILES.containsKey(fileName + ":FieldsToRemoveInvalidCharacters"))
			if (Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName + ":FieldsToRemoveInvalidCharacters").contains(fieldName))
				fieldValue = StringManipulations.clean(fieldValue);

		//delimiter between fields:
		if (fieldValue.equals(""))
			fieldValue = " ";
		if (itIsTheFirstFieldInTheLine.get())
			itIsTheFirstFieldInTheLine.set(false);
		else
			fieldValue = "\t" + fieldValue;
		return fieldValue;
	}//String getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(....
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//This method needs cleaning (i.e., use MyUtild.println, ...):
	public static FileManipulationResult copyNeededFieldsFromJSONIntoNewTSVFile(String inputPath, String outputPath, String fileName,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + " Reading input (json) file and writing into output (TSV) file:");
			System.out.println(MyUtils.indent(indentationLevel) + outputPath + "\\" + fileName + ".tsv");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");

			BufferedReader br = new BufferedReader(new FileReader(inputPath + "\\" + fileName + ".json")); 
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject;
			FileWriter writer = new FileWriter(outputPath + "\\" + fileName + ".tsv");
			String title = "";
			for (String field: Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName + ":labels")){
				if (!title.equals(""))	
					title = title + "\t";
				title = title + field;					
			}//for (String 
			writer.append(title + "\n");

			String s, severalFieldsSeparatedByDollarSign, firstLevelField, secondLevelJSONString, fieldValue;
			String[] fieldsArray;
			int i=0, j;
			AtomicBoolean itIsTheFirstFieldInTheLine = new AtomicBoolean();
			boolean itIsTheFirstMemberInTheJSONArray;
			while((s = br.readLine()) != null) {
				//s=s.replaceAll("\\p{Cntrl}", "");
				jsonObject= (JSONObject) jsonParser.parse(s);
				itIsTheFirstFieldInTheLine.set(true);
				for (String field: Constants.USEFUL_FIELDS_IN_JSON_FILES.get(fileName))
					if (field.matches(".*\\{\\}.*")){
						firstLevelField = field.substring(0, field.indexOf("{}"));

						if (jsonObject.get(firstLevelField) == null){
							secondLevelJSONString = "";
							severalFieldsSeparatedByDollarSign = field.substring(field.indexOf("{}") + 2);
							fieldsArray = severalFieldsSeparatedByDollarSign.split(Constants.FIELD_DELIMITER_FOR_JSON_OBJECT); //i.e., "\\&"
							for (j=0; j<fieldsArray.length; j++){
								if (itIsTheFirstFieldInTheLine.get()){
									itIsTheFirstFieldInTheLine.set(false);
									fieldValue = " ";
								}//if.
								else
									fieldValue = "\t "; 
								writer.append(fieldValue);
							}//for
						}//if.
						else{
							secondLevelJSONString = jsonObject.get(firstLevelField).toString();

							severalFieldsSeparatedByDollarSign = field.substring(field.indexOf("{}") + 2);
							fieldsArray = severalFieldsSeparatedByDollarSign.split(Constants.FIELD_DELIMITER_FOR_JSON_OBJECT); //i.e., "\\&"
							for (j=0; j<fieldsArray.length; j++){
								JSONParser secondLevelJSONParser = new JSONParser();
								JSONObject secondLevelJSONObject;

								secondLevelJSONObject = (JSONObject) secondLevelJSONParser.parse(secondLevelJSONString);
								fieldValue = getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(secondLevelJSONObject, fieldsArray[j], fileName, itIsTheFirstFieldInTheLine);  
								writer.append(fieldValue);
							}//for (j....
						}//else.
					} //if (field....
					else
						if (field.matches(".*\\[\\].*")){
							fieldValue = "[";
							String mainFieldName = field.substring(0, field.indexOf("[]"));//: The field comes in the form of "mainField[]subField" --> only one subField is allowed as of now.
							JSONArray jsonArray = (JSONArray) jsonObject.get(mainFieldName);
							Iterator<?> iterator = jsonArray.iterator();
							itIsTheFirstMemberInTheJSONArray = true;
							while (iterator.hasNext()) {
								Object jsonObjectOrString = iterator.next();
								String newFieldValue = "";
								if (jsonObjectOrString.toString().startsWith("{")){//: means that if the labels is a JSON array like this: "labels":[{"name":"..."}, {"name":"..."}, ...]
									JSONObject labelsJsonObject = (JSONObject) jsonObjectOrString;
									String subFieldName = field.substring(field.indexOf("[]")+2);
									if (subFieldName.contains("&")){//:means that there are more than one field in the object.
										String[] subFieldsNames = subFieldName.split("&");
										newFieldValue = (String) labelsJsonObject.get(subFieldsNames[0]);
										for (int k=1; k<subFieldsNames.length; k++)
											newFieldValue = newFieldValue + Constants.MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM 
											+ (String) labelsJsonObject.get(subFieldsNames[k]);
									}
									else
										newFieldValue = (String) labelsJsonObject.get(subFieldName);
								}//if (iterator....
								else//: means that it is a JSON array of Strings, like this: "labels":["x", "y", ...]
									newFieldValue = jsonObjectOrString.toString();

								if (itIsTheFirstMemberInTheJSONArray){
									itIsTheFirstMemberInTheJSONArray = false;
									fieldValue = fieldValue + newFieldValue; 								
								}//if (itIsThe....
								else
									fieldValue = fieldValue + Constants.SEPARATOR_FOR_ARRAY_ITEMS + newFieldValue;

							}//while.
							fieldValue = fieldValue + "]";

							if (itIsTheFirstFieldInTheLine.get())
								itIsTheFirstFieldInTheLine.set(false);
							else
								fieldValue = "\t" + fieldValue;

							writer.append(fieldValue);
						}//if (field....
						else{
							fieldValue = getValueFromJSONAndRemoveInvalidCharactersAndPutSeparatorBeforeIt(jsonObject, field, fileName, itIsTheFirstFieldInTheLine);  	
							writer.append(fieldValue);
						}//else.
				writer.append("\n");
				writer.flush();
				i++;

				if (i % showProgressInterval == 0)
					MyUtils.println(Integer.toString(i), indentationLevel+2);
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal) 
						break;
			}
			br.close();
			writer.flush();
			writer.close();
			fMR.doneSuccessfully++;

			MyUtils.println("Number of records written: " + Constants.integerFormatter.format(i), indentationLevel+2);
			MyUtils.println("Finished.", indentationLevel+1);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			System.out.println();
		}catch (Exception e){
			e.printStackTrace();
			fMR.errors++;
		}
		fMR.processed++;
		return fMR;
	}//public static void copyNeededFieldsIntoNewTSVFile(....
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void generateTSVFromJSONs(String inputPath, String outputPath, 
			FileManipulationResult fMRResult, int indentationLevel, String writeMessageStep){ 
		//Not yet: All were run successfully for 13 projects [+ 2 project families (13 + 6 more projects)]:
		MyUtils.println(writeMessageStep + "- Converting JSONs to TSV:", 0);
		MyUtils.println("Started ...", indentationLevel+1);
		FileManipulationResult fMR = new FileManipulationResult();
		FileManipulationResult totalFMR = new FileManipulationResult();

		MyUtils.createFolderIfDoesNotExist(outputPath, fMR, 1, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "1"));
		//bugs:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "bugs", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "2"));	
		totalFMR.add(fMR);


		//comments:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "comments", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "3"));	
		totalFMR.add(fMR);


		//commits:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "commits", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "4"));	
		totalFMR.add(fMR);


		//githubissues:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "githubissues", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "5"));	
		totalFMR.add(fMR);


		//githubprofiles:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "githubprofiles", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "6"));	
		totalFMR.add(fMR);


		//githubprofiles:
		fMR = copyNeededFieldsFromJSONIntoNewTSVFile(inputPath, outputPath, "projects", 
				true, 50000, indentationLevel+1, Constants.THIS_IS_REAL, MyUtils.concatTwoWriteMessageSteps(writeMessageStep, "7"));	
		totalFMR.add(fMR);

		
		fMRResult = totalFMR;
		MyUtils.println("--------------------------------------------------", 1);
		MyUtils.println("-------------------- SUMMARY: --------------------", 1);
		MyUtils.println("--------------------------------------------------", 1);
		MyUtils.println(" Total # of JSON files processed: " + totalFMR.processed, 1);
		MyUtils.println("# of files converted succesfully: " + totalFMR.doneSuccessfully, 1);
		if (totalFMR.errors > 0)
			MyUtils.println("# of files not converted (or converted wit errors): " + totalFMR.errors, 1);
		MyUtils.println("Finished.", 1);
		MyUtils.println("-----------------------------------", 0);
		MyUtils.println("-----------------------------------", 0);
		MyUtils.println("-----------------------------------", 0);
	}//generateTSVFromJSONs().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//This method merges the two files that have information of issues (bugs and PRs), normally githubissues.tsv and bugs.tsv. 
		//Then generates two separate file for bugs and PRs, normally bugs_complete.tsv and PRs_complete.tsv.
	public static FileManipulationResult merge_bugsTSV_and_githubissuesTSV(String inputPath, String bugsFileName, String githubissuesFileName, 
			String outputPath, String issuesOutputFileName, String prsOutputFileName, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);
			MyUtils.println(writeMessageStep + " Merging \"" + bugsFileName + ".tsv\" and \"" + githubissuesFileName + ".tsv\":", indentationLevel);
			MyUtils.println("Started ...", indentationLevel);

			TreeMap<String, String[]> githubissues = TSVManipulations.readUniqueKeyAndItsValueFromTSV(inputPath, githubissuesFileName+".tsv", null, 
					1, 6, "0$2$3$4$5", LogicalOperation.NO_CONDITION, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					wrapOutputInLines, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-1");

			MyUtils.println("-----------------------------------", indentationLevel+1);
			MyUtils.println(writeMessageStep + "-2-Readig \"" + bugsFileName + ".tsv\" and merging with \"" + bugsFileName + ".tsv\" (which is already in memory):" , indentationLevel+1);
			MyUtils.println("Started ...", indentationLevel+1);
			BufferedReader br = new BufferedReader(new FileReader(inputPath + "\\" + bugsFileName + ".tsv")); 
			FileWriter issuesWriter = new FileWriter(outputPath + "\\" + issuesOutputFileName + ".tsv");
			FileWriter pRsWriter = new FileWriter(outputPath + "\\" + prsOutputFileName + ".tsv");
			int neglectedRecords = 0;
			int i = 0;
			int bugRecordsWritten = 0;
			int pRRecordsWritten = 0;
			String s = br.readLine(); //the title line.
			issuesWriter.append("id" + Constants.TAB + "projectId" + Constants.TAB + "number" + Constants.TAB + "assignees" + Constants.TAB  
					+ "url" + Constants.TAB + "author" + Constants.TAB + "createdAt" + Constants.TAB + "labels" + Constants.TAB + "status" + Constants.TAB + "title" + Constants.TAB + "body" + "\n");
			pRsWriter.append("id" + Constants.TAB + "projectId" + Constants.TAB + "number" + Constants.TAB + "assignees" + Constants.TAB  
					+ "url" + Constants.TAB + "author" + Constants.TAB + "createdAt" + Constants.TAB + "labels" + Constants.TAB + "status" + Constants.TAB + "title" + Constants.TAB + "body" + "\n");
			while((s = br.readLine()) != null) {
				String[] bugFields = s.split(Constants.TAB);
				String bugId = bugFields[0];
				String line = "";
				if (githubissues.containsKey(bugId)){ //The key field is the field that is like "GH..."
					String[] githubissueFields = githubissues.get(bugId);//bugId is in the form of GH12345....
					//Adding fields from githubissues file:
					line = githubissueFields[0] + Constants.TAB //id
							+ githubissueFields[1] + Constants.TAB //projectId
							+ githubissueFields[3] + Constants.TAB //number
							+ githubissueFields[4]; //assignees
					//Adding fields from bugs file:
					for (int j=1; j<bugFields.length;j++)
						line = line + Constants.TAB + bugFields[j];
					if (githubissueFields[2].equals("true")){//isPR=true:
						pRsWriter.append(line+"\n");
						pRRecordsWritten++;
					}
					else{ //isPR=false:
						issuesWriter.append(line+"\n");
						bugRecordsWritten++;
					}
				}
				else
					neglectedRecords++;
				i++;

				if (i % showProgressInterval == 0)
					MyUtils.println(Integer.toString(i), indentationLevel+2);
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal) 
						break;
			}

			br.close();
			pRsWriter.flush();
			pRsWriter.close();
			issuesWriter.flush();
			issuesWriter.close();
			fMR.doneSuccessfully = 1;

			MyUtils.println("Number of records ignored (which are in \"" + bugsFileName + ".tsv\" but are nots in \"" + githubissuesFileName + ".tsv\":" , indentationLevel+2);
			MyUtils.println(Constants.integerFormatter.format(neglectedRecords), indentationLevel+3);
			MyUtils.println("Number of bugs matched and written: " + Constants.integerFormatter.format(bugRecordsWritten), indentationLevel+2);
			MyUtils.println("Number of PRs matched and written: " + Constants.integerFormatter.format(pRRecordsWritten), indentationLevel+2);
			MyUtils.println("Finished.", indentationLevel+1);
			MyUtils.println("-----------------------------------", indentationLevel+1);

			MyUtils.println("Finished.", indentationLevel);
			if (wrapOutputInLines)
				MyUtils.println("-----------------------------------", indentationLevel);

			if (fMR.errors > 0)
				MyUtils.println("Finished with " + fMR.errors + "errors.", 0);
			else
				MyUtils.println("Done with no errors.", indentationLevel);			System.out.println();
		}catch (Exception e){
			e.printStackTrace();
			fMR.doneSuccessfully = 0;
			fMR.errors++;
		}
		fMR.processed = 1;
		return fMR;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//This method parses JSON files, removes redundant characters and stores the needed fields in .tsv format. 
	//Later, we need to re-read this file and read info of fixers and store stats of fixers of each bug. Then we need to remove everything other than SO tags.
	public static void generateSecondaryTSVsAndCleanDataSet(){ 
		//Not yet: All were run successfully for 13 projects [+ 2 project families (13 + 6 more projects)]:
		MyUtils.println("Converting JSONs to TSV:", 0);
		MyUtils.println("Started ...", 0);
		FileManipulationResult fMR;
		FileManipulationResult totalFMR = new FileManipulationResult();

		//Merge the two files that have information of issues (bugs and PRs), and then generate two separate file for bugs and PRs:
		fMR = merge_bugsTSV_and_githubissuesTSV(DATASET_DIRECTORY_GH_TSV, "bugs", "githubissues", DATASET_DIRECTORY_GH_TSV, "bugs_complete", "PRs_Complete",
				true, 50000, 0, Constants.THIS_IS_REAL, "");	
		totalFMR.add(fMR);


		MyUtils.println("--------------------------------------------------", 0);
		MyUtils.println("-------------------- SUMMARY: --------------------", 0);
		MyUtils.println("--------------------------------------------------", 0);
		MyUtils.println(" Total # of tasks done: " + totalFMR.processed, 0);
		MyUtils.println("# of tasks done succesfully: " + totalFMR.doneSuccessfully, 0);
		if (totalFMR.errors > 0)
			MyUtils.println("# of tasks not done (or done wit errors): " + totalFMR.errors, 0);
		else
			MyUtils.println("All done with no errors.", 0);
		MyUtils.println("-----------------------------------", 0);
		MyUtils.println("-----------------------------------", 0);
		MyUtils.println("-----------------------------------", 0);
	}//createSecondaryTSVsAndCleanDataSet().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) throws IOException, ParseException {
//		generateTSVFromJSONs(DATASET_DIRECTORY_GH_JSON, DATASET_DIRECTORY_GH_TSV);

		String s = "a b c  d   e f";
		String[] ss = s.split(" ");
		System.out.println("<"+ss[0]+">");
		System.out.println("<"+ss[1]+">");
		System.out.println("<"+ss[2]+">");
		System.out.println("<"+ss[3]+">");
		System.out.println("<"+ss[4]+">");
		System.out.println("<"+ss[5]+">");
		System.out.println("<"+ss[6]+">");
		System.out.println("<"+ss[7]+">");
		System.out.println("<"+ss[8]+">");
//		generateSecondaryTSVsAndCleanDataSet();

		//		//TESTING-BEGIN:
		////		String s = "link:ftp://example.com/aa#&*()@asd/asd:asd~!@#$%^&*()_+=-`{}|\\l][:\";'<>?,./";
		////		String s = "written.\n\nWhat";
		////		String s = "@gkop @oscardelben The rails stable guides, much like code, are updated only with rails releases. And http://edgeguides.rubyonrails.org is where you can find the master guides as they are being written.\n\nWhat you find in http://guides.rubyonrails.org is the current stable version of the guide as available in Rails 3.2.3. Any updates to this will happen when the next stable version is released (3.2.4 or 4.0.0 whichever is earlier). \n\nIf the earlier version is 3.2.4 (most likely scenario), the changes that will get updated in the stable guide will be the ones done in 3-2-stable since 3.2.3 was released. The changes done in master will not be available in this case.\n\nOn the other hand, if the next stable version we release is 4.0.0 (unlikely that this will happen before 3.2.4), the changes done in master will be available in the stable guides at http://guides.rubyonrails.org.\n\n@oscardelben As far as I know, your's and Ryan's work on the getting started guide is targeted only at Rails 4. I'm not sure if we should make a complete rewrite of the guide in a patch release (3.2.4). Let's ask @fxn what he thinks.\n\nAnd for the issue that @gkop pointed out, this is already [fixed](https://github.com/rails/rails/commit/ccf80c2ec458586d3a7a534dcca5622ad6ff7ee3) in 3-2-stable and will definitely be available in 3.2.4. ";
		////		String s = "written.\n\nWhat \n\nIf case.\n\nOn changes http://guides.rubyonrails.org.\n\n@oscardelben thinks.\n\nAnd . ";
		//		String s = "I got the following, when i try to open the Sprint 13 version:\n\n-------------------------------------------------------------------------------\n\nProcess:         Brackets [391]\nPath:            /Applications/Brackets Sprint 13.app/Contents/MacOS/Brackets\nIdentifier:      io.brackets.appshell\nVersion:         ??? (1.0)\nCode Type:       X86 (Native)\nParent Process:  launchd [151]\n\nDate/Time:       2012-09-13 21:58:27.137 -0300\nOS Version:      Mac OS X 10.7.4 (11E53)\nReport Version:  9\n\nInterval Since Last Report:          251124 sec\nCrashes Since Last Report:           4\nPer-App Interval Since Last Report:  6 sec\nPer-App Crashes Since Last Report:   4\nAnonymous UUID:                      340CC19F-E715-47EF-B3A8-1AE9816E1B8F\n\nCrashed Thread:  0  Dispatch queue: com.apple.main-thread\n\nException Type:  EXC_BREAKPOINT (SIGTRAP)\nException Codes: 0x0000000000000002, 0x0000000000000000\n\nApplication Specific Information:\nobjc[391]: garbage collection is OFF\n\nThread 0 Crashed:: Dispatch queue: com.apple.main-thread\n0   libcef.dylib                  \u00090x004b8237 0x11c000 + 3785271\n1   libcef.dylib                  \u00090x004d0257 0x11c000 + 3883607\n2   libcef.dylib                  \u00090x009dfe72 0x11c000 + 9191026\n3   libcef.dylib                  \u00090x009df8f4 0x11c000 + 9189620\n4   libcef.dylib                  \u00090x0016211f 0x11c000 + 287007\n5   libcef.dylib                  \u00090x009ad0fe 0x11c000 + 8982782\n6   libcef.dylib                  \u00090x009accad 0x11c000 + 8981677\n7   libcef.dylib                  \u00090x0011db69 cef_initialize + 441\n8   io.brackets.appshell          \u00090x000fbefd 0xe6000 + 89853\n9   io.brackets.appshell          \u00090x000f97c8 0xe6000 + 79816\n10  io.brackets.appshell          \u00090x000e9bf5 0xe6000 + 15349\n\nThread 1:: Dispatch queue: com.apple.libdispatch-manager\n0   libsystem_kernel.dylib        \u00090x946b990a kevent + 10\n1   libdispatch.dylib             \u00090x96436e10 _dispatch_mgr_invoke + 969\n2   libdispatch.dylib             \u00090x9643585f _dispatch_mgr_thread + 53\n\nThread 2:\n0   libsystem_kernel.dylib        \u00090x946b902e __workq_kernreturn + 10\n1   libsystem_c.dylib             \u00090x976e0ccf _pthread_wqthread + 773\n2   libsystem_c.dylib             \u00090x976e26fe start_wqthread + 30\n\nThread 3:\n0   libsystem_kernel.dylib        \u00090x946b902e __workq_kernreturn + 10\n1   libsystem_c.dylib             \u00090x976e0ccf _pthread_wqthread + 773\n2   libsystem_c.dylib             \u00090x976e26fe start_wqthread + 30\n\nThread 0 crashed with X86 Thread State (32-bit):\n  eax: 0x00000000  ebx: 0xc00e39e8  ecx: 0xce8f1681  edx: 0x946b8f0e\n  edi: 0x004cfec1  esi: 0x00000000  ebp: 0xc00e3998  esp: 0xc00e3990\n   ss: 0x00000023  efl: 0x00000286  eip: 0x004b8237   cs: 0x0000001b\n   ds: 0x00000023   es: 0x00000023   fs: 0x00000000   gs: 0x0000000f\n  cr2: 0x97721888\nLogical CPU: 0\n\nBinary Images:\n   0xe6000 -   0x112ffb +io.brackets.appshell (??? - 1.0) \u003c772DF075-F7CC-38A6-9183-C88FF064FDDB\u003e /Applications/Brackets Sprint 13.app/Contents/MacOS/Brackets\n  0x11c000 -  0x2ca2fff +libcef.dylib (??? - ???) \u003cD9D1556B-5997-3001-A815-3F0C924DFB14\u003e /Applications/Brackets Sprint 13.app/Contents/Frameworks/Chromium Embedded Framework.framework/Libraries/libcef.dylib\n0x8fee5000 - 0x8ff17aa7  dyld (195.6 - ???) \u003c3A866A34-4CDD-35A4-B26E-F145B05F3644\u003e /usr/lib/dyld\n0x9002f000 - 0x90042ff8  com.apple.MultitouchSupport.framework (231.4 - 231.4) \u003c083F7787-4C3B-31DA-B5BB-1993D9A9723D\u003e /System/Library/PrivateFrameworks/MultitouchSupport.framework/Versions/A/MultitouchSupport\n0x90043000 - 0x90043ff0  com.apple.ApplicationServices (41 - 41) \u003cC48EF6B2-ABF9-35BD-A07A-A38EC0008294\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/ApplicationServices\n0x90080000 - 0x90090ff7  libCRFSuite.dylib (??? - ???) \u003c94E040D2-2769-359A-A21B-DB85FCB73BDC\u003e /usr/lib/libCRFSuite.dylib\n0x90091000 - 0x90095ffd  IOSurface (??? - ???) \u003cEDDBEE65-1EB8-33A7-9972-E361A3508234\u003e /System/Library/Frameworks/IOSurface.framework/Versions/A/IOSurface\n0x900e9000 - 0x903ebfff  com.apple.CoreServices.CarbonCore (960.24 - 960.24) \u003c9692D838-85A5-32C1-B7FB-7C141FFC2557\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/CarbonCore.framework/Versions/A/CarbonCore\n0x904aa000 - 0x904aafff  com.apple.Accelerate (1.7 - Accelerate 1.7) \u003c4192CE7A-BCE0-3D3C-AAF7-6F1B3C607386\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Accelerate\n0x904b0000 - 0x90612ffb  com.apple.QuartzCore (1.7 - 270.4) \u003c6BC84C60-1003-3008-ABE4-779EF7B4F524\u003e /System/Library/Frameworks/QuartzCore.framework/Versions/A/QuartzCore\n0x90613000 - 0x90613ffe  libkeymgr.dylib (23.0.0 - compatibility 1.0.0) \u003c7F0E8EE2-9E8F-366F-9988-E2F119DB9A82\u003e /usr/lib/system/libkeymgr.dylib\n0x90614000 - 0x9066dfff  com.apple.HIServices (1.21 - ???) \u003c5F4D3797-32E2-3709-85F4-4B56515A17D7\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/HIServices.framework/Versions/A/HIServices\n0x906b2000 - 0x9072dffb  com.apple.ApplicationServices.ATS (317.11.0 - ???) \u003c42238C8B-C93F-3369-A500-EC0F10EB2C80\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ATS.framework/Versions/A/ATS\n0x9072e000 - 0x907b5fff  com.apple.print.framework.PrintCore (7.1 - 366.3) \u003cEEC03CAB-7F79-3931-87FE-4DF0B767BF47\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/PrintCore.framework/Versions/A/PrintCore\n0x90808000 - 0x9086affb  com.apple.datadetectorscore (3.0 - 179.4) \u003c3A418498-C189-37A1-9B86-F0ECB33AD91C\u003e /System/Library/PrivateFrameworks/DataDetectorsCore.framework/Versions/A/DataDetectorsCore\n0x90944000 - 0x90a1aaab  libobjc.A.dylib (228.0.0 - compatibility 1.0.0) \u003c2E272DCA-38A0-3530-BBF4-47AE678D20D4\u003e /usr/lib/libobjc.A.dylib\n0x90a59000 - 0x90d1cfff  com.apple.security (7.0 - 55148.1) \u003c77754898-4FCD-3CA3-9339-F1058C852806\u003e /System/Library/Frameworks/Security.framework/Versions/A/Security\n0x90d1d000 - 0x90d25ff5  libcopyfile.dylib (85.1.0 - compatibility 1.0.0) \u003cBB0C7B49-600F-3551-A460-B7E36CA4C4A4\u003e /usr/lib/system/libcopyfile.dylib\n0x90d26000 - 0x90d27ffd  libCVMSPluginSupport.dylib (??? - ???) \u003c22B85645-AA98-372B-BB55-55DCCF0EC716\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libCVMSPluginSupport.dylib\n0x90d9f000 - 0x90de2ffd  libcommonCrypto.dylib (55010.0.0 - compatibility 1.0.0) \u003c6B35F203-5D72-335A-A4BC-CC89FEC0E14F\u003e /usr/lib/system/libcommonCrypto.dylib\n0x90ec4000 - 0x91016fff  com.apple.audio.toolbox.AudioToolbox (1.7.2 - 1.7.2) \u003cE369AC9E-F548-3DF6-B320-9D09E486070E\u003e /System/Library/Frameworks/AudioToolbox.framework/Versions/A/AudioToolbox\n0x91291000 - 0x91299ff3  libunwind.dylib (30.0.0 - compatibility 1.0.0) \u003cE8DA8CEC-12D6-3C8D-B2E2-5D567C8F3CB5\u003e /usr/lib/system/libunwind.dylib\n0x9129a000 - 0x912c8ff7  com.apple.DictionaryServices (1.2.1 - 158.2) \u003cDA16A8B2-F359-345A-BAF7-8E6A5A0741A1\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/DictionaryServices.framework/Versions/A/DictionaryServices\n0x912dc000 - 0x913fafec  com.apple.vImage (5.1 - 5.1) \u003c7757F253-B281-3612-89D4-F2B04061CBE1\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vImage.framework/Versions/A/vImage\n0x913fb000 - 0x913feff7  libmathCommon.A.dylib (2026.0.0 - compatibility 1.0.0) \u003c69357047-7BE0-3360-A36D-000F55E39336\u003e /usr/lib/system/libmathCommon.A.dylib\n0x9147c000 - 0x9155fff7  libcrypto.0.9.8.dylib (44.0.0 - compatibility 0.9.8) \u003cBD913D3B-388D-33AE-AA5E-4810C743C28F\u003e /usr/lib/libcrypto.0.9.8.dylib\n0x91560000 - 0x915c2ff3  libstdc++.6.dylib (52.0.0 - compatibility 7.0.0) \u003c266CE9B3-526A-3C41-BA58-7AE66A3B15FD\u003e /usr/lib/libstdc++.6.dylib\n0x91608000 - 0x91610ff3  liblaunch.dylib (392.38.0 - compatibility 1.0.0) \u003cD7F6E875-263A-37B5-B403-53F76710538C\u003e /usr/lib/system/liblaunch.dylib\n0x91611000 - 0x9166cff3  com.apple.Symbolication (1.3 - 91) \u003c4D12D2EC-5010-3958-A205-9A67E972C76A\u003e /System/Library/PrivateFrameworks/Symbolication.framework/Versions/A/Symbolication\n0x9166d000 - 0x9166dfff  com.apple.vecLib (3.7 - vecLib 3.7) \u003c8CCF99BF-A4B7-3C01-9219-B83D2AE5F82A\u003e /System/Library/Frameworks/vecLib.framework/Versions/A/vecLib\n0x916e9000 - 0x917e1ff7  libFontParser.dylib (??? - ???) \u003c1A0DA421-62B2-3AA7-9F62-0E01C1887D09\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ATS.framework/Versions/A/Resources/libFontParser.dylib\n0x917e2000 - 0x918f1fff  com.apple.DesktopServices (1.6.3 - 1.6.3) \u003c18CAAA9E-7065-3FF7-ACFE-CDB60E5426A2\u003e /System/Library/PrivateFrameworks/DesktopServicesPriv.framework/Versions/A/DesktopServicesPriv\n0x918f2000 - 0x918f7ff7  libmacho.dylib (800.0.0 - compatibility 1.0.0) \u003c943213F3-CC9B-328E-8A6F-16D85C4274C7\u003e /usr/lib/system/libmacho.dylib\n0x918f8000 - 0x91a24ff9  com.apple.CFNetwork (520.4.3 - 520.4.3) \u003cE9E315D4-CE22-3715-BED2-BB95AD5E10E8\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/CFNetwork.framework/Versions/A/CFNetwork\n0x91a39000 - 0x91b49fe7  libsqlite3.dylib (9.6.0 - compatibility 9.0.0) \u003c34E1E3CC-7B6A-3B37-8D07-1258D11E16CB\u003e /usr/lib/libsqlite3.dylib\n0x91b4a000 - 0x91b5bfff  libbsm.0.dylib (??? - ???) \u003c54ACF696-87C6-3652-808A-17BE7275C230\u003e /usr/lib/libbsm.0.dylib\n0x91b5e000 - 0x91bbcfff  com.apple.coreui (1.2.2 - 165.10) \u003cC6B099D6-7F02-3971-99B9-E415308959CF\u003e /System/Library/PrivateFrameworks/CoreUI.framework/Versions/A/CoreUI\n0x91bc1000 - 0x91bc8ff7  libsystem_notify.dylib (80.1.0 - compatibility 1.0.0) \u003c47DB9E1B-A7D1-3818-A747-382B2C5D9E1B\u003e /usr/lib/system/libsystem_notify.dylib\n0x91bc9000 - 0x91c65fef  com.apple.ink.framework (1.4 - 110) \u003c1A3E2916-60C1-3AC1-86BF-202F6567B228\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/Ink.framework/Versions/A/Ink\n0x91c66000 - 0x91c6fff3  com.apple.CommonAuth (2.2 - 2.0) \u003cC3FD6EC2-8EB3-38FB-BBB7-05009CA49024\u003e /System/Library/PrivateFrameworks/CommonAuth.framework/Versions/A/CommonAuth\n0x92703000 - 0x92707ff7  com.apple.OpenDirectory (10.7 - 146) \u003c4986A382-8FEF-3392-8CE9-CF6A5EE4E365\u003e /System/Library/Frameworks/OpenDirectory.framework/Versions/A/OpenDirectory\n0x92750000 - 0x9275bfff  libkxld.dylib (??? - ???) \u003cD8ED88D0-7153-3514-9927-AF15A12261A5\u003e /usr/lib/system/libkxld.dylib\n0x9275c000 - 0x9286dff7  libJP2.dylib (??? - ???) \u003c845C74F4-1074-3983-945F-EB669538CAA9\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libJP2.dylib\n0x9286e000 - 0x92956fff  libxml2.2.dylib (10.3.0 - compatibility 10.0.0) \u003c1841196F-68B5-309F-8ED1-6714B1DFEC83\u003e /usr/lib/libxml2.2.dylib\n0x92957000 - 0x92958fff  libsystem_blocks.dylib (53.0.0 - compatibility 1.0.0) \u003cB04592B1-0924-3422-82FF-976B339DF567\u003e /usr/lib/system/libsystem_blocks.dylib\n0x92d14000 - 0x92ec8ff3  libicucore.A.dylib (46.1.0 - compatibility 1.0.0) \u003c6AD14A51-AEA8-3732-B07B-DEA37577E13A\u003e /usr/lib/libicucore.A.dylib\n0x92fe1000 - 0x92fe3ff7  libdyld.dylib (195.6.0 - compatibility 1.0.0) \u003c1F865C73-5803-3B08-988C-65B8D86CB7BE\u003e /usr/lib/system/libdyld.dylib\n0x930c8000 - 0x930f2ff1  com.apple.CoreServicesInternal (113.17 - 113.17) \u003c41979516-2F26-3707-A6CA-7A95A1B0D963\u003e /System/Library/PrivateFrameworks/CoreServicesInternal.framework/Versions/A/CoreServicesInternal\n0x930f3000 - 0x93145ffb  com.apple.CoreMediaIO (212.0 - 3199.1.1) \u003cBBC14F4C-2748-3583-85E3-EF3A1F249370\u003e /System/Library/Frameworks/CoreMediaIO.framework/Versions/A/CoreMediaIO\n0x931cf000 - 0x9384afe5  com.apple.CoreAUC (6.16.11 - 6.16.11) \u003cE52E2D54-138B-3F44-AA2C-309FB876DF6A\u003e /System/Library/PrivateFrameworks/CoreAUC.framework/Versions/A/CoreAUC\n0x9384b000 - 0x93870ff9  libJPEG.dylib (??? - ???) \u003c743578F6-8C0C-39CC-9F15-3A01E1616EAE\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libJPEG.dylib\n0x93871000 - 0x93879fff  com.apple.DiskArbitration (2.4.1 - 2.4.1) \u003c28D5D8B5-14E8-3DA1-9085-B9BC96835ACF\u003e /System/Library/Frameworks/DiskArbitration.framework/Versions/A/DiskArbitration\n0x9387a000 - 0x9387bff7  libsystem_sandbox.dylib (??? - ???) \u003cEBC6ED6B-7D94-32A9-A718-BB9EDA1732C9\u003e /usr/lib/system/libsystem_sandbox.dylib\n0x9387c000 - 0x9387fffd  libCoreVMClient.dylib (??? - ???) \u003c361CCFAF-8565-383F-915F-0B059C793E42\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libCoreVMClient.dylib\n0x93880000 - 0x93880fff  com.apple.audio.units.AudioUnit (1.7.2 - 1.7.2) \u003c2E71E880-25D1-3210-8D26-21EC47ED810C\u003e /System/Library/Frameworks/AudioUnit.framework/Versions/A/AudioUnit\n0x93881000 - 0x938deffb  com.apple.htmlrendering (76 - 1.1.4) \u003c409EF0CB-2997-369A-9326-BE12436B9EE1\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/HTMLRendering.framework/Versions/A/HTMLRendering\n0x938ee000 - 0x94383ff6  com.apple.AppKit (6.7.3 - 1138.47) \u003cD8CD06D7-F18C-39BE-BC68-B343F87F0469\u003e /System/Library/Frameworks/AppKit.framework/Versions/C/AppKit\n0x9438a000 - 0x943b9ff7  libsystem_info.dylib (??? - ???) \u003c37640811-445B-3BB7-9934-A7C99848250D\u003e /usr/lib/system/libsystem_info.dylib\n0x943ba000 - 0x943d4fff  com.apple.Kerberos (1.0 - 1) \u003cD7920A1C-FEC4-3460-8DD0-D02491578CBB\u003e /System/Library/Frameworks/Kerberos.framework/Versions/A/Kerberos\n0x943d5000 - 0x943d5fff  com.apple.Accelerate.vecLib (3.7 - vecLib 3.7) \u003c22997C20-BEB7-301D-86C5-5BFB3B06D212\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vecLib.framework/Versions/A/vecLib\n0x9450d000 - 0x945cdffb  com.apple.ColorSync (4.7.4 - 4.7.4) \u003c0A68AF35-15DF-3A0A-9B17-70CE2A106A6C\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ColorSync.framework/Versions/A/ColorSync\n0x945ce000 - 0x945d4ffb  com.apple.print.framework.Print (7.4 - 247.3) \u003cCB075EEE-FA1F-345C-A1B5-1AB266FC73A1\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/Print.framework/Versions/A/Print\n0x945d5000 - 0x94624ffb  com.apple.AppleVAFramework (5.0.14 - 5.0.14) \u003c7FF10781-5418-37BB-A6B3-1606DA82CBFF\u003e /System/Library/PrivateFrameworks/AppleVA.framework/Versions/A/AppleVA\n0x94625000 - 0x9464dff7  libxslt.1.dylib (3.24.0 - compatibility 3.0.0) \u003cAB530FB2-8BD1-3328-95E8-CF449F0429CA\u003e /usr/lib/libxslt.1.dylib\n0x9469b000 - 0x9469fff3  libsystem_network.dylib (??? - ???) \u003c62EBADDA-FC72-3275-AAB3-5EDD949FEFAF\u003e /usr/lib/system/libsystem_network.dylib\n0x946a0000 - 0x946beff7  libsystem_kernel.dylib (1699.26.8 - compatibility 1.0.0) \u003c3705DE40-E00F-3E37-ADB0-D4AE5F9950F5\u003e /usr/lib/system/libsystem_kernel.dylib\n0x946bf000 - 0x946c5ffd  com.apple.CommerceCore (1.0 - 17) \u003cE59CD307-58E2-35FD-9131-B38978799910\u003e /System/Library/PrivateFrameworks/CommerceKit.framework/Versions/A/Frameworks/CommerceCore.framework/Versions/A/CommerceCore\n0x94769000 - 0x947cdfff  com.apple.framework.IOKit (2.0 - ???) \u003c88D60E59-430D-35B8-B1E9-F5138301AEF9\u003e /System/Library/Frameworks/IOKit.framework/Versions/A/IOKit\n0x947ce000 - 0x947e4ffe  libxpc.dylib (77.19.0 - compatibility 1.0.0) \u003c0585AA94-F4FD-32C1-B586-22E7184B781A\u003e /usr/lib/system/libxpc.dylib\n0x94818000 - 0x9481fff9  libsystem_dnssd.dylib (??? - ???) \u003cD3A766FC-C409-3A57-ADE4-94B7688E1C7E\u003e /usr/lib/system/libsystem_dnssd.dylib\n0x94820000 - 0x94842ffe  com.apple.framework.familycontrols (3.0 - 300) \u003c5BCCDDC2-AFAC-3290-AEEF-23B2664CA11F\u003e /System/Library/PrivateFrameworks/FamilyControls.framework/Versions/A/FamilyControls\n0x94b3e000 - 0x94b69fff  com.apple.GSS (2.2 - 2.0) \u003c2C468B23-FA87-30B5-B9A6-8C5D1373AA30\u003e /System/Library/Frameworks/GSS.framework/Versions/A/GSS\n0x94b6a000 - 0x95046ff6  libBLAS.dylib (??? - ???) \u003c134ABFC6-F29E-3DC5-8E57-E13CB6EF7B41\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vecLib.framework/Versions/A/libBLAS.dylib\n0x953f7000 - 0x95433ffa  libGLImage.dylib (??? - ???) \u003c504E7865-571E-38B4-A84A-D7B513AC84F5\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGLImage.dylib\n0x95774000 - 0x95776ffb  libRadiance.dylib (??? - ???) \u003c4721057E-5A1F-3083-911B-200ED1CE7678\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libRadiance.dylib\n0x95777000 - 0x960a159b  com.apple.CoreGraphics (1.600.0 - ???) \u003c62026E0C-E30F-3FF0-B0F6-6A2D270B20BF\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/CoreGraphics.framework/Versions/A/CoreGraphics\n0x96355000 - 0x96372fff  libresolv.9.dylib (46.1.0 - compatibility 1.0.0) \u003c2870320A-28DA-3B44-9D82-D56E0036F6BB\u003e /usr/lib/libresolv.9.dylib\n0x96373000 - 0x963c3ff8  libTIFF.dylib (??? - ???) \u003c4DC2025D-15E7-35CA-B7C5-9F73B26C8B53\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libTIFF.dylib\n0x963c4000 - 0x9640cff7  com.apple.SystemConfiguration (1.11.3 - 1.11) \u003c68B92FEA-F754-3E7E-B5E6-D512E26144E7\u003e /System/Library/Frameworks/SystemConfiguration.framework/Versions/A/SystemConfiguration\n0x96434000 - 0x96442fff  libdispatch.dylib (187.9.0 - compatibility 1.0.0) \u003c2F918480-12C8-3F22-9B1A-9B2D76F6F4F5\u003e /usr/lib/system/libdispatch.dylib\n0x9644b000 - 0x9644effc  libpam.2.dylib (3.0.0 - compatibility 3.0.0) \u003c6FFDBD60-5EC6-3EFA-996B-EE030443C16C\u003e /usr/lib/libpam.2.dylib\n0x964db000 - 0x96821ff3  com.apple.HIToolbox (1.9 - ???) \u003c409E6397-0DCB-3431-9CCC-368317C62545\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/HIToolbox.framework/Versions/A/HIToolbox\n0x96822000 - 0x968b8ff7  com.apple.LaunchServices (480.33 - 480.33) \u003c5A4BF529-391E-3987-940E-287ACE56078A\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/LaunchServices\n0x968bc000 - 0x96930fff  com.apple.CoreSymbolication (2.2 - 73.2) \u003cFA9305CA-FB9B-3646-8C41-FF8DF15AB2C1\u003e /System/Library/PrivateFrameworks/CoreSymbolication.framework/Versions/A/CoreSymbolication\n0x96937000 - 0x96939ff9  com.apple.securityhi (4.0 - 1) \u003cACEEED5F-8D58-377D-B2B8-E4A7F4E5E286\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/SecurityHI.framework/Versions/A/SecurityHI\n0x96954000 - 0x9698bfef  com.apple.DebugSymbols (2.1 - 87) \u003cEB951B78-31A5-379F-AFA1-B5C9A7BB3D23\u003e /System/Library/PrivateFrameworks/DebugSymbols.framework/Versions/A/DebugSymbols\n0x9699b000 - 0x96a28fe7  libvMisc.dylib (325.4.0 - compatibility 1.0.0) \u003cF2A8BBA3-6431-3CED-8CD3-0953410B6F96\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vecLib.framework/Versions/A/libvMisc.dylib\n0x96d9e000 - 0x96d9efff  libdnsinfo.dylib (395.11.0 - compatibility 1.0.0) \u003c7EFAD88C-AFBC-3D48-BE14-60B8EACC68D7\u003e /usr/lib/system/libdnsinfo.dylib\n0x96d9f000 - 0x96da8fff  libc++abi.dylib (14.0.0 - compatibility 1.0.0) \u003cFEB5330E-AD5D-37A0-8AB2-0820F311A2C8\u003e /usr/lib/libc++abi.dylib\n0x96da9000 - 0x96dc6ff3  com.apple.openscripting (1.3.3 - ???) \u003c33713C0B-B7D5-37AA-87DB-2727FDCC8007\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/OpenScripting.framework/Versions/A/OpenScripting\n0x96e37000 - 0x96e38ff7  libquarantine.dylib (36.6.0 - compatibility 1.0.0) \u003c600909D9-BD75-386E-8D3E-7CBD29079DF3\u003e /usr/lib/system/libquarantine.dylib\n0x96e4f000 - 0x96ee6ff3  com.apple.securityfoundation (5.0 - 55116) \u003cEB53CEF7-4836-39FD-B012-6BC122ED4CE9\u003e /System/Library/Frameworks/SecurityFoundation.framework/Versions/A/SecurityFoundation\n0x96ee7000 - 0x972dbffb  com.apple.VideoToolbox (1.0 - 705.78) \u003cBE955448-F79F-3136-A4AF-6EDBAFEDD9C2\u003e /System/Library/PrivateFrameworks/VideoToolbox.framework/Versions/A/VideoToolbox\n0x972dc000 - 0x972e1ffd  libGFXShared.dylib (??? - ???) \u003c1CA9B41B-2C61-38F8-ABAC-1D5511478F5C\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGFXShared.dylib\n0x9761e000 - 0x97622fff  com.apple.CommonPanels (1.2.5 - 94) \u003cEA47550D-7DAF-30D9-91DB-1FB594CC8522\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/CommonPanels.framework/Versions/A/CommonPanels\n0x97665000 - 0x97681ffc  libPng.dylib (??? - ???) \u003c75F41C08-E187-354C-8115-79387F57FC2C\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libPng.dylib\n0x97682000 - 0x9774dfff  libsystem_c.dylib (763.13.0 - compatibility 1.0.0) \u003c52421B00-79C8-3727-94DE-62F6820B9C31\u003e /usr/lib/system/libsystem_c.dylib\n0x9774e000 - 0x9774efff  com.apple.Carbon (153 - 153) \u003c63603A0C-723B-3968-B302-EBEEE6A14E97\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Carbon\n0x977d8000 - 0x977d9fff  liblangid.dylib (??? - ???) \u003cC8C204E9-1785-3785-BBD7-22D59493B98B\u003e /usr/lib/liblangid.dylib\n0x977da000 - 0x977deffa  libcache.dylib (47.0.0 - compatibility 1.0.0) \u003c56256537-6538-3522-BCB6-2C79DA6AC8CD\u003e /usr/lib/system/libcache.dylib\n0x977df000 - 0x977e3fff  libGIF.dylib (??? - ???) \u003cA6F1ACAE-7B9B-3B3F-A54A-ED4004EA1D85\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/Resources/libGIF.dylib\n0x977ed000 - 0x977f0ffb  com.apple.help (1.3.2 - 42) \u003cB1E6701C-7473-30B2-AB5A-AFC9A4823694\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/Help.framework/Versions/A/Help\n0x977f1000 - 0x97860fff  com.apple.Heimdal (2.2 - 2.0) \u003c2E1B8779-36D4-3C62-A67E-0034D77D7707\u003e /System/Library/PrivateFrameworks/Heimdal.framework/Versions/A/Heimdal\n0x97c5c000 - 0x97c99ff7  libcups.2.dylib (2.9.0 - compatibility 2.0.0) \u003c1C757924-4E54-3522-A885-99795EA10228\u003e /usr/lib/libcups.2.dylib\n0x97db8000 - 0x97e45ff7  com.apple.CoreText (220.20.0 - ???) \u003c0C3EDD4F-6112-353A-8A3A-8D630182C22A\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/CoreText.framework/Versions/A/CoreText\n0x97e46000 - 0x97fa8fff  com.apple.QTKit (7.7.1 - 2330) \u003cDD58823D-D3E7-31CB-9DF9-ACB981F5A744\u003e /System/Library/Frameworks/QTKit.framework/Versions/A/QTKit\n0x9864a000 - 0x98654ff2  com.apple.audio.SoundManager (3.9.4.1 - 3.9.4.1) \u003c2A089CE8-9760-3F0F-B77D-29A78940EA17\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/CarbonSound.framework/Versions/A/CarbonSound\n0x98bfa000 - 0x98bfbff4  libremovefile.dylib (21.1.0 - compatibility 1.0.0) \u003c6DE3FDC7-0BE0-3791-B6F5-C15422A8AFB8\u003e /usr/lib/system/libremovefile.dylib\n0x98bfc000 - 0x98c4dff9  com.apple.ScalableUserInterface (1.0 - 1) \u003c3C39DF4D-5CAE-373A-BE08-8CD16E514337\u003e /System/Library/Frameworks/QuartzCore.framework/Versions/A/Frameworks/ScalableUserInterface.framework/Versions/A/ScalableUserInterface\n0x98c4e000 - 0x98c55ffd  com.apple.NetFS (4.0 - 4.0) \u003cAE731CFE-1B2E-3E46-8759-843F5FB8C24F\u003e /System/Library/Frameworks/NetFS.framework/Versions/A/NetFS\n0x98c56000 - 0x98f60ff3  com.apple.Foundation (6.7.2 - 833.25) \u003c4C52ED74-A1FD-3087-A2E1-035AB3CF9610\u003e /System/Library/Frameworks/Foundation.framework/Versions/C/Foundation\n0x98fb3000 - 0x99226ffb  com.apple.CoreImage (7.98 - 1.0.1) \u003cEDC91BA1-673D-3B47-BFD5-BBF11C36EE6A\u003e /System/Library/Frameworks/QuartzCore.framework/Versions/A/Frameworks/CoreImage.framework/Versions/A/CoreImage\n0x99268000 - 0x9927dfff  com.apple.speech.synthesis.framework (4.0.74 - 4.0.74) \u003c92AADDB0-BADF-3B00-8941-B8390EDC931B\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/SpeechSynthesis.framework/Versions/A/SpeechSynthesis\n0x9927e000 - 0x9934effb  com.apple.ImageIO.framework (3.1.2 - 3.1.2) \u003c94798A2B-4C7A-30EA-9920-283451BDB9FA\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ImageIO.framework/Versions/A/ImageIO\n0x9934f000 - 0x9935dff7  libxar-nossl.dylib (??? - ???) \u003c5BF4DA8E-C319-354A-967E-A0C725DC8BA3\u003e /usr/lib/libxar-nossl.dylib\n0x9935e000 - 0x995e3fe3  com.apple.QuickTime (7.7.1 - 2330) \u003c060B6A47-FC15-3D38-8EFB-FCF38680510B\u003e /System/Library/Frameworks/QuickTime.framework/Versions/A/QuickTime\n0x995e4000 - 0x99606ff8  com.apple.PerformanceAnalysis (1.11 - 11) \u003c453463FF-7C42-3526-8C96-A9971EE07154\u003e /System/Library/PrivateFrameworks/PerformanceAnalysis.framework/Versions/A/PerformanceAnalysis\n0x99607000 - 0x997defe7  com.apple.CoreFoundation (6.7.2 - 635.21) \u003c4D1D2BAF-1332-32DF-A81B-7E79D4F0A6CB\u003e /System/Library/Frameworks/CoreFoundation.framework/Versions/A/CoreFoundation\n0x997df000 - 0x997e0fff  com.apple.TrustEvaluationAgent (2.0 - 1) \u003c4BB39578-2F5E-3A50-AD59-9C0AB99472EB\u003e /System/Library/PrivateFrameworks/TrustEvaluationAgent.framework/Versions/A/TrustEvaluationAgent\n0x99fdd000 - 0x99fddff2  com.apple.CoreServices (53 - 53) \u003c7CB7AA95-D5A7-366A-BB8A-035AA9E582F8\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/CoreServices\n0x9a01d000 - 0x9a492ff7  FaceCoreLight (1.4.7 - compatibility 1.0.0) \u003c3E2BF587-5168-3FC5-9D8D-183A9C7C1DED\u003e /System/Library/PrivateFrameworks/FaceCoreLight.framework/Versions/A/FaceCoreLight\n0x9a493000 - 0x9a509fff  com.apple.Metadata (10.7.0 - 627.32) \u003c650EE880-1488-3DC6-963B-F3D6E043FFDC\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/Metadata.framework/Versions/A/Metadata\n0x9a50a000 - 0x9a56fff7  libvDSP.dylib (325.4.0 - compatibility 1.0.0) \u003c4B4B32D2-4F66-3B0D-BD61-FA8429FF8507\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vecLib.framework/Versions/A/libvDSP.dylib\n0x9a5b3000 - 0x9a63dffb  com.apple.SearchKit (1.4.0 - 1.4.0) \u003cCF074082-64AB-3A1F-831E-582DF1667827\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/SearchKit.framework/Versions/A/SearchKit\n0x9a6c2000 - 0x9a6c5ff7  libcompiler_rt.dylib (6.0.0 - compatibility 1.0.0) \u003c7F6C14CC-0169-3F1B-B89C-372F67F1F3B5\u003e /usr/lib/system/libcompiler_rt.dylib\n0x9a6f8000 - 0x9a705fff  libGL.dylib (??? - ???) \u003c30E6DED6-0213-3A3B-B2B3-310E33301CCB\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGL.dylib\n0x9a706000 - 0x9a71dff8  com.apple.CoreMediaAuthoring (2.0 - 891) \u003c69D569FD-670C-3BD0-94BF-7A8954AA2953\u003e /System/Library/PrivateFrameworks/CoreMediaAuthoring.framework/Versions/A/CoreMediaAuthoring\n0x9a71e000 - 0x9aa64fff  com.apple.MediaToolbox (1.0 - 705.78) \u003cE6990E4A-B562-3051-86A6-B39E040BF766\u003e /System/Library/PrivateFrameworks/MediaToolbox.framework/Versions/A/MediaToolbox\n0x9aa65000 - 0x9aa9bff7  com.apple.AE (527.7 - 527.7) \u003c7BAFBF18-3997-3656-9823-FD3B455056A4\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/AE.framework/Versions/A/AE\n0x9aac8000 - 0x9aad3ffe  libbz2.1.0.dylib (1.0.5 - compatibility 1.0.0) \u003cB63F5D07-93B3-3F02-BFB7-472B4ED3521F\u003e /usr/lib/libbz2.1.0.dylib\n0x9aad4000 - 0x9ab35ffb  com.apple.audio.CoreAudio (4.0.2 - 4.0.2) \u003cE617857C-D870-3E2D-BA13-3732DD1BC15E\u003e /System/Library/Frameworks/CoreAudio.framework/Versions/A/CoreAudio\n0x9ab73000 - 0x9ab74fff  libDiagnosticMessagesClient.dylib (??? - ???) \u003cDB3889C2-2FC2-3087-A2A2-4C319455E35C\u003e /usr/lib/libDiagnosticMessagesClient.dylib\n0x9ab75000 - 0x9abb7ff7  com.apple.CoreMedia (1.0 - 705.78) \u003cD88AC852-8844-3B73-81C8-DF605F00AB40\u003e /System/Library/Frameworks/CoreMedia.framework/Versions/A/CoreMedia\n0x9ad95000 - 0x9ad96ff0  libunc.dylib (24.0.0 - compatibility 1.0.0) \u003c2F4B35B2-706C-3383-AA86-DABA409FAE45\u003e /usr/lib/system/libunc.dylib\n0x9ad97000 - 0x9adabfff  com.apple.CFOpenDirectory (10.7 - 146) \u003c9149C1FE-865E-3A8D-B9D9-547384F553C8\u003e /System/Library/Frameworks/OpenDirectory.framework/Versions/A/Frameworks/CFOpenDirectory.framework/Versions/A/CFOpenDirectory\n0x9adac000 - 0x9adfeff7  libFontRegistry.dylib (??? - ???) \u003c96E9602C-DFD3-3021-8090-60228CC80D26\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/ATS.framework/Versions/A/Resources/libFontRegistry.dylib\n0x9adff000 - 0x9ae3fff7  com.apple.NavigationServices (3.7 - 193) \u003c16A8BCC8-7343-3A90-88B3-AAA334DF615F\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/NavigationServices.framework/Versions/A/NavigationServices\n0x9ae40000 - 0x9ae4bffb  com.apple.speech.recognition.framework (4.0.21 - 4.0.21) \u003cA1764D2F-EB84-33DC-9ED5-CDA3B468FF3E\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/SpeechRecognition.framework/Versions/A/SpeechRecognition\n0x9b100000 - 0x9b2f8ff7  com.apple.CoreData (104.1 - 358.14) \u003cC1730963-F75D-3338-B65F-D50235538B28\u003e /System/Library/Frameworks/CoreData.framework/Versions/A/CoreData\n0x9b473000 - 0x9b48fff5  com.apple.GenerationalStorage (1.0 - 126.1) \u003cE622F823-7D98-3D13-9C3D-7EA482567394\u003e /System/Library/PrivateFrameworks/GenerationalStorage.framework/Versions/A/GenerationalStorage\n0x9b490000 - 0x9b534fff  com.apple.QD (3.40 - ???) \u003c3881BEC6-0908-3073-BA44-346356E1CDF9\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/QD.framework/Versions/A/QD\n0x9b638000 - 0x9b678ff7  libauto.dylib (??? - ???) \u003c984C81BE-FA1C-3228-8F7E-2965E7E5EB85\u003e /usr/lib/libauto.dylib\n0x9b679000 - 0x9b73cfff  com.apple.CoreServices.OSServices (478.46 - 478.46) \u003cF2063FC8-2BE1-3B97-98AF-8796B0D4BE58\u003e /System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/OSServices.framework/Versions/A/OSServices\n0x9b73d000 - 0x9bb3fff6  libLAPACK.dylib (??? - ???) \u003c00BE0221-8564-3F87-9F6B-8A910CF2F141\u003e /System/Library/Frameworks/Accelerate.framework/Versions/A/Frameworks/vecLib.framework/Versions/A/libLAPACK.dylib\n0x9bb40000 - 0x9bb4efff  libz.1.dylib (1.2.5 - compatibility 1.0.0) \u003cE73A4025-835C-3F73-9853-B08606E892DB\u003e /usr/lib/libz.1.dylib\n0x9bb4f000 - 0x9bb78ffe  com.apple.opencl (1.50.69 - 1.50.69) \u003c2601993F-F3B3-3737-91AE-4A5795C52CD5\u003e /System/Library/Frameworks/OpenCL.framework/Versions/A/OpenCL\n0x9bb79000 - 0x9bbc2ff7  libGLU.dylib (??? - ???) \u003c5EE0B644-FAD6-3E3C-A380-9B0CDA0B6432\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/Libraries/libGLU.dylib\n0x9bbc3000 - 0x9bc2bff7  libc++.1.dylib (28.1.0 - compatibility 1.0.0) \u003cFE3304C5-C000-3DA0-9E53-0E4CA074B73B\u003e /usr/lib/libc++.1.dylib\n0x9bc2c000 - 0x9bc5afe7  libSystem.B.dylib (159.1.0 - compatibility 1.0.0) \u003c30189C33-6ADD-3142-83F3-6114B1FC152E\u003e /usr/lib/libSystem.B.dylib\n0x9bc5b000 - 0x9bc7efff  com.apple.CoreVideo (1.7 - 70.3) \u003c4234C11C-E8E9-309A-9465-27D6D7458895\u003e /System/Library/Frameworks/CoreVideo.framework/Versions/A/CoreVideo\n0x9ccf7000 - 0x9cd05fff  com.apple.opengl (1.7.7 - 1.7.7) \u003c2D362F15-5EA6-37B6-9BCB-58F2C599ACDA\u003e /System/Library/Frameworks/OpenGL.framework/Versions/A/OpenGL\n0x9cd10000 - 0x9cd1bffe  com.apple.NetAuth (3.2 - 3.2) \u003c4377FB18-A550-35C6-BCD2-71C42134EEA6\u003e /System/Library/PrivateFrameworks/NetAuth.framework/Versions/A/NetAuth\n0x9cd1c000 - 0x9cd31ff7  com.apple.ImageCapture (7.0.1 - 7.0.1) \u003c1C8933A9-C7C6-36E9-9D8B-0EF08ACA3315\u003e /System/Library/Frameworks/Carbon.framework/Versions/A/Frameworks/ImageCapture.framework/Versions/A/ImageCapture\n0x9cdab000 - 0x9cdcbff7  com.apple.RemoteViewServices (1.4 - 44.1) \u003c1F831750-1E77-3013-B1A6-0DF528623790\u003e /System/Library/PrivateFrameworks/RemoteViewServices.framework/Versions/A/RemoteViewServices\n0x9ce72000 - 0x9cf62ff1  libiconv.2.dylib (7.0.0 - compatibility 7.0.0) \u003c9E5F86A3-8405-3774-9E0C-3A074273C96D\u003e /usr/lib/libiconv.2.dylib\n0x9cf63000 - 0x9cf73fff  com.apple.LangAnalysis (1.7.0 - 1.7.0) \u003c6D6F0C9D-2EEA-3578-AF3D-E2A09BCECAF3\u003e /System/Library/Frameworks/ApplicationServices.framework/Versions/A/Frameworks/LangAnalysis.framework/Versions/A/LangAnalysis\n\nExternal Modification Summary:\n  Calls made by other processes targeting this process:\n    task_for_pid: 3\n    thread_create: 0\n    thread_set_state: 0\n  Calls made by this process:\n    task_for_pid: 0\n    thread_create: 0\n    thread_set_state: 0\n  Calls made by all processes on this machine:\n    task_for_pid: 756\n    thread_create: 0\n    thread_set_state: 0\n\nVM Region Summary:\nReadOnly portion of Libraries: Total=193.8M resident=95.8M(49%) swapped_out_or_unallocated=98.0M(51%)\nWritable regions: Total=51.4M written=972K(2%) resident=2692K(5%) swapped_out=0K(0%) unallocated=48.8M(95%)\n \nREGION TYPE                      VIRTUAL\n===========                      =======\nCG shared images                    128K\nCoreServices                       1752K\nMALLOC                             40.4M\nMALLOC guard page                    32K\nStack                              65.5M\nVM_ALLOCATE                         968K\n__CI_BITMAP                          80K\n__DATA                             8608K\n__DATA/__OBJC                       116K\n__IMAGE                             528K\n__IMPORT                              4K\n__LINKEDIT                         43.7M\n__OBJC                             1656K\n__OBJC/__DATA                        72K\n__PAGEZERO                            4K\n__TEXT                            150.2M\n__UNICODE                           544K\nmapped file                       117.1M\nshared memory                       312K\nshared pmap                         9.9M\n===========                      =======\nTOTAL                             441.1M\n\nModel: iMac12,1, BootROM IM121.0047.B1F, 4 processors, Intel Core i5, 2.5 GHz, 4 GB, SMC 1.71f22\nGraphics: AMD Radeon HD 6750M, AMD Radeon HD 6750M, PCIe, 512 MB\nMemory Module: BANK 0/DIMM0, 2 GB, DDR3, 1333 MHz, 0x80CE, 0x4D34373142353637334648302D4348392020\nMemory Module: BANK 1/DIMM0, 2 GB, DDR3, 1333 MHz, 0x80CE, 0x4D34373142353637334648302D4348392020\nAirPort: spairport_wireless_card_type_airport_extreme (0x168C, 0x9A), Atheros 9380: 4.0.64.8-P2P\nBluetooth: Version 4.0.5f11, 2 service, 18 devices, 1 incoming serial ports\nNetwork Service: Ethernet, Ethernet, en0\nNetwork Service: AirPort, AirPort, en1\nSerial ATA Device: WDC WD5000AAKS-402AA0, 500,11 GB\nSerial ATA Device: HL-DT-STDVDRW  GA32N\nUSB Device: hub_device, 0x0424  (SMSC), 0x2514, 0xfa100000 / 3\nUSB Device: BRCM2046 Hub, 0x0a5c  (Broadcom Corp.), 0x4500, 0xfa110000 / 4\nUSB Device: Bluetooth USB Host Controller, apple_vendor_id, 0x8215, 0xfa111000 / 5\nUSB Device: FaceTime HD Camera (Built-in), apple_vendor_id, 0x850b, 0xfa200000 / 2\nUSB Device: hub_device, 0x0424  (SMSC), 0x2514, 0xfd100000 / 2\nUSB Device: Internal Memory Card Reader, apple_vendor_id, 0x8403, 0xfd110000 / 4\nUSB Device: IR Receiver, apple_vendor_id, 0x8242, 0xfd120000 / 3\n";
		//		System.out.println(s);
		//		System.out.println("-----------------------");
		//		System.out.println("-----------------------");
		//		s = s.replaceAll("((https?|ftp)://[^`=\\[\\]\\\\\n\r\t;',~!\\$%^&*(){}|\"<>?/:]+)?[^a-zA-Z0-9\\.\\#\\+\\-\\@]", "$1 ");
		//		System.out.println(s);
		//		System.out.println("-----------------------");
		//		//TESTING-END.
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
}









