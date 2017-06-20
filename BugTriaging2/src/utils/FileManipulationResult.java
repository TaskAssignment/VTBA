package utils;

public class FileManipulationResult {
	public int errors = 0;
	public int processed = 0;
	public int doneSuccessfully = 0;
	
	public void add(FileManipulationResult anFMR){
		errors = errors + anFMR.errors;
		processed = processed + anFMR.processed;
		doneSuccessfully = doneSuccessfully + anFMR.doneSuccessfully;
	}
}
