package org.acme.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.CardStatusEntity;

@ApplicationScoped
public class CardStatusRepository implements PanacheRepository<CardStatusEntity> {
    public CardStatusEntity findByCardID(Long cardId) {
        return find("cardId", cardId).firstResult();
    }
}
