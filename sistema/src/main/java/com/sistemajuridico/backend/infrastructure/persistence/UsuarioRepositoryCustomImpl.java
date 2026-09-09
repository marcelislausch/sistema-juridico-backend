package com.sistemajuridico.backend.infrastructure.persistence;

import com.sistemajuridico.backend.core.domain.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRepositoryCustomImpl implements UsuarioRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Usuario> buscarComFiltros(Boolean ativo, String termo, Pageable pageable) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();

        // 1. Query de seleção dos registros
        CriteriaQuery<Usuario> cq = cb.createQuery(Usuario.class);
        Root<Usuario> root = cq.from(Usuario.class);

        List<Predicate> predicates = criarPredicados(cb, root, ativo, termo);
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Ordenação imperativa baseada no Pageable
        if (pageable != null && pageable.getSort() != null && pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order sortOrder : pageable.getSort()) {
                if (sortOrder.isAscending()) {
                    orders.add(cb.asc(root.get(sortOrder.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(sortOrder.getProperty())));
                }
            }
            cq.orderBy(orders);
        }

        TypedQuery<Usuario> query = this.entityManager.createQuery(cq);
        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        List<Usuario> usuarios = query.getResultList();

        // 2. Query de contagem total de registros para a paginação
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Usuario> countRoot = countCq.from(Usuario.class);

        List<Predicate> countPredicates = criarPredicados(cb, countRoot, ativo, termo);
        if (!countPredicates.isEmpty()) {
            countCq.where(countPredicates.toArray(new Predicate[0]));
        }
        countCq.select(cb.count(countRoot));

        Long total = this.entityManager.createQuery(countCq).getSingleResult();
        long totalRegistros = total != null ? total : 0L;

        return new PageImpl<>(usuarios, pageable != null ? pageable : Pageable.unpaged(), totalRegistros);
    }

    private List<Predicate> criarPredicados(CriteriaBuilder cb, Root<Usuario> root, Boolean ativo, String termo) {
        List<Predicate> predicates = new ArrayList<>();

        if (ativo != null) {
            predicates.add(cb.equal(root.get("ativo"), ativo));
        }

        if (termo != null && !termo.trim().isEmpty()) {
            String pattern = "%" + termo.trim().toLowerCase() + "%";
            Predicate nomeLike = cb.like(cb.lower(root.get("nome")), pattern);
            Predicate emailLike = cb.like(cb.lower(root.get("email")), pattern);
            Predicate oabLike = cb.like(cb.lower(root.get("oab")), pattern);
            predicates.add(cb.or(nomeLike, emailLike, oabLike));
        }

        return predicates;
    }
}
