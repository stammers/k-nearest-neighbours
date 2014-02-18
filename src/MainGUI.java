import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class MainGUI {

	private File f = null;
	private JTextField k;
	private JFrame frame;
	
	public static void main(String[] args) {
		final MainGUI gui = new MainGUI();
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				gui.go();
			}
			
		});
		
	}
	
	/**
	 * Initialises the JFrame and it's components and displays the 
	 * JFileChooser for the user to select the data file to use.
	 */
	private void go(){		
		frame = new JFrame("Enter Value for K:");
		JPanel panel = new JPanel(new GridLayout(0, 1));
		frame.setContentPane(panel);

		k = new JTextField();
		panel.add(k);
		JButton go = new JButton("Start Classifier");
		panel.add(go);
		
		k.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				k.setText("");			
			}		
		});	
		
		go.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				startClassifier();	
			}
			
		});

		frame.setSize(250, 150);

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		int returnVal = fileChooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			f = fileChooser.getSelectedFile();
			frame.setVisible(true);
		}
	}
	
	private void startClassifier(){
		int kValue = 0;
		try{
			kValue = Integer.parseInt(k.getText());
		}catch(NumberFormatException e){
			k.setText("Enter a valid number");
		}
		try {
			if(f != null && checkFile()){
				Classifier classifier = new Classifier(f.getAbsolutePath(), kValue);
				frame.setVisible(false);
				JOptionPane.showMessageDialog(frame, classifier.start());
				frame.dispose();
			}else{
				JOptionPane.showMessageDialog(frame, "Invalid File");
				frame.dispose();
				go();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * Checks if the selected file is in the correct format.
	 * 
	 * @return If the file is valid
	 * @throws IOException
	 */
	private boolean checkFile() throws IOException{

			BufferedReader br = new BufferedReader(new FileReader(f.getAbsolutePath()));
			String line = br.readLine();
			br.close();
			if(line.startsWith("MeanDepth")){
				return true;
			}else{
				return false;
			}
		
	}

}
