package com.badfic.philbot.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SnarkyReminderResponseRepository extends JpaRepository<SnarkyReminderResponse, Long>, JpaSpecificationExecutor<SnarkyReminderResponse> {
}
