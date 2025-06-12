import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

class Cotacao {
    String data;
    String codigo;
    double preco;
    
    public Cotacao(String data, String codigo, double preco) {
        this.data = data;
        this.codigo = codigo;
        this.preco = preco;
    }
}

public class Main {
    public static Random rnd = new Random();  // Gerador de números aleatórios para o algoritmo genético
    public static String NOME_ARQUIVO = "cotacoes.csv";  // Arquivo com histórico de cotações
    public static final int NUMERO_POTES = 10;  // Número de divisões do investimento
    public static int montante = 100000;  // Montante inicial em centavos (R$ 1000,00)
    public static final int TAMANHO_POPULACAO = 10000; 
    public static final int NUMERO_GERACOES = 50;


	public static void main(String[] args) {
		negociarAcoesGenetico();
		System.out.println("\nNegociações concluídas.");
	}

	public static int[] dividirMontanteEmPotes() {
        int[] potes = new int[NUMERO_POTES];
        int valorPorPote = montante / NUMERO_POTES;
        
        for (int i = 0; i < NUMERO_POTES; i++) {
            potes[i] = valorPorPote;
        }
        
        // Adiciona os centavos restantes da divisão ao último pote
        int resto = montante % NUMERO_POTES;
        if (resto > 0) {
            potes[NUMERO_POTES - 1] += resto;
        }
        
        // Imprime o valor de cada pote
        for (int i = 0; i < NUMERO_POTES; i++) {
            System.out.printf("Pote %d: R$ %.2f\n", i + 1, potes[i] / 100.0);
        }
        
        return potes;
    }

	public static ArrayList<Cotacao> getCotacoes(String nomeArquivo) {
    ArrayList<Cotacao> cotacoes = new ArrayList<>();
    try {
        BufferedReader bfr = new BufferedReader(new FileReader(nomeArquivo));
        String linha;
        
        // Pula o cabeçalho
        bfr.readLine();
        
        while ((linha = bfr.readLine()) != null) {
            String[] dados = linha.split(";");
            if (dados.length >= 3) {
                String data = dados[0].split(" ")[0];
                String codigo = dados[1].trim();
                
                // Verifica se o código tem exatamente 5 caracteres alfanuméricos
                if (!codigo.matches("^[A-Z0-9]{5}$")) {
                    continue;
                }
                
                String precoStr = dados[2].trim().replace(",", ".");
                double preco = Double.parseDouble(precoStr);
                
                if (preco > 0) {
                    cotacoes.add(new Cotacao(data, codigo, preco));
                }
            }
        }
        bfr.close();
        
        
    } catch (Exception e) {
        System.out.println("Erro ao ler arquivo: " + e.getMessage());
        e.printStackTrace();
    }
    return cotacoes;
}

	public static void negociarAcoesGenetico() {
    ArrayList<Cotacao> cotacoes = getCotacoes(NOME_ARQUIVO);
    
    // Agrupa as cotações por data
    HashMap<String, ArrayList<Cotacao>> cotacoesPorData = new HashMap<>();
    for (Cotacao c : cotacoes) {
        cotacoesPorData.computeIfAbsent(c.data, k -> new ArrayList<>()).add(c);
    }
    
    // Coleta todas as ações disponíveis
    Set<String> codigosAcoes = new java.util.HashSet<>();
    for(Cotacao c : cotacoes) {
        codigosAcoes.add(c.codigo);
    }
    ArrayList<String> acoesDisponiveis = new ArrayList<>(codigosAcoes);
    
    // Ordena as datas
    ArrayList<String> datas = new ArrayList<>(cotacoesPorData.keySet());
    Collections.sort(datas);
    
    // Parâmetros do algoritmo genético
    int tamanhoPopulacao = TAMANHO_POPULACAO;
    int numeroGeracoes = NUMERO_GERACOES;
    ArrayList<DNA> populacao = new ArrayList<>();
    
    double montanteAtual = montante; // Track current total money
    
    for(int i = 0; i < datas.size() - 1; i += 2) {
        String dataCompra = datas.get(i);
        String dataVenda = datas.get(i + 1);
        
        System.out.printf("\nOtimizando para compra em %s e venda em %s\n", dataCompra, dataVenda);
        System.out.printf("Montante disponível: R$ %.2f\n", montanteAtual/100.0);
        
        // Update current montante for this trading cycle
        montante = (int)montanteAtual;
        
        // Cria população inicial
        populacao.clear();
        for(int j = 0; j < tamanhoPopulacao; j++) {
            DNA dna = new DNA();
            dna.criarandomico(acoesDisponiveis);
            populacao.add(dna);
        }
        
        // Evolui a população
        for(int geracao = 0; geracao < numeroGeracoes; geracao++) {
            // Avalia fitness
            for(DNA dna : populacao) {
                dna.avaliarFitness(cotacoesPorData, dataCompra, dataVenda);
            }
            
            // Ordena por fitness
            Collections.sort(populacao, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            
            // Mostra melhor resultado da geração
            if(geracao % 10 == 0) {
                DNA melhor = populacao.get(0);
                System.out.printf("Geração %d - Lucro/Prejuízo: R$ %.2f\n", 
                    geracao, melhor.getFitness() / 100.0);
            }
            
            // Cria nova geração
            ArrayList<DNA> novaGeracao = new ArrayList<>();
            
            // Elitismo - mantém os melhores
            for(int j = 0; j < tamanhoPopulacao * 0.1; j++) {
                novaGeracao.add(populacao.get(j));
            }
            
            // Crossover
            while(novaGeracao.size() < tamanhoPopulacao) {
                DNA pai = populacao.get(rnd.nextInt(tamanhoPopulacao / 2));
                DNA mae = populacao.get(rnd.nextInt(tamanhoPopulacao / 2));
                DNA filho = new DNA();
                filho.criaPaiMae(pai, mae, rnd.nextInt(NUMERO_POTES));
                novaGeracao.add(filho);
            }
            
            populacao = novaGeracao;
        }
        
        // Usa o melhor resultado encontrado
        DNA melhorSolucao = populacao.get(0);
        melhorSolucao.avaliarFitness(cotacoesPorData, dataCompra, dataVenda);
        
        // Update montante with profits/losses
        montanteAtual = montante + melhorSolucao.getFitness();
        
        System.out.printf("\nMelhor carteira encontrada para %s -> %s:\n", dataCompra, dataVenda);
        for(int p = 0; p < NUMERO_POTES; p++) {
            String acao = melhorSolucao.getAcao(p);
            double precoCompra = encontrarPrecoAcao(cotacoesPorData.get(dataCompra), acao);
            double precoVenda = encontrarPrecoAcao(cotacoesPorData.get(dataVenda), acao);
            System.out.printf("Pote %d: %s - %d cotas (Compra: R$ %.2f, Venda: R$ %.2f)\n",
                p + 1, acao, (int)((Main.montante / (Main.NUMERO_POTES * 100.0)) / precoCompra),
                precoCompra, precoVenda);
        }
        System.out.printf("Lucro/Prejuízo na operação: R$ %.2f\n", melhorSolucao.getFitness() / 100.0);
        System.out.printf("Novo montante total: R$ %.2f\n", montanteAtual/100.0);
    }
}

	private static double encontrarPrecoAcao(ArrayList<Cotacao> cotacoes, String codigo) {
    for(Cotacao c : cotacoes) {
        if(c.codigo.equals(codigo)) {
            return c.preco;
        }
    }
    return 0;
}
}