package com.sistemajuridico.backend.infrastructure.document;

import com.sistemajuridico.backend.core.domain.Cliente;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class MockDocumentGeneratorService implements DocumentGeneratorService {

    @Override
    public byte[] gerarProcuracao(Cliente cliente, String acao, String varaCivel, String comarca, boolean imprimirDeclaracao) {
        StringBuilder sb = new StringBuilder();

        String nome = cliente.getNome() != null ? cliente.getNome().toUpperCase() : "NOME COMPLETO";
        String estadoCivil = cliente.getEstadoCivil() != null ? cliente.getEstadoCivil().name().toLowerCase() : "estado civil";
        String profissao = cliente.getProfissao() != null ? cliente.getProfissao() : "profissão";
        String cpf = cliente.getCpfCnpj() != null ? cliente.getCpfCnpj() : "___________";
        String rua = cliente.getLogradouro() != null ? cliente.getLogradouro() : "___________";
        String numero = cliente.getNumero() != null ? cliente.getNumero() : "___";
        String bairro = cliente.getBairro() != null ? cliente.getBairro() : "___________";
        String cidade = cliente.getCidade() != null ? cliente.getCidade() : "Ijuí";
        String uf = cliente.getUf() != null ? cliente.getUf() : "RS";

        sb.append("                              PROCURAÇÃO\n\n\n");
        
        sb.append("OUTORGANTE: ").append(nome).append(", brasileiro(a), ").append(estadoCivil)
          .append(", ").append(profissao).append(", inscrito(a) no CPF sob o nº ").append(cpf)
          .append(", RG nº _________, residente e domiciliado(a) na ").append(rua)
          .append(", nº ").append(numero).append(", Bairro ").append(bairro)
          .append(" na cidade de ").append(cidade).append("/").append(uf).append(".\n\n");

        sb.append("OUTORGADO(S): Constitui e nomeia seu procurador CRISTHIAN MENEZES DE JEZUS, ");
        sb.append("inscrito no CPF sob o nº 039.623.600-69, brasileiro, casado, advogado inscrito ");
        sb.append("na OAB/RS sob o nº 121.837, com escritório profissional na Rua Tiradentes, nº 676, ");
        sb.append("Centro, na cidade de Ijuí/RS, onde recebem avisos e intimações;\n\n");

        String acaoTexto = "XX";
        if (acao != null && !acao.trim().isEmpty()) {
            acaoTexto = acao.trim();
        }

        String varaCivelTexto = "__ vara cível";
        if (varaCivel != null && !varaCivel.trim().isEmpty()) {
            varaCivelTexto = varaCivel.trim();
        }

        String textoJurisdicao;
        if (comarca != null && !comarca.trim().isEmpty()) {
            textoJurisdicao = "AJUIZAR " + acaoTexto + ", na " + varaCivelTexto + " da comarca de " + comarca.trim() + " e representá-la";
        } else {
            textoJurisdicao = "AJUIZAR " + acaoTexto + ", na " + varaCivelTexto + " e representá-la";
        }

        sb.append("PODERES: Confere os poderes especiais para, em nome do Outorgante, ");
        sb.append(textoJurisdicao).append(", quando se fizer necessário para defender os interesses do outorgante, ");
        sb.append("utilizando-se para tanto de todos os poderes necessários para o foro em geral, com cláusula ");
        sb.append("\"AD JUDITIA\" e \"ET EXTRA\", para propor quaisquer ações na defesa dos interesses do constituinte, ");
        sb.append("e ainda os PODERES ESPECIAIS para transigir, acordar, receber valores, dar quitação, desistir, ");
        sb.append("firmar recibos, interpor recursos, ratificar, medidas preparatórias, concordar ou não com dívidas e avaliações ");
        sb.append("em arrolamento de bens e ou inventários, e, praticar todos os atos que se fizerem preciso para o integral cumprimento do mandato.\n\n\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        sb.append(cidade).append("/").append(uf).append(", ").append(LocalDate.now().format(formatter)).append(".\n\n\n");

        sb.append("_____________________________________________________\n");
        sb.append(nome).append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] gerarContratoHonorarios(Cliente cliente, String acao, String vara, String comarca, String valorServicos, String objetivoDemanda) {
        String nome = "NOME COMPLETO";
        if (cliente != null && cliente.getNome() != null) {
            nome = cliente.getNome().toUpperCase();
        }

        String cpfCnpj = "___________";
        if (cliente != null && cliente.getCpfCnpj() != null) {
            cpfCnpj = cliente.getCpfCnpj();
        }

        String acaoTexto = "ação judicial";
        if (acao != null && !acao.trim().isEmpty()) {
            acaoTexto = acao.trim();
        }

        String valorTexto = "conforme estipulado";
        if (valorServicos != null && !valorServicos.trim().isEmpty()) {
            valorTexto = valorServicos.trim();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("   CONTRATO DE PRESTAÇÃO DE SERVIÇOS E HONORÁRIOS    \n");
        sb.append("=====================================================\n\n");
        sb.append("CONTRATANTE:\n");
        sb.append("Nome: ").append(nome).append("\n");
        sb.append("CPF/CNPJ: ").append(cpfCnpj).append("\n\n");
        sb.append("CONTRATADO:\n");
        sb.append("CRISTHIAN MENEZES DE JEZUS, Advogado OAB/RS 121.837\n\n");
        sb.append("CLÁUSULA PRIMEIRA - DO OBJETO:\n");
        sb.append("O CONTRATADO prestará serviços advocatícios na defesa dos interesses jurídicos do CONTRATANTE na ação: ").append(acaoTexto).append(".\n\n");
        sb.append("CLÁUSULA SEGUNDA - DOS HONORÁRIOS:\n");
        sb.append("Pelos serviços prestados, o CONTRATANTE pagará ao CONTRATADO os honorários de: ").append(valorTexto).append(".\n\n");
        sb.append("Data: ").append(LocalDate.now()).append("\n\n\n");
        sb.append("_____________________________________________________\n");
        sb.append(nome).append("\n\n");
        sb.append("_____________________________________________________\n");
        sb.append("CRISTHIAN MENEZES DE JEZUS\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
