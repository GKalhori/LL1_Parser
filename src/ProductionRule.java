import java.util.ArrayList;

class ProductionRule {
    private NonTerminal nonTerminal;
    private ArrayList<Word> rule;

    ProductionRule(NonTerminal nonTerminal, ArrayList<Word> rule) {
        this.nonTerminal = nonTerminal;
        this.rule = rule;
    }

    NonTerminal getNonTerminal() {
        return nonTerminal;
    }

    ArrayList<Word> getWords() {
        return rule;
    }
}