import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;

public class Asg_2 
{
	public static void main(String[] args) 
	{
		graph nfa = new graph(args);
		nfa.find_lambda();
		System.out.println("nfa is");
		nfa.print();
		System.out.println("********************");
		
		graph dfa =nfa.nfa_dfa(nfa);
		dfa.normalize();
		System.out.println("dfa is");
		dfa.print();
		System.out.println("********************");
		
		graph min=dfa.dfa_minimize(dfa);
		min.combine();
		System.out.println("minimized dfa is"); 
		min.print();
		System.out.println("");
		System.out.println("********************");
		
		System.out.println("input answeres from dfa are");
		dfa.calculate();
		System.out.println("********************");
		
		System.out.println("input answeres from minimized dfa are");
		min.calculate();
		System.out.println("********************");	
	}
}
