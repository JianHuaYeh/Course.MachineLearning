package ch7;

import java.util.ArrayList;
import java.util.HashMap;

public class AdaBoost {
	private double[][] data;
	
	public static void main(String[] args) {
		AdaBoost ab = new AdaBoost();
		ab.go();
	}
	
	public void go() {
		Object[] objs = loadSimpData();
		double[][] datMat = (double[][])objs[0];
		double[] classLabels = (double[])objs[1];
		ArrayList<HashMap> classifierArray = adaBoostTrainDS(datMat, classLabels);
		
		double[][] testData = new double[][]{{5.0, 5.0}, {0.0, 0.0}};
		double[] result = adaClassify(testData, classifierArray);
		//print result
		System.out.print("{ ");
		for (int i=0; i<result.length; i++) System.out.print(result[i]+" ");
		System.out.print("}");
	}
	
	public Object[] loadSimpData() {
		double[][] datMat = new double[][]{{1.0, 2.1}, {2.0, 1.1}, {1.3, 1.0},
											{1.0, 1.0}, {2.0, 1.0}};
		double[] classLabels = new double[]{1.0, 1.0, -1.0, -1.0, 1.0};
		return new Object[]{datMat, classLabels};
	}
	
	public double[] adaClassify(double[][] dataMatrix, ArrayList<HashMap> classifierArr) {
		int m = dataMatrix.length;
		double[] aggClassEst = new double[m];
		for (int i=0; i<classifierArr.size(); i++) {
			//classEst = stumpClassify(dataMatrix,classifierArr[i]['dim'],\
			//							classifierArr[i]['thresh'],\
			//							classifierArr[i]['ineq'])
			int dim = (Integer)classifierArr.get(i).get("dim");
			double thresh = (Double)classifierArr.get(i).get("thresh");
			String ineq = (String)classifierArr.get(i).get("ineq");
			double[] classEst = stumpClassify(dataMatrix, dim, thresh, ineq);
			//aggClassEst += classifierArr[i]['alpha']*classEst
			double alpha = (Double)classifierArr.get(i).get("alpha");
			for (int j=0; j<aggClassEst.length; j++)
				aggClassEst[j] += alpha*classEst[j];
			//print aggClassEst
			print("Test aggClassEst", aggClassEst);
		}
		return sign(aggClassEst);
	}
	
	public ArrayList<HashMap> adaBoostTrainDS(double[][] dataArr, double[] classLabels) {
		return adaBoostTrainDS(dataArr, classLabels, 40);
	}
	
	public ArrayList<HashMap> adaBoostTrainDS(double[][] dataArr, double[] classLabels,
			int numIt) {
		ArrayList<HashMap> weakClassArr = new ArrayList<HashMap>();
		int m = dataArr.length;
		double[] D = new double[m];
		for (int i=0; i<D.length; i++) D[i]=1.0/m;
		double[] aggClassEst = new double[m];
		
		for (int i=0; i<numIt; i++) {
			//bestStump,error,classEst = buildStump(dataArr,classLabels,D)
			Object[] objs = buildStump(dataArr, classLabels, D);
			HashMap bestStump = (HashMap)objs[0];
			double error = (Double)objs[1];
			double[] classEst = (double[])objs[2];
			
			//print "D:",D.T
			print("D", D);
			
			//alpha = float(0.5*log((1.0-error)/max(error,1e-16)))
			double alpha = 0.5*Math.log((1.0-error)/Math.max(error, Math.pow(10.0, -16)));
			//bestStump['alpha'] = alpha
			bestStump.put("alpha", alpha);
			//weakClassArr.append(bestStump)
			weakClassArr.add(bestStump);
			
			//print "classEst: ",classEst.T
			print("classEst", classEst);
			
			//expon = multiply(-1*alpha*mat(classLabels).T,classEst)
			double[] classLabels2 = classLabels.clone();
			for (int j=0; j<classLabels2.length; j++) classLabels2[j] *= (-1*alpha);
			double[] expon = new double[classLabels2.length];
			for (int j=0; j<classLabels2.length; j++) expon[j]=classLabels2[j]*classEst[j];
			
			//D = multiply(D,exp(expon))
			//D = D/D.sum()
			double sum=0.0;
			for (int j=0; j<D.length; j++) {
				D[j] *= Math.pow(Math.E, expon[j]);
				sum += D[j];
			}
			for (int j=0; j<D.length; j++) D[j]/=sum;
			//aggClassEst += alpha*classEst
			for (int j=0; j<aggClassEst.length; j++) aggClassEst[j]+=alpha*classEst[j];

			//print "aggClassEst: ",aggClassEst.T
			print("aggClassEst", aggClassEst);
			
			//aggErrors = multiply(sign(aggClassEst) !=	mat(classLabels).T,ones((m,1)))
			double[] aggErrors = new double[aggClassEst.length];
			double errorRate=0.0;
			for (int j=0; j<aggClassEst.length; j++) {
				if (sign(aggClassEst[j]) != classLabels[j]) aggErrors[j]=1.0;
				errorRate += aggErrors[j];
			}
			errorRate /= m;
			//print "total error: ",errorRate,"\n"
			System.out.println("total error: "+errorRate);
			if (errorRate == 0.0) break;
		}
		return weakClassArr;
	}
	
	public double[] stumpClassify(double[][] dataMatrix, int dimen, double threshVal, 
			String threshIneq) {
		double[] retArray = new double[dataMatrix.length];
		for (int i=0; i<retArray.length; i++) retArray[i]=1.0;
		
		if (threshIneq.equals("lt")) {
			for (int i=0; i<dataMatrix.length; i++)
				if (dataMatrix[i][dimen] <= threshVal) retArray[i]=-1.0;
		}
		else {
			for (int i=0; i<dataMatrix.length; i++)
				if (dataMatrix[i][dimen] > threshVal) retArray[i]=-1.0;
		}
		return retArray;
	}
	
	public Object[] buildStump(double[][] dataMatrix, double[] labelMat, double[] D) {
		int m=dataMatrix.length;
		int n=dataMatrix[0].length;
		double numSteps=10.0;
		HashMap bestStump=new HashMap();
		double[] bestClasEst=new double[m];
		double minError=Double.MAX_VALUE;
		
		for (int i=0; i<n; i++) {
			double rangeMin=Double.MAX_VALUE;
			double rangeMax=Double.MIN_VALUE;
			for (int j=0; j<m; j++) {
				if (dataMatrix[j][i] < rangeMin) rangeMin=dataMatrix[j][i];
				if (dataMatrix[j][i] > rangeMax) rangeMax=dataMatrix[j][i];
			}
			double stepSize=(rangeMax-rangeMin)/numSteps;
			
			for (int j=-1; j<((int)numSteps)+1; j++) {
				String[] ine = {"lt", "gt"};
				for (String inequal: ine) {
					double threshVal = rangeMin + j*stepSize;
					double[] predictedVals = stumpClassify(dataMatrix,i,threshVal,inequal);
					
					//errArr = mat(ones((m,1)))
					//errArr[predictedVals == labelMat] = 0
					double[] errArr = new double[m]; // default to 0s
					for (int k=0; k<predictedVals.length; k++)
						if (predictedVals[k]!=labelMat[k]) errArr[k]=1.0;
					//weightedError = D.T*errArr
					double weightedError = innerProduct(D, errArr);
					
					if (weightedError < minError) {
						minError = weightedError;
						bestClasEst = predictedVals.clone();
						bestStump.put("dim", new Integer(i));
						bestStump.put("thresh", new Double(threshVal));
						bestStump.put("ineq", inequal);
					}
				} //for (String inequal: ine)
			} //for (int j=-1; j<((int)numSteps)+1; j++)
		} //for (int i=0; i<n; i++)
		return new Object[]{bestStump, minError, bestClasEst};
	}

	public double innerProduct(double[] vec1, double[] vec2) {
		double sum=0.0;
		for (int i=0; i<vec1.length; i++) sum += vec1[i]*vec2[i];
		return sum;
	}
	
	public double sign(double val) { return (val>=0) ? +1.0:-1.0; }
	
	public double[] sign(double[] val) {
		double[] result = new double[val.length];
		for (int i=0; i<val.length; i++)
			result[i] = (val[i]>=0) ? +1.0:-1.0;
		return result;
	}
	
	public void print(String label, double[] val) {
		System.out.print(label+": { ");
		for (int i=0; i<val.length; i++)
			System.out.print(val[i]+" ");
		System.out.println("}");
	}
} 








