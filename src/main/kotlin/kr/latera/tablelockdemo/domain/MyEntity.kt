package kr.latera.tablelockdemo.domain

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "my_entity")
class MyEntity(
  @Id
  val id: Long
)
