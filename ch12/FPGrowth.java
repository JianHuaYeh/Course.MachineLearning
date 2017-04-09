package ch12;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FPGrowth {
	//HashMap<String[], Integer> dataSet;
	HashMap<ArrayList<String>, Integer> dataSet;
	
	public class AObject implements Comparable<AObject> {
		public int count;
		public String item;
		
		public AObject(String s, int d) {
			this.count = d;
			this.item = s;
		}
		
		public int compareTo(AObject other) {
			if (this.count < other.count) return 1;
			else if (this.count > other.count) return -1;
			else return 0;
		}
	}
	
	public class BObject implements Comparable<BObject> {
		public int count;
		public String item;
		
		public BObject(String s, int d) {
			this.count = d;
			this.item = s;
		}
		
		public int compareTo(BObject other) {
			if (this.count < other.count) return -1;
			else if (this.count > other.count) return 1;
			else return 0;
		}
	}
	
	public static void main(String[] args) {
		FPGrowth fp = new FPGrowth();
		fp.go();
	}
	
	public void go() {
		Object[] objs = createTree(this.dataSet, 3);
		TreeNode myFPtree = (TreeNode)objs[0];
		HashMap<String, Object> myHeaderTab = (HashMap<String, Object>)objs[1];
		myFPtree.disp();
		
		// test "t" for conditional patterns
		/*TreeNode rnode = (TreeNode)((Object[])myHeaderTab.get("y"))[1];
		HashMap<ArrayList<String>, Integer> condPats = findPrefixPath(rnode);
		for (ArrayList<String> key: condPats.keySet()) {
			int freq = condPats.get(key);
			System.out.println(key+":"+freq);
		}*/
		ArrayList<String> prefix = new ArrayList<String>();
		ArrayList<ArrayList<String>> freqItems = new ArrayList<ArrayList<String>>();
		mineTree(myFPtree, myHeaderTab, 3, prefix, freqItems);
		for (ArrayList<String> set: freqItems)
			System.out.println(set);
	}
	
	public void mineTree(TreeNode inTree, HashMap<String, Object> headerTable, 
			int minSup, ArrayList<String> preFix,
			ArrayList<ArrayList<String>> freqItemList) {
		BObject[] aobjs = new BObject[headerTable.keySet().size()];
		int i=0;
		for (String key: headerTable.keySet()) {
			Object[] value = (Object[])headerTable.get(key);
			aobjs[i++] = new BObject(key, (Integer)value[0]);
		}
		Arrays.sort(aobjs);
		ArrayList<String> bigL = new ArrayList<String>();
		for (BObject obj: aobjs) bigL.add(obj.item);
		
		for (String basePat: bigL) {
			ArrayList<String> newFreqSet = (ArrayList<String>)preFix.clone();
			newFreqSet.add(basePat);
			freqItemList.add(newFreqSet);
			
			//condPattBases = findPrefixPath(basePat, headerTable[basePat][1])
			TreeNode theNode = (TreeNode)((Object[])headerTable.get(basePat))[1];
			HashMap<ArrayList<String>, Integer> condPattBases = findPrefixPath(theNode);
			
			//myCondTree, myHead = createTree(condPattBases, minSup)
			Object[] objs = createTree(condPattBases, 3);
			TreeNode myCondTree = (TreeNode)objs[0];
			HashMap<String, Object> myHead = (HashMap<String, Object>)objs[1];
			
			if (myHead != null) {
				//System.out.println("conditional tree for: "+newFreqSet);
				//myCondTree.disp(2);
				mineTree(myCondTree, myHead, minSup, newFreqSet, freqItemList);
			}
		}
		
	}
	
	public void ascendTree(TreeNode leafNode, ArrayList<String> prefixPath) {
		// check root node
		if (leafNode.getParent() != null) {
			prefixPath.add(leafNode.getName());
			ascendTree(leafNode.getParent(), prefixPath);
		}
	}
	
	public HashMap<ArrayList<String>, Integer> findPrefixPath(TreeNode treeNode) {
		HashMap<ArrayList<String>, Integer> condPats = new HashMap<ArrayList<String>, Integer>();
		while (treeNode != null) {
			ArrayList<String> prefixPath = new ArrayList<String>();
			ascendTree(treeNode, prefixPath);
			if (prefixPath.size() > 1) {
				prefixPath.remove(0);
				condPats.put(prefixPath, treeNode.getCount()); 
			}
			treeNode = treeNode.getNodeLink();
		}
		return condPats;
	}
	
	//public Object[] createTree(HashMap<String[], Integer> dataSet, int minSup) {
	public Object[] createTree(HashMap<ArrayList<String>, Integer> dataSet, int minSup) {
		HashMap<String, Object> headerTable = new HashMap<String, Object>();
		for (ArrayList<String> trans: dataSet.keySet()) {
			for (String item: trans) {
				if (headerTable.get(item)==null) headerTable.put(item, 0);
				headerTable.put(item, (Integer)headerTable.get(item)+dataSet.get(trans));
			}
		}
		
		HashSet<String> set = new HashSet<String>(headerTable.keySet());
		for (String k: set)
			if ((Integer)headerTable.get(k) < minSup) headerTable.remove(k);
		
		Set<String> freqItemSet = headerTable.keySet();
		if (freqItemSet.size() == 0) return new Object[]{null, null};
		
		HashSet<String> set2 = new HashSet<String>(headerTable.keySet());
		for (String k: set2)
			headerTable.put(k, new Object[]{headerTable.get(k), null});
		
		TreeNode retTree = new TreeNode("Null Set", 1, null);
		
		//for (Map.Entry<String[], Integer> entry : dataSet.entrySet()) {
		//	String[] tranSet = entry.getKey();
		//	int count = entry.getValue(); 
		//}
		//for (String[] tranSet: dataSet.keySet()) {
		for (ArrayList<String> tranSet: dataSet.keySet()) {
			int count = dataSet.get(tranSet);
			HashMap<String, Integer> localD = new HashMap<String, Integer>();
			for (String item: tranSet) 
				if (freqItemSet.contains(item))
					localD.put(item, (Integer)((Object[])headerTable.get(item))[0]);
			if (localD.keySet().size() > 0) {
				AObject[] orderedItems = new AObject[localD.keySet().size()];
				int i=0;
				for (String item: localD.keySet())
					orderedItems[i++] = new AObject(item, localD.get(item));
				Arrays.sort(orderedItems);
				
				updateTree(orderedItems, retTree, headerTable, count);
			}
		}
		return new Object[]{retTree, headerTable};
	}
	
	public void updateTree(AObject[] items, TreeNode inTree,
			HashMap<String, Object> headerTable, int count) {
		String item = items[0].item;
		HashMap<String, TreeNode> children = inTree.getChildren();
		if (children.get(item) != null) {
			children.get(item).inc(count);
		}
		else {
			children.put(item, new TreeNode(item, count, inTree));
			Object[] val = (Object[])headerTable.get(item);
			if (val[1] == null) { val[1] = children.get(item); }
			else { updateHeader((TreeNode)val[1], children.get(item)); }
		}
		if (items.length > 1) {
			AObject[] newitems = new AObject[items.length-1];
			for (int i=1; i<items.length; i++)
				newitems[i-1] = items[i];
			updateTree(newitems, children.get(item), headerTable, count);
		}
	}
		
	public void updateHeader(TreeNode nodeToTest, TreeNode targetNode) {
		TreeNode node = nodeToTest;
		while (node.getNodeLink() != null)
			node = node.getNodeLink();
		node.setNodeLink(targetNode);
	}
	
	public FPGrowth() {
		String[][] simpDat = this.loadSimpDat();
		this.dataSet = createInitSet(simpDat);
	}
	
	public String[][] loadSimpDat() {
		String[][] simpDat = {{"r", "z", "h", "j", "p"},
				{"z", "y", "x", "w", "v", "u", "t", "s"},
				{"z"},
				{"r", "x", "n", "o", "s"},
				{"y", "r", "x", "z", "q", "t", "p"},
				{"y", "z", "x", "e", "q", "s", "t", "m"}};
		return simpDat;
	}

	public HashMap<ArrayList<String>, Integer> createInitSet(String[][] dataSet) {
		HashMap<ArrayList<String>, Integer> retDict = new HashMap<ArrayList<String>, Integer>();
		for (String[] trans: dataSet) {
			ArrayList<String> key = new ArrayList<String>();
			for (String s: trans) key.add(s);
			if (retDict.get(key) == null) retDict.put(key, 0);
			retDict.put(key, retDict.get(key)+1);
		}
		return retDict;
	}

	/*public HashMap<String[], Integer> createInitSet(String[][] dataSet) {
		HashMap<String[], Integer> retDict = new HashMap<String[], Integer>();
		for (String[] trans: dataSet) {
			if (retDict.get(trans) == null) retDict.put(trans, 0);
			retDict.put(trans, retDict.get(trans)+1);
		}
		return retDict;
	}*/

}
