package data;

import java.text.ParseException;
import java.util.Date;
import java.util.TreeMap;

import utils.Constants;
import utils.FileManipulationResult;

public class Bug {
	public String projectId;
	public String number;
	public String author;
	public Date createdAt;
	public String labels;
	public String title;
	public int title_numberOfWords;
	public String body;
	public int body_numberOfWords;
	public Bug(String projectId, String bugNumber, TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo, FileManipulationResult fMR){
		this.projectId = projectId;
		this.number = bugNumber;
		String[] bugInfo = projectIdBugNumberAndTheirBugInfo.get(projectId+Constants.TAB+bugNumber);
		author = bugInfo[0];
		try {
			createdAt = Constants.dateFormat.parse(bugInfo[1]);
		} catch (ParseException e) {
			fMR.errors++;
			e.printStackTrace();
		}
		labels = bugInfo[2];
		title = bugInfo[3];
		title_numberOfWords = Integer.parseInt(bugInfo[4]);
		body = bugInfo[5];
		body_numberOfWords = Integer.parseInt(bugInfo[6]);
	}
}
