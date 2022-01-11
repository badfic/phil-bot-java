package com.badfic.philbot.data.hungergames;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PronounRepository extends JpaRepository<Pronoun, Long>, JpaSpecificationExecutor<Pronoun> {
}
