package com.rapidquest.querysystem; // <-- Make sure this package matches yours!

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QueryRepository extends JpaRepository<Query, Long> {

      List<Query> findAllByOrderByIdDesc();
}