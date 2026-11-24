$base = "http://localhost:8080/api"
$ErrorActionPreference = "Stop"

function Requisicao($metodo, $rota, $dados) {
    $params = @{
        Method = $metodo
        Uri = "$base$rota"
        ContentType = "application/json"
    }

    if ($null -ne $dados) {
        $params.Body = ConvertTo-Json -InputObject $dados -Depth 10
    }

    Invoke-RestMethod @params
}

while ($true) {
    Write-Host "`n========== ODONTOGEST ==========" -ForegroundColor Cyan
    Write-Host "1 - Listar pacientes"
    Write-Host "2 - Criar plano de tratamento"
    Write-Host "3 - Adicionar etapa"
    Write-Host "4 - Consultar plano"
    Write-Host "5 - Reordenar etapas"
    Write-Host "6 - Remover etapa"
    Write-Host "7 - Alterar status do plano"
    Write-Host "8 - Criar agendamento vinculado"
    Write-Host "9 - Consultar agendamento"
    Write-Host "0 - Sair"

    $opcao = Read-Host "Escolha uma opcao"

    if ($opcao -eq "0") { break }

    try {
        $resultado = switch ($opcao) {
            "1" {
                Requisicao "GET" "/pacientes" $null
            }
            "2" {
                $paciente = [long](Read-Host "ID do paciente")
                $titulo = Read-Host "Titulo do plano"

                Requisicao "POST" "/planos-tratamento" @{
                    pacienteId = $paciente
                    titulo = $titulo
                }
            }
            "3" {
                $id = Read-Host "ID do plano"
                $procedimento = Read-Host "Procedimento"

                Requisicao "POST" "/planos-tratamento/$id/etapas" @{
                    procedimento = $procedimento
                }
            }
            "4" {
                $id = Read-Host "ID do plano"
                Requisicao "GET" "/planos-tratamento/$id" $null
            }
            "5" {
                $id = Read-Host "ID do plano"
                $ordem = Read-Host "IDs das etapas separados por virgula"
                $ids = @($ordem.Split(",") | ForEach-Object { [long]$_ })

                Requisicao "PUT" "/planos-tratamento/$id/etapas/ordem" $ids
            }
            "6" {
                $id = Read-Host "ID do plano"
                $etapa = Read-Host "ID da etapa"

                Requisicao "DELETE" "/planos-tratamento/$id/etapas/$etapa" $null
            }
            "7" {
                $id = Read-Host "ID do plano"
                $status = Read-Host "EM_ELABORACAO, ATIVO ou CONCLUIDO"

                Requisicao "PATCH" "/planos-tratamento/$id/status" @{
                    status = $status
                }
            }
            "8" {
                $paciente = [long](Read-Host "ID do paciente")
                $etapa = [long](Read-Host "ID da etapa")
                $inicio = Read-Host "Inicio (AAAA-MM-DDTHH:mm:ss)"
                $fim = Read-Host "Fim (AAAA-MM-DDTHH:mm:ss)"

                Requisicao "POST" "/agendamentos" @{
                    pacienteId = $paciente
                    etapaId = $etapa
                    dataHoraInicio = $inicio
                    dataHoraFim = $fim
                    observacoes = "Demonstracao US03 e US04"
                }
            }
            "9" {
                $id = Read-Host "ID do agendamento"
                Requisicao "GET" "/agendamentos/$id" $null
            }
            default {
                throw "Opcao invalida"
            }
        }

        Write-Host "`n[SUCESSO]" -ForegroundColor Green
        $resultado | ConvertTo-Json -Depth 10

    } catch {
        Write-Host "`n[ERRO]" -ForegroundColor Red
        Write-Host $_.Exception.Message
        Write-Host $_.ErrorDetails.Message
    }
}