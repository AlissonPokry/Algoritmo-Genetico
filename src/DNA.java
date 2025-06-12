import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class DNA {
    // Constantes e atributos
    public static final int ngenes = Main.NUMERO_POTES;  // Cada gene representa um pote de investimento
    private String[] acoes;   // Array com os códigos das ações escolhidas para cada pote
    private double fitness;   // Valor de adequação (lucro/prejuízo) desta solução
    
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
        // Crossover: combina genes dos pais
        for(int i = 0; i < ngenes; i++) {
            if(i < crossover) {
                acoes[i] = pai.getAcao(i); // Herda do pai
            } else {
                acoes[i] = mae.getAcao(i);  // Herda da mãe
            }
        }
        
        // Mutação: 15% de chance de trocar duas ações de posição
        if(Main.rnd.nextInt(100) < 15) {
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
        // Inicialização
        double valorTotal = 0;
        double valorPorPote = Main.montante / (Main.NUMERO_POTES * 100.0);  // Valor em reais por pote

        // Obtém as cotações dos dias de compra e venda
        ArrayList<Cotacao> cotacoesCompra = cotacoesPorData.get(dataCompra);
        ArrayList<Cotacao> cotacoesVenda = cotacoesPorData.get(dataVenda);
        
        // Validação dos dados
        if (cotacoesCompra == null || cotacoesVenda == null) {
            this.fitness = -Main.montante;  // Penalização máxima
            return;
        }
        
        // Calcula o retorno para cada ação escolhida
        for (int i = 0; i < ngenes; i++) {
            String codigoAcao = acoes[i];
            // Busca preço de compra
            Double precoCompra = null;
            for (Cotacao c : cotacoesCompra) {
                if (c.codigo.equals(codigoAcao)) {
                    precoCompra = c.preco;
                    break;
                }
            }
            
            // Busca preço de venda
            Double precoVenda = null;
            for (Cotacao c : cotacoesVenda) {
                if (c.codigo.equals(codigoAcao)) {
                    precoVenda = c.preco;
                    break;
                }
            }
            
            // Calcula retorno se for possível comprar/vender
            if (precoCompra != null && precoVenda != null && precoCompra > 0) {
                int quantidade = (int)(valorPorPote / precoCompra);
                if (quantidade > 0) {
                    double valorRetorno = quantidade * precoVenda;
                    valorTotal += valorRetorno;
                }
            }
        }
        
        // Calcula lucro/prejuízo
        this.fitness = (valorTotal * 100) - Main.montante;
    }
    
    @Override
    public String toString() {
        return Arrays.toString(acoes) + " Fitness: " + fitness;
    }
}
