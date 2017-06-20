package utils;

public class StringManipulations {
	//------------------------------------------------------------------------------------------------------------------------
	public static String clean(String field){
		//This method converts any special character to one space ('\r' and '\n' are converted to three spaces).
		
//		field = field.replaceAll("\\s+", " ");//to remove duplicate whites paces and \n etc.
		//Remove non-alphanumeric characters (except +-.#@ and http://abc.com):
			//.#+-@ (the four first ones for SO tags, and the last one for mentioning the users' ids)  . 
		field = field.replaceAll("\\n", "  ");
		field = field.replaceAll("\\r", "  ");
		field = field.replaceAll("((https?|ftp)://[^`=\\[\\]\\\\\n\r\t;',~!\\$%^&*(){}|\"<>?/:]+)?[^a-zA-Z0-9\\.\\#\\+\\-\\@]", "$1 ");
		
		//I comment the following statement because we need to have all the spaces (after we replaced some invalid characters to space). We need them for merging consecutive words: 
//		field = field.replaceAll("\\s+", " ");//to remove duplicate whites paces and \n etc.
		
		//This is for later (replace any single character by space). The idea is it will be used after merging two consecutive keywords to shape a tag (like "C#" and "3.0" that make "c#-3.0"; sometimes "-" or "."  comes between the two keywords):
//		fieldValue = fieldValue.replaceAll("(?s)(?<!\\S).(?!\\S)", " "); //to replace any single character by space.
		return field;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static String concatTwoStringsWithSpace(String s1, String s2){
		if (s1.equals(""))
			return s2;
		else
			if (s2.equals(""))
				return s1;
			else
				return s1 + " " + s2;
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static String removeRedundantSpaces(String s){
		s= s.trim();
		return s.replaceAll("\\s+", " ");//to remove duplicate whites paces and \n etc, in case there are some.
	}
	//------------------------------------------------------------------------------------------------------------------------
	public static boolean twoStringArraysAreEqual(String[] s1, String[] s2){
		boolean result = true;
		if (s1.length != s2.length)
			result = false;
		else
			for (int i=0; i<s1.length; i++)
				if (!s1[i].equals(s2[i])){
					result = false;
					break;
				}
		return result;
	}//compareTwoStringArrays().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static boolean specificFieldsOfTwoStringArraysAreEqual(String[] s1, String[] s2, String fieldNumbersToBeChecked_separatedByDollar){
		//Compare the first 'fieldNumbersToBeChecked_separatedByDollar' strings in two stringArrays, and, if all are the same, returns true, otherwise false. 
		boolean result = true;
		if (s1.length != s2.length)
			result = false;
		else{
			String[] fieldNumbersToBeChecked_string = fieldNumbersToBeChecked_separatedByDollar.split("\\$");
			int[] fieldNumbersToBeRead = new int[fieldNumbersToBeChecked_string.length];
			for (int i=0; i<fieldNumbersToBeRead.length; i++)
				fieldNumbersToBeRead[i] = Integer.parseInt(fieldNumbersToBeChecked_string[i]); 
			
			for (int j=0; j<fieldNumbersToBeRead.length; j++)
				if (!s1[fieldNumbersToBeRead[j]].equals(s2[fieldNumbersToBeRead[j]])){
					result = false;
					break;
				}
		}
		return result;
	}//compareTwoStringArrays().
	//------------------------------------------------------------------------------------------------------------------------------------------------
	public static String concatTwoStringsWithDelimiter(String s1, String s2, String delimiter){
		if (s1.equals(""))
			return s2;
		else
			if (s2.equals(""))
				return s1;
			else
				return s1 + delimiter + s2;
	}
	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------------------------------
}
