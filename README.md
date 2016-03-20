Hidden Markov Model (HMM) and Part of Speech (POS) Tagging
========================================================

## Introduction
This repository includes demo codes on how to use hidden Markov model (HMM) to perform part of speech (POS) tagging. It involves model training, testing and evaluation.

## Table of Contents
* How it works
* Contributor(s)
* Additional information

## How it works
1. To save space, the data is not included in this repository. After cloning this repository, you can find some public data, and make sure they follow the formats below:
	* Training data in "training.txt" (each line): word1 tag1 word2 tag2 ... wordN tagN
	* Testing data in "testing.txt" (each line): word1 word2 ... wordN
	* Evaluation data in "evaluation.txt" (each line): tag1 tag2 ... tagN (corresponds to each line in testing data)
2. Place all data in the same directory as the codes (HMM.java and POS.java)
3. Compile using command: javac POS.java HMM.java
4. Run using commnad: java POS
5. Enter lambda (0~1) at the prompted line for parameter configuration
6. See the POS tagged sentences for testing.txt in "report.txt" (at the same location as the codes)

## Contributors
* Tianlong Song

## Additional information
Please refer to [Tianlong's blog](https://stlong0521.github.io/20160319%20-%20HMM%20and%20POS.html) for theoretical details about HMM and POS tagging.
