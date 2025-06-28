/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.auditlogweb.api.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditQuery;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.envers.OpenmrsRevisionEntity;
import org.openmrs.module.auditlogweb.AuditEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data access object (DAO) for retrieving audit log information using Hibernate Envers.
 * This DAO provides methods for fetching entity revisions and revision metadata such as
 * who made the change, what was changed, and when it occurred.
 */
@Repository("auditlogweb.AuditlogwebDao")
@RequiredArgsConstructor
public class AuditDao {

    private final SessionFactory sessionFactory;

    /**
     * Retrieves a paginated list of all revisions for a given audited entity class.
     *
     * @param entityClass the audited entity class to retrieve revisions for
     * @param page        the page number (0-based)
     * @param size        the number of results per page
     * @param <T>         the type of the audited entity
     * @return a list of {@link AuditEntity} containing revision data
     */
    @SuppressWarnings("unchecked")
    public <T> List<AuditEntity<T>> getAllRevisions(Class<T> entityClass, int page, int size) {
        AuditReader auditReader = AuditReaderFactory.get(sessionFactory.getCurrentSession());

        AuditQuery auditQuery = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .addOrder(org.hibernate.envers.query.AuditEntity.revisionNumber().desc())
                .setFirstResult(page * size)
                .setMaxResults(size);

        return (List<AuditEntity<T>>) auditQuery.getResultList().stream().map(result -> {
            Object[] array = (Object[]) result;
            T entity = entityClass.cast(array[0]);
            OpenmrsRevisionEntity revisionEntity = (OpenmrsRevisionEntity) array[1];
            RevisionType revisionType = (RevisionType) array[2];
            Integer userId = revisionEntity.getChangedBy();
            return new AuditEntity<>(entity, revisionEntity, revisionType, userId);
        }).collect(Collectors.toList());
    }

    /**
     * Counts the total number of revisions for a given audited entity class.
     *
     * @param entityClass the audited entity class
     * @return the total number of revisions as a long value
     */
    public long countAllRevisions(Class<?> entityClass) {
        AuditReader auditReader = AuditReaderFactory.get(sessionFactory.getCurrentSession());

        return (long) auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .addProjection(org.hibernate.envers.query.AuditEntity.revisionNumber().count())
                .getSingleResult();
    }

    /**
     * Retrieves a specific revision of an entity by its entity ID and revision number.
     *
     * @param entityClass the class of the audited entity
     * @param entityId    the ID of the entity
     * @param revisionId  the revision number to fetch
     * @param <T>         the type of the audited entity
     * @return the entity instance at the specified revision, or {@code null} if not found
     */
    public <T> T getRevisionById(Class<T> entityClass, int entityId, int revisionId) {
        AuditReader auditReader = AuditReaderFactory.get(sessionFactory.getCurrentSession());
        return auditReader.find(entityClass, entityId, revisionId);
    }

    /**
     * Retrieves a specific {@link AuditEntity} that includes revision metadata for
     * a given entity and revision ID.
     *
     * @param entityClass the class of the audited entity
     * @param entityId    the ID of the entity
     * @param revisionId  the revision number to retrieve
     * @param <T>         the type of the audited entity
     * @return an {@link AuditEntity} containing the entity, revision metadata, and user info
     */
    public <T> AuditEntity<T> getAuditEntityRevisionById(Class<T> entityClass, int entityId, int revisionId) {
        AuditReader auditReader = AuditReaderFactory.get(sessionFactory.getCurrentSession());
        AuditQuery auditQuery = auditReader.createQuery()
                .forRevisionsOfEntity(entityClass, false, true)
                .add(org.hibernate.envers.query.AuditEntity.id().eq(entityId))
                .add(org.hibernate.envers.query.AuditEntity.revisionNumber().eq(revisionId));

        Object[] result = (Object[]) auditQuery.getSingleResult();
        T entity = entityClass.cast(result[0]);
        OpenmrsRevisionEntity revisionEntity = (OpenmrsRevisionEntity) result[1];
        RevisionType revisionType = (RevisionType) result[2];
        Integer userId = revisionEntity.getChangedBy();
        return new AuditEntity<>(entity, revisionEntity, revisionType, userId);
    }
}