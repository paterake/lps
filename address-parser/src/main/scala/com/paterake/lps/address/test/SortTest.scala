package com.paterake.lps.address.test

object SortTest extends App {
  val clcn = List[String]("Asta", "Asta (Dhartu)")
  val clcnSorted = clcn.sortBy(x => {
    val z = x.replaceAll("[^A-Za-z0-9]", "")
    println(z)
    z
  })
  println(clcnSorted.mkString("\n"))

}
