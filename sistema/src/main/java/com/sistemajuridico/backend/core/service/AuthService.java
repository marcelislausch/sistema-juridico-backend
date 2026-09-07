package com.sistemajuridico.backend.core.service;

import com.sistemajuridico.backend.core.domain.PasswordResetToken;
import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.exceptions.RecursoNaoEncontradoException;
import com.sistemajuridico.backend.core.domain.exceptions.RegraNegocioException;
import com.sistemajuridico.backend.infrastructure.persistence.PasswordResetTokenRepository;
import com.sistemajuridico.backend.infrastructure.persistence.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:menezescrithian@gmail.com}")
    private String remetente;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional(rollbackFor = Exception.class)
    public void solicitarRecuperacaoSenha(String email) {
        if (email == null || email.trim().isEmpty()) {
            return;
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(email.trim());
        if (optUsuario.isEmpty()) {
            // Regra anti-enumeração de contas: retorna silenciosamente sem revelar se o e-mail existe
            return;
        }

        Usuario usuario = optUsuario.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime dataExpiracao = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(token, usuario.getId(), dataExpiracao);
        this.passwordResetTokenRepository.save(resetToken);

        try {
            MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(this.remetente);
            helper.setTo(usuario.getEmail());
            helper.setSubject("Redefinição de Senha - Cristhian Menezes Advocacia");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;\">"
                    + "  <div style=\"max-width: 600px; background-color: #ffffff; margin: 0 auto; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">"
                    + "    <div style=\"background-color: #1a1a1a; padding: 30px; text-align: center; border-bottom: 4px solid #d4af37;\">"
                    + "      <h1 style=\"color: #ffffff; margin: 0; font-size: 22px; letter-spacing: 1px;\">CRISTHIAN MENEZES</h1>"
                    + "      <p style=\"color: #d4af37; margin: 5px 0 0 0; font-size: 12px; letter-spacing: 2px;\">ADVOCACIA E CONSULTORIA JURÍDICA</p>"
                    + "    </div>"
                    + "    <div style=\"padding: 40px 30px; color: #333333; line-height: 1.6;\">"
                    + "      <p style=\"font-size: 16px; margin-top: 0;\">Olá, <strong>" + usuario.getNome() + "</strong>,</p>"
                    + "      <p style=\"font-size: 14px; color: #555555;\">Recebemos uma solicitação para redefinir a senha de acesso ao seu painel jurídico. Utilize o token seguro abaixo para concluir o processo:</p>"
                    + "      <div style=\"text-align: center; margin: 30px 0;\">"
                    + "        <span style=\"display: inline-block; background-color: #fcf8e3; border: 1px dashed #d4af37; color: #1a1a1a; font-size: 20px; font-weight: bold; padding: 12px 25px; border-radius: 6px; letter-spacing: 2px;\">" + token + "</span>"
                    + "      </div>"
                    + "      <p style=\"font-size: 13px; color: #777777;\">Este token possui validade estrita de <strong>15 minutos</strong>.</p>"
                    + "      <p style=\"font-size: 13px; color: #777777;\">Caso você não tenha solicitado esta alteração, por favor desconsidere esta mensagem com segurança.</p>"
                    + "    </div>"
                    + "    <div style=\"background-color: #f9f9f9; padding: 20px; text-align: center; border-top: 1px solid #eeeeee; font-size: 12px; color: #999999;\">"
                    + "      <p style=\"margin: 0;\">Atenciosamente,</p>"
                    + "      <p style=\"margin: 5px 0 0 0; font-weight: bold; color: #333333;\">Equipe Cristhian Menezes Advocacia</p>"
                    + "    </div>"
                    + "  </div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            this.mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao estruturar e-mail HTML de recuperação", e);
        }
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.trim().isEmpty()) {
            throw new RegraNegocioException("O token de recuperação é obrigatório.");
        }

        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            throw new RegraNegocioException("A nova senha é obrigatória.");
        }

        Optional<PasswordResetToken> optToken = this.passwordResetTokenRepository.findByToken(token.trim());
        if (optToken.isEmpty()) {
            throw new RegraNegocioException("Token de recuperação inválido ou inexistente.");
        }

        PasswordResetToken resetToken = optToken.get();

        if (resetToken.isUsado()) {
            throw new RegraNegocioException("Este token de recuperação já foi utilizado.");
        }

        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException("O token de recuperação expirou.");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findById(resetToken.getUsuarioId());
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário associado ao token não encontrado.");
        }

        Usuario usuario = optUsuario.get();
        String novaSenhaCodificada = this.passwordEncoder.encode(novaSenha);
        usuario.setSenhaHash(novaSenhaCodificada);
        this.usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        this.passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void alterarSenhaAutenticada(String emailLogado, String senhaAtual, String novaSenha) {
        if (emailLogado == null || emailLogado.trim().isEmpty() || "anonymousUser".equals(emailLogado)) {
            throw new RegraNegocioException("Usuário não autenticado.");
        }

        if (senhaAtual == null || senhaAtual.trim().isEmpty()) {
            throw new RegraNegocioException("A senha atual é obrigatória.");
        }

        if (novaSenha == null || novaSenha.trim().isEmpty()) {
            throw new RegraNegocioException("A nova senha é obrigatória.");
        }

        Optional<Usuario> optUsuario = this.usuarioRepository.findByEmail(emailLogado.trim());
        if (optUsuario.isEmpty()) {
            throw new RecursoNaoEncontradoException("Usuário autenticado não encontrado.");
        }

        Usuario usuario = optUsuario.get();

        if (!this.passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual informada está incorreta.");
        }

        String novaSenhaCodificada = this.passwordEncoder.encode(novaSenha);
        usuario.setSenhaHash(novaSenhaCodificada);
        this.usuarioRepository.save(usuario);
    }
}
