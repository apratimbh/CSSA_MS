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
	public double[][] result=null;
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
		try {
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		result=read_file("fun_cellcycle_result.txt");

		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File("E:/ontologies/cellcycle_FUN.owl"));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();

		for(int test_ex_no=15;test_ex_no<result[0].length;test_ex_no++)
		{
			// --- create lists
			System.out.println("\n\nNew example - - - - - - - - - -");
			double[] weights=new double[result.length];
			for(int i=0;i<result.length;i++)
			{
				weights[i]=result[i][test_ex_no];
			}

			ArrayList<Supernode> sn_list=create_supernodes(vertex,weights,vertex); 
			ArrayList<Supernode> questionable=new ArrayList<Supernode>();
			ArrayList<String> ms=new ArrayList<String>();
			ArrayList<String> selected=new ArrayList<String>();

			ArrayList<String> considered_nodes=new ArrayList<String>();
			Split current_best=null;

			// -------------

			// ------ CSSA

			int k=0;
			while(k<5)
			{
				// ----- select supernode with max snv

				Supernode selected_sn=null;
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
				if(selected.contains(selected_sn.parent)||selected_sn.parent=="NIL") // If it is the child of some previously selected node
				{
					selected_sn.print();
					for(String tmp : selected_sn.nodes) 
					{
						considered_nodes.add(tmp);
					}
					boolean supernode_child_of_ms_flag=false;
					boolean supernode_found_sibling_flag=false;
					boolean supernode_considered=false;
					// --- check if child of previously selected ms node

					main_loop: for(String ms_node : ms)
					{
						OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+ms_node));
						OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+selected_sn.ms));
						OWLAxiom axiom=factory.getOWLSubClassOfAxiom(c2, c1);


						Split best_split=null;
						if(reasoner.isEntailed(axiom)) // if it is a child of a previously selected ms node 
						{
							System.out.println("Child of ms");
							selected_sn.print();
							supernode_child_of_ms_flag=true;
							supernode_found_sibling_flag=false;
							double curr_best_sibling_snv=0;
							for(Supernode pot_sib : questionable) // search for siblings
							{
								OWLClass c3=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+pot_sib.ms));
								OWLAxiom axiom1=factory.getOWLSubClassOfAxiom(c3,c1);
								if(reasoner.isEntailed(axiom1))
								{
									OWLAxiom axiom2=factory.getOWLSubClassOfAxiom(c2,c3);
									OWLAxiom axiom3=factory.getOWLSubClassOfAxiom(c3,c2);
									if(!reasoner.isEntailed(axiom2)&&!reasoner.isEntailed(axiom3))
									{

										// --- Sibling found
										System.out.println("Sibling found");
										supernode_found_sibling_flag=true;
										Split temp=new Split(selected_sn,pot_sib);
										if(temp.snv>curr_best_sibling_snv)
										{
											curr_best_sibling_snv=temp.snv;
											best_split=temp;
										}
									}
								}
							} // end of search
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
							else 
							{
								questionable.add(selected_sn);
							}
							break main_loop;
						} //  end of search for siblings if it is child of some prev. ms
					}    // -- end of main loop

					// --- afterwards
					if(!supernode_child_of_ms_flag&&!(selected_sn.parent=="NIL"))
					{
						System.out.println("1");
						Split temp=new Split(selected_sn,null);
						System.out.println("Split value- "+temp.snv);
						//System.out.println("Here: ");
						//selected_sn.print();
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
						sn_list=cleanup(vertex,considered_nodes,weights);
					}
					else if(!supernode_child_of_ms_flag&&(selected_sn.parent=="NIL")) 
					{
						System.out.println("2");
						for(String tmp : selected_sn.nodes)
						{
							selected.add(tmp);
							//System.out.println("Selected- "+tmp);
						}
						System.out.println("Selected-ms: "+selected_sn.ms);
						ms.add(selected_sn.ms);
						k++;
						considered_nodes.clear();
						sn_list=cleanup(vertex,selected,weights);
					}
					else 
					{
						System.out.println("3");
						sn_list=cleanup(vertex,considered_nodes,weights);
					}
					if(sn_list==null)
					{
						System.out.println("Entered");
						for(String tmp : current_best.nodes)
						{
							selected.add(tmp);
						}
						for(String tmp : current_best.ms)
						{
							ms.add(tmp);
							System.out.println("Selected- "+tmp);
						}
						k++;
						considered_nodes.clear();
						sn_list=cleanup(vertex,selected,weights);
					}
				} // -- end
				else
				{
					String parent=sn_list.get(selected_sn_idx).parent;
					selected_sn.add(parent, weights[vertex.indexOf(parent)]);
					sn_list.set(selected_sn_idx,selected_sn);
				}

				//  -------
			} // ---- while loop (one example)

			// --------------
		} // --for loop end // all examples
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
