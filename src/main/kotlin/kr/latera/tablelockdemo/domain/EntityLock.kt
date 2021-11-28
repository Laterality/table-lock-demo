package kr.latera.tablelockdemo.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "entity_lock")
class EntityLock(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long,
  val name: String,
)
