import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class graph 
{
	//all the nodes in the graph
	private ArrayList<node> my_graph = new ArrayList<node>();
	//the letters
	private ArrayList<String> alphabet = new ArrayList<String>(); 
	//the starting state
	private String start;
	//number of states in NDA note that the number of lines in file is state_num+4
	private int state_num;
	//the input string to test
	private ArrayList<String> input = new ArrayList<String>(); 
	
	public graph()
	{
		//left empty on purpose 
	}
	
	public graph(String[] args)
	{
		String file_line;
		
		//this reads in the input file
		try 
		{
			if (args.length < 2) throw new Exception("Please provide input file.");
			BufferedReader  br = new BufferedReader(new FileReader(args[1]));
			while((file_line=br.readLine()) != null)
			{
				input.add(file_line);
			}
			br.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		
		//keep track of number of lines read
		int i=0;
		try 
		{
			if (args.length == 0) throw new Exception("Please provide a file name.");
			BufferedReader  br = new BufferedReader(new FileReader(args[0]));
			while((file_line=br.readLine()) != null)
			{
				file_line=file_line.replaceAll("\\s+","");
				//get the number of states
				if(i==0)
				{
					state_num = Integer.parseInt(file_line);
				}
				//get the alphabet
				else if(i==1)
				{
					for(int j=0; j<file_line.length();j++)
				    {
				        alphabet.add(String.valueOf(file_line.charAt(j)));	
				    }
					//the $ will be used to represent lambda 
					alphabet.add("$");
				}
				//get the node and transitions 
				else if(i<state_num+2)
				{
					//file_line=file_line.replaceAll("\\s+","");
					StringTokenizer st = new StringTokenizer(file_line, "{}:", true);
					String maybe_new_node=st.nextToken();
					add_node(maybe_new_node);
					int token_count=0;
					
					while (st.hasMoreTokens()) 
				    {
				        String temp=st.nextToken();
				        if(temp.equals("}"))
				        {
				        	token_count++;
				        }
				        //split the token into its component states 
				        StringTokenizer st_2 = new StringTokenizer(temp, ",", false);
				        while(st_2.hasMoreTokens())
				        {
				        	String temp_2=st_2.nextToken();
				        	if(!temp_2.equals("{") && !temp_2.equals("}") && !temp_2.equals(":") && !temp_2.equals(","))
				        	{
				        		int j=find_node(maybe_new_node);
				        		if(j==-1)
				        		{
				        			System.out.println("error-1");
				        			System.exit(1);
				        		}
				        		if(token_count>alphabet.size())
				        		{
				        			System.out.println("error-2");
				        			System.exit(1);
				        		}
				        		//put in the new edge
				        		my_graph.get(j).add_edge(temp_2, alphabet.get(token_count));        		
				        	}
				        }
				    }
				}
				//get the start state
				else if(i==state_num+2)
				{
					start=file_line;
				}
				//get the end states
				else if(i==state_num+3)
				{
					StringTokenizer st = new StringTokenizer(file_line, "{}:, ", false);
					int j;
					while (st.hasMoreTokens()) 
				    {
				        String temp=st.nextToken();
				        j=find_node(temp);
				        if(j==-1)
				        {
				        	System.out.println("error-3");
							System.exit(1);
				        }
				        my_graph.get(j).set_end(true);	
				    }
				    
				}
				else
				{
					//not part of NFA so do nothing
				}
				i++;
			}
			br.close();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	public graph dfa_minimize(graph dfa) 
	{
		graph min_dfa=new graph();
		//same alphabet
		min_dfa.alphabet=dfa.alphabet;
		min_dfa.input=dfa.input;
		min_dfa.my_graph=dfa.my_graph;
		min_dfa.start=dfa.start;
		boolean stop=false;
		//first step is to set all pairs of final and nonfinal to be distinct
		for(int i=0; i<my_graph.size(); i++)
		{
			//only want nodes of greater index 
			for(int j=i+1; j<my_graph.size(); j++)
			{
				if(my_graph.get(i).end_node!=my_graph.get(j).end_node)
				{
					pair temp = new pair(my_graph.get(j).name,"T");
					my_graph.get(i).distinguish.add(temp);
				}
				else
				{
					pair temp = new pair(my_graph.get(j).name,"F");
					my_graph.get(i).distinguish.add(temp);
				}
			}
		}
		while(stop==false)
		{
			stop=true;
			//look at each node in graph
			for(int i=0; i<my_graph.size(); i++)
			{
				//look at each letter in alphabet 
				for(int j=0; j<alphabet.size(); j++)
				{
					//look at each element in distinguish list
					for(int k=0; k<my_graph.get(i).distinguish.size(); k++)
					{
						//only check if it is false no reason to do it if already true
						if(my_graph.get(i).distinguish.get(k).use.equals("F"))
						{
							if(check(i, my_graph.get(i).distinguish.get(k).to, alphabet.get(j))==true)
							{
								my_graph.get(i).distinguish.get(k).use="T";
								stop=false;
							}
						}
					}
				}
			}
		}
		return min_dfa;
	}
	
	public boolean check(int name_1, String name_2, String letter)
	{
		//has index of first element, needs to use name to find the second
		int ans_1=-1;
		int ans_2=-1;
		for(int i=0; i<my_graph.get(name_1).edge.size(); i++)
		{
			//see what the delta using the letter is, note that it may not be defined
			if(my_graph.get(name_1).edge.get(i).use.equals(letter))
			{
				ans_1=find_node(my_graph.get(name_1).edge.get(i).to);
			}
		}
		
		//see what the delta using the letter is, note that it may not be defined
		int hold=find_node(name_2);
		for(int i=0; i<my_graph.get(hold).edge.size(); i++)
		{
			if(my_graph.get(hold).edge.get(i).use.equals(letter))
			{
				ans_2=find_node(my_graph.get(hold).edge.get(i).to);
			}
		}
		//if it is -1 it means the delta is not defined for this letter
		if(ans_1<0)
		{
			return false;
		}
		if(ans_2<0)
		{
			return false;
		}
		
		if(ans_1<ans_2)
		{
			for(int i=0; i<my_graph.get(ans_1).distinguish.size(); i++)
			{
				if(my_graph.get(ans_1).distinguish.get(i).to.equals(my_graph.get(ans_2).name))
				{
					return true;
				}
			}
			return false;
		}
		/* this would be used but the are of the table it covers is undefined 
		else if(ans_1>ans_2)
		{
			for(int i=0; i<my_graph.get(ans_2).distinguish.size(); i++)
			{
				if(my_graph.get(ans_2).distinguish.get(i).to.equals(my_graph.get(ans_1).name))
				{
					return true;
				}
			}
			return false;
		}
		*/
		//if they lead to the same node it is not part of the table
		else
		{
			return false;
		}
	}
	
	public void combine()
	{
		boolean stop=false;
		ArrayList<String> to_delete = new ArrayList<String>();
		while(stop==false)
		{
			stop=true;
			//loop through the nodes
			for(int i=0; i<my_graph.size(); i++)
			{
				//loop through the distinguish set
				for(int j=0; j<my_graph.get(i).distinguish.size(); j++)
				{
					//see if new rules can be added
					if(my_graph.get(i).distinguish.get(j).use.equals("F"))
					{
						boolean test=add(i, my_graph.get(i).distinguish.get(j).to);
						if(test==true)
						{
							stop=false;
						}
						//need to delete this node
						if(!(to_delete.contains(my_graph.get(i).distinguish.get(j).to)))
						{
							to_delete.add(my_graph.get(i).distinguish.get(j).to);
							//if start node will be deleted make this new start
							if(my_graph.get(i).distinguish.get(j).to.equals(start))
							{
								start=my_graph.get(i).name;
							}
							//if this is end node make combined node end node, not sure if this is ever hit, it probably is not
							if(my_graph.get(find_node(my_graph.get(i).distinguish.get(j).to)).end_node==true)
							{
								my_graph.get(i).end_node=true;
							}
							//all rules that said to go to my_graph.get(i).distinguish.get(j) now go to my_graph.get(i)
							for(int k=0; k<my_graph.size(); k++)
							{
								for(int l=0; l<my_graph.get(k).edge.size(); l++)
								{
									if(my_graph.get(k).edge.get(l).to.equals(my_graph.get(i).distinguish.get(j).to))
									{
										my_graph.get(k).edge.get(l).to=my_graph.get(i).name;
									}
								}
							}
						}
					}
				}
			}
		}
		//delete nodes that are not used
		for(int i=0; i<to_delete.size(); i++)
		{
			for(int j=0; j<my_graph.size(); j++)
			{
				if(my_graph.get(j).name.equals(to_delete.get(i)))
				{
					my_graph.remove(j);
				}
			}
			my_graph.remove(to_delete.get(i));
		}
	}
	
	public boolean add(int added_to, String node_to_add)
	{
		int index_to_add = find_node(node_to_add);
		boolean ans=false;
		for(int i=0; i<my_graph.get(index_to_add).edge.size(); i++)
		{
			boolean found_new=true;
			for(int j=0; j<my_graph.get(added_to).edge.size(); j++)
			{
				if(my_graph.get(added_to).edge.get(j).to.equals(my_graph.get(index_to_add).edge.get(i).to)
				   && my_graph.get(added_to).edge.get(j).use.equals(my_graph.get(index_to_add).edge.get(i).use))
				{
					found_new=false;
				}
			}
			if(found_new==true)
			{
				my_graph.get(added_to).edge.add(my_graph.get(index_to_add).edge.get(i));
				ans=true;
			}
		}
		return ans;
	}
	
	//used for debugging prints the table
	public void print_min()
	{
		for(int i=0; i<my_graph.size(); i++)
		{
			for(int j=0; j<my_graph.get(i).distinguish.size(); j++)
			{
				my_graph.get(i).distinguish.get(j).print();
			}
			System.out.println("");
		}
	}
	
	//done to print out the dfa the right way
	public void normalize()
	{
		for(int i=0; i<my_graph.size(); i++)
		{
			String temp = my_graph.get(i).name;
			//save what start should be if start node is changed
			if(start.equals(temp))
			{
				start=Integer.toString(i);
			}
			//remake names, use the position they are in the graph at
			my_graph.get(i).name=Integer.toString(i);
			//find all instances of temp in the rules and update it to the new name
			for(int j=0; j<my_graph.size(); j++)
			{
				for(int k=0; k<my_graph.get(j).edge.size(); k++)
				{
					if(my_graph.get(j).edge.get(k).get_to().equals(temp))
					{
						my_graph.get(j).edge.get(k).to=Integer.toString(i);
					}
				}
			}
		}
	}
	
	public graph nfa_dfa(graph nfa)
	{
		graph dfa=new graph();
		//same alphabet
		dfa.alphabet=nfa.alphabet;
		dfa.input=nfa.input;
		//this makes the first state of the dfa
		String p0="";
		boolean is_end=false;
		for(int i=0; i<nfa.my_graph.get((nfa.find_node(nfa.start))).lambda_closer.size(); i++)
		{
			p0+=(nfa.my_graph.get((nfa.find_node(nfa.start))).lambda_closer.get(i).name);
			if(nfa.my_graph.get((nfa.find_node(nfa.start))).lambda_closer.get(i).end_node==true)
			{
				is_end=true;
			}
			if(i<nfa.my_graph.get((nfa.find_node(nfa.start))).lambda_closer.size()-1)
			{
				p0+=",";
			}
		}
		dfa.add_node(p0);
		if(is_end==true)
		{
			dfa.my_graph.get(dfa.my_graph.size()-1).set_end(true);
		}
		dfa.start=dfa.my_graph.get(0).name;
		is_end=false;
		
		//go through the alphabet but do not use lambda which is the last one
		for(int i=0; i<nfa.alphabet.size()-1; i++)
		{
			//go through all nodes in the dfa 
			for(int j=0; j<dfa.my_graph.size(); j++)
			{
				//get each part of the DFA node 
				StringTokenizer st = new StringTokenizer(dfa.my_graph.get(j).name, ",", false);
				//works to get each element of dfa
				String new_node="";
				
				while (st.hasMoreTokens()) 
			    {
					//this is the nfa node form the dfa node element
					int index=find_node(st.nextToken());
					if(index!=-1)
					{
						node temp=nfa.my_graph.get((index));
						for(int k=0; k<temp.lambda_closer.size(); k++)
						{							
							//look at the lambda closer of all the element nodes
							node temp_2=temp.lambda_closer.get(k);
							for(int l=0; l<temp_2.edge.size(); l++)
							{
								//found element of where this edge will go
								if(temp_2.edge.get(l).get_use().equals(nfa.alphabet.get(i)))
								{
									String test_node=temp_2.edge.get(l).get_to();
									if(new_node.indexOf(test_node)<0)
									{
										new_node+=test_node+",";
									}
								}
							}
						}
					}	
			    }
				//add the new node if it exists
				if(!new_node.isEmpty())
				{
					//string will have a , at the end, this gets rid of it
					new_node=(new_node.substring(0,new_node.length()-1));
					if(!new_node.isEmpty())
					{
						dfa.add_node(new_node);
						if(dfa.my_graph.get(j).find_edge(new_node, nfa.alphabet.get(i))==-1)
						{
							//edge does not exist so make it
							dfa.my_graph.get(j).add_edge(new_node, nfa.alphabet.get(i));
						}
					}
				}
			}
		}
		//set nodes to be accepting states if they should be
		for(int i=0; i<dfa.my_graph.size(); i++)
		{
			is_end=false;
			StringTokenizer st = new StringTokenizer(dfa.my_graph.get(i).name, ",", false);
			
			while (st.hasMoreTokens()) 
		    {
				int index=find_node(st.nextToken());
				if(index!=-1)
				{
					node temp=nfa.my_graph.get((index));
					if(temp.end_node==true)
					{
						is_end=true;
					}
				}
		    }
			if(is_end==true)
			{
				dfa.my_graph.get(i).set_end(true);
			}
		}
		return dfa;
	}
	
	public void find_lambda()
	{
		for(int i=0; i<my_graph.size(); i++)
		{
			my_graph.get(i).find_lambda(my_graph.get(i),i);
		}
	}
	
	//this is used to print the lambda closers, it was used to help debug
	public void lambda_print()
	{
		System.out.println("");
		for(int i=0; i<my_graph.size(); i++)
		{
			System.out.println("");
			System.out.print(my_graph.get(i).name+": ");
			for(int j=0; j<my_graph.get(i).lambda_closer.size(); j++)
			{
				System.out.print(my_graph.get(i).lambda_closer.get(j).name+" ");
			}
		}
	}
	
	//add a node if the node does not already exist 
	public void add_node(String name)
	{
		if(find_node(name)==-1)
		{
			my_graph.add(new node(name));
		}
	}
	
	//see if a node is in the graph and get its index, or return -1 if it does not exist
	public int find_node(String name)
	{
		for(int i=0; i<my_graph.size(); i++)
		{
			if(name.equals(my_graph.get(i).name))
			{
				return i;
			}
		}
		return -1;
	}
	
	//add edge if it does not exist
	public void add_edge(String from, String go, String letter)
	{
		int i=find_node(from);
		//the first node in the edge is not in the graph
		if(i==-1)
		{
			add_node(from);
			my_graph.get(my_graph.size()-1).add_edge(go, letter);
		}
		else
		{
			my_graph.get(i).add_edge(go, letter);
		}
	}
	
	public void print()
	{
		System.out.print("Sigma: ");
		for(int i=0; i<alphabet.size(); i++)
		{
			System.out.print(alphabet.get(i)+" ");
		}
		System.out.println("");
		
		for(int i=0; i<my_graph.size(); i++)
		{
			my_graph.get(i).print();
			System.out.println("");
		}
		System.out.println("S: "+start);
		System.out.print("A: ");
		for(int i=0; i<my_graph.size(); i++)
		{
			if(my_graph.get(i).end_node==true)
			{
				System.out.print("{"+my_graph.get(i).name+"} ");
			}
		}
		System.out.println("");
	}
	
	//see if a string should be accepted or not
	public void calculate()
	{
		//go through all inputs
		for(int i=0; i<input.size(); i++)
		{
			String test=input.get(i);
			node current=my_graph.get(find_node(start));
			boolean skip=false;
			//loop through each char of the input string
			for(int j=0; j<test.length(); j++)
			{
				char check=test.charAt(j);
				int go_to=-1;
				for(int k=0; k<current.edge.size(); k++)
				{
					//found edge to go to
					if(current.edge.get(k).get_use().equals(String.valueOf(check)))
					{
						go_to=find_node(current.edge.get(k).get_to());
						current=my_graph.get(go_to);
					}
				}
				//did not find edge so it will fail
				if(go_to==-1)
				{
					System.out.println(test+" is rejected");
					skip=true;
					break; 
				}
			}
			//was accepted
			if(current.end_node==true && skip==false)
			{
				System.out.println(test+" is accepted");
			}
			//did not stop in a accepting state, so it is not accepted
			else if(skip==false)
			{
				System.out.println(test+" is rejected");
			}
			
		}
		
	}
	
	private class node
	{
		//all the edges the node has
		private ArrayList<pair> edge = new ArrayList<pair>();
		private ArrayList<node> lambda_closer  = new ArrayList<node>();
		private ArrayList<String> seen  = new ArrayList<String>();
		private ArrayList<pair>  distinguish = new ArrayList<pair>(); 
		private String name;
		//to tell if this is a accepting node or not
		boolean end_node=false;
		
		public node(String input)
		{
			name=input;
			//a node is always in its own lambda closer 
			lambda_closer.add(this);
		}
		//see if a node is already in lambda closer
		public boolean in_lambda(String test)
		{
			for(int i=0; i<lambda_closer.size(); i++)
			{
				if(test.equals(lambda_closer.get(i).name))
				{
					return true;
				}
			}
			return false;
		}
		//finds the lambda closer of a node
		public void find_lambda(node working_node, int j)
		{
			//have seen this node,, so do not come back to it
			my_graph.get(j).seen.add(working_node.name);
			for(int i=0; i<working_node.edge.size(); i++)
			{
				if(working_node.edge.get(i).use.equals("$"))
				{
					if(!my_graph.get(j).in_lambda(working_node.edge.get(i).to))
					{
						//found a new element to add to lambda closer
						my_graph.get(j).lambda_closer.add(my_graph.get(find_node(working_node.edge.get(i).to)));
					}
				}
			}
			for(int i=0; i<working_node.lambda_closer.size(); i++)
			{
				if(!working_node.seen.contains(working_node.lambda_closer.get(i).name))
				{
					//find the lamda closer of eveything that has not been seen
					find_lambda(my_graph.get(j).lambda_closer.get(i), j);
				}
			}
		}
		
		public void add_edge(String go, String letter)
		{
			//the node this edge goes to does not exist so make it
			if(find_node(go)!=-1)
			{
				add_node(go);
			}
			//did not find this edge so make it
			if(find_edge(go, letter)==-1)
			{
				edge.add(new pair(go, letter));
			}
		}
		
		//get the index of the edge or return -1 if it does not exist
		public int find_edge(String go, String letter)
		{
			for(int i=0; i<edge.size(); i++)
			{
				if(letter.equals(edge.get(i).get_use()) && go.equals(edge.get(i).get_use()))
				{
					return i;
				}
			}
			return -1;
		}
		
		//make this node accepting
		public void set_end(boolean s)
		{
			end_node=s;
		}
		
		public void print()
		{
			System.out.print(name+":  ");
			System.out.print(end_node+":  ");
			for(int i=0; i<edge.size(); i++)
			{
				//System.out.print(name);
				edge.get(i).print();
			}
		}
	}
	
	//this is a small class to make the edges simpler to work with
	private class pair
	{
		//the node that the edge goes to 
		private String to;
		//the letter that is needed to get to that node
		private String use;
		
		public pair(String go, String letter)
		{
			to=go;
			use=letter;
		}
		
		public String get_to()
		{
			return to;
		}
		
		public String get_use()
		{
			return use;
		}
		
		public void print()
		{
			System.out.print("("+use+",{"+to+"}) ");
		}
	}
}
