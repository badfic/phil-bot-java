package com.badfic.philbot.data.phil;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FireDrillPermissionRepository extends JpaRepository<FireDrillPermission, UUID>, JpaSpecificationExecutor<FireDrillPermission> {
}
