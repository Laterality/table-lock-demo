package kr.latera.tablelockdemo.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import javax.persistence.LockModeType

interface EntityLockRepository : JpaRepository<EntityLock, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  fun findByName(name: String): EntityLock?
}