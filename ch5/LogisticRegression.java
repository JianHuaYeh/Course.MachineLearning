package ch5;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class LogisticRegression {
	private double[][] dataArr;
	private int[] labelMat;
	
	public static void main(String[] args) {
		LogisticRegression lr = new LogisticRegression("testSet.txt");
		lr.go();
	}
	
	public void go() {
		//double[][] weights = gradAscent(this.dataArr, this.labelMat);
		double[] weights = stocGradAscent0(this.dataArr, this.labelMat);
		
		//double[] rec = new double[]{1.0, -0.017612, 14.053064};
		double[] rec = new double[]{1.0, -1.337472, 0.468339};
		
		int label = classify(weights, rec);
		System.out.println(label);
	}
	
	public int classify(double[] weights, double[] rec) {
		double sum=0.0;
		for (int i=0; i<weights.length; i++)
			sum += weights[i]*rec[i];
		System.out.println(sigmoid(sum));
		return (int)Math.round(sigmoid(sum));
	}
	
	public int classify0(double[][] weights, double[] rec) {
		double sum=0.0;
		for (int i=0; i<weights.length; i++)
			sum += weights[i][0]*rec[i];
		return (int)Math.round(sigmoid(sum));
	}
	
	public LogisticRegression(String s) {
		Object[] objs = loadDataSet(s);
		if (objs == null) {
			System.err.println("Load data error, stop.");
			System.exit(0);
		}
		int size = ((ArrayList<double[]>)objs[0]).size();
		this.dataArr = new double[size][];
		for (int i=0; i<size; i++) this.dataArr[i] = ((ArrayList<double[]>)objs[0]).get(i);
		size = ((ArrayList<Integer>)objs[1]).size();
		this.labelMat = new int[size];
		for (int i=0; i<size; i++) this.labelMat[i] = ((ArrayList<Integer>)objs[1]).get(i);
	}
	
	public Object[] loadDataSet(String s) {
		ArrayList<double[]> dataMat = new ArrayList<double[]>();
		ArrayList<Integer> labelMat = new ArrayList<Integer>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(s));
			String line="";
			while ((line=br.readLine()) != null) {
				String[] lineArr = line.trim().split("\t");
				double[] rec = new double[]{1.0, 
						Double.parseDouble(lineArr[0]), Double.parseDouble(lineArr[1])};
				dataMat.add(rec);
				labelMat.add(Integer.parseInt(lineArr[2]));
			}
			br.close();
			
			return new Object[]{dataMat, labelMat};
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public double sigmoid(double inX) {
		return 1.0/(1+Math.exp(-inX));
	}
	
	public double[][] sigmoid(double[][] inX) {
		int m=inX.length;
		int n=inX[0].length;
		double[][] result = new double[m][n];
		for (int i=0; i<m; i++)
			for (int j=0; j<n; j++)
				result[i][j] = sigmoid(inX[i][j]);
		return result;
	}
	
	public double innerProduct(double[] vec1, double[] vec2) {
		double sum=0.0;
		for (int i=0; i<vec1.length; i++) sum += vec1[i]*vec2[i];
		return sum;
	}
	
	public double[][] matrixTranspose(double[][] mat) {
		int m=mat.length;
		int n=mat[0].length;
		double[][] result = new double[n][m];
		for (int i=0; i<m; i++)
			for (int j=0; j<n; j++)
				result[j][i] = mat[i][j];
		return result;
	}
	
	public double[][] matrixMul(double[][] mat1, double[][] mat2) {
		int m=mat1.length;
		int p=mat2[0].length;
		double[][] mat2t = matrixTranspose(mat2);
		double[][] result = new double[m][p];
		
		for (int j=0; j<p; j++)
			for (int i=0; i<m; i++)
				result[i][j] = innerProduct(mat1[i], mat2t[j]);
		return result;
	}
	
	public double[][] matrixScalarMul(double[][] mat, double a) {
		return matrixScalarMul(a, mat);
	}
	
	public double[][] matrixScalarMul(double a, double[][] mat) {
		int m=mat.length;
		int n=mat[0].length;
		double[][] result = new double[m][n];
		
		for (int i=0; i<m; i++)
			for (int j=0; j<n; j++)
				result[i][j] = a*mat[i][j];
		return result;
	}
	
	public double[][] matrixAdd(double[][] mat1, double[][] mat2) {
		int m=mat1.length;
		int n=mat1[0].length;
		double[][] result = new double[m][n];
		
		for (int i=0; i<m; i++)
			for (int j=0; j<n; j++)
				result[i][j] = mat1[i][j]+mat2[i][j];
		return result;
	}

	public double[][] matrixSub(double[][] mat1, double[][] mat2) {
		int m=mat1.length;
		int n=mat1[0].length;
		double[][] result = new double[m][n];
		
		for (int i=0; i<m; i++)
			for (int j=0; j<n; j++)
				result[i][j] = mat1[i][j]-mat2[i][j];
		return result;
	}

	public double[][] gradAscent(double[][] dataMatIn, int[] classLabels) {
		
		double[][] labelMat = new double[classLabels.length][1];
		for (int i=0; i<labelMat.length; i++) labelMat[i][0] = classLabels[i];
		
		double alpha = 0.001;
		int maxCycles = 500;
		double[][] weights = new double[dataMatIn[0].length][1];
		for (int i=0; i<weights.length; i++) weights[i][0] = 1.0;
		
		double[][] dataMatInT = matrixTranspose(dataMatIn);
		for (int k=0; k<maxCycles; k++) {
			double[][] h = sigmoid(matrixMul(dataMatIn, weights));
			double[][] error = matrixSub(labelMat, h);
			weights = matrixAdd(weights, matrixScalarMul(alpha, matrixMul(dataMatInT, error)));
		}
		return weights;
	}
	
	public double[] stocGradAscent0(double[][] dataMatIn, int[] classLabels) {
		int m=dataMatIn.length;
		int n=dataMatIn[0].length;
		double alpha = 0.01;
		double[] weights = new double[n];
		for (int i=0; i<m; i++) {
			double h = sigmoid(innerProduct(dataMatIn[i], weights));
			double[] error = new double[dataMatIn[i].length];
			for (int j=0; j<dataMatIn[i].length; j++)
				error[j] = dataMatIn[i][j] - h;
			double a = alpha*innerProduct(error, dataMatIn[i]);
			for (int j=0; j<weights.length; j++)
				weights[j] += a;
		}
		return weights;
	}

}
















