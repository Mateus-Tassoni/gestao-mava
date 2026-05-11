package com.igreja.gestao.controller;

import com.igreja.gestao.model.Membro;
import com.igreja.gestao.model.enums.EstadoCivil;
import com.igreja.gestao.repository.MembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
public class MembroController {

    @Autowired
    private MembroRepository repository;

    @GetMapping("/membros")
    public String listarMembros(@RequestParam(value = "busca", required = false) String busca, Model model) {
        List<Membro> lista;
        if (busca != null && !busca.isEmpty()) {
            lista = repository.buscarPorNomeOrdemAlfabeticaForcada(busca);
        } else {
            lista = repository.buscarTodosOrdemAlfabeticaForcada();
        }
        model.addAttribute("membros", lista);
        model.addAttribute("busca", busca);
        return "membros/lista";
    }

    @GetMapping("/membros/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("membro", new Membro());
        model.addAttribute("todosEstadosCivis", EstadoCivil.values());
        model.addAttribute("cargos", Membro.Cargo.values());
        model.addAttribute("statusList", Membro.StatusMembro.values());
        return "membros/formulario";
    }

    @PostMapping("/membros/salvar")
    public String salvarMembro(Membro membro, @RequestParam("file") MultipartFile file, RedirectAttributes attributes) {
        try {
            if (membro.getNome() != null) {
                membro.setNome(membro.getNome().trim());
            }

            // --- INÍCIO DA TRAVA DE DUPLICIDADE ---
            // Verifica apenas se for um NOVO cadastro (ID vazio)
            if (membro.getId() == null) {

                // Trava do CPF
                if (membro.getCpf() != null && !membro.getCpf().trim().isEmpty()) {
                    if (repository.existsByCpf(membro.getCpf())) {
                        attributes.addFlashAttribute("mensagemErro", "Cadastro bloqueado: Já existe um membro com este CPF.");
                        return "redirect:/membros/novo";
                    }
                }

                // Trava do Telefone
                if (membro.getTelefone() != null && !membro.getTelefone().trim().isEmpty()) {
                    if (repository.existsByTelefone(membro.getTelefone())) {
                        attributes.addFlashAttribute("mensagemErro", "Cadastro bloqueado: Já existe um membro com este Telefone/WhatsApp.");
                        return "redirect:/membros/novo";
                    }
                }
            }
            // --- FIM DA TRAVA ---

            membro = repository.save(membro);

            if (!file.isEmpty()) {
                String pastaUploads = "C:/igreja/uploads/";
                File diretorio = new File(pastaUploads);
                if (!diretorio.exists()) diretorio.mkdirs();

                String nomeArquivo = membro.getId() + "_" + file.getOriginalFilename();
                Path caminhoDestino = Paths.get(pastaUploads + nomeArquivo);
                Files.copy(file.getInputStream(), caminhoDestino, StandardCopyOption.REPLACE_EXISTING);

                membro.setFotoUrl(nomeArquivo);
                repository.save(membro);
                avisarPythonParaRecarregar();
            }

            attributes.addFlashAttribute("mensagemSucesso", "Membro salvo com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
            attributes.addFlashAttribute("mensagemErro", "Erro ao salvar a foto do membro.");
        }
        return "redirect:/membros";
    }

    private void avisarPythonParaRecarregar() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String urlPython = "http://localhost:5000/reload";
            restTemplate.postForEntity(urlPython, null, String.class);
        } catch (Exception e) {
            System.out.println(">>> AVISO: Não consegui avisar o Python.");
        }
    }

    @GetMapping("/membros/editar/{id}")
    public String editarMembro(@PathVariable Long id, Model model) {
        Membro membro = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Inválido: " + id));
        model.addAttribute("membro", membro);
        model.addAttribute("todosEstadosCivis", EstadoCivil.values());
        model.addAttribute("cargos", Membro.Cargo.values());
        model.addAttribute("statusList", Membro.StatusMembro.values());
        return "membros/formulario";
    }

    @GetMapping("/membros/excluir/{id}")
    public String excluirMembro(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            repository.deleteById(id);
            avisarPythonParaRecarregar();
            attributes.addFlashAttribute("mensagemSucesso", "Cadastro excluído.");
        } catch (DataIntegrityViolationException e) {
            attributes.addFlashAttribute("mensagemErro", "Ação Bloqueada: Histórico vinculado.");
        } catch (Exception e) {
            attributes.addFlashAttribute("mensagemErro", "Erro interno.");
        }
        return "redirect:/membros";
    }
}