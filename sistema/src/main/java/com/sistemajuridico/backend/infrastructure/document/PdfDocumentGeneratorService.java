package com.sistemajuridico.backend.infrastructure.document;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.sistemajuridico.backend.core.domain.Cliente;
import com.sistemajuridico.backend.core.domain.enums.EstadoCivilEnum;
import com.sistemajuridico.backend.core.domain.enums.SexoEnum;
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
    public byte[] gerarProcuracao(Cliente cliente, String acao, String varaCivel, String comarca, boolean imprimirDeclaracao) {
        java.net.URL urlNormal = getClass().getResource("/BOOKOS.TTF");
        java.net.URL urlBold = getClass().getResource("/BOOKOSB.TTF");
        if (urlNormal != null) {
            FontFactory.register(urlNormal.toString(), "BookmanNormal");
        }
        if (urlBold != null) {
            FontFactory.register(urlBold.toString(), "BookmanBold");
        }

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

            Font fontTitulo = FontFactory.getFont("BookmanBold", BaseFont.WINANSI, BaseFont.EMBEDDED, 14, Font.NORMAL);
            Font fontSubtitulo = FontFactory.getFont("BookmanBold", BaseFont.WINANSI, BaseFont.EMBEDDED, 11, Font.NORMAL);
            Font fontNegrito = FontFactory.getFont("BookmanBold", BaseFont.WINANSI, BaseFont.EMBEDDED, 10, Font.NORMAL);
            Font fontTexto = FontFactory.getFont("BookmanNormal", BaseFont.WINANSI, BaseFont.EMBEDDED, 10, Font.NORMAL);
            Font fontRodape = FontFactory.getFont("BookmanNormal", BaseFont.WINANSI, BaseFont.EMBEDDED, 8, Font.NORMAL);

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
            if (imprimirDeclaracao) {
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
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o arquivo PDF de Procuração e Declaração: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    public static class ContratoPageEvent extends PdfPageEventHelper {
        private Image logo;

        public ContratoPageEvent() {
            try {
                java.net.URL logoUrl = ContratoPageEvent.class.getResource("/logo.png");
                if (logoUrl != null) {
                    this.logo = Image.getInstance(logoUrl);
                    this.logo.scaleToFit(110f, 110f);
                }
            } catch (Exception e) {
                // Trata silenciosamente caso não encontre a imagem
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            if (this.logo != null) {
                try {
                    float x = (document.getPageSize().getWidth() - this.logo.getScaledWidth()) / 2f;
                    float y = document.getPageSize().getTop() - this.logo.getScaledHeight() - 15f;
                    this.logo.setAbsolutePosition(x, y);
                    cb.addImage(this.logo);
                } catch (DocumentException e) {
                    // Trata exceção de inserção de imagem
                }
            }

            java.net.URL urlNormal = ContratoPageEvent.class.getResource("/BOOKOS.TTF");
            if (urlNormal != null) {
                FontFactory.register(urlNormal.toString(), "BookmanNormal");
            }
            Font fontRodape = FontFactory.getFont("BookmanNormal", BaseFont.WINANSI, BaseFont.EMBEDDED, 8, Font.NORMAL);
            cb.setLineWidth(0.5f);
            cb.moveTo(40, 75);
            cb.lineTo(555, 75);
            cb.stroke();

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Cristhian Menezes de Jezus | Advocacia e Consultoria Jurídica", fontRodape), 297.5f, 60f, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Endereço: Rua Tiradentes, nº 676, Centro, Ijui/RS.", fontRodape), 297.5f, 50f, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase("Contato: (55) 9.9114-5944 | E-mail: cristhian.menezes@outlook.com", fontRodape), 297.5f, 40f, 0);
        }
    }

    @Override
    public byte[] gerarContratoHonorarios(Cliente cliente, String acao, String vara, String comarca, String valorServicos, String objetivoDemanda) {
        java.net.URL urlNormal = getClass().getResource("/BOOKOS.TTF");
        java.net.URL urlBold = getClass().getResource("/BOOKOSB.TTF");
        if (urlNormal != null) {
            FontFactory.register(urlNormal.toString(), "BookmanNormal");
        }
        if (urlBold != null) {
            FontFactory.register(urlBold.toString(), "BookmanBold");
        }

        String nome = "_________";
        if (cliente != null) {
            if (cliente.getNome() != null && !cliente.getNome().trim().isEmpty()) {
                nome = cliente.getNome().toUpperCase();
            }
        }

        boolean isFeminino = false;
        if (cliente != null && cliente.getSexo() != null && cliente.getSexo() == SexoEnum.FEMININO) {
            isFeminino = true;
        }

        String artMaiusculo = "O";
        String artMinusculo = "o";
        String obrigado = "obrigado";

        if (isFeminino) {
            artMaiusculo = "A";
            artMinusculo = "a";
            obrigado = "obrigada";
        }

        String nacionalidade = "brasileiro";
        String estadoCivil = "_________";
        String residente = "residente e domiciliado";
        String inscrito = "inscrito";

        if (isFeminino) {
            nacionalidade = "brasileira";
            residente = "residente e domiciliada";
            inscrito = "inscrita";
            if (cliente.getEstadoCivil() != null) {
                if (cliente.getEstadoCivil() == EstadoCivilEnum.SOLTEIRO) {
                    estadoCivil = "solteira";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.CASADO) {
                    estadoCivil = "casada";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.VIUVO) {
                    estadoCivil = "viúva";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.DIVORCIADO) {
                    estadoCivil = "divorciada";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.UNIAO_ESTAVEL) {
                    estadoCivil = "em união estável";
                } else {
                    estadoCivil = cliente.getEstadoCivil().name().toLowerCase();
                }
            }
        } else {
            if (cliente != null && cliente.getEstadoCivil() != null) {
                if (cliente.getEstadoCivil() == EstadoCivilEnum.SOLTEIRO) {
                    estadoCivil = "solteiro";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.CASADO) {
                    estadoCivil = "casado";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.VIUVO) {
                    estadoCivil = "viúvo";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.DIVORCIADO) {
                    estadoCivil = "divorciado";
                } else if (cliente.getEstadoCivil() == EstadoCivilEnum.UNIAO_ESTAVEL) {
                    estadoCivil = "em união estável";
                } else {
                    estadoCivil = cliente.getEstadoCivil().name().toLowerCase();
                }
            }
        }

        String profissao = "_________";
        if (cliente != null) {
            if (cliente.getProfissao() != null && !cliente.getProfissao().trim().isEmpty()) {
                profissao = cliente.getProfissao().trim();
            }
        }

        String cpf = "_________";
        if (cliente != null) {
            if (cliente.getCpfCnpj() != null && !cliente.getCpfCnpj().trim().isEmpty()) {
                String docLimpo = cliente.getCpfCnpj().replaceAll("\\D", "");
                if (docLimpo.length() == 11) {
                    cpf = docLimpo.substring(0, 3) + "." + docLimpo.substring(3, 6) + "." + docLimpo.substring(6, 9) + "-" + docLimpo.substring(9, 11);
                } else if (docLimpo.length() == 14) {
                    cpf = docLimpo.substring(0, 2) + "." + docLimpo.substring(2, 5) + "." + docLimpo.substring(5, 8) + "/" + docLimpo.substring(8, 12) + "-" + docLimpo.substring(12, 14);
                } else {
                    cpf = cliente.getCpfCnpj().trim();
                }
            }
        }

        String logradouro = "_________";
        if (cliente != null) {
            if (cliente.getLogradouro() != null && !cliente.getLogradouro().trim().isEmpty()) {
                logradouro = cliente.getLogradouro().trim();
            }
        }

        String numero = "_________";
        if (cliente != null) {
            if (cliente.getNumero() != null && !cliente.getNumero().trim().isEmpty()) {
                numero = cliente.getNumero().trim();
            }
        }

        String bairro = "_________";
        if (cliente != null) {
            if (cliente.getBairro() != null && !cliente.getBairro().trim().isEmpty()) {
                bairro = cliente.getBairro().trim();
            }
        }

        String cidade = "Ijuí";
        if (cliente != null) {
            if (cliente.getCidade() != null && !cliente.getCidade().trim().isEmpty()) {
                cidade = cliente.getCidade().trim();
            }
        }

        String uf = "RS";
        if (cliente != null) {
            if (cliente.getUf() != null && !cliente.getUf().trim().isEmpty()) {
                uf = cliente.getUf().trim();
            }
        }

        String acaoTexto = "_________";
        if (acao != null && !acao.trim().isEmpty()) {
            acaoTexto = acao.trim();
        }

        String objetivoTexto = "_________________________";
        if (objetivoDemanda != null && !objetivoDemanda.trim().isEmpty()) {
            objetivoTexto = objetivoDemanda.trim();
        }

        String varaTexto = "_________";
        if (vara != null && !vara.trim().isEmpty()) {
            varaTexto = vara.trim();
        }

        String jurisdicao;
        if (comarca != null && !comarca.trim().isEmpty()) {
            jurisdicao = varaTexto + " da comarca de " + comarca.trim();
        } else {
            jurisdicao = varaTexto;
        }

        String valorServicosTexto = "_________________________";
        if (valorServicos != null && !valorServicos.trim().isEmpty()) {
            valorServicosTexto = valorServicos.trim();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataFormatada = LocalDate.now().format(formatter);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 70, 70, 120, 80);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new ContratoPageEvent());
            document.open();

            Font fontTitulo = FontFactory.getFont("BookmanBold", BaseFont.WINANSI, BaseFont.EMBEDDED, 12, Font.NORMAL);
            Font fontNegrito = FontFactory.getFont("BookmanBold", BaseFont.WINANSI, BaseFont.EMBEDDED, 10, Font.NORMAL);
            Font fontTexto = FontFactory.getFont("BookmanNormal", BaseFont.WINANSI, BaseFont.EMBEDDED, 10, Font.NORMAL);

            // ==========================================
            // TÍTULO
            // ==========================================
            Chunk chunkTitulo = new Chunk("CONTRATO DE PRESTAÇÃO DE SERVIÇOS E HONORÁRIOS ADVOCATÍCIOS", fontTitulo);
            chunkTitulo.setCharacterSpacing(-0.4f); // Aproxima as letras
            Paragraph titulo = new Paragraph(chunkTitulo);
            titulo.setAlignment(Element.ALIGN_JUSTIFIED);
            titulo.setLeading(18f);
            titulo.setSpacingAfter(12f);
            document.add(titulo);

            // ==========================================
            // PREÂMBULO
            // ==========================================
            Paragraph preambulo = new Paragraph();
            preambulo.setLeading(16f);
            preambulo.setAlignment(Element.ALIGN_JUSTIFIED);
            preambulo.setSpacingAfter(10f);
            preambulo.add(new Chunk("Pelo presente instrumento particular de Contrato, de um lado ", fontTexto));
            preambulo.add(new Chunk("CRISTHIAN MENEZES DE JEZUS", fontNegrito));
            preambulo.add(new Chunk(", brasileiro, casado, advogado, inscrito na OAB/RS sob o nº 121.837 e CPF nº 039.623.600-69, com escritório localizado na Rua Tiradentes, 676, Centro, na cidade de Ijuí/RS, ora denominado ", fontTexto));
            preambulo.add(new Chunk("CONTRATADO", fontNegrito));
            preambulo.add(new Chunk(" e ", fontTexto));
            preambulo.add(new Chunk(nome, fontNegrito));
            preambulo.add(new Chunk(", " + nacionalidade + ", " + estadoCivil + ", " + profissao + ", " + inscrito + " no CPF nº " + cpf + ", " + residente + " na " + logradouro + ", nº " + numero + ", Bairro " + bairro + " na cidade de " + cidade + "/" + uf + ", e ora denominad" + (isFeminino ? "a" : "o") + " ", fontTexto));
            preambulo.add(new Chunk("CONTRATANTE", fontNegrito));
            preambulo.add(new Chunk(", ajustam entre si, justo e contratado o seguinte:", fontTexto));
            document.add(preambulo);

            // ==========================================
            // CLÁUSULA 1 - DO OBJETO DESTE CONTRATO
            // ==========================================
            Paragraph secObjeto = new Paragraph("DO OBJETO DESTE CONTRATO", fontNegrito);
            secObjeto.setLeading(16f);
            secObjeto.setAlignment(Element.ALIGN_CENTER);
            secObjeto.setSpacingAfter(6f);
            document.add(secObjeto);

            Paragraph clausula1 = new Paragraph();
            clausula1.setLeading(16f);
            clausula1.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula1.setSpacingAfter(6f);
            clausula1.add(new Chunk("CLÁUSULA 1. ", fontNegrito));
            clausula1.add(new Chunk("O contratado prestará o serviço de advocacia, especialmente:\n", fontTexto));
            clausula1.add(new Chunk("Para ", fontTexto));
            clausula1.add(new Chunk("REPRESENTAR " + artMaiusculo + " CONTRATANTE", fontNegrito));
            clausula1.add(new Chunk(" na " + jurisdicao + ", para ", fontTexto));
            clausula1.add(new Chunk("AJUIZAR " + acaoTexto, fontNegrito));
            clausula1.add(new Chunk(", em conformidade com os poderes conferidos na respectiva procuração, com o máximo de zelo e compromisso com " + artMinusculo, fontTexto));
            clausula1.add(new Chunk( " CONTRATANTE", fontNegrito));
            clausula1.add(new Chunk(" e sua demanda. O contratado informou dos riscos do processo, não o garantindo sucesso na demanda, pelo motivo da advocacia se tratar de atividade-meio.\n", fontTexto));
            clausula1.add(new Chunk("Parágrafo único: ", fontNegrito));
            clausula1.add(new Chunk("O contratado deixará o contratante devidamente atualizado mensalmente sobre sua demanda, ou quando vier a acontecer qualquer movimentação em seu processo que seja de interesse do contratado.\n", fontTexto));
            clausula1.add(new Chunk("Objetivo da demanda: " , fontNegrito));
            clausula1.add(new Chunk(objetivoTexto, fontTexto));
            document.add(clausula1);

            // ==========================================
            // CLÁUSULA 2 - DOS HONORÁRIOS
            // ==========================================
            Paragraph secHonorarios = new Paragraph("DOS HONORÁRIOS", fontNegrito);
            secHonorarios.setLeading(16f);
            secHonorarios.setAlignment(Element.ALIGN_CENTER);
            secHonorarios.setSpacingAfter(6f);
            document.add(secHonorarios);

            Paragraph clausula2 = new Paragraph();
            clausula2.setLeading(16f);
            clausula2.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula2.setSpacingAfter(10f);
            clausula2.add(new Chunk("CLÁUSULA 2. ", fontNegrito));
            clausula2.add(new Chunk(artMaiusculo + " contratante pagará, ao contratado, a título de honorários advocatícios, pelos serviços prestados, ", fontTexto));
            clausula2.add(new Chunk(valorServicosTexto, fontNegrito));
            clausula2.add(new Chunk(".", fontTexto));
            document.add(clausula2);

            // ==========================================
            // CLÁUSULAS 3 E 4 - DAS DESPESAS EXTRAS
            // ==========================================
            Paragraph secDespesas = new Paragraph("DAS DESPESAS EXTRAS", fontNegrito);
            secDespesas.setLeading(16f);
            secDespesas.setAlignment(Element.ALIGN_CENTER);
            secDespesas.setSpacingAfter(6f);
            document.add(secDespesas);

            Paragraph clausula3 = new Paragraph();
            clausula3.setLeading(16f);
            clausula3.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula3.setSpacingAfter(6f);
            clausula3.add(new Chunk("CLÁUSULA 3. ", fontNegrito));
            clausula3.add(new Chunk("Não se compreende nas quantias acima estipuladas, quaisquer despesas judiciais ou extras, tais como custas processuais, honorários de terceiros (peritos, cálculos etc.), correspondentes jurídicos e despesas de viagem, quando necessárias.", fontTexto));
            document.add(clausula3);

            Paragraph clausula4 = new Paragraph();
            clausula4.setLeading(16f);
            clausula4.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula4.setSpacingAfter(10f);
            clausula4.add(new Chunk("CLÁUSULA 4. ", fontNegrito));
            clausula4.add(new Chunk(artMaiusculo + " contratante será " + obrigado + " a fornecer numerário necessário para a satisfação das referidas despesas, de modo a não interromper o andamento do processo, quando for o caso, ou dos trabalhos extrajudiciais, e, não o fazendo, fica o contratado isento de qualquer responsabilidade pela demora ou interrupção que dela resulte.", fontTexto));
            document.add(clausula4);

            // ==========================================
            // CLÁUSULA 5 - DO PRAZO
            // ==========================================
            Paragraph secPrazo = new Paragraph("DO PRAZO", fontNegrito);
            secPrazo.setLeading(16f);
            secPrazo.setAlignment(Element.ALIGN_CENTER);
            secPrazo.setSpacingAfter(6f);
            document.add(secPrazo);

            Paragraph clausula5 = new Paragraph();
            clausula5.setLeading(16f);
            clausula5.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula5.setSpacingAfter(10f);
            clausula5.add(new Chunk("CLÁUSULA 5. ", fontNegrito));
            clausula5.add(new Chunk("Este contrato tem validade até a sentença de 1º GRAU.", fontTexto));
            document.add(clausula5);

            document.newPage();

            // ==========================================
            // CLÁUSULA 6 - DA RESCISÃO CONTRATUAL
            // ==========================================
            Paragraph secRescisao = new Paragraph("DA RESCISÃO CONTRATUAL", fontNegrito);
            secRescisao.setLeading(16f);
            secRescisao.setAlignment(Element.ALIGN_CENTER);
            secRescisao.setSpacingAfter(6f);
            document.add(secRescisao);

            Paragraph clausula6 = new Paragraph();
            clausula6.setLeading(16f);
            clausula6.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula6.setSpacingAfter(10f);
            clausula6.add(new Chunk("CLÁUSULA 6. ", fontNegrito));
            clausula6.add(new Chunk("Da rescisão contratual não caberá restituição de valores já pagos pela Contratante.\n", fontTexto));
            clausula6.add(new Chunk("Parágrafo primeiro: ", fontNegrito));
            clausula6.add(new Chunk("O presente contrato de prestação de serviço tem como principal princípio a confiança entre as partes, qualquer ato que quebre o respeito e a confiança, será devidamente debatido e se assim justificar, será rescindido o contrato, por culpa daquele que o causou.\n", fontTexto));
            clausula6.add(new Chunk("Parágrafo segundo: ", fontNegrito));
            clausula6.add(new Chunk("Em caso de desistência por parte d" + artMinusculo + " contratante, antes do início da prestação de serviço, fica " + artMinusculo + " contratante " + obrigado + " a pagar ao contratado uma multa de 10% sobre o valor do contrato, a título de ressarcimento pelo tempo à disposição do contratante.\n", fontTexto));
            clausula6.add(new Chunk("Parágrafo terceiro: ", fontNegrito));
            clausula6.add(new Chunk("Em caso de desistência por parte d" + artMinusculo + " contratante, após a distribuição do processo, deverá pagar ao contratado, honorários advocatícios no valor de 1 (um) salário mínimo nacional.", fontTexto));
            document.add(clausula6);

            // ==========================================
            // CLÁUSULA 7 - DAS INFORMAÇÕES
            // ==========================================
            Paragraph secInfo = new Paragraph("DAS INFORMAÇÕES", fontNegrito);
            secInfo.setLeading(16f);
            secInfo.setAlignment(Element.ALIGN_CENTER);
            secInfo.setSpacingAfter(6f);
            document.add(secInfo);

            Paragraph clausula7 = new Paragraph();
            clausula7.setLeading(16f);
            clausula7.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula7.setSpacingAfter(10f);
            clausula7.add(new Chunk("CLÁUSULA 7. ", fontNegrito));
            clausula7.add(new Chunk(artMaiusculo + " contratante se obriga a informar ao contratado imediatamente, por escrito, sua eventual alteração de endereço, inclusive eletrônico, autorizando a informação dessa atualização nos autos.", fontTexto));
            document.add(clausula7);

            // ==========================================
            // CLÁUSULA 8 - DA CONFIDENCIALIDADE
            // ==========================================
            Paragraph secConf = new Paragraph("DA CONFIDENCIALIDADE", fontNegrito);
            secConf.setLeading(16f);
            secConf.setAlignment(Element.ALIGN_CENTER);
            secConf.setSpacingAfter(6f);
            document.add(secConf);

            Paragraph clausula8 = new Paragraph();
            clausula8.setLeading(16f);
            clausula8.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula8.setSpacingAfter(10f);
            clausula8.add(new Chunk("CLÁUSULA 8. ", fontNegrito));
            clausula8.add(new Chunk("As informações sobre a AÇÃO objeto deste contrato, são confidenciais, só devendo ser repassadas ao contratante ou àquele autorizado pelo mesmo.\n", fontTexto));
            clausula8.add(new Chunk("Parágrafo único: ", fontNegrito));
            clausula8.add(new Chunk("As informações e dados pessoais d" + artMinusculo + " contratante, estará resguardado pela LGPD, sendo único e exclusivamente usados na demanda judicial aqui contratada.", fontTexto));
            document.add(clausula8);

            // ==========================================
            // CLÁUSULA 9 - DO FORO
            // ==========================================
            Paragraph secForo = new Paragraph("DO FORO", fontNegrito);
            secForo.setLeading(16f);
            secForo.setAlignment(Element.ALIGN_CENTER);
            secForo.setSpacingAfter(6f);
            document.add(secForo);

            Paragraph clausula9 = new Paragraph();
            clausula9.setLeading(16f);
            clausula9.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula9.setSpacingAfter(10f);
            clausula9.add(new Chunk("CLÁUSULA 9: ", fontNegrito));
            clausula9.add(new Chunk("As partes elegem o Foro da Comarca de Ijuí/RS para dirimirem os casos de omissões no presente instrumento e não regulados por lei.", fontTexto));
            document.add(clausula9);

            // ==========================================
            // CLÁUSULAS 10 E 11 - DO TÍTULO EXECUTIVO
            // ==========================================
            Paragraph secTituloExec = new Paragraph("DO TÍTULO EXECUTIVO", fontNegrito);
            secTituloExec.setLeading(16f);
            secTituloExec.setAlignment(Element.ALIGN_CENTER);
            secTituloExec.setSpacingAfter(6f);
            document.add(secTituloExec);

            Paragraph clausula10 = new Paragraph();
            clausula10.setLeading(16f);
            clausula10.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula10.setSpacingAfter(6f);
            clausula10.add(new Chunk("CLÁUSULA 10: ", fontNegrito));
            clausula10.add(new Chunk("Este contrato tem força de título executivo extrajudicial, na forma da lei e também, " + artMinusculo + " contratante é ciente e autoriza a penhora de contas bancárias e salários em uma possível inadimplência, com a devida atualização monetária e acréscimo de juros de 1% ao mês e multa de 10% sobre o valor do contrato.", fontTexto));
            document.add(clausula10);

            Paragraph clausula11 = new Paragraph();
            clausula11.setLeading(16f);
            clausula11.setAlignment(Element.ALIGN_JUSTIFIED);
            clausula11.setSpacingAfter(10f);
            clausula11.add(new Chunk("CLÁUSULA 11: ", fontNegrito));
            clausula11.add(new Chunk("No presente contrato, as obrigações d" + artMinusculo + " Contratante são transmitidas aos seus herdeiros e sucessores.", fontTexto));
            document.add(clausula11);

            Paragraph fechamento = new Paragraph("E, por estarem assim justos contratados, assinam o presente contrato, ficando cada uma das partes com um exemplar para os devidos fins.", fontTexto);
            fechamento.setLeading(16f);
            fechamento.setAlignment(Element.ALIGN_JUSTIFIED);
            fechamento.setSpacingAfter(15f);
            document.add(fechamento);

            Paragraph dataP = new Paragraph(cidade + "/" + uf + ", " + dataFormatada + ".", fontTexto);
            dataP.setLeading(16f);
            dataP.setAlignment(Element.ALIGN_CENTER);
            dataP.setSpacingAfter(50f);
            document.add(dataP);

            // ==========================================
            // ASSINATURAS E TESTEMUNHAS
            // ==========================================
            Paragraph assContratado = new Paragraph();
            assContratado.setLeading(16f);
            assContratado.setAlignment(Element.ALIGN_CENTER);
            assContratado.setSpacingAfter(30f);
            assContratado.add(new Chunk("_____________________________________________________\n", fontTexto));
            assContratado.add(new Chunk("CRISTHIAN MENEZES DE JEZUS\n", fontNegrito));
            assContratado.add(new Chunk("Contratado", fontTexto));
            document.add(assContratado);

            Paragraph assContratante = new Paragraph();
            assContratante.setLeading(16f);
            assContratante.setAlignment(Element.ALIGN_CENTER);
            assContratante.setSpacingAfter(40f);
            assContratante.add(new Chunk("_____________________________________________________\n", fontTexto));
            assContratante.add(new Chunk(nome + "\n", fontNegrito));
            assContratante.add(new Chunk("Contratante", fontTexto));
            document.add(assContratante);

            Paragraph testHeader = new Paragraph("Testemunhas:\n", fontTexto);
            testHeader.setLeading(16f);
            testHeader.setAlignment(Element.ALIGN_JUSTIFIED);
            testHeader.setSpacingAfter(5f);
            document.add(testHeader);

            Paragraph test1 = new Paragraph();
            test1.setLeading(20f);
            test1.setAlignment(Element.ALIGN_JUSTIFIED);
            test1.setSpacingAfter(25f);
            test1.add(new Chunk("Nome Completo:___________________________________________________\n", fontTexto));
            test1.add(new Chunk("CPF: ___________________________________________\n", fontTexto));
            test1.add(new Chunk("________________________________________________", fontTexto));
            document.add(test1);

            Paragraph test2 = new Paragraph();
            test2.setLeading(20f);
            test2.setAlignment(Element.ALIGN_JUSTIFIED);
            test2.add(new Chunk("Nome Completo:___________________________________________________\n", fontTexto));
            test2.add(new Chunk("CPF:____________________________________________\n", fontTexto));
            test2.add(new Chunk("________________________________________________", fontTexto));
            document.add(test2);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o PDF de Contrato de Honorários: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }
}
