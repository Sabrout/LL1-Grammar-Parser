import java.util.ArrayList;

public class Rule {
	
	NonTerminal head;
	ArrayList<ArrayList<Term>> definitions = new ArrayList<ArrayList<Term>>();
	
	public Rule(NonTerminal head, ArrayList<ArrayList<Term>> definitions){
		this.head = head;
		this.definitions = definitions;
	}
	
	public Rule(NonTerminal head){
		this.head = head;
	}
	
	public String toString(){
		String temp = "";
		for (int i = 0; i < definitions.size(); i++) {
			for (int j = 0; j < definitions.get(i).size(); j++) {
				temp += definitions.get(i).get(j).toString();
			}
			if (i != definitions.size()-1) {
				temp += "|";
			}	
		}
		return "Rule: "+head.toString()+" --> "+temp;
	}
	
	public static void main(String[] args) {
		NonTerminal head = new NonTerminal("E");
		NonTerminal non = new NonTerminal("E");
		NonTerminal non2 = new NonTerminal("T");
		Terminal ter = new Terminal("+");
		ArrayList<ArrayList<Term>> body = new ArrayList<ArrayList<Term>>();
		ArrayList<Term> body1 = new ArrayList<Term>();
		ArrayList<Term> body2 = new ArrayList<Term>();
		body1.add(non);
		body1.add(ter);
		body1.add(non2);
		body2.add(non2);
		body.add(body1);
		body.add(body2);
		Rule rule = new Rule(head, body);
		System.out.println(rule.toString());
	}

}
