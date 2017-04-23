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
import java.io.File;

import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.Dimension;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.ScrollPaneConstants;
import javax.swing.Box;

public class SwingMain extends JFrame {

	private JPanel contentPane;
//	private JTextField textPane_1;
	private JTextPane textPane_1;
	private Instances instances;
	private Instances dataset = null;
	private Instances filteredDataset = null;
	private JTextPane textPane;
	JComboBox comboBox;
	
	private StringToWordVector filter;
	
	private FilteredClassifier fcNaiveBayes = null;
	private Evaluation eval_NB = null;
	private NaiveBayes treeNB = null;
	
	private FilteredClassifier fcJ48 = null;
	private Evaluation eval_J48 = null;
	private J48 treeJ48 = null;
	
	private Vote voter = null;
	private Evaluation eval_voter = null;
	private Classifier[] classifiers = {				
			new BayesNet(),
			new NaiveBayesMultinomial()
	};
	
	private FilteredClassifier fcSMO = null;
	private Evaluation eval_fcSMO = null;
	private SMO treeSMO = null;
	
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
		//450, 400
		setSize(1024, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblLoadTrainingDataset = new JLabel("Load Training Dataset");
		lblLoadTrainingDataset.setBounds(303, 32, 144, 16);
		contentPane.add(lblLoadTrainingDataset);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(26, 135, 418, 335);
		contentPane.add(scrollPane);
		
		textPane = new JTextPane();
		textPane.setToolTipText("Evaluation Summary");
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setBounds(469, 135, 534, 540);
		contentPane.add(scrollPane_1);
		
		JTextPane textPane_2 = new JTextPane();
		textPane_2.setEditable(false);
		scrollPane_1.setViewportView(textPane_2);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.setToolTipText("Click to load spamham.arff");
		btnLoad.setBounds(469, 27, 117, 29);
		btnLoad.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				File selectedFile = null;
//				if(dataset == null){
					DataSource source;
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					int result = fileChooser.showOpenDialog(SwingMain.this);
					if (result == JFileChooser.APPROVE_OPTION) {
					    // user selects a file
						try {
							selectedFile = fileChooser.getSelectedFile();
							source = new DataSource(selectedFile.getAbsolutePath());
//							source = new DataSource("/Users/amaryadav/Desktop/spamham.arff");
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
							JOptionPane.showMessageDialog(null, selectedFile.getName() + " was successfully loaded");
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(null, e1.getMessage());
							e1.printStackTrace();
							return;
						}
						
						fcNaiveBayes = null;
						voter = null;
						fcJ48 = null;
						fcSMO = null;
						
						textPane.setText("");
						textPane_2.setText("");
					}
//				}				
			}
		});
		contentPane.add(btnLoad);
		
		JButton btnNaivebayes = new JButton("NaiveBayes");
		btnNaivebayes.setBounds(469, 68, 128, 55);
		btnNaivebayes.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dataset!=null && fcNaiveBayes == null){
					treeNB = new NaiveBayes();
					
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
					fcNaiveBayes.setClassifier(treeNB);
					try {
						fcNaiveBayes.buildClassifier(dataset);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					
					try {
						
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcNaiveBayes, dataset);
						eval_NB = eval;
						textPane.setText(eval_NB.toSummaryString()+"\n"+eval_NB.toMatrixString());
						textPane_2.setText(treeNB+"");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}else if(dataset == null){
					JOptionPane.showMessageDialog(null, "Load dataset first!");
				}else{
					try {
						textPane.setText(eval_NB.toSummaryString()+"\n"+eval_NB.toMatrixString());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					textPane_2.setText(treeNB+"");
				}
				
			}
		});
		contentPane.add(btnNaivebayes);
		
		JButton btnJ = new JButton("J48");
		btnJ.setBounds(768, 68, 106, 55);
		btnJ.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(dataset!=null && fcJ48 == null){
					treeJ48 = new J48();
					
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
					fcJ48.setClassifier(treeJ48);
					try {
						fcJ48.buildClassifier(dataset);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					try {	
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcJ48, dataset);
						eval_J48 = eval;
						textPane.setText(eval_J48.toSummaryString()+"\n"+eval_J48.toMatrixString());
						textPane_2.setText(treeJ48.graph());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}else if(dataset == null){
					JOptionPane.showMessageDialog(null, "Load dataset first!");
				}else{
					try {
						textPane.setText(eval_J48.toSummaryString()+"\n"+eval_J48.toMatrixString());
						textPane_2.setText(treeJ48.graph());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		contentPane.add(btnJ);
		
		JButton btnVotin = new JButton("Voting");
		btnVotin.setBounds(619, 68, 128, 55);
		btnVotin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(filteredDataset!=null && voter == null){
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
						eval_voter = eval;
						textPane.setText(eval_voter.toSummaryString()+"\n"+eval.toMatrixString());
						textPane_2.setText(classifiers[0].toString()+"\n\n=========================================\n\n"+classifiers[1].toString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}else if(dataset == null){
					JOptionPane.showMessageDialog(null, "Load dataset first!");
				}else{
					try {
						textPane.setText(eval_voter.toSummaryString()+"\n"+eval_voter.toMatrixString());
						textPane_2.setText(classifiers[0].toString()+"\n\n=========================================\n\n"+classifiers[1].toString());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		contentPane.add(btnVotin);
		
		JButton btnSmo = new JButton("SMO");
		btnSmo.setBounds(897, 68, 106, 55);
		btnSmo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(dataset!=null && fcSMO == null){
					treeSMO = new SMO();
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
					fcSMO.setClassifier(treeSMO);
					try {
						fcSMO.buildClassifier(dataset);
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}
					
					try {	
						Evaluation eval = new Evaluation(dataset);
						eval.evaluateModel(fcSMO, dataset);
						eval_fcSMO = eval;
						textPane.setText(eval_fcSMO.toSummaryString() + "\n" + eval_fcSMO.toMatrixString());
						textPane_2.setText(treeSMO+"");
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}else if(dataset == null){
					JOptionPane.showMessageDialog(null, "Load dataset first!");
				}else{ //dataset!=null && fcSMO != null
					try {
						textPane.setText(eval_fcSMO.toSummaryString() + "\n" + eval_fcSMO.toMatrixString());
						textPane_2.setText(treeSMO+"");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}				
				}
			}
		});
		contentPane.add(btnSmo);
		
		JLabel lblEnterTextTo = new JLabel("Enter text to classify");
		lblEnterTextTo.setBounds(35, 490, 138, 16);
		contentPane.add(lblEnterTextTo);
		
		comboBox = new JComboBox();
		comboBox.setBounds(26, 652, 197, 27);
		for(int i = 0; i<availableClassifiers.length; i++){
			comboBox.addItem(availableClassifiers[i]);
		}

		contentPane.add(comboBox);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane_2.setBounds(26, 522, 418, 107);
		contentPane.add(scrollPane_2);
		
		JTextPane textPane_1_1 = new JTextPane();
		scrollPane_2.setViewportView(textPane_1_1);
		
		JButton btnGo = new JButton("GO");
		btnGo.setBounds(284, 651, 117, 29);
		btnGo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				switch(comboBox.getSelectedIndex()){
				case 0:
					if(fcNaiveBayes != null){
						if(textPane_1_1.getText() != ""){
							makeInstance(textPane_1_1.getText());
							try {
								Double val = fcNaiveBayes.classifyInstance(instances.firstInstance());
								if(val == 0){
									JOptionPane.showMessageDialog(null," NOT SPAM (-__-) ");
								}else if(val == 1){
									JOptionPane.showMessageDialog(null," SPAM DETECTED (*__*) ");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						JOptionPane.showMessageDialog(null," Train NaiveBayes first! ");
					}
					break;
				case 1:
					if(voter != null){
						if(textPane_1_1.getText() != ""){
							makeInstance(textPane_1_1.getText());
							try {
								Instances filteredInstance = Filter.useFilter(instances, filter);
								Double val = voter.classifyInstance(filteredInstance.firstInstance());
								if(val == 0){
									JOptionPane.showMessageDialog(null," NOT SPAM (-__-) ");
								}else if(val == 1){
									JOptionPane.showMessageDialog(null," SPAM DETECTED (*__*) ");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						JOptionPane.showMessageDialog(null," Train Voting Classifier first! ");
					}
					break;
				case 2:
					if(fcJ48 != null){
						if(textPane_1_1.getText() != ""){
							makeInstance(textPane_1_1.getText());
							try {
								Double val = fcJ48.classifyInstance(instances.firstInstance());
								if(val == 0){
									JOptionPane.showMessageDialog(null," NOT SPAM (-__-) ");
								}else if(val == 1){
									JOptionPane.showMessageDialog(null," SPAM DETECTED (*__*) ");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						JOptionPane.showMessageDialog(null," Train J48 first! ");
					}
					break;
				case 3:
					if(fcSMO != null){
						if(textPane_1_1.getText() != ""){
							makeInstance(textPane_1_1.getText());
							try {
								Double val = fcSMO.classifyInstance(instances.firstInstance());
								if(val == 0){
									JOptionPane.showMessageDialog(null," NOT SPAM (-__-) ");
								}else if(val == 1){
									JOptionPane.showMessageDialog(null," SPAM DETECTED (*__*) ");
								}
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}else{
						JOptionPane.showMessageDialog(null," Train SMO first! ");
					}
					break;
				default:
					textPane.setText("Please select a classifier first!");
				}
			}
		});
		contentPane.add(btnGo);
		
		JLabel lblClickToTrain = new JLabel("Click to train classifiers");
		lblClickToTrain.setBounds(288, 86, 150, 16);
		contentPane.add(lblClickToTrain);
		
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
