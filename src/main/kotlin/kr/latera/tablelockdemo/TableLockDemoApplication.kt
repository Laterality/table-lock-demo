package kr.latera.tablelockdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TableLockDemoApplication

fun main(args: Array<String>) {
	runApplication<TableLockDemoApplication>(*args)
}
