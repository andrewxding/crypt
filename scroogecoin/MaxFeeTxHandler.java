import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
public class MaxFeeTxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        UTXOPool uniqueUtxos = new UTXOPool();
        double previousTxSum = 0;
        double currentTxSum = 0;
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input in = tx.getInput(i);
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!utxoPool.contains(utxo)) 
                return false;//check prev transactions recorded
            Transaction.Output output = utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), in.signature))//check signature is valid
                return false;
            if (uniqueUtxos.contains(utxo)) 
                return false;//check that it isnt double spent
            uniqueUtxos.addUTXO(utxo, output);//record it
            previousTxSum += output.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            if (out.value < 0) return false;//non negative outputs
            currentTxSum += out.value;
        }
        return previousTxSum >= currentTxSum;
    }
    private double calcFees(Transaction tx) {
        double sumInputs = 0;
        double sumOutputs = 0;
        for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!utxoPool.contains(utxo) || !isValidTx(tx)) continue;
            Transaction.Output txOutput = utxoPool.getTxOutput(utxo);
            sumInputs += txOutput.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            sumOutputs += out.value;
        }
        return sumInputs - sumOutputs;
    }
    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Arrays.sort(possibleTxs,(tx1, tx2) ->{
                double tx1Fees = calcFees(tx1);
                double tx2Fees = calcFees(tx2);
                return Double.valueOf(tx2Fees).compareTo(tx1Fees);
            }
        );
        ArrayList<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs) {
            if (!isValidTx(tx)) {
                continue;
            }
            validTxs.add(tx);

            for (Transaction.Input input : tx.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                this.utxoPool.removeUTXO(utxo);
            }
            int i = 0;
            for (Transaction.Output output : tx.getOutputs()) {
                UTXO utxo = new UTXO(tx.getHash(), i);
                i ++;
                this.utxoPool.addUTXO(utxo, output);    
            }
        }
        
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

    

}
