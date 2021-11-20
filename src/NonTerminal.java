import java.util.HashSet;

class NonTerminal extends Word {
    private HashSet<Terminal> firstSet;
    private HashSet<Terminal> followSet;
    private Boolean isNullable = null;

    NonTerminal(String name) {
        super(name);
    }

    HashSet<Terminal> getFirstSet() {
        return firstSet;
    }

    void setFirstSet(HashSet<Terminal> firstSet) {
        this.firstSet = firstSet;
    }

    HashSet<Terminal> getFollowSet() {
        return followSet;
    }

    void setFollowSet(HashSet<Terminal> followSet) {
        this.followSet = followSet;
    }

    Boolean isNullable() {
        return isNullable;
    }

    void setNullable(boolean nullable) {
        isNullable = nullable;
    }
}