package com.sistemajuridico.backend.infrastructure.document;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.sistemajuridico.backend.core.domain.Cliente;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Primary
public class PdfDocumentGeneratorService implements DocumentGeneratorService {

    @Override
    public byte[] gerarProcuracao(Cliente cliente, String acao, String varaCivel, String comarca) {
        FontFactory.registerDirectories();

        String nome = "____________________";
        if (cliente != null && cliente.getNome() != null && !cliente.getNome().trim().isEmpty()) {
            nome = cliente.getNome().toUpperCase();
        }

        String estadoCivil = "estado civil";
        if (cliente != null && cliente.getEstadoCivil() != null) {
            estadoCivil = cliente.getEstadoCivil().name().toLowerCase();
        }

        String profissao = "profissão";
        if (cliente != null && cliente.getProfissao() != null && !cliente.getProfissao().trim().isEmpty()) {
            profissao = cliente.getProfissao();
        }

        String cpf = "___________";
        if (cliente != null && cliente.getCpfCnpj() != null && !cliente.getCpfCnpj().trim().isEmpty()) {
            String docLimpo = cliente.getCpfCnpj().replaceAll("\\D", "");
            if (docLimpo.length() == 11) {
                cpf = docLimpo.substring(0, 3) + "." + docLimpo.substring(3, 6) + "." + docLimpo.substring(6, 9) + "-" + docLimpo.substring(9, 11);
            } else if (docLimpo.length() == 14) {
                cpf = docLimpo.substring(0, 2) + "." + docLimpo.substring(2, 5) + "." + docLimpo.substring(5, 8) + "/" + docLimpo.substring(8, 12) + "-" + docLimpo.substring(12, 14);
            } else {
                cpf = cliente.getCpfCnpj().trim();
            }
        }

        String logradouro = "___________";
        if (cliente != null && cliente.getLogradouro() != null && !cliente.getLogradouro().trim().isEmpty()) {
            logradouro = cliente.getLogradouro();
        }

        String numero = "___";
        if (cliente != null && cliente.getNumero() != null && !cliente.getNumero().trim().isEmpty()) {
            numero = cliente.getNumero();
        }

        String bairro = "___________";
        if (cliente != null && cliente.getBairro() != null && !cliente.getBairro().trim().isEmpty()) {
            bairro = cliente.getBairro();
        }

        String cidade = "Ijuí";
        if (cliente != null && cliente.getCidade() != null && !cliente.getCidade().trim().isEmpty()) {
            cidade = cliente.getCidade();
        }

        String uf = "RS";
        if (cliente != null && cliente.getUf() != null && !cliente.getUf().trim().isEmpty()) {
            uf = cliente.getUf();
        }

        String acaoTexto = "AÇÃO JUDICIAL";
        if (acao != null && !acao.trim().isEmpty()) {
            acaoTexto = acao.trim();
        }

        String varaCivelTexto = "____ vara cível";
        if (varaCivel != null && !varaCivel.trim().isEmpty()) {
            varaCivelTexto = varaCivel.trim();
        }

        String textoJurisdicaoComplemento;
        if (comarca != null && !comarca.trim().isEmpty()) {
            textoJurisdicaoComplemento = ", na " + varaCivelTexto + " da comarca de " + comarca.trim() + " e representá-la";
        } else {
            textoJurisdicaoComplemento = ", na " + varaCivelTexto + " e representá-la";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataFormatada = LocalDate.now().format(formatter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 70, 70, 20, 40);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont("Bookman Old Style", 14, Font.BOLD);
            Font fontSubtitulo = FontFactory.getFont("Bookman Old Style", 12, Font.BOLD);
            Font fontNegrito = FontFactory.getFont("Bookman Old Style", 11, Font.BOLD);
            Font fontTexto = FontFactory.getFont("Bookman Old Style", 11, Font.NORMAL);
            Font fontRodape = FontFactory.getFont("Bookman Old Style", 8, Font.NORMAL);

            java.net.URL logoUrl = getClass().getResource("/logo.png");

            // ==========================================
            // PÁGINA 1: PROCURAÇÃO
            // ==========================================
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(110, 110);
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.setSpacingAfter(30);
                document.add(logo);
            }

            Paragraph tituloProcuracao = new Paragraph("PROCURAÇÃO", fontTitulo);
            tituloProcuracao.setAlignment(Element.ALIGN_CENTER);
            tituloProcuracao.setSpacingAfter(15);
            document.add(tituloProcuracao);

            // OUTORGANTE
            Paragraph outorgante = new Paragraph();
            outorgante.setLeading(16f);
            outorgante.setAlignment(Element.ALIGN_JUSTIFIED);
            outorgante.setSpacingAfter(10);
            outorgante.add(new Chunk("OUTORGANTE: ", fontNegrito));
            outorgante.add(new Chunk(nome, fontNegrito));
            outorgante.add(new Chunk(", brasileiro(a), " + estadoCivil + ", " + profissao +
                    ", inscrito(a) no CPF sob o nº " + cpf + ", residente e domiciliado(a) na " +
                    logradouro + ", nº " + numero + ", Bairro " + bairro + " na cidade de " +
                    cidade + "/" + uf + ".", fontTexto));
            document.add(outorgante);

            // OUTORGADO
            Paragraph outorgado = new Paragraph();
            outorgado.setLeading(16f);
            outorgado.setAlignment(Element.ALIGN_JUSTIFIED);
            outorgado.setSpacingAfter(10);
            outorgado.add(new Chunk("OUTORGADO(S): ", fontNegrito));
            outorgado.add(new Chunk("Constitui e nomeia seu procurador ", fontTexto));
            outorgado.add(new Chunk("CRISTHIAN MENEZES DE JEZUS", fontNegrito));
            outorgado.add(new Chunk(", inscrito no CPF sob o nº 039.623.600-69, brasileiro, casado, advogado inscrito " +
                    "na OAB/RS sob o nº 121.837, com escritório profissional na Rua Tiradentes, nº 676, " +
                    "Centro, na cidade de Ijuí/RS, onde recebe avisos e intimações.", fontTexto));
            document.add(outorgado);

            // PODERES
            Paragraph poderes = new Paragraph();
            poderes.setLeading(16f);
            poderes.setAlignment(Element.ALIGN_JUSTIFIED);
            poderes.setSpacingAfter(10);
            poderes.add(new Chunk("PODERES: ", fontNegrito));
            poderes.add(new Chunk("Confere os poderes especiais para, em nome do Outorgante, ", fontTexto));
            poderes.add(new Chunk("AJUIZAR " + acaoTexto, fontNegrito));
            poderes.add(new Chunk(textoJurisdicaoComplemento + ", quando se fizer necessário para defender os interesses do outorgante, " +
                    "utilizando-se para tanto de todos os poderes necessários para o foro em geral, com cláusula ", fontTexto));
            
            Chunk chunkAdJuditia = new Chunk("\"AD JUDITIA\"", fontNegrito);
            chunkAdJuditia.setUnderline(1f, -2f);
            poderes.add(chunkAdJuditia);

            poderes.add(new Chunk(" e ", fontTexto));

            Chunk chunkEtExtra = new Chunk("\"ET EXTRA\"", fontNegrito);
            chunkEtExtra.setUnderline(1f, -2f);
            poderes.add(chunkEtExtra);

            poderes.add(new Chunk(", para propor quaisquer ações na defesa dos interesses do constituinte, e ainda os ", fontTexto));
            poderes.add(new Chunk("PODERES ESPECIAIS", fontNegrito));
            poderes.add(new Chunk(" para transigir, acordar, receber valores, dar quitação, desistir, " +
                    "firmar recibos, interpor recursos, ratificar, medidas preparatórias, concordar ou não com dívidas e avaliações " +
                    "em arrolamento de bens e ou inventários, e, praticar todos os atos que se fizerem preciso para o integral cumprimento do mandato, " +
                    "como se expressamente declarados fossem, com poderes especiais substabelecer, no todo ou em parte os poderes aqui contidos, " +
                    "com ou sem reserva dos mesmos. Igualmente para representar o outorgante perante quaisquer repartições ou órgãos públicos, " +
                    "quer seja Municipal, Estadual ou Federal, podendo praticar todos os atos acima mencionados.", fontTexto));
            document.add(poderes);

            // HONORÁRIOS
            Paragraph honorarios = new Paragraph();
            honorarios.setLeading(16f);
            honorarios.setAlignment(Element.ALIGN_JUSTIFIED);
            honorarios.setSpacingAfter(15);
            honorarios.add(new Chunk("Fica estabelecido, desde logo, que os honorários profissionais, se não contratados especificamente em separado, serão aqueles previstos na Resolução nº 02/2015 do Conselho Seccional da OAB/RS, pagáveis em Ijuí/RS.", fontTexto));
            document.add(honorarios);

            // DATA E ASSINATURA PÁGINA 1
            Paragraph dataP1 = new Paragraph(cidade + "/" + uf + ", " + dataFormatada + ".", fontTexto);
            dataP1.setAlignment(Element.ALIGN_CENTER);
            dataP1.setSpacingAfter(35);
            document.add(dataP1);

            Paragraph linhaAssinaturaP1 = new Paragraph("_____________________________________________________\nOUTORGANTE", fontNegrito);
            linhaAssinaturaP1.setAlignment(Element.ALIGN_CENTER);
            document.add(linhaAssinaturaP1);

            // RODAPÉ FIXO ABSOLUTO PÁGINA 1
            PdfContentByte cb1 = writer.getDirectContent();
            cb1.setLineWidth(0.5f);
            cb1.moveTo(40, 75);
            cb1.lineTo(555, 75);
            cb1.stroke();

            ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, new Phrase("Cristhian Menezes de Jezus Advocacia e Consultoria Jurídica", fontRodape), 297.5f, 60f, 0);
            ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, new Phrase("Endereço: Rua Tiradentes, nº 676, Centro, Ijui/RS.", fontRodape), 297.5f, 50f, 0);
            ColumnText.showTextAligned(cb1, Element.ALIGN_CENTER, new Phrase("Contato: (55) 9.9114-5944 | E-mail: cristhian.menezes@outlook.com", fontRodape), 297.5f, 40f, 0);

            // ==========================================
            // PÁGINA 2: DECLARAÇÃO DE HIPOSSUFICIÊNCIA
            // ==========================================
            document.newPage();

            if (logoUrl != null) {
                Image logoP2 = Image.getInstance(logoUrl);
                logoP2.scaleToFit(110, 110);
                logoP2.setAlignment(Element.ALIGN_CENTER);
                logoP2.setSpacingAfter(30);
                document.add(logoP2);
            }

            Paragraph tituloDeclaracao = new Paragraph("DECLARAÇÃO", fontTitulo);
            tituloDeclaracao.setAlignment(Element.ALIGN_CENTER);
            tituloDeclaracao.setSpacingAfter(15);
            document.add(tituloDeclaracao);

            Paragraph textoDeclaracao = new Paragraph();
            textoDeclaracao.setLeading(16f);
            textoDeclaracao.setAlignment(Element.ALIGN_JUSTIFIED);
            textoDeclaracao.setSpacingAfter(20);
            textoDeclaracao.add(new Chunk("Eu, ", fontTexto));
            textoDeclaracao.add(new Chunk(nome, fontNegrito));
            textoDeclaracao.add(new Chunk(", brasileiro(a), " + estadoCivil + ", " + profissao +
                    ", inscrito(a) no CPF sob o nº " + cpf + ", residente e domiciliado(a) na " +
                    logradouro + ", nº " + numero + ", Bairro " + bairro + ", na cidade de " +
                    cidade + "/" + uf + ", DECLARO, para todos os efeitos legais, nos termos do artigo 98 e seguintes da Lei nº 13.105/2015 " +
                    "(Código de Processo Civil) e da Lei nº 1.060/1950, que não possuo condições financeiras de arcar com as custas " +
                    "processuais, taxas judiciárias e honorários advocatícios sem prejuízo do meu próprio sustento e de minha família, " +
                    "fazendo jus à concessão dos benefícios da ", fontTexto));
            textoDeclaracao.add(new Chunk("ASSISTÊNCIA JUDICIÁRIA GRATUITA.", fontNegrito));
            textoDeclaracao.add(new Chunk("\n\nPor ser expressão da verdade, firmo a presente declaração para que produza seus regulares efeitos de direito.", fontTexto));
            document.add(textoDeclaracao);

            Paragraph dataP2 = new Paragraph(cidade + "/" + uf + ", " + dataFormatada + ".", fontTexto);
            dataP2.setAlignment(Element.ALIGN_CENTER);
            dataP2.setSpacingAfter(35);
            document.add(dataP2);

            Paragraph linhaAssinaturaP2 = new Paragraph("_____________________________________________________\nDECLARANTE", fontNegrito);
            linhaAssinaturaP2.setAlignment(Element.ALIGN_CENTER);
            document.add(linhaAssinaturaP2);

            // RODAPÉ FIXO ABSOLUTO PÁGINA 2
            PdfContentByte cb2 = writer.getDirectContent();
            cb2.setLineWidth(0.5f);
            cb2.moveTo(40, 75);
            cb2.lineTo(555, 75);
            cb2.stroke();

            ColumnText.showTextAligned(cb2, Element.ALIGN_CENTER, new Phrase("Cristhian Menezes de Jezus Advocacia e Consultoria Jurídica", fontRodape), 297.5f, 60f, 0);
            ColumnText.showTextAligned(cb2, Element.ALIGN_CENTER, new Phrase("Endereço: Rua Tiradentes, nº 676, Centro, Ijui/RS.", fontRodape), 297.5f, 50f, 0);
            ColumnText.showTextAligned(cb2, Element.ALIGN_CENTER, new Phrase("Contato: (55) 9.9114-5944 | E-mail: cristhian.menezes@outlook.com", fontRodape), 297.5f, 40f, 0);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o arquivo PDF de Procuração e Declaração: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    @Override
    public byte[] gerarContratoHonorarios(Cliente cliente) {
        String nome = "NOME COMPLETO";
        if (cliente != null && cliente.getNome() != null && !cliente.getNome().trim().isEmpty()) {
            nome = cliente.getNome().toUpperCase();
        }

        String cpfCnpj = "___________";
        if (cliente != null && cliente.getCpfCnpj() != null && !cliente.getCpfCnpj().trim().isEmpty()) {
            cpfCnpj = cliente.getCpfCnpj();
        }

        String cidade = "Ijuí";
        if (cliente != null && cliente.getCidade() != null && !cliente.getCidade().trim().isEmpty()) {
            cidade = cliente.getCidade();
        }

        String uf = "RS";
        if (cliente != null && cliente.getUf() != null && !cliente.getUf().trim().isEmpty()) {
            uf = cliente.getUf();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataFormatada = LocalDate.now().format(formatter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font fontNegrito = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font fontTexto = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);

            Paragraph titulo = new Paragraph("CONTRATO DE PRESTAÇÃO DE SERVIÇOS E HONORÁRIOS ADVOCATÍCIOS", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(25);
            document.add(titulo);

            Paragraph pContratante = new Paragraph();
            pContratante.setAlignment(Element.ALIGN_JUSTIFIED);
            pContratante.setSpacingAfter(15);
            pContratante.add(new Chunk("CONTRATANTE: ", fontNegrito));
            pContratante.add(new Chunk(nome + ", inscrito(a) no CPF/CNPJ sob o nº " + cpfCnpj + ".\n", fontTexto));
            document.add(pContratante);

            Paragraph pContratado = new Paragraph();
            pContratado.setAlignment(Element.ALIGN_JUSTIFIED);
            pContratado.setSpacingAfter(15);
            pContratado.add(new Chunk("CONTRATADO: ", fontNegrito));
            pContratado.add(new Chunk("CRISTHIAN MENEZES DE JEZUS, Advogado inscrito na OAB/RS sob o nº 121.837, com endereço profissional na Rua Tiradentes, nº 676, Centro, Ijuí/RS.\n", fontTexto));
            document.add(pContratado);

            Paragraph pClausula1 = new Paragraph();
            pClausula1.setAlignment(Element.ALIGN_JUSTIFIED);
            pClausula1.setSpacingAfter(15);
            pClausula1.add(new Chunk("CLÁUSULA PRIMEIRA - DO OBJETO: ", fontNegrito));
            pClausula1.add(new Chunk("O CONTRATADO prestará serviços advocatícios na defesa dos interesses jurídicos do CONTRATANTE.\n", fontTexto));
            document.add(pClausula1);

            Paragraph pClausula2 = new Paragraph();
            pClausula2.setAlignment(Element.ALIGN_JUSTIFIED);
            pClausula2.setSpacingAfter(25);
            pClausula2.add(new Chunk("CLÁUSULA SEGUNDA - DOS HONORÁRIOS: ", fontNegrito));
            pClausula2.add(new Chunk("Pelos serviços prestados, o CONTRATANTE pagará ao CONTRATADO os honorários acordados conforme as condições contratuais estabelecidas.\n", fontTexto));
            document.add(pClausula2);

            Paragraph dataP = new Paragraph(cidade + "/" + uf + ", " + dataFormatada + ".", fontTexto);
            dataP.setAlignment(Element.ALIGN_CENTER);
            dataP.setSpacingAfter(40);
            document.add(dataP);

            Paragraph assinaturas = new Paragraph("_____________________________________________________\n" + nome + " (CONTRATANTE)\n\n\n_____________________________________________________\nCRISTHIAN MENEZES DE JEZUS (CONTRATADO)", fontNegrito);
            assinaturas.setAlignment(Element.ALIGN_CENTER);
            document.add(assinaturas);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o PDF de Contrato de Honorários: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }
}
