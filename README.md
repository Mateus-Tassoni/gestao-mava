# Sistema de Gestão - Igreja Manancial

Sistema de gestão para igreja com módulos de membros, presença, eventos, kids, tesouraria, estoque e mais.

## Pré-requisitos

- **Java 17**
- **Docker** (para o banco PostgreSQL)
- **Maven** ou uso do Maven Wrapper (incluído no projeto)

## Configuração inicial

### 1. Clone o repositório

```bash
git clone <url-do-repositorio>
cd gestao
```

### 2. (Opcional) Credenciais

Em **desenvolvimento** o banco usa valores fixos: usuário `gestao`, senha `gestao123`, banco `gestaodb`. Não é preciso criar `.env` para rodar.

Se quiser outra senha, copie `.env.example` para `.env` e ajuste. O arquivo `.env` não é versionado.

### 3. Suba o banco de dados

Com o Docker rodando, execute:

```bash
docker compose up -d
```

O PostgreSQL ficará disponível na porta 5432.

## Como rodar a aplicação

### Opção 1: Script (recomendado)

O script carrega o `.env` e inicia a aplicação. Não precisa ter Maven instalado.

**Windows (PowerShell):**
```powershell
.\run.ps1
```

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

### Opção 2: Maven Wrapper

```bash
# Carregue as variáveis do .env antes (ou use o run.ps1/run.sh)
.\mvnw.cmd spring-boot:run   # Windows
./mvnw spring-boot:run      # Linux/Mac
```

### Opção 3: IntelliJ / IDE

1. Configure as variáveis de ambiente na Run Configuration:
   - `POSTGRES_DB=gestaodb`
   - `POSTGRES_USER=gestao`
   - `POSTGRES_PASSWORD=<sua_senha>`

2. Execute a classe `GestaoApplication` ou a configuração Maven `spring-boot:run`.

## Acesso

Após iniciar, a aplicação estará em:

- **URL:** http://localhost:8080

## Comandos úteis

| Comando | Descrição |
|---------|-----------|
| `docker compose up -d` | Sobe o PostgreSQL em background |
| `docker compose down` | Para o PostgreSQL |
| `docker compose logs -f postgres` | Ver logs do banco |
| `.\mvnw.cmd clean` | Limpa o build (resolve conflitos após mudar packages) |
| `.\mvnw.cmd test` | Executa os testes |

## Estrutura

- `src/main/java` - Código da aplicação
- `src/main/resources` - Configurações, templates, scripts SQL
- `docker-compose.yml` - PostgreSQL
- `.env.example` - Modelo de credenciais

## Problemas comuns

**Erro: "autenticação do tipo senha falhou para o usuário 'gestao'"**

O banco em dev usa **senha fixa `gestao123`**. Se o container foi criado antes com outra configuração, recrie:

```bash
docker compose down -v
docker compose up -d
```

Depois rode a aplicação (`.\run.ps1` ou pela IDE — a app usa a senha `gestao123` por padrão).

**Porta 8080 em uso**

```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Erro de bean duplicado após mover classes**

```bash
.\mvnw.cmd clean
```
