package com.badfic.philbot.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SwampyGamesConfigRepository extends ListCrudRepository<SwampyGamesConfig, Short> {
}
