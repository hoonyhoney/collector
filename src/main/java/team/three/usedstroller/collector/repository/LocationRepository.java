package team.three.usedstroller.collector.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import team.three.usedstroller.collector.domain.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

  List<Location> findByCodeIsNull();
}
