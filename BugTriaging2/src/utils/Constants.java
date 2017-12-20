package utils;


//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FilenameFilter;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.math.BigDecimal;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Random;
//import java.util.TreeMap;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.HashSet;
//import java.util.Random;
//import data.Assignee;
//import main.AlgPrep;
//import utils.Constants.ConditionType;
//import utils.Constants.FieldType;
//import utils.Constants.LogicalOperation;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;

import data.Evidence;
import main.AlgPrep;
//import com.google.gwt.thirdparty.guava.common.collect.ImmutableMap;
import main.WordsAndCounts;
import utils.Constants.BTOption1_whatToAddToAllBugs;
import utils.Constants.BTOption2_w;
import utils.Constants.BTOption3_TF;
import utils.Constants.BTOption4_IDF;
import utils.Constants.BTOption5_prioritizePAs;
import utils.Constants.BTOption6_whatToAddToAllCommits;
import utils.FileManipulationResult;


public class Constants { 
	public static final String DATASET_OVERAL_DIRECTORY = "C:\\2-Study\\BugTriaging2\\Data Set\\Main";
	//GH dataset:
	public static final String DATASET_DIRECTORY_GH_JSON = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\1-JSON";
	//	private static final String DATASET_DIRECTORY_GH_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\TSV";
	public static final String DATASET_DIRECTORY_GH_2_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\2-TSV\\3- 16 projects + 2 project families (13 + 3 + 6 more projects)";
	public static final String DATASET_DIRECTORY_GH_3_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\3-TSV-Cleaned";
	public static final String DATASET_DIRECTORY_GH_3_TSV_OUTPUT = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\3-TSV-Cleaned\\Output";
	//SO dataset:
	public static final long TOTAL_NUMBER_OF_SO_QUESTIONS = 12350818; //Total number of questions as of 2016/09/12 (based on the latest data set was downloaded at 2016/11/10 from https://archive.org/download/stackexchange/stackoverflow.com-Posts.7z).
	public static final String DATASET_DIRECTORY_SO_1_XML_EXTERNAL = "D:\\BugTriaging2\\Data Set\\SO\\20161110\\1-XML";
	public static final String DATASET_DIRECTORY_SO_2_TSV = DATASET_OVERAL_DIRECTORY + "\\SO\\20161110\\2-TSV";
	//	public static final String DATASET_DIRECTORY_SO_2_TSV_EXTERNAL = "D:\\BugTriaging2\\Data Set\\SO\\20161110\\2-TSV";
	public static final String DATASET_DIRECTORY_SO_3_TSV_CLEANED = DATASET_OVERAL_DIRECTORY + "\\SO\\20161110\\3-TSV-Cleaned";
	//	public static final String DATASET_DIRECTORY_SO_3_TSV_CLEANED_EXTERNAL = "D:\\BugTriaging2\\Data Set\\SO\\20161110\\3-TSV-Cleaned";

	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//************** FINAL DATASET FOLDERS *******************************************************************************************
	//********************
	//Final dataset folder (first version: complete cleaned text; all-text-data) 
	//********************
	//4A1: 16 FASE projects and project families of rails/rails and angular/angular.js (note that 3 projects don't have public bugs; <"scala/scala" "2888818">, <"mozilla-b2g/gaia" "2317369">, <"edx/edx-platform" "10391073">):
	public static final String DATASET_DIRECTORY_GH_4A1_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4A1-TSV";
	//4A2: 13 main FASE projects (with issues enabled) and project families of rails/rails and angular/angular.js:
	public static final String DATASET_DIRECTORY_GH_4A2_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4A2-TSV";
	//4A3: Just 13 main FASE projects (with issues enabled):
	public static final String DATASET_DIRECTORY_GH_4A3_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4A3-TSV";
	//4A4: Just one project (elastic/elasticsearch):
	public static final String DATASET_DIRECTORY_GH_4A4_ELASTICSEARCH_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4A4-elasticsearch-TSV";
	//********************
	//Final dataset folder (second version: removed "non SO tags"; just-SOTag-data):
	//********************
	//4B1: 16 FASE projects and project families of rails/rails and angular/angular.js:
	public static final String DATASET_DIRECTORY_GH_4B1_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4B1-TSV";
	//4B2: 13 main FASE projects (with issues enabled) and project families of rails/rails and angular/angular.js:
	public static final String DATASET_DIRECTORY_GH_4B2_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4B2-TSV";
	//4B3: Just 13 main FASE projects (with issues enabled):
	public static final String DATASET_DIRECTORY_GH_4B3_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4B3-TSV";
	//4B3: Just one project (elasticsearch/elasticsearch):
	public static final String DATASET_DIRECTORY_GH_4B4_ELASTICSEARCH_TSV = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\4B4-elasticsearch-TSV";
	//********************
	public static final String DATASET_DIRECTORY_OUTPUT = DATASET_OVERAL_DIRECTORY + "\\GH\\AtLeastUpTo20161001\\Output";
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
//	public static final String DATASET_DIRECTORY_FOR_THE_ALGORITHM__SOURCE = "C:\\BT2\\BugTriaging2";
	public static final String DATASET_DIRECTORY_BASE = "C:\\BT2\\BugTriaging2C";
	public static final String DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_MAIN = DATASET_DIRECTORY_BASE + "\\Exp\\In\\GH\\DSForMainExp";
	public static final String DATASET_DIRECTORY_FOR_THE_ALGORITHM__GH__EXPERIMENT_TFIDF = DATASET_DIRECTORY_BASE + "\\Exp\\In\\GH\\DSForTFIDFExp";
	public static final String DATASET_DIRECTORY_FOR_THE_ALGORITHM__SO__EXPERIMENT = DATASET_DIRECTORY_BASE + "\\Exp\\In\\SO";
	public static final String DATASET_DIRECTORY_FOR_THE_ALGORITHM__EXPERIMENT_OUTPUT = DATASET_DIRECTORY_BASE + "\\Exp\\Out";
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	public enum ProjectType{
		FASE_3__NO_PUBLIC_BUGS /* 3 FASE projects that are available in Github but do not have issues enabled */,  
		FASE_13_EXTENSION__PROJECT_FAMILIES_OF_TWO_PROJECTS /* The project families of rails/rails and angular/angular.js (13 and 6 projects) */, 
		FASE_13, /* Just 13 FASE projects that have public issues in Github (no project families) */
		OTHERS_UNKNOWN
	}
	public static String ELASTIC_ELASTICSEARCH__PROJECT_NAME = "elastic/elasticsearch";	
	
	public static final String ASSIGNMENT_RESULTS_OVERAL_FOLDER_NAME = "outDetails";

	public static final String TAGS_SEPARATOR = ";;";

	
	public static final String FIELD_DELIMITER_FOR_JSON_OBJECT = "\\&";
	//[] means array. {} means an object (In this case, first, the name of the object comes. Then the fields come delimited by &).
	public static final Map<String, List<String>> USEFUL_FIELDS_IN_JSON_FILES = ImmutableMap.<String, List<String>> builder() 
			.put("bugs", Arrays.asList(new String[] { "_id", "url", "author", "createdAt{}$date", "labels[]name", "status", "title", "body" }))
			.put("bugs:labels", Arrays.asList(new String[] { "id", "url", "author", "createdAt", "labels", "status", "title", "body" }))
			.put("bugs:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { "title", "body" }))

			.put("comments", Arrays.asList(new String[] { "_id", "projectId", "createdAt{}$date", "user", "type", "commitSha", "issueNumber", "body" }))
			.put("comments:labels", Arrays.asList(new String[] { "id", "projectId", "createdAt", "user", "type", "commitSha", "issueNumber", "body" }))
			.put("comments:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { "body" }))

			.put("commits", Arrays.asList(new String[] { "_id", "projectId", "user", "createdAt{}$date", "url", "message" }))
			.put("commits:labels", Arrays.asList(new String[] { "sha", "projectId", "user", "createdAt", "url", "commitMessage" }))
			.put("commits:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { "message" }))

			.put("githubissues", Arrays.asList(new String[] { "_id", "bug", "project", "isPR", "number", "assignees[]username" }))
			.put("githubissues:labels", Arrays.asList(new String[] { "id", "issue_Or_PRId_In_Bugs.tsv", "projectId", "isPR", "number", "assignees" }))
			.put("githubissues:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { }))

			.put("githubprofiles", Arrays.asList(new String[] { "_id", "email", "createdAt{}$date", "updatedAt{}$date", "repositories" }))
			.put("githubprofiles:labels", Arrays.asList(new String[] { "id", "email", "createdAt", "updatedAt", "repositories" }))
			.put("githubprofiles:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] { }))

			.put("projects", Arrays.asList(new String[] { "_id", "name", "description", "mainLanguagesPercentages", "languages[]_id&amount" }))
			.put("projects:labels", Arrays.asList(new String[] { "id", "name", "description", "mainLanguagesPercentages", "[language^linesOfCode;;...]" }))
			.put("projects:FieldsToRemoveInvalidCharacters", Arrays.asList(new String[] {"description"}))
			.build();

	//	public static final String VALID_CHARACTERS_REGEX = "a-zA-Z0-9\\.\\#\\+\\-";  //The valid characters that can be used in SO tags or @ (for mentioning a user name), :// (for links like http://... because if a developer mentions "http://..." it doesn't mean he is expert in "http", so we need to keep that) and / (again, for links like http://...). 
	public static final String SEPARATOR_FOR_ARRAY_ITEMS = ";;";
	public static final String MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM = "^";
	public static final String MINOR_SEPARATOR_FOR_FIELDS_IN_OBJECT_IN_AN_ARRAY_ITEM_REGEX = "\\^";

	public static final String allValidCharactersInSOQUESTION_AND_ANSWER_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\(\\)\\[\\]\\{\\}\\~\\!\\$\\%\\^\\&\\*\\_\\:\\;\\<\\>\\,\\.\\?\\/\\|\\=\\\"\\'\\`\\\\";  //The same as the above line, but for the question / answer text. 
	//    public static final String allValidCharactersInGH_Descriptions_ForRegEx = "a-zA-Z0-9\\.\\#\\+\\-\\_\\@\\(\\)\\[\\]\\{\\}\\*\\!\\,\\:\\;";  
	public static final String SEPARATOR_FOR_TABLE_AND_FIELD = ":";
	public static final String TAB = "\t";
	public static final String COMBINED_KEY_SEPARATOR = TAB;
	public static final DecimalFormat integerFormatter = new DecimalFormat("###,###");
	public static final DecimalFormat floatFormatter = new DecimalFormat("###,###.#");
	public static final DecimalFormat floatPercentageFormatter = new DecimalFormat("###,##0.00000");
	public static final DecimalFormat highPrecisionFloatFormatter = new DecimalFormat("###,###.######");
	public static final int NUMBER_OF_TAB_CHARACTERS = 4;
	public static final int NUMBER_OF_LANGUAGES_TO_CONSIDER_IN_LANGUAGES_STUDY = 10;
	//	public static final int NUMBER_OF_LANGUAGES_TO_CONSIDER_IN_LANGUAGES_STUDY = 1;

	public static final String ALL = "ALL";

	public static final long THIS_IS_A_SMALL_TEST = 200;
	public static final long THIS_IS_A_TEST = 10;
	public static final long THIS_IS_REAL = -1;//unlimited!
	public static final int ERROR = -1;
	public static final long AN_EXTREMELY_NEGATIVE_LONG = Long.MIN_VALUE; 
	public static final long AN_EXTREMELY_POSITIVE_LONG = Long.MAX_VALUE; 
	public static final int AN_EXTREMELY_POSITIVE_INT = Integer.MAX_VALUE; 
//	public static final int AN_EXTREMELY_NEGATIVE_INT = Integer.MIN_VALUE; 
	public static final int SEQ_NUM____THIS_IS_NOT__B_A_EVIDENCE = Integer.MIN_VALUE; 
	public static final int SEQ_NUM____NO_NEED_TO_TRIAGE_THIS_TYPE___OR___THIS_IS_NOT__NON_B_A_EVIDENCE = Integer.MIN_VALUE; 
	
	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	//Sort order:
	public enum SortOrder{ 
		ASCENDING_INTEGER, DESCENDING_INTEGER, DEFAULT_FOR_STRING
	}

	public enum FieldType{
		LONG, STRING, NOT_IMPORTANT
	}

	public enum LogicalOperation { 
		NO_CONDITION, AND, OR, IGNORE_THE_SECOND_OPERAND
	}

	public enum ConditionType {
		EQUALS, NOT_EQUALS, NOTHING, GREATER_OR_EQUAL
	}

	public enum JoinType{
		INNER_JOIN, LEFT_JOIN, RIGHT_JOIN, FULL_JOIN
	}

	public static final int NUMBER_OF_ASSIGNEE_TYPES = 5;
	//These are used as suffixes for the variations of assignees.tsv file names:
//	public static final String[] ASSIGNEE_TYPES = {
//			"ASSIGNEE_TYPE_1_BUG_FIX_CODE_AUTHOR", //Committing (in the same project) and referencing the bug with one of the keywords.
//			"ASSIGNEE_TYPE_2_BUG_FIX_CODE_COAUTHOR", //Committing (in the same project) and referencing the bug with one of the keywords. This may be the same developer who originally wrote the code, or the [core] developer who merged the PR, did the rebase, edited the commit or re-committed.
//			"ASSIGNEE_TYPE_3_ADMINISTRATIVE_BUG_RESOLVER", //closing the bug
//			"ASSIGNEE_TYPE_4_DRAFTED_ASSIGNEE_WHEN_THE_BUG_WAS_CLOSED", //Being the assignee when the bug was closed
//			"ASSIGNEE_TYPE_5_UNION_OF_ALL_TYPES_1_2_3_4" //Union of all the above types.
//	};
//	public static enum EVIDENCE_TYPES_TO_CONSIDER{
//		BUG_ASSIGNMENT, 
//		COMMIT, 
//		PR, 
//		BUG_COMMENT, 
//		COMMIT_COMMENT, 
//		PR_COMMENT
//		}

	public static enum ASSIGNMENT_TYPES_TO_TRIAGE{
		T1_AUTHOR, //0
		T2_COAUTHOR,  //1
		T3_ADMIN_CLOSER, //2
		T4_DRAFTED_A, //3
		T5_ALL_TYPES //4
		};

	public static final int EVIDENCE_TYPE_COMMIT = 11; 
	public static final int EVIDENCE_TYPE_PR = 12; 
	public static final int EVIDENCE_TYPE_BUG_COMMENT = 13; 
	public static final int EVIDENCE_TYPE_COMMIT_COMMENT = 14; 
	public static final int EVIDENCE_TYPE_PR_COMMENT = 15;
	
	public static final int[] EVIDENCE_TYPE = {
			ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal()/*0*/, 
			ASSIGNMENT_TYPES_TO_TRIAGE.T2_COAUTHOR.ordinal() /*1*/,
			ASSIGNMENT_TYPES_TO_TRIAGE.T3_ADMIN_CLOSER.ordinal()/*2*/, 
			ASSIGNMENT_TYPES_TO_TRIAGE.T4_DRAFTED_A.ordinal()/*3*/, 
			ASSIGNMENT_TYPES_TO_TRIAGE.T5_ALL_TYPES.ordinal()/*4*/,
			EVIDENCE_TYPE_COMMIT/*11*/, 
			EVIDENCE_TYPE_PR/*12*/, 
			EVIDENCE_TYPE_BUG_COMMENT/*13*/, 
			EVIDENCE_TYPE_COMMIT_COMMENT/*14*/, 
			EVIDENCE_TYPE_PR_COMMENT/*15*/, 
			}; 
	public static Double[] TYPE_SIMILARITY = {
			1.0, /* for ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); 0 */
			1.0, /* for ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); 1 */
			1.0, /* for ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); 2 */
			1.0, /* for ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); 3 */
			1.0, /* for ASSIGNMENT_TYPES_TO_TRIAGE.T1_AUTHOR.ordinal(); 4 */
			0.0,
			0.0,
			0.0,
			0.0,
			0.0,
			0.0,
			0.3, /* for EVIDENCE_TYPE_COMMIT; 11 */
			0.3, /* for EVIDENCE_TYPE_PR; 12 */
			0.6, /* for EVIDENCE_TYPE_BUG_COMMENT; 13 */
			0.3, /* for EVIDENCE_TYPE_COMMIT_COMMENT; 14 */
			0.1, /* for EVIDENCE_TYPE_PR_COMMENT; 15 */
	};
//	public static final int EVIDENCE_TYPES__COUNT = 10; //This will be changed when we want to include everything.

	
	public static final String[] ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS = {
			"T1_AUTHOR",
			"T2_COAUTHOR",
			"T3_ADMIN_CLOSER",
			"T4_DRAFTED_A",
			"T5_ALL_TYPES",
			};
	
	public static final String[] ASSIGNMENT_FILE_NAMES = {
			"9-ASSIGNMENTS_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[0], //Committing (in the same project) and referencing the bug with one of the keywords.
			"9-ASSIGNMENTS_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[1], //Committing (in the same project) and referencing the bug with one of the keywords. This may be the same developer who originally wrote the code, or the [core] developer who merged the PR, did the rebase, edited the commit or re-committed.
			"9-ASSIGNMENTS_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[2], //closing the bug
			"9-ASSIGNMENTS_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[3], //Being the assignee when the bug was closed
			"9-ASSIGNMENTS_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[4] //Union of all the above types.
			};
	
	public static final String[] COMMUNITY_FILE_NAMES = {
			"10-COMMUNITY_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[0],  
			"10-COMMUNITY_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[1], 
			"10-COMMUNITY_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[2], 
			"10-COMMUNITY_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[3], 
			"10-COMMUNITY_"+ASSIGNED_BUGS_TYPES__SHORT_DESCRIPTIONS[4], 
			};
	
//	//The indices of otherOptions in Algorithm.java and AlgPrep.java:
//	public static int OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_PROJECT_TITLE_AND_DESCRIPTION_TO_THE_BUG = 0; 
//	public static int OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_MAIN_LANGUAGES_TO_THE_BUG = 1;
//	public static int OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_PROJECT_TITLE_AND_DESCRIPTION_TO_THE_COMMIT = 2; 
//	public static int OTHER_ASSIGNMENT_OPTIONS__CONCATENATE_MAIN_LANGUAGES_TO_THE_THE_COMMIT = 3;
	
//	public static final String[] ASSIGNED_BUGS_TYPES = {
//			"ASSIGNED_TYPE_1",
//			"ASSIGNED_TYPE_2",
//			"ASSIGNED_TYPE_3",
//			"ASSIGNED_TYPE_4",
//			"ASSIGNED_TYPE_5",
//	};
//	public static final int EVIDENCE_TYPE__BUG_TITLE_DESCRIPTION = 0; //0, 1, 2, 3 or 4
//	public static final int EVIDENCE_TYPE__COMMIT = 5;
//	public static final int EVIDENCE_TYPE__BUG_COMMENT = 6;
	//...

	//	public static final String ASSIGNEE_TYPE_1_BUG_FIX_CODE_AUTHOR = "ASSIGNMENTS_TYPE_1_BUG_FIX_CODE_AUTHOR"; //Committing (in the same project) and referencing the bug with one of the keywords.
	//	public static final String ASSIGNEE_TYPE_2_BUG_FIX_CODE_COAUTHOR = "ASSIGNMENTS_TYPE_2_BUG_FIX_CODE_COAUTHOR"; //Committing (in the same project) and referencing the bug with one of the keywords. This may be the same developer who originally wrote the code, or the [core] developer who merged the PR, did the rebase, edited the commit or re-committed.
	//	public static final String ASSIGNEE_TYPE_3_ADMINISTRATIVE_BUG_RESOLVER = "ASSIGNMENTS_TYPE_3_ADMINISTRATIVE_BUG_RESOLVER"; //closing the bug
	//	public static final String ASSIGNEE_TYPE_4_OFFICIAL_ASSIGNEE_WHEN_THE_BUG_WAS_CLOSED = "ASSIGNMENTS_TYPE_4_OFFICIAL_ASSIGNEE_WHEN_THE_BUG_WAS_CLOSED"; //Being the assignee when the bug was closed
	//	public static final String ASSIGNEE_TYPE_5_UNION_OF_ALL_TYPES_1_2_3_4 = "ASSIGNMENTS_TYPE_5_UNION_OF_ALL_TYPES_1_2_3_4"; //Union of all the above types.
	//	public static final String COMMUNITY_TYPE_1 = "COMMUNITY_TYPE_1";
	//	public static final String COMMUNITY_TYPE_2 = "COMMUNITY_TYPE_2";
	//	public static final String COMMUNITY_TYPE_3 = "COMMUNITY_TYPE_3";
	//	public static final String COMMUNITY_TYPE_4 = "COMMUNITY_TYPE_4";
	//	public static final String COMMUNITY_TYPE_5 = "COMMUNITY_TYPE_5";

//	public static Double maxScore = 0.0;
	public static double[] highScores = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
	public enum BTOption1_whatToAddToAllBugs{//: What to be added to the bugs by default; since some bugs have no tags, we may add default strings to all of them to increase accuracy:
		JUST_USE_BUG_TD, //: add nothing to the bug title and description.
		ADD_PTD, //: add project title and description
		ADD_ML, //: add project language
		ADD_PTD_ML //: add project title and description + project language
	}
	
	public enum BTOption2_w{//: Term weighting:
		NO_TERM_WEIGHTING,
		USE_TERM_WEIGHTING //: our graph term weights.
	}
	
	public enum BTOption3_TF{//: TF formula:
		ONE,
		FREQ,
		FREQ__TOTAL_NUMBER_OF_TERMS,
		LOG_BASED
	}
	
	public enum BTOption4_IDF{//: IDF formula:
		ONE,
		FREQ,
		FREQ__TOTAL_NUMBER_OF_TERMS,
		LOG_BASED
	}
	
	public enum BTOption5_prioritizePAs{//: Prioritize previous assignees:
		NO_PRIORITY,
		PRIORITY_FOR_PREVIOUS_ASSIGNEES
	}
	
	public enum BTOption6_whatToAddToAllCommits{//: What to be added to the commits by default; since some commit messages have no tags, we may add default strings to all of them to increase accuracy:
		JUST_USE_COMMIT_M, //: add nothing to the commit message.
		ADD_PTD, //: add project title and description
		ADD_mL, //: add project language
		ADD_PTD_mL //: add project title and description + project language
	}
	
	public enum BTOption7_whenToCountTextLength{ //: Consider the text length before/after non-SO terms removal.
		USE_TEXT_LENGTH_BEFORE_REMOVING_NON_SO_TAGS,
		USE_TEXT_LENGTH_AFTER_REMOVING_NON_SO_TAGS
	}
	
	public enum BTOption8_recency{
		NO_RECENCY,
		RECENCY1,
		RECENCY2
	}
	
	public static void f1(FileManipulationResult fMR){
		FileManipulationResult fMR2 = new FileManipulationResult();
		fMR2.errors++;
		if (fMR2.errors > 0)
			fMR.errors = fMR2.errors;
	}
	
	public static void main(String[] args) {
		FileManipulationResult fMR = new FileManipulationResult();
		f1(fMR);
		if (fMR.errors > 0)
			System.out.println("ERROR");
		else
			System.out.println("No error");
		
//		for (BTOption1_whatToAddToAllBugs option1: BTOption1_whatToAddToAllBugs.values()){//: What to be added to the bugs by default.
//			for (BTOption2_w option2: BTOption2_w.values()){//: Term weighting
//				for (BTOption3_TF option3: BTOption3_TF.values()){//: TF formula.
//					for (BTOption4_IDF option4: BTOption4_IDF.values()){//: IDF formula.
//						for (BTOption5_prioritizePAs option5: BTOption5_prioritizePAs.values()){//: Prioritize previous assignees.
//							for (BTOption6_whatToAddToAllCommits option6: BTOption6_whatToAddToAllCommits.values()){//: What to be added to the commits by default.
//								if (option6 != BTOption6_whatToAddToAllCommits.JUST_USE_COMMIT_M)
//									continue;
//								System.out.println(option1 + "   " + option2 + "   " + option3 + "   " + option4 + "   " + option5 + "   " + option6);
//							}
//						}
//					}
//				}
//			}
//		}

		
//		WordsAndCounts wAC = new WordsAndCounts("abc ab ac d d ab d d d abc ac ab abc");
//		for (int i=0; i<wAC.size; i++)
//			System.out.println(wAC.words[i] + "\t" + wAC.counts[i]);
//		System.out.println(wAC.totalNumberOfWords);
//		Date d1 = new Date();
//		String s = "";
//		for (int i=0; i<10000; i++)
//			s = s + "-"+Integer.toString(i);
//		System.out.println("<"+s+">");
//		Date d2 = new Date();
//	    DecimalFormat myDecimalFormatter = new DecimalFormat("###,###.###");
//	    System.out.println(myDecimalFormatter.format(((double)1000000000000.0+d2.getTime()-d1.getTime())/1000));
		
//		Random random = new Random();
//		System.out.println(random.nextInt(10));
//		Double a = 1.0;
//		Double b = 1.10;
//		if (b > a)
//			System.out.println("sss");
		
		
//		String s = "abc abc de de abc f de f abc f r r g d s ff";
//		String[] words = new String[5];
//		int[] wordCounts = new int[]{1, 2, 3, 4, 5};
//
//		wordCounts = ArrayUtils.removeElement(wordCounts, 1)

//		ArrayList<Integer> a = new ArrayList<Integer>();
//		a.add(31);
//		a.add(41);
//		a.add(1);
//		if (a.contains(1))
//			System.out.println("ssss");
//		
//		Date d1 = new Date();
//		String s = "1 ee 1 2222 ww 333 hh 1 ee 1 333 333 2222 kk";
//		String[] words = s.split(" ");//"", "s", "dsad", "wewer", "", "hjgfj", "bncv", "", "ksdf", "", "", "sdfsdf", "", "sdhhhfsfdg", "", "s", "dsad", "wewer", "", "h6jgfj", "bndcv", "", "", "444", "", "sdgggfsdf", "", "sdfsffdg", "", "s", "dsad", "wewer", "", "hjgfj", "bncv", "", "ksdf", "", "", "sdfsdfdfg", "", "sdhhhfsfdg", "", "ggs", "dsad", "wewer", "", "h5556jgfj", "bndcv", "", "", "4i44", "", "sdgggfsdf", "", "sdfsff77dg"};
//		int[] wordCounts = AlgPrep.getWordCountsOfDistinctWords(s, words);
//		WordsAndCounts wACA = AlgPrep.getWordsAndTheirCounts(s);
//		for (int i=0; i<wACA.size; i++)
//			System.out.println(wACA.words[i] + "\t" + wACA.counts[i]);
//		int counter = 0;
//		for (int j=0; j<100000; j++)
//			for (int i=0; i<s.length; i++)
//				if (s[i].equals("s"))
//					counter++;
//		System.out.println(counter);
//		AlgPrep.makeArraysOfWordsAndCounts(s, words, wordCounts);
//		Date d2 = new Date();
//		System.out.println((float)(d2.getTime()-d1.getTime())/1000);

		
		
		
//		AlgPrep.makeArraysOfWordsAndCounts(s, words, wordCounts);;
//		for (int i=0; i< words.length; i++)
//			System.out.println(words[i]);
//		System.out.println();
//		for (int i=0; i< wordCounts.length; i++)
//			System.out.println(wordCounts[i]);
		
		
//		Integer[] array = new Integer[]{2, 3, 4, 6, 7, 8, 9, 11, 13, 15, 16, 19, 21, 22, 25, 26, 29, 33, 40, 44};
//		ArrayList<Integer> aL = new ArrayList<Integer>(Arrays.asList(array));
//		array[0]=111;
//		System.out.println(aL);
//		System.out.println(MyUtils.specialBinarySearch(aL, 1));
//		
//		System.out.println(AlgPrep.getMainLanguages("[julia^66;;c^19]"));
		
		
		
		
		
//		int[] a = {1, 2, 3};
//		a[3] = 8;
//		System.out.println(a[0] + "  " + a[1] + "  " + a[2] + "  " + a[3]);
//		Date d1 = new Date();
//		String s1 = "asdfghj";
//		String s2 = "iuyyu  treew ";
//		for (int j=0; j<1; j++)
//			for (int i=0; i<100000; i++){
//				String words = (s1 + " " + s2).replaceAll("\\s{2,}", " ").trim();
//				if (i == 12 && j == 13)
//					if (words.length() == 1234)
//						System.out.println("NOOO");
//			}
//		Date d2 = new Date();
//		System.out.println((float)(d2.getTime()-d1.getTime())/1000);
//		System.out.println("=======================");


//		Date d3 = new Date();
//		HashMap<Integer, Integer> h2 = new HashMap<Integer, Integer>();
//		for (int j=0; j<000; j++)
//			for (int i=0; i<100000; i++){
//				h2.put(1, i);
//			}
//		System.out.println(h2.get(1));
//		Date d4 = new Date();
//		System.out.println((float)(d4.getTime()-d3.getTime())/1000);
//		System.out.println("=======================");
//
//		
//		
//		Date d1 = new Date();
//		HashMap<EVIDENCE_TYPES_TO_CONSIDER, Integer> h = new HashMap<EVIDENCE_TYPES_TO_CONSIDER, Integer>();
//		for (int j=0; j<100; j++)
//			for (int i=0; i<100000; i++){
//				h.put(EVIDENCE_TYPES_TO_CONSIDER.COMMIT, i);
//			}
//		System.out.println(h.get(EVIDENCE_TYPES_TO_CONSIDER.COMMIT));
//		Date d2 = new Date();
//		System.out.println((float)(d2.getTime()-d1.getTime())/1000);
//		System.out.println("=======================");


		
		
//        HashMap<Integer, HashMap<Integer, Integer>> hashMap = new HashMap<Integer, HashMap<Integer, Integer>>();    
//        HashMap<Integer, HashMap<Integer, Integer>> hashMap1 = new HashMap<Integer, HashMap<Integer, Integer>>();    
//        HashMap<Integer, Integer> h = new HashMap<Integer, Integer>();
//        h.put(11, 111);
//        h.put(22, 222);
//        h.put(33, 333);
//        hashMap.put(1, h);
//
//        HashMap<Integer, Integer> h2 = new HashMap<Integer, Integer>();
//        h2.put(110, 1110);
//        h2.put(220, 2220);
//        h2.put(330, 3330);
//        hashMap.put(20, h2);
//
//        System.out.println("Original HashMap : " + hashMap);
//        hashMap1 = (HashMap<Integer, HashMap<Integer, Integer>>) hashMap.clone();
//        System.out.println("------------------------");
//        hashMap = new HashMap();
//        System.out.println("Copied HashMap : " + hashMap1); 
//        System.out.println("Original HashMap : " + hashMap);

		
		
//		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//		try {
//			String s1 = "2016-11-01T00:01:00.000Z";
//			String s2 = "2016-11-01T00:02:00.000Z";
//			String s3 = "2016-11-01T00:03:00.000Z";
//			
//			Date date1 = dateFormat.parse(s1);
//			Date date2 = dateFormat.parse(s2);
//			Date date3 = dateFormat.parse(s3);
//			System.out.println(date1);
//			double timeDiff = ((double)(date2.getTime()-date1.getTime())/(date3.getTime()-date1.getTime()));
//			System.out.println("    --> " + timeDiff);
//			System.out.println((date2.getTime()-date1.getTime())/1000);
//			double recency2 = 1.1 - java.lang.Math.log10(9+(date2.getTime()-date1.getTime())/1000)/10;
//			System.out.println("--> " + recency2);
//			double d = 0;
//			System.out.println(d);
//			System.out.println(d+1);
//			int alaki = 0;
//
//			Date d1 = new Date();
//			
//			for (int i=0; i<10000000; i++)
//				if (s1.compareTo(s2) < 0)
//					alaki++;
//			System.out.println(alaki);
//			
//			Date d2 = new Date();
//			System.out.println("'string' comparison: "+(float)(d2.getTime()-d1.getTime())/1000);
//
//			alaki = 0;
//			for (int i=0; i<10000000; i++)
//				if (date1.compareTo(date2) < 0)
//					alaki++;
//			System.out.println(alaki);
//
//			Date d3 = new Date();
//			System.out.println("'date' comparison: "+(float)(d3.getTime()-d2.getTime())/1000);
//			
//				
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		
//		try{
//			//Using text file:
//			System.out.println("----------------------------------------");
//			System.out.println("Step 1: Using text file:");
//			FileManipulationResult fMR = new FileManipulationResult();
//			
//			Date d1 = new Date();
//			int i;
//			TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo = null;;
//			for (int j=0; j<10; j++){
//				projectIdBugNumberAndTheirBugInfo = TSVManipulations.readUniqueCombinedKeyAndItsValueFromTSV(
//						Constants.DATASET_DIRECTORY_GH_3_TSV, "1-bugs-ASSIGNED_TYPE_1.tsv", fMR, null, 
//						"0$1",  
//						7, "2$3$4$5$6", 
//						LogicalOperation.NO_CONDITION, 
//						0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
//						0, ConditionType.NOTHING, "", FieldType.NOT_IMPORTANT, 
//						false, 100000, 0, Constants.THIS_IS_REAL, "1");
//				
//				System.out.println("");
//				i=0;
//				for (String s: projectIdBugNumberAndTheirBugInfo.keySet()){
//					if (s.equals("aaaa"))
//						System.out.println(s + " --> " + projectIdBugNumberAndTheirBugInfo.get(s)[0] + "\t" + projectIdBugNumberAndTheirBugInfo.get(s)[1]);
//					i++;
//					if (i>0)
//						break;
//				}
//			}
//
//			Date d2 = new Date();
//			float time = (float)(d2.getTime()-d1.getTime())/1000;
//			System.out.println("time: " + time);
//			System.out.println("----------------------------------------");
//
//			//Serializing the file:
//			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\3-TSV-Cleaned\\1-bugs-serialized.ser"));
//			out.writeObject(projectIdBugNumberAndTheirBugInfo);
//			out.close();
//
//
//			//Using serialized file:
//			System.out.println("----------------------------------------");
//			System.out.println("Step 2: Using serialized file:");
//			ObjectInputStream ois; 
//			Date d3 = new Date();
//			for (int j=0; j<10; j++){
//				ois = new ObjectInputStream(new FileInputStream("C:\\2-Study\\BugTriaging2\\Data Set\\GH\\AtLeastUpTo20161001\\3-TSV-Cleaned\\1-bugs-serialized.ser"));
//
//				TreeMap<String, String[]> projectIdBugNumberAndTheirBugInfo2 = (TreeMap<String, String[]>) ois.readObject();
////				System.out.println("total records in serialized file: " + projectIdBugNumberAndTheirBugInfo2.size());
//
//				System.out.println("");
//				int ii=0;
//				for (String s: projectIdBugNumberAndTheirBugInfo2.keySet()){
//					if (s.equals("aaaa"))
//						System.out.println(s + " --> " + projectIdBugNumberAndTheirBugInfo2.get(s)[0] + "\t" + projectIdBugNumberAndTheirBugInfo2.get(s)[1]);
//					ii++;
//					if (ii>0)
//						break;
//				}
//			}
//			Date d4 = new Date();
//			float time2 = (float)(d4.getTime()-d3.getTime())/1000;
//			System.out.println("time: " + time2);
//			System.out.println("----------------------------------------");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}








		//		int N = 100000000;
		//		HashMap<Integer, String> h = new HashMap<Integer, String>();
		//		for (int i=0; i<20; i++)
		//			h.put(i, Integer.toString(i));
		//
		//		Date d1 = new Date();
		//		for (int j=0; j<N; j++){
		//			int size = h.size();
		//			for (int i=0; i<size; i++)
		//				if (h.get(i).equals("12345"))
		//					System.out.println("Alaki test");
		//		}
		//		Date d2 = new Date();
		//		float time = (float)(d2.getTime()-d1.getTime())/1000;
		//		System.out.println("\"size=h.size()\" time: " + time);
		//
		//		d1 = new Date();
		//		for (int j=0; j<N; j++)
		//			for (int i=0; i<h.size(); i++)
		//				if (h.get(i).equals("12345"))
		//					System.out.println("Alaki test");
		//		d2 = new Date();
		//		time = (float)(d2.getTime()-d1.getTime())/1000;
		//		System.out.println("\"h.size()\" time: " + time);
		//
		//		d1 = new Date();
		//		for (int j=0; j<N; j++)
		//			for (int i=0; i<20; i++)
		//				if (h.get(i).equals("12345"))
		//					System.out.println("Alaki test");
		//		d2 = new Date();
		//		time = (float)(d2.getTime()-d1.getTime())/1000;
		//		System.out.println("\"10\" time: " + time);








//				int MAX = 10000000;
//				Date d1 = new Date();
//				ArrayList<String> a = new ArrayList<String>();
//				for (int i=0; i<100; i++)
//					a.add(Integer.toString(i));
//				for (int j=0; j<MAX; j++)
//					for (int i=0; i<100; i++)
//						if (a.get(i).equals("12345"))
//							System.out.println("yes");
//				Date d2 = new Date();
//				float time = (float)(d2.getTime()-d1.getTime())/1000;
//				System.out.println("ArrayList time:" + time);
//		
//				d1 = new Date();
//				HashMap<Integer, String> h = new HashMap<Integer, String>();
//				for (int i=0; i<100; i++)
//					h.put(i, Integer.toString(i));
//				for (int j=0; j<MAX; j++)
//					for (int i=0; i<100; i++)
//						if (a.get(i).equals("12345"))
//							System.out.println("yes");
//				d2 = new Date();
//				time = (float)(d2.getTime()-d1.getTime())/1000;
//				System.out.println("HashMap time:" + time);
//				
//				d1 = new Date();
//				String[] ss = new String[100];
//				for (int i=0; i<100; i++)
//					ss[i] = Integer.toString(i);
//				for (int j=0; j<MAX; j++)
//					for (int i=0; i<100; i++)
//						if (ss[i].equals("12345"))
//							System.out.println("yes");
//				d2 = new Date();
//				time = (float)(d2.getTime()-d1.getTime())/1000;
//				System.out.println("Array time:" + time);
				
	}





}
