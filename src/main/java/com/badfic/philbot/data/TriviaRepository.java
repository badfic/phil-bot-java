package com.badfic.philbot.data;

import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TriviaRepository extends ListCrudRepository<Trivia, UUID> {
}
