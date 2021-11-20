public class App {
    public static void main(String[] args) {
        String url = "input.txt";
        Computer computer = new Computer();
        computer.InitStuff(url);
        computer.printGrammar();
        computer.printIsNullable();
        computer.printFirstSets();
        computer.printFollowSets();
        computer.printParseTable();
    }
}