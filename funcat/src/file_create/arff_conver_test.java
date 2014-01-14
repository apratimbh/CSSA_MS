package file_create;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
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
public class arff_conver_test {
	public static int[][] path = null, dis = null, is_a = null, part_of = null;
	public static ArrayList<String> vertex = null;
	public static void main(String[] args) throws OWLOntologyCreationException
	{
		File file = new File("E:/ontologies/cellcycle_FUN.owl"); //go_daily-termdb  treeOnt
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	 	OWLOntology go = manager.loadOntologyFromOntologyDocument(file);
	 	System.out.println("Loaded ontology: " + go.getOntologyID().toString());
	 	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	 	ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
	 	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
	 	OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
	 	OWLDataFactory factory = manager.getOWLDataFactory();
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
			br = new BufferedReader(new FileReader("E:/dataset/cellcycle_FUN.test.arff"));
			bw = new BufferedWriter(new FileWriter("E:/dataset/cellcycle_FUN_test_expanded_11.arff"));
			String line;
			int c=1;
			boolean flag=true;
			ArrayList<String> predicted_classes = new ArrayList<String>(vertex);
			l1: while((line = br.readLine())!=null)
			{
				if(!line.isEmpty())
				{
					if(flag)
					{
						if(!line.contains("hierarchical"))
						{
							bw.write(line+"\n");
							bw.flush();
						}
						else
							flag=false;
					}
					else
					{
						for(String temp : vertex)
							bw.write("@attribute "+temp+" {0,1}\n");
						break l1;
					}
				}
			}
			String[] gene=null;
			String out="";
			String label_vec="";
			bw.write("\n@data\n\n");
			bw.flush();
			c=0;
			int num=0;
			while((line = br.readLine())!=null)
			{
				if(!line.isEmpty()&&!line.contains("data")&&!line.contains("DATA"))
				{
					//System.out.println(line+"\n");
					System.out.println("\n--------------------\nTaining example: "+(++c)+"\n-----------------");
					out="";
					label_vec="";
					line=line.replaceAll("/", ".");
					String[] part=line.split(",");
					String gene_l=part[part.length-1];
					out=line.substring(0,line.length()-1-gene_l.length());
					System.out.println(gene_l);
					gene=null;
					if(gene_l.contains("@"))
						gene=gene_l.split("@");
					else
					{
						gene=new String[1];
						gene[0]=gene_l;
					}
					ArrayList<Double> vec=new ArrayList<Double>();
					for(String temp : predicted_classes)
					{
						vec.add(new Double(0));
					}
					boolean f=false;
					int x=0;
					for(String temp : gene)
					{
						System.out.println("Temp> "+gene.length);
						int y=predicted_classes.indexOf(temp);
						vec.set(predicted_classes.indexOf(temp),new Double(1));
						
					}
					label_vec=",";
					//System.out.println();
					for(double d : vec)
					{
						if(d==1)
						{
							System.out.println("d>>"+d);
							num++;
						}
						label_vec+=(int)d+",";
					}
					System.out.println("-----------");
					vec.clear();
					label_vec=label_vec.substring(0,label_vec.length()-1);
					//System.out.println(label_vec);
					bw.write(out+label_vec+"\n");
					bw.flush();
					new ObjectOutputStream(new FileOutputStream("n_labels")).writeObject(vertex.size());
				}
			}
			System.out.println("Num== "+num);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static float computeCost(int i,int j,String a_path)
	{
		float w=0;
		int prev=i;
		for(int k=0;k < a_path.length();k++)
		{
			if(Character.isDigit(a_path.charAt(k)))
			{
				String num = "";
				int l;
				for(l=k;a_path.charAt(l)!=' ';l++)
				{
					num+=a_path.charAt(l)+"";
				}
				k+=l-1;
				int idx=Integer.parseInt(num);
				if(is_a[prev][idx]==1)
				{
					w+=1;
				}
				else if(part_of[prev][idx]==1)
				{
					w+=2;
				}
				prev=idx;
			}
		}
		if(is_a[prev][j]==1)
		{
			w+=1;
		}
		else if(part_of[prev][j]==1)
		{
			w+=2;
		}
		return w;
	}
	public static String GetPath (int i, int j) {
	    if (dis[i][j] == 99999) 
	    	return "no path";
	    int intermediate = path[i][j];
	    if (intermediate < 0) return " ";
	    return GetPath(i,intermediate) + intermediate + GetPath(intermediate,j);
	}
}

