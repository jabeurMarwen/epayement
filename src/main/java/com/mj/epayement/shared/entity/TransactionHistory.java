package com.mj.epayement.shared.entity;

import javax.persistence.*;

import com.mj.epayement.model.Auditable;
import com.mj.epayement.shared.model.PaymentMethod;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaction_history")
public class TransactionHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id")
    private String application;

    @Column(name = "shop_id")
    private String shopId;

    @Column(name = "shop_password")
    private String shopPassword;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "app_Transaction_id")
    private String appTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "epaiment_provider")
    private PaymentMethod epaimentProvider;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Column(name = "provider_transaction_status")
    private String providerTransactionStatus;

    @Column(name = "provider_id_paiement_client")
    private String providerPayementId;
}
