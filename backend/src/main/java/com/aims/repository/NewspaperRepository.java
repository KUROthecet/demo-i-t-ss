/*
Communication Cohesion
This repository is dedicated to Newspaper type.
*/

/*
SOLID Violations: None
*/

package com.aims.repository;

import com.aims.entity.Newspaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewspaperRepository extends JpaRepository<Newspaper, Long> {}
