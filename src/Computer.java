import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

class Computer {
    private ArrayList<ProductionRule> rules = new ArrayList<>();
    private ArrayList<NonTerminal> openList = new ArrayList<>();
    private HashSet<Terminal> terminals = new HashSet<>();
    private HashMap<NonTerminal, ArrayList<ProductionRule>> nonTerminals = new HashMap<>();
    private HashMap<NonTerminal, HashMap<Terminal, Integer>> parseTable = new HashMap<>();
    private StringBuilder grammar = new StringBuilder();

    private boolean isNullable(Word word) {
        if (word.getName().equals("#"))
            return true;
        if (word instanceof Terminal)
            return false;

        NonTerminal nt = (NonTerminal) word;

        if (nt.isNullable() != null)  // if already set
            return nt.isNullable();

        for (ProductionRule rule : nonTerminals.get(nt)) {
            boolean b = true;
            for (Word w : rule.getWords()) {
                if (!isNullable(w)) {
                    b = false;
                    break;
                }
            }
            if (b) {
                nt.setNullable(true);
                return true;
            }
        }
        nt.setNullable(false);
        return false;
    }

    private HashSet<Terminal> SetFirst(NonTerminal word) {
        if (word.getFirstSet() != null)
            return word.getFirstSet();

        HashSet<Terminal> set = new HashSet<>();

        for (ProductionRule rule : nonTerminals.get(word)) {
            for (Word w : rule.getWords()) {
                if (w instanceof Terminal) {
                    if (!w.getName().equals("#"))
                        set.add((Terminal) w);
                    break;
                }
                NonTerminal nt = (NonTerminal) w;
                set.addAll(SetFirst(nt));
                if (!isNullable(w))
                    break;
            }
        }
        word.setFirstSet(set);
        return set;
    }

    private HashSet<Terminal> SetFirst(int productRuleNum) {
        HashSet<Terminal> set = new HashSet<>();

        for (Word w : rules.get(productRuleNum).getWords()) {
            if (w instanceof Terminal) {
                if (!w.getName().equals("#"))
                    set.add((Terminal) w);
                break;
            }
            NonTerminal nt = (NonTerminal) w;
            set.addAll(SetFirst(nt));
            if (!isNullable(w))
                break;
        }
        return set;
    }

    private HashSet<Terminal> SetFollow(NonTerminal nonT) {
        if (nonT.getFollowSet() != null)
            return nonT.getFollowSet();

        HashSet<Terminal> set = new HashSet<>();

        if (nonT.getName().equals("S")) {
            set.add(new Terminal("$"));
            nonT.setFollowSet(set);
            return set;
        }
        openList.add(nonT);

        for (ProductionRule rule : rules) {
            int index = rule.getWords().indexOf(nonT);
            if (index > -1) {
                while (index < rule.getWords().size()) {
                    if (index == rule.getWords().size() - 1) {
                        // To avoid getting stuck in the loop
                        if (!openList.contains(rule.getNonTerminal()))
                            set.addAll(SetFollow(rule.getNonTerminal()));
                        break;
                    }
                    index++;
                    Word word = rule.getWords().get(index);
                    if (word instanceof Terminal) {
                        if (!word.getName().equals("#"))
                            set.add((Terminal) word);
                        break;
                    }
                    set.addAll(SetFirst((NonTerminal) word));
                    if (!isNullable(word))
                        break;
                }
            }
        }
        openList.clear();
        nonT.setFollowSet(set);
        return set;
    }

    private HashSet<Terminal> SetpredictSet(int productRuleNum) {
        ProductionRule rule = rules.get(productRuleNum);

        HashSet<Terminal> set = new HashSet<>(SetFirst(productRuleNum));
        if (isNullable(rule.getNonTerminal()))
            set.addAll(SetFollow(rule.getNonTerminal()));

        return set;
    }

    private void initGrammar(String url) {
        while (true) {
            File file = new File(url);
            try (Scanner scanner = new Scanner(file)) {
                int i = 0;
                while (scanner.hasNext()) {
                    String rule = scanner.nextLine();
                    grammar.append(i++).append(". ").append(rule);
                    String nonT = rule.substring(0, rule.indexOf(":")).trim();

                    NonTerminal nt = new NonTerminal(nonT);
                    if (!nonTerminals.containsKey(nt))
                        nonTerminals.put(nt, new ArrayList<>());

                    String[] words = rule.substring(rule.indexOf(":") + 1).trim().split(" ");
                    ArrayList<Word> list = new ArrayList<>();
                    for (String s : words) {
                        if (s.toLowerCase().equals(s)) {
                            Terminal t = new Terminal(s);
                            list.add(t);
                            if (!s.equals("#"))
                                terminals.add(t);
                        } else
                            list.add(new NonTerminal(s));
                    }
                    ProductionRule pr = new ProductionRule(nt, list);
                    rules.add(pr);
                    nonTerminals.get(nt).add(pr);
                    grammar.append("\n");
                }
                terminals.add(new Terminal("$"));
                break;
            } catch (FileNotFoundException e) {
                System.out.println("The url is wrong. Please reEnter the input file url");
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
            }
        }
    }

    private void computeParseTable() {
        for (int i = 0; i < rules.size(); i++) {
            NonTerminal nt = rules.get(i).getNonTerminal();
            if (!parseTable.containsKey(nt))
                parseTable.put(nt, new HashMap<>());
            HashSet<Terminal> set = SetpredictSet(i);
            for (Terminal t : set) {
                parseTable.get(nt).put(t, i);
            }
        }
    }

    void InitStuff(String url) {
        initGrammar(url);
        ComputeNullable();
        ComputeFirst();
        ComputeFollow();
        computeParseTable();
    }

    void printFollowSets() {
        PrintStars("Follow Set");
        for (NonTerminal nt : nonTerminals.keySet()) {
            System.out.printf("%-8s : %s\n", nt, Arrays.toString(nt.getFollowSet().toArray()));
        }
    }

    void printFirstSets() {
        PrintStars("First Set");
        for (NonTerminal nt : nonTerminals.keySet()) {
            System.out.printf("%-8s : %s\n", nt, Arrays.toString(nt.getFirstSet().toArray()));
        }
    }

    void printIsNullable() {
        PrintStars("Nullable Set");
        for (NonTerminal nt : nonTerminals.keySet()) {
            System.out.printf("%-8s : %s\n", nt, nt.isNullable().toString().toUpperCase());
        }
    }

    void printParseTable() {
        PrintStars("Parse Table");
        System.out.print("         ");
        for (Terminal t : terminals) {
            System.out.printf("|   %-3s  ", t);
        }
        System.out.println();

        for (NonTerminal nt : nonTerminals.keySet()) {
            printLine();
            System.out.printf("%-9s", nt);
            for (Terminal t : terminals) {
                if (parseTable.get(nt).containsKey(t))
                    System.out.printf("|   %-2d   ", parseTable.get(nt).get(t));
                else
                    System.out.print("|        ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void printLine() {
        System.out.print("---------.");
        for (Terminal ignored : terminals) {
            System.out.print("--------.");
        }
        System.out.println();
    }

    void printGrammar() {
        PrintStars("Grammar");
        System.out.println(grammar);
    }

    private void ComputeNullable() {
        for (NonTerminal nt : nonTerminals.keySet()) {
            isNullable(nt);
        }
    }

    private void ComputeFirst() {
        for (NonTerminal nt : nonTerminals.keySet()) {
            SetFirst(nt);
        }
    }

    private void ComputeFollow() {
        for (NonTerminal nt : nonTerminals.keySet()) {
            SetFollow(nt);
        }
    }

    private void PrintStars(String s) {
        System.out.printf("*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*..*\n%s =>\n", s);
    }
}