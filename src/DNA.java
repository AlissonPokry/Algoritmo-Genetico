import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DNA {
    public static final int ngenes = Main.NUMERO_POTES;
    private String[] acoes; // códigos das ações escolhidas
    private double fitness;
    
    public DNA() {
        this.acoes = new String[ngenes];
        this.fitness = 0.0;
    }
    
    public void criarandomico(ArrayList<String> acoesDisponiveis) {
        for(int i = 0; i < ngenes; i++) {
            int index = Main.rnd.nextInt(acoesDisponiveis.size());
            acoes[i] = acoesDisponiveis.get(index);
        }
    }
    
    public void criaPaiMae(DNA pai, DNA mae, int crossover) {
        for(int i = 0; i < ngenes; i++) {
            if(i < crossover) {
                acoes[i] = pai.getAcao(i);
            } else {
                acoes[i] = mae.getAcao(i);
            }
        }
        
        // Mutação
        if(Main.rnd.nextInt(100) < 15) { // 15% chance de mutação
            int index = Main.rnd.nextInt(ngenes);
            int novoIndex = Main.rnd.nextInt(ngenes);
            String temp = acoes[index];
            acoes[index] = acoes[novoIndex];
            acoes[novoIndex] = temp;
        }
    }
    
    public String getAcao(int index) {
        return acoes[index];
    }
    
    public double getFitness() {
        return fitness;
    }
    
    public void avaliarFitness(HashMap<String, ArrayList<Cotacao>> cotacoesPorData, String dataCompra, String dataVenda) {
        double valorTotal = 0;
        double valorInicial = 1000.00; // R$ 100,00 por pote * 10 potes
        
        ArrayList<Cotacao> cotacoesCompra = cotacoesPorData.get(dataCompra);
        ArrayList<Cotacao> cotacoesVenda = cotacoesPorData.get(dataVenda);
        
        if (cotacoesCompra == null || cotacoesVenda == null) {
            this.fitness = -valorInicial * 100; // Penalidade máxima
            return;
        }
        
        for (int i = 0; i < ngenes; i++) {
            double valorPote = 100.00; // R$ 100,00 por pote
            String codigoAcao = acoes[i];
            
            // Encontra preço de compra
            Double precoCompra = null;
            for (Cotacao c : cotacoesCompra) {
                if (c.codigo.equals(codigoAcao)) {
                    precoCompra = c.preco;
                    break;
                }
            }
            
            // Encontra preço de venda
            Double precoVenda = null;
            for (Cotacao c : cotacoesVenda) {
                if (c.codigo.equals(codigoAcao)) {
                    precoVenda = c.preco;
                    break;
                }
            }
            
            // Se encontrou ambos os preços
            if (precoCompra != null && precoVenda != null && precoCompra > 0) {
                int quantidade = (int)(valorPote / precoCompra);
                if (quantidade > 0) {
                    double valorInvestido = quantidade * precoCompra;
                    double valorRetorno = quantidade * precoVenda;
                    valorTotal += valorRetorno;
                }
            }
        }
        
        // Calcula o lucro/prejuízo em centavos
        this.fitness = (valorTotal - valorInicial) * 100;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(acoes) + " Fitness: " + fitness;
    }
}
