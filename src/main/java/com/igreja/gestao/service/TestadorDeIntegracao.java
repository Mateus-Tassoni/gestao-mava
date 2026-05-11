package com.igreja.gestao.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.file.Files;

@Component
public class TestadorDeIntegracao implements CommandLineRunner {

    @Autowired
    private ReconhecimentoFacialService reconhecimentoService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==========================================");
        System.out.println("INICIANDO TESTE DE CONEXÃO COM O PYTHON...");
        System.out.println("==========================================");

        try {
            // 1. Lê a foto que colocamos no C:
            File arquivoFoto = new File("C:/teste.jpg");

            if (arquivoFoto.exists()) {
                byte[] bytesDaFoto = Files.readAllBytes(arquivoFoto.toPath());

                // 2. Chama o serviço
                System.out.println("Enviando foto para o motor de IA...");
                String resultado = reconhecimentoService.verificarPresenca(bytesDaFoto);

                // 3. Mostra o resultado
                System.out.println(">>> RESPOSTA DO PYTHON: " + resultado);
            } else {
                System.err.println("ERRO: Não achei o arquivo C:/teste.jpg. Crie ele para testar!");
            }

        } catch (Exception e) {
            System.err.println("ERRO FATAL NO TESTE: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("==========================================");
        System.out.println("FIM DO TESTE");
        System.out.println("==========================================");
    }
}
