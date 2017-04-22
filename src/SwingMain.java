import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.stemmers.IteratedLovinsStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import java.awt.FlowLayout;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Dimension;
import javax.swing.JSpinner;
import javax.swing.JComboBox;

public class SwingMain extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private Instances instances;
	private Instances dataset = null;
	private Instances filteredDataset = null;
	private JTextPane textPane;
	JComboBox comboBox;
	
	private StringToWordVector filter;
	private FilteredClassifier fcNaiveBayes = null;
	private FilteredClassifier fcJ48 = null;
	private Vote voter = null;
	private FilteredClassifier fcSMO = null;
	
	private String[] availableClassifiers = { "NaiveBayes", "Voting", "J48",
		      "SMO" };

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingMain frame = new SwingMain();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		
	}

	/**
	 * Create the frame.
	 */
	public SwingMain() {
		
		setTitle("Detect spam");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		setSize(450, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblLoadTrainingDataset = new JLabel("Load Training Dataset");
		lblLoadTrainingDataset.setBounds(67, 16, 144, 16);
		contentPane.add(lblLoadTrainingDataset);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.setToolTipText("Click to load spamham.arff");
		btnLoad.setBounds(238, 11, 117, 29);
		btnLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dataset == null){
					DataSource source;
					try {
						source = new DataSource("/Users/amaryadav/Desktop/spamham.arff");
						dataset = source.getDataSet();
						//set class index to the last attribute
						dataset.setClassIndex(dataset.numAttributes()-1);
						filter = new StringToWordVector();
						filter.setInputFormat(dataset);
						filter.setIDFTransform(true);
						filter.setTFTransform(true);
						filter.setOutputWordCounts(true);
						IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
						filter.setStemmer(stemmer);
						filter.setLowerCaseTokens(true);
						filteredDataset = Filter.useFilter(dataset, filter);
					} catch (Exception e1) {
						textPane.setText(e1.getMessage());
						e1.printStackTrace();
						return;
					}
				}

				textPane.setText("Loading spamham.arff was successful!");
//				JOptionPane.showMessageDialog(frame, "This is the message...", "Title");
				
			}
		});
		contentPane.add(btnLoad);
		
		JButton btnNaivebayes = new JButton("NaiveBayes");
		btnNaivebayes.setBounds(6, 68, 117, 29);
		btnNaivebayes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dataset!=null && fcNaiveBayes == null){
					NaiveBayes tree = new NaiveBayes();
					
					StringToWordVector filter = new StringToWordVector();
					try {
						filter.setInputFormat(dataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					filter.setIDFTransform(true);
					filter.setTFTransform(true);
					filter.setOutputWordCounts(true);
					IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
					filter.setStemmer(stemmer);
					fcNaiveBayes = new FilteredClassifier();
					fcNaiveBayes.setFilter(filter);
					fcNaiveBayes.setClassifier(tree);
					try {
						fcNaiveBayes.buildClassifier(dataset);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					try {
						
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcNaiveBayes, dataset);
						textPane.setText(tree+"\n\n==================================================\n\n"+eval.toSummaryString()+"==================================================\n\n"+eval.toMatrixString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
				
			}
		});
		contentPane.add(btnNaivebayes);
		
		JButton btnJ = new JButton("J48");
		btnJ.setBounds(245, 68, 81, 29);
		btnJ.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dataset!=null && fcJ48 == null){
					J48 tree = new J48();
					
					StringToWordVector filter = new StringToWordVector();
					try {
						filter.setInputFormat(dataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					filter.setIDFTransform(true);
					filter.setTFTransform(true);
					filter.setOutputWordCounts(true);
					IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
					filter.setStemmer(stemmer);

					fcJ48 = new FilteredClassifier();
					fcJ48.setFilter(filter);
					fcJ48.setClassifier(tree);
					try {
						fcJ48.buildClassifier(dataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					try {	
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcJ48, dataset);
						textPane.setText(tree+"\n\n==================================================\n\n"+eval.toSummaryString()+"==================================================\n\n"+eval.toMatrixString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		contentPane.add(btnJ);
		
		JButton btnVotin = new JButton("Voting");
		btnVotin.setBounds(124, 68, 117, 29);
		btnVotin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(filteredDataset!=null && voter == null){
					Classifier[] classifiers = {				
							new BayesNet(),
							new NaiveBayesMultinomial()
					};
					
					voter = new Vote();
					voter.setClassifiers(classifiers);//needs one or more classifiers
					try {
						voter.buildClassifier(filteredDataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
					
					try {	
						Evaluation eval = new Evaluation(filteredDataset);
						eval.evaluateModel(voter, filteredDataset);
						textPane.setText("\n\n==================================================\n\n"+eval.toSummaryString()+"==================================================\n\n"+eval.toMatrixString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		contentPane.add(btnVotin);
		
		JButton btnSmo = new JButton("SMO");
		btnSmo.setBounds(327, 68, 117, 29);
		btnSmo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(dataset!=null && fcSMO == null){
					SMO tree = new SMO();
					StringToWordVector filter = new StringToWordVector();
					try {
						filter.setInputFormat(dataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					filter.setIDFTransform(true);
					filter.setTFTransform(true);
					filter.setOutputWordCounts(true);
					IteratedLovinsStemmer stemmer = new IteratedLovinsStemmer();
					filter.setStemmer(stemmer);
					
					fcSMO = new FilteredClassifier();
					fcSMO.setFilter(filter);
					fcSMO.setClassifier(tree);
					try {
						fcSMO.buildClassifier(dataset);
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					try {	
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcSMO, dataset);
						textPane.setText(tree+"\n\n==================================================\n\n"+eval.toSummaryString()+"==================================================\n\n"+eval.toMatrixString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		contentPane.add(btnSmo);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 109, 416, 104);
		contentPane.add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setToolTipText("Evaluation Summary");
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		JButton btnGo = new JButton("GO");
		btnGo.setBounds(315, 322, 117, 29);
		btnGo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				switch(comboBox.getSelectedIndex()){
				case 0:
					if(fcNaiveBayes != null){
						if(textField.getText() != ""){
							makeInstance(textField.getText());
							try {
								Double val = fcNaiveBayes.classifyInstance(instances.firstInstance());
								if(val == 0){
									textPane.setText("NOT SPAM :)");
								}else if(val == 1){
									textPane.setText("SPAM DETECTED! *__*");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						textPane.setText("Train NaiveBayes first!");
					}
					break;
				case 1:
					if(voter != null){
						if(textField.getText() != ""){
							makeInstance(textField.getText());
							try {
								Instances filteredInstance = Filter.useFilter(instances, filter);
								Double val = voter.classifyInstance(filteredInstance.firstInstance());
								if(val == 0){
									textPane.setText("NOT SPAM :)");
								}else if(val == 1){
									textPane.setText("SPAM DETECTED! *__*");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						textPane.setText("Train voter first!");
					}
					break;
				case 2:
					if(fcJ48 != null){
						if(textField.getText() != ""){
							makeInstance(textField.getText());
							try {
								Double val = fcJ48.classifyInstance(instances.firstInstance());
								if(val == 0){
									textPane.setText("NOT SPAM :)");
								}else if(val == 1){
									textPane.setText("SPAM DETECTED! *__*");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						textPane.setText("Train J48 first!");
					}
					break;
				case 3:
					if(fcSMO != null){
						if(textField.getText() != ""){
							makeInstance(textField.getText());
							try {
								Double val = fcSMO.classifyInstance(instances.firstInstance());
								if(val == 0){
									textPane.setText("NOT SPAM :)");
								}else if(val == 1){
									textPane.setText("SPAM DETECTED! *__*");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						textPane.setText("Train SMO first!");
					}
					break;
				default:
					textPane.setText("Please select a classifier first!");
				}
			}
		});
		contentPane.add(btnGo);
		
		textField = new JTextField();
		textField.setBounds(183, 226, 249, 70);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblEnterTextTo = new JLabel("Enter text to classify");
		lblEnterTextTo.setBounds(19, 238, 138, 16);
		contentPane.add(lblEnterTextTo);
		
		comboBox = new JComboBox();
		comboBox.setBounds(16, 323, 197, 27);
		for(int i = 0; i<availableClassifiers.length; i++){
			comboBox.addItem(availableClassifiers[i]);
		}

		contentPane.add(comboBox);
	}
	
	public void makeInstance(String text) {
		FastVector fvNominalVal = new FastVector(2);
		fvNominalVal.addElement("ham");
		fvNominalVal.addElement("spam");
		
		Attribute attribute1 = new Attribute("text",(FastVector) null);
		Attribute attribute2 = new Attribute("class", fvNominalVal);
		
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		instances = new Instances("Test relation", fvWekaAttributes, 1);
		instances.setClassIndex(1);
		Instance instance = new Instance(2);
		instance.setValue(attribute1, text);
		instances.add(instance);
	}
}
