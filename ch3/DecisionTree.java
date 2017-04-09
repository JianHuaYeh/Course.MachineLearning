package ch3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class DecisionTree {
	
	public static void main(String[] args) {
		DecisionTree tree = new DecisionTree(); 
		Object[] objs = tree.createDataSet();
		Object[][] dataset = (Object[][])objs[0];
		ArrayList<String> labels = (ArrayList<String>)objs[1];
		//HashMap<String, Object> myTree
		HashMap<String, Object> dtree = 
				(HashMap<String, Object>)tree.createTree(dataset, 
						(ArrayList<String>)labels.clone());
		System.out.println("Decision tree created.");
		tree.printTree(dtree);
		Object[] testVec1 = new Object[]{1, 0};
		Object classstr = tree.classify(dtree, labels, testVec1);
		System.out.println("Class1 = "+classstr);
		Object[] testVec2 = new Object[]{1, 1};
		tree.classify(dtree, labels, testVec2);
		System.out.println("Class2 = "+classstr);
	}
	
	public void printTree(HashMap<String, Object> inputTree) {
		String firstStr = (new ArrayList<String>(inputTree.keySet())).get(0);
		System.out.println(firstStr);
		Object secondDict = inputTree.get(firstStr);
		if (secondDict instanceof HashMap) {
			printTree((HashMap<String, Object>)secondDict);
		}
		else {
			System.out.println(secondDict);
		}
	}
	
	public Object classify(HashMap<String, Object> inputTree, 
			ArrayList<String> featLabels, Object[] testVec) {
		Object classLabel = null;
		String firstStr = (new ArrayList<String>(inputTree.keySet())).get(0);
		Object secondDict = inputTree.get(firstStr);
		//System.out.println("firstStr: "+firstStr);
		//for (String feat: featLabels) System.out.println("label: "+feat);
		int featIndex = featLabels.indexOf(firstStr);
		if (secondDict instanceof HashMap) {
			HashMap<String, Object> secondDict2 = (HashMap<String, Object>)secondDict;
			for (String key: secondDict2.keySet()) {
				if (testVec[featIndex].equals(key)) {
					if (secondDict2.get(key) instanceof HashMap) {
						classLabel = classify((HashMap<String, Object>)secondDict2.get(key), 
										featLabels, testVec);
					}
					else
						classLabel = secondDict2.get(key);
					
				}
			}
		}
		return classLabel;
	}
	
	public Object[] createDataSet() {
		Object[][] dataset = new Object[][]{
				{1, 1, "yes"},
				{1, 1, "yes"},
				{1, 0, "no"},
				{0, 1, "no"},
				{0, 1, "no"}
		};
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("no surfacing");
		labels.add("flippers");
		Object[] objs = new Object[]{dataset, labels};
		return objs;
	}
	
	public double calcShannonEnt(Object[][] dataSet) {
		int numEntries = dataSet.length;
		HashMap<String, Integer> labelCounts = new HashMap<String, Integer>();
		for (Object[] featVec: dataSet) {
			//if (featVec == null) System.out.println("Null featVec");
			//System.out.println("featVec.len = "+featVec.length);
			//for (Object f: featVec) System.out.println("feat:"+f);
			String currentLabel = (String)featVec[featVec.length-1];
			if (labelCounts.get(currentLabel) == null) labelCounts.put(currentLabel, 0);
			labelCounts.put(currentLabel, labelCounts.get(currentLabel)+1);
		}
		double shannonEnt = 0.0;
		for (String key: labelCounts.keySet()) {
			double prob = ((double)labelCounts.get(key))/numEntries;
			shannonEnt += (-prob*Math.log(prob));
		}
		return shannonEnt;
	}
	
	public Object[][] splitDataSet(Object[][] dataSet, int axis, Object value) {
		ArrayList<Object[]> retDataSet = new ArrayList<Object[]>();
		int count=0;
		for (Object[] featVec: dataSet) {
			if (featVec[axis].equals(value)) {
				//System.out.println("axis="+axis);
				Object[] reducedFeatVec = new Object[featVec.length-1];
				if (axis > 0)
					System.arraycopy(featVec, 0, reducedFeatVec, 0, axis-1);
				//System.out.println("featVec.len="+featVec.length);
				System.arraycopy(featVec, axis+1, reducedFeatVec, axis, featVec.length-axis-1);
				retDataSet.add(reducedFeatVec);
			}
		}
		Object[][] retDataSet2 = new Object[retDataSet.size()][];
		for (int i=0; i<retDataSet.size(); i++) retDataSet2[i] = retDataSet.get(i);
		//return (Object[][])retDataSet.toArray();
		return retDataSet2;
	}
	
	public int chooseBestFeatureToSplit(Object[][] dataSet) {
		int numFeatures = dataSet[0].length-1;
		double baseEntropy = calcShannonEnt(dataSet);
		double bestInfoGain = 0.0;
		int bestFeature = -1;
		for (int i=0; i<numFeatures; i++) {
			HashSet uniqueVals = new HashSet();
			for (Object[] example: dataSet) uniqueVals.add(example[i]);
			double newEntropy = 0.0;
			for (Object value: uniqueVals) {
				Object[][] subDataSet = splitDataSet(dataSet, i, value);
				//System.out.println("subSet: "+subDataSet.length+"/"+dataSet.length);
				double prob = subDataSet.length/(double)dataSet.length;
				newEntropy += prob*calcShannonEnt(subDataSet);
			}
			double infoGain = baseEntropy - newEntropy;
			if (infoGain > bestInfoGain) {
				bestInfoGain = infoGain;
				bestFeature = i;
			}
		}
		return bestFeature;
	}
	
	public Object majorityCnt(ArrayList classList) {
		HashMap<Object, Integer> classCount = new HashMap<Object, Integer>();
		for (Object vote: classList) {
			if (!classCount.keySet().contains(vote)) classCount.put(vote, 0);
			classCount.put(vote, classCount.get(vote)+1);
		}
		Object bestLabel = null;
		int bestCount = -1;
		for (Object key: classCount.keySet()) {
			int count = classCount.get(key);
			if (count > bestCount) {
				bestCount = count;
				bestLabel = key;
			}
		}
		return bestLabel;
	}
	
	public Object createTree(Object[][] dataSet, ArrayList<String> labels) {
		ArrayList classList = new ArrayList();
		for (Object[] example: dataSet) classList.add(example[example.length-1]);
		int count=0;
		for (Object classlabel: classList) {
			if (classlabel.equals(classList.get(0))) count++;
		}
		if (count == classList.size()) return classList.get(0);
		if (dataSet[0].length == 1)
			return majorityCnt(classList);
		int bestFeat = chooseBestFeatureToSplit(dataSet);
		String bestFeatLabel = labels.get(bestFeat);
		HashMap<String, Object> myTree = new HashMap<String, Object>();
		labels.remove(bestFeatLabel);
		HashSet uniqueVals = new HashSet();
		for (Object[] example: dataSet) uniqueVals.add(example[bestFeat]);
		for (Object value: uniqueVals) {
			ArrayList<String> subLabels = (ArrayList<String>)labels.clone();
			myTree.put(bestFeatLabel, 
					createTree(splitDataSet(dataSet, bestFeat, value), subLabels));
		}
		return myTree;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
