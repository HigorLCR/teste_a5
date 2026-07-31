1-
```
inicio
    leia x
    leia y
    z <- x * y + 5
    se z <= 0 entao
        Resultado <- "A"
    senao
        se z <= 100 entao
            Resultado <- "B"
        senao
            Resultado <- "C"
        fim-se
    fim-se
    escrever: z, Resultado
fim
```

### Quadro preenchido

| X | Y | Z | Resultado |
|---:|---:|---:|:---|
| 3 | 2 | 11 | B |
| 150 | 3 | 455 | C |
| 7 | -1 | -2 | A |
| -2 | 5 | -5 | A |
| 50 | 3 | 155 | B |


2-

### Regras media

| Media | Resultado |
|---:|:---|
| 0 até 4,9 | Recuperação |
| 5 até 6,9 | Prova Final |
| 7 até 10 | Aprovado |

![alt text](image.png)


3- 

inserir
```sql
INSERT INTO Pessoa (Nome, DataNascimento)
VALUES ('Higor Lachini', '1999-01-01');
```

atualizar
```sql
UPDATE Pessoa
SET Nome = 'Higor Lachini Colli da Rocha',
    DataNascimento = '2000-11-04'
WHERE Id = 1;
```


4-


```sql
SELECT p.Nome,
       e.CEP,
       e.Logradouro,
       e.Bairro,
       e.Cidade,
       e.UF,
       pe.Numero,
       pe.Complemento
FROM Pessoa p
INNER JOIN Pessoa_x_Endereco pe ON pe.IdPessoa = p.Id
INNER JOIN Endereco e ON e.CEP = pe.CEP
ORDER BY p.Nome;
```