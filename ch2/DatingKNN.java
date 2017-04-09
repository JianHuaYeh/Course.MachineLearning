package ch2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class DatingKNN extends KNN {
	private String fname;
	
	public static void main(String[] args) {
		DatingKNN dknn = new DatingKNN("datingTestSet1.txt");
		Object[] data = dknn.createDataSet();
		double[][] points = (double[][])data[0];
		String[] labels = (String[])data[1];
		double errRate = dknn.doTest("datingTestSet2.txt", points, labels, 3);
		System.out.println("Error rate = "+errRate);
	}
	
	public DatingKNN(String s) {
		this.fname = s;
	}
	
	public double doTest(String s, double[][] dataSet, String[] labels, int k) {
		// normalize
		Object[] data2 = autoNorm(dataSet);
		double[][] points2 = (double[][])data2[0];
		double[] ranges = (double[])data2[1];
		double[] minVals = (double[])data2[2];				
		try {
			BufferedReader br = new BufferedReader(new FileReader(s));
			String line="";
			int count=0;
			int err=0;
			while ((line=br.readLine()) != null) {
				//40920	8.326976	0.953952	largeDoses
				StringTokenizer st = new StringTokenizer(line, "\t");
				double miles=0.0;
				double game=0.0;
				double icecream=0.0;
				if (st.hasMoreTokens()) {
					miles = Double.parseDouble(st.nextToken());
					if (st.hasMoreTokens()) {
						game = Double.parseDouble(st.nextToken());
						if (st.hasMoreTokens()) {
							icecream = Double.parseDouble(st.nextToken());
							if (st.hasMoreTokens()) {
								String label = st.nextToken();
								//double[] data = new double[]{miles, game, icecream};
								double[] data = new double[]{miles, game};
								double[] d2 = new double[data.length];
								// normalize
								for (int i=0; i<data.length; i++) {
									d2[i] = (data[i]-minVals[i])/ranges[i];
								}
									
								String plabel = classify0(d2, points2, labels, k);
								if (!label.equals(plabel)) err++;
								count++;
							}
						}
					}
				}
			}
			br.close();	
			return ((double)err)/count;
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
		return -1;
	}
	
	public Object[] createDataSet() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.fname));
			String line="";
			ArrayList<double[]> persons = new ArrayList<double[]>();
			ArrayList<String> plabels = new ArrayList<String>();
			while ((line=br.readLine()) != null) {
				//40920	8.326976	0.953952	largeDoses
				StringTokenizer st = new StringTokenizer(line, "\t");
				double miles=0.0;
				double game=0.0;
				double icecream=0.0;
				if (st.hasMoreTokens()) {
					miles = Double.parseDouble(st.nextToken());
					if (st.hasMoreTokens()) {
						game = Double.parseDouble(st.nextToken());
						if (st.hasMoreTokens()) {
							icecream = Double.parseDouble(st.nextToken());
							if (st.hasMoreTokens()) {
								String label = st.nextToken();
								plabels.add(label);
								//double[] data = new double[]{miles, game, icecream};
								double[] data = new double[]{miles, game};
								persons.add(data);
							}
						}
					}
				}
			}
			br.close();

			double[][] group = new double[persons.size()][];
			String[] labels = new String[plabels.size()];
			int i=0;
			for (double[] person: persons) group[i++] = person;
			i=0;
			for (String label: plabels) labels[i++] = label;

			return new Object[]{group, labels};
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.exit(-1);
		}
		return null;
	}

}
