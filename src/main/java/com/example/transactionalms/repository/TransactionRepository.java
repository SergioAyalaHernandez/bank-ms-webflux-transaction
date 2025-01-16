package com.example.transactionalms.repository;

import com.example.transactionalms.model.Transaction;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    @Tailable
    @Query("{}")
    Flux<Transaction> findWithTailableCursor();
}