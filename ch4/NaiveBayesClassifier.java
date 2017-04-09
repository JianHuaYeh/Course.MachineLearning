package ch4;

import java.util.ArrayList;
import java.util.HashSet;

public class NaiveBayesClassifier {
	private String[][] listOPosts;
	private int[] listClasses;
	private ArrayList<String> vocabSet;
	
	public static void main(String[] args) {
		NaiveBayesClassifier nbc = new NaiveBayesClassifier();
		nbc.testingNB();
	}
	
	public NaiveBayesClassifier() {
		Object[] ret = loadDataSet();
		this.listOPosts = (String[][])ret[0];
		this.listClasses = (int[])ret[1];
	}
	
	public Object[] loadDataSet() {
		String[][] postingList = new String[][]{{"my", "dog", "has", "flea", "problems", "help", "please"},
							{"maybe", "not", "take", "him", "to", "dog", "park", "stupid"},
							{"my", "dalmation", "is", "so", "cute", "I", "love", "him"},
							{"stop", "posting", "stupid", "worthless", "garbage"},
							{"mr", "licks", "ate", "my", "steak", "how", "to", "stop", "him"},
							{"quit", "buying", "worthless", "dog", "food", "stupid"}};
		int[] classVec = new int[]{0, 1, 0, 1, 0, 1}; // 1 is abusive, 0 not
		return new Object[]{postingList, classVec};
	}
	
	public ArrayList<String> createVocabList(String[][] dataSet) {
		HashSet<String> vocabSet = new HashSet<String>();
		for (String[] document: dataSet) {
			for (String vocab: document) vocabSet.add(vocab);
		}
		return new ArrayList<String>(vocabSet);
	}

	public int[] setOfWords2Vec(ArrayList<String> vocabList, String[] inputSet) {
		int[] returnVec = new int[vocabList.size()];
		for (String word: inputSet) {
			if (vocabList.contains(word)) returnVec[vocabList.indexOf(word)] = 1;
			else System.err.println("the word: "+word+" is not in my Vocabulary!");
		}
		return returnVec;
	}
	
	public int[] bagOfWords2VecMN(ArrayList<String> vocabList, String[] inputSet) {
		int[] returnVec = new int[vocabList.size()];
		for (String word: inputSet) {
			if (vocabList.contains(word)) returnVec[vocabList.indexOf(word)] += 1;
		}
		return returnVec;
	}
	
	public Object[] trainNB0(int[][] trainMatrix, int[] trainCategory) {
		int numTrainDocs = trainMatrix.length;
		int numWords = trainMatrix[0].length;
		int sum=0;
		for (int i: trainCategory) sum+=i;
		double pAbusive = sum/(double)numTrainDocs;
		int[] p0Num = new int[numWords];
		int[] p1Num = new int[numWords];
		double p0Denom=0.0; double p1Denom=0.0;
		double[] p1Vect = new double[numWords];
		double[] p0Vect = new double[numWords];
		for (int i=0; i<numTrainDocs; i++) {
			if (trainCategory[i] == 1) {
				for (int j=0; j<numWords; j++) {
					p1Num[j] += trainMatrix[i][j];
					p1Denom += trainMatrix[i][j];
				}
			}
			else {
				for (int j=0; j<numWords; j++) {
					p0Num[j] += trainMatrix[i][j];
					p0Denom += trainMatrix[i][j];
				}
			}
		}
		// user log to avoid underflow
		for (int j=0; j<numWords; j++) p1Vect[j] = Math.log(p1Num[j]/p1Denom);
		for (int j=0; j<numWords; j++) p0Vect[j] = Math.log(p0Num[j]/p0Denom);
		return new Object[]{p0Vect, p1Vect, pAbusive};
	}
	
	public int classifyNB(int[] vec2Classify, double[] p0Vec, 
			double[] p1Vec, double pClass1) {
		double sum=0.0;
		for (int i=0; i<vec2Classify.length; i++)
			sum += vec2Classify[i]*p1Vec[i];
		double p1 = sum + Math.log(pClass1);
		sum = 0.0;
		for (int i=0; i<vec2Classify.length; i++)
			sum += vec2Classify[i]*p0Vec[i];
		double p0 = sum + Math.log(1.0-pClass1);
		
		if (p1 > p0) return 1;
		return 0;
	}
	
	public void testingNB() {
		ArrayList<String> myVocabList = createVocabList(this.listOPosts);
		int[][] trainMat = new int[listOPosts.length][];
		for (int i=0; i<listOPosts.length; i++) {
			String[] postinDoc = this.listOPosts[i];
			//trainMat[i] = setOfWords2Vec(myVocabList, postinDoc);
			trainMat[i] = bagOfWords2VecMN(myVocabList, postinDoc);
		}
		Object[] ret = trainNB0(trainMat, this.listClasses);
		double[] p0V = (double[])ret[0];
		double[] p1V = (double[])ret[1];
		double pAb = (double)ret[2];
		//System.out.println("pAb = "+pAb);
		
		String[] testEntry = new String[]{"love", "my", "dalmation"};
		//int[] thisDoc = setOfWords2Vec(myVocabList, testEntry);
		int[] thisDoc = bagOfWords2VecMN(myVocabList, testEntry);
		System.out.println("test1 is classified as: "+classifyNB(thisDoc, p0V, p1V, pAb));
	}
}













