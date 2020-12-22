package io.ctlove0523.spring.mysql.listeners.inheritance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InheritanceInfoRepository extends JpaRepository<InheritanceInfo, String> {
}
