package utils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.JoinType;
import utils.Constants.LogicalOperation;
import utils.Constants.SortOrder;

//import Constants.SortOrder;

public class TSVManipulations {
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static final String COMBINED_KEY_SEPARATOR = Constants.TAB;
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, String[]> readUniqueKeyAndItsValueFromTSV(String inputPath, String inputFileName, Set<String> keySetToCheckExistenceOfKeyField, 
			int keyFieldNumber, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, 
			LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){//This method reads TSV lines into HashMap. The key is a unique field and value is a String[] containing all the values of that row. 
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		TreeMap<String, String[]> tsvRecordsHashMap = new TreeMap<String, String[]>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			MyUtils.println(writeMessageStep + "- Parsing " + inputFileName + ":", indentationLevel);
			MyUtils.println("Started ...", indentationLevel+1);
			int error1 = 0, error2 = 0, unmatchedRecords = 0;
			int i=0, matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == totalFieldsCount){
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							matchedRec++;
							if (!tsvRecordsHashMap.containsKey(keyField)){
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									tsvRecordsHashMap.put(keyField, fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									tsvRecordsHashMap.put(keyField, neededFields);
								}//else.
							}//if.
							else
								error2++;	//System.out.println("----" + keyField + "----" + fields[0] + "\t" + fields[1] + "\t" + fields[2]); //It should have been unique though!
						}//if (KeyS....					
					}
					else
						unmatchedRecords++;
				}//if.
				else
					error1++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with !=" + totalFieldsCount + " fields: " + Constants.integerFormatter.format(error1));
			if (error2>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with repeated keyField: " + Constants.integerFormatter.format(error2));

			if (logicalOperation == LogicalOperation.NO_CONDITION)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + ":-) No unmatched records with the conditions provided.");
				else
					System.out.println(MyUtils.indent(indentationLevel+1) + "Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
			}//if (cond....
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			//			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return tsvRecordsHashMap;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, String[]> readUniqueCombinedKeyAndItsValueFromTSV(String inputPath, String inputFileName, FileManipulationResult fMR, Set<String> keySetToCheckExistenceOfKeyField, 
			String keyFieldNumbers_separatedByDollar, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, 
			LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){//This method reads TSV lines into HashMap. The key is a unique field and value is a String[] containing all the values of that row. 
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		TreeMap<String, String[]> tsvRecordsHashMap = new TreeMap<String, String[]>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			MyUtils.println(writeMessageStep + "- Parsing " + inputFileName + ":", indentationLevel);
			MyUtils.println("Started ...", indentationLevel+1);
			int error1 = 0, error2 = 0, unmatchedRecords = 0;
			int i=0, matchedRec = 0;
			String s, keyField, keyField1, keyField2;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				String[] fields = s.split("\t");
				if (fields.length == totalFieldsCount){
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						String[] keyFieldNumbers = keyFieldNumbers_separatedByDollar.split("\\$");
						int keyField1Number = Integer.parseInt(keyFieldNumbers[0]);
						int keyField2Number = Integer.parseInt(keyFieldNumbers[1]);
						keyField1 = fields[keyField1Number];
						keyField2 = fields[keyField2Number];
						keyField = keyField1 + COMBINED_KEY_SEPARATOR + keyField2;
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							matchedRec++;
							if (!tsvRecordsHashMap.containsKey(keyField)){
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									tsvRecordsHashMap.put(keyField, fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									tsvRecordsHashMap.put(keyField, neededFields);
								}//else.
							}//if.
							else
								error2++;	//System.out.println("----" + keyField + "----" + fields[0] + "\t" + fields[1] + "\t" + fields[2]); //It should have been unique though!
						}//if (KeyS....					
					}
					else
						unmatchedRecords++;
				}//if.
				else
					error1++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0){
				MyUtils.println("Error) Number of records with !=" + totalFieldsCount + " fields: " + Constants.integerFormatter.format(error1), indentationLevel+1);
				fMR.errors = 1;
			}
			if (error2>0){
				MyUtils.println("Error) Number of records with repeated keyField: " + totalFieldsCount + " fields: " + Constants.integerFormatter.format(error2), indentationLevel+1);
				fMR.errors = 1;
			}

			if (logicalOperation == LogicalOperation.NO_CONDITION)
				MyUtils.println(Constants.integerFormatter.format(matchedRec) + " records have been read.", indentationLevel+1);
			else{
				MyUtils.println(Constants.integerFormatter.format(matchedRec) + " records have been read (matched with the provided conditions).", indentationLevel+1);
				if (unmatchedRecords == 0)
					MyUtils.println(":-) No unmatched records with the conditions provided.", indentationLevel+1);
				else
					MyUtils.println("Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords), indentationLevel+1);
			}//if (cond....
			MyUtils.println("Finished.", indentationLevel+1);
			//			System.out.println();
			br.close();
		}catch (Exception e){
			fMR.errors = 1;
			e.printStackTrace();
		}
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return tsvRecordsHashMap;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static HashSet<String> readNonUniqueFieldFromTSV_OnlyRepeatEachEntryOnce(String inputPath, String inputFileName,
			int keyFieldNumber, int totalFieldsCount,
			LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int indentationLevel, int showProgressInterval, long testOrReal, int writeMessageStep){//This method reads only one field in TSV lines into HashSet. The key is a non-unique field. If it sees repeats of that field, just ignores it. 
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		HashSet<String> tsvfieldHashSet = new HashSet<String>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(writeMessageStep + MyUtils.indent(indentationLevel) + "- Parsing " + inputFileName + ":");
			System.out.println(MyUtils.indent(indentationLevel) + "Started ...");
			int error1 = 0, unmatchedRecords = 0;
			String[] fields;
			int i=0; 
			int matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error1++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (!tsvfieldHashSet.contains(keyField))
							tsvfieldHashSet.add(keyField);
						matchedRec++;
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel) + Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println(MyUtils.indent(indentationLevel) + "Error) Number of records with !=" + totalFieldsCount + " fields: " + error1);
			if (logicalOperation == LogicalOperation.NO_CONDITION)
				System.out.println(MyUtils.indent(indentationLevel) + "Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println(MyUtils.indent(indentationLevel) + "Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println(MyUtils.indent(indentationLevel) + ":-) No unmatched records with the conditions provided.");
				else{
					System.out.println(MyUtils.indent(indentationLevel) + "Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
				}//else.
			}//else.
			System.out.println(MyUtils.indent(indentationLevel) + "Finished.");
			System.out.println();
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		return tsvfieldHashSet;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------	
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static HashSet<String> readUniqueFieldFromTSV(String inputPath, String inputFileName, 
			int keyFieldNumber, int totalFieldsCount,
			LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int indentationLevel, int showProgressInterval, long testOrReal, String writeMessageStep){//This method reads only one field in TSV lines into HashSet. The key is a unique field. If it sees repeats of that field, just ignores it but increments the number of errors and finally reports it. 
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		HashSet<String> tsvfieldHashSet = new HashSet<String>();
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			MyUtils.println(writeMessageStep+"- Parsing "+ inputFileName+":", indentationLevel);
			MyUtils.println("Started ...", indentationLevel+1);
			int error1 = 0, error2 = 0, unmatchedRecords = 0;
			String[] fields;
			int i=0; 
			int matchedRec = 0;
			String s, keyField;
			boolean recordShouldBeRead;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error1++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (!tsvfieldHashSet.contains(keyField))
							tsvfieldHashSet.add(keyField);
						else
							error2++;
						matchedRec++;
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....
			if (error1>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with != " + totalFieldsCount + " fields: " + error1);
			if (error2>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with duplicate keyfield: " + error2);
			if (logicalOperation == LogicalOperation.NO_CONDITION)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else{
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + ":-) No unmatched records with the conditions provided.");
				else{
					System.out.println(MyUtils.indent(indentationLevel+1) + "Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
				}//else.
			}//else.
			MyUtils.println("Finished.", indentationLevel+1);
			br.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		else
			System.out.println();
		return tsvfieldHashSet;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------	
	//----------------------------------------------------------------------------------------------------------------------------------------
	//This method 
	public static void mergeTwoTSVFieldsTogether(String inputPath, String inputFileName,  
			String outputPathAndFileName, 
			int field1Number, int field2Number, String delimiter, int totalFieldsNumber,
			boolean wrapOutputInLines, int indentationLevel, int showProgressInterval, long testOrReal, int writeMessageStep){
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(writeMessageStep + "- Parsing " + inputFileName + ", merging two columns and writing in " + outputPathAndFileName + ":");
			System.out.println(MyUtils.indent(indentationLevel) + "Started ...");
			FileWriter writer = new FileWriter(outputPathAndFileName);
			int error = 0;
			String[] fields;
			int i=0;
			String s, output;
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length == totalFieldsNumber){
					fields[field1Number] = fields[field1Number] + "/" + fields[field2Number];
					output = fields[0];
					for (int j=1; j<totalFieldsNumber; j++)
						if (j != field2Number)
							output = output + "\t" + fields[j];
					output = output + "\n";
					writer.append(output);
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel) + Constants.integerFormatter.format(i));
			}
			writer.flush();writer.close();
			br.close();
			System.out.println(MyUtils.indent(indentationLevel) +  Constants.integerFormatter.format(i) + " records have been read.");
			if (error>0)
				System.out.println(MyUtils.indent(indentationLevel) + "Error) Number of records with !=" + totalFieldsNumber + " fields: " + error);
			System.out.println(MyUtils.indent(indentationLevel) + "Finished.");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
	}//mergeTwoTSVFieldsTogether().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, ArrayList<String[]>> readNonUniqueKeyAndItsValueFromTSV(String inputPath, String inputFileName, FileManipulationResult fMR, Set<String> keySetToCheckExistenceOfKeyField, 
			int keyFieldNumber, Constants.SortOrder sortOrder, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, ArrayList<String> titlesToReturn, //:this parameter returns the titles of those needed fields.  
			Constants.LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, 
			long testOrReal, String writeMessageStep){//This method reads TSV lines into HashMap. 
		//The key is a non-unique field (#keyFieldNumber) and value is an ArrayList<String[]> containing all the values of all the rows that have the same keyFieldNumber. Values of each row is stored in a String[].
		//		TreeMap<String, ArrayList<String[]>> tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>();
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		TreeMap<String, ArrayList<String[]>> tsvRecordsTreeMap;
		if (sortOrder == Constants.SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>();
		else
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//Uncomment these lines if you have empty (or space) values:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) > Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) < Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//if.
			else{
				tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) < Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) > Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Parsing " + inputFileName + ":");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int error = 0, unmatchedRecords = 0;
			String[] fields;
			boolean isNew, recordShouldBeRead;
			ArrayList<String[]> aTSVItemRelatedToTheRepeatedKey; 
			int i=0, equalObjectsFound = 0, matchedRec = 0;
			String s, keyField;
			s = br.readLine(); //header.
			
			String[] titles  = s.split(Constants.TAB); //The complete titles.
			String[] numbersOfNeededTitles = fieldNumbersToBeRead_separatedByDollar.split("\\$");
			for (int k=0; k<numbersOfNeededTitles.length; k++)
				titlesToReturn.add(titles[Integer.parseInt(numbersOfNeededTitles[k])]);
			
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							if (tsvRecordsTreeMap.containsKey(keyField)){
								aTSVItemRelatedToTheRepeatedKey = tsvRecordsTreeMap.get(keyField);
								isNew = true;
								for (String[] stringArray: aTSVItemRelatedToTheRepeatedKey)
									if (StringManipulations.twoStringArraysAreEqual(fields, stringArray)){ //Note:   if (fields.equals(stringArray))   does not work! Just the used notation is correct.
										equalObjectsFound++;
										isNew = false;
										break;
									}//if.					
								if (isNew){
									if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
										aTSVItemRelatedToTheRepeatedKey.add(fields);
									else{//means that only some of the fields are needed.
										String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
										for (int k=0; k<neededFields.length; k++)
											neededFields[k] = fields[Integer.parseInt(neededFields[k])];
										aTSVItemRelatedToTheRepeatedKey.add(neededFields);
									}//else.
								}//if
							}//if.
							else{
								ArrayList<String[]> aTSVItemRelatedToANewKey = new ArrayList<String[]>();
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									aTSVItemRelatedToANewKey.add(fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									//									if (neededFields.length>1 || !neededFields[0].equals(""))//Just added this "if" because the user may want no extra field from a table.
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									aTSVItemRelatedToANewKey.add(neededFields);
								}//else.
								tsvRecordsTreeMap.put(keyField, aTSVItemRelatedToANewKey);
							}//else.
							matchedRec++;
						}//if (tsvRecordsHashMap.containsKey(keyField)).
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....

			if (error>0)
				MyUtils.println("Error) Number of records with !=" + totalFieldsCount + " fields: " + error, indentationLevel+1);

			if (equalObjectsFound>0)
				MyUtils.println("Hint) Number of repeated TSV records (ignored): " + equalObjectsFound, indentationLevel+1);

			if (logicalOperation == LogicalOperation.NO_CONDITION)
				MyUtils.println(Constants.integerFormatter.format(matchedRec) + " records have been read.", indentationLevel+1);
			else{
				MyUtils.println(Constants.integerFormatter.format(matchedRec) + " records have been read (matched with the provided conditions).", indentationLevel+1);
				if (unmatchedRecords == 0)
					MyUtils.println(":-) No unmatched records with the conditions provided.", indentationLevel+1);
				else
					MyUtils.println("Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords), indentationLevel+1);
			}//if (cond....
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			br.close();

			fMR.errors = 0;
			fMR.doneSuccessfully = 1;
		}catch (Exception e){
			fMR.errors = 1;
			fMR.doneSuccessfully = 0;
			e.printStackTrace();
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return tsvRecordsTreeMap;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, TreeSet<String>> readNonUniqueKeyAndItsValueAsTreeSetFromTSV(
			String inputPath, String inputFileName, FileManipulationResult fMR, Set<String> keySetToCheckExistenceOfKeyField, 
			int keyFieldNumber, Constants.SortOrder sortOrder, int totalFieldsCount, int valueFieldNumber, 
			Constants.LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, 
			long testOrReal, String writeMessageStep){//This method reads TSV lines into HashMap. 
		//The key is a non-unique field (#keyFieldNumber) and value is an ArrayList<String[]> containing all the values of all the rows that have the same keyFieldNumber. Values of each row is stored in a String[].
		//		TreeMap<String, ArrayList<String[]>> tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>();
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		TreeMap<String, TreeSet<String>> tsvRecordsTreeMap;
		if (sortOrder == Constants.SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			tsvRecordsTreeMap = new TreeMap<String, TreeSet<String>>();
		else
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				tsvRecordsTreeMap = new TreeMap<String, TreeSet<String>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//Uncomment these lines if you have empty (or space) values:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) > Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) < Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//if.
			else{
				tsvRecordsTreeMap = new TreeMap<String, TreeSet<String>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) < Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) > Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Parsing " + inputFileName + ":");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int error = 0, unmatchedRecords = 0;
			String[] fields;
			boolean recordShouldBeRead;
			TreeSet<String> aTreeSetRelatedToTheRepeatedKey; 
			int i=0, equalObjectsFound = 0, matchedRec = 0;
			int numberOfKeyValuesRead = 0;
			String s, keyField, valueField;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\t");
				if (fields.length != totalFieldsCount)
					error++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						keyField = fields[keyFieldNumber];
						valueField = fields[valueFieldNumber];
						if (!valueField.equals(" "))
							if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
								if (tsvRecordsTreeMap.containsKey(keyField)){
									aTreeSetRelatedToTheRepeatedKey = tsvRecordsTreeMap.get(keyField);
									if (aTreeSetRelatedToTheRepeatedKey.contains(valueField))
										equalObjectsFound++;//This key and value exists (like a committer login in a project that may be repeated). Do nothing except recording the number of equal key-values.
									else{
										aTreeSetRelatedToTheRepeatedKey.add(valueField);
										numberOfKeyValuesRead++;
									}
								}//if.
								else{
									TreeSet<String> aTSVItemRelatedToANewKey = new TreeSet<String>();
									aTSVItemRelatedToANewKey.add(valueField);
									tsvRecordsTreeMap.put(keyField, aTSVItemRelatedToANewKey);
									numberOfKeyValuesRead++;
								}//else.
								matchedRec++;
							}//if (tsvRecordsHashMap.containsKey(keyField)).
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....

			if (error>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with !=" + totalFieldsCount + " fields: " + error);

			if (equalObjectsFound>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Hint) Number of repeated key-values in the TSV records (ignored): " + equalObjectsFound);

			if (logicalOperation == LogicalOperation.NO_CONDITION)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else
			{
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + ":-) No unmatched records with the conditions provided.");
				else
					System.out.println(MyUtils.indent(indentationLevel+1) + "Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
			}//if (cond....
			MyUtils.println("Total # of distinct key-values read: " + Constants.integerFormatter.format(numberOfKeyValuesRead), indentationLevel+1);
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			br.close();

			fMR.errors = 0;
			fMR.doneSuccessfully = 1;
		}catch (Exception e){
			fMR.errors = 1;
			fMR.doneSuccessfully = 0;
			e.printStackTrace();
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return tsvRecordsTreeMap;
	}//readNonUniqueKeyAndItsValueAsHashSetFromTSV().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, ArrayList<String[]>> readNonUniqueCombinedKeyAndItsValueFromTSV(String inputPath, String inputFileName, FileManipulationResult fMR, Set<String> keySetToCheckExistenceOfKeyField, 
			String keyFieldNumbers_separatedByDollar,    
			Constants.SortOrder sortOrder, int totalFieldsCount, String fieldNumbersToBeRead_separatedByDollar, 
			Constants.LogicalOperation logicalOperation, 
			int field1Number, ConditionType condition1Type, String field1Value, FieldType field1Type, 
			int field2Number, ConditionType condition2Type, String field2Value, FieldType field2Type, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, 
			long testOrReal, String writeMessageStep){//This method reads TSV lines into HashMap. 
		//The key is a non-unique field (#keyFieldNumber) and value is an ArrayList<String[]> containing all the values of all the rows that have the same keyFieldNumber. Values of each row is stored in a String[].
		//		TreeMap<String, ArrayList<String[]>> tsvRecordsHashMap = new TreeMap<String, ArrayList<String[]>>();
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		TreeMap<String, ArrayList<String[]>> tsvRecordsTreeMap;
		if (sortOrder == Constants.SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>();
		else
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//Uncomment these lines if you have empty (or space) values:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						String firstPartOfS1 = s1.split(COMBINED_KEY_SEPARATOR)[0];
						String firstPartOfS2 = s2.split(COMBINED_KEY_SEPARATOR)[0];
						if (Integer.parseInt(firstPartOfS1) > Integer.parseInt(firstPartOfS2))
							return 1;
						else
							if (Integer.parseInt(firstPartOfS1) < Integer.parseInt(firstPartOfS2))
								return -1;
							else{//This means that the first parts (index 0) are the same. So let's compare the second parts (index 1):
								String secondPartOfS1 = s1.split(COMBINED_KEY_SEPARATOR)[1];
								String secondPartOfS2 = s2.split(COMBINED_KEY_SEPARATOR)[1];
								if (Integer.parseInt(secondPartOfS1) > Integer.parseInt(secondPartOfS2))
									return 1;
								else
									if (Integer.parseInt(secondPartOfS1) < Integer.parseInt(secondPartOfS2))
										return -1;
									else
										return 0;
							}
					}
				});
			}//if.
			else{
				tsvRecordsTreeMap = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) < Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) > Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			//Reading posts and adding <repoId, totalNumberOfMembers> in a hashMap:
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Parsing " + inputFileName + ":");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int errorInNumberOfFields = 0, unmatchedRecords = 0;
			String[] fields;
			boolean isNew, recordShouldBeRead;
			ArrayList<String[]> aTSVItemRelatedToTheRepeatedKey; 
			int i=0, equalObjectsFound = 0, matchedRec = 0;
			String s, keyField, keyField1, keyField2;
			br.readLine(); //header.
			while ((s=br.readLine())!=null){
				fields = s.split("\\t");
				if (fields.length != totalFieldsCount)
					errorInNumberOfFields++;
				else{
					recordShouldBeRead = MyUtils.runLogicalComparison(logicalOperation, fields[field1Number], condition1Type, field1Value, field1Type, fields[field2Number], condition2Type, field2Value, field2Type);
					if (recordShouldBeRead){
						String[] keyFieldNumbers = keyFieldNumbers_separatedByDollar.split("\\$");
						int keyField1Number = Integer.parseInt(keyFieldNumbers[0]);
						int keyField2Number = Integer.parseInt(keyFieldNumbers[1]);
						keyField1 = fields[keyField1Number];
						keyField2 = fields[keyField2Number];
						keyField = keyField1 + COMBINED_KEY_SEPARATOR + keyField2;
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyField)){
							if (tsvRecordsTreeMap.containsKey(keyField)){
								aTSVItemRelatedToTheRepeatedKey = tsvRecordsTreeMap.get(keyField);
								isNew = true;
								for (String[] stringArray: aTSVItemRelatedToTheRepeatedKey)
									if (StringManipulations.twoStringArraysAreEqual(fields, stringArray)){ //Note:   if (fields.equals(stringArray))   does not work! Just the used notation is correct.
										equalObjectsFound++;
										isNew = false;
										break;
									}//if.					
								if (isNew){
									if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
										aTSVItemRelatedToTheRepeatedKey.add(fields);
									else{//means that only some of the fields are needed.
										String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
										for (int k=0; k<neededFields.length; k++)
											neededFields[k] = fields[Integer.parseInt(neededFields[k])];
										aTSVItemRelatedToTheRepeatedKey.add(neededFields);
									}//else.
								}//if
							}//if.
							else{
								ArrayList<String[]> aTSVItemRelatedToANewKey = new ArrayList<String[]>();
								if (fieldNumbersToBeRead_separatedByDollar.equals("ALL"))//means that all the fields are needed.
									aTSVItemRelatedToANewKey.add(fields);
								else{//means that only some of the fields are needed.
									String[] neededFields = fieldNumbersToBeRead_separatedByDollar.split("\\$");
									//									if (neededFields.length>1 || !neededFields[0].equals(""))//Just added this "if" because the user may want no extra field from a table.
									for (int k=0; k<neededFields.length; k++)
										neededFields[k] = fields[Integer.parseInt(neededFields[k])];
									aTSVItemRelatedToANewKey.add(neededFields);
								}//else.
								tsvRecordsTreeMap.put(keyField, aTSVItemRelatedToANewKey);
							}//else.
							matchedRec++;
						}//if (tsvRecordsHashMap.containsKey(keyField)).
					}//if (reco....
					else
						unmatchedRecords++;
				}//else.
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while ((s=br....

			if (errorInNumberOfFields>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with !=" + totalFieldsCount + " fields: " + errorInNumberOfFields);

			if (equalObjectsFound>0)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Hint) Number of repeated TSV records (ignored): " + equalObjectsFound);

			if (logicalOperation == LogicalOperation.NO_CONDITION)
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(matchedRec));
			else
			{
				System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read (matched with the provided conditions): " + Constants.integerFormatter.format(matchedRec));
				if (unmatchedRecords == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + ":-) No unmatched records with the conditions provided.");
				else
					System.out.println(MyUtils.indent(indentationLevel+1) + "Number of ignored records (unmatched with the provided conditions): " + Constants.integerFormatter.format(unmatchedRecords));
			}//if (cond....
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			br.close();

			fMR.errors = 0;
			fMR.doneSuccessfully = 1;
		}catch (Exception e){
			fMR.errors = 1;
			fMR.doneSuccessfully = 0;
			e.printStackTrace();
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return tsvRecordsTreeMap;
	}
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult saveKeyAndLongValuesAsTSVFile(String outputPath, String outputFileName, TreeMap<String, Long> counts, 
			int totalFieldsCount, String[] titles,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Writing file \"" + outputFileName + "\"");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int i = 0;
			FileWriter writer = new FileWriter(outputPath + "\\" + outputFileName);
			writer.append(titles[0]);
			for (int j=1; j<totalFieldsCount; j++)
				writer.append(Constants.TAB + titles[j]);
			writer.append("\n");
			for(Map.Entry<String,Long> entry : counts.entrySet()) {
				String key = entry.getKey();
				Long value = entry.getValue();
				writer.append(key + Constants.TAB + value + "\n");
				i++;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
			}
			writer.flush();
			writer.close();
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records written: " + Constants.integerFormatter.format(i) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Finished.");
			fMR.doneSuccessfully = 1;
		}catch (Exception e){
			e.printStackTrace();
			fMR.errors = 1;
		}
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//saveCountsAsTSVFile().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult saveKeyAndDoubleValuesAsTSVFile(String outputPath, String outputFileName, TreeMap<String, Double> counts, 
			int totalFieldsCount, String[] titles,
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Writing file \"" + outputFileName + "\"");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int i = 0;
			FileWriter writer = new FileWriter(outputPath + "\\" + outputFileName);
			writer.append(titles[0]);
			for (int j=1; j<totalFieldsCount; j++)
				writer.append(Constants.TAB + titles[j]);
			writer.append("\n");
			for(Map.Entry<String,Double> entry : counts.entrySet()) {
				String key = entry.getKey();
				Double value = entry.getValue();
				writer.append(key + Constants.TAB + value + "\n");
				i++;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
			}
			writer.flush();
			writer.close();
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records written: " + Constants.integerFormatter.format(i) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Finished.");
			fMR.doneSuccessfully = 1;
		}catch (Exception e){
			e.printStackTrace();
			fMR.errors = 1;
		}
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//saveCountsAsTSVFile().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult saveTreeMapToTSVFile(String outputPath, String outputFileName, TreeMap<String, ArrayList<String[]>> tm, 
			String titles, boolean alsoSaveTheKey, int indexForKeyFieldToSave, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Writing file \"" + outputFileName + "\"");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int i = 0;
			FileWriter writer = new FileWriter(outputPath + "\\" + outputFileName);
			writer.append(titles + "\n");
			for(Map.Entry<String,ArrayList<String[]>> entry : tm.entrySet()) {
				for (int j=0; j<entry.getValue().size(); j++){
					String[] s = entry.getValue().get(j);
					String aLine = "";
					if (s.length>0){
						//Saving "indexForKeyFieldToSave" number of fields:
						for (int k=0; k<indexForKeyFieldToSave; k++)
							aLine = concatTwoSetsOfFields(aLine, s[k]);
						//Saving the key field:
						if (alsoSaveTheKey)
							aLine = concatTwoSetsOfFields(aLine, entry.getKey());
						//Saving the rest of the fields (fields from number "indexForKeyFieldToSave" forward):
						for (int k=indexForKeyFieldToSave; k<s.length; k++)
							aLine = concatTwoSetsOfFields(aLine, s[k]);
						i++;
					}
					writer.append(aLine + "\n");
				}//for.
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
			}
			writer.flush();
			writer.close();
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records written: " + Constants.integerFormatter.format(i) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Finished.");
			fMR.doneSuccessfully = 1;
			fMR.errors = 0;
		}catch (Exception e){
			e.printStackTrace();
			fMR.doneSuccessfully = 0;
			fMR.errors = 1;
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//saveTreeMapToTSVFile().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult saveTreeMapOfStringAndTreeSetToTSVFile(String outputPath, String outputFileName, TreeMap<String, TreeSet<String>> ts, 
			String titles,  
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep){
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult fMR = new FileManipulationResult();
		try{
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Writing file \"" + outputFileName + "\"");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int i = 0;
			int j = 0;
			FileWriter writer = new FileWriter(outputPath + "\\" + outputFileName);
			writer.append(titles + "\n");
			for(Map.Entry<String,TreeSet<String>> entry : ts.entrySet()) {
				TreeSet<String> tempTS = entry.getValue();
				for (String s:tempTS){
					String aLine = entry.getKey() + Constants.TAB + s;
					writer.append(aLine + "\n");
					j++;
				}//for.
				i++;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
			}
			writer.flush();
			writer.close();
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records written: " + Constants.integerFormatter.format(i) + "-->" 
					+ Constants.integerFormatter.format(j) + ".");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			fMR.doneSuccessfully = 1;
			fMR.errors = 0;
		}catch (Exception e){
			e.printStackTrace();
			fMR.doneSuccessfully = 0;
			fMR.errors = 1;
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//saveTreeMapOfStringAndHashSetToTSVFile().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, Long> groupBy_count_fromTSV(String inputPath, String inputFileName, FileManipulationResult[] fMRArray, Set<String> keySetToCheckExistenceOfKeyField, 
			String keyField, Constants.SortOrder sortOrder, int totalFieldsCount, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel,
			long testOrReal, String writeMessageStep){//This method reads TSV lines and counts the records for each key (something like getting count(x) after group by key in SQL). 
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		TreeMap<String, Long> result = new TreeMap<String, Long>();
		if (sortOrder == Constants.SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			result = new TreeMap<String, Long>();
		else
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				result = new TreeMap<String, Long>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//You can comment these (4) lines if you don't have empty (or space) values:
						if (s1.equals("") || s1.equals(" "))
							s1 = Long.toString(Constants.AN_EXTREMELY_NEGATIVE_LONG);
						if (s2.equals("") || s2.equals(" "))
							s2 = Long.toString(Constants.AN_EXTREMELY_NEGATIVE_LONG);
						//Up to here.
						if (Long.parseLong(s1) > Long.parseLong(s2))
							return 1;
						else
							if (Long.parseLong(s1) < Long.parseLong(s2))
								return -1;
							else
								return 0;
					}
				}); //result = new Tree...
			}//if.
			else{
				result = new TreeMap<String, Long>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//You can comment these (4) lines if you don't have empty (or space) values:
						if (s1.equals("") || s1.equals(" "))
							s1 = Long.toString(Constants.AN_EXTREMELY_POSITIVE_LONG);
						if (s2.equals("") || s2.equals(" "))
							s2 = Long.toString(Constants.AN_EXTREMELY_POSITIVE_LONG);
						//Up to here.
						if (Long.parseLong(s1) < Long.parseLong(s2))
							return 1;
						else
							if (Long.parseLong(s1) > Long.parseLong(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Counting values for \"" + keyField + "\" (in \"" + inputFileName + "\"):");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int error1=0, error2 = 0;
			String[] fields, titles;
			int i=0, keyFieldNumber = Constants.ERROR;
			String s, keyFieldValue;
			s = br.readLine(); //header.
			titles = s.split(Constants.TAB);
			for (int j=0; j< titles.length; j++)
				if (titles[j].equals(keyField))
					keyFieldNumber = j;
			if (keyFieldNumber == Constants.ERROR)
				error1++;
			else
				while ((s=br.readLine())!=null){
					if (!s.equals("")){
						fields = s.split(Constants.TAB);
						if (fields.length != totalFieldsCount)
							error2++;
						else{
							keyFieldValue = fields[keyFieldNumber];
							if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyFieldValue)){
								long count;
								if (result.containsKey(keyFieldValue))
									count = result.get(keyFieldValue)+1;
								else
									count = 1;
								result.put(keyFieldValue, count);
							}						
						}//else.
						i++;
						if (i % showProgressInterval == 0)
							System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
						if (testOrReal > Constants.THIS_IS_REAL)
							if (i >= testOrReal)
								break;
					}//if.
				}//while ((s=br....
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(i) + ".");
			if (error1>0){
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) \"" + keyField + "\" was not identified!");
				fMRArray[0].errors = 1;
			}
			if (error2>0){
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with != " + totalFieldsCount + " fields: " + Constants.integerFormatter.format(error2));
				fMRArray[0].errors = 1;
			}
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			fMRArray[0].errors = 1;
		}
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static TreeMap<String, Long> groupBy_sum_fromTSV(String inputPath, String inputFileName, Set<String> keySetToCheckExistenceOfKeyField, 
			String keyField, String summingField, Constants.SortOrder sortOrder, int totalFieldsCount, FileManipulationResult[] fMRArray, 
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel,
			long testOrReal, String writeMessageStep){//This method reads TSV lines and counts the records for each key (something like getting count(x) after group by key in SQL). 
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		TreeMap<String, Long> result = new TreeMap<String, Long>();
		if (sortOrder == Constants.SortOrder.DEFAULT_FOR_STRING)//means that keyfield is not integer.
			result = new TreeMap<String, Long>();
		else
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				result = new TreeMap<String, Long>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//You can comment these (4) lines if you don't have empty (or space) values:
						if (s1.equals("") || s1.equals(" "))
							s1 = Long.toString(Constants.AN_EXTREMELY_NEGATIVE_LONG);
						if (s2.equals("") || s2.equals(" "))
							s2 = Long.toString(Constants.AN_EXTREMELY_NEGATIVE_LONG);
						//Up to here.
						if (Long.parseLong(s1) > Long.parseLong(s2))
							return 1;
						else
							if (Long.parseLong(s1) < Long.parseLong(s2))
								return -1;
							else
								return 0;
					}
				}); //result = new Tree...
			}//if.
			else{
				result = new TreeMap<String, Long>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//You can comment these (4) lines if you don't have empty (or space) values:
						if (s1.equals("") || s1.equals(" "))
							s1 = Long.toString(Constants.AN_EXTREMELY_POSITIVE_LONG);
						if (s2.equals("") || s2.equals(" "))
							s2 = Long.toString(Constants.AN_EXTREMELY_POSITIVE_LONG);
						//Up to here.
						if (Long.parseLong(s1) < Long.parseLong(s2))
							return 1;
						else
							if (Long.parseLong(s1) > Long.parseLong(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
		try{ 
			BufferedReader br;
			br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Sum(" + summingField + ") after grouping by \"" + keyField + "\" (in \"" + inputFileName + "\"):");
			System.out.println(MyUtils.indent(indentationLevel+1) +  "Started ...");
			int error = 0;
			String[] fields, titles;
			int i=0, keyFieldNumber = Constants.ERROR, summingFieldNumber = Constants.ERROR;
			String s, keyFieldValue, summingFieldValue;
			s = br.readLine(); //header.
			titles = s.split(Constants.TAB);
			for (int j=0; j< titles.length; j++){
				if (titles[j].equals(keyField))
					keyFieldNumber = j;
				if (titles[j].equals(summingField))
					summingFieldNumber = j;
			}//for.
			if (keyFieldNumber == Constants.ERROR || summingFieldNumber == Constants.ERROR)
				error++;
			else
				while ((s=br.readLine())!=null){
					fields = s.split(Constants.TAB);
					if (fields.length != totalFieldsCount)
						error++;
					else{
						keyFieldValue = fields[keyFieldNumber];
						summingFieldValue = fields[summingFieldNumber];
						if (keySetToCheckExistenceOfKeyField == null || keySetToCheckExistenceOfKeyField.contains(keyFieldValue)){
							long theNewValue;
							if (summingFieldValue.equals(""))
								theNewValue = 0;
							else
								theNewValue = Integer.parseInt(summingFieldValue);
							long theNewValuePlusSumOfAllOtherValues;
							if (result.containsKey(keyFieldValue))
								theNewValuePlusSumOfAllOtherValues = result.get(keyFieldValue)+theNewValue;
							else
								theNewValuePlusSumOfAllOtherValues = theNewValue;
							result.put(keyFieldValue, theNewValuePlusSumOfAllOtherValues);
						}						
					}//else.
					i++;
					if (i % showProgressInterval == 0)
						System.out.println(MyUtils.indent(indentationLevel+1) +  Constants.integerFormatter.format(i));
					if (testOrReal > Constants.THIS_IS_REAL)
						if (i >= testOrReal)
							break;
				}//while ((s=br....
			System.out.println(MyUtils.indent(indentationLevel+1) + "Number of records read: " + Constants.integerFormatter.format(i) + ".");
			if (error>0){
				System.out.println(MyUtils.indent(indentationLevel+1) + "Error) Number of records with != " + totalFieldsCount + " fields: " + Constants.integerFormatter.format(error));
				fMRArray[0].errors = 1;
			}
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			br.close();
		}catch (Exception e){
			e.printStackTrace();
			fMRArray[0].errors = 1;
		}
		if (wrapOutputInLines)
			System.out.println("-----------------------------------");
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//This file replaces a foreign key (e.g., userId) with its value from another TSV (provided that the relation is 1:1 or 1:n)
	public static FileManipulationResult replaceForeignKeyInTSVWithValueFromAnotherTSV(String foreignKeyInputPath, String foreignKeyInputFileName,  
			String primaryKeyInputPath, String primaryKeyInputFileName, 
			String outputPathAndFileName, 
			String fKField, int foreignKeyTotalFieldsNumber,
			String pKField, int primaryKeyTotalFieldsNumber, 
			String pKSubstituteField, //this is the field that is written instead of foreign key.
			String substituteNewTitle,//under this title.
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, long testOrReal, String writeMessageStep
			){
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult fMR = new FileManipulationResult();
		try{ 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Foreign Key replacement (\"" + fKField + "\") in \"" + foreignKeyInputFileName + "\"):");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int error = 0;
			String[] titles;
			int i=0, PrimaryKeyFieldNumber = Constants.ERROR, foreignKeyFieldNumber = Constants.ERROR, primaryKeySubstituteFieldNumber = Constants.ERROR;
			String s, header, outputLine;

			BufferedReader br;
			br = new BufferedReader(new FileReader(primaryKeyInputPath + "\\" + primaryKeyInputFileName)); 
			s = br.readLine();
			titles = s.split(Constants.TAB);
			for (int j=0; j< titles.length; j++){
				if (titles[j].equals(pKField))
					PrimaryKeyFieldNumber = j;
				if (titles[j].equals(pKSubstituteField))
					primaryKeySubstituteFieldNumber = j;
			}//for.
			br.close();

			TreeMap<String, String[]> primaryKeyRecords = readUniqueKeyAndItsValueFromTSV(
					primaryKeyInputPath, primaryKeyInputFileName, null, PrimaryKeyFieldNumber, primaryKeyTotalFieldsNumber, "ALL", 
					LogicalOperation.NO_CONDITION, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					false, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-1");
			//TreeMap<String, String[]> foreignKeyRecords = readUniqueKeyAndItsValueFromTSV(foreignKeyInputTSVPath, foreignKeyInputTSVFile, ForeignKeyFieldNumber, foreignKeyTotalFieldsNumber, ConditionType.NO_CONDITION, 0, "", 0, "", 100000, testOrReal, 2);
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep+"-2- Producing output (foreign key replaced by value) ...");
			System.out.println(MyUtils.indent(indentationLevel+2) + "Started ...");
			String[] theFKRecord, aPKRecord;

			//Reading the header and substituting the foreign key field title:
			br = new BufferedReader(new FileReader(foreignKeyInputPath + "\\" + foreignKeyInputFileName)); 
			s = br.readLine();
			titles = s.split(Constants.TAB);
			for (int j=0; j< titles.length; j++){
				if (titles[j].equals(fKField))
					foreignKeyFieldNumber = j;
			}//for.

			if (foreignKeyFieldNumber == 0)
				header = substituteNewTitle;
			else
				header = titles[0];
			for (int j=1; j<foreignKeyTotalFieldsNumber; j++)
				if (j == foreignKeyFieldNumber)
					header = header + "\t" + substituteNewTitle;
				else
					header = header + "\t" + titles[j];
			header = header + "\n";
			FileWriter writer = new FileWriter(outputPathAndFileName);
			writer.append(header);
			//Now, replacing the FK with values from PK file:
			while ((s=br.readLine())!=null){
				theFKRecord = s.split("\t");
				if (theFKRecord.length == foreignKeyTotalFieldsNumber){
					aPKRecord = primaryKeyRecords.get(theFKRecord[foreignKeyFieldNumber]);
					if (aPKRecord == null)//:means that the foreign key points to something that does not exist. return "" (NULL) in this case.
						theFKRecord[foreignKeyFieldNumber] = "";
					else
						theFKRecord[foreignKeyFieldNumber] = aPKRecord[primaryKeySubstituteFieldNumber];
					outputLine = theFKRecord[0];
					for (int j=1; j<theFKRecord.length; j++)
						outputLine = outputLine + "\t" + theFKRecord[j];
					outputLine = outputLine + "\n";
					writer.append(outputLine);
				}
				else
					error++;
				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(i));

			}//for (Stri....
			writer.flush();writer.close();
			br.close();
			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records read: " + Constants.integerFormatter.format(i));
			if (error>0){
				System.out.println(MyUtils.indent(indentationLevel+2) + "Error) Number of FK records with !=" + foreignKeyTotalFieldsNumber + " fields: " + error);
				fMR.errors = 1;
				fMR.doneSuccessfully = 0;
			}//if.
			else
				fMR.doneSuccessfully = 1;
			System.out.println(MyUtils.indent(indentationLevel+2) + "Finished.");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
		}catch(Exception e){
			e.printStackTrace();
			fMR.errors = 1;
			fMR.doneSuccessfully = 0;
		}
		fMR.processed = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return fMR;
	}//replaceForeignKeyInTSVWithValueFromAnotherTSV().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult runGroupBy_count_andSaveResultToTSV(String inputPath, String inputTSVFileName, String outputPath, String outputTSVFileName,  
			String groupByField, int totalFieldsCount, String titleOfGroupByFieldInOutputFile, String titleOfCountedFieldForOutputFile, SortOrder sortOrder, 
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep) {
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Groupping by \"" + groupByField + "\" (in \"" + inputTSVFileName + "\"):");
		System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
		FileManipulationResult result = new FileManipulationResult();	

		FileManipulationResult[] fMRArray = new FileManipulationResult[1]; 
		fMRArray[0] = new FileManipulationResult();//: this variable is for making a call-by-reference variable.
		TreeMap<String, Long> usersAndTheirFollowers = groupBy_count_fromTSV(
				inputPath, inputTSVFileName, fMRArray, null, groupByField, sortOrder , totalFieldsCount, 
				false, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-1");
		result = MyUtils.addFileManipulationResults(result, fMRArray[0]);

		String[] titles = new String[]{titleOfGroupByFieldInOutputFile, titleOfCountedFieldForOutputFile};
		fMRArray[0] = saveKeyAndLongValuesAsTSVFile(outputPath, outputTSVFileName, usersAndTheirFollowers,  
				totalFieldsCount, titles, 
				false, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-2");
		result = MyUtils.addFileManipulationResults(result, fMRArray[0]);

		System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
		result.processed = 1;
		if (result.errors == 0)
			result.doneSuccessfully = 1;
		else
			result.doneSuccessfully = 0;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return result;
	}//convertAllFilseInFolderToTSV().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult runGroupBy_sum_andSaveResultToTSV(String inputPath, String inputTSVFileName, String outputPath, String outputTSVFileName,  
			String groupByField, String summingField, int totalFieldsCount, String titleOfGroupByFieldInOutputFile, String titleOfSummedFieldForOutputFile, SortOrder sortOrder, 
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep) {
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Groupping by \"" + groupByField + "\" (in \"" + inputTSVFileName + "\"):");
		System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
		FileManipulationResult result = new FileManipulationResult();	

		FileManipulationResult[] fMRArray = new FileManipulationResult[1]; 
		fMRArray[0] = new FileManipulationResult();//: this variable is for making a call-by-reference variable.
		TreeMap<String, Long> usersAndTheirFollowers = groupBy_sum_fromTSV(
				inputPath, inputTSVFileName, null, groupByField, summingField, sortOrder , totalFieldsCount, fMRArray, 
				false, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-1");
		result = MyUtils.addFileManipulationResults(result, fMRArray[0]);

		String[] titles = new String[]{titleOfGroupByFieldInOutputFile, titleOfSummedFieldForOutputFile};
		fMRArray[0] = saveKeyAndLongValuesAsTSVFile(outputPath, outputTSVFileName, usersAndTheirFollowers,  
				totalFieldsCount, titles, 
				false, showProgressInterval, indentationLevel+1, testOrReal, writeMessageStep+"-2");
		result = MyUtils.addFileManipulationResults(result, fMRArray[0]);

		System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
		result.processed = 1;
		if (result.errors == 0)
			result.doneSuccessfully = 1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return result;
	}//groupBy_sum().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//T1Fields and T2Fields are lists of fields separated by Constants.SEPARATOR_FOR_FIELDS_IN_TSV_FILE (i.e. "\t")
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult joinTwoTSVs(String inputPath1, String inputTSV1, String inputPath2, String inputTSV2, String outputPath, String outputTSV, 
			String t1Key, String t2Key, Constants.JoinType joinType, String t1NeededFields, String t2NeededFields, 
			Constants.SortOrder sortOrder, String substituteForNullValuesInJoin, //:the sortOrder is for sorting the records while processing. If the key fields are "integer" then it's better to select ASCENDING_INTEGER (otherwise DEFAULT_FOR_STRING).
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep
			){//Join condition: inputTSV1.T1Key = inputTSV2.T2Key
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult result = new FileManipulationResult();	
		try{ 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Joining (\"" + inputTSV1 + "\" and \"" + inputTSV2 + "\"):");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int t1KeyNumber = Constants.ERROR, t2KeyNumber = Constants.ERROR;
			String s;
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-1- Reading needed fields from \"" + inputTSV1 + "\" ...");
			//Determining needed fields from TSV1: 
			BufferedReader br1;
			br1 = new BufferedReader(new FileReader(inputPath1 + "\\" + inputTSV1)); 
			s = br1.readLine();
			String[] tSVTitles1 = s.split(Constants.TAB);
			String[] neededTitles1 = t1NeededFields.split(Constants.TAB);
			String neededFieldNumbers1 = "";
			for (int j=0; j<neededTitles1.length; j++)//:iterate over all needed titles from TSV1.
				for (int k=0; k< tSVTitles1.length; k++){//:iterate over all titles in TSV1.
					if (tSVTitles1[k].equals(neededTitles1[j]))
						if (neededFieldNumbers1.equals(""))
							neededFieldNumbers1 = Integer.toString(k);
						else
							neededFieldNumbers1 = neededFieldNumbers1 + "$" + k;//:as a field separator (to prepare a string containing all needed field numbers separated by dollar).
					if (tSVTitles1[k].equals(t1Key))
						t1KeyNumber = k;
				}//for.
			br1.close();
			if (t1KeyNumber == Constants.ERROR)
				result.errors = 1;
			//Reading needed fields from TSV1:
			FileManipulationResult fMR = new FileManipulationResult(); 
			fMR = new FileManipulationResult();
			ArrayList<String> titlesToReturn_IS_NOT_NEEDED_AND_USED = new ArrayList<String>();
			TreeMap<String, ArrayList<String[]>> t1Records = readNonUniqueKeyAndItsValueFromTSV(
					inputPath1, inputTSV1, fMR, null, t1KeyNumber, SortOrder.DEFAULT_FOR_STRING, tSVTitles1.length, neededFieldNumbers1, titlesToReturn_IS_NOT_NEEDED_AND_USED,
					LogicalOperation.NO_CONDITION, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					false, showProgressInterval, indentationLevel+2, testOrReal, writeMessageStep+"-1-1");

			//Determining needed fields from TSV2: 
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-2- Reading needed fields from \"" + inputTSV2 + "\" ...");
			BufferedReader br2;
			br2 = new BufferedReader(new FileReader(inputPath2 + "\\" + inputTSV2)); 
			s = br2.readLine();
			String[] tSVTitles2 = s.split(Constants.TAB);
			String[] neededTitles2 = t2NeededFields.split(Constants.TAB);
			String neededFieldNumbers2 = "";
			for (int j=0; j<neededTitles2.length; j++)//:iterate over all needed titles from TSV1.
				for (int k=0; k< tSVTitles2.length; k++){//:iterate over all titles in TSV1.
					if (tSVTitles2[k].equals(neededTitles2[j]))
						if (neededFieldNumbers2.equals(""))
							neededFieldNumbers2 = Integer.toString(k);
						else
							neededFieldNumbers2 = neededFieldNumbers2 + "$" + k;//:as a field separator (to prepare a string containing all needed field numbers separated by dollar).
					if (tSVTitles2[k].equals(t2Key))
						t2KeyNumber = k;
				}//for.
			br2.close();
			if (t2KeyNumber == Constants.ERROR)
				result.errors = 1;
			//Reading needed fields from TSV1:
			TreeMap<String, ArrayList<String[]>> t2Records = readNonUniqueKeyAndItsValueFromTSV(
					inputPath2, inputTSV2, fMR, null, t2KeyNumber, SortOrder.DEFAULT_FOR_STRING, tSVTitles2.length, neededFieldNumbers2, titlesToReturn_IS_NOT_NEEDED_AND_USED, 
					LogicalOperation.NO_CONDITION, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
					false, showProgressInterval, indentationLevel+2, testOrReal, writeMessageStep+"-2-1");

			//Now joining T1Records and T2Records:
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-3- Joining \"" + inputTSV1 + "\" with \"" + inputTSV2 + "\" (part 1; inner join) ...");
			Map.Entry<String, ArrayList<String[]>> entry1;
			//Inner join:
			TreeMap<String, ArrayList<String[]>> resultingRecords;
			if (sortOrder == Constants.SortOrder.ASCENDING_INTEGER){//means that keyfield is an integer.
				resultingRecords = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the ascending order of number:
						//Uncomment these lines if you have empty (or space) values:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) > Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) < Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//if.
			else{
				resultingRecords = new TreeMap<String, ArrayList<String[]>>(new Comparator<String>(){
					public int compare(String s1, String s2){//We want the descending order of number:
						//						if (s1.equals("") || s1.equals(" "))
						//							s1 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						//						if (s2.equals("") || s2.equals(" "))
						//							s2 = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
						if (Integer.parseInt(s1) < Integer.parseInt(s2))
							return 1;
						else
							if (Integer.parseInt(s1) > Integer.parseInt(s2))
								return -1;
							else
								return 0;
					}
				});
			}//else.
			int i = 0;
			int innerJoinCounter = 0;
			for(Iterator<Map.Entry<String, ArrayList<String[]>>> iter1 = t1Records.entrySet().iterator(); iter1.hasNext();){
				entry1 = iter1.next();
				boolean entry1ShouldBeDeleted = false;	
				if (t2Records.containsKey(entry1.getKey())){
					ArrayList<String[]> matchedRecordsFromT2 = t2Records.get(entry1.getKey());
					ArrayList<String[]> joinedValues = new ArrayList<String[]>();
					for (int j=0; j<entry1.getValue().size(); j++)
						for (int k=0; k<matchedRecordsFromT2.size(); k++){
							//Making records of <entry1Values[j], entry2Values[k]>
							String[] aResultingRecord = MyUtils.concatTwoStringArrays(entry1.getValue().get(j), matchedRecordsFromT2.get(k));
							joinedValues.add(aResultingRecord);
						}//for k.
					resultingRecords.put(entry1.getKey(), joinedValues);//Adding the joining key, entry1.getKey() to the start of each record, records of <entry1Values[j], entry2Values[k]> will be complete.
					t2Records.remove(entry1.getKey());
					entry1ShouldBeDeleted = true;
					innerJoinCounter++;
				}//if (t2Re....
				i++;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(i));
				if (entry1ShouldBeDeleted)
					iter1.remove();
			}//for.
			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records added in inner join: " + innerJoinCounter);
			//Left join (also part of full join):
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-4- Joining \"" + inputTSV1 + "\" with \"" + inputTSV2 + "\" (part 2; left join) ...");
			int leftJoinCounter = 0;
			i = 0;
			if (joinType == JoinType.LEFT_JOIN || joinType == JoinType.FULL_JOIN){
				for(Iterator<Map.Entry<String, ArrayList<String[]>>> iter1 = t1Records.entrySet().iterator(); iter1.hasNext();){
					entry1 = iter1.next();
					ArrayList<String[]> joinedValues = new ArrayList<String[]>();
					for (int j=0; j<entry1.getValue().size(); j++){
						//Making records of <entry1Values[j], NULL>             *Note: In fact, NULL is replaced by "".
						String[] arrayOfEmptyStrings = new String[neededTitles2.length]; //neededTitles2.length is the number of fields from t2; in fact, we need this number of NULL values.
						for (int k=0; k<neededTitles2.length; k++)
							arrayOfEmptyStrings[k] = substituteForNullValuesInJoin;
						String[] aResultingRecord = MyUtils.concatTwoStringArrays(entry1.getValue().get(j), arrayOfEmptyStrings);
						joinedValues.add(aResultingRecord);
					}//for j.
					resultingRecords.put(entry1.getKey(), joinedValues);//Adding the joining key, entry1.getKey() to the start of each record, records of <entry1Values[j], entry2Values[k]> will be complete.
					iter1.remove();
					leftJoinCounter++;
					i++;
					if (i % showProgressInterval == 0)
						System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(i));
				}//for.
			}//if.
			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records added in left join: " + leftJoinCounter);
			//Right join (also part of full join):
			int rightJoinCounter = 0;
			i = 0;
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-5- Joining \"" + inputTSV1 + "\" with \"" + inputTSV2 + "\" (part 3; right join) ...");
			if (joinType == JoinType.RIGHT_JOIN || joinType == JoinType.FULL_JOIN){
				for(Iterator<Map.Entry<String, ArrayList<String[]>>> iter2 = t2Records.entrySet().iterator(); iter2.hasNext();){
					Map.Entry<String, ArrayList<String[]>> entry2 = iter2.next();
					ArrayList<String[]> joinedValues = new ArrayList<String[]>();
					for (int j=0; j<entry2.getValue().size(); j++){
						//Making records of <NULL, entry1Values[j]>             *Note: In fact, NULL is replaced by "".
						String[] arrayOfEmptyStrings = new String[neededTitles1.length]; //neededTitles1.length is the number of fields from t1; in fact, we need this number of NULL values.
						for (int k=0; k<neededTitles1.length; k++)
							arrayOfEmptyStrings[k] = substituteForNullValuesInJoin;
						String[] aResultingRecord = MyUtils.concatTwoStringArrays(arrayOfEmptyStrings, entry2.getValue().get(j));
						joinedValues.add(aResultingRecord);
					}//for j.
					resultingRecords.put(entry2.getKey(), joinedValues);//Adding the joining key, entry2.getKey() to the start of each record, records of <entry1Values[j], entry2Values[k]> will be complete.
					iter2.remove();
					rightJoinCounter++;
					i++;
					if (i % showProgressInterval == 0)
						System.out.println(MyUtils.indent(indentationLevel+2) + Constants.integerFormatter.format(i));
				}//for.
			}//if.
			System.out.println(MyUtils.indent(indentationLevel+2) + "Number of records added in right join: " + rightJoinCounter);
			//Finally, saving the results in the output file:
			System.out.println(MyUtils.indent(indentationLevel+1) + writeMessageStep + "-6- Saving the results");
			String titles = t1NeededFields + Constants.TAB + t2NeededFields;
			saveTreeMapToTSVFile(outputPath, outputTSV, resultingRecords, titles, false, -1,
					false, showProgressInterval, indentationLevel+2, testOrReal, writeMessageStep+"-6-1");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			if (fMR.errors > 0)
				result.doneSuccessfully = 0;
			else
				result.doneSuccessfully = 1;
		}catch(Exception e){
			result.errors = 1;
			result.doneSuccessfully = 0;
			e.printStackTrace();
		}
		result.processed=1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return result;
	}//joinTwoTSV().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult joinTwoTSVsWithIdenticalIDs(String inputPath1, String inputTSV1, String inputPath2, String inputTSV2, String outputPath, String outputTSV, 
			String t1Key, String t2Key, FieldType keyFieldType, Constants.JoinType joinType, ArrayList<String> t1NeededFields, ArrayList<String> t2NeededFields, 
			String substituteForNullValuesInJoin, 
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep
			){//This method joins two files with the same number of rows, and the same id's for each line.
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult result = new FileManipulationResult();	
		try{ 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Joining [with identical ID's] (\"" + inputTSV1 + "\" and \"" + inputTSV2 + "\"):");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			int t1KeyNumber = Constants.ERROR, t2KeyNumber = Constants.ERROR;

			//Determining needed fields from TSV1: 
			BufferedReader br1;
			br1 = new BufferedReader(new FileReader(inputPath1 + "\\" + inputTSV1)); 
			String s1 = br1.readLine();
			String[] tSVTitles1 = s1.split(Constants.TAB);
			HashSet<Integer> neededFieldNumbers1 = new HashSet<Integer>();
			for (int j=0; j<t1NeededFields.size(); j++)//: iterate over all needed titles from TSV1.
				for (int k=0; k< tSVTitles1.length; k++){//: iterate over all titles currently in TSV1 file.
					if (tSVTitles1[k].equals(t1NeededFields.get(j)))
						neededFieldNumbers1.add(k);
					if (tSVTitles1[k].equals(t1Key))
						t1KeyNumber = k;
				}//for.
			if (t1KeyNumber == Constants.ERROR)
				result.errors = 1;

			//Determining needed fields from TSV2: 
			BufferedReader br2;
			br2 = new BufferedReader(new FileReader(inputPath2 + "\\" + inputTSV2)); 
			String s2 = br2.readLine();
			String[] tSVTitles2 = s2.split(Constants.TAB);
			HashSet<Integer> neededFieldNumbers2 = new HashSet<Integer>();
			for (int j=0; j<t2NeededFields.size(); j++)//: iterate over all needed titles from TSV1.
				for (int k=0; k< tSVTitles2.length; k++){//: iterate over all titles currently in TSV1 file.
					if (tSVTitles2[k].equals(t2NeededFields.get(j)))
						neededFieldNumbers2.add(k);
					if (tSVTitles2[k].equals(t2Key))
						t2KeyNumber = k;
				}//for.
			if (t2KeyNumber == Constants.ERROR)
				result.errors = 1;

			FileWriter writer = new FileWriter(outputPath + "\\" + outputTSV);
			int i = 0, numberOfRecordsFromT1Side_FilledWithNULL = 0, numberOfRecordsFromT2Side_FilledWithNULL = 0;
			String[] fields1, fields2;
			String newGeneratedLine;
			while ((result.errors==0) && (s1!=null) && (s2!=null)){//: s1 and s2 are initialized with the titles.
				fields1 = s1.split(Constants.TAB);
				fields2 = s2.split(Constants.TAB);
				newGeneratedLine = "";
				String t1keyValueInALine = fields1[t1KeyNumber];
				String t2keyValueInALine = fields2[t2KeyNumber];
				int whichKeyValueIsGreater = 0; //1: t1 is greater.      2: t1 is greater.     0: they are equal.
				if (i == 0) //: i.e., if we are reading the header. 
					whichKeyValueIsGreater = 0; //because we're reading the header, and, the value of this variable is needed when we're checking the records against each oter, not the headers.
				else
					if (keyFieldType == FieldType.LONG){
						long t1LongKeyValueInALine = Long.parseLong(t1keyValueInALine);
						long t2LongKeyValueInALine = Long.parseLong(t2keyValueInALine);
						if (t1LongKeyValueInALine > t2LongKeyValueInALine)
							whichKeyValueIsGreater = 1;
						else
							if (t1LongKeyValueInALine < t2LongKeyValueInALine)
								whichKeyValueIsGreater = 2;
							else
								whichKeyValueIsGreater = 0;
					}//if (keyF....
					else
						if (keyFieldType == FieldType.STRING){
							if (t1keyValueInALine.compareTo(t2keyValueInALine) > 0) //: i.e., t1keyValueInALine>t2keyValueInALine 
								whichKeyValueIsGreater = 1;
							else
								if (t1keyValueInALine.compareTo(t2keyValueInALine) < 0)
									whichKeyValueIsGreater = 2;
								else
									whichKeyValueIsGreater = 0;
						}//if (keyF....
						else{
							MyUtils.println("Error in join: The key field should be Long or String!", indentationLevel);
							result.errors = 1;
						}
				switch (whichKeyValueIsGreater){
				case 0: //: means that the PK of the records match.
					//First, adding the needed fields from first file:
					for (int j=0; j<fields1.length; j++)
						if (neededFieldNumbers1.contains(j))
							if (newGeneratedLine.equals(""))
								newGeneratedLine = fields1[j];
							else
								newGeneratedLine = newGeneratedLine + Constants.TAB + fields1[j];
					//Then adding the needed fields from second file:
					for (int k=0; k<fields2.length; k++)
						if (neededFieldNumbers2.contains(k))
							if (newGeneratedLine.equals(""))
								newGeneratedLine = fields2[k];
							else
								newGeneratedLine = newGeneratedLine + Constants.TAB + fields2[k];
					s1 = br1.readLine();
					s2 = br2.readLine();
					break;//case 0.
				case 1: //: means that the PK for the t1 is greater. So the record from t1 is missing and should be replaced by NULL. 
					if (joinType == JoinType.RIGHT_JOIN || joinType == JoinType.FULL_JOIN){
						//First, adding NULL values from first file:
						for (int j=0; j<fields1.length; j++)
							if (neededFieldNumbers1.contains(j))
								if (newGeneratedLine.equals(""))
									newGeneratedLine = substituteForNullValuesInJoin;
								else
									newGeneratedLine = newGeneratedLine + Constants.TAB + substituteForNullValuesInJoin;
						numberOfRecordsFromT1Side_FilledWithNULL++;
						//Then adding the needed fields from second file:
						for (int k=0; k<fields2.length; k++)
							if (neededFieldNumbers2.contains(k))
								if (newGeneratedLine.equals(""))
									newGeneratedLine = fields2[k];
								else
									newGeneratedLine = newGeneratedLine + Constants.TAB + fields2[k];
					}//if (joinT....
					s2 = br2.readLine();
					break;//case 1.
				case 2: //: means that the PK for the t2 is greater. So the record from t2 is missing and should be replaced by NULL.
					if (joinType == JoinType.LEFT_JOIN || joinType == JoinType.FULL_JOIN){
						//First, adding the needed fields from first file:
						for (int j=0; j<fields1.length; j++)
							if (neededFieldNumbers1.contains(j))
								if (newGeneratedLine.equals(""))
									newGeneratedLine = fields1[j];
								else
									newGeneratedLine = newGeneratedLine + Constants.TAB + fields1[j];
						//Then adding NULL values from second file:
						for (int k=0; k<fields2.length; k++)
							if (neededFieldNumbers2.contains(k))
								if (newGeneratedLine.equals(""))
									newGeneratedLine = substituteForNullValuesInJoin;
								else
									newGeneratedLine = newGeneratedLine + Constants.TAB + substituteForNullValuesInJoin;
						numberOfRecordsFromT2Side_FilledWithNULL++;
					}//if (joinT....
					s1 = br1.readLine();
					break;//case 2.
				}//switch (whichK....				

				if (!newGeneratedLine.equals("")){
					newGeneratedLine = newGeneratedLine + "\n";
					writer.append(newGeneratedLine);
				}//if (!newGe....

				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while

			writer.flush();    writer.close();
			br1.close();
			br2.close();

			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			if (numberOfRecordsFromT1Side_FilledWithNULL > 0)
				MyUtils.println("Warning: Number of records from T1 side filled with NULL: " + numberOfRecordsFromT1Side_FilledWithNULL, indentationLevel);
			if (numberOfRecordsFromT2Side_FilledWithNULL > 0)
				MyUtils.println("Warning: Number of records from T2 side filled with NULL: " + numberOfRecordsFromT2Side_FilledWithNULL, indentationLevel);
			if (result.errors > 0){
				result.doneSuccessfully = 0;
				MyUtils.println("Error in join!", indentationLevel);
			}
			else
				result.doneSuccessfully = 1;
		}catch(Exception e){
			MyUtils.println("Error in join (an exception occured)!", indentationLevel);
			result.errors = 1;
			result.doneSuccessfully = 0;
			e.printStackTrace();
		}
		result.processed=1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return result;
	}//joinTwoTSVsWithIdenticalIDs().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult joinSeveralTSVsWithIdenticalIDs(String iOPath, ArrayList<String> inputTSVs, String outputTSVFileName, 
			ArrayList<String> keysToJoin, ArrayList<ArrayList<String>> neededFields,  
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep
			){//This method joins several files with the same number of rows, and the same id's for each line of each file.
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		FileManipulationResult result = new FileManipulationResult();	
		try{ 
			System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Joining [with identical ID's] " + inputTSVs.size() + " files:");
			System.out.println(MyUtils.indent(indentationLevel+1) + "Started ...");
			ArrayList<Integer> keyNumbers = new ArrayList<Integer>();
			for (int p=0; p<inputTSVs.size(); p++)
				keyNumbers.add(Constants.ERROR);

			//Determining needed fields from all TSV files: 
			ArrayList<BufferedReader> bRAL = new ArrayList<BufferedReader>();
			ArrayList<String> s = new ArrayList<String>();//: This is for every line which is being read.
			ArrayList<HashSet<Integer>> neededFieldNumbers = new ArrayList<HashSet<Integer>>(); 
			for (int p=0; p<inputTSVs.size(); p++){
				String fileName = inputTSVs.get(p);
				BufferedReader aBR = new BufferedReader(new FileReader(iOPath + "\\" + fileName)); 
				bRAL.add(aBR);
				s.add(aBR.readLine());
				ArrayList<String[]> tSVTitles = new ArrayList<String[]>();
				String[] titlesOfAFile = s.get(p).split(Constants.TAB);
				tSVTitles.add(titlesOfAFile);
				HashSet<Integer> neededFieldNumbersForAFile = new HashSet<Integer>();
				ArrayList<String> neededFieldsForAFile = neededFields.get(p);
				int numberOfNeededTitlesInAFileWhichAreVerified = 0;
				for (int j=0; j<neededFieldsForAFile.size(); j++)//: iterate over all needed titles from TSV #p (given through parameters).
					for (int k=0; k< titlesOfAFile.length; k++){//: iterate over all titles currently in TSV #p file (read from file).
						if (titlesOfAFile[k].equals(neededFieldsForAFile.get(j))){
							neededFieldNumbersForAFile.add(k);
							numberOfNeededTitlesInAFileWhichAreVerified++;
						}//if (titlesO....
						if (titlesOfAFile[k].equals(keysToJoin.get(p)))
							keyNumbers.set(p, k);
					}//for.
				if (keyNumbers.get(p) == Constants.ERROR){//: means that if the keyNumber hasn't set in the above loop.
					result.errors = 1;
					MyUtils.println("Error: Key field not found.", indentationLevel);
				}
				if (neededFieldsForAFile.size() != numberOfNeededTitlesInAFileWhichAreVerified){//: means that the number of titles available is not the same as the number of titles to be read.
					result.errors = 1;
					MyUtils.println("Error in number of fields in \"" + fileName + "\"!", indentationLevel+1);
				}
				neededFieldNumbers.add(neededFieldNumbersForAFile);
			}//for (p.

			FileWriter writer = new FileWriter(iOPath + "\\" + outputTSVFileName);
			int i = 0;
			//			ArrayList<String[]> fields = new ArrayList<String[]>();
			String newGeneratedLine;
			boolean anyOfTheLinesIsEmpty = false;
			while ((result.errors==0) && (!anyOfTheLinesIsEmpty)){//: s is initialized with the titles before entering the loop.
				newGeneratedLine = "";
				String valueOfKeyFieldInFirstFile = "";
				for (int p=0; p<inputTSVs.size(); p++){
					String[] fields = s.get(p).split(Constants.TAB);
					for (int j=0; j<fields.length; j++)
						if (neededFieldNumbers.get(p).contains(j))
							if (newGeneratedLine.equals(""))
								newGeneratedLine = fields[j];
							else
								newGeneratedLine = newGeneratedLine + Constants.TAB + fields[j];
					if (p==0)
						valueOfKeyFieldInFirstFile = fields[keyNumbers.get(p)];
					else{
						String valueOfKeyFieldInCurrentFile = fields[keyNumbers.get(p)];
						if (!valueOfKeyFieldInFirstFile.equals(valueOfKeyFieldInCurrentFile)){
							result.errors = 1;
							MyUtils.println("Error in join: The value of key does not match!", indentationLevel+1);
						}
					}//else.
				}//for (p.
				newGeneratedLine = newGeneratedLine + "\n";
				writer.append(newGeneratedLine);

				s.clear();
				for (int p=0; p<inputTSVs.size(); p++){
					BufferedReader aBR = bRAL.get(p);
					String tempString = aBR.readLine();
					if (tempString == null)
						anyOfTheLinesIsEmpty = true;
					else
						s.add(tempString);
				}//for (p.

				i++;
				if (testOrReal > Constants.THIS_IS_REAL)
					if (i >= testOrReal)
						break;
				if (i % showProgressInterval == 0)
					System.out.println(MyUtils.indent(indentationLevel+1) + Constants.integerFormatter.format(i));
			}//while

			writer.flush();    writer.close();
			for (int p=0; p<inputTSVs.size(); p++){
				BufferedReader aBR = bRAL.get(p);
				aBR.close();
			}//for (p.

			System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
			if (result.errors > 0){
				result.doneSuccessfully = 0;
				MyUtils.println("Error in join!", indentationLevel+1);
			}
			else
				result.doneSuccessfully = 1;
		}catch(Exception e){
			result.errors = 1;
			result.doneSuccessfully = 0;
			e.printStackTrace();
		}
		result.processed=1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return result;
	}//joinTwoTSVsWithIdenticalIDs().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//Note that in this method, the first TSV file should be inclusive of all id's. Otherwise since it joins the first two files, and then the rest of the files one by one, it may lead to unwanted results:
	//Also only one field from each table will be kept (neededFields).
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult joinSeveralTSVs(String iOPath, ArrayList<String> inputTSV, String outputTSVFileName, 
			ArrayList<String> keysToJoin, Constants.JoinType joinType, ArrayList<String> neededFields, 
			Constants.SortOrder sortOrder, String substituteForNullValuesInJoin, //:the sortOrder is for sorting the records while processing. If the key fields are "integer" then it's better to select ASCENDING_INTEGER (otherwise DEFAULT_FOR_STRING).
			boolean wrapOutputInLines, int indentationLevel, long testOrReal, int showProgressInterval, String writeMessageStep
			){//Join condition: inputTSV1.T1Key = inputTSV2.T2Key
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		int numberOfTSVFiles = inputTSV.size();
		MyUtils.println(writeMessageStep + "- Joining " + numberOfTSVFiles + " files (in " + (numberOfTSVFiles-1) + " steps):", indentationLevel);
		FileManipulationResult totalFMR = new FileManipulationResult(), fMR;
		try{ 
			//Handle files: These are the handles for the first file (#0). Then, in a loop, we join this file with the file number "i" (from 1 to n-1), and store the results in the same handle variables:
			String handlingInputFileName = "handlingInput.tsv",   
					handlingKeyToJoin = keysToJoin.get(0), //this field is fixed and transfered to the new file produced each time.
					handlingNeededFields = neededFields.get(0), 
					handlingTemporaryOutputTSV = "temporaryOutput.tsv";		
			MyUtils.copyFile(iOPath, inputTSV.get(0), iOPath, handlingInputFileName, indentationLevel+1, writeMessageStep+"-1");
			MyUtils.println(writeMessageStep+"-2- Joins", indentationLevel+1);
			for (int i=1; i<numberOfTSVFiles; i++){
				//join handle file with file #i, and, put the results back in the handle (to be looked in the next iteration):
				MyUtils.println(writeMessageStep+"-2-"+i+"- Iterating over "+(numberOfTSVFiles-1)+" files: #"+i, indentationLevel+2);
				//					if (i == 2)
				//						JOptionPane.showMessageDialog(null, "Stop here!", "java", JOptionPane.PLAIN_MESSAGE);
				fMR = TSVManipulations.joinTwoTSVs(iOPath, handlingInputFileName, iOPath, inputTSV.get(i), iOPath, handlingTemporaryOutputTSV, 
						handlingKeyToJoin, keysToJoin.get(i), joinType, 
						handlingNeededFields, neededFields.get(i), SortOrder.ASCENDING_INTEGER, substituteForNullValuesInJoin, 
						false, indentationLevel+3, testOrReal, showProgressInterval, writeMessageStep+"-2-"+i+"-1");
				totalFMR = MyUtils.addFileManipulationResults(totalFMR, fMR);
				MyUtils.deleteTemporaryFiles(iOPath, new String[]{handlingInputFileName}, true, indentationLevel+3, writeMessageStep+"-2-"+i+"-2");
				MyUtils.renameFile(iOPath, handlingTemporaryOutputTSV, iOPath, handlingInputFileName, indentationLevel+3, writeMessageStep+"-2-"+i+"-3");
				handlingNeededFields = concatTwoSetsOfFields(handlingNeededFields, neededFields.get(i));
			}//for.
			//Delete the file outputTSVFileName, if exists (which can be result of another running of the program):
			MyUtils.deleteTemporaryFiles(iOPath, new String[]{outputTSVFileName}, false, indentationLevel+1, writeMessageStep+"-3");
			MyUtils.renameFile(iOPath, handlingInputFileName, iOPath, outputTSVFileName, indentationLevel+1, writeMessageStep+"-4");
			totalFMR.doneSuccessfully = 1;
		}catch(Exception e){
			e.printStackTrace();
			totalFMR.errors = 1;
		}
		System.out.println(MyUtils.indent(indentationLevel+1) + "Finished.");
		totalFMR.processed=1;
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		return totalFMR;
	}//joinSeveralTSVs().
	//--------------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static String concatTwoSetsOfFields(String fields1, String fields2){
		if (fields1 == null || fields1.equals(""))
			return fields2;
		if (fields2 == null || fields2.equals(""))
			return fields1;
		return fields1 + Constants.TAB + fields2;
	}//concatTwoSetsOfFields().
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static HashSet<String> readAndCombineTwoUniqueFieldsFromTSV(String inputPath, String inputFileName, FileManipulationResult fMR, 
			int totalFieldsCount, int field1Number, int field2Number, String separatorBetweenField1And2,  
			boolean wrapOutputInLines, int showProgressInterval, int indentationLevel, 
			long testOrReal, String writeMessageStep){//This method reads two fields separated by Constants.SEPARATOR_FOR_ARRAY_ITEMS
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		MyUtils.println(writeMessageStep + "- Reading file \"" + inputFileName + "\"into a HashMap:", indentationLevel);
		MyUtils.println("Started ...", indentationLevel+1);
		int i = 0;
		int linesWithError = 0;
		HashSet<String> result = new HashSet<String>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(inputPath + "\\" + inputFileName)); 
			String s = br.readLine(); //Skip the title line.
			while((s = br.readLine()) != null) {
				String[] fields = s.split(Constants.TAB);
				if (fields.length == totalFieldsCount){
					String projectId = fields[field1Number];
					String prNumber = fields[field2Number];
					String twoFieldsConcattedWithASeparator = projectId+separatorBetweenField1And2+prNumber;
					if (result.contains(twoFieldsConcattedWithASeparator))
						linesWithError++;
					else
						result.add(twoFieldsConcattedWithASeparator);
					i++;
					if (i % showProgressInterval == 0)
						MyUtils.println(Constants.integerFormatter.format(i), indentationLevel+1);
					if (testOrReal > Constants.THIS_IS_REAL)
						if (i >= testOrReal)
							break;
				}
				else
					linesWithError++;
			}
			br.close();		
			fMR.doneSuccessfully++;
		}
		catch (Exception e){
			e.printStackTrace();
			fMR.doneSuccessfully--;
			fMR.errors++;
		}

		MyUtils.println("Number of records read: " + Constants.integerFormatter.format(i), indentationLevel+1);
		if (linesWithError > 0)
			MyUtils.println("Finished with " + Constants.integerFormatter.format(linesWithError) + " errors.", indentationLevel+1);
		else
			MyUtils.println("Finished.", indentationLevel+1);
		if (wrapOutputInLines)
			MyUtils.println("-----------------------------------", indentationLevel);
		fMR.processed++;
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------------------------------------------------------
	public static void main(String[] args) { 
		//testing:
		//		String iOPath = "C:\\2-Study\\Influentials\\Data Set\\MySQL-20150401\\TSV\\LanguagesStudy";
		//		TSVManipulations.joinTwoTSVs(iOPath, "test1.tsv", iOPath, "test2.tsv", iOPath, "tempOut.tsv", 
		//				"id", "userId", JoinType.FULL_JOIN, 
		//				"id", "numberOfWatchersOfProjectsOfThisUser_1_JavaScript", SortOrder.ASCENDING_INTEGER, "0", 
		//				false, 1, Constants.THIS_IS_REAL, 1000, "1");

		//		ArrayList<String> fileNamesToJoin = new ArrayList<String>();
		//		fileNamesToJoin.add("users.tsv");
		//		fileNamesToJoin.add("usersAndNumberOfUsersWatchingTheirProjects1_JavaScript.tsv");
		//		fileNamesToJoin.add("usersAndNumberOfUsersWatchingTheirProjects2_Ruby.tsv");
		//		fileNamesToJoin.add("usersAndNumberOfUsersWatchingTheirProjects3_Python.tsv");
		//		
		//		ArrayList<String> keysToJoin = new ArrayList<String>();
		//		keysToJoin.add("id");
		//		keysToJoin.add("userId");
		//		keysToJoin.add("userId");
		//		keysToJoin.add("userId");
		//
		//		ArrayList<String> neededFields = new ArrayList<String>();
		//		keysToJoin.add("id\tlogin");
		//		keysToJoin.add("numberOfWatchersOfProjectsOfThisUser_1_JavaScript");
		//		keysToJoin.add("numberOfWatchersOfProjectsOfThisUser_2_Ruby");
		//		keysToJoin.add("numberOfWatchersOfProjectsOfThisUser_3_Python");
		//
		//		joinSeveralTSVs(iOPath, fileNamesToJoin, "Out.tsv", keysToJoin, JoinType.FULL_JOIN, neededFields, 
		//			SortOrder.ASCENDING_INTEGER, "0", true, 1, Constants.THIS_IS_REAL, 10000, "1");

		//		joinSeveralTSVs(Constants.DATASET_DIRECTORY_GH_TSV__LANGUAGE_STUDY, 4, 
		//				new String[]{"users.tsv", "usersAndNumberOfUsersWatchingTheirProjects1_JavaScript.tsv", "usersAndNumberOfUsersWatchingTheirProjects2_Ruby.tsv", "usersAndNumberOfUsersWatchingTheirProjects3_Python.tsv"}, 
		//				"Out.tsv", new String[]{"id", "userId", "userId", "userId"}, JoinType.FULL_JOIN, 
		//				new String[]{"id", "numberOfWatchersOfProjectsOfThisUser_1_JavaScript", "numberOfWatchersOfProjectsOfThisUser_2_Ruby", "numberOfWatchersOfProjectsOfThisUser_3_Python"}, 
		//				SortOrder.ASCENDING_INTEGER, "-1", true, 1, Constants.THIS_IS_REAL, 10000, "1");
		//		MyUtils.renameFile("C:\\2-Study", "alaki.txt", "C:\\2-Study", "alaki2.txt", 1);

		//		//Was not run yet: 
		//		mergeTwoTSVFieldsTogether(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "repos.tsv",
		//				Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "repos-mergedTwoColumns.tsv",
		//				1, 2, "/", 6, 
		//				500000,
		//				Constants.THIS_IS_REAL, 1);

		//		//Was not run yet: 
		//				mergeTwoTSVFieldsTogether(Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned.tsv",
		//						Constants.DATASET_DIRECTORY_GH_MongoDB_TSV, "issues-Assigned-mergedTwoColumns.tsv",
		//						1, 2, "/", 12, 
		//						500000,
		//						Constants.THIS_IS_REAL, 1);

		//		//Was not run yet: 
		//				replaceForeignKeyInTSVWithValueFromAnotherTSV(Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2 - Cleaned.tsv",
		//						Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "users3 (only important fields)-fixed.tsv", 
		//						Constants.DATASET_DIRECTORY_GH_MySQL_TSV, "projects2-Cleaned-ownerIdReplacedWithLogin.tsv",
		//						2, 10,
		//						0, 3, 
		//						1,
		//						"ownerLogin",
		//						500000,
		//						Constants.THIS_IS_REAL, 1);

	}//main().
}//Class.
