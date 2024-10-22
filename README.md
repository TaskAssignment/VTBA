# TTBA: Thesaurus and Time based Bug-assignment 
TTBA, plus the original tf-idf experiment.

This project includes the experiments for bug assignment (using Github data sets).

The bug assignment is based on expertise scores of the developers. The scores are obtained by calculating the similarity between the new bug and each developer. Our similarity metric is based on IR method, TF-IDF, and takes into account time and importance of the used keywords. 

How to run the code:

1- Clone the code in C:\BT2. After cloning, in this directory there should be "BugTriaging2" and "Exp" folders. The first one includes the source codes. The second folder includes all the input files and the output files that are results of runnning the code. These are the results we provided in our paper. "Exp\In\GH" folder includes two sub-folders; "DSForMainExp" is the main data for TTBA experiment. "DSForTFIDFExp" is the data set that is used for two test experiments that we have done for comparing our main experiment against them (the original tf-idf and the Tiime-TF-IDF). It includes the full text (after cleaning) and no filtering of non-StackOverflow tags is done for it.

2- Install Eclipse IDE for Java Developers. The program is tested on Eclipse Neon.3 Release 4.6.3: http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/neon3 on a 64 bit version with MS Windows 10 OS. However, other releases and even other versions of Eclipse and on different platforms should be fine.

3- You need to install two .jar files; "json-simple-1.1.1.jar" and "guava-10.0.1.jar" that are provided in the jar folder. For doing this, you need to right click on "src" in your project in Eclipse, then choose "Build Path" --> "Configure build path". Then go to "Libraries" tab and select "Add external jars". Then brows to the folder containing those two .jar files and select them and add them to your library.

4- If you are running the code on Linux, you need to change the formatting of the paths in the code (in Constants.java, CSVManipulations.java, CheckFeasibility.java and JSONToTSV.java); change "\\\\" to "//" everywhere.

5- The main file to run is "BugTriaging2\src\main\Algorithm.java". Note that there are several loops in the main() method of this class. These were used once for tuning and running the "bugAssignment()" method under different configurations. But all of the extra cases are now disabled and the "bugAssignment()" method is executed just once every time we run the code. After running, the output should be created  if the "Out" folder is empty. Otherwise it will automatically add the new results at the end of the contents of the output files. Currently the program is set to  run just T5 (combination of all assignee types). But if you want to run for specific, narrowed assignee types (T1, T2, T3 and T4), or a combination of them, you can change "0"s to "1"s in "assignmentTypesToTriage" array in experiment() method. Currently, the "isMainRun = true;" statement makes the main() method run the code for all projects. If you want to run the code just for three test projects, then change true to false. evidenceTypes in the experiment() method defines the sources to use for bug-assignment. The default is set to only bug's information. If you want to use oter sources like commit and comments as well (multi-source), then you need to set those values to 1 respectively. generalExperimentType in this method determines if we are running our approach, the original TF-IDF, or the Time-TF-IDF method (we implemented these two for comparing our approach against them). 

6- If you want to run the original tf-idf experiment, you should set the value of "justCalculateOriginalTFIDF" variable to true. In that experiment, there is a filtering possibility that removes developers with less than a threshold number of bug assignments. This threshold is set by variable "developerFilterationThreshold_leastNumberOfBugsToFixToBeConsidered". If it is set to 1, then there is no filtering. For running the TTBA experiment, set "justCalculateOriginalTFIDF" to false.

7- If you have any problems or issues, you can create an issue in this repository and I will answer to that. Or you can contact "alisajedi [at] ualberta.ca" for other questions.

Thank you for your interest in our project. Ali Sajedi.
