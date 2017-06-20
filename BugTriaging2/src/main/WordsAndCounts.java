package main;

import java.util.ArrayList;
import java.util.HashMap;

import utils.Constants.BTOption7_whenToCountTextLength;

public class WordsAndCounts {
	public String[] words;
	public int[] counts;
	public int size;
	public int totalNumberOfWords; //: Generally, this is the total number of words right after splitting the input string. It is equal to the sum of counts[i] over all the terms.
		// But if option7=USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS, then it is equal to the original number of words in the text, before removing non-SO tags.
	public WordsAndCounts(String s, BTOption7_whenToCountTextLength option7_whenToCountTextLength, int originalNumberOfWordsInBugText){
		String[] tempWords = s.split(" ");
		if (option7_whenToCountTextLength == BTOption7_whenToCountTextLength.USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS)
			totalNumberOfWords = originalNumberOfWordsInBugText;
		else
			totalNumberOfWords = tempWords.length;
		ArrayList<String> aL = new ArrayList<String>();
		HashMap<String, Integer> wAC_hM = new HashMap<String, Integer>();
		for (int i=0; i<tempWords.length; i++){
			if (!tempWords[i].equals("")){
				if (wAC_hM.containsKey(tempWords[i]))
					wAC_hM.put(tempWords[i], wAC_hM.get(tempWords[i])+1);
				else{
					wAC_hM.put(tempWords[i], 1);
					aL.add(tempWords[i]);
				}
			}
		}
		size = aL.size();
		words = new String[size];
		counts = new int[size];
		for (int i=0; i<size; i++){
			words[i] = aL.get(i);
			counts[i] = wAC_hM.get(words[i]);
		}
	}
}
