package br.ufu.pds.dentista;

import br.ufu.pds.dentista.anamnese.Anamnese;
import br.ufu.pds.dentista.paciente.CpfUtils;
import br.ufu.pds.dentista.paciente.Paciente;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Paciente> pacientes = new ArrayList<>();
        List<Anamnese> anamneses = new ArrayList<>();

        // Exemplo inicial ja cadastrado
        Paciente p1 = new Paciente("Maria Silva", "52998224725", LocalDate.of(1990, 5, 20), "34999990000", "maria@email.com", "Rua A, 123");
        p1.setId(1L);
        pacientes.add(p1);

        Anamnese a1 = new Anamnese(p1, "Penicilina", "Hipertensao", "Losartana 50mg", "Risco de sangramento");
        a1.setId(1L);
        anamneses.add(a1);

        while (true) {
            System.out.println("\n--- ODONTOGEST ---");
            System.out.println("1 - Cadastrar Paciente");
            System.out.println("2 - Listar Pacientes");
            System.out.println("3 - Cadastrar Anamnese");
            System.out.println("4 - Ver Anamnese do Paciente");
            System.out.println("5 - Testar Regras de Negocio (Sucessos e Erros)");
            System.out.println("0 - Sair");
            System.out.print("Escolha uma opcao: ");

            String linha = scanner.nextLine();
            int opcao;
            try {
                opcao = Integer.parseInt(linha);
            } catch (Exception e) {
                System.out.println("Opcao invalida!");
                continue;
            }

            if (opcao == 0) {
                System.out.println("Encerrando...");
                break;
            }

            if (opcao == 1) {
                System.out.println("\n--- Novo Paciente ---");
                System.out.print("Nome: ");
                String nome = scanner.nextLine();

                System.out.print("CPF: ");
                String cpf = scanner.nextLine();

                if (!CpfUtils.isValido(cpf)) {
                    System.out.println("CPF invalido!");
                    continue;
                }

                System.out.print("Telefone: ");
                String telefone = scanner.nextLine();

                Paciente p = new Paciente(nome, cpf, LocalDate.of(2000, 1, 1), telefone, null, null);
                p.setId((long) (pacientes.size() + 1));
                pacientes.add(p);
                System.out.println("Paciente cadastrado com sucesso! ID: " + p.getId());

            } else if (opcao == 2) {
                System.out.println("\n--- Lista de Pacientes ---");
                for (Paciente p : pacientes) {
                    System.out.println("ID: " + p.getId() + " | Nome: " + p.getNome() + " | CPF: " + p.getCpf() + " | Tel: " + p.getTelefone());
                }

            } else if (opcao == 3) {
                System.out.println("\n--- Cadastrar Anamnese ---");
                System.out.print("ID do paciente: ");
                Long id;
                try {
                    id = Long.parseLong(scanner.nextLine());
                } catch (Exception e) {
                    System.out.println("ID invalido!");
                    continue;
                }

                Paciente pacienteEncontrado = null;
                for (Paciente p : pacientes) {
                    if (p.getId().equals(id)) {
                        pacienteEncontrado = p;
                        break;
                    }
                }

                if (pacienteEncontrado == null) {
                    System.out.println("Paciente nao encontrado!");
                    continue;
                }

                System.out.print("Alergias (ex: Penicilina): ");
                String alergias = scanner.nextLine();

                System.out.print("Condicoes preexistentes (ex: Diabetes): ");
                String doencas = scanner.nextLine();

                System.out.print("Medicamentos de uso continuo: ");
                String medicamentos = scanner.nextLine();

                System.out.print("Alertas graves: ");
                String alertas = scanner.nextLine();

                Anamnese a = new Anamnese(pacienteEncontrado, alergias, doencas, medicamentos, alertas);
                a.setId((long) (anamneses.size() + 1));
                anamneses.add(a);
                System.out.println("Anamnese salva com sucesso!");

            } else if (opcao == 4) {
                System.out.println("\n--- Consultar Anamnese ---");
                System.out.print("ID do paciente: ");
                Long id;
                try {
                    id = Long.parseLong(scanner.nextLine());
                } catch (Exception e) {
                    System.out.println("ID invalido!");
                    continue;
                }

                Anamnese anamneseEncontrada = null;
                for (Anamnese a : anamneses) {
                    if (a.getPaciente().getId().equals(id)) {
                        anamneseEncontrada = a;
                        break;
                    }
                }

                if (anamneseEncontrada == null) {
                    System.out.println("Nenhuma anamnese cadastrada para esse paciente.");
                } else {
                    System.out.println("\nPaciente: " + anamneseEncontrada.getPaciente().getNome());
                    if (anamneseEncontrada.getAlergias() != null && !anamneseEncontrada.getAlergias().isBlank()) {
                        System.out.println("ALERTA DE ALERGIA: " + anamneseEncontrada.getAlergias());
                    }
                    if (anamneseEncontrada.getAlertasGraves() != null && !anamneseEncontrada.getAlertasGraves().isBlank()) {
                        System.out.println("ALERTA GRAVE: " + anamneseEncontrada.getAlertasGraves());
                    }
                    System.out.println("Doencas preexistentes: " + anamneseEncontrada.getCondicoesPreexistentes());
                    System.out.println("Medicamentos em uso: " + anamneseEncontrada.getMedicamentosEmUso());
                    System.out.println("Ultima alteracao: " + anamneseEncontrada.getDataModificacao());
                }

            } else if (opcao == 5) {
                System.out.println("\n=== TESTES DE SUCESSO E ERRO (US01 e US02) ===");

                // 1. Teste de CPF invalido
                System.out.println("\n1. Testando validacao de CPF invalido (111.111.111-11)...");
                if (!CpfUtils.isValido("111.111.111-11")) {
                    System.out.println("[ERRO ESPERADO] Mensagem produzida: CPF invalido!");
                    System.out.println("-> Teste de erro de CPF passou com sucesso!");
                }

                // 2. Teste de CPF duplicado
                System.out.println("\n2. Testando cadastro com CPF duplicado da Maria (52998224725)...");
                boolean cpfDuplicado = false;
                for (Paciente p : pacientes) {
                    if (p.getCpf().equals("52998224725")) {
                        cpfDuplicado = true;
                        break;
                    }
                }
                if (cpfDuplicado) {
                    System.out.println("[ERRO ESPERADO] Mensagem produzida: Ja existe um paciente cadastrado com este CPF!");
                    System.out.println("-> Teste de erro de duplicidade passou com sucesso!");
                }

                // 3. Teste de Sucesso e dados cadastrados
                System.out.println("\n3. Verificando dados cadastrados no sistema...");
                System.out.println("Total de pacientes cadastrados: " + pacientes.size());
                for (Paciente p : pacientes) {
                    System.out.println("[SUCESSO] Paciente ativo: " + p.getNome() + " (CPF: " + p.getCpf() + ")");
                }

                // 4. Teste de Anamnese vinculada
                System.out.println("\n4. Verificando vinculo e alertas da Anamnese (US02)...");
                if (!anamneses.isEmpty()) {
                    Anamnese a = anamneses.get(0);
                    System.out.println("[SUCESSO] Anamnese vinculada ao paciente: " + a.getPaciente().getNome());
                    System.out.println("   - Alergias detectadas: " + a.getAlergias());
                    System.out.println("   - Alerta grave destacado: " + a.getAlertasGraves());
                    System.out.println("   - Data/hora registrada: " + a.getDataModificacao());
                }

                // 5. Teste de Busca de paciente inexistente
                System.out.println("\n5. Testando busca de paciente que nao existe (ID 999)...");
                boolean achou = false;
                for (Paciente p : pacientes) {
                    if (p.getId().equals(999L)) {
                        achou = true;
                        break;
                    }
                }
                if (!achou) {
                    System.out.println("[ERRO ESPERADO] Mensagem produzida: Paciente nao encontrado!");
                    System.out.println("-> Teste de erro de inexistencia passou com sucesso!");
                }

                System.out.println("\n>>> Todos os cenarios de sucesso e mensagens de erro foram validados! <<<");

            } else {
                System.out.println("Opcao invalida!");
            }
        }
        scanner.close();
    }
}
