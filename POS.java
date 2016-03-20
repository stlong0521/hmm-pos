//************************************************************************************************
// Author: Tianlong Song
// Name: POS.java
// Description: POS tagging 
// Date created: 02/07/2015 
//************************************************************************************************

import java.io.*;
import java.util.*;

class POS {
	public static void main(String args[]) throws Exception {
		// Program start running
		System.out.println("***************************************************************************");
		System.out.println("** Program started");
		
		// Specify lambda and construct a POS tagger
		System.out.print("** Specify the lambda between 0 and 1: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		double lambda = Double.parseDouble(br.readLine());
		if(lambda<0||lambda>1) {
			System.out.println("** Illegal parameter! Program terminated.");
			System.out.println("***************************************************************************");
			return;
		}
		HMM posTagger = new HMM(lambda);

		// Training
		System.out.println("** Traning...");
		posTagger.training();
		if(!posTagger.getIsTrained()) {
			System.out.println("** Training error! Make sure data and program are in the same location.");
			System.out.println("***************************************************************************");
			return;
		}
		System.out.println("** Training done");

		// Testing
		System.out.println("** Testing... (might take about one or two minutes)");
		posTagger.testing();
		if(!posTagger.getIsTested()) {
			System.out.println("** Testing error! Make sure data and program are in the same location.");
			System.out.println("***************************************************************************");
			return;
		}
		System.out.println("** Testing done");

		// Evaluation
		System.out.println("** Evaluating...");
		double accuracy = posTagger.evaluation();
		if(!posTagger.getIsEvaluated()) {
			System.out.println("** Evaluation error! Make sure data and program are in the same location.");
			System.out.println("***************************************************************************");
			return;
		}
		System.out.println("** Evaluation done");
		System.out.printf("** Prediction accuracy: %.2f%%%n",100*posTagger.evaluation());
		System.out.println("***************************************************************************");
	}
}
