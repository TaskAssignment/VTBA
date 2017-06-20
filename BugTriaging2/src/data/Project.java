package data;

import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

import utils.Constants;
import utils.FileManipulationResult;
import utils.MyUtils;

public class Project {
	public String id;
	public String owner_repo;
	public String description;
	public int description_numberOfWords;
	public String mainLanguagePercentages;
	public String languagesAndTheirLinesOfCode;
	public Date bugsStartingDate;
	public Date commitsStartingDate;
	public Date pRsStartingDate;
	public Date bugCommentsStartingDate;
	public Date commitCommentsStartingDate;
	public Date pRCommentsStartingDate;
	public Date bugEventsStartingDate;
	public Date overalStartingDate;
	
	public Project(TreeMap<String, String[]> projects, String projectId, int indentationLevel, FileManipulationResult fMR){
		if (projects.containsKey(projectId)){
			String[] projectInfo = projects.get(projectId);
			id = projectId;
			owner_repo = projectInfo[0];
			description = projectInfo[1];
			description_numberOfWords = Integer.parseInt(projectInfo[2]);
			mainLanguagePercentages = projectInfo[3];
			languagesAndTheirLinesOfCode = projectInfo[4];
			try {
				bugsStartingDate = Constants.dateFormat.parse(projectInfo[5]);
				commitsStartingDate = Constants.dateFormat.parse(projectInfo[6]);
				pRsStartingDate = Constants.dateFormat.parse(projectInfo[7]);
				bugCommentsStartingDate = Constants.dateFormat.parse(projectInfo[8]);
				commitCommentsStartingDate = Constants.dateFormat.parse(projectInfo[9]);
				pRCommentsStartingDate = Constants.dateFormat.parse(projectInfo[10]);
				bugEventsStartingDate = Constants.dateFormat.parse(projectInfo[11]);
				overalStartingDate = Constants.dateFormat.parse(projectInfo[12]);
			} catch (ParseException e) {
				fMR.errors++;
				e.printStackTrace();
			}
		}
		else
			MyUtils.println("ERROR! Project \"" + projectId + "\" not found", indentationLevel);
	}
}
