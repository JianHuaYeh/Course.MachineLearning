package ch8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import Jama.Matrix;

public class Regression {
	private ArrayList<double[]> dataMat;
	private ArrayList<Double> labelMat;

	public static void main(String[] args) {
		String fname="ex0.txt";
		Regression reg = new Regression(fname);
		reg.go();
	}
	
	public void go() {
		double[][] xArr = new double[this.dataMat.size()][];
		for (int i=0; i<this.dataMat.size(); i++)
			xArr[i] = this.dataMat.get(i);
		double[] yArr = new double[this.labelMat.size()];
		for (int i=0; i<this.labelMat.size(); i++)
			yArr[i] = this.labelMat.get(i);
		Matrix xMat = new Matrix(xArr);
		Matrix yMat = (new Matrix(yArr, 1)).transpose();

		Matrix ws = standRegres(xMat, yMat);
		
		double[][] ws2 = ws.getArray();
		for (double[] rec: ws2) {
			for (double d: rec)	System.out.print(d+"\t");
			System.out.println();
		}
		// yHat = xMat*ws
		Matrix yHat = xMat.times(ws);
		System.out.println("\nRegression result:");
		double[][] yh2 = yHat.getArray();
		for (double[] rec: yh2) {
			for (double d: rec)	System.out.print(d+"\t");
			System.out.println();
		}
		
		System.out.println("Local weighted linear regression:");
		System.out.println("yArr[0] = "+yArr[0]);
		double d1 = lwlr2(xArr[0], xMat, yMat, 1.0);
		System.out.println("yArr[0] = "+d1+" under lwlr k=1.0");
		double d2 = lwlr2(xArr[0], xMat, yMat, 0.001);
		System.out.println("yArr[0] = "+d2+" under lwlr k=0.001");

		System.out.println("\nlwlrTest: ");
		Matrix yHat2 = lwlrTest(xMat, xMat, yMat, 0.003);
		for (double[] rec: yHat2.getArray()) {
			for (double d: rec)
				System.out.println(d+"\t");
			System.out.println();
		}
	}
	
	public Regression(String fname) {
		Object[] objs = loadDataSet(fname);
		this.dataMat = (ArrayList<double[]>)objs[0];
		this.labelMat = (ArrayList<Double>)objs[1];
		System.out.println("Data matrix dimension=["+this.dataMat.size()+"x"+
							this.dataMat.get(0).length+"]");
		System.out.println("Label matrix dimension=["+this.labelMat.size()+"x1]");
	}
		
	public Object[] loadDataSet(String fname) {
		ArrayList<double[]> dataMat = new ArrayList<double[]>();
		ArrayList<Double> labelMat = new ArrayList<Double>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line=br.readLine();
			StringTokenizer st = new StringTokenizer(line, "\t");
			int numFeat=st.countTokens()-1;
			do {
				double[] lineArr = new double[numFeat];
				int i=0;
				StringTokenizer curLine = new StringTokenizer(line, "\t");
				while (i < numFeat)
					lineArr[i++] = Double.parseDouble(curLine.nextToken());
				dataMat.add(lineArr);
				labelMat.add(Double.parseDouble(curLine.nextToken()));
			} while ((line=br.readLine()) != null);
			br.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return new Object[]{dataMat, labelMat};
	}
	
	public Matrix standRegres(Matrix xMat, Matrix yMat) {
		// xMat.T*xMat
		Matrix xTx = xMat.transpose().times(xMat);
		if (xTx.det() == 0.0) {
			System.err.println("This matrix is singular, cannot do inverse");
			return null;
		}
		// ws = xTx.I * (xMat.T*yMat)
		Matrix ws = xTx.inverse().times(xMat.transpose().times(yMat));
		return ws;
	}
	
	// Locally weighted linear regression
	public Matrix lwlr(double[] testPoint, Matrix xMat, Matrix yMat) {
		return lwlr(testPoint, xMat, yMat, 1.0);
	}

	public Matrix lwlr(double[] testPoint, Matrix xMat, Matrix yMat, double k) {
		int m = xMat.getRowDimension();
		Matrix weights = Matrix.identity(m, m);
		Matrix tp = new Matrix(testPoint, 1);
		for (int j=0; j<m; j++) {
			//diffMat = testPoint - xMat[j,:]
			Matrix xm = xMat.getMatrix(new int[]{j}, 0, xMat.getColumnDimension()-1);
			Matrix diffMat = tp.minus(xm);
			//weights[j,j] = exp(diffMat*diffMat.T/(-2.0*k**2))
			Matrix mm = diffMat.times(diffMat.transpose());
			weights.set(j, j, Math.exp(mm.get(0, 0)/(-2.0*k*k)));
		}
		Matrix xTx = xMat.transpose().times(weights.times(xMat));
		if (xTx.det() == 0.0) {
			System.err.println("This matrix is singular, cannot do inverse");
			return null;
		}
		// ws = xTx.I * (xMat.T*yMat)
		Matrix ws = xTx.inverse().times(xMat.transpose().times(weights.times(yMat)));
		return tp.times(ws);
	}
	
	public double lwlr2(double[] testPoint, Matrix xMat, Matrix yMat, double k) {
		Matrix mm = lwlr(testPoint, xMat, yMat, k);
		return mm.get(0, 0);
	}
	
	public Matrix lwlrTest(Matrix testMat, Matrix xMat, Matrix yMat) {
		return lwlrTest(testMat, xMat, yMat, 1.0);
	}

	public Matrix lwlrTest(Matrix testMat, Matrix xMat, Matrix yMat, double k) {
		//m = shape(testArr)[0]
		int m = testMat.getRowDimension();
		System.out.println("# test points: "+m);
		//yHat = zeros(m)
		double[] yh = new double[m];
		Matrix yHat = (new Matrix(yh, 1)).transpose();
		//for i in range(m):
		//	yHat[i] = lwlr(testArr[i],xArr,yArr,k)
		for (int i=0; i<m; i++) {
			Matrix mm = lwlr(testMat.getArray()[i], xMat, yMat, k);
			yHat.setMatrix(new int[]{i}, 0, mm.getColumnDimension()-1, mm);
		}
		return yHat;
	}
}
