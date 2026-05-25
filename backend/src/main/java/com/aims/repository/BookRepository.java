// Communication Cohesion
// This repository is dedicated to Book type.
package com.aims.repository;

import com.aims.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {}
