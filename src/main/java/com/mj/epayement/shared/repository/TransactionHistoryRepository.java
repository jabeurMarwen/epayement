package com.mj.epayement.shared.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mj.epayement.shared.entity.TransactionHistory;
import com.mj.epayement.shared.model.PaymentMethod;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    /**
     * find Transaction History By AppTransactionId and EpaimentProvider
     *
     * @param appTransactionId @{@link String}
     * @return @{@link TransactionHistory}
     */
    @Query("SELECT transactionHistory FROM TransactionHistory AS transactionHistory " +
            "WHERE transactionHistory.appTransactionId = :appTransactionId " +
            "AND transactionHistory.epaimentProvider = :provider")
    TransactionHistory findTransactionHistoryByAppTransactionIdAndEpaimentProvider(@Param("appTransactionId") String appTransactionId,
                                                                                   @Param("provider") PaymentMethod provider);
}
