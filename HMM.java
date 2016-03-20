//************************************************************************************************
// Author: Tianlong Song
// Name: HMM.java
// Description: Hidden Markov Model for POS tagging 
// Date created: 02/03/2015 
//************************************************************************************************

import java.io.*;
import java.util.*;

class HMM {
	private	double lambda;
	private String [] tagSet;
	private	HashMap<String,Double> probTags = new HashMap<String,Double>();
	private	HashMap<String,Double> probEmission = new HashMap<String,Double>();
	private	HashMap<String,Double> probTransition = new HashMap<String,Double>();
	private boolean isTrained = false;
	private boolean isTested = false;
	private boolean isEvaluated = false;

	// Constructor: initialize the lambda
	HMM(double lambda) {
		this.lambda = lambda;
	}

	// Check whether the model is trained
	public boolean getIsTrained() {
		return isTrained;
	}

	// check whether the model is tested
	public boolean getIsTested() {
		return isTested;
	}

	// Check whether the model is evaluated
	public boolean getIsEvaluated() {
		return isEvaluated;
	}

	// Training
	public void training() throws Exception {
		// Intermediate tables needed in training
		HashMap<String,Integer> countTags = new HashMap<String,Integer>();
		HashMap<String,Integer> countWords = new HashMap<String,Integer>();
		HashMap<String,Integer> countEmission = new HashMap<String,Integer>();
		HashMap<String,Integer> countTransition = new HashMap<String,Integer>();
		HashSet<String> unknownWords = new HashSet<String>();
		HashMap<String,Integer> countEmissionUNKA = new HashMap<String,Integer>();
		// File reading related
		FileReader fr = new FileReader("training.txt");
		if(fr==null) return;
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		int sizeCorpus = 0;

		// Count words, tags, emission and transitions 
	     	while((line=br.readLine())!=null) {
			String [] strInLine = line.trim().split("[ ]+");
			for(int i=0;i<strInLine.length;i+=2) {
				// Count words 
				if(countWords.containsKey(strInLine[i]))
					countWords.put(strInLine[i],countWords.get(strInLine[i])+1);
				else
					countWords.put(strInLine[i],1);
				// Count tags
				if(countTags.containsKey(strInLine[i+1]))
					countTags.put(strInLine[i+1],countTags.get(strInLine[i+1])+1);
				else
					countTags.put(strInLine[i+1],1);
				// Count emission 
				String wordTag = strInLine[i] + " " + strInLine[i+1];
				if(countEmission.containsKey(wordTag))
					countEmission.put(wordTag,countEmission.get(wordTag)+1);
				else
					countEmission.put(wordTag,1);
				// Count tag transitions: t_i|t_{i-1}
				if(i>0)	{ // Skips i==0, since transition needs two tags
					String tagTag = strInLine[i+1] + " " + strInLine[i-1];
					if(countTransition.containsKey(tagTag))
						countTransition.put(tagTag,countTransition.get(tagTag)+1);
					else
						countTransition.put(tagTag,1);
				}	
			}

			// Deal with "START" and "END"
			if(countTags.containsKey("START"))
				countTags.put("START",countTags.get("START")+1);
			else
				countTags.put("START",1);
			if(countTags.containsKey("END"))
				countTags.put("END",countTags.get("END")+1);
			else
				countTags.put("END",1);
			String specialTagTag = strInLine[1] + " " + "START";
			if(countTransition.containsKey(specialTagTag))
				countTransition.put(specialTagTag,countTransition.get(specialTagTag)+1);
			else
				countTransition.put(specialTagTag,1);
			specialTagTag = "END" + " " + strInLine[strInLine.length-1];
			if(countTransition.containsKey(specialTagTag))
				countTransition.put(specialTagTag,countTransition.get(specialTagTag)+1);
			else
				countTransition.put(specialTagTag,1);

			// Accumulate to calculate the size of corpus
			sizeCorpus += strInLine.length/2;
		}

		// Identify the words that occurs less than five times, and hash them into the unknown word set
		Iterator<Map.Entry<String,Integer>> itWord = countWords.entrySet().iterator();
		while(itWord.hasNext()) {
			Map.Entry<String,Integer> pair = itWord.next();
			if(pair.getValue()<5)
				unknownWords.add(pair.getKey());
		}

		// Calculate the probabilities of words given tags (emission probabilities)
		Iterator<Map.Entry<String,Integer>> itWordTag = countEmission.entrySet().iterator();
		while(itWordTag.hasNext()) {
			Map.Entry<String,Integer> pair = itWordTag.next();
			String [] wordTag = pair.getKey().split("[ ]+");
			if(unknownWords.contains(wordTag[0])) { // Unknown words: UNKA
				String wordTagUNKA = "UNKA" + " " + wordTag[1];
				if(countEmissionUNKA.containsKey(wordTagUNKA))
					countEmissionUNKA.put(wordTagUNKA,countEmissionUNKA.get(wordTagUNKA)+pair.getValue());
				else
					countEmissionUNKA.put(wordTagUNKA,pair.getValue());
			} else { // Known words
				probEmission.put(pair.getKey(),1.0*pair.getValue()/countTags.get(wordTag[1]));
			}
		}
		Iterator<Map.Entry<String,Integer>> itWordTagUNKA = countEmissionUNKA.entrySet().iterator();
		while(itWordTagUNKA.hasNext()) {
			Map.Entry<String,Integer> pair = itWordTagUNKA.next();
			String [] wordTagUNKA = pair.getKey().split("[ ]+");
			probEmission.put(pair.getKey(),1.0*pair.getValue()/countTags.get(wordTagUNKA[1]));
		}
		
		// Calculte the tag probabilities and tag transition probabilities
		Iterator<Map.Entry<String,Integer>> itTag = countTags.entrySet().iterator();
		int numTags = countTags.size();
		String [] str = new String[numTags]; 
		int k = 0;
		while(itTag.hasNext()) {
			Map.Entry<String,Integer> pair = itTag.next();
			str[k] = pair.getKey();
			probTags.put(str[k],1.0*pair.getValue()/sizeCorpus);
		       	k++;	
		}
		tagSet = str;
		Iterator<Map.Entry<String,Integer>> itTagTag = countTransition.entrySet().iterator();
		while(itTagTag.hasNext()) {
			Map.Entry<String,Integer> pair = itTagTag.next();
			String [] tagTag = pair.getKey().split("[ ]+");
			double probTagTagML = 1.0*pair.getValue()/countTags.get(tagTag[1]);
			double prob = lambda*(probTagTagML) + (1-lambda)*probTags.get(tagTag[0]);
			probTransition.put(pair.getKey(),prob);	
		}

		br.close();

		// Label the model as "trained"
		isTrained = true;
	}

	// Testing
	public void testing() throws Exception {
		// File reading related
		FileReader fr = new FileReader("testing.txt");
		if(fr==null) return;
		BufferedReader br = new BufferedReader(fr);
		String line = null;
	     	// File writing related
		FileWriter fw = new FileWriter("report.txt");
		BufferedWriter bw = new BufferedWriter(fw);
		while((line=br.readLine())!=null) {
			String [] strInLine = line.trim().split("[ ]+");
			String [] tags = new String[strInLine.length];
			double logProb = viterbi(strInLine,tags);
			for(int i=0;i<tags.length;i++) {
				bw.write(strInLine[i] + " " + tags[i] + " ");
			}
			bw.write("LogProb:" + Double.toString(logProb));
			bw.newLine();
		}
		
		br.close();
		bw.close();

		// Label the model as "tested"
		isTested = true;
	}

	// Finding the most probable path: Viterbi algorithm
	public double viterbi(String [] line, String [] tags) {
		// Initialization
		int N = line.length;
		int M = tagSet.length;
		double vScore[][] = new double[M][N];
		int backPTR[][] = new int[M][N];
		for(int i=0;i<M;i++) {
			String wordTag = line[0] + " " + tagSet[i];
			String tagTag = tagSet[i] + " " + "START";
			double probWordTag = probEmission.containsKey(wordTag)?probEmission.get(wordTag):0.0;
			double probTagTag = probTransition.containsKey(tagTag)?probTransition.get(tagTag):0.0;
			vScore[i][0] = Math.log(probWordTag) + Math.log(probTagTag);
			backPTR[i][0] = -1;
		}

		// Iteration
		for(int j =1;j<N;j++) {
			for(int i=0;i<M;i++) {
				vScore[i][j] = Double.NEGATIVE_INFINITY;
				for(int k=0;k<M;k++) {
					String tagTag = tagSet[i] + " " + tagSet[k];
					String wordTag = line[j] + " " + tagSet[i];
					double probTagTag = probTransition.containsKey(tagTag)?probTransition.get(tagTag):0.0;
					double probWordTag = probEmission.containsKey(wordTag)?probEmission.get(wordTag):0.0;
					double score = vScore[k][j-1] + Math.log(probTagTag) + Math.log(probWordTag);
					if(score>vScore[i][j]) {
						vScore[i][j] = score;
						backPTR[i][j] = k;
					}
				}
			}
		}

		// Get the path and log probability
		double logProb = Double.NEGATIVE_INFINITY;
		int path[] = new int[N];
		for(int i=0;i<M;i++) {
			String tagTag = "END" + " " + tagSet[i];
		       	double probTagTag = probTransition.containsKey(tagTag)?probTransition.get(tagTag):0.0;	
			double score = vScore[i][N-1] + Math.log(probTagTag);
			if(score>logProb) {
				logProb = score;
				path[N-1] = i;
			}
		}
		tags[N-1] = tagSet[path[N-1]];
		for(int j=N-2;j>=0;j--) {
			path[j] = backPTR[path[j+1]][j+1];
			tags[j] = tagSet[path[j]];
		}
		
		return logProb;
	}

	// Evaluation
	public double evaluation() throws Exception {
		// File reading related
		FileReader frTruth = new FileReader("evaluation.txt");
		if(frTruth==null) return 0;
		BufferedReader brTruth = new BufferedReader(frTruth);
		FileReader frResult = new FileReader("report.txt");
		BufferedReader brResult = new BufferedReader(frResult);
		String line = null;
		int sumTags = 0;
		int sumCorrectPrediction = 0;

		while((line=brTruth.readLine())!=null) {
			String [] strInLine = line.trim().split("[ ]+");
			line = brResult.readLine();
			String [] tagsInLine = line.trim().split("[ ]+");

			for(int i=1;i<strInLine.length;i+=2) {
				if(tagsInLine[i].equals(strInLine[i]))
					sumCorrectPrediction += 1;
			}
			sumTags += strInLine.length/2;
		}

		brTruth.close();
		brResult.close();

		// Label the model as "evaluated"
		isEvaluated = true;
		
		// Return the prediction accuracy
		return 1.0*sumCorrectPrediction/sumTags;
	}
}
