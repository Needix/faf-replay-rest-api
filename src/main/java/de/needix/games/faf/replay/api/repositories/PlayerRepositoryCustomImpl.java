package de.needix.games.faf.replay.api.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

interface PlayerRepositoryCustom {
    <T> Slice<T> findSlice(Specification<T> spec, Pageable pageable, Class<T> domainClass);
}

@Repository
public class PlayerRepositoryCustomImpl implements PlayerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> Slice<T> findSlice(Specification<T> spec, Pageable pageable, Class<T> domainClass) {
        return SpliceHelper.findSplice(entityManager, spec, pageable, domainClass);
    }
}
