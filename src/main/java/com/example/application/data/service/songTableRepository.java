package com.example.application.data.service;

import com.example.application.data.entity.SongTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface songTableRepository
        extends
            JpaRepository<SongTable, Long>,
            JpaSpecificationExecutor<SongTable> {

}
