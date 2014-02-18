
public class DiveData {

	double meanDepth;
	double medianDepth;
	double sdDepth;
	double iqrDepth;
	double meanTemp;
	double medianTemp;
	double sdTemp;
	double iqrTemp;
	String type;
	
	public DiveData(double meanDepth, double medianDepth, double sdDepth, double iqrDepth, double meanTemp, double medianTemp, double sdTemp, double iqrTemp, String type){
		this.meanDepth = meanDepth;
		this.medianDepth = medianDepth;
		this.sdDepth = sdDepth;
		this.iqrDepth = iqrDepth;
		this.meanTemp = meanTemp;
		this.medianTemp = medianTemp;
		this.sdTemp = sdTemp;
		this.iqrTemp = iqrTemp;
		this.type = type.toUpperCase();
	}
}
