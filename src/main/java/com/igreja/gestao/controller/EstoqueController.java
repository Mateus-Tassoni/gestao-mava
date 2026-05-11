package com.igreja.gestao.controller;

import com.igreja.gestao.model.MovimentacaoEstoque;
import com.igreja.gestao.model.Produto;
import com.igreja.gestao.model.TipoMovimentacao;
import com.igreja.gestao.repository.FamiliaRepository;
import com.igreja.gestao.repository.MovimentacaoEstoqueRepository;
import com.igreja.gestao.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private FamiliaRepository familiaRepository;

    // ROTA PADRÃO (Evita o erro 404 se acessar /estoque)
    @GetMapping
    public String index() {
        return "redirect:/estoque/lista";
    }

    // LISTAGEM GERAL COM INTELIGÊNCIA DE VENCIMENTO CORRIGIDA
    @GetMapping("/lista")
    public String listarEstoque(Model model) {
        List<Produto> todosProdutos = produtoRepository.findAll();

        // Busca apenas movimentações de ENTRADA/DOAÇÃO que possuem data de validade
        List<MovimentacaoEstoque> entradasComValidade = movimentacaoRepository.findAll().stream()
                .filter(m -> (m.getTipo() == TipoMovimentacao.ENTRADA || m.getTipo() == TipoMovimentacao.DOACAO))
                .filter(m -> m.getDataValidade() != null)
                .collect(Collectors.toList());

        // Agrupa esses lotes pelo ID do Produto para o HTML saber quem é de quem
        Map<Long, List<MovimentacaoEstoque>> lotesPorProduto = entradasComValidade.stream()
                .collect(Collectors.groupingBy(m -> m.getProduto().getId()));

        model.addAttribute("produtos", todosProdutos);
        model.addAttribute("lotesPorProduto", lotesPorProduto); // Mandando os lotes pra tela

        return "estoque/lista";
    }

    // TELA DE MOVIMENTAÇÃO (Entrada/Saída)
    @GetMapping("/movimentar")
    public String novaMovimentacao(Model model) {
        model.addAttribute("movimentacao", new MovimentacaoEstoque());
        model.addAttribute("listaProdutos", produtoRepository.findAll());
        model.addAttribute("familias", familiaRepository.findAll());
        return "estoque/formulario-movimentacao";
    }

    // SALVAR MOVIMENTAÇÃO
    @PostMapping("/salvar-movimentacao")
    public String salvarMovimentacao(MovimentacaoEstoque movimentacao) {
        if (movimentacao.getDataHora() == null) {
            movimentacao.setDataHora(LocalDateTime.now());
        }

        Produto produto = produtoRepository.findById(movimentacao.getProduto().getId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        int qtd = movimentacao.getQuantidade();

        // Soma se for Compra OU Doação Recebida
        if (movimentacao.getTipo() == TipoMovimentacao.ENTRADA || movimentacao.getTipo() == TipoMovimentacao.DOACAO) {
            produto.setQuantidadeAtual(produto.getQuantidadeAtual() + qtd);
        }
        // Subtrai se for Saída, Consumo ou Perda
        else {
            if (produto.getQuantidadeAtual() < qtd) {
                return "redirect:/estoque/movimentar?erro=estoque_insuficiente";
            }
            produto.setQuantidadeAtual(produto.getQuantidadeAtual() - qtd);
        }

        movimentacaoRepository.save(movimentacao);
        produtoRepository.save(produto);

        return "redirect:/estoque/lista";
    }

    // ABRE O FORMULÁRIO DE CADASTRO
    @GetMapping("/novo")
    public String novoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "estoque/formulario-produto";
    }

    // SALVA O PRODUTO
    @PostMapping("/salvar")
    public String salvarProduto(Produto produto) {
        if (produto.getQuantidadeAtual() < 0) {
            produto.setQuantidadeAtual(0);
        }
        produtoRepository.save(produto);
        return "redirect:/estoque/lista";
    }

    // ABRE O FORMULÁRIO PARA EDIÇÃO
    @GetMapping("/editar/{id}")
    public String editarProduto(@PathVariable Long id, Model model) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ID inválido: " + id));
        model.addAttribute("produto", produto);
        return "estoque/formulario-produto";
    }

    // --- CORREÇÃO: Tratamento de erro na exclusão (Chave Estrangeira) ---
    @GetMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            produtoRepository.deleteById(id);
            attributes.addFlashAttribute("mensagemSucesso", "Produto excluído do estoque com sucesso.");
        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("mensagemErro", "Não é possível excluir este produto pois ele já possui histórico de entradas ou saídas vinculadas. A exclusão afetaria os relatórios contábeis da igreja.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Ocorreu um erro inesperado ao tentar excluir o produto.");
        }
        return "redirect:/estoque/lista";
    }

    @GetMapping("/cesta")
    public String montarCesta(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("familias", familiaRepository.findAll());
        return "estoque/entrega-cesta";
    }

    @PostMapping("/salvar-cesta")
    public String salvarCesta(@RequestParam Long familiaId,
                              @RequestParam Map<String, String> allParams) {

        var familia = familiaRepository.findById(familiaId)
                .orElseThrow(() -> new IllegalArgumentException("Família inválida"));

        String obsGeral = allParams.get("observacao");

        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            String key = entry.getKey();

            if (key.startsWith("qtds[")) {
                try {
                    String idStr = key.substring(key.indexOf("[") + 1, key.indexOf("]"));
                    Long produtoId = Long.parseLong(idStr);
                    int quantidade = Integer.parseInt(entry.getValue());

                    if (quantidade > 0) {
                        Produto produto = produtoRepository.findById(produtoId).orElse(null);

                        if (produto != null && produto.getQuantidadeAtual() >= quantidade) {
                            // Baixa estoque
                            produto.setQuantidadeAtual(produto.getQuantidadeAtual() - quantidade);
                            produtoRepository.save(produto);

                            // Gera histórico
                            MovimentacaoEstoque mov = new MovimentacaoEstoque();
                            mov.setProduto(produto);
                            mov.setFamiliaAssistida(familia);
                            mov.setQuantidade(quantidade);
                            mov.setTipo(TipoMovimentacao.SAIDA);
                            mov.setDataHora(LocalDateTime.now());
                            mov.setObservacao("Item de Cesta Básica. " + (obsGeral != null ? obsGeral : ""));

                            movimentacaoRepository.save(mov);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return "redirect:/familias/" + familiaId + "/historico";
    }
}