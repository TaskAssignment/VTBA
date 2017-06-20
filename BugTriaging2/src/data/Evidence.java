package data;

import java.util.Date;

import utils.Constants;
import utils.Constants.ASSIGNMENT_TYPES_TO_TRIAGE;
import utils.Constants.BTOption3_TF;

public class Evidence {
	public Date date;
	public int bASeqNum; //if the evidence is a bugAssignment, then this is the sequence number of the bugAssignment sequence number. Otherwise Constants.THIS_IS_NOT__B_A_EVIDENCE. 
	public int[] nonBA_virtualSeqNum; //If the evidence is bug assignment, then these values are Constants.THIS_IS_NOT__NON_B_A_EVIDENCE. Otherwise the sequence number of the bugAssignment type 0 to 4 that is at the same time (or the last one before this evidence).
	public int freq; //The frequency of the term (tag) in the respective field.
	public int totalNumberOfWordsInThisEvidence;
	public double tf;
	public Evidence(Date date, int seqNum, int[] virtualSeqNum, int freq, int totalNumberOfWordsInThisEvidence, BTOption3_TF option3_TF){
		this.date = date;
		this.bASeqNum = seqNum;
		this.nonBA_virtualSeqNum = new int[Constants.NUMBER_OF_ASSIGNEE_TYPES];
		for (int i=ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); i<=ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal(); i++)
			this.nonBA_virtualSeqNum[i] = virtualSeqNum[i];
		this.freq = freq;
		this.totalNumberOfWordsInThisEvidence = totalNumberOfWordsInThisEvidence;
		switch (option3_TF){
		case ONE: 
			tf = 1;
		case FREQ: 
			tf = freq;
		case FREQ__TOTAL_NUMBER_OF_TERMS: 
			tf = (double)freq/totalNumberOfWordsInThisEvidence; 
		case LOG_BASED: 
			tf = 1+(double)Math.log10(freq); 
		}
	}
}
