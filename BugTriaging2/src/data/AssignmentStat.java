package data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AssignmentStat {
	public String bugNumber;
	public Date assignmentDate;
	public String ourTopRecommendedAssignee;
	public int ourTopRecommendedAssigneeRank;
	public HashMap<String, Integer> realAssigneesAndTheirRanksForThisAssignment;
	
	public AssignmentStat(//String projectId, 
			String bugNumber, Date assignmentDate, 
			String ourTopRecommendedAssignee, int ourTopRecommendedAssigneeRank, HashMap<String, Integer> realAssigneesAndTheirRanksForThisAssignment){
		this.bugNumber = bugNumber;
		this.assignmentDate = assignmentDate;
		this.ourTopRecommendedAssignee = ourTopRecommendedAssignee;
		this.ourTopRecommendedAssigneeRank = ourTopRecommendedAssigneeRank;
		this.realAssigneesAndTheirRanksForThisAssignment = new HashMap<String, Integer>();
		//Now, recording the logins (real assignees) and their ranks for this assignment:
		for (Map.Entry<String, Integer> e:realAssigneesAndTheirRanksForThisAssignment.entrySet())
			this.realAssigneesAndTheirRanksForThisAssignment.put(e.getKey(), e.getValue());
	}//public AssignmentStat.
	
	public String getRealAssignees(){
		String result = "";
		for (String login: realAssigneesAndTheirRanksForThisAssignment.keySet())
			if (result.equals(""))
				result = login;
			else
				result = result + ", " + login;
		return result;
	}
}
