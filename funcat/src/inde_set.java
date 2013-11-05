import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

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


public class inde_set {
	public double[][] result=null,flabels=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		inde_set o=new inde_set();
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
		result=read_file("D:/matp/funcat_cellcyle_1.txt");
		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File("E:/ontologies/cellcycle_FUN.owl"));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();

		for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
		{
			// --- create lists
			System.out.println("\n\nNew example - - - - - - - - - -"+test_ex_no);
			double[] weights=new double[result.length];
			for(int i=0;i<result.length;i++)
			{
				weights[i]=result[i][test_ex_no];
			}
			double max=weights[0],min=weights[0];
			for(int i=0;i<weights.length;i++)
			{
				if(weights[i]>max)
				{
					max=weights[i];
				}
				if(weights[i]<min)
				{
					min=weights[i];
				}
			}
			for(int i=0;i<weights.length;i++)
			{
				weights[i]=(weights[i]-min)/(max-min);
			}
			ArrayList<String> nvertex=get_vertex(vertex,weights,0.5);
			ArrayList<vertex> vertex_list=new ArrayList<vertex>();
			ArrayList<edge> edge_list=new ArrayList<edge>();
			for(String v : nvertex)
			{
				vertex v1=new vertex(v,1);
				vertex v2=new vertex(v,2);
				vertex_list.add(v1);
				vertex_list.add(v2);
				edge_list.add(new edge(v1,v2,weights[vertex.indexOf(v)]));
			}
			for(String v1: nvertex)
			{
				OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+v1));
				for(String v2 : nvertex)
				{
					OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+v2));
					OWLAxiom axiom=factory.getOWLSubClassOfAxiom(c2, c1);
					if(reasoner.isEntailed(axiom))
					{
						vertex v1l=null,v2e=null;
						for(vertex v : vertex_list)
						{
							if(v.name==v1&&v.type==2)
							{
								v1l=v;
							}
							else if(v.name==v2&&v.type==1)
							{
								v2e=v;
							}
						}
						edge_list.add(new edge(v1l,v2e,999999));
					}
				}
			}
			// --check which vertices do not have incoming edges
			ArrayList<String> has_in_e=new ArrayList<String>();
			for(edge e : edge_list)
			{
				has_in_e.add(e.v2.name);
			}
			ArrayList<String> max_v=new ArrayList<String>();
			for(String v : vertex)
			{
				if(!has_in_e.contains(v))
				{
					max_v.add(v);
				}
			}
			// -- check which vertices do not have out-going edges
			ArrayList<String> has_out_e=new ArrayList<String>();
			for(edge e : edge_list)
			{
				has_out_e.add(e.v1.name);
			}
			ArrayList<String> min_v=new ArrayList<String>();
			for(String v : vertex)
			{
				if(!has_in_e.contains(v))
				{
					min_v.add(v);
				}
			}

			// --- create source and destination vertices
			vertex vs=new vertex("s",2);
			vertex vd=new vertex("d",1);
			vertex_list.add(vs);
			vertex_list.add(vd);
			// -- create incoming edges
			for(String v : max_v )
			{
				vertex vi=null;
				for(vertex vv : vertex_list)
				{
					if(vv.name==v&&vv.type==1)
					{
						vi=vv;
					}
				}
				edge_list.add(new edge(vs,vi,999999));
			}
			// -- create outgoing edges

			for(String v : min_v )
			{
				vertex vi=null;
				for(vertex vv : vertex_list)
				{
					if(vv.name==v&&vv.type==2)
					{
						vi=vv;
					}
				}
				edge_list.add(new edge(vi,vd,999999));
			}
			// --------------

		} // --for loop end // all examples
	}
	public ArrayList<String> get_vertex(ArrayList<String> vertex,double[] weights,double w)
	{
		ArrayList<String> tmp=new ArrayList<String>();
		for(int i=0;i<weights.length;i++)
		{
			if(weights[i]>=w)
			{
				tmp.add(vertex.get(i));
			}
		}
		return tmp;

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
class vertex {
	String name="";
	int type=0; // 1 for entering 2 leaving 
	public vertex(String name,int type) {
		this.name=name;
		this.type=type;
	}
}
class edge {
	vertex v1=null,v2=null;
	double capacity=0;
	public edge(vertex v1,vertex v2,double capacity)
	{
		this.v1=v1;
		this.v2=v2;
		this.capacity=capacity;
	}
}