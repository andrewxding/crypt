import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private int numRounds;
    private HashSet fs;
    private HashSet reject;
    private Set<Transaction> txs;
    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.p_graph = p_graph; 
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
        this.fs = new HashSet(); 
        this.reject = new HashSet();
    }

    public void setFollowees(boolean[] followees) {
        for(int i = 0; i < followees.length; i ++){
            if (followees[i])
                fs.add(i);
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.txs = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        Set<Transaction> tmp = new HashSet<Transaction>(txs);
        txs.clear();
        return tmp;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for(Candidate c : candidates){
            if(!fs.contains(c.sender))
                reject.add(c.sender);
        }
        for (Candidate c : candidates) {
            if (!reject.contains(c.sender)) {
                txs.add(c.tx);
            }
        }
    }
}
