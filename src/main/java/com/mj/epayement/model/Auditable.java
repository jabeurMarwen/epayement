package com.mj.epayement.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable implements Serializable {

    @CreatedDate
    @Column(updatable = false, name="created_at")
    protected LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at")
    protected LocalDateTime updatedAt;
}
