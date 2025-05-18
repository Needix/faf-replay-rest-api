package de.needix.games.faf.replay.api.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

interface ReplayRepositoryCustom {
    <T> Slice<T> findSlice(Specification<T> spec, Pageable pageable, Class<T> domainClass);
}

@Repository
public class ReplayRepositoryCustomImpl implements ReplayRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> Slice<T> findSlice(Specification<T> spec, Pageable pageable, Class<T> domainClass) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(domainClass);
        Root<T> root = query.from(domainClass);

        if (spec != null) {
            Predicate predicate = spec.toPredicate(root, query, cb);
            query.where(predicate);
        }

        query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));

        TypedQuery<T> typedQuery = entityManager.createQuery(query);

        // Apply pagination
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize() + 1);

        List<T> content = typedQuery.getResultList();
        boolean hasNext = content.size() > pageable.getPageSize();

        if (hasNext) {
            content.remove(content.size() - 1); // Remove the extra element
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}
