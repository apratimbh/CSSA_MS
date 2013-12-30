package funcat;

public class edge {
	public vertex v1=null,v2=null;
	public double l=0,c=0;
	public edge(vertex v1,vertex v2,double lowerbound,double capacity)
	{
		this.v1=v1;
		this.v2=v2;
		this.l=lowerbound;
		this.c=capacity;
	}
}
