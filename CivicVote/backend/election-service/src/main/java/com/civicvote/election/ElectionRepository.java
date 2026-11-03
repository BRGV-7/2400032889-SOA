package com.civicvote.election;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ElectionRepository extends JpaRepository<Election,Long> {}
