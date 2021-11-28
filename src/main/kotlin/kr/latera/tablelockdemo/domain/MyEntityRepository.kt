package kr.latera.tablelockdemo.domain

import org.springframework.data.jpa.repository.JpaRepository

interface MyEntityRepository: JpaRepository<MyEntity, Long>