package by.mpkrepak.btkatsserver.repository;

import by.mpkrepak.btkatsserver.domain.Record;
import by.mpkrepak.btkatsserver.dto.DataDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordsRepository extends JpaRepository<Record, Long> {
    Optional<Record> findFirstByOrderByIdDesc();
    Page<Record> findAll(Pageable pageable);

    List<Record> findByCallReceiverAndRecordDateBetween(String receiver, long startDate, long endDate);
}
