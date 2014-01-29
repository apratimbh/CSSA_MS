package funcat;

import java.util.ArrayList;

public class Split {
	ArrayList<String> nodes=new ArrayList<String>();
	ArrayList<String> ms=new ArrayList<String>();
	ArrayList<Supernode> cointained_supernodes=new ArrayList<Supernode>();
	String parent="";
	double snv=0;
	double new_curr_best=0;
	public Split(Supernode node1,Supernode node2)
	{
		this.parent=node1.parent;
		for(String tmp : node1.nodes)
		{
			this.nodes.add(tmp);
		}
		ms.add(node1.ms);
		if(node2!=null)
		{
			for(String tmp : node2.nodes)
			{
				this.nodes.add(tmp);
			}
			ms.add(node2.ms);
			this.snv=(node1.snv*node1.size+node2.snv*node2.size)/(node1.size+node2.size);
		}
		else
			this.snv=(node1.snv*node1.size)/(node1.size);
	}
	public Split(ArrayList<String> nodes,ArrayList<String> vertex,ArrayList<String> ms_list,String parent_ms,double[] weights,ArrayList<Supernode> contained_supernodes)
	{
		// here parent is the ms node the split is a child of
		if(parent_ms!=null)
			this.parent=parent_ms;
		double avg=0;
		for(String tmp : nodes)
		{
			this.nodes.add(tmp);
			avg+=weights[vertex.indexOf(tmp)];
		}
		for(String tmp : ms_list)
		{
			this.ms.add(tmp);
		}
		for(Supernode sn : contained_supernodes)
		{
			this.cointained_supernodes.add(sn);
		}
		this.snv=(double)avg/nodes.size();
	}
	public Split(ArrayList<String> nodes,ArrayList<String> ms_list,String parent_ms,double new_curr_best)
	{
		// here parent is the ms node the split is a child of
		if(parent_ms!=null)
			this.parent=parent_ms;
		double avg=0;
		for(String tmp : nodes)
		{
			this.nodes.add(tmp);
		}
		for(String tmp : ms_list)
		{
			this.ms.add(tmp);
		}
		this.new_curr_best=new_curr_best;
	}
}
class pr {
	double p=0,r=0;
	public pr(double p,double r) {
		this.p=p;
		this.r=r;  
	}
}
