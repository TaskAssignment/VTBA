package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Constants.ConditionType;
import utils.Constants.FieldType;
import utils.Constants.LogicalOperation;

public class MyUtils {
	//----------------------------------------------------------------------------------------------------------------
	public static String moveTheBufferedReaderCursorToTheLineAfter(BufferedReader br, String startingString){
		String s;
		try{
		while( ((s = br.readLine()) != null) && (! s.startsWith(startingString)) ){
			//do nothing.
		} //while.
		return s;
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}
	//----------------------------------------------------------------------------------------------------------------
	public static String indent(int indentationLevel){
		String s = "", ss = "";
		for (int i=0; i<Constants.NUMBER_OF_TAB_CHARACTERS; i++)
			s = s + " ";
		for (int i=0; i<indentationLevel; i++)
			ss = ss + s;
		return ss;
	}
	public static void println(String s, int indentationLevel){
		System.out.println(indent(indentationLevel) + s);
	}
	//----------------------------------------------------------------------------------------------------------------
	public static int indexOf_ifExists_LengthIfDoNotExist(String s1, String s2, int startingIndex){
		int result = 0;
		if (s1 != null){
			result = s1.indexOf(s2, startingIndex);
			if (result < 0) //: it means that there is no s2 (",") up to the end of the string, which means that we are at the end of the line and have an extra character (";").
				result = s1.length()-2;
		}
		return result;
	}
	//----------------------------------------------------------------------------------------------------------------
	public static String removeExtraCharactersFromTheEndOfRecord(String tabSeparatedRecord){//Removes ")\t" or ");\t" from the end (if there is).
		if (tabSeparatedRecord.endsWith(")\t"))
			return tabSeparatedRecord.substring(0, tabSeparatedRecord.length()-2);
		else
			if (tabSeparatedRecord.endsWith(");\t"))
				return tabSeparatedRecord.substring(0, tabSeparatedRecord.length()-3);
			else
				return tabSeparatedRecord;
	}
	//----------------------------------------------------------------------------------------------------------------
	public static String removeFromEnd(String s, int num){
		return s.substring(0, s.length()-num);
	}
	//----------------------------------------------------------------------------------------------------------------
	public static String applyRegexOnString(String regex, String value){
		Pattern pattern = Pattern.compile("[^"+regex+"]+");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find())
			value = value.replaceAll("[^"+regex+"]+", " ");
		if (value.equals(""))
			value = " ";
		return value;
	}
	//----------------------------------------------------------------------------------------------------------------
	public static boolean compareTwoStringsBasedOnconditionType(String valueA, ConditionType conditionType, String valueB, FieldType fieldType){
		boolean result;
		switch (conditionType){
			case EQUALS:
				result = valueA.equals(valueB);
				break;
			case NOT_EQUALS:
				result = !valueA.equals(valueB);
				break;
			case GREATER_OR_EQUAL:
				switch(fieldType){
				case LONG:
					if (valueA.equals(" ") || valueA.equals(""))
						valueA = Long.toString(Constants.AN_EXTREMELY_NEGATIVE_LONG);
//					if (valueB.equals("") || valueB.equals(" "))
//						valueB = Integer.toString(Constants.AN_EXTREMELY_NEGATIVE_INT);
					result = (Integer.parseInt(valueA) >= Integer.parseInt(valueB));
					break;
				case STRING:
					result = valueA.compareTo(valueB) >= 0; //if result of str1.compareTo(str2) is positive, then str1 > str2
					break;
				default:
					result = true;
					System.out.println("Warning: You are comparing two fields with GREATER_OR_EQUAL, but with NOT_IMPORTANT fieldType!");
					break;
				}//switch.
				break;
			default:
				result = false;
				break;
		}//switch.
		return result;
	}//compareTwoStringsBasedOnconditionType().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static boolean runLogicalComparison(LogicalOperation logicalOperation, 
			String value1A, ConditionType condition1Type, String value1B, FieldType field1Type, 
			String value2A, ConditionType condition2Type, String value2B, FieldType field2Type){
		boolean result = false;
		if (logicalOperation == LogicalOperation.NO_CONDITION)
			result = true;
		else{
			boolean resultOfCondition1 = compareTwoStringsBasedOnconditionType(value1A, condition1Type, value1B, field1Type);
			if (resultOfCondition1)
				if (logicalOperation == LogicalOperation.AND)
					result = compareTwoStringsBasedOnconditionType(value2A, condition2Type, value2B, field2Type);
				else
					result = true; //OR or IGNORE_THE_SECOND_OPERAND
			else //i.e., when resultOfCondition1 is not true:
				if (logicalOperation == LogicalOperation.OR)
					result = compareTwoStringsBasedOnconditionType(value2A, condition2Type, value2B, field2Type);
				else
					result = false;
		}//else.
		return result;
	}//runLogicalComparison().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult addFileManipulationResults(FileManipulationResult fCR1, FileManipulationResult fCR2){
		FileManipulationResult result = new FileManipulationResult();
		result.doneSuccessfully = fCR1.doneSuccessfully + fCR2.doneSuccessfully;
		result.processed = fCR1.processed + fCR2.processed;
		result.errors = fCR1.errors + fCR2.errors;
		return result;
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static String[] concatTwoStringArrays(String[] s1, String[] s2){
		String[] result = new String[s1.length+s2.length];
		int i;
		for (i=0; i<s1.length; i++)
			result[i] = s1[i];
		int tempIndex =s1.length; 
		for (i=0; i<s2.length; i++)
			result[tempIndex+i] = s2[i];
		return result;
	}//concatTwoStringArrays().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult deleteTemporaryFiles(String path, String[] temporaryFilesToBeDeleted, boolean showErrorMessageIfAFileDoesNotExist, int indentationLevel, String writeMessageStep){
		System.out.println(MyUtils.indent(indentationLevel) + writeMessageStep + "- Deleting the temporary files ...");
		FileManipulationResult result = new FileManipulationResult();
		int numberOfTemporaryFilesDeleted = 0;
		for (int i=0; i<temporaryFilesToBeDeleted.length; i++){
			File file = new File(path+"\\"+temporaryFilesToBeDeleted[i]);
			if (file.exists()){
				file.delete();
				numberOfTemporaryFilesDeleted++;
			}//if.
			else{
				result.errors++;
				if (showErrorMessageIfAFileDoesNotExist)
					System.out.println("Error: Cannot find file \"" + temporaryFilesToBeDeleted[i] + "\" to delete it!" );
			}
		}//for.
		System.out.println(MyUtils.indent(indentationLevel+1) + "Number of temporary files deleted: " + numberOfTemporaryFilesDeleted + " / " + temporaryFilesToBeDeleted.length);
		return result;
	}//deleteTemporaryFiles(....
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult copyFile(String inputPath, String inputFileName, String outputPath, String outputFileName, int indentationLevel, String writeMessageStep) {
		FileManipulationResult result = new FileManipulationResult();
		MyUtils.println(writeMessageStep + "- Copying file \"" + inputFileName + "\" to \"" + outputFileName + "\"", indentationLevel);
		File source = new File(inputPath+"\\"+inputFileName);
		File destination = new File(outputPath+"\\"+outputFileName);
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        FileInputStream fisIn = null;
        FileOutputStream fisOut = null; 
        try {
        	//Old way (gave warning: "Resource leak: '<unassigned Closeable value>' is never closed"):
//            inputChannel = new FileInputStream(source).getChannel();
//            outputChannel = new FileOutputStream(destination).getChannel();
        	//New way (warning is gone!):
        	fisIn = new FileInputStream(source);
        	inputChannel = fisIn.getChannel();
        	fisOut = new FileOutputStream(destination);
        	outputChannel = fisOut.getChannel();
        	
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            result.doneSuccessfully = 1;
            result.errors = 0;
    		MyUtils.println("Copied successfully.", indentationLevel+1);
        }
        catch (Exception e){
            result.doneSuccessfully = 0;
            result.errors = 1;
        	MyUtils.println("Error in copy.", indentationLevel);        }
        finally {
        	result.processed = 1;
        	try{
        		inputChannel.close();
        		outputChannel.close();
        		
        		fisIn.close();
        		fisOut.close();
        	}
        	catch(Exception e){
        		MyUtils.println("Error in closing i/o channel!", indentationLevel);
        	}
        }
        return result;
	}//copyFile().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static FileManipulationResult renameFile(String inputPath, String inputFileName, String outputPath, String outputFileName, int indentationLevel, String writeMessageStep){
		MyUtils.println(writeMessageStep + "- Renaming file \"" + inputFileName + "\" to \"" + outputFileName + "\"", indentationLevel);
		FileManipulationResult result = new FileManipulationResult();
		File oldfile = new File(inputPath+"\\"+inputFileName);
		File newfile = new File(outputPath+"\\"+outputFileName);
		if(!oldfile.renameTo(newfile)){
			result.errors++;
        	MyUtils.println("Error in rename.", indentationLevel);        
		}
		return result;
	}//renameFile().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static void createFolderIfDoesNotExist(String pathAndFolderName, 
			FileManipulationResult fMR, int indentationLevel, String writeMessageStep){
		File file = new File(pathAndFolderName);
		if (!file.exists()) {
		    MyUtils.println(concatTwoWriteMessageSteps(writeMessageStep, "Creating directory \"" + pathAndFolderName + "\" if it does not exist ...."), indentationLevel);
		    try{
		    	file.mkdir();
			    MyUtils.println("Folder created.", indentationLevel+1);
		        fMR.doneSuccessfully = 1;
		        fMR.errors = 0;
		    } 
		    catch(Exception e){
			    MyUtils.println("Exception in creating folder \"" + pathAndFolderName + "\".", indentationLevel+1);
		    	fMR.errors++;
		        fMR.doneSuccessfully = 0;
		    	e.printStackTrace();
		    }        
		}
		else
		    MyUtils.println("Folder currently exists. No need to re-create it.", indentationLevel+1);
	}//createFolderIfDoesNotExist(....
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static String convertArrayListOfStringToDelimiterSeparatedString(ArrayList<String> aL){
		String result = aL.get(0);
		for (int i=1; i<aL.size(); i++)
			result = result + Constants.TAB + aL.get(i);
		return result;
	}//convertArrayListOfStringToDelimiterSeparatedString().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static String concatTwoWriteMessageSteps(String prefix1, String prefix2){
		//This is for concatenating two message prefixes (like 1-1 and 3 to be 1-1-3):
		if (prefix1 == null || prefix1.equals(""))
			return prefix2;
		else
			if (prefix2 == null || prefix2.equals(""))
				return prefix1;
			else
				return prefix1 + "-" + prefix2;
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------
    public static int specialBinarySearch(ArrayList<Integer> a, int key) {
    	//This is the test method to be used from Constants. The useful method is specialBinarySearch().
    	//This method returns the index of the key if it exists in the ArrayList, otherwise the index of the key before that. If the key is smaller than the first item, then returns -1. 
    		//This return value is then incremented by one to make seqNum. 
				//In case that the key is smaller than the first item, then the returning value (-1) will be added by one and will make 0. There is no "0" sequence number, but the fact is that we need the difference between two seqNum's (of the bugAssignment and the evidence) should be positive (non-zero) integer [it will be taken parted at the denominator for the recency score calculation].
        int lo = 0;
        int hi = a.size() - 1;
        int mid = 0;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            mid = lo + (hi - lo) / 2;
            if (key < a.get(mid)) 
            	hi = mid - 1;
            else 
            	if (key > a.get(mid)) 
            		lo = mid + 1;
            else 
            	return mid;
        }
//        if (key < a.get(0))
//        	return -1;
//        else
        	return mid-1;
    }
	//------------------------------------------------------------------------------------------------------------------------------------------------
    public static int specialBinarySearch2(ArrayList<String[]> assignments, int indexOfDateField, String key) {
    	//This method returns the index of the key if it exists in the ArrayList, otherwise the index of the key before that. If the key is smaller than the first item, then returns -1. 
    		//This return value is then incremented by one to make seqNum. 
    			//In case that the key is smaller than the first item, then the returning value (-1) will be added by one and will make 0. There is no "0" sequence number, but the fact is that we need the difference between two seqNum's (of the bugAssignment and the evidence) should be positive (non-zero) integer [it will be taken parted at the denominator for the recency score calculation].
        int lo = 0;
        int hi = assignments.size() - 1;
        int mid = 0;
        while (lo <= hi) {
            // Key is in a[lo..hi] or not present.
            mid = lo + (hi - lo) / 2;
            if (key.compareTo(assignments.get(mid)[indexOfDateField]) < 0) 
            	hi = mid - 1;
            else 
            	if (key.compareTo(assignments.get(mid)[indexOfDateField]) > 0)
            		lo = mid + 1;
            else 
            	return mid;
        }
//        if (key.compareTo(assignments.get(0)[indexOfDateField]) < 0)
//        	return -1;
//        else
        	return mid-1;
    }
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
}











