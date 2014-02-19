package com.stripe.ctf.instantcodesearch

import java.io._
import java.nio.file._
import scala.sys.process._

import com.twitter.concurrent.Broker

abstract class SearchResult
case class Match(fileno: Int, path: String, line: Int) extends SearchResult
case class Done() extends SearchResult

class Searcher(index : Index)  {
  val root = FileSystems.getDefault().getPath(index.path)

  def search(needle : String, b : Broker[SearchResult]) = {
    //index.findMe(needle,b)
    System.out.println("Searching "+needle+" in "+root.toString())
    //val cwd = Seq("bash","-c","cd",root.toString()," && grep -rn", needle,"| cut -d : -f 1-2")
    val grep : String = "grep -rn "+needle+" "+root.toString()
    val cmd =  grep #| "cut -d : -f 1-2"  #|| "echo nothing found" 
    //val cmd =  grep  #|| "echo nothing found" //lines 
    val searchresults = cmd.!!
    System.out.println(searchresults)
    //val cmd = cwd #|| "echo nothing found" 
    //val lines = cmd.lines
    //searchresults.toList().map ({ line => b !! new Match(1,line.substring(root.toString().length()+1),
     //    index.fileToNumber(line.substring(root.toString().length()+1)))})
    b !! new Done()
  }

}
