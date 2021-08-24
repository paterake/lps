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

  var address = "The Briars,  254,  Swakeleys Road,  Ickenham,  Uxbridge,  Middlesex,  UB10 8AU"
  address = address.replaceAll("Uxbridge,  Middlesex", "Uxbridge")
  println(address)



}
