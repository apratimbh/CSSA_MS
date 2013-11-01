import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
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

public class myue_data {
public static void main(String[] args) throws OWLOntologyCreationException
{
	String file="latest";
	double[][] arr = null;
	BufferedReader pbr,br;
	int a1=0,a2=0;
	boolean flag=false;
	ArrayList<String> vertex=null;
	
	File file1 = new File("E:/ontologies/cellcycle_FUN.owl");
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
 	OWLOntology go = manager.loadOntologyFromOntologyDocument(file1);
 	System.out.println("Loaded ontology: " + go.getOntologyID().toString());
 	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
 	ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
 	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
 	OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
 	OWLDataFactory factory = manager.getOWLDataFactory();
	
	try {
		vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
		int c=0,i;
		String curr;
		pbr = new BufferedReader(new FileReader("fun_cellcycle_result.txt"));
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
		br = new BufferedReader(new FileReader("fun_cellcycle_result.txt"));
		flag=false;
		c=0;
		while ((curr = br.readLine()) != null) {
			if(!curr.isEmpty()) 
			{
				String[] part=curr.split(",");
				//System.out.println("Part Length>"+part.length+" est: "+a1);
				for(i=0;i<a2;i++)
				{
					arr[c][i]=Double.parseDouble(part[i]);
				}
				c++;
			}
		}
	} catch (Exception e) {
		e.printStackTrace();
	}

	for(int i=0;i<a2;i++)
	{
		double max=arr[0][i],min=arr[0][i];
		for(int j=0;j<a1;j++)
		{
			if(arr[j][i]>max)
				max=arr[j][i];
			if(arr[j][i]<min)
				min=arr[j][i];
		}
		if(max-min>0)
		{
			for(int j=0;j<a1;j++)
			{
				arr[j][i]=(arr[j][i]-min)/(max-min);
			}
		}
	}
	
	BufferedWriter bw;
	try {
		for(int j=0;j<a2;j++)
		{
			bw=new BufferedWriter(new FileWriter("./cellcycle_fun/data-"+j+".txt"));
			bw.write("%\n");
			for(String tmp : vertex)
			{
				OWLClass vertex_c = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+tmp));
				NodeSet<OWLClass> set=reasoner.getSubClasses(vertex_c, true);
				for(Node<OWLClass> cls : set)
				{
					if(!cls.isBottomNode())
					{
						String news=cls+"";
						String[] part=news.split(" ");
						part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
						bw.write("ordered("+tmp+","+part[1]+").\n");
					}
				}
			}
			bw.write("\n%\n");
			for(int i=0;i<arr.length;i++)
			{
				bw.write("w("+vertex.get(i)+","+arr[i][j]+").\n");
			}
			bw.write("%");
			bw.close();
		}
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
}
}
