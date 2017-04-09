package ch10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class KMeansClustering {
	private ArrayList<double[]> data;
	
	public static void main(String[] arges) {
		String fname = "testSet.txt";
		KMeansClustering kmc = new KMeansClustering(fname);
		kmc.go(2);
	}
	
	public KMeansClustering(String s) {
		this.data = loadDataSet(s);
	}
	
	public void go(int k) {
		Object[] objs = this.kMeans(this.data, k);
		double[][] clusterAssment = (double[][])objs[1];
		System.out.print("[ ");
		for (int i=0; i<k; i++) {
			int count=0;
			for (double[] ca: clusterAssment) if (ca[0] == i) count++;
			System.out.print(count+" ");
		}
		System.out.println("]");
	}

	public ArrayList<double[]> loadDataSet(String s) {
		ArrayList<double[]> dataMat = new ArrayList<double[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(s));
			String line="";
			while ((line=br.readLine()) != null) {
				String[] lineArr = line.trim().split("\t");
				double[] rec = new double[]{Double.parseDouble(lineArr[0]), 
						Double.parseDouble(lineArr[1])};
				dataMat.add(rec);
			}
			br.close();
			
			return dataMat;
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public double distEclud(double[] vecA, double[] vecB) {
		double sum = 0.0;
		for (int i=0; i<vecA.length; i++)
			sum += (vecA[i]-vecB[i])*(vecA[i]-vecB[i]);
		return Math.sqrt(sum);
	}
	
	public double[][] randCent(ArrayList<double[]> dataSet, int k) {
		int n = dataSet.get(0).length;
		double[][] centroids = new double[k][n];
		for (int j=0; j<n; j++) {
			double minJ = Double.MAX_VALUE;
			double maxJ = Double.MIN_VALUE;
			for (double[] rec: dataSet) {
				if (rec[j] < minJ) minJ=rec[j];
				if (rec[j] > maxJ) maxJ=rec[j];
			}
			double rangeJ = maxJ-minJ;
			for (int i=0; i<k; i++) 
				centroids[i][j] = minJ + rangeJ*Math.random();
		}
		return centroids;
	}
	
	public Object[] kMeans(ArrayList<double[]> dataSet, int k) {
		int m = dataSet.size();
		double[][] clusterAssment = new double[m][2];
		double[][] centroids = randCent(dataSet, k);
		boolean clusterChanged = true;
		while (clusterChanged) {
			clusterChanged = false;
			for (int i=0; i<m; i++) {
				double minDist = Double.MAX_VALUE;
				int minIndex = -1;
				for (int j=0; j<k; j++) {
					double distJI = distEclud(dataSet.get(i), centroids[j]);
					if (distJI < minDist) {
						minDist = distJI;
						minIndex = j;
					}
				}
				if (clusterAssment[i][0] != minIndex) clusterChanged=true;
				clusterAssment[i][0] = minIndex;
				clusterAssment[i][1] = minDist*minDist;
			} // for
			
			for (int cent=0; cent<k; cent++) {
				ArrayList<double[]> ptsInClust = new ArrayList<double[]>();
				for (int i=0; i<clusterAssment.length; i++) {
					if (clusterAssment[i][0] == cent)
						ptsInClust.add(dataSet.get(i));
				}
				double sum=0.0;
				for (int j=0; j<dataSet.get(0).length; j++) {
					for (double[] rec: ptsInClust) sum += rec[j];
					centroids[cent][j] = sum/ptsInClust.size();
				}
			} // for
		} // while
		
		return new Object[]{centroids, clusterAssment};
	}
}










