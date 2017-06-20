package data;

public class AssignmentStatSummary {
	public int n; //: number of bug assignments.
	public int sumTop1; //: number of top 1 assignments (Note: this is not the percentage. We need to divide it by n).
	public int sumTop5; // ...
	public int sumTop10; // ...
	public double sumRR; // ...  
	public double sumAP;
	public double sumPAt1;
	public double sumRAt1;
	public double sumPAt5;
	public double sumRAt5;
	public double sumPAt10;
	public double sumRAt10;
	
	public AssignmentStatSummary(int n, int sumTop1, int sumTop5, int sumTop10, double sumRR, double sumAP, 
			double sumPAt1, double sumRAt1, double sumPAt5, double sumRAt5, double sumPAt10, double sumRAt10){
		this.n = n;
		this.sumTop1 = sumTop1;
		this.sumTop5 = sumTop5;
		this.sumTop10 = sumTop10;
		this.sumRR = sumRR;
		this.sumAP = sumAP;
		this.sumPAt1 = sumPAt1;
		this.sumRAt1 = sumRAt1;
		this.sumPAt5 = sumPAt5;
		this.sumRAt5 = sumRAt5;
		this.sumPAt10 = sumPAt10;
		this.sumRAt10 = sumRAt10;
	}
}
