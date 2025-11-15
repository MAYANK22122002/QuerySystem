package com.rapidquest.querysystem; // <-- Make sure this package matches yours!

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryRepository extends JpaRepository<Query, Long> {
    // This tells JPA to find all queries and order them by the 'id' field in descending order
      List<Query> findAllByOrderByIdDesc();
}