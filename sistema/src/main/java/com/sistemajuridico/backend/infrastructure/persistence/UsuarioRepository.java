package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Usuario;
import com.sistemajuridico.backend.core.domain.enums.PerfilAcessoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findByPerfil(PerfilAcessoEnum perfil);
    List<Usuario> findByPerfilAndAtivoTrue(PerfilAcessoEnum perfil);
    List<Usuario> findByPerfilInAndAtivoTrue(List<PerfilAcessoEnum> perfis);

    @Query(value = "SELECT u.* FROM tb_usuario u WHERE u.ativo = true AND u.oab IS NOT NULL AND TRIM(u.oab) != ''", nativeQuery = true)
    List<Usuario> buscarAdvogadosComOabAtiva();

    @Query(value = "SELECT u.* FROM tb_usuario u WHERE " +
                   "(CAST(:ativo AS boolean) IS NULL OR u.ativo = CAST(:ativo AS boolean)) AND " +
                   "(CAST(:termo AS text) IS NULL OR (" +
                   "lower(u.nome) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                   "lower(u.email) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           countQuery = "SELECT count(u.id) FROM tb_usuario u WHERE " +
                        "(CAST(:ativo AS boolean) IS NULL OR u.ativo = CAST(:ativo AS boolean)) AND " +
                        "(CAST(:termo AS text) IS NULL OR (" +
                        "lower(u.nome) LIKE lower(concat('%', CAST(:termo AS text), '%')) OR " +
                        "lower(u.email) LIKE lower(concat('%', CAST(:termo AS text), '%'))))",
           nativeQuery = true)
    Page<Usuario> buscarComFiltros(@Param("ativo") Boolean ativo, @Param("termo") String termo, Pageable pageable);
}
