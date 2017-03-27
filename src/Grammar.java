import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Grammar {

	HashSet<String> StringTerminals = new HashSet<String>();
	ArrayList<String> StringNonTerminals = new ArrayList<String>();
	ArrayList<String> StringDefinitions = new ArrayList<String>();

	ArrayList<Terminal> terminals = new ArrayList<Terminal>();
	ArrayList<NonTerminal> nonTerminals = new ArrayList<NonTerminal>();
	ArrayList<Rule> rules = new ArrayList<Rule>();

	HashMap<ArrayList<Term>, Integer> freq = new HashMap<ArrayList<Term>, Integer>();

	ArrayList<String> allFirstsAndFollows = new ArrayList<String>();
	Terminal sign = new Terminal("$");

	// Normal Constructor for text files with only rules
	public Grammar(String FILENAME) {
		ArrayList<String> file = getString(FILENAME);
		// Reading nonTermainals and their definitions
		for (int i = 0; i < file.size(); i++) {
			if (i % 2 == 0) {
				StringNonTerminals.add(file.get(i));
				// Creating NonTerminal Objects
				nonTerminals.add(new NonTerminal(file.get(i)));
			} else {
				StringDefinitions.add(file.get(i));
			}
		}

		// Extracting terminals from definitions
		for (int i = 0; i < StringDefinitions.size(); i++) {
			String[] barSplit = StringDefinitions.get(i).split("\\|");
			extractTerminals(barSplit);
		}

		// Creating Terminal Objects
		for (String s : StringTerminals) {
			if (s.equals("!")) {
				continue;
			}
			terminals.add(new Terminal(s));
		}
		terminals.add(new Terminal("!"));

		// Creating Rules
		for (int i = 0; i < StringDefinitions.size(); i++) {
			createRule(nonTerminals.get(i), StringDefinitions.get(i));
		}
	}

	// Special Constructor for text files with Terminals and NonTerminals
	// headers
	public Grammar(String FILENAME, boolean isSpecial) {
		ArrayList<String> file = getString(FILENAME);
		// Reading NonTerminals
		String[] strNonTerminals = file.get(0).split(",");
		for (int i = 0; i < strNonTerminals.length; i++) {
			nonTerminals.add(new NonTerminal(strNonTerminals[i]));
		}
		// Reading Terminals
		String[] strTerminals = file.get(1).split(",");
		for (int i = 0; i < strTerminals.length; i++) {
			terminals.add(new Terminal(strTerminals[i]));
		}
		terminals.add(new Terminal("!"));

		for (int i = 2; i < file.size(); i++) {
			NonTerminal newHead = null;
			ArrayList<ArrayList<Term>> newDef = new ArrayList<ArrayList<Term>>();
			if (i % 2 == 0) {
				newHead = (NonTerminal) findTerm(file.get(i));
				String[] productions = file.get(i + 1).split("\\|");
				for (int j = 0; j < productions.length; j++) {
					int s = 0; // Start index
					ArrayList<Term> production = new ArrayList<Term>();
					int originalLen = productions[j].length();
					for (int k = productions[j].length(); k > 0; k--) {

						// System.out.println("S="+s+", K="+k);
						if (k == s)
							break; // Finished with the production
						if (findTerm(productions[j].substring(s, k)) != null) {
							// System.out.println("FOUND:
							// "+productions[j].substring(s, k));
							production.add(findTerm(productions[j].substring(s, k)));
							s = k;
							k = originalLen + 1;
						}
					}
					newDef.add(production);
					// System.out.println("Production: "+production.toString());
					// System.out.println("----");
				}
				// System.out.println(newHead);
				rules.add(new Rule(newHead, newDef));
			}
		}
	}

	public void createRule(NonTerminal head, String def) {
		ArrayList<ArrayList<Term>> body = new ArrayList<ArrayList<Term>>();
		String[] defs = def.split("\\|");
		for (int k = 0; k < defs.length; k++) {
			ArrayList<Term> Nbody = new ArrayList<Term>();
			for (int i = 0; i < defs[k].length(); i++) {
				for (int j = i; j < defs[k].length(); j++) {
					Term temp = findTerm(defs[k].substring(i, j + 1));
					if (temp != null) {
						Nbody.add(temp);
						i = j;
						break;
					}
				}
			}
			body.add(Nbody);
		}
		Rule rule = new Rule(head, body);
		rules.add(rule);
	}

	public Term findTerm(String name) {
		for (int i = 0; i < terminals.size(); i++) {
			if (terminals.get(i).value.equals(name)) {
				return terminals.get(i);
			}
		}
		for (int i = 0; i < nonTerminals.size(); i++) {
			if (nonTerminals.get(i).value.equals(name)) {
				return nonTerminals.get(i);
			}
		}
		// System.out.println("TERM NOT FOUND");
		return null;
	}

	public Rule findRule(NonTerminal nonTerminal) {
		for (int i = 0; i < rules.size(); i++) {
			if (rules.get(i).head.equals(nonTerminal)) {
				return rules.get(i);
			}
		}
		// System.out.println("RULE NOT FOUND");
		return null;
	}

	public void replaceTerms(Rule prevRule, Rule rule) {
		for (int i = 0; i < rule.definitions.size(); i++) {
			if (rule.definitions.get(i).get(0).equals(prevRule.head)) {
				rule.definitions.get(i).remove(0);
				@SuppressWarnings("unchecked")
				ArrayList<Term> temp = (ArrayList<Term>) rule.definitions.get(i).clone();
				rule.definitions.get(i).addAll(0, prevRule.definitions.get(0));
				int tempIndex = i;
				for (int j = 1; j < prevRule.definitions.size(); j++) {
					ArrayList<Term> tempDefinition = new ArrayList<Term>();
					tempDefinition.addAll(0, prevRule.definitions.get(j));
					tempDefinition.addAll(temp);
					rule.definitions.add(tempIndex + 1, tempDefinition);
					tempIndex++;
				}
			}
		}
		// System.out.println("Test: "+rule);
	}

	public void extractTerminals(String[] barSplit) {
		for (int i = 0; i < barSplit.length; i++) {
			String tempBarSplit = barSplit[i];
			for (int j = 0; j < StringNonTerminals.size(); j++) {
				tempBarSplit = tempBarSplit.replaceAll(StringNonTerminals.get(j), " ");
			}
			// System.out.println("tempBarSplit: "+tempBarSplit);
			String[] terminalString = tempBarSplit.split(" ");
			for (int j = 0; j < terminalString.length; j++) {
				if (!terminalString[j].equals("")) {
					for (int j2 = 0; j2 < terminalString[j].length(); j2++) {
						StringTerminals.add(terminalString[j].charAt(j2) + "");
					}
				}
			}
		}

	}

	public static ArrayList<String> getString(String FILENAME) {

		ArrayList<String> result = new ArrayList<String>();
		BufferedReader br = null;
		FileReader fr = null;

		try {

			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			br = new BufferedReader(new FileReader(FILENAME));

			while ((sCurrentLine = br.readLine()) != null) {
				result.add(sCurrentLine);
				// System.out.println(sCurrentLine);
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
		return result;
	}

	public void eliminateRecursion() {
		for (int i = 0; i < rules.size(); i++) {
			for (int j = 0; j < i; j++) {
				replaceTerms(rules.get(j), rules.get(i));
			}
			// eliminateImmediateRecursion(rules.get(i));
		}
		for (int i = 0; i < rules.size(); i++) {
			eliminateImmediateRecursion(rules.get(i));
		}
	}

	public void eliminateImmediateRecursion(Rule rule) {
		ArrayList<ArrayList<Term>> newNonTerminal = new ArrayList<ArrayList<Term>>();
		for (int i = 0; i < rule.definitions.size(); i++) {
			if (rule.definitions.get(i).get(0).equals(rule.head)) {
				System.out.println("DEBUG " + i + ": " + rule.definitions.get(i));
				rule.definitions.get(i).remove(0);
				newNonTerminal.add(rule.definitions.get(i));
				rule.definitions.remove(i);
				i--;
			}
		}
		if (!newNonTerminal.isEmpty()) {
			NonTerminal head = new NonTerminal(rule.head.value + "'");

			// Update Original NonTerminal
			for (int i = 0; i < rule.definitions.size(); i++) {
				rule.definitions.get(i).add(head);
			}

			// Update New NonTerminal
			for (int i = 0; i < newNonTerminal.size(); i++) {
				newNonTerminal.get(i).add(head);
			}
			ArrayList<Term> epsilon = new ArrayList<Term>();
			epsilon.add(findTerm("!"));
			newNonTerminal.add(epsilon);

			// Create Rule
			Rule newRule = new Rule(head, newNonTerminal);
			rules.add(newRule);
		}
	}

	public boolean isPrefix(ArrayList<Term> LHS, ArrayList<Term> RHS) {
		if (RHS.size() >= LHS.size()) {
			for (int i = 0; i < LHS.size(); i++) {
				if (!LHS.get(i).value.equals(RHS.get(i).value)) {
					return false;
				}
				// System.out.println("-----LHS: "+LHS.get(i).value+" RHS:
				// "+RHS.get(i).value);
			}
			return true;
		} else {
			return false;
		}
	}

	public void leftFactor() {
		for (int i = 0; i < rules.size(); i++) {
			leftFactorOneRule(i);
			freq.clear();
		}
	}

	public void leftFactorOneRule(int i) {

		// for (int i = 0; i < rules.size(); i++) {
		factorRule(rules.get(i));
		// }

		int maxFreq = 0;
		int maxLength = 0;
		// Get Prefix with highest frequency
		ArrayList<ArrayList<Term>> factorPrefix = new ArrayList<ArrayList<Term>>();
		for (Map.Entry<ArrayList<Term>, Integer> entry : freq.entrySet()) {
			Integer value = entry.getValue();
			if (value >= maxFreq) {
				maxFreq = value;
			}
		}
		// if there are no common prefixes
		if (maxFreq == 1) {
			return;
		}
		// Get Prefix with longest string
		for (Map.Entry<ArrayList<Term>, Integer> entry : freq.entrySet()) {
			ArrayList<Term> key = entry.getKey();
			Integer value = entry.getValue();
			if (value == maxFreq) {
				if (key.size() >= maxLength) {
					maxLength = key.size();
				}
			}
		}
		// Get Prefixes with assigned max frequency and length
		for (Map.Entry<ArrayList<Term>, Integer> entry : freq.entrySet()) {
			ArrayList<Term> key = entry.getKey();
			Integer value = entry.getValue();
			if (value == maxFreq) {
				if (key.size() == maxLength) {
					factorPrefix.add(key);
				}
			}
		}
		System.out.println("Factor Prefix: " + factorPrefix.toString());

		// Replace Prefixes
		// for (int i = 0; i < rules.size(); i++) {
		String newName = rules.get(i).head + "'";
		NonTerminal newHead = new NonTerminal(newName);
		Rule newRule = new Rule(newHead);
		for (int j = 0; j < rules.get(i).definitions.size(); j++) {
			// Adding productions to the new Rule
			for (int k = 0; k < factorPrefix.size(); k++) {
				// if
				// (rules.get(i).definitions.get(j).containsAll(factorPrefix.get(k)))
				// {
				if (isPrefix(factorPrefix.get(k), rules.get(i).definitions.get(j))) {
					System.out.println("FOUND: " + factorPrefix.get(k).toString() + ", IN PRODUCTION: "
							+ rules.get(i).definitions.get(j).toString());
					if (rules.get(i).definitions.get(j).size() == factorPrefix.get(k).size()) {
						ArrayList<Term> epsilon = new ArrayList<Term>();
						epsilon.add(new Terminal("!"));
						newRule.definitions.add(epsilon);
					} else {
						ArrayList<Term> tempProduction = new ArrayList<Term>();
						for (int l = factorPrefix.get(k).size(); l < rules.get(i).definitions.get(j).size(); l++) {
							tempProduction.add(rules.get(i).definitions.get(j).get(l));
						}
						// Add production to the new Rule
						if (!newRule.definitions.contains(tempProduction)) {
							newRule.definitions.add(tempProduction);
						}
						// Add the new Rule in the old productions
						rules.get(i).definitions.get(j).add(newHead);
					}
				}
			}
		}
		// Adding the new Rule to the Grammar
		if (!newRule.definitions.isEmpty()) {
			rules.add(newRule);
			nonTerminals.add(newRule.head);
		}
		// Removing Terms from the original Rule
		ArrayList<ArrayList<Term>> newProductions = new ArrayList<ArrayList<Term>>();
		// System.out.println("ALL: "+rules.get(i).definitions.toString());
		for (int j = 0; j < rules.get(i).definitions.size(); j++) {
			for (int k = 0; k < factorPrefix.size(); k++) {
				if (j == -1)
					j++;
				System.out
						.println("LOOP: " + rules.get(i).definitions.get(j) + " and " + factorPrefix.get(k).toString());
				// if
				// (rules.get(i).definitions.get(j).containsAll(factorPrefix.get(k)))
				// {
				if (isPrefix(factorPrefix.get(k), rules.get(i).definitions.get(j))) {
					System.out.println("DELETE: " + rules.get(i).definitions.get(j));
					rules.get(i).definitions.remove(j);
					j--;
					if (!newProductions.contains(factorPrefix.get(k))) {
						newProductions.add(factorPrefix.get(k));
					}
					break;
				}
			}
		}
		// Adding new Productions to the original Rule
		System.out.println("newProductions: " + newProductions.toString());
		for (int j = 0; j < newProductions.size() && !newProductions.isEmpty(); j++) {
			newProductions.get(j).add(newHead);
			rules.get(i).definitions.add(0, newProductions.get(j));
		}

		// Printing freq
		System.out.println("Frequencies in Rule #" + (i + 1) + " are : " + freq.toString());
		// }
		// Clearing Frequencies
		freq.clear();
		// Recursion for Left Factoring until no common prefixes
		leftFactorOneRule(i);
	}

	public void first() {
		ArrayList<String> output = new ArrayList<String>();
		for (int i = 0; i < terminals.size(); i++) {
			if (terminals.get(i).value.equals("!")) {
				continue;
			}
			output.add("First(" + terminals.get(i).toString() + "): " + firstOneTerm(terminals.get(i)));
		}
		for (int i = 0; i < nonTerminals.size(); i++) {
			output.add("First(" + nonTerminals.get(i).toString() + "): " + firstOneTerm(nonTerminals.get(i)));
		}

		// Printing
		for (int i = 0; i < output.size(); i++) {
			System.out.println(output.get(i));
		}

		// Writing File
		allFirstsAndFollows.addAll(output);
	}

	public HashSet<Term> firstOneTerm(Term term) {
		HashSet<Term> output = new HashSet<Term>();
		if (term instanceof Terminal) {
			output.add(term);
			// System.out.println("Terminal: "+term.toString()+" First:
			// "+output.toString());
			return output;
		}
		if (term instanceof NonTerminal) {
			Rule rule = findRule((NonTerminal) term);
			for (int i = 0; i < rule.definitions.size(); i++) {
				// System.out.println("------------Production:
				// "+rule.definitions.get(i).toString());

				for (int j = 0; j < rule.definitions.get(i).size(); j++) {
					// System.out.println("Term:
					// "+rule.definitions.get(i).get(j).toString());
					if (!rule.definitions.get(i).get(j).equals(term)) {
						HashSet<Term> temp = firstOneTerm(rule.definitions.get(i).get(j));
						output.addAll(temp);
						// System.out.println("First: "+output.toString());
						boolean flag = false;
						for (Term t : temp) {
							if (t.value.equals("!")) {
								flag = true;
							}
						}
						if (!flag) {
							// System.out.println("break");
							break;
						}
					}
				}
			}
			return output;
		}
		return null;
	}
	
	public HashSet<Term> firstOfSet(ArrayList<Term> set){
		HashSet<Term> output = new HashSet<Term>();
		for (int i = 0; i < set.size(); i++) {
			HashSet<Term> temp = firstOneTerm(set.get(i));
			output.addAll(temp);
			boolean flag = false;
			for (Term t : temp) {
				if (t.value.equals("!")) {
					flag = true;
				}
			}
			if (!flag) {
				// System.out.println("break");
				break;
			}
		}
		return output;
	}

	public void follow() {
		ArrayList<String> output = new ArrayList<String>();
		for (int i = 0; i < nonTerminals.size(); i++) {
			output.add("Follow(" + nonTerminals.get(i).toString() + "): " + followOneTerm(nonTerminals.get(i)));
		}

		// Printing
		for (int i = 0; i < output.size(); i++) {
			System.out.println(output.get(i));
		}

		// Writing File
		allFirstsAndFollows.addAll(output);
	}
	
	public HashSet<Term> followOneTerm(NonTerminal term) {
		HashSet<Term> output = new HashSet<Term>();
		ArrayList<ArrayList<Term>> followings = new ArrayList<ArrayList<Term>>();
		ArrayList<Integer> followingsRule = new ArrayList<Integer>();
		if (term.value.equals(nonTerminals.get(0).value)) {
			output.add(sign);
		}
		for (int i = 0; i < rules.size(); i++) {
			for (int j = 0; j < rules.get(i).definitions.size(); j++) {
				ArrayList<Term> tmp = new ArrayList<Term>();
				for (int k = 0; k < rules.get(i).definitions.get(j).size(); k++) {
					
					// If Term is not the last term
					if (rules.get(i).definitions.get(j).get(k).equals(term)
							&& (k + 1) < rules.get(i).definitions.get(j).size()) {

						tmp = subArrayList(rules.get(i).definitions.get(j), k + 1,
								rules.get(i).definitions.get(j).size() - 1);
						followings.add(tmp);
						followingsRule.add(i);
					}
					
					// If Term is the last term
					if (rules.get(i).definitions.get(j).get(k).equals(term)
							&& k == rules.get(i).definitions.get(j).size() - 1) {
						followings.add(tmp);
						followingsRule.add(i);
					}
				}
			}
		}
		
		for (int i = 0; i < followings.size(); i++) {
			if (followings.get(i).isEmpty()) {
				// If there are no followings
//				System.out.println("EMPTY");
				if (!rules.get(followingsRule.get(i)).head.value.equals(term.value)) {
					output.addAll(followOneTerm(rules.get(followingsRule.get(i)).head));
				}
			} else {
				HashSet<Term> temp = firstOfSet(followings.get(i));
				temp.remove(new Terminal("!"));
				
				// Detect if the first of the followings contains !
				boolean flag = false;
				for (Term t: temp) {
					if (t.value.equals("!")) {
						flag = true;
					}
				}
				if (flag && !rules.get(followingsRule.get(i)).head.value.equals(term.value)) {
					output.addAll(followOneTerm(rules.get(followingsRule.get(i)).head));
				}
				output.addAll(temp);
			}
		}
		
		// Remove !
		ArrayList<Term> dummyFilter = new ArrayList<Term>(output);
		for (int i = 0; i < dummyFilter.size(); i++) {
			if (dummyFilter.get(i).value.equals("!")) {
				dummyFilter.remove(i);
				if (i<0) {
					i--;
				}
			}
		}
		output = new HashSet<Term>(dummyFilter);
		return output;
	}

	public ArrayList<Term> subArrayList(ArrayList<Term> array, int from, int to) {
		ArrayList<Term> output = new ArrayList<Term>();
		for (int i = 0; i < array.size(); i++) {
			if (i >= from && i <= to) {
				output.add(array.get(i));
			}
		}
		return output;
	}

	public void factorRule(Rule rule) {
		for (int i = 0; i < rule.definitions.size(); i++) {
			if (rule.definitions.get(i).get(0).value.equals("!")) {
				continue;
			}
			for (int j = 1; j < rule.definitions.get(i).size() + 1; j++) {
				ArrayList<ArrayList<Term>> onePrefix = new ArrayList<ArrayList<Term>>();
				onePrefix.add(new ArrayList<Term>(rule.definitions.get(i).subList(0, j)));
				// System.out.println("RULE("+rule.head+"): ONE FUCKIN' PREFIX:
				// "+onePrefix.toString());
				frequencyCount(onePrefix);
				// System.out.println(freq);
			}
		}
	}

	public void frequencyCount(ArrayList<ArrayList<Term>> rule) {

		for (int i = 0; i < rule.size(); i++) {
			ArrayList<Term> c = rule.get(i);
			Integer val = freq.get(c);
			if (val != null) {
				freq.put(c, new Integer(val + 1));
			} else {
				freq.put(c, 1);
			}
		}
	}

	public void writeOutput(String fileName) {
		BufferedWriter writer = null;
		try {
			File File = new File(fileName);
			// System.out.println(File.getCanonicalPath());

			writer = new BufferedWriter(new FileWriter(File));

			// Writing Space

			for (int i = 0; i < rules.size(); i++) {
				writer.write(rules.get(i).head.value + "->[");

				for (int j = 0; j < rules.get(i).definitions.size(); j++) {
					if (j != 0) {
						writer.write(", ");
					}
					for (int k = 0; k < rules.get(i).definitions.get(j).size(); k++) {
						writer.write(rules.get(i).definitions.get(j).get(k).value);
					}
				}
				writer.write("]");
				writer.newLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public void writeOutput(String fileName, ArrayList<String> input) {
		BufferedWriter writer = null;
		try {
			File File = new File(fileName);
			// System.out.println(File.getCanonicalPath());

			writer = new BufferedWriter(new FileWriter(File));

			// Writing Space
			for (int i = 0; i < input.size(); i++) {
				writer.write(input.get(i));
				writer.newLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// Close the writer regardless of what happens...
				writer.close();
			} catch (Exception e) {
			}
		}
	}

	public static void main(String[] args) {

		 // Sample #1
		
		 Grammar sample1 = new Grammar("Sample.in");
		 System.out.println("String Definition: " +
		 sample1.StringDefinitions.toString());
		 System.out.println("Terminals :" + sample1.terminals.toString());
		 System.out.println("NonTerminals :" +
		 sample1.nonTerminals.toString());
		
		 // sample1.eliminateRecursion();
		 sample1.leftFactor();
		
		 for (int i = 0; i < sample1.rules.size(); i++) {
		 System.out.println(sample1.rules.get(i).toString());
		 }
		 System.out.println("NumberOfRules: " + sample1.rules.size());
		
		 sample1.writeOutput("Sample.out");
		 System.out.println("-------------------------------------------");
		
		 // Sample #2
		
		 Grammar sample2 = new Grammar("Sample2.in");
		
		 System.out.println("String Definition: " +
		 sample2.StringDefinitions.toString());
		 System.out.println("Terminals :" + sample2.terminals.toString());
		 System.out.println("NonTerminals :" +
		 sample2.nonTerminals.toString());
		
		 // sample2.eliminateRecursion();
		 sample2.leftFactor();
		
		 for (int i = 0; i < sample2.rules.size(); i++) {
		 System.out.println(sample2.rules.get(i).toString());
		 }
		 System.out.println("NumberOfRules: " + sample2.rules.size());
		
		 sample2.writeOutput("Sample2.out");
		 System.out.println("-------------------------------------------");
		
		 // Sample #3
		
		 Grammar sample3 = new Grammar("Sample3.in");
		
		 System.out.println("String Definition: " +
		 sample3.StringDefinitions.toString());
		 System.out.println("Terminals :" + sample3.terminals.toString());
		 System.out.println("NonTerminals :" +
		 sample3.nonTerminals.toString());
		
		 for (int i = 0; i < sample3.rules.size(); i++) {
		 System.out.println(sample3.rules.get(i).toString());
		 }
		 System.out.println("NumberOfRules: " + sample3.rules.size());
		
		 sample3.writeOutput("Sample3.out");
		 System.out.println("-------------------------------------------");

		// Sample #4

		Grammar sample4 = new Grammar("Sample4.in", true);

		System.out.println("String Definition: " + sample4.StringDefinitions.toString());
		System.out.println("Terminals :" + sample4.terminals.toString());
		System.out.println("NonTerminals :" + sample4.nonTerminals.toString());

		for (int i = 0; i < sample4.rules.size(); i++) {
			System.out.println(sample4.rules.get(i).toString());
		}
		System.out.println("NumberOfRules: " + sample4.rules.size());

		// Fist and Follow methods
		sample4.first();
		sample4.follow();
		sample4.writeOutput("Sample4.out", sample4.allFirstsAndFollows);

		System.out.println("-------------------------------------------");

		 // Sample #5
		
		Grammar sample5 = new Grammar("sample5.in", true);

		System.out.println("String Definition: " + sample5.StringDefinitions.toString());
		System.out.println("Terminals :" + sample5.terminals.toString());
		System.out.println("NonTerminals :" + sample5.nonTerminals.toString());

		for (int i = 0; i < sample5.rules.size(); i++) {
			System.out.println(sample5.rules.get(i).toString());
		}
		System.out.println("NumberOfRules: " + sample5.rules.size());

		// Fist and Follow methods
		sample5.first();
		sample5.follow();
		sample5.writeOutput("Sample5.out", sample5.allFirstsAndFollows);
		 System.out.println("-------------------------------------------");
	}
}
