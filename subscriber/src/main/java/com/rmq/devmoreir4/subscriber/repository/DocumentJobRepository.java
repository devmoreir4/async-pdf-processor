package com.rmq.devmoreir4.subscriber.repository;

import com.rmq.devmoreir4.subscriber.model.DocumentJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJobRepository extends JpaRepository<DocumentJob, UUID> {
}
