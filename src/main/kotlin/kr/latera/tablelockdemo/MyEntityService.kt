package kr.latera.tablelockdemo

import kr.latera.tablelockdemo.domain.EntityLockRepository
import kr.latera.tablelockdemo.domain.MyEntity
import kr.latera.tablelockdemo.domain.MyEntityRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class MyEntityService(
  private val repo: MyEntityRepository,
  private val lockRepo: EntityLockRepository,
  private val eventPublisher: ApplicationEventPublisher,
) {

  private val logger = LoggerFactory.getLogger(javaClass)

  @Transactional
  fun insert(id: Long, lock: Boolean) {
    if (lock) {
      lockRepo.findByName("my_entity")
      logger.info("Locked")
    }

    repo.save(MyEntity(id))
    eventPublisher.publishEvent(CommittedEvent(id))
  }

  @Transactional(readOnly = true)
  fun getAll(): List<MyEntity> =
    repo.findAll()

  @TransactionalEventListener(CommittedEvent::class, phase = TransactionPhase.AFTER_COMMIT)
  fun listenTransactionalEvent(event: CommittedEvent) {
    logger.info("Committed with: {}", event.id)
  }
}

data class CommittedEvent(
  val id: Long
)