package data;

import java.util.ArrayList;
import java.util.Date;

import utils.Constants;
import utils.MyUtils;

public class Assignment {
	public String bugNumber;
	public Date date;
	public String login;
	public Assignment(ArrayList<String[]> assignments, int index, int indentationLevel){
		try{
			String[] fields = assignments.get(index);
			bugNumber = fields[0];
			date = Constants.dateFormat.parse(fields[1]);
			login = fields[2];
		}catch(Exception e){
			MyUtils.println("Error: Index \"" + index + "\" out of bound.", indentationLevel);
		}
	}
}
