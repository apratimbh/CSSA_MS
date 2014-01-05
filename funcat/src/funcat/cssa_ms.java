package funcat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;


public class cssa_ms {
	public double[][] result=null,flabels=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		cssa_ms o=new cssa_ms();
		o.main();
	}

	public void main() throws OWLOntologyCreationException
	{
		ArrayList<String> vertex = null;
		// the vertex file contains the name of the concepts (labels) in the ontology
		try {
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// the result file from matlab
		result=read_file("D:/matp/funcat_cellcyle_1.txt");
		//flabels=read_file("test.arff");
		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File("E:/ontologies/cellcycle_FUN.owl"));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();
		// test data to check results
		double[][] test_data=load_test_data("E:/dataset/cellcycle_FUN_test_expanded.arff",77);
		int k=1;
		// k is the number of labels required to be predicted
		while(k<vertex.size())
		{
			double tp=0;
			double fp=0;
			double fn=0;
			System.out.println("k -- :"+k);
			// for each example in the test dataset
			for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
			{
				// --- create lists
				//System.out.println("\n\nNew example - - - - - - - - - -"+test_ex_no);
				
				// load the results for each example
				double[] weights=new double[result.length];
				for(int i=0;i<result.length;i++)
				{
					weights[i]=result[i][test_ex_no];
				}
				
				// list of supernodes (each supernode contains one node and snv is the weight of that node)
				ArrayList<Supernode> sn_list=create_supernodes(vertex,weights,vertex);
				// list of questionable supernodes
				ArrayList<Supernode> questionable=new ArrayList<Supernode>();
				// current selected most specific labels
				ArrayList<String> ms=new ArrayList<String>();
				// current selected lables (contains the  most specific nodes and nodes logically implied by a ms node)
				ArrayList<String> selected=new ArrayList<String>();
				// list of nodes which were considered while searching for splits
				// if all nodes were considered then we can exit
				ArrayList<String> considered_nodes=new ArrayList<String>();
				Split current_best=null;

				// -------------

				// ------ CSSA

				int k1=0;
				// k1 keeps track of the number of ms labels (nodes) selected so far
				while(k1<k)
				{
					// ----- select supernode with max snv
					Supernode selected_sn=sn_list.get(0);
					int selected_sn_idx=0;
					double max_snv=sn_list.get(0).snv;
					for(Supernode tmp : sn_list)
					{
						if(tmp.snv>max_snv)
						{
							selected_sn=tmp;
							selected_sn_idx=sn_list.indexOf(tmp);
						}
					}
					// we check if its parent has been selected (that is, its parent is a most specific node or is logically implied by a ms node)
					if(selected.contains(selected_sn.parent)||selected_sn.parent=="NIL") // If it is the child of some previously selected node
					{
						// if its parent is selected it is now a node in the non-decreasing graph and so we can search for a sibling 
						//  ---- to form a split
						// also all labels (nodes) in the supernode has been considered
						for(String tmp : selected_sn.nodes) 
						{
							considered_nodes.add(tmp);
						}
						// we have to now search for a sibling
						boolean supernode_child_of_ms_flag=false;
						boolean supernode_found_sibling_flag=false;
						boolean supernode_considered=false;
						
						// --- check if the supernode is a child of previously selected ms node
						main_loop: for(String ms_node : ms)
						{
							// remember all the nodes in the supernode are parents of the most specific node 
							// it is child of a previously selected most specific node then its  most specific node will also be 
							// ---  a child of the  previously selected ms node
							// --- so here we are checking across all previously selected ms nodes
							OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+ms_node));
							OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+selected_sn.ms));
							OWLAxiom axiom=factory.getOWLSubClassOfAxiom(c2, c1);


							Split best_split=null;
							if(reasoner.isEntailed(axiom)) 
							{
								// if it is a child of a previously selected ms node 
								// --- then now we need to search for a sibling
								// --- so we search across all the questionable supernodes
								//System.out.println("Child of ms");
								//selected_sn.print();
								supernode_child_of_ms_flag=true;
								supernode_found_sibling_flag=false;
								double curr_best_sibling_snv=0;
								for(Supernode pot_sib : questionable) // search for siblings
								{
									// we need a sibling which is a child so the same ms node
									OWLClass c3=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+pot_sib.ms));
									OWLAxiom axiom1=factory.getOWLSubClassOfAxiom(c3,c1);
									if(reasoner.isEntailed(axiom1))
									{
										// now we need to check if they are really siblings
										OWLAxiom axiom2=factory.getOWLSubClassOfAxiom(c2,c3);
										OWLAxiom axiom3=factory.getOWLSubClassOfAxiom(c3,c2);
										if(!reasoner.isEntailed(axiom2)&&!reasoner.isEntailed(axiom3))
										{
											// --- Sibling found
											//System.out.println("Sibling found");
											supernode_found_sibling_flag=true;
											Split temp=new Split(selected_sn,pot_sib);
											// there can be many siblings but we select only the one which maximizes the weight (snv)
											if(temp.snv>curr_best_sibling_snv)
											{
												curr_best_sibling_snv=temp.snv;
												best_split=temp;
											}
										}
									}
								} // end of search
								// if we have found a sibling that means we have a new split
								// --- so we compare it with the current best split (by snv)
								if(supernode_found_sibling_flag) // if a sibling has been found
								{
									Split temp=best_split;
									if(current_best==null)
									{
										current_best=temp;
									}
									else
									{
										if(temp==(compare_splits(selected,current_best,temp,weights,vertex)))
										{
											current_best=temp;
											supernode_considered=true;
											sn_list.remove(selected_sn);
										}
									}

								}
								else // if no sibling found then add to the list of questionable supernodes
								{
									questionable.add(selected_sn);
								}
								break main_loop;
							} //  end of search for siblings if it is child of some prev. ms
						}    // -- end of main loop

						// --- afterwards
						// if the supernode is not the child of a previosuly selected ms node 
						if(!supernode_child_of_ms_flag||(selected_sn.parent=="NIL"))
						{
							// we create a new split with just the supernode
							Split temp=new Split(selected_sn,null);
							// Then update the current best split (if the new split is better)
							if(current_best==null)
							{
								current_best=temp;
							}
							else
							{
								if(temp==(compare_splits(selected,current_best,temp,weights,vertex)))
								{
									current_best=temp;
									supernode_considered=true;
								}
							}
							// cleanup returns a list of supernodes consiting of single nodes ehich can be expanded by merging parent nodes
							sn_list=cleanup(vertex,considered_nodes,weights);
						}
						else 
						{
							sn_list=cleanup(vertex,considered_nodes,weights);
						}
						// cleanup tells us how many nodes are left to be considered

						if(sn_list==null)
						{
							// if all nodes considered
							// --- select the split with highest snv
							for(String tmp : current_best.nodes)
							{
								selected.add(tmp);
								//System.out.println("Selected- "+tmp);
							}
							for(String tmp : current_best.ms)
							{
								ms.add(tmp);
							}
							k1++;
							current_best=null;
							considered_nodes.clear();
							for(String s : selected)
							{
								considered_nodes.add(s);
							}
							sn_list=cleanup(vertex,selected,weights);
						}
					} // -- end
					else
					{
						// merge selected supernode with its parent (if part from line 121)
						String parent=sn_list.get(selected_sn_idx).parent;
						selected_sn.add(parent, weights[vertex.indexOf(parent)]);
						sn_list.set(selected_sn_idx,selected_sn);
					}

					//  -------
				} // ---- while loop (one example)
				
				int[] test_data_this_ex=new int[test_data[test_ex_no].length];
				for(int l=0;l<test_data[test_ex_no].length;l++)
				{
					test_data_this_ex[l]=(int) test_data[test_ex_no][l]; // actual labels for this examples (from test data)
				}
				for(String s : selected)
				{
					// if a node (label) has been selected and it is present in the test data then it is a true positive 
					if(test_data_this_ex[vertex.indexOf(s)]==1)
					{
						tp++;
					}
					else
					{
						fp++;
					}
				}
				for(int l=0;l<test_data[test_ex_no].length;l++)
				{
					//  if a node (label) is present in the test data then it should be present in our selected labels 
					if(test_data_this_ex[l]==1)
					{
						// if it is not its a false positive
						if(!selected.contains(vertex.get(l)))
						{
							fn++;
						}
					}
				}
				// --------------
			} // --for loop end // all examples 
			k++;
			// output p and r for each value of k
			System.out.println("Precision: "+tp/(tp+fp));
			System.out.println("Recall: "+tp/(tp+fn));
		}  // ---- end of k - while loop
	}
	public pr calculate_pr(ArrayList<String> selected,double[] weights,ArrayList<String> vertex,int k,double[] results)
	{
		pr tmp=new pr(0,0);
		return tmp;
	}
	public ArrayList<Supernode> cleanup(ArrayList<String> vertex,ArrayList<String> considered,double[] weights)
	{
		ArrayList<String> new_list=new ArrayList<String>();
		for(String tmp : vertex)
		{
			if(!considered.contains(tmp))
			{
				new_list.add(tmp);
			}
		}
		if(new_list.size()==0)
		{
			return null;
		}
		ArrayList<Supernode> new_sn_list=new ArrayList<Supernode>();
		new_sn_list=create_supernodes(new_list,weights,vertex);
		return new_sn_list;
	}

	public Split compare_splits(ArrayList<String> selected,Split s1,Split s2,double[] weights,ArrayList<String> vertex)
	{
		double split1=0,split2=0;
		for(String tmp : selected)
		{
			split1+=weights[vertex.indexOf(tmp)];
		}
		split2=split1;

		for(String tmp : s1.nodes)
		{
			split1+=weights[vertex.indexOf(tmp)];
		}
		split1/=(selected.size()+s1.nodes.size());

		for(String tmp : s2.nodes)
		{
			split2+=weights[vertex.indexOf(tmp)];
		}
		split2/=(selected.size()+s2.nodes.size());

		if(split1>split2)
			return s1;
		else
			return s2;
	}

	public ArrayList<Supernode> create_supernodes(ArrayList<String> sn_list,double[] w,ArrayList<String> vertex)
	{
		ArrayList<Supernode> temp=new ArrayList<Supernode>();
		if(vertex.size()!=w.length)
		{
			System.out.println("Dimensions mis-match");
			System.exit(1);
		}
		for(String tmp : sn_list)
		{
			Supernode tmp_sn=new Supernode(tmp,w[vertex.indexOf(tmp)]);
			temp.add(tmp_sn);
		}
		return temp;
	}
	
	public double[][] load_test_data(String file,int no_of_columns_to_exclude)
	{
		double[][] temp=read_file(file);
		double[][] temp1=new double[temp.length][temp[0].length- no_of_columns_to_exclude];
		for(int j= no_of_columns_to_exclude+1;j<temp[0].length;j++)
		{
			for(int i=0;i<temp.length;i++)
			{
				temp1[i][j-no_of_columns_to_exclude]=temp[i][j];
			}
		}
		return temp1;
	}
	
	public double[][] read_file(String file)
	{
		double[][] temp=null;
		try {

			// -- read file size lines and rows

			System.out.println("Reading file: "+file);
			LineNumberReader reader  = new LineNumberReader(new FileReader(file));
			int rows = 0, cols=0;
			String line = "";
			while ((line = reader.readLine()) != null) {
				if(rows==0)
				{
					cols=line.split(",").length;
					rows++;
				}
			}

			rows = reader.getLineNumber(); 
			reader.close();

			System.out.println("File size: "+rows+" - "+cols);

			// -- read the file

			int i=0,j=0;
			temp = new double[rows][cols];
			BufferedReader br  = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null)
			{
				j=0;
				for(String tmp : line.split(","))
				{
					temp[i][j]=Double.parseDouble(tmp);
					j++;
				}
				i++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

}

class Supernode {
	ArrayList<String> nodes=null;
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
		System.out.print("Supernode: ");
		for(String tmp : nodes)
		{
			System.out.print(tmp+", ");
		}
		System.out.println();
	}
}

class Split {
	ArrayList<String> nodes=new ArrayList<String>();
	ArrayList<String> ms=new ArrayList<String>();
	String parent="";
	double snv=0;
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
			snv=(node1.snv*node1.size)/(node1.size);
	}
}
class pr {
	double p=0,r=0;
	public pr(double p,double r) {
		this.p=p;
		this.r=r;  
	}
}
