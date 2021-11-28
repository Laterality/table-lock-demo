package kr.latera.tablelockdemo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/myentity")
class MyController(
  private val service: MyEntityService
) {
  @PostMapping
  fun insertSome(
    @RequestParam id: Long,
    @RequestParam(required = false, defaultValue = "false") lock: Boolean,
  ) {
    service.insert(id, lock)
  }

  @GetMapping
  fun getAll() = service.getAll()
}