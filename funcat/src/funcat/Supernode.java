package funcat;

import java.util.ArrayList;

public class Supernode {
	ArrayList<String> nodes=null;
	ArrayList<String> parents_list=null;
	ArrayList<String> ms_list=null;
	String parent="";
	String ms="";
	double snv=0;
	int size=0;
	public Supernode(String node,double val)
	{
		nodes=new ArrayList<String>();
		nodes.add(node);
		this.snv=val;
		this.size++;
		this.ms=node;
		if(node.length()>2)
		{
			this.parent=node.substring(0, node.length()-3);
		}
		else
		{
			this.parent="NIL";
		}
	}
	public Supernode(ArrayList<String> node_list,double val,ArrayList<String> parents)
	{
		nodes=new ArrayList<String>();
		parents_list=new ArrayList<String>();
		for(String node : node_list)
		{
			nodes.add(node);
			this.size++;
		}
		this.snv=val;
		for(String parent : parents)
		{
			this.parents_list.add(parent);
		}
	}
	public Supernode(ArrayList<String> node_list,double val,ArrayList<String> parents,ArrayList<String> ms_list)
	{
		nodes=new ArrayList<String>();
		parents_list=new ArrayList<String>();
		this.ms_list=new ArrayList<String>();
		for(String node : node_list)
		{
			nodes.add(node);
			this.size++;
		}
		this.snv=val;
		for(String parent : parents)
		{
			this.parents_list.add(parent);
		}
		for(String ms : ms_list)
		{
			this.ms_list.add(ms);
		}
	}
	public Supernode(String  node,double val,ArrayList<String> parents)
	{
		parents_list=new ArrayList<String>();
		nodes=new ArrayList<String>();
		ms_list=new ArrayList<String>();
		nodes.add(node);
		this.size++;
		this.snv=val;
		this.ms_list.add(node);
		if(parents.size()!=0)
		{
			for(String parent : parents)
			{
				this.parents_list.add(parent);
			}
		}
	}
	public void add(String node,double val)
	{
		nodes.add(node);
		this.snv=(nodes.size()*snv+val)/(nodes.size()+1);
		this.size++;
		if(node.length()>2)
		{
			this.parent=node.substring(0, node.length()-3);
		}
		else
		{
			this.parent="NIL";
		}
	}
	public void print()
	{
		System.out.println("\nSupernode: ");
		System.out.println("Nodes:");
		for(String tmp : nodes)
		{
			System.out.print(tmp+", ");
		}
		System.out.println("Snv: "+this.snv);
		System.out.println("Patents:");
		for(String tmp : parents_list)
		{
			System.out.print(tmp+", ");
		}
		System.out.println();
	}
}

