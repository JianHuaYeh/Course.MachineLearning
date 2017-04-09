package ch11;

import java.util.*;
import java.io.*;

public class Apriori {
	private ArrayList<ArrayList<String>> data;
	
	public class Rule {
		private TreeSet<String> fs;
		private TreeSet<String> conseq;
		private double conf;
		public Rule(TreeSet<String> fs, TreeSet<String> conseq, double conf) {
			this.fs = fs;
			this.conseq = conseq;
			this.conf = conf;
		}
		public String toString() {
			return fs+"-->"+conseq+", conf: "+conf;
		}
	}

	public static void main(String[] args) {
		Apriori ap = new Apriori("tran.txt");
		ap.go();
	}
	
	public void go() {
		//L,suppData=apriori.apriori(dataSet)
		Object[] objs = apriori(this.data, 0.3);
		ArrayList<ArrayList<TreeSet<String>>> L = 
				(ArrayList<ArrayList<TreeSet<String>>>)objs[0];
		HashMap<TreeSet<String>, Double> supportData = 
				(HashMap<TreeSet<String>, Double>)objs[1];
		System.out.println("Frequent item set: "+L);
		ArrayList<Rule> rules = generateRules(L, supportData, 0.7);
		for (Rule r: rules) System.out.println(r);
	}
	
	public ArrayList<TreeSet<String>> calcConf(TreeSet<String> freqSet, ArrayList<TreeSet<String>> H, 
			HashMap<TreeSet<String>, Double> supportData, ArrayList<Rule> brl, double minConf) {
		ArrayList<TreeSet<String>> prunedH = new ArrayList<TreeSet<String>>();
		for (TreeSet<String> conseq: H) {
			TreeSet<String> fs = (TreeSet<String>)freqSet.clone();
			fs.removeAll(conseq);
			System.out.println("1. Support data for "+freqSet+" = "+supportData.get(freqSet));
			System.out.println("2. Support data for "+fs+" = "+supportData.get(fs));
			double conf = supportData.get(freqSet)/supportData.get(fs);
			if (conf >= minConf) {
				Rule rule = new Rule(fs, conseq, conf);
				System.out.println(rule);
				brl.add(rule);
				prunedH.add(conseq);
			}
		}
		return prunedH;
	}
	
	public void rulesFromConseq(TreeSet<String> freqSet, ArrayList<TreeSet<String>> H, 
			HashMap<TreeSet<String>, Double> supportData, ArrayList<Rule> brl, double minConf) {
		int m = H.get(0).size();
		if (freqSet.size() > m+1) {
			ArrayList<TreeSet<String>> Hmp1 = aprioriGen(H, m+1);
			Hmp1 = calcConf(freqSet, Hmp1, supportData, brl, minConf);
			if (Hmp1.size() > 1) {
				rulesFromConseq(freqSet, Hmp1, supportData, brl, minConf);
			}
		}
	}
	
	public ArrayList<Rule> generateRules(ArrayList<ArrayList<TreeSet<String>>> L,
			HashMap<TreeSet<String>, Double> supportData, double minConf) {
		ArrayList<Rule> bigRuleList = new ArrayList<Rule>();
		for (int i=1; i<L.size(); i++) {
			for (TreeSet<String> freqSet: L.get(i)) {
				ArrayList<TreeSet<String>> H1 = new ArrayList<TreeSet<String>>();
				for (String item: freqSet) {
					TreeSet<String> ts = new TreeSet<String>();
					ts.add(item);
					H1.add(ts);
				}
				if (i > 1)
					rulesFromConseq(freqSet, H1, supportData, bigRuleList, minConf);
				else
					calcConf(freqSet, H1, supportData, bigRuleList, minConf );
			}
		}
		return bigRuleList;
	}
	
	public ArrayList<TreeSet<String>> aprioriGen(ArrayList<TreeSet<String>> Lk, int k) {
		ArrayList<TreeSet<String>> retList = new ArrayList<TreeSet<String>>();
		int lenLk = Lk.size();
		for (int i=0; i<lenLk; i++) {
			for (int j=i+1; j<lenLk; j++) {
				
				ArrayList<String> L1 = new ArrayList<String>(Lk.get(i));
				int toIndex = L1.size()-1;
				int fromIndex = toIndex-(k-2);
				List<String> L11 = L1.subList(fromIndex, toIndex);
				
				ArrayList<String> L2 = new ArrayList<String>(Lk.get(j));
				toIndex = L2.size()-1;
				fromIndex = toIndex-(k-2);
				List<String> L22 = L2.subList(fromIndex, toIndex);
				
				if (L11.equals(L22)) {
					TreeSet<String> set = new TreeSet<String>();
					set.addAll(Lk.get(i));
					set.addAll(Lk.get(j));
					retList.add(set);
				}
			}
		}
		return retList;
	}
	
	public Object[] apriori(ArrayList<ArrayList<String>> dataSet, double minSupport) {
		ArrayList<TreeSet<String>> C1 = createC1(dataSet);
		// D = map(set, dataSet)
		ArrayList<ArrayList<String>> D = dataSet;
		// L1, supportData = scanD(D, C1, minSupport)
		Object[] objs = scanD(D, C1, minSupport);
		ArrayList<TreeSet<String>> L1 = (ArrayList<TreeSet<String>>)objs[0];
		HashMap<TreeSet<String>, Double> supportData = (HashMap<TreeSet<String>, Double>)objs[1];
		System.out.println("L1: "+L1);
		// L = [L1]
		ArrayList<ArrayList<TreeSet<String>>> L = 
				new ArrayList<ArrayList<TreeSet<String>>>();
		L.add(L1);
		int k=2;
		// while (len(L[k-2]) > 0)
		while (L.get(k-2).size() > 0) {
			// Ck = aprioriGen(L[k-2], k)
			ArrayList<TreeSet<String>> Ck = aprioriGen(L.get(k-2), k);
			// Lk, supK = scanD(D, Ck, minSupport)
			objs = scanD(D, Ck, minSupport);
			ArrayList<TreeSet<String>> Lk = (ArrayList<TreeSet<String>>)objs[0];
			HashMap<TreeSet<String>, Double> supK = (HashMap<TreeSet<String>, Double>)objs[1];
			supportData.putAll(supK);
			//System.out.println("L"+k+": "+Lk);
			L.add(Lk);
			k++;
		}
		return new Object[]{L, supportData};
	}
	
	public Apriori(String s) {
		this.data = loadDataSet(s);
		if (this.data == null) {
			System.out.println("Data error, stop.");
			System.exit(0);
		}
	}
	
	public ArrayList<ArrayList<String>> loadDataSet(String fname) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line="";
			while ((line=br.readLine()) != null) {
				String[] slist = line.split(",");
				ArrayList<String> rec = new ArrayList<String>();
				for (String s: slist) rec.add(s.trim());
				result.add(rec);
			}
			br.close();
			return result;
		} catch (Exception e) {
			
			e.printStackTrace(System.err);
		}
		return null;
	}
	
	public ArrayList<TreeSet<String>> createC1(ArrayList<ArrayList<String>> dataSet) {
		ArrayList<TreeSet<String>> C1 = new ArrayList<TreeSet<String>>();
		
		for (ArrayList<String> transaction: dataSet) {
			for (String item: transaction) {
				TreeSet<String> set = new TreeSet<String>();
				set.add(item);
				if (!C1.contains(set)) C1.add(set);
			}
		}
		// C1.sort()
		// return map(frozenset, C1)
		return C1;
	}
	
	public Object[] scanD(ArrayList<ArrayList<String>> D, 
			ArrayList<TreeSet<String>> Ck, double minSupport) {
		HashMap<TreeSet<String>, Integer> ssCnt = 
				new HashMap<TreeSet<String>, Integer>();
		for (ArrayList<String> tid: D) {
			for (TreeSet<String> can: Ck) {
				// if can.issubset(tid)
				if (tid.containsAll(can)) {
					if (ssCnt.get(can) == null) ssCnt.put(can, 1);
					else ssCnt.put(can, ssCnt.get(can)+1);
				}
			}
		}
		
		double numItems = (double)D.size();
		ArrayList<TreeSet<String>> retList = new ArrayList<TreeSet<String>>();
		HashMap<TreeSet<String>, Double> supportData =
				new HashMap<TreeSet<String>, Double>();
		for (TreeSet<String> key: ssCnt.keySet()) {
			double support = ssCnt.get(key)/numItems;
			if (support >= minSupport) retList.add(0, key);
			supportData.put(key, support);
		}
		
		return new Object[]{retList, supportData};
	}
}












