package com.paterake.lps.address.test

import com.paterake.lps.address.builder.DocumentUtility

object SortTest extends App {
  val clcn = List[String]("Asta", "Asta (Dhartu)")
  val clcnSorted = clcn.sortBy(x => {
    val z = x.replaceAll("[^A-Za-z0-9]", "")
    println(z)
    z
  })
  println(clcnSorted.mkString("\n"))

  println(DocumentUtility.getNameSuffix)

  println(DocumentUtility.getIndexNameDrop())

  println(DocumentUtility.stripNameSuffix("Ramkumari"))

}
