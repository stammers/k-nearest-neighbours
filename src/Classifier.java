import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class Classifier {

	/**
	 * The number of neighbours to compare to.
	 */
	int k;
	/**
	 * The path to the data file.
	 */
	String path;
	/**
	 * The List containing the main dive data file.
	 */
	List<DiveData> data;
	/**
	 * The array containing the nearest neighbours.
	 */
	DiveData[] neighbours;
	/**
	 * Array containing the nearest neighbours distances for easier comparing.
	 */
	double[] distances;

	/**
	 * Stores the mean of each variable.
	 */
	DiveData scaleMean;
	/**
	 * Stores the standard deviation of each variable.
	 */
	DiveData scaleSD;

	public Classifier(String path, int k){
		this.path = path;
		this.k = k;
		neighbours = new DiveData[k];
		distances = new double[k];
	}

	/**
	 * Reads the data in and then loops over each case, classifying it from it's neighbours.
	 */
	public String start(){
		String result = "Value of k: " + k;
		try {
			readData();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		calculateScale(data.size());
		int correct = 0;
		for(int i = 0; i < data.size(); i++){
			initialiseDistances();
			String resultClassification = nearestNeighbours(i);

			//keeps track of the number of correct predictions
			if(resultClassification.equals(data.get(i).type)){
				correct++;
			}

		}
		result = result + "\n" + "Number correct: " + correct + "/" + data.size();
		//System.out.println("Number correct: " + correct + "/" + data.size());
		float percentage = (correct/(float) data.size())*100.0f;
		result = result + "\n" + "Percentage correct: " + percentage + "%";
		//System.out.println("Percentage correct: " + percentage + "%");
		return result;

	}

	/**
	 * Calculates the mean and standard deviation for each variable in the data.
	 * 
	 * @param size The number of cases in the data list.
	 */
	private void calculateScale(int size) {
		double[] meanDepth = new double[size];
		double[] medianDepth = new double[size];
		double[] sdDepth = new double[size];
		double[] iqrDepth = new double[size];
		double[] meanTemp = new double[size];
		double[] medianTemp = new double[size];
		double[] sdTemp = new double[size];
		double[] iqrTemp = new double[size];

		for(int i = 0; i < size; i++){
			meanDepth[i] = data.get(i).meanDepth;
			medianDepth[i] = data.get(i).medianDepth;
			sdDepth[i] = data.get(i).sdDepth;
			iqrDepth[i] = data.get(i).iqrDepth;
			meanTemp[i] = data.get(i).meanTemp;
			medianTemp[i] = data.get(i).medianTemp;
			sdTemp[i] = data.get(i).sdTemp;
			iqrTemp[i] = data.get(i).iqrTemp;
		}

		scaleMean = new DiveData(getMean(meanDepth), getMean(medianDepth), getMean(sdDepth), getMean(iqrDepth), getMean(meanTemp), getMean(medianTemp), getMean(sdTemp), getMean(iqrTemp), "Mean");
		scaleSD = new DiveData(getStdDeviation(meanDepth), getStdDeviation(medianDepth), getStdDeviation(sdDepth), getStdDeviation(iqrDepth), getStdDeviation(meanTemp), getStdDeviation(medianTemp), getStdDeviation(sdTemp), getStdDeviation(iqrTemp), "Standard Deviation");
	}

	/**
	 * Sets all the distances to -1 in the array.
	 */
	private void initialiseDistances() {
		for(int i = 0; i < k; i++){
			distances[i] = -1.0;
		}
	}

	/**
	 * Reads the data in from the data file and stores it all in a list of DiveData.
	 * @throws IOException
	 */
	private void readData() throws IOException{
		data = new ArrayList<DiveData>();

		BufferedReader br = new BufferedReader(new FileReader(path));
		String line;
		while ((line = br.readLine()) != null) {
			if(line.startsWith("MeanDepth")) continue;
			String [] variables = line.split("\\s+");
			DiveData temp = new DiveData(Double.parseDouble(variables[0]), Double.parseDouble(variables[1]), Double.parseDouble(variables[2]), Double.parseDouble(variables[3]), Double.parseDouble(variables[4]), Double.parseDouble(variables[5]), Double.parseDouble(variables[6]), Double.parseDouble(variables[7]), variables[8]);
			data.add(temp);
		}
		br.close();
	}

	/**
	 * Does the K Nearest Neighbour classification and returns the classification given.
	 * 
	 * @param i The index of the case being used.
	 * @return The classification given.
	 */
	private String nearestNeighbours(int i){
		for(int j = 0; j < data.size(); j++){
			//Skips over itself as a neighbour.
			if(j == i) continue;
			compareDistance(data.get(i), data.get(j));	
		}
		return findType(0);
	}

	/**
	 * Finds and returns the most frequently occurring classification type. Loops over 
	 * each neighbour in the nearest neighbours list and keeps track of the number of times each
	 * classification type occurs.
	 * 
	 * @param offset The number of neighbours to ignore at the end of the array. Used when there are multiple top types.
	 * @return The most common classification type.
	 */
	private String findType(int offset) {
		HashMap<String, Integer> type = new HashMap<String, Integer>();
		type.put("S", 0);
		type.put("T", 0);
		type.put("U", 0);
		type.put("V", 0);
		type.put("TX", 0);

		DiveData[] neighboursTemp = new DiveData[neighbours.length-offset];
		for(int i = 0; i < neighboursTemp.length; i++){
			neighboursTemp[i] = neighbours[i];
		}

		for(DiveData neighbour: neighboursTemp){
			type.put(neighbour.type, type.get(neighbour.type) + 1);
		}

		Integer largestVal = null;
		List<Entry<String, Integer>> mostFrequent = new ArrayList<Entry<String, Integer>>();
		for (Entry<String, Integer> entry : type.entrySet()){
			if (largestVal == null || largestVal  < entry.getValue()){
				largestVal = entry.getValue();
				mostFrequent.clear();
				mostFrequent.add(entry);
			}else if (largestVal == entry.getValue()){
				mostFrequent.add(entry);
			}
		}

		//Returns the first found most frequent type
		return mostFrequent.get(0).getKey();

		//returns the most frequent type if there is only one, otherwise chooses one at random if there are multiple.
		/*
		if(mostFrequent.size() == 1){
			return mostFrequent.get(0).getKey();
		}else{
			return mostFrequent.get(new Random().nextInt(mostFrequent.size())).getKey();
		}
		 */

		//Returns the most frequent type if there is only one, otherwise tries classifying with 1 fewer neighbours
		/*
		if(mostFrequent.size() == 1){
			return mostFrequent.get(0).getKey();
		}else{
			return findType(offset+1);
		}
		 */

	}

	/**
	 * Compares the calculated distance between the cases dataCase and neighbour with the current k
	 * nearest distances. If the distance is closer then all the distance values and neighbours are moved along
	 * to make space for the new distance and neighbour. 
	 * 
	 * @param dataCase The current case.
	 * @param neighbour The case the distance is calculated to.
	 */
	private void compareDistance(DiveData dataCase, DiveData neighbour){
		double distance;

		//distance = calculateDistanceScaled(dataCase, neighbour);
		distance = calculateDistance(dataCase, neighbour);
		
		int index = k-1;
		while(index >= 0 && distance < distances[index] || index >= 0 && distances[index] == -1.0 ){
			index--;
		}
		if(index+1 < 0 || index+1 >=k) return;
		int temp = k-1;

		//move all the stored distances and neighbours along the array
		//to make space for the new one to be added.
		//acts like a priority queue.
		while(temp > index+1){
			distances[temp] = distances[temp-1];
			neighbours[temp] = neighbours[temp-1];
			temp--;
		}
		distances[index+1] = distance;
		neighbours[index+1] = neighbour;
	}


	/**
	 * Calculates and returns the distance between the two cases given.
	 * Uses scaled data and two pairs of variables to calculate the distance.
	 * 
	 * @param dataCase The current case.
	 * @param neighbour The neighbour to calculate distance to.
	 * @return The Euclidean distance.
	 */
	private double calculateDistanceScaled(DiveData dataCase, DiveData neighbour){
		double distance = 0.0;

		double value2 = Math.abs(((dataCase.medianDepth - scaleMean.medianDepth)/scaleSD.medianDepth) - ((neighbour.medianDepth - scaleMean.medianDepth)/scaleSD.medianDepth));
		double value3 = Math.abs(((dataCase.sdDepth - scaleMean.sdDepth)/scaleSD.sdDepth) - ((neighbour.sdDepth - scaleMean.sdDepth)/scaleSD.sdDepth));

		double value5 = Math.abs(((dataCase.meanTemp - scaleMean.meanTemp)/scaleSD.meanTemp) - ((neighbour.meanTemp - scaleMean.meanTemp)/scaleSD.meanTemp));
		double value8 = Math.abs(((dataCase.iqrTemp - scaleMean.iqrTemp)/scaleSD.iqrTemp) - ((neighbour.iqrTemp - scaleMean.iqrTemp)/scaleSD.iqrTemp));

		distance = Math.sqrt(square(value2) + square(value3) + square(value5) + square(value8));
		return distance;
	}

	/**
	 * Calculates and returns the distance between the two cases given.
	 * Uses non-scaled data and two pairs of variables to calculate the distance.
	 * 
	 * @param dataCase The  current case.
	 * @param neighbour The  neighbour to calculate distance to.
	 * @return The Euclidean distance.
	 */
	private double calculateDistance(DiveData dataCase, DiveData neighbour){
		double distance = 0.0;

		double value6 = Math.abs(dataCase.medianTemp - neighbour.medianTemp);
		double value7 = Math.abs(dataCase.sdTemp - neighbour.sdTemp);
		double value8 = Math.abs(dataCase.iqrTemp - neighbour.iqrTemp);

		distance = Math.sqrt(square(value6) + square(value8) + square(value7) + square(value8));
		return distance;
	}

	/**
	 * Calculates and returns the distance between the two cases given. Uses all the 8 variables to calculate
	 * the distance, and uses non-scaled data.
	 * 
	 * @param dataCase The current case.
	 * @param neighbour The neighbour to calculate distance to.
	 * @return The Euclidean distance.
	 */
	private double calculateDistanceAll(DiveData dataCase, DiveData neighbour){
		double distance = 0.0;
		double value1 = Math.abs(dataCase.meanDepth - neighbour.meanDepth);
		double value2 = Math.abs(dataCase.medianDepth - neighbour.medianDepth);
		double value3 = Math.abs(dataCase.sdDepth - neighbour.sdDepth);
		double value4 = Math.abs(dataCase.iqrDepth - neighbour.iqrDepth);

		double value5 = Math.abs(dataCase.meanTemp - neighbour.meanTemp);
		double value6 = Math.abs(dataCase.medianTemp - neighbour.medianTemp);
		double value7 = Math.abs(dataCase.sdTemp - neighbour.sdTemp);
		double value8 = Math.abs(dataCase.iqrTemp - neighbour.iqrTemp);

		distance = Math.sqrt(square(value1) + square(value2) + square(value3) + square(value4) + square(value5) + square(value6) + square(value7)+ square(value8));
		return distance;
	}

	/**
	 * Calculates and returns the distance between the two cases given. Uses all the 8 variables to calculate
	 * the distance, and uses non-scaled data.
	 * 
	 * @param dataCase The current case.
	 * @param neighbour The i neighbour to calculate distance to.
	 * @return The Euclidean distance.
	 */
	private double calculateDistanceScaledAll(DiveData dataCase, DiveData neighbour){
		double distance = 0.0;
		double value1 = Math.abs(((dataCase.meanDepth - scaleMean.meanDepth)/scaleSD.meanDepth) - ((neighbour.meanDepth - scaleMean.meanDepth)/scaleSD.meanDepth));
		double value2 = Math.abs(((dataCase.medianDepth - scaleMean.medianDepth)/scaleSD.medianDepth) - ((neighbour.medianDepth - scaleMean.medianDepth)/scaleSD.medianDepth));
		double value3 = Math.abs(((dataCase.sdDepth - scaleMean.sdDepth)/scaleSD.sdDepth) - ((neighbour.sdDepth - scaleMean.sdDepth)/scaleSD.sdDepth));
		double value4 = Math.abs(((dataCase.iqrDepth - scaleMean.iqrDepth)/scaleSD.iqrDepth) - ((neighbour.iqrDepth - scaleMean.iqrDepth)/scaleSD.iqrDepth));

		double value5 = Math.abs(((dataCase.meanTemp - scaleMean.meanTemp)/scaleSD.meanTemp) - ((neighbour.meanTemp - scaleMean.meanTemp)/scaleSD.meanTemp));
		double value6 = Math.abs(((dataCase.medianTemp - scaleMean.medianTemp)/scaleSD.medianTemp) - ((neighbour.medianTemp - scaleMean.medianTemp)/scaleSD.medianTemp));
		double value7 = Math.abs(((dataCase.sdTemp - scaleMean.sdTemp)/scaleSD.sdTemp) - ((neighbour.sdTemp - scaleMean.sdTemp)/scaleSD.sdTemp));
		double value8 = Math.abs(((dataCase.iqrTemp - scaleMean.iqrTemp)/scaleSD.iqrTemp) - ((neighbour.iqrTemp - scaleMean.iqrTemp)/scaleSD.iqrTemp));

		distance = Math.sqrt(square(value1) + square(value2) + square(value3) + square(value4) + square(value5) + square(value6) + square(value7) + square(value8));
		return distance;
	}

	/**
	 * Returns the square of the given double.
	 * 
	 * @param value The given double to square.
	 * @return The squared result.
	 */
	private double square(double value){
		return value*value;
	}

	/**
	 * Calculates and returns the mean using the given array.
	 * 
	 * @param data The given array of data to calculate the mean from.
	 * @return The mean of the data.
	 */
	private double getMean(double[] data){
		int size = data.length;
		double total = 0.0;
		for(double temp : data){
			total = total + temp;
		}
		return total/size;
	}

	/**
	 * Calculates and returns the standard deviation of the given data array.
	 * 
	 * @param data The given array of data.
	 * @return The standard deviation of the data.
	 */
	private double getStdDeviation(double[] data){
		double mean = getMean(data);
		double temp = 0;
		int size = data.length;
		for(double a :data){
			temp += (mean-a)*(mean-a);
		} 
		return Math.sqrt(temp/size);
	}

}
