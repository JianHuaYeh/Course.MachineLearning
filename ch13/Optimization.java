package ch13;

import java.util.*;
import java.io.*;

public class Optimization {
	private HashMap<String,ArrayList<Object[]>> flights;
	private String[][] people = {	{"Seymour", "BOS"},
            						{"Franny", "DAL"},
            						{"Zooey", "CAK"},
            						{"Walt", "MIA"},
            						{"Buddy", "ORD"},
            						{"Les", "OMA"}};
	private String destination = "LGA";
	
	public class AObject implements Comparable<AObject> {
		public int[] sol;
		public double cost;
		public AObject(int[] s, double c) {
			sol = s;
			cost = c;
		}
		public int compareTo(AObject other) {
			if (cost < other.cost) return -1;
			else if (cost > other.cost) return 1;
			else return 0;
		}
	}
	
	
	public static void main(String[] args) {
		Optimization opt = new Optimization("schedule.txt");
		opt.go();
	}
	
	public void go() {
		//int[] sol = {1,4,3,2,7,3,6,3,2,4,5,3};
		//printschedule(sol);
		//System.out.println("Cost = $"+schedulecost(sol));
		
		/*int[] sol = randomguess(10000);
		printschedule(sol);
		System.out.println("Random guess cost = $"+schedulecost(sol));
		System.out.println();*/
		
		/*int[] sol = hillclimb();
		printschedule(sol);
		System.out.println("Hill climbing cost = $"+schedulecost(sol));
		System.out.println();*/
		
		/*int[] sol = randomrestartHillclimb();
		printschedule(sol);
		System.out.println("Random restart hill climbing cost = $"+schedulecost(sol));
		System.out.println();*/
		
		/*int[] sol = annealingoptimize();
		printschedule(sol);
		System.out.println("Simulated annealing cost = $"+schedulecost(sol));
		System.out.println();*/
		
		/*int[] sol = randomrestartAnnealing();
		printschedule(sol);
		System.out.println("Random restart simulated annealing cost = $"+schedulecost(sol));
		System.out.println();*/
		
		int[] sol = geneticoptimize();
		printschedule(sol);
		System.out.println("Genetic optimization cost = $"+schedulecost(sol));
		System.out.println();
	}
	
	public int[] mutate(int[] vec) {
		int[] r = vec.clone();
		int i = (int)(Math.random()*(this.people.length*2));
		if ((Math.random()<0.5) && (r[i]>0)) r[i]--;
		else if (r[i]<9) r[i]++;
		return r;
	}
	
	public int[] crossover(int[] r1, int[] r2) {
		int[] r = r1.clone();
		int i = (int)(Math.random()*(this.people.length*2)-1);
		for (int j=i+1; j<r2.length; j++) r[j]=r2[j];
		return r;
	}
	
	public int[] geneticoptimize() {
		int popsize=500;
		double mutprob=0.2;
		double elite=0.1;
		int maxiter=100;
		
		// Build the initial population
		ArrayList<int[]> pop = new ArrayList<int[]>();
		while (pop.size() < popsize) {
			int[] vec = new int[this.people.length*2];
			for (int j=0; j<vec.length; j++) vec[j] = (int)(Math.random()*10);
			pop.add(vec);
		}
		
		// How many winners from each generation?
		int topelite = (int)(elite*popsize);
		
		// main loop
		for (int i=0; i<maxiter; i++) {
			AObject[] scores = new AObject[pop.size()];
			for (int j=0; j<pop.size(); j++) {
				int[] sol = pop.get(j);
				//System.out.println("sol length("+j+"): "+sol.length);
				double cost = schedulecost(sol);
				scores[j] = new AObject(sol, cost);
			}
			Arrays.sort(scores);
			
			pop = new ArrayList<int[]>();
			for (int j=0; j<topelite; j++) pop.add(scores[j].sol);
			
			// Add mutated and bred forms of the winners
			while (pop.size() < popsize) {
				if (Math.random() < mutprob) {
					// mutation
					int c = (int)(Math.random()*topelite);
					pop.add(mutate(pop.get(c)));
				}
				else {
					// crossover
					int c1 = (int)(Math.random()*topelite);
					int c2 = (int)(Math.random()*topelite);
					if (c1 != c2)
						pop.add(crossover(pop.get(c1), pop.get(c2)));
				}
			}
		}
		
		return pop.get(0);
	}
	
	public int[] randomrestartAnnealing() {
		double best=Double.MAX_VALUE;
		int[] bestr=null;
		for (int i=0; i<1000; i++) {
			int[] sol = annealingoptimize();
			double cost = schedulecost(sol);
			if (cost < best) {
				best = cost;
				bestr = sol;
			}
		}
		return bestr;
	}
	
	public int[] annealingoptimize() {
		double T=100000.0,cool=0.9999;
		int step=1;
		
		// create a random solution
		int[] vec = new int[this.people.length*2];
		for (int j=0; j<vec.length; j++) vec[j] = (int)(Math.random()*10);
		
		while (T > 0.1) {
			// Choose one of the indices
			int i = (int)(Math.random()*(this.people.length*2));
			
			// Choose a direction to change it
			if (Math.random() < 0.5) step = -step;
			
			// Create a new list with one of the values changed
			int[] vecb = vec.clone();
			vecb[i] += step;
			if (vecb[i]<0) vecb[i]=0;
			else if (vecb[i]>9) vecb[i]=9;
			
			// Calculate the current cost and the new cost
			double ea = schedulecost(vec);
			double eb = schedulecost(vecb);
			double p = Math.pow(Math.E,-(eb-ea)/T);
			
			// is it better, or does it make the probability cutoff?
			if ((eb<ea) || (Math.random()<p)) vec=vecb;
			
			// Decrease the temperature
			T = T*cool;
		}
		return vec;
	}
	
	public int[] randomrestartHillclimb() {
		double best=Double.MAX_VALUE;
		int[] bestr=null;
		for (int i=0; i<1000; i++) {
			int[] sol = hillclimb();
			double cost = schedulecost(sol);
			if (cost < best) {
				best = cost;
				bestr = sol;
			}
		}
		return bestr;
	}
	
	public int[] hillclimb() {
		// create a random solution
		int[] sol = new int[this.people.length*2];
		for (int j=0; j<sol.length; j++) sol[j] = (int)(Math.random()*10);
		
		while (true) {
			ArrayList<int[]> neighbors = new ArrayList<int[]>();
			
			for (int i=0; i<sol.length; i++) {
				int[] s1 = sol.clone();
				if (s1[i]==0) s1[i]=9;
				else s1[i]--;
				neighbors.add(s1);
				
				int[] s2 = sol.clone();
				if (s2[i]==9) s2[i]=0;
				else s2[i]++;
				neighbors.add(s2);
			}
			
			double current = schedulecost(sol);
			double best = current;
			
			for (int[] neighbor: neighbors) {
				double cost = schedulecost(neighbor);
				if (cost < best) {
					best = cost;
					sol = neighbor;
				}
			}
			
			if (best == current) break;
		}
		
		return sol;
	}
	
	public int[] randomguess(int maxiter) {
		double best = Double.MAX_VALUE;
		int[] bestr = null;
		
		for (int i=0; i<maxiter; i++) {
			// create a random solution
			int[] r = new int[this.people.length*2];
			for (int j=0; j<r.length; j++) r[j] = (int)(Math.random()*10);
			
			double cost = schedulecost(r);
			if (cost < best) {
				best = cost;
				bestr = r;
			}
		}
		
		return bestr;
	}
	
	public Optimization(String s) {
		this.flights = loadData(s);
		/*for (String key: this.flights.keySet()) {
			System.out.print(this.flights.get(key).size()+" ");
		}
		System.out.println();*/
	}
	
	public HashMap<String,ArrayList<Object[]>> loadData(String fname) {
		HashMap<String,ArrayList<Object[]>> result = 
				new HashMap<String,ArrayList<Object[]>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line="";
			while ((line=br.readLine()) != null) {
				String[] slist = line.split(",");
				String origin = slist[0];
				String dest = slist[1];
				String depart = slist[2];
				String arrive = slist[3];
				int price = Integer.parseInt(slist[4]);
				String key = origin+","+dest;
				if (result.get(key) == null) result.put(key, new ArrayList<Object[]>());
				ArrayList<Object[]> values = result.get(key);
				Object[] value = new Object[]{depart, arrive, price};
				values.add(value);
			}
			br.close();
		} catch (Exception e) {
			return null;
		}
		return result;
	}
	
	public int getminutes(String t) {
		String[] slist = t.split(":");
		try {
			int hh = Integer.parseInt(slist[0]);
			int mm = Integer.parseInt(slist[1]);
			int result = 60*hh+mm;
			return result;
		} catch (Exception e) {	}
		return -1;
	}
	
	public void printschedule(int[] r) {
		for (int d=0; d<r.length/2; d++) {
			String name = people[d][0];
			String origin=people[d][1];
			//out=flights[(origin,destination)][r[d]]
			Object[] out = this.flights.get(origin+","+destination).get(r[d*2]);
			//ret=flights[(destination,origin)][r[d+1]]
			Object[] ret = this.flights.get(destination+","+origin).get(r[d*2+1]);
			
			System.out.println(name+"\t"+origin+"\t"+out[0]+"-"+out[1]+"\t$"+out[2]+
					"\t"+ret[0]+"-"+ret[1]+"\t$"+ret[2]);
		}
	}
	
	public double schedulecost(int[] sol) {
		double totalprice = 0.0;
		int latestarrival = 0;
		int earliestdep = 24*60;
		
		for (int d=0; d<this.people.length; d++) {
			String origin=people[d][1];
			// Get the inbound and outbound flights
			Object[] outbound = this.flights.get(origin+","+destination).get(sol[d*2]);
			Object[] returnf = this.flights.get(destination+","+origin).get(sol[d*2+1]);
			
			// Total price is the price of all outbound and return flights
			totalprice += (Integer)outbound[2];
			totalprice += (Integer)returnf[2];
			
			// flight duration
			totalprice += getminutes((String)outbound[1])-getminutes((String)outbound[0]);
			totalprice += getminutes((String)returnf[1])-getminutes((String)returnf[0]);
			
			// Track the latest arrival and earliest departure
			if (latestarrival<getminutes((String)outbound[1]))
				latestarrival = getminutes((String)outbound[1]);
			if (earliestdep>getminutes((String)returnf[0]))
				earliestdep = getminutes((String)returnf[0]);
		}
		
		// Every person must wait at the airport until the latest person arrives.
		// They also must arrive at the same time and wait for their flights.
		int totalwait=0;
		for (int d=0; d<sol.length/2; d++) {
			String origin=people[d][1];
			// Get the inbound and outbound flights
			Object[] outbound = this.flights.get(origin+","+destination).get(sol[d*2]);
			Object[] returnf = this.flights.get(destination+","+origin).get(sol[d*2+1]);
			
			totalwait += latestarrival-getminutes((String)outbound[1]);
			totalwait += getminutes((String)returnf[0])-earliestdep;
		}
		
		// Does this solution require an extra day of car rental? That'll be $50!
		if (latestarrival>earliestdep) totalprice += 50;
		
		return totalprice+totalwait*0.5;
	}

}











