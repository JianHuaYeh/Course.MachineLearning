package chx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class OPTICS {
	
	public class Point {
		public double[] coord;
		public double reachabilityDistance;
		public boolean processed;
		
		public Point(double[] coord) {
			this.coord = coord;
			this.reachabilityDistance = UNDEFINED;
			this.processed = false;
		}
	}
	
	public static final double UNDEFINED = -1.0;
	private double eps;
	private int MinPts;
	private ArrayList<Point> data;
	
	public static void main(String[] args) {
		String path="";
		String fname = path+"data.txt";
		OPTICS op = new OPTICS(fname);
		op.go();
	}
	
	public void go() {
		for (Point p: this.data) {
			if (p.processed == true) continue;
			ArrayList<Point> N = getNeighbors(p, this.eps);
			p.processed = true;
			//!!!!!!!!!!!!!!!!!!!!!!!!!!!
			//output p to the ordered list
			if (coreDistance(p, this.eps, this.MinPts) != UNDEFINED) {
				PriorityQueue<Point> Seeds = new PriorityQueue<Point>();
				update(N, p, Seeds, this.eps, this.MinPts);
				while (!Seeds.isEmpty()) {
					Point q = Seeds.poll();
					ArrayList<Point> N2 = getNeighbors(q, eps);
					q.processed = true;
					//!!!!!!!!!!!!!!!!!!!!!!!!!!!
					//output q to the ordered list
					if (coreDistance(q, this.eps, this.MinPts) != UNDEFINED) {
						update(N2, p, Seeds, this.eps, this.MinPts);
					}
				}
			}
		}
	}
	
	public ArrayList<Point> getNeighbors(Point p, double eps) {
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// eps
		return null;
	}
	
	public double coreDistance(Point p, double eps, int MinPts) {
		return 0.0;
	}
	
	public void update(ArrayList<Point> N, Point p, PriorityQueue Seeds, double eps, int MinPts) {
		double coredist = coreDistance(p, eps, MinPts);
		for (Point o: N) {
			if (o.processed == true) continue;
			double newReachDist = Math.max(coredist, dist(p, o));
			if (o.reachabilityDistance == UNDEFINED) { // o is not in Seeds
				o.reachabilityDistance = newReachDist;
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!
				//Seeds.insert(o, new-reach-dist)
			}
			else { // o in Seeds, check for improvement
				if (newReachDist < o.reachabilityDistance) {
					o.reachabilityDistance = newReachDist;
					//!!!!!!!!!!!!!!!!!!!!!!!!!!!
					//Seeds.move-up(o, new-reach-dist)
				}
			}
		}
	}
	
	public double dist(Point p1, Point p2) {
		return distEclud(p1.coord, p2.coord);
	}
	
	public OPTICS(String fname) {
		this(fname, 300, 700);
	}
	
	public OPTICS(String fname, double eps, int MinPts) {
		this.data = loadDataSet(fname);
		this.eps = eps;
		this.MinPts = MinPts;
	}
	
	public ArrayList<Point> loadDataSet(String s) {
		ArrayList<Point> dataMat = new ArrayList<Point>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(s));
			String line="";
			while ((line=br.readLine()) != null) {
				String[] lineArr = line.trim().split("\t");
				double[] rec = new double[]{Double.parseDouble(lineArr[0]), 
						Double.parseDouble(lineArr[1])};
				Point p = new Point(rec);
				dataMat.add(p);
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
}
