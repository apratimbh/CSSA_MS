import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import org.ejml.simple.SimpleMatrix;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


public class matlab_cssa_m {
	public static int[][]  dis = null;
	public static void main(String[] args)  throws OWLOntologyCreationException
	{
		matlab_cssa_m mc=new matlab_cssa_m();
		mc.main();
	}
	public void main() throws OWLOntologyCreationException
	{
		BufferedReader br = null, pbr = null;
		 boolean flag=false;
		 double[][] arr = null;
		 int n_labels = 0,a1=0,a2=0;
		 String err="";
		 String file="latest";
		 ArrayList<String> vertex=null;
		try {
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
			dis=(int[][]) new ObjectInputStream(new FileInputStream("dis")).readObject();
			int c=0,i;
			String curr;
			pbr = new BufferedReader(new FileReader("D:/matp/"+file+".txt"));
			while ((curr = pbr.readLine()) != null) {
				if(!curr.isEmpty())
				{
					if(a1==0)
						a2=curr.split(",").length;
					a1++;
				}
			}
			System.out.println("Rows> "+a1+"Columns> "+a2);
				
			arr = new double[a1][a2];
			br = new BufferedReader(new FileReader("D:/matp/"+file+".txt"));
			flag=false;
			c=0;
			while ((curr = br.readLine()) != null) {
				if(!curr.isEmpty()) 
				{
					String[] part=curr.split(",");
					//System.out.println("Part Length>"+part.length+" est: "+a1);
					for(i=0;i<a2;i++)
					{
						err=part[i];
						arr[c][i]=Double.parseDouble(part[i]);
					}
					c++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: "+err);
		}
		
		SimpleMatrix Result=new SimpleMatrix(arr);
		//Result=Result.transpose();
		SimpleMatrix final_r=new SimpleMatrix(Result.numRows(),Result.numCols());
		
		
		// Load actual results ---------------------------------------------------------------------------------------------
		
		double[][] labels = null;
		try {
		a1=0;a2=0;
	    int c=0;
		flag=false;
		String curr;
		pbr = new BufferedReader(new FileReader("E:/datasets/cellcycle-test-super.arff"));
		while ((curr = pbr.readLine()) != null) {
			if(curr.equals("@data"))
			{
				flag=true;
				continue;
			}
			if(flag)
			{
				if(!curr.isEmpty())
					a2++;
			}
			else
			{
				if(!curr.isEmpty()&&(curr.contains("attribute")||curr.contains("ATTRIBUTE")))
					a1++;
			}
		}
		pbr.close();
		System.out.println("Attribute> "+a1+"Data>"+a2);
		br = new BufferedReader(new FileReader("E:/datasets/cellcycle-test-super.arff"));
		n_labels=(Integer) new ObjectInputStream(new FileInputStream("n_labels")).readObject();
		System.out.println("Number of Labels: "+n_labels);
        String line=null;
        labels = new double[a2][n_labels];
        flag=false;
        while((curr=br.readLine())!=null)
        {
        	if(curr.equals("@data"))
			{
				flag=true;
				continue;
			}
			if(flag)
			{
				if(!curr.isEmpty()) 
				{
					String[] part=curr.split(",");
					for(int i=0;i<(n_labels);i++)
					{
						//err=part[a1+i-n_lables];
						labels[c][i]=Double.parseDouble(part[a1-n_labels+i]);
					}
					c++;
				}
			}
        }
        br.close();
		}
		catch(Exception ee)
		{
			ee.printStackTrace();
		}
        
        SimpleMatrix O=new SimpleMatrix(labels);
		O=O.transpose();
		O.printDimensions();
		
		// CSSA  -------------------------------------------------------------------------------------------------------
		
		SimpleMatrix recall=new SimpleMatrix(final_r);
		File file1 = new File("E:/ontologies/cellcycle.owl"); //go_daily-termdb  treeOnt
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	 	OWLOntology go = manager.loadOntologyFromOntologyDocument(file1);
	 	System.out.println("Loaded ontology: " + go.getOntologyID().toString());
	 	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	 	ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
	 	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
	 	OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
	 	OWLDataFactory factory = manager.getOWLDataFactory();
		
		
		ArrayList<String> err_l=null;
	    try {
	    for(int q=1;q<8;q++)
	    {
	    	System.out.println("For label size="+q);
	    	double limit=q;
		    double prev=0;
		    double avg_depth=0;
		 	for(int i=0;i < Result.numCols();i++)
		 	{
		 		if((i*100)/Result.numCols()!=prev)
		 			System.out.println((i*100)/Result.numCols()+"%");
		 		prev=(i*100)/Result.numCols();
		 		
			 	
			 	// ---- CSSA ------------------------------------------
			 	
			 	/*-----*/ 
			 	ArrayList<ArrayList<String>> super_nodes=new ArrayList<ArrayList<String>>();
			 	/*-----*/ 
			 	HashMap<ArrayList<String>,Double> snv=new HashMap<ArrayList<String>,Double>();
			 	/*-----*/ 
			 	HashMap<ArrayList<String>,Double> selected=new HashMap<ArrayList<String>,Double>();
			 	/*-----*/
			 	
			 	ArrayList<String> root=new ArrayList<String>();
			 	root.add("root");
			 	
			 	//super_nodes.add(root);
			 	
			 	for(String tmp : vertex)
			 	{
			 		ArrayList<String> temp_super_node_l=new ArrayList<String>();
			 		temp_super_node_l.add(tmp);
			 		super_nodes.add(temp_super_node_l);
			 		snv.put(temp_super_node_l, Result.get(vertex.indexOf(tmp), i));
			 		selected.put(temp_super_node_l, new Double(0));
			 	}
			 	selected.remove(root);
			 	selected.put(root, new Double(1));
			 	/*snv.remove(root);
			 	snv.put(root, new Double(1));*/
			 	double gm=0;
			 	
			 	while(limit>gm)
			 	{
			 		ArrayList<String> list = super_nodes.get(100);
			 		//print_list(list,"Intitial");
			 		double max=snv.get(super_nodes.get(100));
			 		for(ArrayList<String> list1 : super_nodes)
			 		{
			 			err_l=list1;
			 			if(snv.get(list1)>max)
			 			{
			 				if(!list1.equals(root)&&selected.get(list1).equals(0.0))
			 				{
				 				max=snv.get(list1);
				 				list=list1;
			 				}
			 			}
			 		}
			 		//print_list(list,"Selected max suprnode: snv"+snv.get(list));
			 		if(list==null)
			 		{
			 			System.out.print("No more left");
			 			break;
			 		}
					boolean flag1=false;
					
					ArrayList<ArrayList<String>> potential=new ArrayList<ArrayList<String>>();

					
					for(int j=0;j<list.size();j++)
					{
						String snode_class=list.get(j);
						OWLClass parentClass = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+snode_class));
			 			OWLClassExpression ce=parentClass.asOWLClass();
			 		 	NodeSet<OWLClass> set=reasoner.getSuperClasses(ce, true);
			 	        for (Node<OWLClass> ind : set) 
			 	        {
			 	        	if(!ind.isBottomNode()&&!ind.isTopNode())
			 	        	{
			 	        		String news=ind+"";
					            String[] part=news.split(" ");
					            part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "");
					            part[1]=part[1].split("#")[1];
					            if(!list.contains(part[1]))
					            {
					            	for(ArrayList<String> list1 : super_nodes)
					        		{
					            		if(list1.contains(part[1]))
					            		{
					            			if(selected.get(list1).equals(1.0))
					            			{
					            				
					            			}
					            			else
					            			{
					            				//print_list(list1,"Potential: "+snv.get(list1));
					            				flag1=true;
					            				potential.add(list1);
					            			}
					            		}
					        		}
					            }
			 	        	}
			 	        }
					}
					if(potential.contains(root))
					{
						System.out.println("Warning contains root. value:"+snv.get(root));
					}
					//System.out.println("Potential-size: "+potential.size());
					if(flag1)
					{
						ArrayList<String> select_to_merge=potential.get(0);
						double min=snv.get(potential.get(0));
						for(ArrayList<String> list1 : potential)
				 		{
							if(snv.get(list1)<min)
							{
								min=snv.get(list1);
								select_to_merge=list1;
							}
				 		}
						ArrayList<String> new_super_node=merge(list,select_to_merge);
						super_nodes.remove(list);
						super_nodes.remove(select_to_merge);
						super_nodes.add(new_super_node);
						//print_list(new_super_node,"new_super_node");
						//print_list(list,"list");
						//print_list(select_to_merge,"select_to_merge");
						int idxR=vertex.indexOf("root");
						double sum=0,dep=0;
						for(String tmp : list)
						{
							sum=sum+(dis[idxR][vertex.indexOf(tmp)]*Result.get(vertex.indexOf(tmp), i));
							dep+=dis[idxR][vertex.indexOf(tmp)];
						}
						for(String tmp : select_to_merge)
						{
							sum=sum+(dis[idxR][vertex.indexOf(tmp)]*Result.get(vertex.indexOf(tmp), i));
							dep+=dis[idxR][vertex.indexOf(tmp)];
						}
						double avg=(double)sum/dep;
						//double avg=((snv.get(list)*list.size())+(snv.get(select_to_merge)*select_to_merge.size()))/(list.size()+select_to_merge.size());
						snv.remove(list);
						snv.remove(select_to_merge);
						snv.put(new_super_node, avg);
						selected.remove(list);
						selected.remove(select_to_merge);
						selected.put(new_super_node, new Double(0.0));
					}
					else
					{
						//snv.remove(list);
						selected.remove(list);
						selected.put(list, Math.min(1, (limit-gm)/list.size()));
						//System.out.println("SNV: "+ Math.min(1, (limit-gm)/list.size())+"List size: "+list.size());
						gm=gm+list.size();
						//print_list(list,"selected");
					}
			 	}
			 	
			 	// ----------------------------------------------------
			 	
			 	ArrayList<String> cssa_r=new ArrayList<String>();
			 	int c=0;
			 	double t_avg=0;
			 	for(ArrayList<String> list1 : super_nodes)
		 		{
			 		if(selected.get(list1)>0.0)
			 		{
			 			for(String tmp : list1)
			 			{
			 				cssa_r.add(tmp);
			 				int m=vertex.indexOf("root");
			 				int n=vertex.indexOf(tmp);
			 				t_avg+=dis[m][n];
			 				c++;
			 			}
			 		}
		 		}
			 	t_avg=(double)t_avg/c;
			 	avg_depth+=t_avg;
			 	//print_list(cssa_r,"Selected:"+i);
			 	ArrayList<Double> final_r_temp=new ArrayList<Double>();
		 		for(String tmp : vertex)
		 		{
		 			if(cssa_r.contains(tmp))
		 			{
		 				//System.out.println("Selected: "+tmp);
		 				final_r_temp.add(new Double(1));
		 			}
		 			else
		 			{
		 				final_r_temp.add(new Double(0));
		 			}
		 		}
			 	ArrayList<String> temp_l=new ArrayList<String>(vertex);
		 	    ArrayList<Double> recall_temp=new ArrayList<Double>(final_r_temp);
		 	    int x=0;
		 	    for(double d : final_r_temp)
		 	    {
		 	    	if(d==1)
		 	    	{
		 	    		recall_temp.set(x, new Double(1));
		 	    		String gene=vertex.get(x);
		 	    		OWLClass t_cls = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+gene));
		 	    		NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
		 	    		for(Node<OWLClass> cls : set)
		 	    		{
		 	    			if(!cls.isTopNode())
		 	    			{
		 	    				String news=cls+"";
		 	    				String[] part=news.split(" ");
		 	    				part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
		 	    				recall_temp.set(vertex.indexOf(part[1]), new Double(1));
		 	    			}
		 	    		}
		 	    	}
		 	    	x++;
		 	    }
		 	    x=0;
		 	    for(double d : final_r_temp)
		 	    {
		 	    	final_r.set(x,i,d);
		 	    	x++;
		 	    }
		 	   x=0;
		 	    for(double d : recall_temp)
		 	    {
		 	    	recall.set(x,i,d);
		 	    	x++;
		 	    }
		 	}
		 
		 	
		 	
		 	
		 	
		 	
		 	
		 	final_r.printDimensions();
		 	O.printDimensions();
		 	
		 	
		 	
		 	//O.print();
		 //	final_r.print(); 
		 	double tp=0,fp=0,fn=0,no=0,no1=0,pi=0,ri=0,p=0,r=0,tpfp=0,tpfn=0;
		 	for(int i=0;i<final_r.numRows();i++)
		 	{
		 		/*flag=false;
		 		double tp1=0,fp1=0,fn1=0;
		 		for(int j=0;j<final_r.numCols();j++)
		 		{
		 			if(final_r.get(i,j) == 1)
		 			{
		 				flag=true;
		 				if(O.get(i,j) == 1)
		 				{
		 					tp1=tp1+1;
		 				}
		 				else
		 				{
		 					/*boolean fflag=true;
		 					String gene=vertex.get(j);
		 					OWLClass t_cls = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+gene));
		 					NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
		 					for(Node<OWLClass> cls : set)
		 					{
		 						if(!cls.isTopNode())
		 						{
		 							String news=cls+"";
		 							String[] part=news.split(" ");
		 							part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
		 							int l=vertex.indexOf(part[1]);
		 							if(O.get(l, j)==1)
		 								fflag=false;
		 						}
		 					}
		 					if(flag)
		 						fp1=fp1+1;
		 				}
		 			}
		 		}
		 		if(flag)
		 		{
			 		for(int j=0;j<recall.numCols();j++)
			 		{
			 			if(recall.get(i, j)==1)
			 			{
			 				if(final_r.get(i, j)==0)
				 			{
			 					fn1++;
				 			}
			 			}
			 		}
		 		}
		 		if(tp1>0||fp1>0)
		 		{
		 			tp=tp+tp1;
		 			fp=fp+fp1;
		 		}
		 		if(flag&&tp1>0)
		 		{
		 			System.out.println(fn1);
		 			fn=fn+fn1;
		 		}*/
		 		double tp1=0,tpfp1=0,tpfn1=0;
		 		boolean flag1=false;
		 		for(int j=0;j<final_r.numCols();j++)
		 		{
		 			tp1=tp1+final_r.get(i, j)*O.get(i, j);
		 		}
		 		for(int j=0;j<final_r.numCols();j++)
		 		{
		 			if(final_r.get(i, j)==1)
		 			{
		 				tpfp1=tpfp1+final_r.get(i, j);
		 				flag1=true;
		 			}
		 		}
		 		for(int j=0;j<O.numCols();j++)
		 		{
		 			if(O.get(i, j)==1)
		 			tpfn1=tpfn1+O.get(i, j);
		 		}
		 		tp+=tp1;
		 		tpfp+=tpfp1;
		 		//if(flag1)
		 		tpfn+=tpfn1;
		 	}
		 	//final_r.print();
		 	/*System.out.println("Precision:"+((double)p/pi));
		 	System.out.println("Recall:"+((double)r/ri));
		 	/*System.out.println("no- "+no+" no1- "+no1);*/
			System.out.println("Precision:"+((double)tp/(tpfp)));
			System.out.println("Recall:"+((double)tp/(tpfn)));
			System.out.println("Depth-avg: "+avg_depth/final_r.numCols());
	    }
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    	System.out.println("Mesg: "+e.getMessage());
	    	print_list(err_l,"error");
	    }
	}
	public ArrayList<String> merge(ArrayList<String> a,ArrayList<String> b)
	{
		ArrayList<String> c=new ArrayList<String>();
		for(String tmp: a)
		{
			c.add(tmp);
		}
		for(String tmp: b)
		{
			c.add(tmp);
		}
		return c;
	}
	public void print_list(ArrayList<String> a,String name)
	{
		System.out.println("Printing: "+name+"\n");
		for(String tmp: a)
		{
			System.out.print(tmp+"\t");
		}
		System.out.println();
	}
}
