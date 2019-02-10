// Do not submit with package statements if you are using eclipse.
// Only use what is provided in the standard libraries.

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class NaiveBayes {
	
	//Working directory
	private String cwd = System.getProperty("user.dir");
	//spam emails directory from working directory
	private File spamDir = new File(cwd+"\\data\\train\\spam");
	//ham emails directory from working directory
	private File hamDir = new File(cwd+"\\data\\train\\ham");
	//test emails directory from working directory
	private File testDir = new File(cwd+"\\data\\test");
	//number of spam emails
	private double spamEmails = spamDir.listFiles().length;
	//number of ham emails
	private double hamEmails = hamDir.listFiles().length;
	
	//HashMap that counts number of times a word is seen in the set of spam emails
	private HashMap<String, Integer> spamWords = new HashMap<String, Integer>();
	//HashMap that counts number of times a word is seen in the set of ham emails
	private HashMap<String, Integer> hamWords = new HashMap<String, Integer>();
	//HashMap that stores the calculated probability of a word being in a spam email
	private HashMap<String, Double> spamProb = new HashMap<String, Double>();
	//HashMap that stores the calculated probability of a word being in a ham email
	private HashMap<String, Double> hamProb = new HashMap<String, Double>();
	//Stores the calculated "probability" of spam emails by dividing the amount of spam in the testing set by the total amount of emails
	private Double pSpam;
	//Stores the calculated "probability" of ham emails by dividing the amount of ham in the testing set by the total amount of emails
	private Double pHam;
	
    /*
     * !! DO NOT CHANGE METHOD HEADER !!
     * If you change the method header here, our grading script won't
     * work and you will lose points!
     * 
     * Train your Naive Bayes Classifier based on the given training
     * ham and spam emails.
     *
     * Params:
     *      hams - email files labeled as 'ham'
     *      spams - email files labeled as 'spam'
     */
    public void train(File[] hams, File[] spams) throws IOException {
    	
    	//Adds words that appeared in spam to spamsWords hashmap that keeps track of number of times each word is used in spam emails.
    	//No smoothing is done at this point
    	for(File spamword : spams) {
    		HashSet<String> spam = tokenSet(spamword);
    		for(String word : spam) {
    			if(this.spamWords.containsKey(word)) {
    				this.spamWords.put(word, this.spamWords.get(word)+1);
    			}
    			else{ this.spamWords.put(word, 1); }
    		}
    	}
    	
    	//Adds words that appeared in ham to hamsWords hashmap that keeps track of number of times each word is used in ham emails.
    	//No smoothing is done at this point
    	for(File hamword : hams) {
    		HashSet<String> ham = tokenSet(hamword);
    		for(String word : ham) {
    			if(this.hamWords.containsKey(word)) {
    				this.hamWords.put(word, this.hamWords.get(word)+1);
    			}
    			else{ this.hamWords.put(word, 1); }
    		}
    	}
    	
    	//Calculate and store the probability of each word from the spam/hamWords maps appearing in spam and ham.
    	
    	//P(w | S), +1 numerator and +2 denominator for smoothing
    	for(String word : spamWords.keySet()) {
    		this.spamProb.put(word, (spamWords.get(word).doubleValue()+1)/(this.spamEmails+2));
    	}
    	//P(w | H), +1 numerator and +2 denominator for smoothing
    	for(String word : hamWords.keySet()) {
    		this.hamProb.put(word, (hamWords.get(word).doubleValue()+1)/(this.hamEmails+2));
    	}
    	
    	//Compute P(Spam)
    	this.pSpam = this.spamEmails/(this.spamEmails+this.hamEmails);
    	
    	//Compute P(Ham)
    	this.pHam = this.hamEmails/(this.spamEmails+this.hamEmails);
    	
    }

    /*
     * !! DO NOT CHANGE METHOD HEADER !!
     * If you change the method header here, our grading script won't
     * work and you will lose points!
     *
     * Classify the given unlabeled set of emails. Follow the format in
     * example_output.txt and output your result to stdout. Note the order
     * of the emails in the output does NOT matter.
     * 
     * Do NOT directly process the file paths, to get the names of the
     * email files, check out File's getName() function.
     *
     * Params:
     *      emails - unlabeled email files to be classified
     */
    public void classify(File[] emails) throws IOException {
    	
    	//This check might be unnecessary with SpamFilterMain
    	if(this.spamDir.exists() && this.hamDir.exists() && this.testDir.exists()) {
        	
        	//Use the train method to prepare spamWords and hamWords HashMaps
        	train(this.hamDir.listFiles(), this.spamDir.listFiles());
        	
        	//Iterate through each email, marking each as either spam or ham
        	//Seems emails are loaded in an unintended order, being {1.txt,10.txt,100.txt...} instead of {1.txt,2.txt,3.txt...)
        	for(File email : emails) {
        		
        		HashSet<String> wordSet = tokenSet(email);
        		
        		//Used to calculate sum of all P(Xi | S) + P(S)
            	double probWordSpam = Math.log10(this.pSpam);
            	//Used to calculate sum of P(Xi | H) + P(H)
            	double probWordHam = Math.log10(this.pHam);
        		
        		for(String word : wordSet) {
        			if(this.spamProb.containsKey(word)) {
        				probWordSpam += Math.log10(this.spamProb.get(word));
        			}
        			else {
        				probWordSpam += Math.log10(1/this.spamEmails);
        			}
        			if(this.hamProb.containsKey(word)) {
        				probWordHam += Math.log10(this.hamProb.get(word));
        			}
        			else {
        				probWordHam += Math.log10(1/this.hamEmails);
        			}
        		}
        		if(probWordSpam > probWordHam) {
        			System.out.println(email.getName()+" spam");
            	}
            	else {
            		System.out.println(email.getName()+" ham");
            	}
        	}
    	}
    }


    /*
     *  Helper Function:
     *  This function reads in a file and returns a set of all the tokens. 
     *  It ignores "Subject:" in the subject line.
     *  
     *  If the email had the following content:
     *  
     *  Subject: Get rid of your student loans
     *  Hi there ,
     *  If you work for us , we will give you money
     *  to repay your student loans . You will be 
     *  debt free !
     *  FakePerson_22393
     *  
     *  This function would return to you
     *  ['be', 'student', 'for', 'your', 'rid', 'we', 'of', 'free', 'you', 
     *   'us', 'Hi', 'give', '!', 'repay', 'will', 'loans', 'work', 
     *   'FakePerson_22393', ',', '.', 'money', 'Get', 'there', 'to', 'If', 
     *   'debt', 'You']
     */
    public static HashSet<String> tokenSet(File filename) throws IOException {
        HashSet<String> tokens = new HashSet<String>();
        Scanner filescan = new Scanner(filename);
        filescan.next(); // Ignoring "Subject"
        while(filescan.hasNextLine() && filescan.hasNext()) {
            tokens.add(filescan.next());
        }
        filescan.close();
        return tokens;
    }
}
