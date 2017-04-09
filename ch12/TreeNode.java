package ch12;

import java.util.LinkedHashMap;

public class TreeNode {
	private String name;
	private int count;
	private TreeNode nodeLink;
	private TreeNode parentNode;
	private LinkedHashMap<String, TreeNode> children;
	
	public TreeNode(String nameValue, int numOccur, TreeNode parentNode) {
		this.name = nameValue;
		this.count = numOccur;
		this.nodeLink = null;
		this.parentNode = parentNode;
		this.children = new LinkedHashMap<String, TreeNode>();
	}
	
	public void inc(int numOccur) {
		this.count += numOccur;
	}
	
	public void disp() { disp(1); }
	
	public void disp(int ind) {
		for (int i=0; i<ind; i++) System.out.print(" ");
		System.out.println(this.name+" "+this.count);
		for (TreeNode child: this.children.values()) child.disp(ind+2);
	}
	
	public void addChild(String key, TreeNode value) {
		this.children.put(key, value);
	}
		
	public LinkedHashMap<String, TreeNode> getChildren() { return this.children; }
	public TreeNode getNodeLink() {	return nodeLink; }
	public void setNodeLink(TreeNode node) { this.nodeLink = node; }
	public TreeNode getParent() { return this.parentNode; }
	public String getName() { return this.name;	}
	public int getCount() { return this.count; }

}
