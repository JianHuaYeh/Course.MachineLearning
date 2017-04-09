package ch2;

import java.util.Arrays;
import java.util.HashMap;

public class KNN {
	
	public class AObject implements Comparable<AObject> {
		public double distance;
		public String label;
		
		public AObject(double d, String s) {
			this.distance = d;
			this.label = s;
		}
		
		public int compareTo(AObject other) {
			if (this.distance < other.distance) return -1;
			else if (this.distance > other.distance) return 1;
			else return 0;
		}
	}
	
	public static void main(String[] args) {
		double[] inX = new double[]{0.1, 0.1};
		KNN knn = new KNN();
		Object[] data = knn.createDataSet();
		double[][] points = (double[][])data[0];
		String[] labels = (String[])data[1];
		
		// normalize
		Object[] data2 = knn.autoNorm(points);
		double[][] points2 = (double[][])data2[0];
		double[] ranges = (double[])data2[1];
		double[] minVals = (double[])data2[2];
		
		double[] inX2 = new double[inX.length];
		for (int i=0; i<inX.length; i++) {
			inX2[i] = (inX[i]-minVals[i])/ranges[i];
		}
		
		String label = knn.classify0(inX2, points2, labels, 3);
		System.out.println("inX is classified as: "+label);
	}
	
	public Object[] createDataSet() {
		double[][] group = {{1.0, 1.1}, {1.0, 1.0}, {0, 0}, {0, 0.1}};
		String[] labels = {"A", "A", "B", "B"};
		return new Object[]{group, labels};
	}
	
	public String classify0(double[] inX, double[][] dataSet, String[] labels, int k) {
		int dataSetSize = dataSet.length;
		double[][] diffMat = new double[dataSetSize][dataSet[0].length];
		for (int j=0; j<dataSetSize; j++) {
			double[] pt = dataSet[j];
			for (int i=0; i<dataSet[0].length; i++) {
				diffMat[j][i] = pt[i]-inX[i];
				diffMat[j][i] = Math.pow(diffMat[j][i], 2);
			}
		}
		double[] distances = new double[dataSetSize];
		for (int j=0; j<dataSetSize; j++) {
			for (int i=0; i<dataSet[0].length; i++) {
				distances[j] += diffMat[j][i];
			}
			distances[j] = Math.pow(distances[j], 0.5);
		}
		AObject[] distobj = new AObject[dataSetSize];
		for (int i=0; i<distobj.length; i++) {
			distobj[i] = new AObject(distances[i], labels[i]);
		}
		Arrays.sort(distobj);
		HashMap<String, Integer> classCount = new HashMap<String, Integer>();
		for (int i=0; i<k; i++) {
			String voteIlabel = distobj[i].label;
			if (classCount.get(voteIlabel) == null) classCount.put(voteIlabel, 1);
			else classCount.put(voteIlabel, classCount.get(voteIlabel)+1);
		}
		String maxLabel="N/A";
		int maxCount=-1;
		for (String label: classCount.keySet()) {
			int count = classCount.get(label);
			if (count > maxCount) {
				maxLabel = label;
				maxCount = count;
			}
		}
		return maxLabel;
	}
	
	public Object[] autoNorm(double[][] dataSet) {
		double[] minVals = dataSet[0].clone();
		double[] maxVals = dataSet[0].clone();
		for (int i=0; i<dataSet[0].length; i++) {
			for (int j=0; j<dataSet.length; j++) {
				if (dataSet[j][i] < minVals[i]) minVals[i]=dataSet[j][i];
				if (dataSet[j][i] > maxVals[i]) maxVals[i]=dataSet[j][i];
			}
		}
		
		double[] ranges = new double[dataSet[0].length];
		for (int i=0; i<dataSet[0].length; i++) {
			ranges[i] = maxVals[i]-minVals[i];
		}
		int m = dataSet.length;
		double[][] normDataSet = new double[dataSet.length][dataSet[0].length];
		for (int i=0; i<dataSet[0].length; i++) {
			for (int j=0; j<dataSet.length; j++) {
				normDataSet[j][i] = (dataSet[j][i]-minVals[i])/ranges[i];
			}
		}
		
		return new Object[]{normDataSet, ranges, minVals};
	}
}
