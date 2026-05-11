(function() {
    'use strict';

    // 1. CSS PARA BLOQUEAR SELEÇÃO E APLICAR BLUR
    const style = document.createElement('style');
    style.innerHTML = `
        /* Proíbe selecionar texto com o mouse */
        body {
            -webkit-user-select: none; /* Safari */
            -ms-user-select: none; /* IE 10 and IE 11 */
            user-select: none; /* Standard syntax */
        }

        /* Classe que embaça a tela */
        .sistema-blur {
            filter: blur(20px) !important;
            background-color: #f0f2f5; /* Fundo cinza para cobrir dados */
        }

        /* Esconde tudo na impressão (Ctrl+P) */
        @media print {
            html, body { display: none !important; }
        }
    `;
    document.head.appendChild(style);

    // 2. BLOQUEIO CONTRA "FERRAMENTA DE CAPTURA" / SNIPPING TOOL
    window.addEventListener('blur', () => {
        document.body.classList.add('sistema-blur');
        document.title = "⚠️ PROTEGIDO"; // Muda o nome da aba
    });

    window.addEventListener('focus', () => {
        document.body.classList.remove('sistema-blur');
        document.title = "Portal Mava";
    });

    // 3. BLOQUEIO DE TECLA PRINT SCREEN (Tentativa de limpar clipboard)
    document.addEventListener('keyup', (e) => {
        if (e.key === 'PrintScreen' || e.keyCode === 44) {
            // Tenta limpar o clipboard jogando um texto vazio
            navigator.clipboard.writeText(' ');

            // Pisca a tela para atrapalhar
            document.body.style.display = 'none';
            setTimeout(() => document.body.style.display = 'block', 100);

            alert('🚫 Captura de tela não permitida nesta área.');
        }
    });

    // 4. BLOQUEIO DE BOTÃO DIREITO
    document.addEventListener('contextmenu', event => event.preventDefault());

    // 5. BLOQUEIO DE TECLAS DE ATALHO (Ctrl+S, Ctrl+P, Ctrl+C)
    document.addEventListener('keydown', (e) => {
        if (e.ctrlKey && (e.key === 'p' || e.key === 's' || e.key === 'u' || e.key === 'c' || e.key === 'a')) {
            e.preventDefault();
        }
    });

    console.log("🛡️ Bloqueio de Seleção e Foco Ativo");
})();