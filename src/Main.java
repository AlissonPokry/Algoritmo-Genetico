import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public static float matrizdistancias[][];
	public static ArrayList<float[]> listadelinhas = new ArrayList<float[]>();
	public static int citycode = 0;
	public static HashMap<Integer,String> mapCodName = new HashMap<Integer,String>();
	public static HashMap<String,Integer> mapNameCod = new HashMap<String,Integer>();
	public static Random rnd = new Random();
	public static int numerodeindividuos = 1000000;
	public static String NOME_ARQUIVO = "cotacoes.csv";
	public static final int NUMERO_POTES = 10;
	public static int montante = 100000; //dinheiro deve SEMPRE ser salvo como Int em centavos


	public static void main(String[] args) {
	

		// carregaMatrizDistancia();
		
		// ArrayList<DNA> pollGenetico = new ArrayList<DNA>();
		
		// for(int i = 0; i < numerodeindividuos; i++) {
		// 	DNA dna1 = new DNA();
		// 	dna1.criarandomico();
		// 	dna1.avaliaDna();
		// 	pollGenetico.add(dna1);
		// }
		
		// int numerogeracoes = 100;
		// for(int geracao = 0; geracao < numerogeracoes; geracao++) {
			
		// 	Collections.sort(pollGenetico, new Comparator<DNA>() {
		// 		@Override
		// 		public int compare(DNA o1, DNA o2) {
		// 			return o1.score < o2.score? -1: o1.score > o2.score? 1:0;
		// 		}
		// 	});
			
		// 	System.out.println("Geracao "+geracao);
		// 	for(int i = 0; i < 10;i++) {
		// 		DNA dna = pollGenetico.get(i);
		// 		System.out.println(""+i+": "+dna.score+","+dna.distanciaTotal+","+dna.repetidos+" "+dna);
		// 	}
			
		// 	ArrayList<DNA> novosGenes = new ArrayList<DNA>();
		// 	for(int i = 0; i < numerodeindividuos; i++) {
		// 		int pai = sorteio();
		// 		int mae = sorteio();
				
		// 		if(pai==mae) {
		// 			mae++;
		// 		}
				
		// 		DNA dna_pai = pollGenetico.get(pai);
		// 		DNA dna_mae = pollGenetico.get(mae);
				
		// 		int pontocrossover = rnd.nextInt(DNA.ngenes-10)+5;
				
		// 		DNA novodna = new DNA();
		// 		novodna.criaPaiMae(dna_pai, dna_mae, pontocrossover);
		// 		novosGenes.add(novodna);
		// 	}
			
		// 	pollGenetico.clear();
		// 	for(int i = 0; i < numerodeindividuos; i++) {
		// 		DNA dna = novosGenes.get(i);
		// 		dna.avaliaDna();
		// 		pollGenetico.add(dna);
		// 	}
		// }

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

public static void negociarAcoes() {
    ArrayList<Cotacao> cotacoes = getCotacoes(NOME_ARQUIVO);
    
    // Agrupa as cotações por data
    HashMap<String, ArrayList<Cotacao>> cotacoesPorData = new HashMap<>();
    for (Cotacao c : cotacoes) {
        cotacoesPorData.computeIfAbsent(c.data, k -> new ArrayList<>()).add(c);
    }
    
    // Ordena as datas
    ArrayList<String> datas = new ArrayList<>(cotacoesPorData.keySet());
    Collections.sort(datas);
    
    boolean diaCompra = true;
    int[] potes = dividirMontanteEmPotes();
    // Mapa para armazenar ações de cada pote separadamente
    HashMap<Integer, HashMap<String, Integer>> acoesPorPote = new HashMap<>();
    String ultimoDiaCompra = null;
    
    System.out.println("\nIniciando negociações...");
    
    for (String data : datas) {
        ArrayList<Cotacao> cotacoesDoDia = cotacoesPorData.get(data);
        
        if (diaCompra) {
            // Dia de compra
            System.out.println("\nDia " + data + " - COMPRANDO");
            acoesPorPote.clear(); // Limpa ações anteriores
            
            for (int i = 0; i < NUMERO_POTES; i++) {
                if (i < cotacoesDoDia.size()) {
                    Cotacao cotacao = cotacoesDoDia.get(i);
                    int quantidade = (int)(potes[i] / (cotacao.preco * 100));
                    int valorGasto = (int)(quantidade * cotacao.preco * 100);
                    potes[i] -= valorGasto;
                    
                    // Armazena as ações compradas com o dinheiro deste pote
                    HashMap<String, Integer> acoesDoPote = new HashMap<>();
                    acoesDoPote.put(cotacao.codigo, quantidade);
                    acoesPorPote.put(i, acoesDoPote);
                    
                    System.out.printf("Pote %d: Comprado %d ações de %s a R$ %.2f cada (Total: R$ %.2f)\n",
                        i + 1, quantidade, cotacao.codigo, cotacao.preco, valorGasto/100.0);
                }
            }
            ultimoDiaCompra = data;
            
        } else {
            // Dia de venda - apenas se houver dia de compra anterior
            if (ultimoDiaCompra != null && !acoesPorPote.isEmpty()) {
                System.out.println("\nDia " + data + " - VENDENDO");
                int valorTotal = 0;
                
                // Vende as ações de cada pote separadamente
                for (int i = 0; i < NUMERO_POTES; i++) {
                    HashMap<String, Integer> acoesDoPote = acoesPorPote.get(i);
                    if (acoesDoPote != null) {
                        int valorPote = 0;
                        
                        for (String codigoAcao : acoesDoPote.keySet()) {
                            // Procura a cotação da ação neste dia
                            Cotacao cotacaoDaAcao = null;
                            for (Cotacao c : cotacoesDoDia) {
                                if (c.codigo.equals(codigoAcao)) {
                                    cotacaoDaAcao = c;
                                    break;
                                }
                            }
                            
                            if (cotacaoDaAcao != null) {
                                int quantidade = acoesDoPote.get(codigoAcao);
                                int valorVenda = (int)(quantidade * cotacaoDaAcao.preco * 100);
                                valorPote += valorVenda;
                                
                                System.out.printf("Pote %d: Vendido %d ações de %s a R$ %.2f cada (Total: R$ %.2f)\n",
                                    i + 1, quantidade, codigoAcao, cotacaoDaAcao.preco, valorVenda/100.0);
                            } else {
                                System.out.printf("AVISO: Não foi possível vender ações do pote %d: %s (cotação não encontrada)\n", 
                                    i + 1, codigoAcao);
                            }
                        }
                        valorTotal += valorPote;
                        potes[i] = valorPote; // Atualiza o valor do pote
                    }
                }
                
                montante = valorTotal;
                System.out.printf("Novo montante total: R$ %.2f\n\n", montante/100.0);
                potes = dividirMontanteEmPotes(); // Redistribui o montante entre os potes
            }
        }
        
        diaCompra = !diaCompra;
    }
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
    int tamanhoPopulacao = 100;
    int numeroGeracoes = 50;
    ArrayList<DNA> populacao = new ArrayList<>();
    
    for(int i = 0; i < datas.size() - 1; i += 2) {
        String dataCompra = datas.get(i);
        String dataVenda = datas.get(i + 1);
        
        System.out.printf("\nOtimizando para compra em %s e venda em %s\n", dataCompra, dataVenda);
        
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
        
        System.out.printf("\nMelhor carteira encontrada para %s -> %s:\n", dataCompra, dataVenda);
        for(int p = 0; p < NUMERO_POTES; p++) {
            String acao = melhorSolucao.getAcao(p);
            double precoCompra = encontrarPrecoAcao(cotacoesPorData.get(dataCompra), acao);
            double precoVenda = encontrarPrecoAcao(cotacoesPorData.get(dataVenda), acao);
            System.out.printf("Pote %d: %s (Compra: R$ %.2f, Venda: R$ %.2f)\n", 
                p + 1, acao, precoCompra, precoVenda);
        }
        System.out.printf("Lucro/Prejuízo total: R$ %.2f\n", melhorSolucao.getFitness() / 100.0);
    }
}
	

	//----------------------------------------------------------------------------------------------
	
	public static int sorteio() {
		int sorteio = rnd.nextInt(2048-1);
		int vbase = 1024;
		for(int i = 0; i < 10; i++) {
			sorteio-=vbase;
			if(sorteio<0) {
				return i;
			}
			vbase = vbase/2;
		}
		return 9;
	}

	private static void carregaMatrizDistancia() {
		BufferedReader bfr;
		try {
			bfr = new BufferedReader(new FileReader("distances.csv"), 1024000);
			String header = bfr.readLine();
			System.out.println("header "+header);
			String line = "";
			

			while((line=bfr.readLine())!=null) {
				//System.out.println(""+line);
				String splt[] = line.split(",");
				String cidade_origem = splt[0];
				String cidade_destino = splt[1];
				float dist = Float.parseFloat(splt[3]);
				
				int origemcode = -1;
				if(mapNameCod.containsKey(cidade_origem)) {
					origemcode = mapNameCod.get(cidade_origem);
				}else {
					origemcode = citycode;
					mapNameCod.put(cidade_origem, origemcode);
					mapCodName.put( origemcode, cidade_origem);
					citycode++;
				}
				
				int destinocode = -1;
				if(mapNameCod.containsKey(cidade_destino)) {
					destinocode = mapNameCod.get(cidade_destino);
				}else {
					destinocode = citycode;
					mapNameCod.put(cidade_destino, destinocode);
					mapCodName.put( destinocode, cidade_destino);
					citycode++;
				}
				
				float dado[] = new float[3];
				dado[0] = origemcode;
				dado[1] = destinocode;
				dado[2] = dist;
				
				listadelinhas.add(dado);
			}
			
			System.out.println(" "+citycode+" "+listadelinhas.size());
			
			matrizdistancias = new float[citycode][citycode];
			
			for(int i = 0; i < listadelinhas.size();i++) {
				float dado[] = listadelinhas.get(i);
				matrizdistancias[(int)dado[0]][(int)dado[1]] = dado[2];
			}
			
			//Lenningen,Asselborn,62.3,85.5
//			int cod1 = mapNameCod.get("Lenningen");
//			int cod2 = mapNameCod.get("Asselborn");
//			float dist = matrizdistancias[cod1][cod2];
			
			//System.out.println("Lenningen ->  Asselborn "+cod1+" - "+cod2+" dist "+dist);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
