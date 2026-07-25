import urllib.request
import urllib.error
import json
import random
import time

nomes_brasileiros = [
    "Ana Silva", "Carlos Oliveira", "Fernanda Souza", "Lucas Santos", "Juliana Costa", 
    "Pedro Rocha", "Mariana Alves", "Rafael Lima", "Camila Gomes", "Bruno Martins", 
    "Amanda Ribeiro", "Thiago Carvalho", "Beatriz Mendes", "Felipe Araujo", "Leticia Cardoso", 
    "Diego Castro", "Jessica Freitas", "Rodrigo Barbosa", "Natalia Correia", "Marcelo Dias"
]

sobrenomes_email = ["silva", "oliveira", "souza", "santos", "costa", "rocha", "alves", "lima", "gomes", "martins"]
niveis_acesso = ["ADMIN", "GERENTE", "OPERADOR"]

# Rota corrigida para bater no trabalho do Amazon Q (/api/v1/usuarios)
url = "http://localhost:8080/api/v1/usuarios"

print("======================================================")
print(" 🤖 ANTIGRAVITY + AMAZON Q DATA SEEDER")
print("======================================================")
print(f"Alvo Sincronizado: {url}\n")

sucessos = 0
falhas = 0

for i in range(20):
    nome = random.choice(nomes_brasileiros)
    email = f"{nome.split()[0].lower()}.{random.choice(sobrenomes_email)}{random.randint(100,9999)}@sistemamilhao.com.br"
    nivel = random.choice(niveis_acesso)
    
    payload = {
        "nome": nome,
        "email": email,
        "nivelAcesso": nivel
    }
    
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'}, method='POST')
    
    try:
        response = urllib.request.urlopen(req)
        print(f"[+] INJEÇÃO SUCESSO: Usuário {nome} ({nivel}) cadastrado com e-mail: {email}.")
        sucessos += 1
    except urllib.error.HTTPError as e:
        print(f"[-] FALHA NA INJEÇÃO: Não foi possível cadastrar {nome}. Código HTTP: {e.code}.")
        falhas += 1
    except urllib.error.URLError as e:
        print(f"\n[!] ALERTA: O Servidor Java (Gradle/Amazon Q) parece estar offline.")
        print(f"Detalhes técnicos: {e.reason}")
        print("-> AÇÃO: Dê './gradlew bootRun' no assistente-voz-java!")
        break
        
    time.sleep(0.3)

print("\n======================================================")
print(f"✅ RELATÓRIO DO ROBÔ: {sucessos} cadastros realizados com sucesso. {falhas} falhas.")
print("======================================================")
