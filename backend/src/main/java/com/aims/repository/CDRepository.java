package com.aims.repository;

import com.aims.entity.CD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CDRepository extends JpaRepository<CD, Long> {}
