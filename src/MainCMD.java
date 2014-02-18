


public class MainCMD {

	public static void main(String[] args) {
		Classifier classifier = new Classifier(args[0], Integer.parseInt(args[1]));
		System.out.println(classifier.start());
	}
	


}
