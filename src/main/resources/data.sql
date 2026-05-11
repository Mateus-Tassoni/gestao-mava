-- src/main/resources/data.sql

-- 1. Inserir Membros (Para serem os líderes)
-- Ajuste os campos conforme sua tabela 'membro' exata
INSERT INTO membro (nome, telefone, cargo, status) VALUES ('Mateus Tassoni', '11995672017', 'MUSICO', 'ATIVO');

-- 2. Inserir Ministérios (Tabela 'ministerio')
INSERT INTO ministerio (nome) VALUES ('Som');
INSERT INTO ministerio (nome) VALUES ('Louvor');
INSERT INTO ministerio (nome) VALUES ('Recepção');
INSERT INTO ministerio (nome) VALUES ('Mídia/Projeção');

-- 3. Inserir Tipos de Evento (CORRIGIDO: Tabela 'tipos_evento' no plural)
INSERT INTO tipos_evento (nome) VALUES ('Culto de Celebração');
INSERT INTO tipos_evento (nome) VALUES ('Reunião de Oração');

-- 4. Definir o que o 'Culto de Celebração' (ID 1) precisa
-- (Tabela 'padrao_estrutura_evento' definida no Java)
INSERT INTO padrao_estrutura_evento (tipo_evento_id, ministerio_id) VALUES (1, 1);
INSERT INTO padrao_estrutura_evento (tipo_evento_id, ministerio_id) VALUES (1, 2);
INSERT INTO padrao_estrutura_evento (tipo_evento_id, ministerio_id) VALUES (1, 4);

-- 5. Definir o que a 'Reunião de Oração' (ID 2) precisa
INSERT INTO padrao_estrutura_evento (tipo_evento_id, ministerio_id) VALUES (2, 1);

-- 6. Definir as EQUIPES (Quem são os Líderes que recebem o Zap)
-- (Tabela 'equipes_ministerio' definida no Java)
-- Carlos (ID 1) é LÍDER do Som (ID 1)
INSERT INTO equipes_ministerio (membro_id, ministerio_id, funcao) VALUES (1, 1, 'LIDER');

-- 7. Inserir Lançamentos de Teste (Tesouraria)
-- Entradas (Dízimos e Ofertas)
INSERT INTO lancamento (descricao, valor, tipo, data) VALUES ('Dízimo Carlos', 250.00, 'DIZIMO', '2026-01-20');
INSERT INTO lancamento (descricao, valor, tipo, data) VALUES ('Oferta Culto Domingo', 1200.00, 'OFERTA', '2026-01-25');

-- Saídas (Contas)
INSERT INTO lancamento (descricao, valor, tipo, data) VALUES ('Conta de Água', 85.50, 'AGUA_LUZ', '2026-01-22');
INSERT INTO lancamento (descricao, valor, tipo, data) VALUES ('Aluguel Salão', 800.00, 'ALUGUEL', '2026-01-10');

-- Mateus (ID 2) é LÍDER do Louvor (ID 2)
INSERT INTO equipes_ministerio (membro_id, ministerio_id, funcao) VALUES (1, 2, 'LIDER');